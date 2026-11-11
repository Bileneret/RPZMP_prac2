package ua.kpi.iot_lighting.prac2.ui.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ukLocale = Locale.forLanguageTag("uk-UA")
private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", ukLocale)
private val dateOnlyFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", ukLocale)
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", ukLocale)
private val dayFormatter = DateTimeFormatter.ofPattern("dd.MM", ukLocale)

fun formatKwh(value: Double): String = String.format(ukLocale, "%.2f", value)

fun formatMoney(value: Double): String = String.format(ukLocale, "%.2f ₴", value)

fun formatPower(value: Double): String =
    if (value % 1.0 == 0.0) "${value.toInt()} Вт"
    else String.format(ukLocale, "%.1f Вт", value)

/** Компактне число без зайвих нулів: 6.0 -> «6», 6.5 -> «6,5». */
fun formatNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format(ukLocale, "%.1f", value)

fun formatDate(timestamp: Long): String =
    dateTimeFormatter.format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()))

fun formatDateOnly(timestamp: Long): String =
    dateOnlyFormatter.format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()))

fun formatTime(timestamp: Long): String =
    timeFormatter.format(Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()))

fun formatDay(day: String): String = runCatching {
    LocalDate.parse(day).format(dayFormatter)
}.getOrDefault(day)
