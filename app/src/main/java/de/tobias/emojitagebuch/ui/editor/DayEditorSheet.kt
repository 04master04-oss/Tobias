package de.tobias.emojitagebuch.ui.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tobias.emojitagebuch.data.DayEntry
import de.tobias.emojitagebuch.data.EmojiUsage
import de.tobias.emojitagebuch.model.MoodCatalog
import de.tobias.emojitagebuch.model.MoodEmoji
import de.tobias.emojitagebuch.ui.EmojiText
import de.tobias.emojitagebuch.ui.longTitle
import de.tobias.emojitagebuch.ui.theme.moodColor
import java.time.LocalDate

/**
 * Bottom Sheet zum Setzen eines Emojis und einer Notiz für einen Tag.
 * Oben stehen Favoriten (per langem Drücken markiert) und häufig verwendete Emojis,
 * die vollständige Liste lässt sich aufklappen.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DayEditorSheet(
    date: LocalDate,
    existing: DayEntry?,
    favorites: List<String>,
    usage: List<EmojiUsage>,
    onToggleFavorite: (String) -> Unit,
    onSave: (emoji: MoodEmoji, note: String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedEmoji by rememberSaveable(date) { mutableStateOf(existing?.emoji) }
    var note by rememberSaveable(date) { mutableStateOf(existing?.note ?: "") }

    // Emoji, das nicht mehr im Katalog steht (alter Eintrag), bleibt trotzdem auswählbar.
    val selected: MoodEmoji? = selectedEmoji?.let { e ->
        MoodCatalog.find(e) ?: existing?.takeIf { it.emoji == e }
            ?.let { MoodEmoji(it.emoji, MoodCatalog.scoreLabel(it.score), it.score) }
    }

    val favoriteMoods = favorites.mapNotNull { MoodCatalog.find(it) }
    val frequentMoods = usage
        .asSequence()
        .filter { it.emoji !in favorites }
        .mapNotNull { MoodCatalog.find(it.emoji) }
        .take(8)
        .toList()
    val hasQuickPicks = favoriteMoods.isNotEmpty() || frequentMoods.isNotEmpty()
    var showAll by rememberSaveable(date) { mutableStateOf(!hasQuickPicks) }
    val haptic = LocalHapticFeedback.current

    val chip: @Composable (MoodEmoji) -> Unit = { mood ->
        EmojiChip(
            mood = mood,
            selected = selected?.emoji == mood.emoji,
            isFavorite = mood.emoji in favorites,
            onClick = { selectedEmoji = mood.emoji },
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleFavorite(mood.emoji)
            },
        )
    }

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
                    EmojiText(emoji = selected?.emoji ?: "❔", size = 40.sp)
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

            // Favoriten
            SectionTitle(
                title = "Favoriten",
                icon = { Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary) },
            )
            Spacer(Modifier.height(6.dp))
            if (favoriteMoods.isEmpty()) {
                Text(
                    text = "Halte ein Emoji gedrückt, um es hier als Favorit abzulegen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    favoriteMoods.forEach { chip(it) }
                }
            }
            Spacer(Modifier.height(14.dp))

            // Häufig verwendet
            if (frequentMoods.isNotEmpty()) {
                SectionTitle(title = "Häufig verwendet")
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    frequentMoods.forEach { chip(it) }
                }
                Spacer(Modifier.height(14.dp))
            }

            // Alle Emojis, aufklappbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showAll = !showAll }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SectionTitle(title = "Alle Emojis")
                Icon(
                    imageVector = if (showAll) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (showAll) "Zuklappen" else "Aufklappen",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (showAll) {
                Spacer(Modifier.height(6.dp))
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
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        category.emojis.forEach { chip(it) }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            } else {
                Spacer(Modifier.height(10.dp))
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
private fun SectionTitle(title: String, icon: (@Composable () -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EmojiChip(
    mood: MoodEmoji,
    selected: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .size(50.dp)
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
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center,
    ) {
        EmojiText(emoji = mood.emoji, size = 26.sp)
        if (isFavorite) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Favorit",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(12.dp),
            )
        }
    }
}
