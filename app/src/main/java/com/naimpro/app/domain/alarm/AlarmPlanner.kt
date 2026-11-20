package com.naimpro.app.domain.alarm

import com.naimpro.app.domain.model.Prayer
import com.naimpro.app.domain.model.PrayerTimes
import java.time.LocalDateTime
import java.time.ZoneId

/** Why a pending alarm exists. */
enum class AlarmKind {
    PRAYER,
    REMINDER,
}

/**
 * One alarm the scheduler should keep pending.
 *
 * [at] is a wall clock value in the user's zone, so it survives daylight saving
 * transitions: converting it to an instant happens at the last moment.
 */
data class PlannedAlarm(
    val kind: AlarmKind,
    val prayer: Prayer,
    val at: LocalDateTime,
) {
    fun epochMillis(zone: ZoneId): Long = at.atZone(zone).toInstant().toEpochMilli()
}

/** The full alarm set derived from one point in time. */
data class AlarmPlan(
    val prayer: PlannedAlarm? = null,
    val reminder: PlannedAlarm? = null,
) {
    val alarms: List<PlannedAlarm> = listOfNotNull(prayer, reminder)

    /** Time at which the safety net worker should verify the chain. */
    val safetyNetAt: LocalDateTime? get() = prayer?.at
}

/**
 * Pure alarm planning.
 *
 * Kept free of Android types so the edge cases (midnight rollover, daylight
 * saving, per prayer reminders, reminder already in the past) are covered by
 * fast JVM unit tests instead of emulator runs.
 */
object AlarmPlanner {
    fun plan(
        now: LocalDateTime,
        today: PrayerTimes,
        tomorrow: PrayerTimes? = null,
        preReminderEnabled: Boolean,
        defaultReminderMinutes: Int,
        perPrayerReminderMinutes: Map<Prayer, Int> = emptyMap(),
    ): AlarmPlan {
        val next = nextPrayer(now, today, tomorrow) ?: return AlarmPlan()
        val prayerAlarm = PlannedAlarm(AlarmKind.PRAYER, next.first, next.second)
        val minutes =
            perPrayerReminderMinutes[next.first]
                ?: if (preReminderEnabled) defaultReminderMinutes else 0
        if (minutes <= 0) return AlarmPlan(prayerAlarm, null)
        val reminderAt = next.second.minusMinutes(minutes.toLong())
        val reminder =
            if (reminderAt.isAfter(now)) {
                PlannedAlarm(AlarmKind.REMINDER, next.first, reminderAt)
            } else {
                null
            }
        return AlarmPlan(prayerAlarm, reminder)
    }

    /** First prayer strictly after [now]; falls through to [tomorrow]. */
    fun nextPrayer(
        now: LocalDateTime,
        today: PrayerTimes,
        tomorrow: PrayerTimes? = null,
    ): Pair<Prayer, LocalDateTime>? {
        nextAfter(today, now)?.let { return it }
        return tomorrow?.let { nextAfter(it, now) }
    }

    private fun nextAfter(
        times: PrayerTimes,
        now: LocalDateTime,
    ): Pair<Prayer, LocalDateTime>? = times.asList().firstOrNull { it.second.isAfter(now) }
}
