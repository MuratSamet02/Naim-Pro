package com.naimpro.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.naimpro.app.domain.model.Prayer
import com.naimpro.app.domain.prayer.AsrMadhab
import com.naimpro.app.domain.prayer.CalculationMethod
import com.naimpro.app.domain.prayer.HighLatitudeRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "naim_settings")

/** Single source of truth for user preferences, backed by DataStore. */
class SettingsRepository(private val context: Context) {
    private object Keys {
        val method = stringPreferencesKey("method")
        val madhab = stringPreferencesKey("madhab")
        val highLatitude = stringPreferencesKey("high_latitude")
        val corrections = stringPreferencesKey("corrections")
        val hicriOffset = intPreferencesKey("hicri_offset")
        val notificationsEnabled = booleanPreferencesKey("notifications_enabled")
        val notificationModes = stringPreferencesKey("notification_modes")
        val preReminderEnabled = booleanPreferencesKey("pre_reminder_enabled")
        val preReminderMinutes = intPreferencesKey("pre_reminder_minutes")
        val ezanVolume = intPreferencesKey("ezan_volume")
        val vibration = booleanPreferencesKey("vibration")
        val salahTracking = booleanPreferencesKey("salah_tracking")
        val diniGunNotifications = booleanPreferencesKey("dini_gun_notifications")
        val camiMode = booleanPreferencesKey("cami_mode")
        val themeMode = stringPreferencesKey("theme_mode")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val language = stringPreferencesKey("language")
        val fontScale = floatPreferencesKey("font_scale")
        val onlineComparison = booleanPreferencesKey("online_comparison")
        val onboarding = booleanPreferencesKey("onboarding")
        val lastZikir = stringPreferencesKey("last_zikir")
        val lastZikirTarget = intPreferencesKey("last_zikir_target")
        val dailyContentSeed = intPreferencesKey("daily_content_seed")
    }

