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
    /** Abends fragen: "Wie war dein Tag?" */
    val eveningEnabled: Boolean = true,
    val eveningHour: Int = 20,
    val eveningMinute: Int = 0,
    /** Morgens nachfragen, falls gestern kein Eintrag gemacht wurde. */
    val morningEnabled: Boolean = true,
    val morningHour: Int = 9,
    val morningMinute: Int = 0,
) {
    val anyReminderEnabled: Boolean get() = eveningEnabled || morningEnabled
}
