package com.naimpro.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.naimpro.app.NaimApplication
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

/**
 * Receives the prayer time and reminder alarms.
 *
 * The work is intentionally short: show the notification, play a few seconds of
 * audio, optionally enable silent mode, then schedule the next alarm. No
 * foreground service is ever started.
 */
class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action == ACTION_STOP_AUDIO) {
            EzanPlayer.stop()
            return
        }
        if (intent.action == ACTION_RESTORE_DND) {
            CamiModeController.restore(context)
            return
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
                        settings.preReminderMinutes,
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
                    EzanPlayer.play(
                        context = context,
                        mode = mode,
                        volumePercent = settings.ezanVolumePercent,
                        customUri = prefs.customEzanUri,
                        vibrationEnabled = settings.vibrationEnabled,
                    )
                    prefs.lastAlarmAt = triggerAt
                    prefs.lastAlarmPrayer = prayer.name

                    if (settings.camiModeEnabled) {
                        CamiModeController.enableFor(context, CAMI_MODE_MINUTES)
                    }
                }

                container.alarms.reschedule()
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
        const val EXTRA_PRAYER = "extra_prayer"
        const val EXTRA_TRIGGER_AT = "extra_trigger_at"
        const val CAMI_MODE_MINUTES = 15
    }
}
