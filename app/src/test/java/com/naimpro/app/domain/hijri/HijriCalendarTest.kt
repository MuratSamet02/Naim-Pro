package com.naimpro.app.domain.hijri

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDate

class HijriCalendarTest {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "2026-01-01",
            "2026-03-20",
            "2026-06-15",
            "2026-11-15",
            "2027-02-28",
            "2030-12-31",
        ],
    )
    @DisplayName("Gregorian -> Hijri -> Gregorian round-trips")
    fun roundTrip(date: String) {
        val gregorian = LocalDate.parse(date)
        val hijri = HijriCalendar.fromGregorian(gregorian)
        assertEquals(gregorian, HijriCalendar.toGregorian(hijri))
    }

    @Test
    @DisplayName("A positive offset moves the Hijri date forward")
    fun offsetMovesForward() {
        val date = LocalDate.of(2026, 11, 15)
        val base = HijriCalendar.fromGregorian(date)
        val shifted = HijriCalendar.fromGregorian(date, 1)
        assertFalse(base == shifted)
        assertEquals(HijriCalendar.toGregorian(base).plusDays(1), date.plusDays(1))
    }

    @Test
    @DisplayName("Offset is symmetric between the two conversion directions")
    fun offsetIsSymmetric() {
        val date = LocalDate.of(2026, 8, 3)
        val hijri = HijriCalendar.fromGregorian(date)
        assertEquals(date.plusDays(2), HijriCalendar.toGregorian(hijri, -2))
        assertEquals(date.minusDays(2), HijriCalendar.toGregorian(hijri, 2))
    }

    @Test
    @DisplayName("Hijri months are 29 or 30 days long")
    fun monthLengths() {
        for (month in 1..12) {
            val length = HijriCalendar.lengthOfMonth(1447, month)
            assertTrue(length in 29..30, "month $month had length $length")
        }
    }

    @Test
    @DisplayName("Month names localise to Turkish, English and Arabic")
    fun monthNames() {
        assertEquals("Ramazan", HijriCalendar.monthName(9, "tr"))
        assertEquals("Ramadan", HijriCalendar.monthName(9, "en"))
        assertEquals("رمضان", HijriCalendar.monthName(9, "ar"))
        assertTrue(HijriCalendar.monthName(9, "de").isNotEmpty())
    }

    @Test
    @DisplayName("Ramadan 1447 starts on the expected civil date")
    fun ramadanStart() {
        val start = HijriCalendar.toGregorian(HijriCalendar.HijriDate(1447, 9, 1))
        assertEquals(LocalDate.of(2026, 2, 18), start)
    }

    @Test
    @DisplayName("The religious calendar contains the classic Turkish observances")
    fun religiousCalendar() {
        val events = IslamicEvents.forYear(2026)
        val keys = events.map { it.key }.toSet()
        assertTrue(keys.contains("ramadan_start"))
        assertTrue(keys.contains("qadr_night"))
        assertTrue(keys.contains("eid_al_fitr"))
        assertTrue(keys.contains("eid_al_adha"))
        assertTrue(keys.contains("ashura"))
        assertTrue(keys.contains("mawlid"))
        assertTrue(events.zipWithNext().all { (a, b) -> !b.gregorian.isBefore(a.gregorian) })
    }

    @Test
    @DisplayName("Upcoming events are sorted and start on or after the reference date")
    fun upcomingEvents() {
        val from = LocalDate.of(2026, 11, 15)
        val upcoming = IslamicEvents.upcoming(from, count = 5)
        assertEquals(5, upcoming.size)
        assertTrue(upcoming.all { !it.gregorian.isBefore(from) })
        assertTrue(upcoming.zipWithNext().all { (a, b) -> !b.gregorian.isBefore(a.gregorian) })
    }

    @Test
    @DisplayName("A two day offset shifts every religious date")
    fun offsetShiftsEvents() {
        val base = IslamicEvents.forYear(2027, offsetDays = 0)
        val shifted = IslamicEvents.forYear(2027, offsetDays = 2)
        val baseEid = base.first { it.key == "eid_al_fitr" }.gregorian
        val shiftedEid = shifted.first { it.key == "eid_al_fitr" }.gregorian
        // A positive calendar offset makes every Hijri date appear later, which
        // means the corresponding Gregorian date arrives earlier.
        assertEquals(baseEid.minusDays(2), shiftedEid)
    }
}
