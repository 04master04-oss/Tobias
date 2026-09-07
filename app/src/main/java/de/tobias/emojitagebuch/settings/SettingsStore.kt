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
        val appearance = runCatching {
            Appearance.valueOf(prefs.getString(KEY_APPEARANCE, null) ?: Appearance.SYSTEM.name)
        }.getOrDefault(Appearance.SYSTEM)
        val accent = if (prefs.contains(KEY_ACCENT)) prefs.getInt(KEY_ACCENT, 0) else null
        val favorites = prefs.getString(KEY_FAVORITES, "")
            .orEmpty()
            .split(SEPARATOR)
            .filter { it.isNotBlank() }
        return AppSettings(appearance = appearance, accentArgb = accent, favorites = favorites)
    }

    private fun write(settings: AppSettings) {
        prefs.edit().apply {
            putString(KEY_APPEARANCE, settings.appearance.name)
            if (settings.accentArgb == null) remove(KEY_ACCENT) else putInt(KEY_ACCENT, settings.accentArgb)
            putString(KEY_FAVORITES, settings.favorites.joinToString(SEPARATOR))
        }.apply()
    }

    companion object {
        private const val KEY_APPEARANCE = "appearance"
        private const val KEY_ACCENT = "accent_argb"
        private const val KEY_FAVORITES = "favorites"
        private const val SEPARATOR = "|"

        @Volatile
        private var instance: SettingsStore? = null

        fun get(context: Context): SettingsStore =
            instance ?: synchronized(this) {
                instance ?: SettingsStore(context.applicationContext).also { instance = it }
            }
    }
}
