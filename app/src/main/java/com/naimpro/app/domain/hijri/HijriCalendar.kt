package com.naimpro.app.domain.hijri

import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate

/**
 * Hijri (Islamic) calendar conversion based on the Umm al-Qura tables shipped
 * with the JDK, plus a user adjustable offset for local moon sighting.
 */
object HijriCalendar {
    data class HijriDate(val year: Int, val month: Int, val day: Int) {
        /** 1 = Muharrem ... 12 = Zilhicce. */
        val monthIndex: Int get() = month
    }

    private val chronology = HijrahChronology.INSTANCE

    fun fromGregorian(
        date: LocalDate,
        offsetDays: Int = 0,
    ): HijriDate {
        val adjusted = if (offsetDays == 0) date else date.plusDays(offsetDays.toLong())
        val hijrah = HijrahDate.from(adjusted)
        return HijriDate(
            year = hijrah.get(java.time.temporal.ChronoField.YEAR),
            month = hijrah.get(java.time.temporal.ChronoField.MONTH_OF_YEAR),
            day = hijrah.get(java.time.temporal.ChronoField.DAY_OF_MONTH),
        )
    }

    fun toGregorian(
        hijri: HijriDate,
        offsetDays: Int = 0,
    ): LocalDate {
        val date = chronology.date(hijri.year, hijri.month, hijri.day)
        return LocalDate.from(date).minusDays(offsetDays.toLong())
    }

    fun lengthOfMonth(
        year: Int,
        month: Int,
    ): Int = chronology.date(year, month, 1).lengthOfMonth()

    /** Classic month names in Turkish, used for display and notifications. */
    val monthNamesTr: List<String> =
        listOf(
            "Muharrem", "Safer", "Rebiülevvel", "Rebiülahir", "Cemaziyelevvel", "Cemaziyelahir",
            "Recep", "Şaban", "Ramazan", "Şevval", "Zilkade", "Zilhicce",
        )

    val monthNamesEn: List<String> =
        listOf(
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Awwal",
            "Jumada al-Thani", "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah",
        )

    val monthNamesAr: List<String> =
        listOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
            "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة",
        )

    fun monthName(
        month: Int,
        languageTag: String,
    ): String =
        when (languageTag) {
            "en" -> monthNamesEn.getOrElse(month - 1) { "" }
            "ar" -> monthNamesAr.getOrElse(month - 1) { "" }
            else -> monthNamesTr.getOrElse(month - 1) { "" }
        }
}
