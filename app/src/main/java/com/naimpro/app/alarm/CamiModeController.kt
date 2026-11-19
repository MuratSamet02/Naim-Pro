package com.naimpro.app.alarm

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * "Cami modu": optionally switch the phone to silent (Do Not Disturb) for a few
 * minutes when a prayer time arrives.
 *
 * This is strictly opt-in, requires the notification policy access the user
 * grants explicitly, and always restores the previous interruption filter.
 */
object CamiModeController {
    private const val TAG = "CamiMode"
    private const val REQUEST_RESTORE = 1201

    fun canControlDnd(context: Context): Boolean {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return false
        return manager.isNotificationPolicyAccessGranted
    }

    fun enableFor(
        context: Context,
        minutes: Int,
    ) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (!manager.isNotificationPolicyAccessGranted) {
            Log.w(TAG, "Notification policy access not granted; skipping silent mode")
            return
        }
        val prefs = AlarmPrefs(context)
        prefs.previousInterruptionFilter = manager.currentInterruptionFilter
        runCatching {
            manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        }.onFailure { Log.w(TAG, "Unable to enable DND", it) }
        scheduleRestore(context, minutes)
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

    private fun scheduleRestore(
        context: Context,
        minutes: Int,
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val at = System.currentTimeMillis() + minutes * 60_000L
        AlarmPrefs(context).dndRestoreAt = at
        val pending =
            android.app.PendingIntent.getBroadcast(
                context,
                REQUEST_RESTORE,
                Intent(context, PrayerAlarmReceiver::class.java).apply {
                    action = PrayerAlarmReceiver.ACTION_RESTORE_DND
                },
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
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
