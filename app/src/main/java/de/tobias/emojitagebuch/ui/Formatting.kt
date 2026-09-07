package de.tobias.emojitagebuch.ui

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

val AppLocale: Locale = Locale.GERMAN

fun YearMonth.title(): String =
    "${month.getDisplayName(TextStyle.FULL_STANDALONE, AppLocale)} $year"

fun YearMonth.shortTitle(): String =
    month.getDisplayName(TextStyle.FULL_STANDALONE, AppLocale)

private val longDate: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", AppLocale)

fun LocalDate.longTitle(): String = format(longDate)

fun DayOfWeek.shortName(): String =
    getDisplayName(TextStyle.SHORT_STANDALONE, AppLocale).trimEnd('.')
