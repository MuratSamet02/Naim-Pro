package com.naimpro.app.domain.geo

/**
 * Magnetic north to true north correction.
 *
 * A phone compass reports the heading relative to **magnetic** north. The qibla
 * bearing is computed against **true** north, so the declination of the current
 * position has to be added. In Türkiye the declination is roughly +4° to +6°
 * east, which is larger than the alignment tolerance of the compass and would
 * otherwise point the user several degrees off.
 */
object MagneticDeclination {
    /** Magnetic heading corrected to true north, normalised to `[0, 360)`. */
    fun correctedHeading(
        magneticAzimuth: Float,
        declinationDegrees: Float,
    ): Float = normalize(magneticAzimuth + declinationDegrees)

    /** Normalises any angle into `[0, 360)`. */
    fun normalize(degrees: Float): Float {
        val value = degrees % 360f
        return if (value < 0f) value + 360f else value
    }
}
