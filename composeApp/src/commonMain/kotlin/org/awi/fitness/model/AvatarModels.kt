package org.awi.fitness.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Represents the emotional state of the avatar
 */
enum class AvatarMood {
    HAPPY,
    ENCOURAGING,
    CALM,
    CONCERNED,
    EXCITED
}

/**
 * Represents a conversation trigger type
 */
enum class ConversationTrigger {
    DAILY_CHECKIN,
    MISSED_WORKOUT,
    WORKOUT_COMPLETED,
    CHALLENGE_COMPLETED,
    INACTIVITY,
    MANUAL
}

/**
 * Represents an avatar selection option
 */
@Serializable
data class AvatarSelection(
    val id: String,
    val name: String,
    val imageUrl: String,
    val isSelected: Boolean = false
)

/**
 * Represents a daily check-in entry
 */
@Serializable
data class DailyCheckIn(
    val date: Long, // Date as epoch milliseconds (start of day)
    val completed: Boolean = false,
    val feelingResponse: String? = null,
    val movementResponse: String? = null
)

/**
 * Represents a message in the avatar conversation
 */
data class AvatarMessage(
    val id: String,
    val content: String,
    val isFromAvatar: Boolean,
    val timestamp: Instant = Clock.System.now(),
    val mood: AvatarMood? = null // Only applicable for avatar messages
)

/**
 * Represents a motivational quote that the avatar can share
 */
data class MotivationalQuote(
    val id: String,
    val content: String,
    val author: String
)

/**
 * Represents a predefined conversation topic that the avatar can discuss
 */
data class ConversationTopic(
    val id: String,
    val title: String,
    val description: String,
    val suggestedResponses: List<String> = emptyList()
)

/**
 * Represents the current state of the avatar conversation
 */
data class AvatarConversationState(
    val messages: List<AvatarMessage> = emptyList(),
    val currentMood: AvatarMood = AvatarMood.ENCOURAGING,
    val suggestedResponses: List<String> = emptyList(),
    val isTyping: Boolean = false,
    val trigger: ConversationTrigger? = null,
    val isDailyCheckIn: Boolean = false
)
