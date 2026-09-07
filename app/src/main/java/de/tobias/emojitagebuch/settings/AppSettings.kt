package de.tobias.emojitagebuch.settings

enum class Appearance(val label: String) {
    SYSTEM("System"),
    LIGHT("Hell"),
    DARK("Dunkel"),
}

/**
 * Nutzereinstellungen. accentArgb == null bedeutet: Systemfarbe (Material You) verwenden.
 */
data class AppSettings(
    val appearance: Appearance = Appearance.SYSTEM,
    val accentArgb: Int? = null,
    val favorites: List<String> = emptyList(),
)
