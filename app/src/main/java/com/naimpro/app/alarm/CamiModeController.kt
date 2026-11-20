package com.naimpro.app.alarm

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.naimpro.app.data.prefs.CamiModeFilter

/**
 * "Cami modu": optionally switches the phone to a quieter interruption filter
 * for a few minutes after a prayer time arrives.
 *
 * The filter is deliberately `ALARMS` (or `PRIORITY`) instead of `NONE`: a full
 * silence would mute the adhan that is playing at the very same moment. The
 * previous filter is restored afterwards, and [reconcile] repairs the restore
 * alarm if the process or the device died in between.
 */
object CamiModeController {
    private const val TAG = "CamiMode"
    private const val REQUEST_RESTORE = 1201
    const val MIN_MINUTES = 5
    const val MAX_MINUTES = 60

    fun canControlDnd(context: Context): Boolean {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return false
        return manager.isNotificationPolicyAccessGranted
    }

    fun enableFor(
        context: Context,
        minutes: Int,
        filter: CamiModeFilter = CamiModeFilter.ALARMS,
    ) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (!manager.isNotificationPolicyAccessGranted) {
            Log.w(TAG, "Notification policy access not granted; skipping silent mode")
            return
        }
        val prefs = AlarmPrefs(context)
        val current = manager.currentInterruptionFilter
        prefs.previousInterruptionFilter = current
        runCatching {
            // Never request NONE: the adhan uses USAGE_ALARM and must stay audible.
            val target =
                if (filter.interruptionFilter == CamiModeFilter.INTERRUPTION_FILTER_ALARMS) {
                    NotificationManager.INTERRUPTION_FILTER_ALARMS
                } else {
                    NotificationManager.INTERRUPTION_FILTER_PRIORITY
                }
            manager.setInterruptionFilter(target)
        }.onFailure { Log.w(TAG, "Unable to enable DND", it) }
        scheduleRestore(context, minutes.coerceIn(MIN_MINUTES, MAX_MINUTES))
    }

    fun restore(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val prefs = AlarmPrefs(context)
        val previous = prefs.previousInterruptionFilter
        if (previous >= 0 && manager.isNotificationPolicyAccessGranted) {
            runCatching { manager.setInterruptionFilter(previous) }
        }
        prefs.previousInterruptionFilter = -1
        prefs.dndRestoreAt = 0L
    }

    /**
     * Repairs the restore alarm after a reboot or when the app process was
     * killed while silent mode was active.
     */
    fun reconcile(context: Context) {
        val prefs = AlarmPrefs(context)
        val restoreAt = prefs.dndRestoreAt
        if (restoreAt <= 0L) return
        val remaining = restoreAt - System.currentTimeMillis()
        if (remaining <= 0L) {
            restore(context)
        } else {
            scheduleRestoreAt(context, restoreAt)
        }
    }

    private fun scheduleRestore(
        context: Context,
        minutes: Int,
    ) {
        val at = System.currentTimeMillis() + minutes * 60_000L
        AlarmPrefs(context).dndRestoreAt = at
        scheduleRestoreAt(context, at)
    }

    private fun scheduleRestoreAt(
        context: Context,
        at: Long,
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pending =
            PendingIntent.getBroadcast(
                context,
                REQUEST_RESTORE,
                Intent(context, PrayerAlarmReceiver::class.java).apply {
                    action = PrayerAlarmReceiver.ACTION_RESTORE_DND
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending)
        }
    }
}
