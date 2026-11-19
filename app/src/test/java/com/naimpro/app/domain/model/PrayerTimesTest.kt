package com.naimpro.app.domain.model

import com.naimpro.app.domain.prayer.CalculationMethod
import com.naimpro.app.domain.prayer.CalculationParameters
import com.naimpro.app.domain.prayer.PrayerTimeCalculator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class PrayerTimesTest {
    private val calculator = PrayerTimeCalculator()
    private val zone = ZoneId.of("Europe/Istanbul")
    private val date = LocalDate.of(2026, 4, 10)

    private fun base(): PrayerTimes = calculator.calculate(date, 41.0082, 28.9784, zone)

    @Test
    @DisplayName("asList returns all six prayers in chronological order")
    fun asListOrder() {
        val list = base().asList()
        assertEquals(Prayer.ALL, list.map { it.first })
        assertTrue(list.zipWithNext().all { (a, b) -> b.second.isAfter(a.second) })
    }

    @Test
    @DisplayName("A single correction shifts only the requested prayer")
    fun singleCorrection() {
        val times = base()
        val corrected = times.withCorrection(Prayer.OGLE, 5)
        assertEquals(times.ogle.plusMinutes(5), corrected.ogle)
        assertEquals(times.imsak, corrected.imsak)
        assertEquals(times.yatsi, corrected.yatsi)
    }

    @Test
    @DisplayName("Zero correction is a no-op")
    fun zeroCorrection() {
        val times = base()
        assertEquals(times, times.withCorrection(Prayer.AKSAM, 0))
    }

    @Test
    @DisplayName("Multiple corrections accumulate")
    fun multipleCorrections() {
        val times = base()
        val corrected =
            times.withCorrections(
                mapOf(Prayer.IMSAK to -3, Prayer.YATSI to 12),
            )
        assertEquals(times.imsak.minusMinutes(3), corrected.imsak)
        assertEquals(times.yatsi.plusMinutes(12), corrected.yatsi)
        assertEquals(times.gunes, corrected.gunes)
    }

    @Test
    @DisplayName("Corrections survive a negative shift without changing the date")
    fun negativeCorrectionKeepsDate() {
        val times = base()
        val corrected = times.withCorrection(Prayer.IMSAK, -1)
        assertEquals(date, corrected.imsak.toLocalDate())
    }

    @Test
    @DisplayName("Calculation is deterministic for the same inputs")
    fun deterministic() {
        val a = calculator.calculate(date, 41.0082, 28.9784, zone, CalculationParameters(CalculationMethod.DIYANET))
        val b = calculator.calculate(date, 41.0082, 28.9784, zone, CalculationParameters(CalculationMethod.DIYANET))
        assertEquals(a, b)
    }

    @Test
    @DisplayName("A month range contains exactly the requested number of days")
    fun rangeSize() {
        val range = calculator.calculateRange(date, 30, 41.0082, 28.9784, zone)
        assertEquals(30, range.size)
        assertEquals(date, range.first().date)
        assertEquals(date.plusDays(29), range.last().date)
        assertTrue(range.all { it.timeOf(Prayer.OGLE).toLocalDate() == it.date })
    }

    @Test
    @DisplayName("Southern and northern seasons are mirrored")
    fun hemisphereSeasons() {
        val june = LocalDate.of(2026, 6, 21)
        val december = LocalDate.of(2026, 12, 21)
        val northJune = calculator.calculate(june, 41.0, 29.0, zone)
        val northDecember = calculator.calculate(december, 41.0, 29.0, zone)
        val dayLengthJune = java.time.Duration.between(northJune.gunes, northJune.aksam)
        val dayLengthDecember = java.time.Duration.between(northDecember.gunes, northDecember.aksam)
        assertTrue(dayLengthJune > dayLengthDecember)

        val southJune = calculator.calculate(june, -33.9, 18.4, ZoneId.of("Africa/Johannesburg"))
        val southDecember = calculator.calculate(december, -33.9, 18.4, ZoneId.of("Africa/Johannesburg"))
        assertTrue(
            java.time.Duration.between(southJune.gunes, southJune.aksam) <
                java.time.Duration.between(southDecember.gunes, southDecember.aksam),
        )
    }

    @Test
    @DisplayName("Times are minute aligned with zero seconds")
    fun minuteAligned() {
        val times = base()
        for (prayer in Prayer.ALL) {
            val value: LocalDateTime = times.timeOf(prayer)
            assertEquals(0, value.second)
            assertEquals(0, value.nano)
        }
    }
}
