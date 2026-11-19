package com.naimpro.app.ui.util

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val clockFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun LocalDateTime.clock(): String = format(clockFormatter)

/** "2s 14dk" / "14dk" / "42sn" style countdown, localised with short units. */
fun formatCountdown(
    target: LocalDateTime,
    now: LocalDateTime,
    languageTag: String,
): String {
    val seconds = Duration.between(now, target).seconds.coerceAtLeast(0)
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    val h = if (languageTag == "tr") "sa" else "h"
    val m = if (languageTag == "tr") "dk" else "m"
    val s = if (languageTag == "tr") "sn" else "s"
    return when {
        hours > 0 -> "${hours}$h ${minutes}$m"
        minutes > 0 -> "${minutes}$m ${secs}$s"
        else -> "${secs}$s"
    }
}

fun formatDate(
    date: java.time.LocalDate,
    languageTag: String,
): String {
    val locale = Locale.forLanguageTag(languageTag)
    return date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", locale))
}

fun formatWeekday(
    date: java.time.LocalDate,
    languageTag: String,
): String {
    val locale = Locale.forLanguageTag(languageTag)
    return date.format(DateTimeFormatter.ofPattern("EEEE", locale))
}

fun formatDecimal(
    value: Double,
    digits: Int = 2,
): String = String.format(Locale.US, "%.${digits}f", value)
