package com.naimpro.app.data.repo

import com.naimpro.app.data.local.NaimDatabase
import com.naimpro.app.data.local.ZikirSessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/** Tesbih / zikirmatik history, stored locally only. */
class ZikirRepository(private val database: NaimDatabase) {
    fun observeRecent(limit: Int = 30): Flow<List<ZikirSessionEntity>> = database.zikirDao().observeRecent(limit)

    fun observeTotal(): Flow<Int> = database.zikirDao().observeTotal()

    suspend fun record(
        phrase: String,
        count: Int,
        target: Int,
        completed: Boolean,
    ): Long =
        withContext(Dispatchers.IO) {
            database.zikirDao().insert(
                ZikirSessionEntity(
                    startedAt = System.currentTimeMillis(),
                    phrase = phrase,
                    count = count,
                    target = target,
                    completed = completed,
                ),
            )
        }

    suspend fun clear() = withContext(Dispatchers.IO) { database.zikirDao().clear() }

    companion object {
        val presets: List<Pair<String, Int>> =
            listOf(
                "Sübhanallah" to 33,
                "Elhamdülillah" to 33,
                "Allahuekber" to 33,
                "Lâ ilâhe illallah" to 100,
                "Estağfirullah" to 100,
                "Salavat" to 100,
            )
    }
}
