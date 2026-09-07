package de.tobias.emojitagebuch.model

/**
 * Ein auswählbares Emoji mit einem Stimmungswert von 1 (sehr schlecht) bis 5 (sehr gut).
 * Der Wert wird zusammen mit dem Eintrag gespeichert, damit Statistiken stabil bleiben,
 * auch wenn der Katalog später verändert wird.
 */
data class MoodEmoji(
    val emoji: String,
    val label: String,
    val score: Int,
)

data class MoodCategory(
    val title: String,
    val score: Int,
    val emojis: List<MoodEmoji>,
)

object MoodCatalog {

    private fun cat(title: String, score: Int, vararg pairs: Pair<String, String>) =
        MoodCategory(title, score, pairs.map { MoodEmoji(it.first, it.second, score) })

    val categories: List<MoodCategory> = listOf(
        cat(
            "Super", 5,
            "😂" to "Lachend", "🤩" to "Begeistert", "😍" to "Verliebt", "🥳" to "Feiernd",
            "😎" to "Cool", "🤗" to "Herzlich", "😁" to "Strahlend", "🥰" to "Geliebt",
        ),
        cat(
            "Gut", 4,
            "😊" to "Fröhlich", "🙂" to "Zufrieden", "😌" to "Entspannt", "😇" to "Friedlich",
            "😋" to "Genießend", "😉" to "Verschmitzt", "😄" to "Lächelnd", "🥲" to "Gerührt",
        ),
        cat(
            "Okay", 3,
            "😐" to "Neutral", "🤔" to "Nachdenklich", "😴" to "Müde", "🙄" to "Genervt",
            "😬" to "Angespannt", "🤷" to "Egal", "😑" to "Ausdruckslos", "🫤" to "Unschlüssig",
        ),
        cat(
            "Schlecht", 2,
            "🙁" to "Unzufrieden", "😔" to "Niedergeschlagen", "😢" to "Traurig", "😕" to "Verwirrt",
            "😞" to "Enttäuscht", "😩" to "Erschöpft", "😰" to "Ängstlich", "🤒" to "Krank",
        ),
        cat(
            "Mies", 1,
            "😭" to "Weinend", "😡" to "Wütend", "😤" to "Frustriert", "😱" to "Panisch",
            "😫" to "Am Ende", "💔" to "Gebrochen", "🤢" to "Übel", "🤯" to "Überfordert",
        ),
    )

    val all: List<MoodEmoji> = categories.flatMap { it.emojis }

    fun find(emoji: String): MoodEmoji? = all.firstOrNull { it.emoji == emoji }

    /** Repräsentatives Emoji für einen (gerundeten) Durchschnittswert. */
    fun representative(score: Double): String = when {
        score >= 4.5 -> "😁"
        score >= 3.5 -> "🙂"
        score >= 2.5 -> "😐"
        score >= 1.5 -> "😔"
        else -> "😭"
    }

    fun scoreLabel(score: Int): String = categories.firstOrNull { it.score == score }?.title ?: "?"
}
