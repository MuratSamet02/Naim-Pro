package com.naimpro.app.domain.alarm

import com.naimpro.app.domain.model.Prayer
import com.naimpro.app.domain.model.PrayerTimes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class AlarmPlannerTest {
    private val date: LocalDate = LocalDate.of(2026, 11, 20)

    private fun times(
        day: LocalDate,
        shift: Long = 0,
    ): PrayerTimes =
        PrayerTimes(
            date = day,
            imsak = LocalDateTime.of(day, java.time.LocalTime.of(5, 30)).plusHours(shift),
            gunes = LocalDateTime.of(day, java.time.LocalTime.of(7, 0)).plusHours(shift),
            ogle = LocalDateTime.of(day, java.time.LocalTime.of(12, 30)).plusHours(shift),
            ikindi = LocalDateTime.of(day, java.time.LocalTime.of(15, 30)).plusHours(shift),
            aksam = LocalDateTime.of(day, java.time.LocalTime.of(17, 45)).plusHours(shift),
            yatsi = LocalDateTime.of(day, java.time.LocalTime.of(19, 15)).plusHours(shift),
        )

    @Test
    @DisplayName("Next prayer is the first one after now")
    fun nextPrayer() {
        val now = LocalDateTime.of(date, java.time.LocalTime.of(13, 0))
        val next = AlarmPlanner.nextPrayer(now, times(date))
        assertEquals(Prayer.IKINDI, next?.first)
    }

    @Test
    @DisplayName("After the last prayer the plan rolls over to tomorrow")
    fun midnightRollover() {
        val now = LocalDateTime.of(date, java.time.LocalTime.of(23, 0))
        val plan =
            AlarmPlanner.plan(
                now = now,
                today = times(date),
                tomorrow = times(date.plusDays(1)),
                preReminderEnabled = false,
                defaultReminderMinutes = 0,
            )
        assertEquals(Prayer.IMSAK, plan.prayer?.prayer)
        assertEquals(LocalDate.of(2026, 11, 21), plan.prayer?.at?.toLocalDate())
    }

    @Test
    @DisplayName("Reminder uses the per prayer override")
    fun perPrayerReminder() {
        val now = LocalDateTime.of(date, java.time.LocalTime.of(8, 0))
        val plan =
            AlarmPlanner.plan(
                now = now,
                today = times(date),
                preReminderEnabled = true,
                defaultReminderMinutes = 10,
                perPrayerReminderMinutes = mapOf(Prayer.OGLE to 25),
            )
        assertEquals(Prayer.OGLE, plan.prayer?.prayer)
        assertEquals(Prayer.OGLE, plan.reminder?.prayer)
        assertEquals(
            plan.prayer?.at?.minusMinutes(25),
            plan.reminder?.at,
        )
    }

    @Test
    @DisplayName("Zero minutes disables the reminder")
    fun reminderDisabled() {
        val now = LocalDateTime.of(date, java.time.LocalTime.of(8, 0))
        val plan =
            AlarmPlanner.plan(
                now = now,
                today = times(date),
                preReminderEnabled = true,
                defaultReminderMinutes = 0,
            )
        assertNull(plan.reminder)
        assertTrue(plan.alarms.size == 1)
    }

    @Test
    @DisplayName("A reminder that is already in the past is dropped")
    fun expiredReminder() {
        val now = LocalDateTime.of(date, java.time.LocalTime.of(12, 25))
        val plan =
            AlarmPlanner.plan(
                now = now,
                today = times(date),
                preReminderEnabled = true,
                defaultReminderMinutes = 15,
            )
        assertEquals(Prayer.OGLE, plan.prayer?.prayer)
        assertNull(plan.reminder)
    }

    @Test
    @DisplayName("No prayer means an empty plan")
    fun emptyPlan() {
        val now = LocalDateTime.of(date, java.time.LocalTime.of(23, 30))
        val plan =
            AlarmPlanner.plan(
                now = now,
                today = times(date),
                preReminderEnabled = true,
                defaultReminderMinutes = 10,
            )
        assertTrue(plan.alarms.isEmpty())
    }

    @Test
    @DisplayName("Wall clock conversion respects the summer time offset")
    fun daylightSaving() {
        val alarm =
            PlannedAlarm(
                kind = AlarmKind.PRAYER,
                prayer = Prayer.OGLE,
                at = LocalDateTime.of(2026, 3, 29, 13, 0),
            )
        val epoch = alarm.epochMillis(ZoneId.of("Europe/Berlin"))
        assertEquals(Instant.parse("2026-03-29T11:00:00Z").toEpochMilli(), epoch)
    }
}
