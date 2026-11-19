package com.naimpro.app.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_prayer_times", primaryKeys = ["locationKey", "date", "paramsKey"])
data class CachedPrayerTimesEntity(
    val locationKey: String,
    val date: String,
    val paramsKey: String,
    val imsak: Long,
    val gunes: Long,
    val ogle: Long,
    val ikindi: Long,
    val aksam: Long,
    val yatsi: Long,
    val zoneId: String,
    val cachedAt: Long,
)

@Entity(tableName = "salah_log", primaryKeys = ["date", "prayer"])
data class SalahLogEntity(
    val date: String,
    val prayer: String,
    val status: String,
    val updatedAt: Long,
)

@Entity(tableName = "zikir_session")
data class ZikirSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val phrase: String,
    val count: Int,
    val target: Int,
    val completed: Boolean,
)

@Dao
interface PrayerTimesDao {
    @Query(
        "SELECT * FROM cached_prayer_times WHERE locationKey = :locationKey " +
            "AND paramsKey = :paramsKey AND date >= :from AND date <= :to ORDER BY date",
    )
    suspend fun cachedRange(
        locationKey: String,
        paramsKey: String,
        from: String,
        to: String,
    ): List<CachedPrayerTimesEntity>

    @Upsert
    suspend fun upsertAll(rows: List<CachedPrayerTimesEntity>)

    @Query("DELETE FROM cached_prayer_times WHERE date < :before")
    suspend fun purgeBefore(before: String)

    @Query("DELETE FROM cached_prayer_times")
    suspend fun clear()
}

@Dao
interface SalahLogDao {
    @Query("SELECT * FROM salah_log WHERE date BETWEEN :from AND :to")
    fun observeRange(
        from: String,
        to: String,
    ): Flow<List<SalahLogEntity>>

    @Query("SELECT * FROM salah_log WHERE date BETWEEN :from AND :to")
    suspend fun range(
        from: String,
        to: String,
    ): List<SalahLogEntity>

    @Upsert
    suspend fun upsert(row: SalahLogEntity)

    @Query("SELECT * FROM salah_log ORDER BY date DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<SalahLogEntity>

    @Query("DELETE FROM salah_log")
    suspend fun clear()
}

@Dao
interface ZikirDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ZikirSessionEntity): Long

    @Query("SELECT * FROM zikir_session ORDER BY startedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<ZikirSessionEntity>>

    @Query("SELECT * FROM zikir_session ORDER BY startedAt DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<ZikirSessionEntity>

    @Query("SELECT COALESCE(SUM(count), 0) FROM zikir_session")
    fun observeTotal(): Flow<Int>

    @Query("DELETE FROM zikir_session")
    suspend fun clear()
}

@Database(
    entities = [CachedPrayerTimesEntity::class, SalahLogEntity::class, ZikirSessionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class NaimDatabase : RoomDatabase() {
    abstract fun prayerTimesDao(): PrayerTimesDao

    abstract fun salahLogDao(): SalahLogDao

    abstract fun zikirDao(): ZikirDao

    companion object {
        const val NAME = "naim.db"
    }
}
