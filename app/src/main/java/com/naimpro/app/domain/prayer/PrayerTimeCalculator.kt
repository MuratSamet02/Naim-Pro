package com.naimpro.app.domain.prayer

import com.naimpro.app.domain.model.Prayer
import com.naimpro.app.domain.model.PrayerTimes
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Pure, dependency free prayer time engine.
 *
 * The engine never touches the network and never reads the wall clock, which
 * makes it deterministic and trivially testable. Everything runs in a few
 * microseconds per day, so a 30 day cache costs nothing to build.
 */
class PrayerTimeCalculator {
    fun calculate(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        zone: ZoneId,
        parameters: CalculationParameters = CalculationParameters(),
    ): PrayerTimes {
        val method = parameters.method

        // Solar noon for the requested local day. Anchoring on the UTC date of
        // local noon keeps the result correct for zones far from Greenwich.
        val noonReference = date.atTime(12, 0).atZone(zone).withZoneSameInstant(ZoneOffset.UTC)
        val utcDate = noonReference.toLocalDate()
        val julian = SolarPosition.julianDay(utcDate.year, utcDate.monthValue, utcDate.dayOfMonth)

        val (noonHours, sun) = solarNoon(julian, longitude)
        val declination = sun.declination
        // Epoch of 00:00 UT for the anchoring day. All hour offsets below are
        // measured from this instant, so they must not be added twice.
        val midnightEpochMillis = utcDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        val asrAngle =
            SolarPosition.acotDeg(
                parameters.madhab.shadowFactor + SolarPosition.tanDeg(abs(latitude - declination)),
            )

        val rawSunrise = sunTime(noonHours, SolarPosition.HORIZON_ANGLE, true, latitude, declination)
        val rawSunset = sunTime(noonHours, SolarPosition.HORIZON_ANGLE, false, latitude, declination)

        val effectiveSunrise = rawSunrise ?: (noonHours - SYNTHETIC_HALF_DAY_HOURS)
        val effectiveSunset = rawSunset ?: (noonHours + SYNTHETIC_HALF_DAY_HOURS)

        val rawFajr = sunTime(noonHours, -method.fajrAngle, true, latitude, declination)
        val rawIsha =
            method.ishaAngle?.let {
                sunTime(noonHours, -it, false, latitude, declination)
            } ?: (
                effectiveSunset + (
                    parameters.ishaIntervalOverrideMinutes
                        ?: method.ishaIntervalMinutes
                ) / 60.0
            )

        val nightDuration =
            nightDurationHours(
                date = date,
                latitude = latitude,
                longitude = longitude,
                zone = zone,
                fallbackSunrise = effectiveSunrise,
                sunset = effectiveSunset,
            )

        val fajrPortion = nightPortion(method.fajrAngle, parameters.highLatitudeRule)
        val ishaPortion = nightPortion(method.ishaAngle ?: 18.0, parameters.highLatitudeRule)

        val imsakHours = rawFajr ?: (effectiveSunset - fajrPortion * nightDuration)
        val yatsiHours =
            if (rawIsha == null) {
                effectiveSunset + ishaPortion * nightDuration
            } else {
                rawIsha
            }

        val ikindiHours =
            sunTime(noonHours, asrAngle, false, latitude, declination)
                ?: (noonHours + SYNTHETIC_HALF_DAY_HOURS * 0.5)

        fun local(
            hours: Double,
            temkinMinutes: Double,
        ): LocalDateTime {
            val millis =
                (hours * HOUR_MILLIS).roundToLong() +
                    (temkinMinutes * MINUTE_MILLIS).roundToLong()
            val roundedEpochMinute = (midnightEpochMillis + millis + 30_000L) / 60_000L
            return Instant.ofEpochMilli(roundedEpochMinute * 60_000L)
                .atZone(zone)
                .toLocalDateTime()
                .withSecond(0)
                .withNano(0)
        }

        val times =
            PrayerTimes(
                date = date,
                imsak = local(imsakHours, method.temkinImsak),
                gunes = local(effectiveSunrise, method.temkinGunes),
                ogle = local(noonHours, method.temkinOgle),
                ikindi = local(ikindiHours, method.temkinIkindi),
                aksam = local(effectiveSunset, method.temkinAksam),
                yatsi = local(yatsiHours, method.temkinYatsi),
            )
        return orderSafely(times, date)
    }

