package com.ruslan.foodtracker.core.common.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class DateTimeUtilsTest {

    // ─── TC-20: weekStart(Monday) returns the same day ────────────────────────

    @Test
    fun `TC-20 weekStart of Monday returns the same day`() {
        val monday = LocalDate.of(2026, 2, 23) // known Monday
        assertEquals(DayOfWeek.MONDAY, monday.dayOfWeek)

        val result = DateTimeUtils.weekStart(monday)

        assertEquals(monday, result)
    }

    // ─── TC-21: weekStart(Sunday) returns the previous Monday ────────────────

    @Test
    fun `TC-21 weekStart of Sunday returns previous Monday`() {
        val sunday = LocalDate.of(2026, 3, 1) // Sunday after 2026-02-23 week
        assertEquals(DayOfWeek.SUNDAY, sunday.dayOfWeek)

        val result = DateTimeUtils.weekStart(sunday)

        val expectedMonday = LocalDate.of(2026, 2, 23)
        assertEquals(expectedMonday, result)
    }

    // ─── TC-22: weekEnd(Monday) returns Sunday of the same week ───────────────

    @Test
    fun `TC-22 weekEnd of Monday returns Sunday of the same week`() {
        val monday = LocalDate.of(2026, 2, 23)
        val result = DateTimeUtils.weekEnd(monday)

        val expectedSunday = LocalDate.of(2026, 3, 1)
        assertEquals(expectedSunday, result)
    }

    // ─── TC-23: formatWeekRange — same month → "22 – 28 фев" ─────────────────

    @Test
    fun `TC-23 formatWeekRange same month returns compact form`() {
        val weekStart = LocalDate.of(2026, 2, 23) // Mon 23 Feb – Sun 1 Mar... wait
        // Let's use a week that stays in the same month: e.g. 2026-02-09 (Mon) to 2026-02-15 (Sun)
        val sameMonthWeekStart = LocalDate.of(2026, 2, 9)
        val result = DateTimeUtils.formatWeekRange(sameMonthWeekStart)

        // Expected: "9 – 15 фев"
        assertEquals("9 – 15 фев", result)
    }

    // ─── TC-24: formatWeekRange — different months → "27 янв – 2 фев" ─────────

    @Test
    fun `TC-24 formatWeekRange different months returns full form`() {
        // Week starting 2026-01-26 (Mon): 26 Jan – 1 Feb
        val crossMonthWeekStart = LocalDate.of(2026, 1, 26)
        val result = DateTimeUtils.formatWeekRange(crossMonthWeekStart)

        // Expected: "26 янв – 1 фев"
        assertEquals("26 янв – 1 фев", result)
    }

    // ─── TC-25: weekDates returns exactly 7 dates ─────────────────────────────

    @Test
    fun `TC-25 weekDates returns exactly 7 dates`() {
        val weekStart = LocalDate.of(2026, 2, 23)
        val result = DateTimeUtils.weekDates(weekStart)

        assertEquals(7, result.size)
    }

    // ─── TC-26: weekDates[0] == weekStart(date) ───────────────────────────────

    @Test
    fun `TC-26 weekDates first element is the Monday of the week`() {
        val date = LocalDate.of(2026, 2, 25) // Wednesday
        val result = DateTimeUtils.weekDates(date)

        val expectedMonday = DateTimeUtils.weekStart(date)
        assertEquals(expectedMonday, result[0])
    }
}
