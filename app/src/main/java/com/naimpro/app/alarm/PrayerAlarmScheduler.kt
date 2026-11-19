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

/**
 * Schedules exactly one upcoming prayer alarm (plus an optional reminder).
 *
 * Only a single pending alarm exists at any time, which keeps the alarm table
 * tiny and means Doze has almost nothing to maintain. After a prayer fires the
 * receiver immediately reschedules the next one, and [RescheduleWorker] acts as
 * a safety net if the process was killed.
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
                return@withLock
            }
            scheduleInternal(settings, location)
        }

    suspend fun scheduleInternal(
        settings: AppSettings,
        location: Location,
    ) {
        val now = LocalDateTime.now(location.zoneId)
        val next = nextPrayer(now, location, settings) ?: return
        val triggerAt = next.second.atZone(location.zoneId).toInstant().toEpochMilli()

        setAlarm(triggerAt, next.first, isReminder = false)

        if (settings.preReminderEnabled && settings.preReminderMinutes > 0) {
            val reminderAt = triggerAt - settings.preReminderMinutes * 60_000L
            if (reminderAt > System.currentTimeMillis()) {
                setAlarm(reminderAt, next.first, isReminder = true)
            }
        }

        RescheduleWorker.scheduleSafetyNet(appContext, triggerAt)
    }

    /** Exposed for the widget and the "next prayer" card. */
    suspend fun nextPrayer(
        now: LocalDateTime,
        location: Location,
        settings: AppSettings,
    ): Pair<Prayer, LocalDateTime>? {
        val today = prayers.timesFor(now.toLocalDate(), location, settings)
        val candidate = nextAfter(today, now)
        if (candidate != null) return candidate
        val tomorrow = prayers.timesFor(now.toLocalDate().plusDays(1), location, settings)
        return nextAfter(tomorrow, now)
    }

    suspend fun timesFor(
        date: LocalDate,
        location: Location,
        settings: AppSettings,
    ): PrayerTimes = prayers.timesFor(date, location, settings)

    private fun nextAfter(
        times: PrayerTimes,
        now: LocalDateTime,
    ): Pair<Prayer, LocalDateTime>? = times.asList().firstOrNull { it.second.isAfter(now) }

    private fun setAlarm(
        triggerAt: Long,
        prayer: Prayer,
        isReminder: Boolean,
    ) {
        val manager = alarmManager ?: return
        val intent =
            Intent(appContext, PrayerAlarmReceiver::class.java).apply {
                action = if (isReminder) PrayerAlarmReceiver.ACTION_REMINDER else PrayerAlarmReceiver.ACTION_PRAYER
                putExtra(PrayerAlarmReceiver.EXTRA_PRAYER, prayer.name)
                putExtra(PrayerAlarmReceiver.EXTRA_TRIGGER_AT, triggerAt)
            }
        val pending =
            PendingIntent.getBroadcast(
                appContext,
                if (isReminder) REQUEST_REMINDER else REQUEST_PRAYER,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

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
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            }
        }.onFailure {
            manager.set(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    fun cancelAll() {
        val manager = alarmManager ?: return
        listOf(REQUEST_PRAYER, REQUEST_REMINDER).forEach { requestCode ->
            val intent = Intent(appContext, PrayerAlarmReceiver::class.java)
            val pending =
                PendingIntent.getBroadcast(
                    appContext,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
                )
            if (pending != null) {
                manager.cancel(pending)
                pending.cancel()
            }
        }
    }

    /** Debug helper used by the emulator verification script. */
    fun pendingAlarmCount(): Int {
        val manager = alarmManager ?: return 0
        return listOf(REQUEST_PRAYER, REQUEST_REMINDER).count { requestCode ->
            PendingIntent.getBroadcast(
                appContext,
                requestCode,
                Intent(appContext, PrayerAlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            ) != null
        }
    }

    companion object {
        const val REQUEST_PRAYER = 1101
        const val REQUEST_REMINDER = 1102

        fun epochToLocal(
            epochMillis: Long,
            zone: ZoneId,
        ): LocalDateTime = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDateTime()
    }
}
