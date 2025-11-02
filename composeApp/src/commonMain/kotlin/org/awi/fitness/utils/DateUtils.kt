package org.awi.fitness.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object DateUtils {
    fun isDateValid(dateString: String): Boolean {
        return try {
            val validTill = LocalDate.parse(dateString)
            val currentDate = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
            
            validTill >= currentDate
        } catch (e: Exception) {
            false
        }
    }
    
    fun formatTimestamp(timestamp: Long): String {
        val now = Clock.System.now()
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val duration = now - instant
        
        return when {
            duration < 1.minutes -> "Just now"
            duration < 1.hours -> "${duration.inWholeMinutes}m ago"
            duration < 24.hours -> "${duration.inWholeHours}h ago"
            duration < 48.hours -> "Yesterday"
            duration < 7.days -> "${duration.inWholeDays}d ago"
            else -> {
                val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}, ${date.year}"
            }
        }
    }
    
    fun formatTimestampRelative(timestamp: Long): String {
        val now = Clock.System.now()
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val duration = now - instant
        
        return when {
            duration < 1.minutes -> "Just now"
            duration < 1.hours -> "${duration.inWholeMinutes}m ago"
            duration < 24.hours -> "${duration.inWholeHours}h ago"
            duration < 48.hours -> "Yesterday"
            else -> "${duration.inWholeDays}d ago"
        }
    }
    
    fun formatTimestampShort(timestamp: Long): String {
        val now = Clock.System.now()
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val duration = now - instant
        
        return when {
            duration < 1.hours -> "${duration.inWholeMinutes}m ago"
            duration < 24.hours -> "${duration.inWholeHours}h ago"
            duration < 48.hours -> "1d ago"
            duration < 7.days -> "${duration.inWholeDays}d ago"
            else -> {
                val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                "${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}"
            }
        }
    }
    
    fun formatSeconds(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
} 