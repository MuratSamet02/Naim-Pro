package com.naimpro.app.ui.util

import java.util.Locale

/**
 * Maps the stored language tag to a locale without touching the global
 * configuration.
 *
 * Reading `Locale.getDefault()` from a composable is flagged by lint as
 * non-observable, and it also lags one frame behind `applyLanguage`. Deriving the
 * locale from the settings value keeps formatting in sync with the UI language.
 */
fun String.toLocale(): Locale =
    when (lowercase(Locale.ROOT)) {
        "tr" -> Locale.forLanguageTag("tr-TR")
        "ar" -> Locale.forLanguageTag("ar")
        "en" -> Locale.forLanguageTag("en")
        else -> Locale.forLanguageTag(this)
    }
