package com.naimpro.app.widget

import android.content.Context
import com.naimpro.app.NaimApplication
import com.naimpro.app.alarm.PrayerNotifications
import com.naimpro.app.data.prefs.AppSettings
import com.naimpro.app.domain.model.Location
import com.naimpro.app.domain.model.Prayer
import com.naimpro.app.domain.model.PrayerTimes
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Everything a widget needs to render, resolved once per update. */
data class WidgetData(
    val locationName: String,
    val locationDetail: String,
    val nextPrayer: Prayer,
    val nextPrayerName: String,
    val nextPrayerTime: String,
    val countdown: String,
    val hijriDate: String,
    val times: List<Pair<String, String>>,
    val isPlaceholder: Boolean = false,
)

/** Loads widget state off the main thread; Glance calls this from a worker. */
object WidgetDataLoader {
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    suspend fun load(context: Context): WidgetData {
        val container = NaimApplication.container(context)
        val settings = runCatching { container.settings.current() }.getOrElse { AppSettings() }
        val location =
            runCatching { container.locations.current() }.getOrNull()
                ?: container.cities.defaultLocation()
        return build(context, container, settings, location)
    }

    private suspend fun build(
        context: Context,
        container: com.naimpro.app.data.di.AppContainer,
        settings: AppSettings,
        location: Location,
    ): WidgetData {
        val zone: ZoneId = location.zoneId
        val now = LocalDateTime.now(zone)
        val today: PrayerTimes = container.prayers.timesForCached(now.toLocalDate(), location, settings)

        var next = today.asList().firstOrNull { it.second.isAfter(now) }
        if (next == null) {
            val tomorrow = container.prayers.timesForCached(now.toLocalDate().plusDays(1), location, settings)
            next = tomorrow.asList().first()
        }

        val remaining = Duration.between(now, next.second)
        val countdown = formatDuration(remaining)

        val hijri = com.naimpro.app.domain.hijri.HijriCalendar.fromGregorian(now.toLocalDate(), settings.hicriOffsetDays)

        return WidgetData(
            locationName = location.shortLabel,
            locationDetail = location.displayName,
            nextPrayer = next.first,
            nextPrayerName = PrayerNotifications.prayerName(context, next.first),
            nextPrayerTime = next.second.format(timeFormatter),
            countdown = countdown,
            hijriDate = "${hijri.day} ${com.naimpro.app.domain.hijri.HijriCalendar.monthName(
                hijri.month,
                settings.language.tag,
            )} ${hijri.year}",
            times =
                today.asList().map { (prayer, time) ->
                    PrayerNotifications.prayerName(context, prayer) to time.format(timeFormatter)
                },
        )
    }

    fun placeholder(): WidgetData =
        WidgetData(
            locationName = "Naim Pro",
            locationDetail = "",
            nextPrayer = Prayer.OGLE,
            nextPrayerName = "—",
            nextPrayerTime = "--:--",
            countdown = "—",
            hijriDate = "",
            times = emptyList(),
            isPlaceholder = true,
        )

    private fun formatDuration(duration: Duration): String {
        val totalMinutes = duration.toMinutes().coerceAtLeast(0)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}s ${minutes}dk" else "${minutes}dk"
    }
}
