package com.naimpro.app.data.repo

import com.naimpro.app.data.local.NaimDatabase
import com.naimpro.app.data.local.SalahLogEntity
import com.naimpro.app.domain.model.Prayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

enum class SalahStatus { PENDING, PRAYED, MISSED, QADA }

/** Prayer tracking. Every value stays on the device; nothing is ever uploaded. */
class SalahRepository(private val database: NaimDatabase) {
    fun observeRange(
        from: LocalDate,
        to: LocalDate,
    ): Flow<Map<String, SalahStatus>> =
        database.salahLogDao().observeRange(from.toString(), to.toString())
            .map { rows -> rows.associate { "${it.date}|${it.prayer}" to parse(it.status) } }

    suspend fun setStatus(
        date: LocalDate,
        prayer: Prayer,
        status: SalahStatus,
    ) = withContext(Dispatchers.IO) {
        database.salahLogDao().upsert(
            SalahLogEntity(
                date = date.toString(),
                prayer = prayer.name,
                status = status.name,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun statuses(
        from: LocalDate,
        to: LocalDate,
    ): Map<String, SalahStatus> =
        withContext(Dispatchers.IO) {
            database.salahLogDao().range(from.toString(), to.toString())
                .associate { "${it.date}|${it.prayer}" to parse(it.status) }
        }

    suspend fun recent(days: Int = 90): List<SalahLogEntity> =
        withContext(Dispatchers.IO) {
            database.salahLogDao().recent(days * 6)
        }

    suspend fun clear() = withContext(Dispatchers.IO) { database.salahLogDao().clear() }

    private fun parse(raw: String): SalahStatus = SalahStatus.entries.firstOrNull { it.name == raw } ?: SalahStatus.PENDING

    companion object {
        fun isCounted(status: SalahStatus): Boolean = status == SalahStatus.PRAYED || status == SalahStatus.QADA
    }
}
