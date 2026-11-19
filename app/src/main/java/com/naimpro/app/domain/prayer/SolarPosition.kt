package com.naimpro.app.domain.prayer

import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * Low precision but very fast solar position model (NOAA / PrayTimes style).
 *
 * Accuracy is ~0.01 degrees for declination and equation of time, which
 * translates to well under one second of prayer time error. The full solar
 * terminator is modelled with the standard 0.833 degree refraction plus solar
 * semi-diameter correction.
 */
internal object SolarPosition {
    const val HORIZON_ANGLE = -0.833

    private const val DEG_TO_RAD = Math.PI / 180.0

    /** Julian day number at 00:00 UT of the given civil date. */
    fun julianDay(
        year: Int,
        month: Int,
        day: Int,
    ): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    data class SunCoordinates(val declination: Double, val equationOfTimeHours: Double)

    fun coordinates(julian: Double): SunCoordinates {
        val d = julian - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sinDeg(g) + 0.020 * sinDeg(2 * g))
        val e = 23.439 - 0.00000036 * d
        val rightAscension = atan2(cosDeg(e) * sinDeg(l), cosDeg(l)) / DEG_TO_RAD / 15.0
        val equationOfTime = q / 15.0 - fixHour(rightAscension)
        val declination = asin(sinDeg(e) * sinDeg(l)) / DEG_TO_RAD
        return SunCoordinates(declination, equationOfTime)
    }

    fun sinDeg(value: Double): Double = sin(value * DEG_TO_RAD)

    fun cosDeg(value: Double): Double = cos(value * DEG_TO_RAD)

    fun tanDeg(value: Double): Double = sinDeg(value) / cosDeg(value)

    fun asinDeg(value: Double): Double = asin(value.coerceIn(-1.0, 1.0)) / DEG_TO_RAD

    fun acosDeg(value: Double): Double = acos(value.coerceIn(-1.0, 1.0)) / DEG_TO_RAD

    fun acotDeg(value: Double): Double = Math.toDegrees(Math.atan(1.0 / value))

    fun fixAngle(angle: Double): Double {
        val v = angle - 360.0 * floor(angle / 360.0)
        return if (v < 0) v + 360.0 else v
    }

    fun fixHour(hour: Double): Double {
        val v = hour - 24.0 * floor(hour / 24.0)
        return if (v < 0) v + 24.0 else v
    }
}
