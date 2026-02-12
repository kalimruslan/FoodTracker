package com.ruslan.foodtracker.core.common.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    fun formatTime(time: LocalDateTime): String = time.format(timeFormatter)

    fun formatDateTime(dateTime: LocalDateTime): String = dateTime.format(dateTimeFormatter)
}
