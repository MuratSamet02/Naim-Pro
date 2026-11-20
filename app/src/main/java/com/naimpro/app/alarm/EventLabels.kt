package com.naimpro.app.alarm

import com.naimpro.app.R

/** Maps the stable event keys from `IslamicEvents` to localised titles. */
object EventLabels {
    fun titleRes(key: String): Int =
        when (key) {
            "hijri_new_year" -> R.string.event_hijri_new_year
            "ashura" -> R.string.event_ashura
            "mawlid" -> R.string.event_mawlid
            "raghaib" -> R.string.event_raghaib
            "miraj" -> R.string.event_miraj
            "baraah" -> R.string.event_baraah
            "ramadan_start" -> R.string.event_ramadan_start
            "qadr_night" -> R.string.event_qadr_night
            "eid_al_fitr" -> R.string.event_eid_al_fitr
            "arafah" -> R.string.event_arafah
            "eid_al_adha" -> R.string.event_eid_al_adha
            else -> R.string.notification_event_fallback
        }
}
