package com.naimpro.app.domain.geo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class MagneticDeclinationTest {
    @Test
    @DisplayName("East declination is added to the magnetic heading")
    fun eastDeclination() {
        assertEquals(105f, MagneticDeclination.correctedHeading(100f, 5f), 0.001f)
    }

    @Test
    @DisplayName("Correction wraps around north")
    fun wrapAround() {
        assertEquals(3f, MagneticDeclination.correctedHeading(358f, 5f), 0.001f)
    }

    @Test
    @DisplayName("Negative declination is supported")
    fun westDeclination() {
        assertEquals(350f, MagneticDeclination.correctedHeading(355f, -5f), 0.001f)
    }

    @Test
    @DisplayName("Normalisation always lands in [0, 360)")
    fun normalise() {
        assertEquals(0f, MagneticDeclination.normalize(360f), 0.001f)
        assertEquals(359f, MagneticDeclination.normalize(-1f), 0.001f)
        assertEquals(180f, MagneticDeclination.normalize(900f), 0.001f)
    }
}
