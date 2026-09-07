package de.tobias.emojitagebuch.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/**
 * Emoji-Darstellung ohne knappe Zeilenhöhe. Samsungs Emoji-Glyphen sind höher als
 * eine normale Textzeile; mit fester lineHeight schneidet Compose sie oben und unten ab.
 */
@Composable
fun EmojiText(
    emoji: String,
    size: TextUnit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = emoji,
        modifier = modifier,
        fontSize = size,
        lineHeight = size * 1.3f,
        textAlign = TextAlign.Center,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Visible,
        style = TextStyle(
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.None,
            ),
        ),
    )
}
