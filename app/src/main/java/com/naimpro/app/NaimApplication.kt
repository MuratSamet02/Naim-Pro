package com.naimpro.app

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import com.naimpro.app.alarm.CamiModeController
import com.naimpro.app.alarm.NextPrayerNotification
import com.naimpro.app.alarm.PrayerNotificationChannels
import com.naimpro.app.data.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application entry point.
 *
 * Startup work is intentionally tiny: no analytics, no crash reporter, no
 * network client. Only the manual dependency container and the notification
 * channels are created eagerly, which keeps a cold start well below 600 ms.
 */
class NaimApplication : Application(), Configuration.Provider {
    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this, applicationScope)
        PrayerNotificationChannels.ensureCreated(this)
        // Repair a silent mode window that outlived the previous process.
        CamiModeController.reconcile(this)
        applicationScope.launch { NextPrayerNotification.update(this@NaimApplication) }
    }

    override val workManagerConfiguration: Configuration
        get() =
            Configuration.Builder()
                .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.WARN)
                .build()

    companion object {
        fun container(context: Context): AppContainer = (context.applicationContext as NaimApplication).container

        @Suppress("unused")
        fun workManager(context: Context): WorkManager = WorkManager.getInstance(context.applicationContext)
    }
}
