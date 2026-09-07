package de.tobias.emojitagebuch.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF7A5A00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDF8E),
    onPrimaryContainer = Color(0xFF261A00),
    secondary = Color(0xFF6B5D3F),
    secondaryContainer = Color(0xFFF5E1BB),
    onSecondaryContainer = Color(0xFF241A04),
    background = Color(0xFFFFF8F0),
    surface = Color(0xFFFFF8F0),
    surfaceVariant = Color(0xFFEDE1CF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF3C14B),
    onPrimary = Color(0xFF402D00),
    primaryContainer = Color(0xFF5C4200),
    onPrimaryContainer = Color(0xFFFFDF8E),
    secondary = Color(0xFFD8C4A0),
    secondaryContainer = Color(0xFF52452A),
    onSecondaryContainer = Color(0xFFF5E1BB),
    background = Color(0xFF17130C),
    surface = Color(0xFF17130C),
    surfaceVariant = Color(0xFF4E4635),
)

@Composable
fun EmojiTagebuchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
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
