package com.naimpro.app.ui.util

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList

/**
 * Applies the chosen language through the platform per-app language API
 * (Android 13+). On older versions the setting still drives content language
 * (Hijri month names, exported tables) while the UI follows the system locale.
 */
fun applyLanguage(
    context: Context,
    tag: String,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val manager = context.getSystemService(LocaleManager::class.java) ?: return
        runCatching {
            manager.applicationLocales = LocaleList.forLanguageTags(tag)
        }
    }
}