    /**
     * Last line of defence for polar latitudes, where the twilight angles are
     * never reached and the raw values can otherwise collide (fajr after
     * sunrise, isha at or before sunset). The result is always monotonic and
     * always inside the requested civil day.
     */
    private fun orderSafely(
        times: PrayerTimes,
        date: LocalDate,
    ): PrayerTimes {
        val dayStart = date.atStartOfDay()
        val dayEnd = date.atTime(23, 59)

        var imsak = times.imsak
        if (!imsak.isBefore(times.gunes)) {
            imsak = maxOf(times.gunes.minusMinutes(FALLBACK_TWILIGHT_MINUTES), dayStart)
        }

        var yatsi = times.yatsi
        if (!yatsi.isAfter(times.aksam)) {
            yatsi = minOf(times.aksam.plusMinutes(FALLBACK_TWILIGHT_MINUTES), dayEnd)
        }

        return times.copy(imsak = imsak, yatsi = yatsi)
    }

    /** Calculates a contiguous block of days, used to warm the 30 day cache. */
    fun calculateRange(
        startDate: LocalDate,
        days: Int,
        latitude: Double,
        longitude: Double,
        zone: ZoneId,
        parameters: CalculationParameters = CalculationParameters(),
    ): List<PrayerTimes> =
        (0 until days).map { offset ->
            calculate(startDate.plusDays(offset.toLong()), latitude, longitude, zone, parameters)
        }

    private fun solarNoon(
        julian: Double,
        longitude: Double,
    ): Pair<Double, SolarPosition.SunCoordinates> {
        var ut = 12.0 - longitude / 15.0
        var coordinates = SolarPosition.coordinates(julian + ut / 24.0)
        repeat(3) {
            ut = 12.0 - coordinates.equationOfTimeHours - longitude / 15.0
            coordinates = SolarPosition.coordinates(julian + ut / 24.0)
        }
        return (12.0 - coordinates.equationOfTimeHours - longitude / 15.0) to coordinates
    }

    /** Returns the local (UT based) hour of a solar altitude crossing, or null. */
    private fun sunTime(
        noonHours: Double,
        angle: Double,
        beforeNoon: Boolean,
        latitude: Double,
        declination: Double,
    ): Double? {
        val denominator = SolarPosition.cosDeg(latitude) * SolarPosition.cosDeg(declination)
        if (abs(denominator) < 1e-9) return null
        val cosH =
            (
                SolarPosition.sinDeg(angle) -
                    SolarPosition.sinDeg(latitude) * SolarPosition.sinDeg(declination)
            ) / denominator
        if (cosH > 1.0 || cosH < -1.0) return null
        val hours = SolarPosition.acosDeg(cosH) / 15.0
        return if (beforeNoon) noonHours - hours else noonHours + hours
    }

    private fun nightDurationHours(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        zone: ZoneId,
        fallbackSunrise: Double,
        sunset: Double,
    ): Double {
        val nextDate = date.plusDays(1)
        val noonReference = nextDate.atTime(12, 0).atZone(zone).withZoneSameInstant(ZoneOffset.UTC)
        val utcDate = noonReference.toLocalDate()
        val julian = SolarPosition.julianDay(utcDate.year, utcDate.monthValue, utcDate.dayOfMonth)
        val (nextNoon, sun) = solarNoon(julian, longitude)
        val nextSunrise =
            sunTime(nextNoon, SolarPosition.HORIZON_ANGLE, true, latitude, sun.declination)
                ?: (nextNoon - SYNTHETIC_HALF_DAY_HOURS)
        val duration = nextSunrise - sunset
        return if (duration in 0.5..23.5) duration else (fallbackSunrise + 24.0 - sunset)
    }

    private fun nightPortion(
        angle: Double,
        rule: HighLatitudeRule,
    ): Double =
        when (rule) {
            HighLatitudeRule.MIDDLE_OF_THE_NIGHT -> 0.5
            HighLatitudeRule.ONE_SEVENTH -> 1.0 / 7.0
            HighLatitudeRule.TWILIGHT_ANGLE -> (angle / 60.0).coerceIn(1.0 / 7.0, 0.5)
            HighLatitudeRule.NONE -> 0.5
        }

    /** Convenience helper: the night portion distance for a given rule (for docs/UI). */
    fun describeRule(rule: HighLatitudeRule): String = rule.displayKey

    companion object {
        private const val HOUR_MILLIS = 3_600_000.0
        private const val MINUTE_MILLIS = 60_000.0
        private const val SYNTHETIC_HALF_DAY_HOURS = 6.0

        /** Minimum gap enforced between sunrise/fajr and sunset/isha near the poles. */
        private const val FALLBACK_TWILIGHT_MINUTES = 90L

        /** Kaaba coordinates, used by the qibla calculation. */
        const val KAABA_LATITUDE = 21.4225
        const val KAABA_LONGITUDE = 39.8262

        fun prayers(): List<Prayer> = Prayer.ALL
    }
}
