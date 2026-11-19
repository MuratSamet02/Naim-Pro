package com.naimpro.app.domain.prayer

/**
 * Supported calculation conventions.
 *
 * [DIYANET] reproduces the published Turkish Presidency of Religious Affairs
 * (Diyanet) times, including the small "temkin" safety margins that Diyanet
 * applies to sunrise, noon, afternoon and sunset. Calibrated against 10 cities
 * x 32 days of official data (see `docs/TEST-REPORT.md`).
 */
enum class CalculationMethod(
    val displayKey: String,
    /** Sun depression angle below the horizon at dawn. */
    val fajrAngle: Double,
    /** Sun depression angle at nightfall, or `null` when an interval is used. */
    val ishaAngle: Double?,
    /** Minutes after sunset used when [ishaAngle] is `null`. */
    val ishaIntervalMinutes: Int,
    val temkinImsak: Double,
    val temkinGunes: Double,
    val temkinOgle: Double,
    val temkinIkindi: Double,
    val temkinAksam: Double,
    val temkinYatsi: Double,
) {
    DIYANET(
        displayKey = "diyanet",
        fajrAngle = 18.0,
        ishaAngle = 17.0,
        ishaIntervalMinutes = 0,
        temkinImsak = 0.0,
        temkinGunes = -7.3,
        temkinOgle = 4.9,
        temkinIkindi = 4.15,
        temkinAksam = 7.25,
        temkinYatsi = 0.0,
    ),
    DIYANET_SAFI(
        displayKey = "diyanet_safi",
        fajrAngle = 18.0,
        ishaAngle = 17.0,
        ishaIntervalMinutes = 0,
        temkinImsak = 0.0,
        temkinGunes = -7.3,
        temkinOgle = 4.9,
        temkinIkindi = 4.15,
        temkinAksam = 7.25,
        temkinYatsi = 0.0,
    ),
    MWL(
        displayKey = "mwl",
        fajrAngle = 18.0,
        ishaAngle = 17.0,
        ishaIntervalMinutes = 0,
        temkinImsak = 0.0,
        temkinGunes = 0.0,
        temkinOgle = 0.0,
        temkinIkindi = 0.0,
        temkinAksam = 0.0,
        temkinYatsi = 0.0,
    ),
    ISNA(
        displayKey = "isna",
        fajrAngle = 15.0,
        ishaAngle = 15.0,
        ishaIntervalMinutes = 0,
        temkinImsak = 0.0,
        temkinGunes = 0.0,
        temkinOgle = 0.0,
        temkinIkindi = 0.0,
        temkinAksam = 0.0,
        temkinYatsi = 0.0,
    ),
    UMM_AL_QURA(
        displayKey = "umm_al_qura",
        fajrAngle = 18.5,
        ishaAngle = null,
        ishaIntervalMinutes = 90,
        temkinImsak = 0.0,
        temkinGunes = 0.0,
        temkinOgle = 0.0,
        temkinIkindi = 0.0,
        temkinAksam = 0.0,
        temkinYatsi = 0.0,
    ),
    EGYPT(
        displayKey = "egypt",
        fajrAngle = 19.5,
        ishaAngle = 17.5,
        ishaIntervalMinutes = 0,
        temkinImsak = 0.0,
        temkinGunes = 0.0,
        temkinOgle = 0.0,
        temkinIkindi = 0.0,
        temkinAksam = 0.0,
        temkinYatsi = 0.0,
    ),
    KARACHI(
        displayKey = "karachi",
        fajrAngle = 18.0,
        ishaAngle = 18.0,
        ishaIntervalMinutes = 0,
        temkinImsak = 0.0,
        temkinGunes = 0.0,
        temkinOgle = 0.0,
        temkinIkindi = 0.0,
        temkinAksam = 0.0,
        temkinYatsi = 0.0,
    ),
    KUWAIT(
        displayKey = "kuwait",
        fajrAngle = 18.0,
        ishaAngle = 17.5,
        ishaIntervalMinutes = 0,
        temkinImsak = 0.0,
        temkinGunes = 0.0,
        temkinOgle = 0.0,
        temkinIkindi = 0.0,
        temkinAksam = 0.0,
        temkinYatsi = 0.0,
    ),
    SINGAPORE(
        displayKey = "singapore",
        fajrAngle = 20.0,
        ishaAngle = 18.0,
        ishaIntervalMinutes = 0,
        temkinImsak = 0.0,
        temkinGunes = 0.0,
        temkinOgle = 0.0,
        temkinIkindi = 0.0,
        temkinAksam = 0.0,
        temkinYatsi = 0.0,
    ),
    ;

    companion object {
        fun fromKey(key: String?): CalculationMethod = entries.firstOrNull { it.displayKey == key } ?: DIYANET
    }
}

/** Which shadow length defines the afternoon prayer. */
enum class AsrMadhab(val shadowFactor: Int) {
    STANDARD(1),
    HANAFI(2),
    ;

    companion object {
        fun fromKey(key: String?): AsrMadhab = entries.firstOrNull { it.name == key } ?: STANDARD
    }
}

/** Strategy applied when the sun never reaches the required angle. */
enum class HighLatitudeRule(val displayKey: String) {
    NONE("none"),
    MIDDLE_OF_THE_NIGHT("middle"),
    ONE_SEVENTH("seventh"),
    TWILIGHT_ANGLE("angle"),
    ;

    companion object {
        fun fromKey(key: String?): HighLatitudeRule = entries.firstOrNull { it.displayKey == key } ?: NONE
    }
}

/** Full, immutable input for one calculation. */
data class CalculationParameters(
    val method: CalculationMethod = CalculationMethod.DIYANET,
    val madhab: AsrMadhab = AsrMadhab.STANDARD,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.NONE,
    val ishaIntervalOverrideMinutes: Int? = null,
)
