package de.tobias.emojitagebuch.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tobias.emojitagebuch.data.DayEntry
import de.tobias.emojitagebuch.ui.EmojiText
import de.tobias.emojitagebuch.ui.shortName
import de.tobias.emojitagebuch.ui.theme.moodColor
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * Monatsraster, Montag als erster Wochentag. Jeder Tag zeigt das gesetzte Emoji oder
 * die Tageszahl. Der heutige Tag ist hervorgehoben.
 */
@Composable
fun CalendarGrid(
    month: YearMonth,
    entries: Map<LocalDate, DayEntry>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val firstDay = month.atDay(1)
    val leadingBlanks = (firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val days = month.lengthOfMonth()
    val cells: List<LocalDate?> = List(leadingBlanks) { null } + (1..days).map { month.atDay(it) }
    val rows = cells.chunked(7)

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            DayOfWeek.values().forEach { dow ->
                Text(
                    text = dow.shortName(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (dow == DayOfWeek.SUNDAY) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        rows.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                week.forEach { date ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (date != null) {
                            DayCell(
                                date = date,
                                entry = entries[date],
                                isToday = date == today,
                                isFuture = date.isAfter(today),
                                onClick = { onDayClick(date) },
                            )
                        }
                    }
                }
                repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    entry: DayEntry?,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val background = when {
        entry != null -> moodColor(entry.score).copy(alpha = 0.18f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isFuture) 0.35f else 0.7f)
    }
    val borderColor = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f)
            .clip(shape)
            .background(background, shape)
            .border(2.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(top = 3.dp, bottom = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isFuture) 0.5f else 1f),
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            if (entry != null) {
                EmojiText(emoji = entry.emoji, size = 23.sp)
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)),
                )
            }
        }
        if (entry != null && entry.note.isNotBlank()) {
            Box(
                modifier = Modifier
                    .padding(bottom = 1.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        } else {
            Spacer(Modifier.size(4.dp))
        }
    }
}
