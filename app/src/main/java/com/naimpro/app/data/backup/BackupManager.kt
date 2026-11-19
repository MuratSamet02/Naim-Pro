package com.naimpro.app.data.backup

import android.content.Context
import android.net.Uri
import com.naimpro.app.data.local.NaimDatabase
import com.naimpro.app.data.prefs.AppLanguage
import com.naimpro.app.data.prefs.AppSettings
import com.naimpro.app.data.prefs.NotificationMode
import com.naimpro.app.data.prefs.SettingsRepository
import com.naimpro.app.data.prefs.ThemeMode
import com.naimpro.app.domain.model.Prayer
import com.naimpro.app.domain.prayer.AsrMadhab
import com.naimpro.app.domain.prayer.CalculationMethod
import com.naimpro.app.domain.prayer.HighLatitudeRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant

@Serializable
data class BackupPayload(
    val schema: Int = 1,
    val createdAt: String = Instant.now().toString(),
    val appVersion: String = "1.0.0",
    val settings: BackupSettings = BackupSettings(),
    val salahLog: List<BackupSalah> = emptyList(),
    val zikir: List<BackupZikir> = emptyList(),
)

@Serializable
data class BackupSettings(
    val method: String = "DIYANET",
    val madhab: String = "STANDARD",
    val highLatitudeRule: String = "NONE",
    val corrections: Map<String, Int> = emptyMap(),
    val hicriOffsetDays: Int = 0,
    val notificationEnabled: Boolean = true,
    val notificationModes: Map<String, String> = emptyMap(),
    val preReminderEnabled: Boolean = false,
    val preReminderMinutes: Int = 0,
    val ezanVolumePercent: Int = 80,
    val vibrationEnabled: Boolean = true,
    val salahTrackingEnabled: Boolean = true,
    val diniGunNotifications: Boolean = false,
    val camiModeEnabled: Boolean = false,
    val themeMode: String = "SYSTEM",
    val dynamicColor: Boolean = true,
    val language: String = "tr",
    val fontScale: Float = 1.0f,
)

@Serializable
data class BackupSalah(val date: String, val prayer: String, val status: String)

@Serializable
data class BackupZikir(val startedAt: Long, val phrase: String, val count: Int, val target: Int, val completed: Boolean)

/** Exports and restores every user setting plus local history as plain JSON. */
class BackupManager(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val database: NaimDatabase,
    private val json: Json,
) {
    suspend fun export(uri: Uri): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                val settings = settingsRepository.current()
                val salah = database.salahLogDao().recent(365 * 6)
                val zikir = database.zikirDao().recent(500)
                val payload =
                    BackupPayload(
                        settings = settings.toBackup(),
                        salahLog = salah.map { BackupSalah(it.date, it.prayer, it.status) },
                        zikir =
                            zikir.map {
                                BackupZikir(it.startedAt, it.phrase, it.count, it.target, it.completed)
                            },
                    )
                val text = json.encodeToString(BackupPayload.serializer(), payload)
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(text.toByteArray(Charsets.UTF_8))
                } ?: error("Unable to open the destination file")
                payload.salahLog.size + payload.zikir.size
            }
        }

    suspend fun import(uri: Uri): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                val text =
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.readBytes().toString(Charsets.UTF_8)
                    } ?: error("Unable to read the selected file")

                val payload = json.decodeFromString(BackupPayload.serializer(), text)
                settingsRepository.update { payload.settings.toSettings(it) }

                val dao = database.salahLogDao()
                payload.salahLog.forEach { row ->
                    val date =
                        runCatching { java.time.LocalDate.parse(row.date) }.getOrNull()
                            ?: return@forEach
                    val prayer = Prayer.entries.firstOrNull { it.name == row.prayer } ?: return@forEach
                    dao.upsert(
                        com.naimpro.app.data.local.SalahLogEntity(
                            date = date.toString(),
                            prayer = prayer.name,
                            status = row.status,
                            updatedAt = System.currentTimeMillis(),
                        ),
                    )
                }

                val zikirDao = database.zikirDao()
                payload.zikir.forEach { row ->
                    zikirDao.insert(
                        com.naimpro.app.data.local.ZikirSessionEntity(
                            startedAt = row.startedAt,
                            phrase = row.phrase,
                            count = row.count,
                            target = row.target,
                            completed = row.completed,
                        ),
                    )
                }
                payload.salahLog.size + payload.zikir.size
            }
        }

    private fun AppSettings.toBackup(): BackupSettings =
        BackupSettings(
            method = method.name,
            madhab = madhab.name,
            highLatitudeRule = highLatitudeRule.name,
            corrections = corrections.entries.associate { it.key.name to it.value },
            hicriOffsetDays = hicriOffsetDays,
            notificationEnabled = notificationEnabled,
            notificationModes = notificationModes.entries.associate { it.key.name to it.value.name },
            preReminderEnabled = preReminderEnabled,
            preReminderMinutes = preReminderMinutes,
            ezanVolumePercent = ezanVolumePercent,
            vibrationEnabled = vibrationEnabled,
            salahTrackingEnabled = salahTrackingEnabled,
            diniGunNotifications = diniGunNotifications,
            camiModeEnabled = camiModeEnabled,
            themeMode = themeMode.name,
            dynamicColor = dynamicColor,
            language = language.tag,
            fontScale = fontScale,
        )

    private fun BackupSettings.toSettings(current: AppSettings): AppSettings =
        current.copy(
            method = CalculationMethod.entries.firstOrNull { it.name == method } ?: current.method,
            madhab = AsrMadhab.entries.firstOrNull { it.name == madhab } ?: current.madhab,
            highLatitudeRule =
                HighLatitudeRule.entries.firstOrNull { it.name == highLatitudeRule }
                    ?: current.highLatitudeRule,
            corrections =
                corrections.mapNotNull { (key, value) ->
                    Prayer.entries.firstOrNull { it.name == key }?.let { it to value }
                }.toMap(),
            hicriOffsetDays = hicriOffsetDays,
            notificationEnabled = notificationEnabled,
            notificationModes =
                notificationModes.mapNotNull { (key, value) ->
                    Prayer.entries.firstOrNull { it.name == key }?.let { it to NotificationMode.fromKey(value) }
                }.toMap().ifEmpty { current.notificationModes },
            preReminderEnabled = preReminderEnabled,
            preReminderMinutes = preReminderMinutes,
            ezanVolumePercent = ezanVolumePercent,
            vibrationEnabled = vibrationEnabled,
            salahTrackingEnabled = salahTrackingEnabled,
            diniGunNotifications = diniGunNotifications,
            camiModeEnabled = camiModeEnabled,
            themeMode = ThemeMode.entries.firstOrNull { it.name == themeMode } ?: current.themeMode,
            dynamicColor = dynamicColor,
            language = AppLanguage.entries.firstOrNull { it.tag == language } ?: current.language,
            fontScale = fontScale.coerceIn(0.85f, 1.6f),
        )
}
