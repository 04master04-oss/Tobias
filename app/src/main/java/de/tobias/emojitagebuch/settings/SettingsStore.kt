package de.tobias.emojitagebuch.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Kleine, synchrone Ablage der Einstellungen in SharedPreferences. */
class SettingsStore private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("emoji_tagebuch_settings", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(read())
    val state: StateFlow<AppSettings> = _state

    fun update(transform: (AppSettings) -> AppSettings) {
        val next = transform(_state.value)
        write(next)
        _state.value = next
    }

    private fun read(): AppSettings {
        val defaults = AppSettings()
        val appearance = runCatching {
            Appearance.valueOf(prefs.getString(KEY_APPEARANCE, null) ?: Appearance.SYSTEM.name)
        }.getOrDefault(Appearance.SYSTEM)
        val accent = if (prefs.contains(KEY_ACCENT)) prefs.getInt(KEY_ACCENT, 0) else null
        val favorites = prefs.getString(KEY_FAVORITES, "")
            .orEmpty()
            .split(SEPARATOR)
            .filter { it.isNotBlank() }
        return AppSettings(
            appearance = appearance,
            accentArgb = accent,
            favorites = favorites,
            eveningEnabled = prefs.getBoolean(KEY_EVENING_ENABLED, defaults.eveningEnabled),
            eveningHour = prefs.getInt(KEY_EVENING_HOUR, defaults.eveningHour),
            eveningMinute = prefs.getInt(KEY_EVENING_MINUTE, defaults.eveningMinute),
            morningEnabled = prefs.getBoolean(KEY_MORNING_ENABLED, defaults.morningEnabled),
            morningHour = prefs.getInt(KEY_MORNING_HOUR, defaults.morningHour),
            morningMinute = prefs.getInt(KEY_MORNING_MINUTE, defaults.morningMinute),
        )
    }

    private fun write(settings: AppSettings) {
        val editor = prefs.edit()
        editor.putString(KEY_APPEARANCE, settings.appearance.name)
        if (settings.accentArgb == null) editor.remove(KEY_ACCENT) else editor.putInt(KEY_ACCENT, settings.accentArgb)
        editor.putString(KEY_FAVORITES, settings.favorites.joinToString(SEPARATOR))
        editor.putBoolean(KEY_EVENING_ENABLED, settings.eveningEnabled)
        editor.putInt(KEY_EVENING_HOUR, settings.eveningHour)
        editor.putInt(KEY_EVENING_MINUTE, settings.eveningMinute)
        editor.putBoolean(KEY_MORNING_ENABLED, settings.morningEnabled)
        editor.putInt(KEY_MORNING_HOUR, settings.morningHour)
        editor.putInt(KEY_MORNING_MINUTE, settings.morningMinute)
        editor.apply()
    }

    companion object {
        private const val KEY_APPEARANCE = "appearance"
        private const val KEY_ACCENT = "accent_argb"
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_EVENING_ENABLED = "evening_enabled"
        private const val KEY_EVENING_HOUR = "evening_hour"
        private const val KEY_EVENING_MINUTE = "evening_minute"
        private const val KEY_MORNING_ENABLED = "morning_enabled"
        private const val KEY_MORNING_HOUR = "morning_hour"
        private const val KEY_MORNING_MINUTE = "morning_minute"
        private const val SEPARATOR = "|"

        @Volatile
        private var instance: SettingsStore? = null

        fun get(context: Context): SettingsStore =
            instance ?: synchronized(this) {
                instance ?: SettingsStore(context.applicationContext).also { instance = it }
            }
    }
}
