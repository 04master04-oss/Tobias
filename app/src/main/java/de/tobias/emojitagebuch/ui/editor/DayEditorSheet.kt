package de.tobias.emojitagebuch.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tobias.emojitagebuch.data.DayEntry
import de.tobias.emojitagebuch.model.MoodCatalog
import de.tobias.emojitagebuch.model.MoodEmoji
import de.tobias.emojitagebuch.ui.longTitle
import de.tobias.emojitagebuch.ui.theme.moodColor
import java.time.LocalDate

/**
 * Bottom Sheet zum Setzen eines Emojis und einer Notiz für einen Tag.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DayEditorSheet(
    date: LocalDate,
    existing: DayEntry?,
    onSave: (emoji: MoodEmoji, note: String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedEmoji by rememberSaveable(date) { mutableStateOf(existing?.emoji) }
    val selected: MoodEmoji? = selectedEmoji?.let { MoodCatalog.find(it) }
    var note by rememberSaveable(date) { mutableStateOf(existing?.note ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .imePadding(),
        ) {
            Text(
                text = date.longTitle(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            selected?.let { moodColor(it.score).copy(alpha = 0.2f) }
                                ?: MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = selected?.emoji ?: "❔", fontSize = 40.sp, lineHeight = 44.sp)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = selected?.label ?: "Wie war dein Tag?",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = selected?.let { "Stimmung: ${MoodCatalog.scoreLabel(it.score)} (${it.score}/5)" }
                            ?: "Wähle unten ein Emoji aus.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            MoodCatalog.categories.forEach { category ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(moodColor(category.score)),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    category.emojis.forEach { mood ->
                        EmojiChip(
                            mood = mood,
                            selected = selected?.emoji == mood.emoji,
                            onClick = { selectedEmoji = mood.emoji },
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notiz") },
                placeholder = { Text("Was ist heute passiert?") },
                minLines = 3,
                maxLines = 8,
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (existing != null) {
                    TextButton(onClick = onDelete) {
                        Text("Löschen", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                Row {
                    TextButton(onClick = onDismiss) { Text("Abbrechen") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { selected?.let { onSave(it, note) } },
                        enabled = selected != null,
                    ) {
                        Text("Speichern")
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EmojiChip(mood: MoodEmoji, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(shape)
            .background(
                if (selected) moodColor(mood.score).copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            )
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) moodColor(mood.score) else Color.Transparent,
                shape = shape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = mood.emoji, fontSize = 26.sp, lineHeight = 30.sp)
    }
}
