package de.tobias.emojitagebuch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.tobias.emojitagebuch.ui.theme.EmojiTagebuchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            EmojiTagebuchTheme {
                EmojiTagebuchApp()
            }
        }
    }
}
