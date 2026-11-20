package com.naimpro.app.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import com.naimpro.app.R
import com.naimpro.app.domain.model.Prayer

/**
 * Notification channels. All channels are intentionally silent: the adhan audio
 * is played by [EzanPlayer] so that the user's chosen volume, chosen sound file
 * and "stop" action all behave predictably on every OEM.
 */
object PrayerNotificationChannels {
    const val CHANNEL_PRAYER = "prayer_time_v1"
    const val CHANNEL_REMINDER = "prayer_reminder_v1"
    const val CHANNEL_EVENTS = "religious_days_v1"
    const val CHANNEL_INFO = "app_info_v1"
    const val CHANNEL_PLAYBACK = "adhan_playback_v1"

    fun ensureCreated(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val silent =
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_PRAYER,
                    context.getString(R.string.channel_prayer_name),
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = context.getString(R.string.channel_prayer_desc)
                    setSound(null, null)
                    enableVibration(false)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                },
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_REMINDER,
                    context.getString(R.string.channel_reminder_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = context.getString(R.string.channel_reminder_desc)
                    setSound(null, silent)
                    enableVibration(true)
                },
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_EVENTS,
                    context.getString(R.string.channel_events_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = context.getString(R.string.channel_events_desc)
                    setSound(null, null)
                },
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_INFO,
                    context.getString(R.string.channel_info_name),
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = context.getString(R.string.channel_info_desc)
                    setSound(null, null)
                },
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_PLAYBACK,
                    context.getString(R.string.channel_playback_name),
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = context.getString(R.string.channel_playback_desc)
                    setSound(null, null)
                    enableVibration(false)
                    setShowBadge(false)
                },
            )
        }
    }

    fun channelFor(prayer: Prayer): String = CHANNEL_PRAYER

    fun notificationIdFor(prayer: Prayer): Int = 1000 + prayer.ordinal

    const val REMINDER_ID = 2001
    const val EVENTS_ID = 2002
}
