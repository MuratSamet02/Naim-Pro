package com.naimpro.app.ui.components

import com.naimpro.app.R
import com.naimpro.app.domain.prayer.AsrMadhab
import com.naimpro.app.domain.prayer.CalculationMethod
import com.naimpro.app.domain.prayer.HighLatitudeRule

/**
 * String resource ids for the calculation options.
 *
 * Kept in one place so the settings dialog, the onboarding flow and any future
 * surface cannot drift apart. These are plain functions returning an id, so
 * they can also be used from non-composable code.
 */
fun methodLabel(method: CalculationMethod): Int =
    when (method) {
        CalculationMethod.DIYANET -> R.string.method_diyanet
        CalculationMethod.DIYANET_SAFI -> R.string.method_diyanet_safi
        CalculationMethod.MWL -> R.string.method_mwl
        CalculationMethod.ISNA -> R.string.method_isna
        CalculationMethod.UMM_AL_QURA -> R.string.method_umm_al_qura
        CalculationMethod.EGYPT -> R.string.method_egypt
        CalculationMethod.KARACHI -> R.string.method_karachi
        CalculationMethod.KUWAIT -> R.string.method_kuwait
        CalculationMethod.SINGAPORE -> R.string.method_singapore
    }

fun madhabLabel(madhab: AsrMadhab): Int =
    when (madhab) {
        AsrMadhab.STANDARD -> R.string.madhab_standard
        AsrMadhab.HANAFI -> R.string.madhab_hanafi
    }

fun highLatLabel(rule: HighLatitudeRule): Int =
    when (rule) {
        HighLatitudeRule.NONE -> R.string.high_lat_none
        HighLatitudeRule.MIDDLE_OF_THE_NIGHT -> R.string.high_lat_middle
        HighLatitudeRule.ONE_SEVENTH -> R.string.high_lat_seventh
        HighLatitudeRule.TWILIGHT_ANGLE -> R.string.high_lat_angle
    }
