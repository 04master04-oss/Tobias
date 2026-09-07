package de.tobias.emojitagebuch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.tobias.emojitagebuch.settings.SettingsStore
import de.tobias.emojitagebuch.ui.theme.EmojiTagebuchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val settingsStore = SettingsStore.get(this)
        setContent {
            val settings by settingsStore.state.collectAsStateWithLifecycle()
            EmojiTagebuchTheme(
                appearance = settings.appearance,
                accent = settings.accentArgb?.let { Color(it) },
            ) {
                EmojiTagebuchApp()
            }
        }
    }
}
