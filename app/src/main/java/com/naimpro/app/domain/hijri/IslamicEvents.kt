package com.naimpro.app.domain.hijri

import java.time.LocalDate

/** A religious day or night shown in the calendar and optionally notified. */
data class IslamicEvent(
    /** Stable id, also used as notification tag. */
    val key: String,
    /** Hijri date the event falls on. */
    val hijri: HijriCalendar.HijriDate,
    val gregorian: LocalDate,
    /** Month of the event, 1..12. */
    val hijriMonth: Int,
    val hijriDay: Int,
)

/**
 * Derives the Turkish religious calendar (dini günler ve geceler) from the
 * Hijri calendar, so it works offline for any year without lookup tables.
 */
object IslamicEvents {
    private data class Definition(
        val key: String,
        val month: Int,
        val day: Int,
        val shiftDays: Long = 0,
    )

    private val definitions =
        listOf(
            Definition("hijri_new_year", 1, 1),
            Definition("ashura", 1, 10),
            Definition("mawlid", 3, 12),
            Definition("raghaib", 7, 1),
            Definition("miraj", 7, 27),
            Definition("baraah", 8, 15),
            Definition("ramadan_start", 9, 1),
            Definition("qadr_night", 9, 27),
            Definition("eid_al_fitr", 10, 1),
            Definition("arafah", 12, 9),
            Definition("eid_al_adha", 12, 10),
        )

    /** All events whose Gregorian date falls inside `year`. */
    fun forYear(
        year: Int,
        offsetDays: Int = 0,
    ): List<IslamicEvent> {
        val start = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year, 12, 31)
        val events = ArrayList<IslamicEvent>(16)
        var date = start
        while (!date.isAfter(end)) {
            val hijri = HijriCalendar.fromGregorian(date, offsetDays)
            events += eventsOn(date, hijri, year)
            date = date.plusDays(1)
        }
        return events.sortedBy { it.gregorian }
    }

    /** Definitions that land exactly on [date], shifted to their observed day. */
    private fun eventsOn(
        date: LocalDate,
        hijri: HijriCalendar.HijriDate,
        year: Int,
    ): List<IslamicEvent> =
        definitions
            .filter { hijri.month == it.month && hijri.day == it.day }
            .map { it.toEvent(hijri, date) }
            .filter { it.gregorian.year == year }

    private fun Definition.toEvent(
        hijri: HijriCalendar.HijriDate,
        date: LocalDate,
    ): IslamicEvent =
        IslamicEvent(
            key = key,
            hijri = hijri,
            gregorian = date.plusDays(shiftDays),
            hijriMonth = month,
            hijriDay = day,
        )

    /** Next `count` events on or after [from]. */
    fun upcoming(
        from: LocalDate,
        count: Int = 6,
        offsetDays: Int = 0,
    ): List<IslamicEvent> {
        val events = ArrayList<IslamicEvent>()
        var year = from.year
        while (events.size < count && year < from.year + 3) {
            events.addAll(forYear(year, offsetDays).filter { !it.gregorian.isBefore(from) })
            year++
        }
        return events.sortedBy { it.gregorian }.take(count)
    }
}
