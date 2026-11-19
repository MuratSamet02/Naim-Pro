package com.naimpro.app.data.repo

import com.naimpro.app.data.local.CachedPrayerTimesEntity
import com.naimpro.app.data.local.NaimDatabase
import com.naimpro.app.data.prefs.AppSettings
import com.naimpro.app.domain.model.Location
import com.naimpro.app.domain.model.Prayer
import com.naimpro.app.domain.model.PrayerTimes
import com.naimpro.app.domain.prayer.PrayerTimeCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Computes prayer times and keeps a rolling 30 day cache in Room.
 *
 * The calculation itself is cheap, but the cache keeps widget updates and alarm
 * rescheduling independent of the calculator and gives us a single place to
 * purge stale rows. All heavy work happens on [Dispatchers.Default].
 */
class PrayerRepository(
    private val database: NaimDatabase,
    private val calculator: PrayerTimeCalculator,
) {
    suspend fun timesFor(
        date: LocalDate,
        location: Location,
        settings: AppSettings,
    ): PrayerTimes =
        withContext(Dispatchers.Default) {
            val base =
                calculator.calculate(
                    date = date,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    zone = location.zoneId,
                    parameters = settings.parameters,
                )
            base.withCorrections(settings.corrections)
        }

    /** Warms (and reads from) the persistent cache for [date]. */
    suspend fun timesForCached(
        date: LocalDate,
        location: Location,
        settings: AppSettings,
    ): PrayerTimes =
        withContext(Dispatchers.Default) {
            val dao = database.prayerTimesDao()
            val key = cacheKey(location, settings)
            val existing = dao.cachedRange(location.id, key, date.toString(), date.toString()).firstOrNull()
            if (existing != null) return@withContext existing.toPrayerTimes(location.zoneId)

            val range =
                calculator.calculateRange(
                    startDate = date,
                    days = CACHE_DAYS,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    zone = location.zoneId,
                    parameters = settings.parameters,
                ).map { it.withCorrections(settings.corrections) }

            val now = System.currentTimeMillis()
            dao.upsertAll(range.map { it.toEntity(location, key, now) })
            dao.purgeBefore(date.minusDays(2).toString())
            range.first()
        }

    suspend fun monthTimes(
        year: Int,
        month: Int,
        location: Location,
        settings: AppSettings,
    ): List<PrayerTimes> =
        withContext(Dispatchers.Default) {
            val first = LocalDate.of(year, month, 1)
            val days = first.lengthOfMonth()
            calculator.calculateRange(
                startDate = first,
                days = days,
                latitude = location.latitude,
                longitude = location.longitude,
                zone = location.zoneId,
                parameters = settings.parameters,
            ).map { it.withCorrections(settings.corrections) }
        }

    suspend fun clearCache() =
        withContext(Dispatchers.IO) {
            database.prayerTimesDao().clear()
        }

    private fun cacheKey(
        location: Location,
        settings: AppSettings,
    ): String =
        buildString {
            append(settings.method.name)
            append('|').append(settings.madhab.name)
            append('|').append(settings.highLatitudeRule.name)
            append('|').append(location.latitude)
            append('|').append(location.longitude)
            append('|').append(
                settings.corrections.entries.sortedBy { it.key.ordinal }
                    .joinToString(",") { "${it.key.name}${it.value}" },
            )
        }

    private fun PrayerTimes.toEntity(
        location: Location,
        key: String,
        now: Long,
    ): CachedPrayerTimesEntity {
        fun LocalDateTime.toMillis(): Long = atZone(location.zoneId).toInstant().toEpochMilli()
        return CachedPrayerTimesEntity(
            locationKey = location.id,
            date = date.toString(),
            paramsKey = key,
            imsak = imsak.toMillis(),
            gunes = gunes.toMillis(),
            ogle = ogle.toMillis(),
            ikindi = ikindi.toMillis(),
            aksam = aksam.toMillis(),
            yatsi = yatsi.toMillis(),
            zoneId = location.zoneId.id,
            cachedAt = now,
        )
    }

    private fun CachedPrayerTimesEntity.toPrayerTimes(fallbackZone: ZoneId): PrayerTimes {
        val zone = runCatching { ZoneId.of(zoneId) }.getOrDefault(fallbackZone)

        fun Long.toLocal(): LocalDateTime = Instant.ofEpochMilli(this).atZone(zone).toLocalDateTime()
        return PrayerTimes(
            date = LocalDate.parse(date),
            imsak = imsak.toLocal(),
            gunes = gunes.toLocal(),
            ogle = ogle.toLocal(),
            ikindi = ikindi.toLocal(),
            aksam = aksam.toLocal(),
            yatsi = yatsi.toLocal(),
        )
    }

    companion object {
        const val CACHE_DAYS = 30
        val PRAYER_ORDER: List<Prayer> = Prayer.ALL
    }
}
