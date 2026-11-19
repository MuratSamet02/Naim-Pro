package com.naimpro.app.domain.prayer

import com.naimpro.app.domain.model.Prayer
import com.naimpro.app.domain.model.PrayerTimes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDate
import java.time.ZoneId

/**
 * Edge cases that are easy to regress: daylight saving jumps, polar latitudes
 * and the boundary between one civil day and the next.
 */
class PrayerTimeEdgeCaseTest {
    private val calculator = PrayerTimeCalculator()

    @ParameterizedTest
    @ValueSource(strings = ["Europe/Berlin", "Europe/Istanbul", "America/New_York", "Australia/Sydney"])
    @DisplayName("Ordering and same-day dates hold across DST transitions")
    fun daylightSavingTransitions(zoneId: String) {
        val zone = ZoneId.of(zoneId)
        val dates =
            listOf(
                LocalDate.of(2026, 3, 27),
                LocalDate.of(2026, 3, 28),
                LocalDate.of(2026, 3, 29),
                LocalDate.of(2026, 3, 30),
                LocalDate.of(2026, 10, 24),
                LocalDate.of(2026, 10, 25),
                LocalDate.of(2026, 10, 26),
            )
        for (date in dates) {
            val times = calculator.calculate(date, 52.52, 13.405, zone)
            assertOrdered(times, date, zone)
        }
    }

    @Test
    @DisplayName("Berlin DST spring forward keeps every time on the requested civil day")
    fun springForwardStaysOnTheSameDay() {
        val zone = ZoneId.of("Europe/Berlin")
        val date = LocalDate.of(2026, 3, 29)
        val times = calculator.calculate(date, 52.52, 13.405, zone)
        assertEquals(date, times.ogle.toLocalDate())
        assertEquals(date, times.aksam.toLocalDate())
        assertOrdered(times, date, zone)
    }

    @Test
    @DisplayName("Polar night still produces a usable, ordered timetable")
    fun polarNightIsOrdered() {
        val zone = ZoneId.of("Europe/Oslo")
        val times =
            calculator.calculate(
                date = LocalDate.of(2026, 12, 21),
                latitude = 69.6492,
                longitude = 18.9553,
                zone = zone,
                parameters =
                    CalculationParameters(
                        method = CalculationMethod.MWL,
                        highLatitudeRule = HighLatitudeRule.MIDDLE_OF_THE_NIGHT,
                    ),
            )
        assertOrdered(times, LocalDate.of(2026, 12, 21), zone)
    }

    @Test
    @DisplayName("Midnight sun produces a usable, ordered timetable")
    fun midnightSunIsOrdered() {
        val zone = ZoneId.of("Europe/Oslo")
        val date = LocalDate.of(2026, 6, 21)
        val times =
            calculator.calculate(
                date = date,
                latitude = 69.6492,
                longitude = 18.9553,
                zone = zone,
                parameters =
                    CalculationParameters(
                        method = CalculationMethod.MWL,
                        highLatitudeRule = HighLatitudeRule.ONE_SEVENTH,
                    ),
            )
        assertTrue(times.imsak.toLocalDate() == date || times.imsak.toLocalDate() == date.minusDays(1))
        assertOrdered(times, date, zone, allowMidnightCrossing = true)
    }

    @Test
    @DisplayName("Extreme southern latitude remains ordered")
    fun southernHemisphere() {
        val zone = ZoneId.of("Pacific/Auckland")
        val date = LocalDate.of(2026, 7, 15)
        val times = calculator.calculate(date, -45.8788, 170.5028, zone)
        assertOrdered(times, date, zone)
    }

    @Test
    @DisplayName("Every supported method returns times for a normal day")
    fun allMethodsProduceTimes() {
        val date = LocalDate.of(2026, 5, 14)
        for (method in CalculationMethod.entries) {
            val times =
                calculator.calculate(
                    date = date,
                    latitude = 41.0082,
                    longitude = 28.9784,
                    zone = ZoneId.of("Europe/Istanbul"),
                    parameters = CalculationParameters(method = method),
                )
            assertOrdered(times, date, ZoneId.of("Europe/Istanbul"))
        }
    }

    @Test
    @DisplayName("Hanafi Asr is later than the standard shadow length")
    fun hanafiAsrIsLater() {
        val date = LocalDate.of(2026, 5, 14)
        val standard =
            calculator.calculate(
                date,
                41.0082,
                28.9784,
                ZoneId.of("Europe/Istanbul"),
                CalculationParameters(madhab = AsrMadhab.STANDARD),
            )
        val hanafi =
            calculator.calculate(
                date,
                41.0082,
                28.9784,
                ZoneId.of("Europe/Istanbul"),
                CalculationParameters(madhab = AsrMadhab.HANAFI),
            )
        assertTrue(hanafi.ikindi.isAfter(standard.ikindi))
    }

    @Test
    @DisplayName("Isha interval override is honoured")
    fun ishaIntervalOverride() {
        val date = LocalDate.of(2026, 2, 10)
        val times =
            calculator.calculate(
                date,
                24.7136,
                46.6753,
                ZoneId.of("Asia/Riyadh"),
                CalculationParameters(
                    method = CalculationMethod.UMM_AL_QURA,
                    ishaIntervalOverrideMinutes = 120,
                ),
            )
        assertEquals(120, java.time.Duration.between(times.aksam, times.yatsi).toMinutes())
    }

    private fun assertOrdered(
        times: PrayerTimes,
        date: LocalDate,
        zone: ZoneId,
        allowMidnightCrossing: Boolean = false,
    ) {
        val values = Prayer.ALL.map { times.timeOf(it) }
        for (index in 1 until values.size) {
            val previous = values[index - 1]
            val current = values[index]
            assertTrue(
                current.isAfter(previous),
                "expected ${Prayer.ALL[index]} after ${Prayer.ALL[index - 1]} on $date ($zone): $times",
            )
        }
        val localDate = times.ogle.toLocalDate()
        assertTrue(
            localDate == date || (allowMidnightCrossing && localDate == date.minusDays(1)),
            "times should belong to $date but were $times",
        )
    }
}
