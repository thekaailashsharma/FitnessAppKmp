package org.awi.fitness.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object DateUtils {
    fun isDateValid(dateString: String): Boolean {
        return try {
            val validTill = LocalDate.parse(dateString)
            val currentDate = Instant.fromEpochMilliseconds(currentTimeMillis())
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
            validTill >= currentDate
        } catch (e: Exception) {
            false
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        val nowMs = currentTimeMillis()
        val diffMs = nowMs - timestamp

        return when {
            diffMs < 60_000L -> "Just now"
            diffMs < 3_600_000L -> "${diffMs / 60_000L}m ago"
            diffMs < 86_400_000L -> "${diffMs / 3_600_000L}h ago"
            diffMs < 172_800_000L -> "Yesterday"
            diffMs < 604_800_000L -> "${diffMs / 86_400_000L}d ago"
            else -> {
                val date = Instant.fromEpochMilliseconds(timestamp)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}, ${date.year}"
            }
        }
    }

    fun formatTimestampRelative(timestamp: Long): String {
        val diffMs = currentTimeMillis() - timestamp

        return when {
            diffMs < 60_000L -> "Just now"
            diffMs < 3_600_000L -> "${diffMs / 60_000L}m ago"
            diffMs < 86_400_000L -> "${diffMs / 3_600_000L}h ago"
            diffMs < 172_800_000L -> "Yesterday"
            else -> "${diffMs / 86_400_000L}d ago"
        }
    }

    fun formatTimestampShort(timestamp: Long): String {
        val diffMs = currentTimeMillis() - timestamp

        return when {
            diffMs < 3_600_000L -> "${diffMs / 60_000L}m ago"
            diffMs < 86_400_000L -> "${diffMs / 3_600_000L}h ago"
            diffMs < 172_800_000L -> "1d ago"
            diffMs < 604_800_000L -> "${diffMs / 86_400_000L}d ago"
            else -> {
                val date = Instant.fromEpochMilliseconds(currentTimeMillis() - diffMs)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                "${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}"
            }
        }
    }

    fun formatSeconds(totalSeconds: Int): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60

        return if (h > 0) {
            "${pad(h)}:${pad(m)}:${pad(s)}"
        } else {
            "${pad(m)}:${pad(s)}"
        }
    }

    private fun pad(n: Int) = if (n < 10) "0$n" else "$n"
}