package com.naimpro.app.alarm

import android.content.Context
import android.content.SharedPreferences
import com.naimpro.app.domain.model.Prayer

/**
 * Tiny synchronous store for values the alarm receivers need instantly.
 *
 * Sounds are stored per prayer because the morning (imsak) adhan is often a
 * different recording from the rest of the day. A missing per prayer entry falls
 * back to the legacy global file, then to the built in tone.
 */
class AlarmPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("naim_alarm", Context.MODE_PRIVATE)

    var customEzanUri: String?
        get() = prefs.getString(KEY_EZAN_URI, null)
        set(value) = prefs.edit().putString(KEY_EZAN_URI, value).apply()

    /** Sound chosen for a single prayer, or `null` when the default applies. */
    fun ezanUriFor(prayer: Prayer): String? = prefs.getString(perPrayerKey(prayer), null) ?: customEzanUri

    fun setEzanUri(
        prayer: Prayer,
        uri: String?,
    ) {
        val editor = prefs.edit()
        if (uri.isNullOrBlank()) {
            editor.remove(perPrayerKey(prayer))
        } else {
            editor.putString(perPrayerKey(prayer), uri)
        }
        editor.apply()
    }

    fun hasCustomEzanFor(prayer: Prayer): Boolean = !prefs.getString(perPrayerKey(prayer), null).isNullOrBlank()

    var lastAlarmAt: Long
        get() = prefs.getLong(KEY_LAST_ALARM, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_ALARM, value).apply()

    var lastAlarmPrayer: String?
        get() = prefs.getString(KEY_LAST_PRAYER, null)
        set(value) = prefs.edit().putString(KEY_LAST_PRAYER, value).apply()

    var dndRestoreAt: Long
        get() = prefs.getLong(KEY_DND_RESTORE, 0L)
        set(value) = prefs.edit().putLong(KEY_DND_RESTORE, value).apply()

    var previousInterruptionFilter: Int
        get() = prefs.getInt(KEY_PREV_FILTER, -1)
        set(value) = prefs.edit().putInt(KEY_PREV_FILTER, value).apply()

    private fun perPrayerKey(prayer: Prayer): String = "custom_ezan_uri_${prayer.name}"

    private companion object {
        const val KEY_EZAN_URI = "custom_ezan_uri"
        const val KEY_LAST_ALARM = "last_alarm_at"
        const val KEY_LAST_PRAYER = "last_alarm_prayer"
        const val KEY_DND_RESTORE = "dnd_restore_at"
        const val KEY_PREV_FILTER = "prev_filter"
    }
}
