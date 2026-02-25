package com.ruslan.foodtracker.core.common.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object DateTimeUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    fun formatTime(time: LocalDateTime): String = time.format(timeFormatter)

    fun formatDateTime(dateTime: LocalDateTime): String = dateTime.format(dateTimeFormatter)

    /**
     * Возвращает понедельник недели, которой принадлежит [date].
     * Использует ISO-стандарт: неделя начинается в понедельник.
     */
    fun weekStart(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    /**
     * Возвращает воскресенье недели, которой принадлежит [date].
     */
    fun weekEnd(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

    /**
     * Формирует строку-заголовок недели вида «22 фев – 28 фев».
     * Если начало и конец в одном месяце — «22 – 28 фев».
     * Если в разных месяцах — «28 янв – 3 фев».
     */
    fun formatWeekRange(weekStartDate: LocalDate): String {
        val start = weekStart(weekStartDate)
        val end = weekEnd(weekStartDate)
        val monthFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("ru"))
        val dayOnlyFormatter = DateTimeFormatter.ofPattern("d")
        return if (start.month == end.month) {
            "${start.format(dayOnlyFormatter)} – ${end.format(monthFormatter)}"
        } else {
            "${start.format(monthFormatter)} – ${end.format(monthFormatter)}"
        }
    }

    /**
     * Возвращает список из 7 дат текущей недели (Пн–Вс),
     * начиная с [weekStartDate].
     */
    fun weekDates(weekStartDate: LocalDate): List<LocalDate> =
        (0..6).map { weekStart(weekStartDate).plusDays(it.toLong()) }
}
