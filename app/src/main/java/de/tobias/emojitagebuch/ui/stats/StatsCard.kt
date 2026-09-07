package de.tobias.emojitagebuch.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tobias.emojitagebuch.data.DayEntry
import de.tobias.emojitagebuch.ui.EmojiText
import de.tobias.emojitagebuch.model.MoodCatalog
import de.tobias.emojitagebuch.model.MoodStats
import de.tobias.emojitagebuch.ui.theme.moodColor
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

@Composable
fun MonthStatsCard(
    month: YearMonth,
    entries: Map<LocalDate, DayEntry>,
    modifier: Modifier = Modifier,
) {
    val stats = MoodStats.ofMonth(entries.values, month)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Monatsbilanz",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))

            if (!stats.hasData) {
                Text(
                    text = "Noch keine Einträge in diesem Monat. Tippe auf einen Tag, um ein Emoji zu setzen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                return@Column
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiText(emoji = stats.averageEmoji ?: "", size = 44.sp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Durchschnitt: ${formatScore(stats.averageScore)} von 5",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "${stats.entryCount} von ${stats.totalDays} Tagen eingetragen (${stats.coveragePercent} %)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            SentimentBar(stats)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Legend(color = moodColor(5), text = "Fröhlich ${stats.positivePercent} %")
                Legend(color = moodColor(3), text = "Okay ${stats.neutralPercent} %")
                Legend(color = moodColor(1), text = "Schlecht ${stats.negativePercent} %")
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Verlauf",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            TrendBars(month, entries)

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Deine Emojis",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            stats.emojiShares.forEach { share ->
                EmojiShareRow(share.emoji, share.label, share.count, share.percent)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

fun formatScore(score: Double?): String =
    if (score == null) "–" else String.format(Locale.GERMAN, "%.1f", score)

@Composable
fun SentimentBar(stats: MoodStats, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        (5 downTo 1).forEach { score ->
            val count = stats.scoreShares[score] ?: 0
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .weight(count.toFloat())
                        .fillMaxHeight()
                        .background(moodColor(score)),
                )
            }
        }
    }
}

@Composable
private fun Legend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color),
        )
        Spacer(Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TrendBars(month: YearMonth, entries: Map<LocalDate, DayEntry>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        (1..month.lengthOfMonth()).forEach { day ->
            val entry = entries[month.atDay(day)]
            val fraction = if (entry == null) 0.08f else entry.score / 5f
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                    .background(
                        if (entry == null) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        else moodColor(entry.score),
                    ),
            )
        }
    }
}

@Composable
fun EmojiShareRow(emoji: String, label: String, count: Int, percent: Int) {
    val score = MoodCatalog.find(emoji)?.score ?: 3
    Row(verticalAlignment = Alignment.CenterVertically) {
        EmojiText(emoji = emoji, size = 22.sp)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = label, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "$percent %  ·  ${count}×",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percent / 100f)
                        .fillMaxHeight()
                        .background(moodColor(score)),
                )
            }
        }
    }
}
