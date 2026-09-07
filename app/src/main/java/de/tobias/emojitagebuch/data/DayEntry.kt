package de.tobias.emojitagebuch.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/** Ein Tageseintrag. Das Datum wird als ISO-String (z. B. 2026-09-07) gespeichert. */
@Entity(tableName = "day_entries")
data class DayEntry(
    @PrimaryKey val date: String,
    val emoji: String,
    val score: Int,
    val note: String = "",
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)

val DayEntry.localDate: LocalDate get() = LocalDate.parse(date)
