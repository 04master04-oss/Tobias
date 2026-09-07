package de.tobias.emojitagebuch.model

import de.tobias.emojitagebuch.data.DayEntry
import java.time.YearMonth
import kotlin.math.roundToInt

data class EmojiShare(
    val emoji: String,
    val label: String,
    val count: Int,
    val percent: Int,
)

/** Auswertung eines Monats (oder einer beliebigen Menge von Einträgen). */
data class MoodStats(
    val totalDays: Int,
    val entryCount: Int,
    val averageScore: Double?,
    val positiveCount: Int,
    val neutralCount: Int,
    val negativeCount: Int,
    val emojiShares: List<EmojiShare>,
    val scoreShares: Map<Int, Int>,
) {
    val hasData: Boolean get() = entryCount > 0
    val positivePercent: Int get() = percent(positiveCount)
    val neutralPercent: Int get() = percent(neutralCount)
    val negativePercent: Int get() = percent(negativeCount)
    val coveragePercent: Int get() = if (totalDays == 0) 0 else (entryCount * 100.0 / totalDays).roundToInt()
    val averageEmoji: String? get() = averageScore?.let { MoodCatalog.representative(it) }

    private fun percent(count: Int): Int =
        if (entryCount == 0) 0 else (count * 100.0 / entryCount).roundToInt()

    companion object {
        fun of(entries: Collection<DayEntry>, totalDays: Int): MoodStats {
            val count = entries.size
            val avg = if (count == 0) null else entries.sumOf { it.score }.toDouble() / count
            val shares = entries.groupBy { it.emoji }
                .map { (emoji, list) ->
                    EmojiShare(
                        emoji = emoji,
                        label = MoodCatalog.find(emoji)?.label ?: MoodCatalog.scoreLabel(list.first().score),
                        count = list.size,
                        percent = (list.size * 100.0 / count).roundToInt(),
                    )
                }
                .sortedWith(compareByDescending<EmojiShare> { it.count }.thenBy { it.emoji })
            val scoreShares = (1..5).associateWith { s -> entries.count { it.score == s } }
            return MoodStats(
                totalDays = totalDays,
                entryCount = count,
                averageScore = avg,
                positiveCount = entries.count { it.score >= 4 },
                neutralCount = entries.count { it.score == 3 },
                negativeCount = entries.count { it.score <= 2 },
                emojiShares = shares,
                scoreShares = scoreShares,
            )
        }

        fun ofMonth(entries: Collection<DayEntry>, month: YearMonth): MoodStats =
            of(entries, month.lengthOfMonth())
    }
}
