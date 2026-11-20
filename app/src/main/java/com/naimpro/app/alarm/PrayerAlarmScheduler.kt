package com.naimpro.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.naimpro.app.data.prefs.AppSettings
import com.naimpro.app.data.prefs.SettingsRepository
import com.naimpro.app.data.repo.LocationRepository
import com.naimpro.app.data.repo.PrayerRepository
import com.naimpro.app.domain.alarm.AlarmPlanner
import com.naimpro.app.domain.hijri.EventReminder
import com.naimpro.app.domain.hijri.IslamicEvents
import com.naimpro.app.domain.model.Location
import com.naimpro.app.domain.model.Prayer
import com.naimpro.app.domain.model.PrayerTimes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Keeps the alarm chain alive with at most three pending alarms: the next prayer,
 * its pre reminder and one religious day reminder.
 *
 * The scheduling rules live in [AlarmPlanner] (pure Kotlin, unit tested); this
 * class only translates the plan into `AlarmManager` calls. After a prayer fires
 * the receiver immediately reschedules the next one, and [RescheduleWorker] acts
 * as a safety net if the process was killed.
 */
class PrayerAlarmScheduler(
    context: Context,
    private val prayers: PrayerRepository,
    private val settingsRepository: SettingsRepository,
    private val locationRepository: LocationRepository,
    private val scope: CoroutineScope,
) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)
    private val mutex = Mutex()

    /** Fire-and-forget reschedule used from UI callbacks. */
    fun rescheduleAsync() {
        scope.launch { reschedule() }
    }

    suspend fun reschedule() =
        mutex.withLock {
            val settings = settingsRepository.current()
            val location = locationRepository.current() ?: return@withLock
            if (!settings.notificationEnabled) {
                cancelAll()
                NextPrayerNotification.update(appContext)
                return@withLock
            }
            scheduleInternal(settings, location)
            NextPrayerNotification.update(appContext)
        }

    suspend fun scheduleInternal(
        settings: AppSettings,
        location: Location,
    ) {
        val zone = location.zoneId
        val now = LocalDateTime.now(zone)
        val today = prayers.timesFor(now.toLocalDate(), location, settings)
        val tomorrow = prayers.timesFor(now.toLocalDate().plusDays(1), location, settings)

        val plan =
            AlarmPlanner.plan(
                now = now,
                today = today,
                tomorrow = tomorrow,
                preReminderEnabled = settings.preReminderEnabled,
                defaultReminderMinutes = settings.preReminderMinutes,
                perPrayerReminderMinutes = settings.perPrayerReminderMinutes,
            )

        val prayerAlarm = plan.prayer
        if (prayerAlarm == null) {
            cancelPrayer(REQUEST_PRAYER, PrayerAlarmReceiver.ACTION_PRAYER)
        } else {
            setAlarm(
                triggerAt = prayerAlarm.epochMillis(zone),
                prayer = prayerAlarm.prayer,
                isReminder = false,
            )
            plan.safetyNetAt?.let {
                RescheduleWorker.scheduleSafetyNet(appContext, it.atZone(zone).toInstant().toEpochMilli())
            }
        }

        val reminderAlarm = plan.reminder
        if (reminderAlarm == null) {
            cancelPrayer(REQUEST_REMINDER, PrayerAlarmReceiver.ACTION_REMINDER)
        } else {
            setAlarm(
                triggerAt = reminderAlarm.epochMillis(zone),
                prayer = reminderAlarm.prayer,
                isReminder = true,
                reminderMinutes =
                    settings.perPrayerReminderMinutes[reminderAlarm.prayer]
                        ?: settings.preReminderMinutes,
            )
        }

        scheduleReligiousDay(settings, location)
    }

    /**
     * Schedules the evening-before reminder for the next religious day.
     * Only one event alarm is kept pending at a time.
     */
    suspend fun scheduleReligiousDay(
        settings: AppSettings,
        location: Location,
    ) {
        if (!settings.diniGunNotifications) {
            cancelEvent()
            return
        }
        val zone = location.zoneId
        val now = ZonedDateTime.now(zone)
        val events = IslamicEvents.upcoming(now.toLocalDate(), EVENT_LOOKAHEAD, settings.hicriOffsetDays)
        val next = EventReminder.next(now, events, zone)
        if (next == null) {
            cancelEvent()
            return
        }
        val manager = alarmManager ?: return
        val triggerAt = next.second.toInstant().toEpochMilli()
        val pending =
            PendingIntent.getBroadcast(
                appContext,
                REQUEST_EVENT,
                Intent(appContext, PrayerAlarmReceiver::class.java).apply {
                    action = PrayerAlarmReceiver.ACTION_EVENT
                    putExtra(PrayerAlarmReceiver.EXTRA_EVENT_KEY, next.first.key)
                    putExtra(PrayerAlarmReceiver.EXTRA_TRIGGER_AT, triggerAt)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        setRawAlarm(manager, triggerAt, pending)
    }

    /** Exposed for the widget and the "next prayer" card. */
    suspend fun nextPrayer(
        now: LocalDateTime,
        location: Location,
        settings: AppSettings,
    ): Pair<Prayer, LocalDateTime>? {
        val today = prayers.timesFor(now.toLocalDate(), location, settings)
        AlarmPlanner.nextPrayer(now, today)?.let { return it }
        val tomorrow = prayers.timesFor(now.toLocalDate().plusDays(1), location, settings)
        return AlarmPlanner.nextPrayer(now, tomorrow)
    }

    suspend fun timesFor(
        date: LocalDate,
        location: Location,
        settings: AppSettings,
    ): PrayerTimes = prayers.timesFor(date, location, settings)

    private fun setAlarm(
        triggerAt: Long,
        prayer: Prayer,
        isReminder: Boolean,
        reminderMinutes: Int = 0,
    ) {
        val manager = alarmManager ?: return
        val intent =
            Intent(appContext, PrayerAlarmReceiver::class.java).apply {
                action = if (isReminder) PrayerAlarmReceiver.ACTION_REMINDER else PrayerAlarmReceiver.ACTION_PRAYER
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER, prayer.name)
                putExtra(PrayerAlarmReceiver.EXTRA_TRIGGER_AT, triggerAt)
                if (isReminder) putExtra(PrayerAlarmReceiver.EXTRA_REMINDER_MINUTES, reminderMinutes)
            }
        val pending =
            PendingIntent.getBroadcast(
                appContext,
                if (isReminder) REQUEST_REMINDER else REQUEST_PRAYER,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        setRawAlarm(manager, triggerAt, pending)
    }

    private fun setRawAlarm(
        manager: AlarmManager,
        triggerAt: Long,
        pending: PendingIntent,
    ) {
        val canExact =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                manager.canScheduleExactAlarms()
            } else {
                true
            }
        runCatching {
            if (canExact) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else {
                // Without the exact alarm permission a hair trigger alarm is the
                // closest the platform allows; times may be a few minutes late.
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            }
        }.onFailure {
            manager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    fun cancelAll() {
        cancelPrayer(REQUEST_PRAYER, PrayerAlarmReceiver.ACTION_PRAYER)
        cancelPrayer(REQUEST_REMINDER, PrayerAlarmReceiver.ACTION_REMINDER)
        cancelEvent()
    }

    private fun cancelPrayer(
        requestCode: Int,
        action: String,
    ) {
        val manager = alarmManager ?: return
        val pending =
            PendingIntent.getBroadcast(
                appContext,
                requestCode,
                Intent(appContext, PrayerAlarmReceiver::class.java).apply { this.action = action },
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
        if (pending != null) {
            manager.cancel(pending)
            pending.cancel()
        }
    }

    private fun cancelEvent() {
        val manager = alarmManager ?: return
        val pending =
            PendingIntent.getBroadcast(
                appContext,
                REQUEST_EVENT,
                Intent(appContext, PrayerAlarmReceiver::class.java).apply {
                    action = PrayerAlarmReceiver.ACTION_EVENT
                },
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
        if (pending != null) {
            manager.cancel(pending)
            pending.cancel()
        }
    }

    /** Debug helper used by the emulator verification script. */
    fun pendingAlarmCount(): Int = listOf(REQUEST_PRAYER, REQUEST_REMINDER, REQUEST_EVENT).count(::isPending)

    /** True when an alarm with [requestCode] is currently registered. */
    fun isPending(requestCode: Int): Boolean =
        PendingIntent.getBroadcast(
            appContext,
            requestCode,
            Intent(appContext, PrayerAlarmReceiver::class.java).apply {
                action =
                    when (requestCode) {
                        REQUEST_REMINDER -> PrayerAlarmReceiver.ACTION_REMINDER
                        REQUEST_EVENT -> PrayerAlarmReceiver.ACTION_EVENT
                        else -> PrayerAlarmReceiver.ACTION_PRAYER
                    }
            },
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) != null

    companion object {
        const val REQUEST_PRAYER = 1101
        const val REQUEST_REMINDER = 1102
        const val REQUEST_EVENT = 1103
        private const val EVENT_LOOKAHEAD = 8

        fun epochToLocal(
            epochMillis: Long,
            zone: ZoneId,
        ): LocalDateTime = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDateTime()
    }
}
