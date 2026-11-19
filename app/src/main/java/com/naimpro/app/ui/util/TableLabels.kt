package com.naimpro.app.ui.util

/**
 * Column labels used by the imsakiye table and the PDF/CSV exporter.
 *
 * Kept in one place so the on-screen table and the exported document can never
 * drift apart. Android string resources are not usable from the PDF writer
 * (it runs off the main thread without a Context), so the three shipped
 * languages are resolved here instead.
 */
internal object TableLabels {
    private val tr =
        mapOf(
            "monthly_day" to "Gün",
            "monthly_hijri" to "Hicri",
            "prayer_imsak" to "İmsak",
            "prayer_gunes" to "Güneş",
            "prayer_ogle" to "Öğle",
            "prayer_ikindi" to "İkindi",
            "prayer_aksam" to "Akşam",
            "prayer_yatsi" to "Yatsı",
        )

    private val en =
        mapOf(
            "monthly_day" to "Day",
            "monthly_hijri" to "Hijri",
            "prayer_imsak" to "Imsak",
            "prayer_gunes" to "Sunrise",
            "prayer_ogle" to "Dhuhr",
            "prayer_ikindi" to "Asr",
            "prayer_aksam" to "Maghrib",
            "prayer_yatsi" to "Isha",
        )

    private val ar =
        mapOf(
            "monthly_day" to "اليوم",
            "monthly_hijri" to "هجري",
            "prayer_imsak" to "الإمساك",
            "prayer_gunes" to "الشروق",
            "prayer_ogle" to "الظهر",
            "prayer_ikindi" to "العصر",
            "prayer_aksam" to "المغرب",
            "prayer_yatsi" to "العشاء",
        )

    /** Label for [key] in [languageTag]; falls back to English. */
    fun of(
        key: String,
        languageTag: String,
    ): String {
        val table =
            when (languageTag.substringBefore('-').lowercase()) {
                "tr" -> tr
                "ar" -> ar
                else -> en
            }
        return table[key] ?: en[key] ?: key
    }
}
