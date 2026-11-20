package com.naimpro.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.naimpro.app.NaimApplication
import com.naimpro.app.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Restores every scheduled alarm after a reboot, a manual clock change, a
 * timezone change, a language change or an app update.
 *
 * Android drops all alarms on these events, so this receiver is the single
 * place that makes the schedule self healing.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            ACTION_EXACT_ALARM_PERMISSION_CHANGED,
            -> reschedule(context)
        }
    }

    private fun reschedule(context: Context) {
        val pendingResult = goAsync()
        val container = NaimApplication.container(context)
        CoroutineScope(Dispatchers.Default).launch {
            try {
                container.alarms.reschedule()
                CamiModeController.reconcile(context)
                NextPrayerNotification.update(context)
                WidgetUpdater.refreshAll(context)
                RescheduleWorker.schedulePeriodic(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_EXACT_ALARM_PERMISSION_CHANGED =
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
    }
}
