package com.naimpro.app.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.naimpro.app.MainActivity
import com.naimpro.app.NaimApplication
import com.naimpro.app.R
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Optional low priority, non dismissible notification with the next prayer.
 *
 * It is updated only when an alarm fires or the app changes settings, so it
 * needs no service and costs almost nothing. Off by default.
 */
object NextPrayerNotification {
    private const val NOTIFICATION_ID = 2100
    private const val OPEN_REQUEST_CODE = 35
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    suspend fun update(context: Context) {
        val container = NaimApplication.container(context)
        val settings = container.settings.current()
        if (!settings.ongoingNotificationEnabled || !settings.notificationEnabled) {
            cancel(context)
            return
        }
        val location = container.locations.current()
        if (location == null) {
            cancel(context)
            return
        }
        val now = LocalDateTime.now(location.zoneId)
        val next = runCatching { container.alarms.nextPrayer(now, location, settings) }.getOrNull()
        if (next == null) {
            cancel(context)
            return
        }
        val remaining = Duration.between(now, next.second).coerceAtLeast(Duration.ZERO)
        val body =
            context.getString(
                R.string.notification_next_prayer_body,
                next.second.format(timeFormatter),
                formatRemaining(context, remaining),
            )
        val openIntent =
            PendingIntent.getActivity(
                context,
                OPEN_REQUEST_CODE,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat.Builder(context, PrayerNotificationChannels.CHANNEL_INFO)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(
                    context.getString(
                        R.string.notification_next_prayer_title,
                        PrayerNotifications.prayerName(context, next.first),
                    ),
                )
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setContentIntent(openIntent)
                .build()
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        runCatching { manager.notify(NOTIFICATION_ID, notification) }
    }

    fun cancel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        runCatching { manager.cancel(NOTIFICATION_ID) }
    }

    private fun formatRemaining(
        context: Context,
        remaining: Duration,
    ): String {
        val hours = remaining.toHours()
        val minutes = remaining.toMinutes() % 60
        return if (hours > 0) {
            context.getString(R.string.notification_remaining_hours_minutes, hours, minutes)
        } else {
            context.getString(R.string.notification_remaining_minutes, minutes)
        }
    }
}
