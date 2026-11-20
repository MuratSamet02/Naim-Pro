package com.naimpro.app.ui.design

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.LocalTime

class SkyPaletteTest {
    @Test
    @DisplayName("Phase boundaries are stable")
    fun phaseBoundaries() {
        assertEquals(DayPhase.NIGHT, DayPhase.from(LocalTime.of(2, 0)))
        assertEquals(DayPhase.DAWN, DayPhase.from(LocalTime.of(5, 0)))
        assertEquals(DayPhase.DAY, DayPhase.from(LocalTime.of(12, 0)))
        assertEquals(DayPhase.GOLDEN, DayPhase.from(LocalTime.of(17, 30)))
        assertEquals(DayPhase.DUSK, DayPhase.from(LocalTime.of(19, 30)))
        assertEquals(DayPhase.NIGHT, DayPhase.from(LocalTime.of(22, 0)))
    }

    @ParameterizedTest
    @EnumSource(DayPhase::class)
    @DisplayName("Primary text meets WCAG AA on both gradient stops")
    fun primaryContrast(phase: DayPhase) {
        listOf(false, true).forEach { dark ->
            val sky = SkyPalette.forPhase(phase, dark)
            assertTrue(
                SkyPalette.contrastRatio(sky.onSky, sky.top) >= 4.5,
                "$phase dark=$dark onSky/top ${SkyPalette.contrastRatio(sky.onSky, sky.top)}",
            )
            assertTrue(
                SkyPalette.contrastRatio(sky.onSky, sky.bottom) >= 4.5,
                "$phase dark=$dark onSky/bottom ${SkyPalette.contrastRatio(sky.onSky, sky.bottom)}",
            )
        }
    }

    @ParameterizedTest
    @EnumSource(DayPhase::class)
    @DisplayName("Muted text meets WCAG AA on both gradient stops")
    fun mutedContrast(phase: DayPhase) {
        listOf(false, true).forEach { dark ->
            val sky = SkyPalette.forPhase(phase, dark)
            assertTrue(SkyPalette.contrastRatio(sky.onSkyMuted, sky.top) >= 4.5)
            assertTrue(SkyPalette.contrastRatio(sky.onSkyMuted, sky.bottom) >= 4.5)
        }
    }

    @ParameterizedTest
    @EnumSource(DayPhase::class)
    @DisplayName("Accent is readable as large text (>= 3:1)")
    fun accentContrast(phase: DayPhase) {
        listOf(false, true).forEach { dark ->
            val sky = SkyPalette.forPhase(phase, dark)
            assertTrue(SkyPalette.contrastRatio(sky.accent, sky.top) >= 3.0)
            assertTrue(SkyPalette.contrastRatio(sky.accent, sky.bottom) >= 3.0)
        }
    }

    @Test
    @DisplayName("Contrast ratio is symmetric and bounded")
    fun ratioMath() {
        assertEquals(21.0, SkyPalette.contrastRatio(0xFFFFFFFF, 0xFF000000), 0.001)
        assertEquals(1.0, SkyPalette.contrastRatio(0xFF123456, 0xFF123456), 0.001)
        assertEquals(
            SkyPalette.contrastRatio(0xFF112233, 0xFFEEEEEE),
            SkyPalette.contrastRatio(0xFFEEEEEE, 0xFF112233),
            0.0001,
        )
    }
}
