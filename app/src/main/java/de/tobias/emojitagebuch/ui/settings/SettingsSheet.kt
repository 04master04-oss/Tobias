package de.tobias.emojitagebuch.ui.settings

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.tobias.emojitagebuch.settings.AppSettings
import de.tobias.emojitagebuch.settings.Appearance
import de.tobias.emojitagebuch.ui.theme.PresetAccents

/**
 * Einstellungen: Hell/Dunkel und Akzentfarbe (Systemfarbe, 16 Vorgaben oder frei per Regler).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    settings: AppSettings,
    onAppearance: (Appearance) -> Unit,
    onAccent: (Int?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentAccent = settings.accentArgb?.let { Color(it) }

    // Regler-Zustand aus der gespeicherten Farbe ableiten
    val hsv = remember(settings.accentArgb) {
        val out = FloatArray(3)
        android.graphics.Color.colorToHSV(settings.accentArgb ?: PresetAccents.first().second.toArgb(), out)
        out
    }
    var hue by remember(settings.accentArgb) { mutableFloatStateOf(hsv[0]) }
    var saturation by remember(settings.accentArgb) { mutableFloatStateOf(hsv[1].coerceIn(0.05f, 1f)) }

    fun applySlider() {
        onAccent(Color.hsv(hue, saturation, 0.95f).toArgb())
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
        ) {
            Text("Aussehen", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))

            Text("Hell oder dunkel", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                Appearance.entries.forEachIndexed { index, appearance ->
                    SegmentedButton(
                        selected = settings.appearance == appearance,
                        onClick = { onAppearance(appearance) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = Appearance.entries.size),
                    ) {
                        Text(appearance.label)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Farbe", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            FilterChip(
                selected = currentAccent == null,
                onClick = { onAccent(null) },
                label = { Text("Systemfarbe des Handys") },
                leadingIcon = if (currentAccent == null) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null,
            )
            Spacer(Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PresetAccents.forEach { (name, color) ->
                    val selected = currentAccent?.toArgb() == color.toArgb()
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selected) 3.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Black.copy(alpha = 0.15f),
                                shape = CircleShape,
                            )
                            .clickable { onAccent(color.toArgb()) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) {
                            Icon(Icons.Filled.Check, contentDescription = name, tint = Color.Black.copy(alpha = 0.75f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Eigene Farbe", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.hsv(hue, saturation, 0.95f))
                        .border(1.dp, Color.Black.copy(alpha = 0.15f), CircleShape),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text("Farbton", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            GradientSlider(
                value = hue,
                valueRange = 0f..360f,
                brush = Brush.horizontalGradient(
                    (0..12).map { Color.hsv(it * 30f, saturation, 0.95f) },
                ),
                onValueChange = { hue = it },
                onValueChangeFinished = { applySlider() },
            )
            Text("Kräftigkeit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            GradientSlider(
                value = saturation,
                valueRange = 0.05f..1f,
                brush = Brush.horizontalGradient(
                    listOf(Color.hsv(hue, 0.05f, 0.95f), Color.hsv(hue, 1f, 0.95f)),
                ),
                onValueChange = { saturation = it },
                onValueChangeFinished = { applySlider() },
            )

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Fertig") }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GradientSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    brush: Brush,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(brush),
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent,
                thumbColor = MaterialTheme.colorScheme.onSurface,
            ),
        )
    }
}
