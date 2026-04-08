package com.v.music.utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object TimeFormatUtil {
    fun Long.formatTime(): String {
        val totalSeconds = this / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        val m = minutes.toString().padStart(2, '0')
        val s = seconds.toString().padStart(2, '0')

        return "$m:$s"
    }

    fun dateFormatter(dateMillis: Long): String {
        val instant = Instant.fromEpochMilliseconds(dateMillis)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val day = dateTime.dayOfMonth.toString().padStart(2, '0')
        val month = dateTime.monthNumber.toString().padStart(2, '0')
        val year = dateTime.year

        return "$day/$month/$year"
        }
}