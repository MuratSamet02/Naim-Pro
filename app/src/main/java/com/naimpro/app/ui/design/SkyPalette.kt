package com.naimpro.app.ui.design

import java.time.LocalTime

/** Time-of-day phase that drives the home screen sky gradient. */
enum class DayPhase {
    NIGHT,
    DAWN,
    DAY,
    GOLDEN,
    DUSK,
    ;

    companion object {
        val DAWN_START: LocalTime = LocalTime.of(4, 30)
        val DAWN_END: LocalTime = LocalTime.of(6, 30)
        val DAY_END: LocalTime = LocalTime.of(16, 30)
        val GOLDEN_END: LocalTime = LocalTime.of(18, 30)
        val DUSK_END: LocalTime = LocalTime.of(20, 30)

        /**
         * Phase for a wall clock time.
         *
         * The boundaries are intentionally fixed rather than derived from the
         * prayer times: the gradient is decorative, and a stable mapping means
         * the home screen does not repaint on every calculation.
         */
        fun from(time: LocalTime): DayPhase =
            when {
                time < DAWN_START -> NIGHT
                time < DAWN_END -> DAWN
                time < DAY_END -> DAY
                time < GOLDEN_END -> GOLDEN
                time < DUSK_END -> DUSK
                else -> NIGHT
            }
    }
}

/** One two-stop sky gradient plus the colours that stay readable on it. */
data class SkyColors(
    /** ARGB integer, `0xFFRRGGBB`. */
    val top: Long,
    val bottom: Long,
    /** Primary text colour, tested for WCAG AA on both stops. */
    val onSky: Long,
    /** Secondary text colour, tested for WCAG AA on both stops. */
    val onSkyMuted: Long,
    /** Accent used for the time and highlights (large text, ≥ 3:1). */
    val accent: Long,
)

/**
 * Hand tuned palettes for each phase, for light and dark UI.
 *
 * Both variants are dark enough that a single high contrast foreground works on
 * every stop, which is why the hero card looks the same in both themes. The
 * values are plain ARGB integers so [contrastRatio] can be unit tested on the
 * JVM without Compose.
 */
object SkyPalette {
    private val NightLight =
        SkyColors(
            top = 0xFF06131A,
            bottom = 0xFF0B2A2E,
            onSky = 0xFFF2F6F5,
            onSkyMuted = 0xFFC8DBD8,
            accent = 0xFFF3D98A,
        )
    private val NightDark =
        SkyColors(
            top = 0xFF020A0D,
            bottom = 0xFF07181A,
            onSky = 0xFFF2F6F5,
            onSkyMuted = 0xFFB9CECB,
            accent = 0xFFF3D98A,
        )
    private val DawnLight =
        SkyColors(
            top = 0xFF23324F,
            bottom = 0xFF8C5A6B,
            onSky = 0xFFFDF8F6,
            onSkyMuted = 0xFFF2E6E8,
            accent = 0xFFF3D98A,
        )
    private val DawnDark =
        SkyColors(
            top = 0xFF151E33,
            bottom = 0xFF5C3A47,
            onSky = 0xFFFDF8F6,
            onSkyMuted = 0xFFDCC8CC,
            accent = 0xFFF3D98A,
        )
    private val DayLight =
        SkyColors(
            top = 0xFF0B3A44,
            bottom = 0xFF2E6F73,
            onSky = 0xFFF2F6F5,
            onSkyMuted = 0xFFDCEEEC,
            accent = 0xFFF3D98A,
        )
    private val DayDark =
        SkyColors(
            top = 0xFF072A31,
            bottom = 0xFF1E4C4F,
            onSky = 0xFFF2F6F5,
            onSkyMuted = 0xFFC6DDDB,
            accent = 0xFFF3D98A,
        )
    private val GoldenLight =
        SkyColors(
            top = 0xFF3A2A12,
            bottom = 0xFF7A5A1E,
            onSky = 0xFFFFF6E4,
            onSkyMuted = 0xFFEBD9B6,
            accent = 0xFFF3D98A,
        )
    private val GoldenDark =
        SkyColors(
            top = 0xFF241A0B,
            bottom = 0xFF55400F,
            onSky = 0xFFFFF6E4,
            onSkyMuted = 0xFFE3D0AA,
            accent = 0xFFF3D98A,
        )
    private val DuskLight =
        SkyColors(
            top = 0xFF1A1B33,
            bottom = 0xFF5A3A5E,
            onSky = 0xFFF6EEFF,
            onSkyMuted = 0xFFDCCFE8,
            accent = 0xFFF3D98A,
        )
    private val DuskDark =
        SkyColors(
            top = 0xFF101022,
            bottom = 0xFF3C2640,
            onSky = 0xFFF6EEFF,
            onSkyMuted = 0xFFD3C6E0,
            accent = 0xFFF3D98A,
        )

    fun forPhase(
        phase: DayPhase,
        dark: Boolean,
    ): SkyColors =
        when (phase) {
            DayPhase.NIGHT -> if (dark) NightDark else NightLight
            DayPhase.DAWN -> if (dark) DawnDark else DawnLight
            DayPhase.DAY -> if (dark) DayDark else DayLight
            DayPhase.GOLDEN -> if (dark) GoldenDark else GoldenLight
            DayPhase.DUSK -> if (dark) DuskDark else DuskLight
        }

    /** WCAG 2.1 contrast ratio between two ARGB colours, in `[1, 21]`. */
    fun contrastRatio(
        foreground: Long,
        background: Long,
    ): Double {
        val a = relativeLuminance(foreground)
        val b = relativeLuminance(background)
        val lighter = maxOf(a, b)
        val darker = minOf(a, b)
        return (lighter + 0.05) / (darker + 0.05)
    }

    fun relativeLuminance(argb: Long): Double {
        fun channel(value: Int): Double {
            val c = (value and 0xFF) / 255.0
            return if (c <= 0.03928) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
        }
        val r = channel(((argb shr 16) and 0xFF).toInt())
        val g = channel(((argb shr 8) and 0xFF).toInt())
        val b = channel((argb and 0xFF).toInt())
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }
}
