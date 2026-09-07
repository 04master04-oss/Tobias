package de.tobias.emojitagebuch

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.tobias.emojitagebuch.data.AppDatabase
import de.tobias.emojitagebuch.settings.SettingsStore
import de.tobias.emojitagebuch.ui.calendar.MonthScreen
import de.tobias.emojitagebuch.ui.editor.DayEditorSheet
import de.tobias.emojitagebuch.ui.settings.SettingsSheet
import de.tobias.emojitagebuch.ui.stats.YearScreen
import java.time.LocalDate

private enum class Tab(val label: String) { MONTH("Monat"), YEAR("Jahr") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiTagebuchApp(
    viewModel: MoodViewModel = viewModel {
        val app = checkNotNull(this[APPLICATION_KEY])
        MoodViewModel(AppDatabase.get(app).dayEntryDao(), SettingsStore.get(app))
    },
) {
    val month by viewModel.month.collectAsStateWithLifecycle()
    val monthEntries by viewModel.monthEntries.collectAsStateWithLifecycle()
    val year by viewModel.year.collectAsStateWithLifecycle()
    val yearEntries by viewModel.yearEntries.collectAsStateWithLifecycle()
    val usage by viewModel.emojiUsage.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableStateOf(Tab.MONTH) }
    var editingDate by rememberSaveable { mutableStateOf<String?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Emoji Tagebuch", fontWeight = FontWeight.SemiBold)
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Filled.Palette, contentDescription = "Aussehen")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.MONTH,
                    onClick = { tab = Tab.MONTH },
                    icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                    label = { Text(Tab.MONTH.label) },
                )
                NavigationBarItem(
                    selected = tab == Tab.YEAR,
                    onClick = { tab = Tab.YEAR },
                    icon = { Icon(Icons.Filled.Insights, contentDescription = null) },
                    label = { Text(Tab.YEAR.label) },
                )
            }
        },
    ) { padding ->
        when (tab) {
            Tab.MONTH -> MonthScreen(
                month = month,
                entries = monthEntries,
                onPreviousMonth = viewModel::previousMonth,
                onNextMonth = viewModel::nextMonth,
                onToday = viewModel::goToCurrentMonth,
                onDayClick = { editingDate = it.toString() },
                contentPadding = padding,
            )
            Tab.YEAR -> YearScreen(
                year = year,
                entries = yearEntries,
                onPreviousYear = viewModel::previousYear,
                onNextYear = viewModel::nextYear,
                onMonthClick = { m ->
                    viewModel.showMonth(m)
                    tab = Tab.MONTH
                },
                contentPadding = padding,
            )
        }
    }

    editingDate?.let { iso ->
        val date = LocalDate.parse(iso)
        DayEditorSheet(
            date = date,
            existing = monthEntries[date],
            favorites = settings.favorites,
            usage = usage,
            onToggleFavorite = viewModel::toggleFavorite,
            onSave = { mood, note ->
                viewModel.save(date, mood.emoji, mood.score, note)
                editingDate = null
            },
            onDelete = {
                viewModel.delete(date)
                editingDate = null
            },
            onDismiss = { editingDate = null },
        )
    }

    if (showSettings) {
        SettingsSheet(
            settings = settings,
            onAppearance = viewModel::setAppearance,
            onAccent = viewModel::setAccent,
            onDismiss = { showSettings = false },
        )
    }
}
