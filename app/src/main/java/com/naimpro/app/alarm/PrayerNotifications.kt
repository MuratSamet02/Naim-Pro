package com.naimpro.app.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.naimpro.app.MainActivity
import com.naimpro.app.R
import com.naimpro.app.domain.model.Prayer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Builds the prayer time, reminder and religious day notifications. */
object PrayerNotifications {
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun showPrayer(
        context: Context,
        prayer: Prayer,
        time: LocalDateTime,
        locationName: String,
        hijriLabel: String,
    ) {
        val stopIntent =
            PendingIntent.getBroadcast(
                context,
                STOP_REQUEST_CODE,
                Intent(context, PrayerAlarmReceiver::class.java).apply {
                    action = PrayerAlarmReceiver.ACTION_STOP_AUDIO
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val contentIntent =
            PendingIntent.getActivity(
                context,
                OPEN_REQUEST_CODE,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val title = context.getString(R.string.notification_prayer_title, prayerName(context, prayer))
        val body =
            context.getString(
                R.string.notification_prayer_body,
                time.format(timeFormatter),
                locationName,
            )

        val notification =
            NotificationCompat.Builder(context, PrayerNotificationChannels.CHANNEL_PRAYER)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(body)
                        .setSummaryText(hijriLabel),
                )
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setOnlyAlertOnce(true)
                .addAction(0, context.getString(R.string.action_stop_audio), stopIntent)
                .build()

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        runCatching {
            manager.notify(PrayerNotificationChannels.notificationIdFor(prayer), notification)
        }
    }

    fun showReminder(
        context: Context,
        prayer: Prayer,
        minutes: Int,
        time: LocalDateTime,
    ) {
        val contentIntent =
            PendingIntent.getActivity(
                context,
                OPEN_REQUEST_CODE,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat.Builder(context, PrayerNotificationChannels.CHANNEL_REMINDER)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.notification_reminder_title, minutes))
                .setContentText(
                    context.getString(
                        R.string.notification_reminder_body,
                        prayerName(context, prayer),
                        time.format(timeFormatter),
                    ),
                )
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build()
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        runCatching { manager.notify(PrayerNotificationChannels.REMINDER_ID, notification) }
    }

    fun showEvent(
        context: Context,
        title: String,
        body: String,
    ) {
        val contentIntent =
            PendingIntent.getActivity(
                context,
                OPEN_REQUEST_CODE,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat.Builder(context, PrayerNotificationChannels.CHANNEL_EVENTS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build()
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        runCatching { manager.notify(PrayerNotificationChannels.EVENTS_ID, notification) }
    }

    fun cancelPrayer(
        context: Context,
        prayer: Prayer,
    ) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        runCatching { manager.cancel(PrayerNotificationChannels.notificationIdFor(prayer)) }
    }

    fun prayerName(
        context: Context,
        prayer: Prayer,
    ): String =
        when (prayer) {
            Prayer.IMSAK -> context.getString(R.string.prayer_imsak)
            Prayer.GUNES -> context.getString(R.string.prayer_gunes)
            Prayer.OGLE -> context.getString(R.string.prayer_ogle)
            Prayer.IKINDI -> context.getString(R.string.prayer_ikindi)
            Prayer.AKSAM -> context.getString(R.string.prayer_aksam)
            Prayer.YATSI -> context.getString(R.string.prayer_yatsi)
        }

    private const val STOP_REQUEST_CODE = 31
    private const val OPEN_REQUEST_CODE = 32
}
