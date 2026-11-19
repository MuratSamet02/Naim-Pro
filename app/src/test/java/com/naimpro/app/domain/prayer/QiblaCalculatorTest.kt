package com.naimpro.app.domain.prayer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.math.abs

class QiblaCalculatorTest {
    @ParameterizedTest(name = "{0} -> {1} deg")
    @CsvSource(
        "Istanbul, 41.0082, 28.9784, 149.9",
        "Ankara, 39.9334, 32.8597, 158.8",
        "London, 51.5072, -0.1276, 117.3",
        "Jakarta, -6.2088, 106.8456, 293.6",
        "NewYork, 40.7128, -74.0060, 60.3",
        "CapeTown, -33.9249, 18.4241, 24.9",
    )
    fun bearings(
        city: String,
        latitude: Double,
        longitude: Double,
        expected: Double,
    ) {
        val actual = QiblaCalculator.bearingFrom(latitude, longitude)
        assertTrue(
            abs(actual - expected) <= 0.6,
            "$city: expected ~$expected but was ${"%.2f".format(actual)}",
        )
    }

    @Test
    @DisplayName("Bearing is always normalised into [0, 360)")
    fun bearingIsNormalised() {
        val places =
            listOf(
                0.0 to 0.0,
                89.0 to -179.0,
                -89.0 to 179.0,
                -33.9 to 151.2,
                64.1 to -21.9,
            )
        for ((latitude, longitude) in places) {
            val bearing = QiblaCalculator.bearingFrom(latitude, longitude)
            assertTrue(bearing >= 0.0 && bearing < 360.0, "bearing $bearing for $latitude/$longitude")
        }
    }

    @Test
    @DisplayName("Distance to the Kaaba is about 2,900 km from Istanbul")
    fun distanceFromIstanbul() {
        val distance = QiblaCalculator.distanceKm(41.0082, 28.9784)
        assertTrue(distance in 2_350.0..2_500.0, "distance was $distance km")
    }

    @Test
    @DisplayName("Distance at the Kaaba is zero")
    fun distanceAtKaaba() {
        val distance =
            QiblaCalculator.distanceKm(
                PrayerTimeCalculator.KAABA_LATITUDE,
                PrayerTimeCalculator.KAABA_LONGITUDE,
            )
        assertTrue(distance < 1.0, "distance was $distance km")
    }

    @Test
    @DisplayName("Alignment tolerance handles the wrap-around at north")
    fun alignmentWrapsAroundNorth() {
        assertTrue(QiblaCalculator.isAligned(358f, 2.0))
        assertTrue(QiblaCalculator.isAligned(2f, 358.0))
        assertFalse(QiblaCalculator.isAligned(20f, 2.0))
    }

    @Test
    @DisplayName("Signed offset is limited to (-180, 180]")
    fun offsetRange() {
        assertEquals(-10f, QiblaCalculator.offsetFrom(350f, 0.0))
        assertEquals(10f, QiblaCalculator.offsetFrom(10f, 0.0))
        assertEquals(180f, kotlin.math.abs(QiblaCalculator.offsetFrom(180f, 0.0)))
        assertEquals(-90f, QiblaCalculator.offsetFrom(270f, 0.0))
    }
}
