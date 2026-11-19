package com.naimpro.app.domain.prayer

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Great circle bearing from an observer to the Kaaba. */
object QiblaCalculator {
    /**
     * @return bearing in degrees clockwise from true north, in `[0, 360)`.
     */
    fun bearingFrom(
        latitude: Double,
        longitude: Double,
    ): Double {
        val latRad = Math.toRadians(latitude)
        val kaabaLatRad = Math.toRadians(PrayerTimeCalculator.KAABA_LATITUDE)
        val deltaLon = Math.toRadians(PrayerTimeCalculator.KAABA_LONGITUDE - longitude)

        val y = sin(deltaLon)
        val x = cos(latRad) * sin(kaabaLatRad) - sin(latRad) * cos(kaabaLatRad) * cos(deltaLon)

        val degrees = Math.toDegrees(atan2(y, x))
        return (degrees + 360.0) % 360.0
    }

    /** Great circle distance in kilometres, shown on the qibla screen. */
    fun distanceKm(
        latitude: Double,
        longitude: Double,
    ): Double {
        val lat1 = Math.toRadians(latitude)
        val lat2 = Math.toRadians(PrayerTimeCalculator.KAABA_LATITUDE)
        val dLat = lat2 - lat1
        val dLon = Math.toRadians(PrayerTimeCalculator.KAABA_LONGITUDE - longitude)
        val a =
            sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return 6371.0088 * c
    }

    /** True when the device heading is within [toleranceDegrees] of the qibla. */
    fun isAligned(
        heading: Float,
        qibla: Double,
        toleranceDegrees: Float = 4f,
    ): Boolean {
        val diff = ((heading - qibla) % 360.0 + 540.0) % 360.0 - 180.0
        return kotlin.math.abs(diff) <= toleranceDegrees
    }

    /** Signed difference between device heading and qibla, in `(-180, 180]`. */
    fun offsetFrom(
        heading: Float,
        qibla: Double,
    ): Float {
        val diff = ((heading - qibla) % 360.0 + 540.0) % 360.0 - 180.0
        return diff.toFloat()
    }
}
