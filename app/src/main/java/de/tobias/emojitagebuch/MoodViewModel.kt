package de.tobias.emojitagebuch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tobias.emojitagebuch.data.DayEntry
import de.tobias.emojitagebuch.data.DayEntryDao
import de.tobias.emojitagebuch.data.EmojiUsage
import de.tobias.emojitagebuch.data.localDate
import de.tobias.emojitagebuch.settings.AppSettings
import de.tobias.emojitagebuch.settings.Appearance
import de.tobias.emojitagebuch.settings.SettingsStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class MoodViewModel(
    private val dao: DayEntryDao,
    private val settingsStore: SettingsStore,
) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month

    private val _year = MutableStateFlow(Year.now().value)
    val year: StateFlow<Int> = _year

    val settings: StateFlow<AppSettings> = settingsStore.state

    /** Einträge des aktuell angezeigten Monats, nach Datum abrufbar. */
    val monthEntries: StateFlow<Map<LocalDate, DayEntry>> = _month
        .flatMapLatest { m -> dao.observeByPrefix(m.toString()) }
        .map { list -> list.associateBy { it.localDate } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /** Alle Einträge des aktuell angezeigten Jahres. */
    val yearEntries: StateFlow<List<DayEntry>> = _year
        .flatMapLatest { y -> dao.observeByPrefix(y.toString()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Häufigkeit aller je verwendeten Emojis, absteigend sortiert. */
    val emojiUsage: StateFlow<List<EmojiUsage>> = dao.observeEmojiUsage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun nextMonth() { _month.value = _month.value.plusMonths(1) }
    fun previousMonth() { _month.value = _month.value.minusMonths(1) }
    fun goToCurrentMonth() { _month.value = YearMonth.now() }
    fun showMonth(month: YearMonth) { _month.value = month }

    fun nextYear() { _year.value = _year.value + 1 }
    fun previousYear() { _year.value = _year.value - 1 }

    fun save(date: LocalDate, emoji: String, score: Int, note: String) {
        viewModelScope.launch {
            dao.upsert(
                DayEntry(
                    date = date.toString(),
                    emoji = emoji,
                    score = score,
                    note = note.trim(),
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    fun delete(date: LocalDate) {
        viewModelScope.launch { dao.delete(date.toString()) }
    }

    fun toggleFavorite(emoji: String) {
        settingsStore.update { s ->
            val next = if (emoji in s.favorites) s.favorites - emoji else s.favorites + emoji
            s.copy(favorites = next)
        }
    }

    fun setAppearance(appearance: Appearance) {
        settingsStore.update { it.copy(appearance = appearance) }
    }

    /** null = Systemfarbe des Handys verwenden. */
    fun setAccent(argb: Int?) {
        settingsStore.update { it.copy(accentArgb = argb) }
    }

    fun setEveningReminder(enabled: Boolean, hour: Int? = null, minute: Int? = null) {
        settingsStore.update {
            it.copy(
                eveningEnabled = enabled,
                eveningHour = hour ?: it.eveningHour,
                eveningMinute = minute ?: it.eveningMinute,
            )
        }
    }

    fun setMorningReminder(enabled: Boolean, hour: Int? = null, minute: Int? = null) {
        settingsStore.update {
            it.copy(
                morningEnabled = enabled,
                morningHour = hour ?: it.morningHour,
                morningMinute = minute ?: it.morningMinute,
            )
        }
    }
}
