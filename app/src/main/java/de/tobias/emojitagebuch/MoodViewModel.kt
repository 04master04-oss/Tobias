package de.tobias.emojitagebuch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.tobias.emojitagebuch.data.DayEntry
import de.tobias.emojitagebuch.data.DayEntryDao
import de.tobias.emojitagebuch.data.localDate
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
class MoodViewModel(private val dao: DayEntryDao) : ViewModel() {

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month

    private val _year = MutableStateFlow(Year.now().value)
    val year: StateFlow<Int> = _year

    /** Einträge des aktuell angezeigten Monats, nach Datum abrufbar. */
    val monthEntries: StateFlow<Map<LocalDate, DayEntry>> = _month
        .flatMapLatest { m -> dao.observeByPrefix(m.toString()) }
        .map { list -> list.associateBy { it.localDate } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    /** Alle Einträge des aktuell angezeigten Jahres. */
    val yearEntries: StateFlow<List<DayEntry>> = _year
        .flatMapLatest { y -> dao.observeByPrefix(y.toString()) }
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
}
