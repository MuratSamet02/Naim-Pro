package com.naimpro.app.alarm

import android.content.Context
import android.content.SharedPreferences

/** Tiny synchronous store for values the alarm receivers need instantly. */
class AlarmPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("naim_alarm", Context.MODE_PRIVATE)

    var customEzanUri: String?
        get() = prefs.getString(KEY_EZAN_URI, null)
        set(value) = prefs.edit().putString(KEY_EZAN_URI, value).apply()

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

    private companion object {
        const val KEY_EZAN_URI = "custom_ezan_uri"
        const val KEY_LAST_ALARM = "last_alarm_at"
        const val KEY_LAST_PRAYER = "last_alarm_prayer"
        const val KEY_DND_RESTORE = "dnd_restore_at"
        const val KEY_PREV_FILTER = "prev_filter"
    }
}
