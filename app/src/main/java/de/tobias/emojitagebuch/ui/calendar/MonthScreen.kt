package de.tobias.emojitagebuch.ui.calendar

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tobias.emojitagebuch.data.DayEntry
import de.tobias.emojitagebuch.ui.stats.MonthStatsCard
import de.tobias.emojitagebuch.ui.title
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthScreen(
    month: YearMonth,
    entries: Map<LocalDate, DayEntry>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val isCurrentMonth = month == YearMonth.now()
    var dragTotal by remember { mutableStateOf(0f) }

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
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Vorheriger Monat")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = month.title(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (!isCurrentMonth) {
                        TextButton(onClick = onToday) { Text("Zu heute") }
                    }
                }
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Nächster Monat")
                }
            }
        }
        item(key = "calendar") {
            CalendarGrid(
                month = month,
                entries = entries,
                onDayClick = onDayClick,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .pointerInput(month) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragTotal = 0f },
                            onDragEnd = {
                                if (dragTotal < -120f) onNextMonth()
                                else if (dragTotal > 120f) onPreviousMonth()
                                dragTotal = 0f
                            },
                            onDragCancel = { dragTotal = 0f },
                            onHorizontalDrag = { _, amount -> dragTotal += amount },
                        )
                    },
            )
            Spacer(Modifier.height(16.dp))
        }
        item(key = "stats") {
            MonthStatsCard(
                month = month,
                entries = entries,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
