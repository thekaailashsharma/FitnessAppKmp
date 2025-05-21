package org.awi.fitness.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
} 