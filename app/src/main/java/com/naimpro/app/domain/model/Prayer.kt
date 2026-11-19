package com.naimpro.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/** The six prayer times the app displays, in chronological order for a normal day. */
enum class Prayer {
    IMSAK,
    GUNES,
    OGLE,
    IKINDI,
    AKSAM,
    YATSI,
    ;

    companion object {
        val ALL: List<Prayer> = entries.toList()
    }
}

/**
 * The six prayer times of one civil day.
 *
 * Times are already resolved into the user's [java.time.ZoneId], so they can be
 * shown directly and are safe across daylight saving transitions.
 */
data class PrayerTimes(
    val date: LocalDate,
    val imsak: LocalDateTime,
    val gunes: LocalDateTime,
    val ogle: LocalDateTime,
    val ikindi: LocalDateTime,
    val aksam: LocalDateTime,
    val yatsi: LocalDateTime,
) {
    fun timeOf(prayer: Prayer): LocalDateTime =
        when (prayer) {
            Prayer.IMSAK -> imsak
            Prayer.GUNES -> gunes
            Prayer.OGLE -> ogle
            Prayer.IKINDI -> ikindi
            Prayer.AKSAM -> aksam
            Prayer.YATSI -> yatsi
        }

    /** Chronologically sorted pairs, useful for schedulers and list rendering. */
    fun asList(): List<Pair<Prayer, LocalDateTime>> = Prayer.ALL.map { it to timeOf(it) }

    /** Applies a user configured correction (in minutes) to a single prayer. */
    fun withCorrection(
        prayer: Prayer,
        minutes: Int,
    ): PrayerTimes {
        if (minutes == 0) return this

        fun adjust(value: LocalDateTime) = value.plusMinutes(minutes.toLong())
        return when (prayer) {
            Prayer.IMSAK -> copy(imsak = adjust(imsak))
            Prayer.GUNES -> copy(gunes = adjust(gunes))
            Prayer.OGLE -> copy(ogle = adjust(ogle))
            Prayer.IKINDI -> copy(ikindi = adjust(ikindi))
            Prayer.AKSAM -> copy(aksam = adjust(aksam))
            Prayer.YATSI -> copy(yatsi = adjust(yatsi))
        }
    }

    /** Applies all six configured corrections at once. */
    fun withCorrections(corrections: Map<Prayer, Int>): PrayerTimes {
        var result = this
        for ((prayer, minutes) in corrections) {
            result = result.withCorrection(prayer, minutes)
        }
        return result
    }
}

/** A resolved "next prayer" descriptor used by the home screen and widgets. */
data class NextPrayer(
    val prayer: Prayer,
    val at: LocalDateTime,
    val dayOffset: Int,
)
