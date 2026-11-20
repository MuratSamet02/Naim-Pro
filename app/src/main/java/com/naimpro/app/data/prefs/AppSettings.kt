package com.naimpro.app.data.prefs

import com.naimpro.app.domain.model.Prayer
import com.naimpro.app.domain.prayer.AsrMadhab
import com.naimpro.app.domain.prayer.CalculationMethod
import com.naimpro.app.domain.prayer.CalculationParameters
import com.naimpro.app.domain.prayer.HighLatitudeRule

/** How a prayer time should notify the user. */
enum class NotificationMode {
    EZAN,
    SHORT_ALERT,
    VIBRATE,
    SILENT,
    ;

    companion object {
        fun fromKey(key: String?): NotificationMode = entries.firstOrNull { it.name == key } ?: SILENT
    }
}

enum class ThemeMode { SYSTEM, LIGHT, DARK, AMOLED }

/**
 * Interruption filter used by the cami (mosque) mode.
 *
 * The numeric values mirror `NotificationManager.INTERRUPTION_FILTER_ALARMS`
 * (4) and `..._PRIORITY` (2) so this enum stays Android free and unit testable.
 * `NONE` is deliberately not offered: it would mute the adhan itself.
 */
enum class CamiModeFilter(
    val interruptionFilter: Int,
) {
    ALARMS(4),
    PRIORITY(2),
    ;

    companion object {
        const val INTERRUPTION_FILTER_PRIORITY = 2
        const val INTERRUPTION_FILTER_ALARMS = 4

        fun fromKey(key: String?): CamiModeFilter = entries.firstOrNull { it.name == key } ?: ALARMS
    }
}

enum class AppLanguage(val tag: String) {
    TURKISH("tr"),
    ENGLISH("en"),
    ARABIC("ar"),
}

/** One immutable snapshot of every user preference. */
data class AppSettings(
    val method: CalculationMethod = CalculationMethod.DIYANET,
    val madhab: AsrMadhab = AsrMadhab.STANDARD,
    val highLatitudeRule: HighLatitudeRule = HighLatitudeRule.NONE,
    val corrections: Map<Prayer, Int> = emptyMap(),
    val hicriOffsetDays: Int = 0,
    val notificationEnabled: Boolean = true,
    val notificationModes: Map<Prayer, NotificationMode> = defaultModes(),
    val preReminderMinutes: Int = 0,
    val preReminderEnabled: Boolean = false,
    /** Per prayer override of [preReminderMinutes]; 0 disables the reminder. */
    val perPrayerReminderMinutes: Map<Prayer, Int> = emptyMap(),
    val ezanVolumePercent: Int = 80,
    val vibrationEnabled: Boolean = true,
    /** Optional low priority notification with the next prayer and countdown. */
    val ongoingNotificationEnabled: Boolean = false,
    val salahTrackingEnabled: Boolean = true,
    val diniGunNotifications: Boolean = false,
    val camiModeEnabled: Boolean = false,
    val camiModeMinutes: Int = 15,
    val camiModeFilter: CamiModeFilter = CamiModeFilter.ALARMS,
    /** Manual trim added to the magnetic declination, in degrees (`-10..10`). */
    val qiblaCalibrationOffset: Float = 0f,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val language: AppLanguage = AppLanguage.TURKISH,
    val fontScale: Float = 1.0f,
    val onboardingCompleted: Boolean = false,
    val lastZikir: String = "Sübhanallah",
    val lastZikirTarget: Int = 33,
    val dailyContentSeed: Int = 0,
) {
    val parameters: CalculationParameters
        get() =
            CalculationParameters(
                method = method,
                madhab = madhab,
                highLatitudeRule = highLatitudeRule,
            )

    companion object {
        fun defaultModes(): Map<Prayer, NotificationMode> =
            Prayer.ALL.associateWith { prayer ->
                if (prayer == Prayer.GUNES) NotificationMode.SILENT else NotificationMode.EZAN
            }
    }
}
