package com.naimpro.app.alarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.naimpro.app.MainActivity
import com.naimpro.app.R
import com.naimpro.app.data.prefs.NotificationMode
import com.naimpro.app.domain.model.Prayer

/**
 * Short lived `mediaPlayback` foreground service that keeps a long adhan alive.
 *
 * Android 8+ kills ordinary background work shortly after the receiver returns,
 * which used to cut a 2–4 minute adhan in half. This service starts when the
 * alarm fires, posts its own "stop" notification and terminates itself as soon
 * as the sound ends. There is no long running background service: the app is
 * only foreground while audio is actually playing.
 */
class EzanPlaybackService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            ACTION_PLAY -> beginPlayback(intent)
            ACTION_STOP -> stopPlayback()
            else -> stopPlayback()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        EzanPlayer.stop()
        super.onDestroy()
    }

    private fun beginPlayback(intent: Intent) {
        val prayer = Prayer.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_PRAYER) }
        val mode = NotificationMode.fromKey(intent.getStringExtra(EXTRA_MODE))
        startForeground(NOTIFICATION_ID, buildNotification(prayer))
        val prefs = AlarmPrefs(this)
        EzanPlayer.play(
            context = this,
            mode = mode,
            volumePercent = intent.getIntExtra(EXTRA_VOLUME, 80),
            customUri = prayer?.let { prefs.ezanUriFor(it) },
            vibrationEnabled = intent.getBooleanExtra(EXTRA_VIBRATE, true),
            onFinished = { stopPlayback() },
        )
        if (!EzanPlayer.isPlaying() && mode != NotificationMode.VIBRATE) {
            // Nothing to keep alive (silent mode, unreadable file, ...).
            stopPlayback()
        }
    }

    private fun stopPlayback() {
        EzanPlayer.stop()
        runCatching { stopForeground(STOP_FOREGROUND_REMOVE) }
        stopSelf()
    }

    private fun buildNotification(prayer: Prayer?): Notification {
        val openIntent =
            PendingIntent.getActivity(
                this,
                OPEN_REQUEST_CODE,
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val stopIntent =
            PendingIntent.getService(
                this,
                STOP_REQUEST_CODE,
                Intent(this, EzanPlaybackService::class.java).apply { action = ACTION_STOP },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val title =
            prayer?.let { getString(R.string.notification_playing_title, PrayerNotifications.prayerName(this, it)) }
                ?: getString(R.string.notification_playing_generic)
        return NotificationCompat.Builder(this, PrayerNotificationChannels.CHANNEL_PLAYBACK)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(getString(R.string.notification_playing_body))
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openIntent)
            .addAction(0, getString(R.string.action_stop_audio), stopIntent)
            .build()
    }

    companion object {
        const val ACTION_PLAY = "com.naimpro.app.action.PLAY_EZAN"
        const val ACTION_STOP = "com.naimpro.app.action.STOP_EZAN_SERVICE"
        const val EXTRA_PRAYER = "extra_prayer"
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_VOLUME = "extra_volume"
        const val EXTRA_VIBRATE = "extra_vibrate"

        private const val NOTIFICATION_ID = 3001
        private const val OPEN_REQUEST_CODE = 33
        private const val STOP_REQUEST_CODE = 34

        /** Starts the service; returns false when the platform refuses an FGS start. */
        fun start(
            context: Context,
            prayer: Prayer,
            mode: NotificationMode,
            volumePercent: Int,
            vibrationEnabled: Boolean,
        ): Boolean {
            val intent =
                Intent(context, EzanPlaybackService::class.java).apply {
                    action = ACTION_PLAY
                    putExtra(EXTRA_PRAYER, prayer.name)
                    putExtra(EXTRA_MODE, mode.name)
                    putExtra(EXTRA_VOLUME, volumePercent)
                    putExtra(EXTRA_VIBRATE, vibrationEnabled)
                }
            return runCatching {
                androidx.core.content.ContextCompat.startForegroundService(context, intent)
                true
            }.getOrElse { error ->
                Log.w(TAG, "Foreground playback not allowed; falling back", error)
                false
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, EzanPlaybackService::class.java).apply { action = ACTION_STOP }
            runCatching { context.startService(intent) }
            runCatching { context.stopService(Intent(context, EzanPlaybackService::class.java)) }
        }

        private const val TAG = "EzanPlayback"
    }
}