    val settings: Flow<AppSettings> =
        context.dataStore.data
            .map { it.toSettings() }
            .distinctUntilChanged()

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        context.dataStore.edit { prefs ->
            val updated = prefs.toSettings().let(transform)
            prefs.write(updated)
        }
    }

    suspend fun current(): AppSettings = settings.first()

    private fun Preferences.toSettings(): AppSettings {
        val d = AppSettings()
        return AppSettings(
            method = CalculationMethod.fromKey(this[Keys.method]),
            madhab = AsrMadhab.fromKey(this[Keys.madhab]),
            highLatitudeRule = HighLatitudeRule.fromKey(this[Keys.highLatitude]),
            corrections = decodeCorrections(this[Keys.corrections]),
            hicriOffsetDays = orDefault(Keys.hicriOffset, d.hicriOffsetDays),
            notificationEnabled = orDefault(Keys.notificationsEnabled, d.notificationEnabled),
            notificationModes = decodeModes(this[Keys.notificationModes]) ?: d.notificationModes,
            preReminderEnabled = orDefault(Keys.preReminderEnabled, d.preReminderEnabled),
            preReminderMinutes = orDefault(Keys.preReminderMinutes, d.preReminderMinutes),
            ezanVolumePercent = orDefault(Keys.ezanVolume, d.ezanVolumePercent),
            vibrationEnabled = orDefault(Keys.vibration, d.vibrationEnabled),
            salahTrackingEnabled = orDefault(Keys.salahTracking, d.salahTrackingEnabled),
            diniGunNotifications = orDefault(Keys.diniGunNotifications, d.diniGunNotifications),
            camiModeEnabled = orDefault(Keys.camiMode, d.camiModeEnabled),
            themeMode = enumOrDefault(this[Keys.themeMode], ThemeMode.entries, d.themeMode) { it.name },
            dynamicColor = orDefault(Keys.dynamicColor, d.dynamicColor),
            language = enumOrDefault(this[Keys.language], AppLanguage.entries, d.language) { it.tag },
            fontScale = orDefault(Keys.fontScale, d.fontScale),
            onlineComparisonEnabled = orDefault(Keys.onlineComparison, d.onlineComparisonEnabled),
            onboardingCompleted = orDefault(Keys.onboarding, d.onboardingCompleted),
            lastZikir = orDefault(Keys.lastZikir, d.lastZikir),
            lastZikirTarget = orDefault(Keys.lastZikirTarget, d.lastZikirTarget),
            dailyContentSeed = orDefault(Keys.dailyContentSeed, d.dailyContentSeed),
        )
    }

    private fun <T> Preferences.orDefault(
        key: androidx.datastore.preferences.core.Preferences.Key<T>,
        default: T,
    ): T = this[key] ?: default

    private fun <E> enumOrDefault(
        stored: String?,
        entries: List<E>,
        default: E,
        keyOf: (E) -> String,
    ): E = stored?.let { value -> entries.firstOrNull { keyOf(it) == value } } ?: default

    private fun androidx.datastore.preferences.core.MutablePreferences.write(settings: AppSettings) {
        this[Keys.method] = settings.method.displayKey
        this[Keys.madhab] = settings.madhab.name
        this[Keys.highLatitude] = settings.highLatitudeRule.displayKey
        this[Keys.corrections] = encodeCorrections(settings.corrections)
        this[Keys.hicriOffset] = settings.hicriOffsetDays
        this[Keys.notificationsEnabled] = settings.notificationEnabled
        this[Keys.notificationModes] = encodeModes(settings.notificationModes)
        this[Keys.preReminderEnabled] = settings.preReminderEnabled
        this[Keys.preReminderMinutes] = settings.preReminderMinutes
        this[Keys.ezanVolume] = settings.ezanVolumePercent
        this[Keys.vibration] = settings.vibrationEnabled
        this[Keys.salahTracking] = settings.salahTrackingEnabled
        this[Keys.diniGunNotifications] = settings.diniGunNotifications
        this[Keys.camiMode] = settings.camiModeEnabled
        this[Keys.themeMode] = settings.themeMode.name
        this[Keys.dynamicColor] = settings.dynamicColor
        this[Keys.language] = settings.language.tag
        this[Keys.fontScale] = settings.fontScale
        this[Keys.onlineComparison] = settings.onlineComparisonEnabled
        this[Keys.onboarding] = settings.onboardingCompleted
        this[Keys.lastZikir] = settings.lastZikir
        this[Keys.lastZikirTarget] = settings.lastZikirTarget
        this[Keys.dailyContentSeed] = settings.dailyContentSeed
    }

    private fun encodeCorrections(corrections: Map<Prayer, Int>): String =
        Prayer.ALL.joinToString(",") { "${it.name}=${corrections[it] ?: 0}" }

    private fun decodeCorrections(raw: String?): Map<Prayer, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(',').mapNotNull { entry ->
            val parts = entry.split('=')
            if (parts.size != 2) return@mapNotNull null
            val prayer = Prayer.entries.firstOrNull { it.name == parts[0] } ?: return@mapNotNull null
            val value = parts[1].toIntOrNull() ?: return@mapNotNull null
            if (value == 0) null else prayer to value
        }.toMap()
    }

    private fun encodeModes(modes: Map<Prayer, NotificationMode>): String =
        Prayer.ALL.joinToString(",") { "${it.name}=${(modes[it] ?: NotificationMode.SILENT).name}" }

    private fun decodeModes(raw: String?): Map<Prayer, NotificationMode>? {
        if (raw.isNullOrBlank()) return null
        return raw.split(',').mapNotNull { entry ->
            val parts = entry.split('=')
            if (parts.size != 2) return@mapNotNull null
            val prayer = Prayer.entries.firstOrNull { it.name == parts[0] } ?: return@mapNotNull null
            prayer to NotificationMode.fromKey(parts[1])
        }.toMap().ifEmpty { null }
    }
}
