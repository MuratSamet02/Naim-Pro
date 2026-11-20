package com.naimpro.app.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.naimpro.app.NaimApplication
import com.naimpro.app.R
import com.naimpro.app.data.prefs.NotificationMode
import com.naimpro.app.domain.hijri.HijriCalendar
import com.naimpro.app.domain.model.Prayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Receives the prayer, reminder and religious day alarms.
 *
 * The receiver itself stays short: it shows the notification, asks the short
 * lived playback service to play the sound, optionally enables cami mode and
 * schedules the next alarm. The audio is intentionally handed to
 * [EzanPlaybackService] so a long adhan survives the receiver returning.
 */
class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        when (intent.action) {
            ACTION_STOP_AUDIO -> {
                EzanPlaybackService.stop(context)
                return
            }
            ACTION_RESTORE_DND -> {
                CamiModeController.restore(context)
                return
            }
            ACTION_EVENT -> {
                handleEvent(context, intent)
                return
            }
        }

        val prayerName = intent.getStringExtra(EXTRA_PRAYER)
        val prayer = Prayer.entries.firstOrNull { it.name == prayerName } ?: return
        val isReminder = intent.action == ACTION_REMINDER
        val triggerAt = intent.getLongExtra(EXTRA_TRIGGER_AT, System.currentTimeMillis())

        val pendingResult = goAsync()
        val container = NaimApplication.container(context)
        val scope = CoroutineScope(Dispatchers.Default)

        scope.launch {
            try {
                val settings = container.settings.current()
                val location = container.locations.current()
                val prefs = AlarmPrefs(context)
                val zone = location?.zoneId ?: ZoneId.systemDefault()
                val time = Instant.ofEpochMilli(triggerAt).atZone(zone).toLocalDateTime()

                if (isReminder) {
                    PrayerNotifications.showReminder(
                        context,
                        prayer,
                        intent.getIntExtra(EXTRA_REMINDER_MINUTES, settings.preReminderMinutes),
                        time,
                    )
                } else {
                    val hijri = HijriCalendar.fromGregorian(LocalDate.now(zone), settings.hicriOffsetDays)
                    val hijriLabel =
                        withContext(Dispatchers.Default) {
                            "${hijri.day} ${HijriCalendar.monthName(hijri.month, settings.language.tag)} ${hijri.year}"
                        }
                    if (settings.notificationEnabled) {
                        PrayerNotifications.showPrayer(
                            context = context,
                            prayer = prayer,
                            time = time,
                            locationName = location?.shortLabel.orEmpty(),
                            hijriLabel = hijriLabel,
                        )
                    }

                    val mode = settings.notificationModes[prayer] ?: NotificationMode.SILENT
                    playAdhan(context, prayer, mode, settings.ezanVolumePercent, settings.vibrationEnabled)
                    prefs.lastAlarmAt = triggerAt
                    prefs.lastAlarmPrayer = prayer.name

                    if (settings.camiModeEnabled) {
                        CamiModeController.enableFor(
                            context,
                            settings.camiModeMinutes,
                            settings.camiModeFilter,
                        )
                    }
                }

                container.alarms.reschedule()
                NextPrayerNotification.update(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * Tries the foreground service first. On Android 12+ the platform may refuse
     * an FGS start; in that case the sound is played directly, which still works
     * while the process lives and is better than silence.
     */
    private fun playAdhan(
        context: Context,
        prayer: Prayer,
        mode: NotificationMode,
        volumePercent: Int,
        vibrationEnabled: Boolean,
    ) {
        if (mode == NotificationMode.SILENT) return
        val started =
            EzanPlaybackService.start(
                context = context,
                prayer = prayer,
                mode = mode,
                volumePercent = volumePercent,
                vibrationEnabled = vibrationEnabled,
            )
        if (!started) {
            EzanPlayer.play(
                context = context,
                mode = mode,
                volumePercent = volumePercent,
                customUri = AlarmPrefs(context).ezanUriFor(prayer),
                vibrationEnabled = vibrationEnabled,
            )
        }
    }

    private fun handleEvent(
        context: Context,
        intent: Intent,
    ) {
        val key = intent.getStringExtra(EXTRA_EVENT_KEY) ?: return
        val triggerAt = intent.getLongExtra(EXTRA_TRIGGER_AT, System.currentTimeMillis())
        val pendingResult = goAsync()
        val container = NaimApplication.container(context)
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settings = container.settings.current()
                val zone = container.locations.current()?.zoneId ?: ZoneId.systemDefault()
                val eventDate = Instant.ofEpochMilli(triggerAt).atZone(zone).toLocalDate().plusDays(1)
                val formatted =
                    eventDate.format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.getDefault()),
                    )
                PrayerNotifications.showEvent(
                    context = context,
                    title = context.getString(R.string.notification_event_title),
                    body =
                        context.getString(
                            R.string.notification_event_body,
                            context.getString(EventLabels.titleRes(key)),
                            formatted,
                        ),
                )
                container.alarms.scheduleReligiousDay(settings, container.locations.current() ?: return@launch)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_PRAYER = "com.naimpro.app.action.PRAYER_TIME"
        const val ACTION_REMINDER = "com.naimpro.app.action.PRAYER_REMINDER"
        const val ACTION_STOP_AUDIO = "com.naimpro.app.action.STOP_AUDIO"
        const val ACTION_RESTORE_DND = "com.naimpro.app.action.RESTORE_DND"
        const val ACTION_EVENT = "com.naimpro.app.action.RELIGIOUS_EVENT"
        const val EXTRA_PRAYER = "extra_prayer"
        const val EXTRA_TRIGGER_AT = "extra_trigger_at"
        const val EXTRA_REMINDER_MINUTES = "extra_reminder_minutes"
        const val EXTRA_EVENT_KEY = "extra_event_key"

        /** Kept for compatibility with the notification manager helpers. */
        const val CAMI_MODE_MINUTES = 15

        @Suppress("unused")
        private val NOTIFICATION_MANAGER_HINT = NotificationManager::class.java
    }
}
