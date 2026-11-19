package com.naimpro.app.data.di

import android.content.Context
import androidx.room.Room
import com.naimpro.app.alarm.PrayerAlarmScheduler
import com.naimpro.app.data.backup.BackupManager
import com.naimpro.app.data.city.CityRepository
import com.naimpro.app.data.local.NaimDatabase
import com.naimpro.app.data.prefs.SettingsRepository
import com.naimpro.app.data.repo.LocationRepository
import com.naimpro.app.data.repo.PrayerRepository
import com.naimpro.app.data.repo.SalahRepository
import com.naimpro.app.data.repo.ZikirRepository
import com.naimpro.app.domain.prayer.PrayerTimeCalculator
import com.naimpro.app.widget.WidgetRefresher
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

/**
 * Hand rolled dependency container.
 *
 * Hilt would add a compiler plugin, generated code and ~400 kB to the APK for a
 * graph this small, so a plain object graph is the lighter and faster choice.
 * Everything is lazy, so a cold start only pays for what the first screen uses.
 */
class AppContainer(context: Context, private val appScope: CoroutineScope) {
    private val appContext = context.applicationContext

    /** Exposed for small, context bound helpers (location lookup, sharing). */
    val context: Context get() = appContext

    val widgets: WidgetRefresher by lazy { WidgetRefresher(appContext, appScope) }

    val json: Json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        }

    val calculator: PrayerTimeCalculator by lazy { PrayerTimeCalculator() }

    val database: NaimDatabase by lazy {
        Room.databaseBuilder(appContext, NaimDatabase::class.java, NaimDatabase.NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    val settings: SettingsRepository by lazy { SettingsRepository(appContext) }

    val locations: LocationRepository by lazy { LocationRepository(appContext, json) }

    val cities: CityRepository by lazy { CityRepository(appContext) }

    val prayers: PrayerRepository by lazy { PrayerRepository(database, calculator) }

    val salah: SalahRepository by lazy { SalahRepository(database) }

    val zikir: ZikirRepository by lazy { ZikirRepository(database) }

    val backup: BackupManager by lazy {
        BackupManager(appContext, settings, database, json)
    }

    val alarms: PrayerAlarmScheduler by lazy {
        PrayerAlarmScheduler(appContext, prayers, settings, locations, appScope)
    }
}
