package com.naimpro.app.domain.hijri

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Plans the reminder for a religious day.
 *
 * The reminder is deliberately sent the evening before the event (20:00 local
 * time), which is when kandil nights are observed, so the user can prepare for
 * the night ahead. Only one event alarm is kept pending at a time.
 */
object EventReminder {
    val NOTIFY_TIME: LocalTime = LocalTime.of(20, 0)

    /** Moment the reminder for [event] should fire in [zone]. */
    fun notifyAt(
        event: IslamicEvent,
        zone: ZoneId,
    ): ZonedDateTime = event.gregorian.minusDays(1).atTime(NOTIFY_TIME).atZone(zone)

    /** Next event whose reminder is still in the future. */
    fun next(
        from: ZonedDateTime,
        events: List<IslamicEvent>,
        zone: ZoneId,
    ): Pair<IslamicEvent, ZonedDateTime>? =
        events
            .map { it to notifyAt(it, zone) }
            .filter { it.second.isAfter(from) }
            .minByOrNull { it.second }

    /** Convenience overload used by the calendar screen. */
    fun notifyDate(
        event: IslamicEvent,
        zone: ZoneId,
    ): LocalDate = notifyAt(event, zone).toLocalDate()
}
