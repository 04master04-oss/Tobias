package de.tobias.emojitagebuch.ui.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tobias.emojitagebuch.data.DayEntry
import de.tobias.emojitagebuch.data.localDate
import de.tobias.emojitagebuch.model.MoodStats
import de.tobias.emojitagebuch.ui.shortTitle
import java.time.YearMonth

@Composable
fun YearScreen(
    year: Int,
    entries: List<DayEntry>,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onMonthClick: (YearMonth) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val byMonth = entries.groupBy { YearMonth.from(it.localDate) }
    val months = (1..12).map { YearMonth.of(year, it) }
    val yearStats = MoodStats.of(entries, if (java.time.Year.isLeap(year.toLong())) 366 else 365)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
    ) {
        item(key = "header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onPreviousYear) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Vorheriges Jahr")
                }
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                IconButton(onClick = onNextYear) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Nächstes Jahr")
                }
            }
        }
        item(key = "yearSummary") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Jahresbilanz", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    if (!yearStats.hasData) {
                        Text(
                            "Noch keine Einträge in diesem Jahr.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(yearStats.averageEmoji ?: "", fontSize = 40.sp, lineHeight = 44.sp)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Durchschnitt: ${formatScore(yearStats.averageScore)} von 5",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    "Fröhlich ${yearStats.positivePercent} % · Okay ${yearStats.neutralPercent} % · Schlecht ${yearStats.negativePercent} %",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Text(
                                    "${yearStats.entryCount} Tage eingetragen",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        SentimentBar(yearStats)
                        if (yearStats.emojiShares.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Am häufigsten",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Spacer(Modifier.height(6.dp))
                            yearStats.emojiShares.take(5).forEach { share ->
                                EmojiShareRow(share.emoji, share.label, share.count, share.percent)
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        items(months, key = { it.toString() }) { month ->
            val stats = MoodStats.ofMonth(byMonth[month].orEmpty(), month)
            MonthSummaryRow(month, stats, onClick = { onMonthClick(month) })
        }
        item(key = "bottom") { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun MonthSummaryRow(month: YearMonth, stats: MoodStats, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stats.averageEmoji ?: "·",
                fontSize = 30.sp,
                lineHeight = 34.sp,
                modifier = Modifier.width(44.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(month.shortTitle(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Text(
                        text = if (stats.hasData) "Ø ${formatScore(stats.averageScore)} · Fröhlich ${stats.positivePercent} %"
                        else "keine Einträge",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(6.dp))
                SentimentBar(stats)
            }
        }
    }
}
