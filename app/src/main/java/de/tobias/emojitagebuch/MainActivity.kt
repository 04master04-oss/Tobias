package de.tobias.emojitagebuch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.tobias.emojitagebuch.reminders.ReminderScheduler
import de.tobias.emojitagebuch.settings.SettingsStore
import de.tobias.emojitagebuch.ui.theme.EmojiTagebuchTheme

class MainActivity : ComponentActivity() {

    /** Datum (ISO), das aus einer Benachrichtigung heraus geöffnet werden soll. */
    private var requestedDate by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        requestedDate = intent?.getStringExtra(EXTRA_DATE)
        val settingsStore = SettingsStore.get(this)
        ReminderScheduler.sync(this)

        setContent {
            val settings by settingsStore.state.collectAsStateWithLifecycle()

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { ReminderScheduler.sync(this) }

            // Beim ersten Start (und wenn Erinnerungen aktiv sind) um Erlaubnis für Benachrichtigungen bitten.
            LaunchedEffect(settings.anyReminderEnabled) {
                if (settings.anyReminderEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val granted = ContextCompat.checkSelfPermission(
                        this@MainActivity, Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!granted) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            // Alarme neu setzen, sobald sich Erinnerungseinstellungen ändern.
            LaunchedEffect(
                settings.eveningEnabled, settings.eveningHour, settings.eveningMinute,
                settings.morningEnabled, settings.morningHour, settings.morningMinute,
            ) {
                ReminderScheduler.sync(this@MainActivity)
            }

            EmojiTagebuchTheme(
                appearance = settings.appearance,
                accent = settings.accentArgb?.let { Color(it) },
            ) {
                EmojiTagebuchApp(
                    requestedDate = requestedDate,
                    onRequestConsumed = { requestedDate = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(EXTRA_DATE)?.let { requestedDate = it }
    }

    companion object {
        const val EXTRA_DATE = "open_date"
    }
}
