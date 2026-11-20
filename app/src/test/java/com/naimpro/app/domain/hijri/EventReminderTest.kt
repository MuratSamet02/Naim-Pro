package com.naimpro.app.domain.hijri

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class EventReminderTest {
    private val zone: ZoneId = ZoneId.of("Europe/Istanbul")

    private fun event(
        key: String,
        date: LocalDate,
    ): IslamicEvent =
        IslamicEvent(
            key = key,
            hijri = HijriCalendar.fromGregorian(date),
            gregorian = date,
            hijriMonth = 9,
            hijriDay = 1,
        )

    @Test
    @DisplayName("Reminder fires the evening before at 20:00")
    fun notifyAt() {
        val at = EventReminder.notifyAt(event("ramadan_start", LocalDate.of(2027, 2, 8)), zone)
        assertEquals(LocalDate.of(2027, 2, 7), at.toLocalDate())
        assertEquals(LocalTime.of(20, 0), at.toLocalTime())
    }

    @Test
    @DisplayName("Only future reminders are considered")
    fun nextFuture() {
        val now = ZonedDateTime.of(2026, 11, 20, 12, 0, 0, 0, zone)
        val events =
            listOf(
                event("ashura", LocalDate.of(2026, 7, 5)),
                event("ramadan_start", LocalDate.of(2027, 2, 8)),
            )
        val next = EventReminder.next(now, events, zone)
        assertEquals("ramadan_start", next?.first?.key)
    }

    @Test
    @DisplayName("No future event returns null")
    fun noFuture() {
        val now = ZonedDateTime.of(2028, 1, 1, 0, 0, 0, 0, zone)
        val events = listOf(event("ashura", LocalDate.of(2026, 7, 5)))
        assertNull(EventReminder.next(now, events, zone))
    }
}
