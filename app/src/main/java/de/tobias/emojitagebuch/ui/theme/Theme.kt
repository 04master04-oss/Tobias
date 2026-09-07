package de.tobias.emojitagebuch.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import de.tobias.emojitagebuch.settings.Appearance

/** Standard-Akzent, wenn keine Systemfarbe verfügbar ist. */
val DefaultAccent = Color(0xFFFFC93C)

/** Vordefinierte Farben für die Farbwahl. */
val PresetAccents: List<Pair<String, Color>> = listOf(
    "Sonnengelb" to Color(0xFFFFC93C),
    "Orange" to Color(0xFFFF8A3D),
    "Koralle" to Color(0xFFFF5E5B),
    "Rot" to Color(0xFFE53935),
    "Pink" to Color(0xFFF06292),
    "Lila" to Color(0xFF9C6ADE),
    "Indigo" to Color(0xFF5C6BC0),
    "Blau" to Color(0xFF42A5F5),
    "Türkis" to Color(0xFF26C6DA),
    "Mint" to Color(0xFF4DB6AC),
    "Grün" to Color(0xFF66BB6A),
    "Limette" to Color(0xFF9CCC65),
    "Olive" to Color(0xFF8D9E4A),
    "Braun" to Color(0xFFA1887F),
    "Blaugrau" to Color(0xFF78909C),
    "Grau" to Color(0xFF9E9E9E),
)

private fun Color.toward(target: Color, fraction: Float): Color = lerp(this, target, fraction)

/**
 * Leitet aus einer einzigen Akzentfarbe ein komplettes Farbschema ab. Text bleibt
 * lesbar, weil Hintergründe stark aufgehellt bzw. abgedunkelt werden.
 */
fun accentColorScheme(accent: Color, dark: Boolean): ColorScheme {
    val white = Color.White
    val black = Color.Black
    return if (dark) {
        darkColorScheme(
            primary = accent.toward(white, 0.25f),
            onPrimary = accent.toward(black, 0.75f),
            primaryContainer = accent.toward(black, 0.55f),
            onPrimaryContainer = accent.toward(white, 0.85f),
            secondary = accent.toward(white, 0.45f),
            secondaryContainer = accent.toward(black, 0.62f),
            onSecondaryContainer = accent.toward(white, 0.85f),
            background = accent.toward(black, 0.90f),
            onBackground = Color(0xFFF2EEE8),
            surface = accent.toward(black, 0.90f),
            onSurface = Color(0xFFF2EEE8),
            surfaceVariant = accent.toward(black, 0.70f),
            onSurfaceVariant = accent.toward(white, 0.70f),
            surfaceContainerLowest = accent.toward(black, 0.93f),
            surfaceContainerLow = accent.toward(black, 0.86f),
            surfaceContainer = accent.toward(black, 0.82f),
            surfaceContainerHigh = accent.toward(black, 0.78f),
            surfaceContainerHighest = accent.toward(black, 0.74f),
            outline = accent.toward(white, 0.35f),
        )
    } else {
        lightColorScheme(
            primary = accent.toward(black, 0.30f),
            onPrimary = white,
            primaryContainer = accent.toward(white, 0.70f),
            onPrimaryContainer = accent.toward(black, 0.75f),
            secondary = accent.toward(black, 0.45f),
            secondaryContainer = accent.toward(white, 0.78f),
            onSecondaryContainer = accent.toward(black, 0.75f),
            background = accent.toward(white, 0.93f),
            onBackground = Color(0xFF1C1B18),
            surface = accent.toward(white, 0.93f),
            onSurface = Color(0xFF1C1B18),
            surfaceVariant = accent.toward(white, 0.72f),
            onSurfaceVariant = accent.toward(black, 0.60f),
            surfaceContainerLowest = white,
            surfaceContainerLow = accent.toward(white, 0.90f),
            surfaceContainer = accent.toward(white, 0.86f),
            surfaceContainerHigh = accent.toward(white, 0.82f),
            surfaceContainerHighest = accent.toward(white, 0.78f),
            outline = accent.toward(black, 0.40f),
        )
    }
}

@Composable
fun EmojiTagebuchTheme(
    appearance: Appearance = Appearance.SYSTEM,
    accent: Color? = null,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (appearance) {
        Appearance.SYSTEM -> isSystemInDarkTheme()
        Appearance.LIGHT -> false
        Appearance.DARK -> true
    }
    val colorScheme = when {
        accent != null -> accentColorScheme(accent, darkTheme)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> accentColorScheme(DefaultAccent, darkTheme)
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}

/** Farbe für einen Stimmungswert von 1 bis 5. */
fun moodColor(score: Int): Color = when (score) {
    5 -> Color(0xFF2E9E5B)
    4 -> Color(0xFF8BC34A)
    3 -> Color(0xFFF2C037)
    2 -> Color(0xFFF08A3E)
    else -> Color(0xFFE04B4B)
}
