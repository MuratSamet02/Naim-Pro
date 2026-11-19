package com.naimpro.app.alarm

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.naimpro.app.NaimApplication
import com.naimpro.app.widget.WidgetUpdater
import java.util.concurrent.TimeUnit

/**
 * Safety net for the alarm chain.
 *
 * Android may drop an exact alarm when the device is very low on memory, when
 * the OEM battery manager is aggressive, or when the user swipes the app away.
 * A WorkManager job that runs shortly after the expected alarm time repairs the
 * schedule; a 6 hourly periodic job keeps it healthy over long periods.
 */
class RescheduleWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = NaimApplication.container(applicationContext)
        container.alarms.reschedule()
        WidgetUpdater.refreshAll(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME_SAFETY = "naim_reschedule_safety"
        private const val WORK_NAME_PERIODIC = "naim_reschedule_periodic"

        fun scheduleSafetyNet(
            context: Context,
            triggerAtMillis: Long,
        ) {
            val delay =
                (triggerAtMillis - System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2))
                    .coerceAtLeast(0L)
            val request =
                OneTimeWorkRequestBuilder<RescheduleWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setConstraints(Constraints.NONE)
                    .build()
            WorkManager.getInstance(context.applicationContext)
                .enqueueUniqueWork(WORK_NAME_SAFETY, ExistingWorkPolicy.REPLACE, request)
        }

        fun schedulePeriodic(context: Context) {
            val request =
                PeriodicWorkRequestBuilder<RescheduleWorker>(6, TimeUnit.HOURS)
                    .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
