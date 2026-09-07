package de.tobias.emojitagebuch.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DayEntryDao {

    /** Alle Einträge, deren Datum mit dem Präfix beginnt (z. B. "2026-09" oder "2026"). */
    @Query("SELECT * FROM day_entries WHERE date LIKE :prefix || '%' ORDER BY date")
    fun observeByPrefix(prefix: String): Flow<List<DayEntry>>

    @Query("SELECT * FROM day_entries ORDER BY date")
    fun observeAll(): Flow<List<DayEntry>>

    @Upsert
    suspend fun upsert(entry: DayEntry)

    @Query("DELETE FROM day_entries WHERE date = :date")
    suspend fun delete(date: String)
}
