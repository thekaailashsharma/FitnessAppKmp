package org.awi.fitness.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Returns current time as milliseconds since epoch — works on all KMP platforms. */
expect fun currentTimeMillis(): Long

fun currentInstant(): Instant = Instant.fromEpochMilliseconds(currentTimeMillis())

fun todayLocalDate(): LocalDate =
    currentInstant().toLocalDateTime(TimeZone.currentSystemDefault()).date

fun todayDateString(): String = todayLocalDate().toString()
