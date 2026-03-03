package org.awi.fitness.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.awi.fitness.data.UserSettings
import org.awi.fitness.data.Language
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.Strings
import org.awi.fitness.model.AvatarConversationState
import org.awi.fitness.model.AvatarMessage
import org.awi.fitness.model.AvatarMood
import org.awi.fitness.model.ConversationTopic
import org.awi.fitness.model.ConversationTrigger
import org.awi.fitness.model.DailyCheckIn
import org.awi.fitness.model.MotivationalQuote
import kotlin.random.Random
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AvatarRepository {
    private val userSettings = UserSettings.getInstance()
    private val geminiRepository = GeminiRepository()
    private val _conversationState = MutableStateFlow(AvatarConversationState())
    val conversationState: StateFlow<AvatarConversationState> = _conversationState.asStateFlow()
    
    private fun getString(key: StringKey): String {
        val currentLanguage = Language.entries.find { it.code == userSettings.language.value } ?: Language.ENGLISH
        return Strings.get(key, currentLanguage)
    }

    // Mock data for motivational quotes
    val motivationalQuotes = listOf(
        MotivationalQuote(
            id = "q1",
            content = "The only bad workout is the one that didn't happen.",
            author = "Unknown"
        ),
        MotivationalQuote(
            id = "q2",
            content = "Strength doesn't come from what you can do. It comes from overcoming the things you once thought you couldn't.",
            author = "Rikki Rogers"
        ),
        MotivationalQuote(
            id = "q3",
            content = "The difference between the impossible and the possible lies in a person's determination.",
            author = "Tommy Lasorda"
        ),
        MotivationalQuote(
            id = "q4",
            content = "The body achieves what the mind believes.",
            author = "Napoleon Hill"
        ),
        MotivationalQuote(
            id = "q5",
            content = "Don't limit your challenges. Challenge your limits.",
            author = "Unknown"
        )
    )

    // Mock data for conversation topics
    val conversationTopics = listOf(
        ConversationTopic(
            id = "t1",
            title = "Feeling unmotivated",
            description = "Let's talk about why you're feeling unmotivated and find ways to get back on track.",
            suggestedResponses = listOf(
                "I'm just tired today",
                "I don't see results",
                "I'm bored with my routine"
            )
        ),
        ConversationTopic(
            id = "t2",
            title = "Setting new goals",
            description = "Let's discuss what you want to achieve next in your fitness journey.",
            suggestedResponses = listOf(
                "I want to build more strength",
                "I want to improve my endurance",
                "I want to lose weight"
            )
        ),
        ConversationTopic(
            id = "t3",
            title = "Celebrating progress",
            description = "Let's celebrate the progress you've made so far!",
            suggestedResponses = listOf(
                "I've been consistent with workouts",
                "I've improved my personal best",
                "I feel more energetic"
            )
        )
    )

    // Initial welcome message
    init {
        _conversationState.update { state ->
            state.copy(
                messages = listOf(
                    AvatarMessage(
                        id = "welcome",
                        content = getString(StringKey.HI_THERE_FITNESS_BUDDY),
                        isFromAvatar = true,
                        mood = AvatarMood.HAPPY
                    )
                ),
                suggestedResponses = listOf(
                    getString(StringKey.FEELING_GOOD_REPLY),
                    getString(StringKey.FEELING_TIRED_REPLY),
                    getString(StringKey.NO_MOTIVATION_REPLY)
                )
            )
        }
    }

    /**
     * Initialize conversation with a specific trigger
     */
    fun initializeWithTrigger(trigger: ConversationTrigger) {
        val welcomeMessage = when (trigger) {
            ConversationTrigger.DAILY_CHECKIN -> {
                AvatarMessage(
                    id = "checkin_welcome",
                    content = getString(StringKey.GOOD_MORNING_CHECKIN),
                    isFromAvatar = true,
                    mood = AvatarMood.HAPPY
                )
            }
            ConversationTrigger.WORKOUT_COMPLETED -> {
                AvatarMessage(
                    id = "workout_completed",
                    content = getString(StringKey.AMAZING_WORK_WORKOUT),
                    isFromAvatar = true,
                    mood = AvatarMood.EXCITED
                )
            }
            ConversationTrigger.CHALLENGE_COMPLETED -> {
                AvatarMessage(
                    id = "challenge_completed",
                    content = getString(StringKey.CONGRATULATIONS_CHALLENGE),
                    isFromAvatar = true,
                    mood = AvatarMood.EXCITED
                )
            }
            ConversationTrigger.INACTIVITY -> {
                AvatarMessage(
                    id = "inactivity",
                    content = getString(StringKey.HEY_INACTIVITY),
                    isFromAvatar = true,
                    mood = AvatarMood.CALM
                )
            }
            ConversationTrigger.MISSED_WORKOUT -> {
                AvatarMessage(
                    id = "missed_workout",
                    content = getString(StringKey.NOTICED_MISSED_WORKOUT),
                    isFromAvatar = true,
                    mood = AvatarMood.CONCERNED
                )
            }
            ConversationTrigger.MANUAL -> {
                AvatarMessage(
                    id = "manual",
                    content = getString(StringKey.HI_THERE_FITNESS_BUDDY),
                    isFromAvatar = true,
                    mood = AvatarMood.HAPPY
                )
            }
        }

        val quickReplies = when (trigger) {
            ConversationTrigger.DAILY_CHECKIN -> listOf(
                getString(StringKey.FEELING_GOOD),
                getString(StringKey.FEELING_TIRED),
                getString(StringKey.NO_MOTIVATION),
                getString(StringKey.FEELING_GREAT)
            )
            ConversationTrigger.WORKOUT_COMPLETED -> listOf(
                getString(StringKey.THAT_FELT_GREAT),
                getString(StringKey.IM_PROUD_OF_MYSELF),
                getString(StringKey.WHATS_NEXT),
                getString(StringKey.I_NEED_A_REST)
            )
            ConversationTrigger.CHALLENGE_COMPLETED -> listOf(
                getString(StringKey.I_DID_IT),
                getString(StringKey.THAT_WAS_TOUGH),
                getString(StringKey.WHAT_CHALLENGE_NEXT),
                getString(StringKey.IM_EXHAUSTED)
            )
            ConversationTrigger.INACTIVITY, ConversationTrigger.MISSED_WORKOUT -> listOf(
                getString(StringKey.ILL_GET_BACK_ON_TRACK),
                getString(StringKey.IVE_BEEN_BUSY),
                getString(StringKey.HELP_ME_GET_MOTIVATED),
                getString(StringKey.I_FORGOT)
            )
            ConversationTrigger.MANUAL -> listOf(
                getString(StringKey.FEELING_GOOD_REPLY),
                getString(StringKey.FEELING_TIRED_REPLY),
                getString(StringKey.NO_MOTIVATION_REPLY)
            )
        }

        _conversationState.update { state ->
            state.copy(
                messages = listOf(welcomeMessage),
                trigger = trigger,
                isDailyCheckIn = trigger == ConversationTrigger.DAILY_CHECKIN,
                suggestedResponses = quickReplies,
                currentMood = welcomeMessage.mood ?: AvatarMood.HAPPY
            )
        }
    }

    // Function to send a message from the user
    suspend fun sendUserMessage(content: String) {
        val currentState = _conversationState.value
        val userMessage = AvatarMessage(
            id = Random.nextInt().toString(),
            content = content,
            isFromAvatar = false
        )

        // Handle daily check-in responses
        if (currentState.isDailyCheckIn) {
            handleDailyCheckInResponse(content)
        }

        // Add user message to conversation (only if not already added optimistically)
        _conversationState.update { state ->
            val lastMessage = state.messages.lastOrNull()
            val alreadyAdded = lastMessage?.isFromAvatar == false && 
                              lastMessage.content == content
            
            if (alreadyAdded) {
                // Message already added optimistically, just clear suggested responses
                state.copy(suggestedResponses = emptyList())
            } else {
                // Add user message to conversation
                state.copy(
                    messages = state.messages + userMessage,
                    suggestedResponses = emptyList()
                )
            }
        }

        // Simulate avatar typing
        _conversationState.update { it.copy(isTyping = true) }
        delay(1500) // Simulate thinking time

        // Generate avatar response using Gemini API or fallback
        val avatarResponse = try {
            // Check if we need to ask the second daily check-in question
            val checkIn = userSettings.getTodayCheckIn()
            val lowercaseContent = content.lowercase()
            
            // If user just answered feeling question, ask movement question
            if (currentState.isDailyCheckIn && 
                (lowercaseContent.contains("feeling good") || lowercaseContent.contains("feeling great") ||
                 lowercaseContent.contains("feeling tired") || lowercaseContent.contains("no motivation")) &&
                checkIn?.movementResponse == null) {
                // Second question: Did you move today?
                AvatarMessage(
                    id = Random.nextInt().toString(),
                    content = getString(StringKey.GREAT_DID_YOU_MOVE),
                    isFromAvatar = true,
                    mood = AvatarMood.HAPPY
                )
            } else if (currentState.isDailyCheckIn && checkIn?.movementResponse != null) {
                // Check-in completed, generate personalized encouragement
                generateAvatarResponseWithGemini(content, currentState.trigger)
            } else {
                generateAvatarResponseWithGemini(content, currentState.trigger)
            }
        } catch (e: Exception) {
            // Fallback to static responses if Gemini fails
            generateAvatarResponse(content)
        }

        _conversationState.update { state ->
            state.copy(
                messages = state.messages + avatarResponse,
                currentMood = avatarResponse.mood ?: AvatarMood.ENCOURAGING,
                isTyping = false,
                suggestedResponses = generateSuggestedResponses(content, currentState.trigger)
            )
        }
    }

    private suspend fun handleDailyCheckInResponse(response: String) {
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        // Convert date to start of day timestamp (midnight) - consistent with UserSettings
        val todayStart = today.toEpochDays() * 86400000L

        val currentCheckIn = userSettings.getTodayCheckIn()
        val lowercaseResponse = response.lowercase()

        when {
            lowercaseResponse.contains("feeling good") || lowercaseResponse.contains("feeling great") ||
            lowercaseResponse.contains("feeling tired") || lowercaseResponse.contains("no motivation") -> {
                // First question: How are you feeling?
                val updatedCheckIn = currentCheckIn?.copy(
                    feelingResponse = response
                ) ?: DailyCheckIn(
                    date = todayStart,
                    feelingResponse = response
                )
                userSettings.addDailyCheckIn(updatedCheckIn)
            }
            lowercaseResponse.contains("yes") || lowercaseResponse.contains("did my workout") ||
            lowercaseResponse.contains("not yet") || lowercaseResponse.contains("planning") ||
            lowercaseResponse.contains("rest day") -> {
                // Second question: Did you move today?
                val updatedCheckIn = currentCheckIn?.copy(
                    movementResponse = response,
                    completed = true
                ) ?: DailyCheckIn(
                    date = todayStart,
                    movementResponse = response,
                    completed = true
                )
                userSettings.addDailyCheckIn(updatedCheckIn)
            }
        }
    }

    private suspend fun generateAvatarResponseWithGemini(
        userMessage: String,
        trigger: ConversationTrigger?
    ): AvatarMessage {
        return try {
            val userContext = buildUserContext()
            val conversationHistory = _conversationState.value.messages.takeLast(3) // Last 3 messages
            val prompt = buildGeminiPrompt(userMessage, trigger, userContext, conversationHistory)
            
            val response = geminiRepository.generateAvatarResponse(prompt)
            response.getOrElse {
                // Fallback to static response
                generateAvatarResponse(userMessage)
            }
        } catch (e: Exception) {
            // Fallback to static response on error
            generateAvatarResponse(userMessage)
        }
    }

    private fun buildUserContext(): String {
        val checkIn = userSettings.getTodayCheckIn()
        val contextParts = mutableListOf<String>()
        
        checkIn?.let {
            if (it.feelingResponse != null) {
                contextParts.add("Feeling: ${it.feelingResponse}")
            }
            if (it.movementResponse != null) {
                contextParts.add("Movement: ${it.movementResponse}")
            }
        }
        
        return contextParts.joinToString(", ")
    }

    private fun buildGeminiPrompt(
        userMessage: String,
        trigger: ConversationTrigger?,
        userContext: String,
        conversationHistory: List<AvatarMessage> = emptyList()
    ): String {
        val contextPart = if (userContext.isNotEmpty()) "User context: $userContext. " else ""
        val triggerPart = when (trigger) {
            ConversationTrigger.DAILY_CHECKIN -> "This is a daily check-in conversation. "
            ConversationTrigger.WORKOUT_COMPLETED -> "User just completed a workout. "
            ConversationTrigger.CHALLENGE_COMPLETED -> "User just completed a challenge. "
            ConversationTrigger.INACTIVITY -> "User has been inactive recently. "
            ConversationTrigger.MISSED_WORKOUT -> "User missed a scheduled workout. "
            else -> ""
        }
        
        // Detect meal-related keywords
        val lowercaseMessage = userMessage.lowercase()
        val mealKeywords = listOf("meal", "food", "eat", "eating", "nutrition", "diet", "recipe", 
                                  "breakfast", "lunch", "dinner", "snack", "calorie", "calories", 
                                  "protein", "carb", "carbs", "fat", "fats", "should i eat", 
                                  "what should i eat", "what meals", "suggest me", "recommend",
                                  "meal plan", "meal plans", "what to eat", "eating plan")
        val isMealRelated = mealKeywords.any { lowercaseMessage.contains(it) }
        
        val mealContext = if (isMealRelated) {
            "The user is asking about meals, nutrition, or food. Provide helpful, generic meal suggestions focusing on balanced nutrition, healthy eating habits, and general meal ideas. Keep it supportive and encouraging. "
        } else ""
        
        // Build conversation history context
        val historyContext = if (conversationHistory.isNotEmpty()) {
            val recentMessages = conversationHistory.joinToString("\n") { msg ->
                "${if (msg.isFromAvatar) "Avatar" else "User"}: ${msg.content}"
            }
            "Recent conversation:\n$recentMessages\n"
        } else ""
        
        return """
            You are a friendly fitness buddy avatar. Keep responses short (1-2 sentences max), simple, and supportive.
            $triggerPart$contextPart$mealContext$historyContext
            User said: "$userMessage"
            Respond as a supportive friend, not a trainer. No medical advice. Be encouraging and calm.
        """.trimIndent()
    }

    // Function to generate avatar response based on user message
    private fun generateAvatarResponse(userMessage: String): AvatarMessage {
        val lowercaseMessage = userMessage.lowercase()
        
        return when {
            lowercaseMessage.contains("not motivated") || lowercaseMessage.contains("don't feel like") -> {
                AvatarMessage(
                    id = Random.nextInt().toString(),
                    content = "It's completely normal to feel unmotivated sometimes. Remember why you started this journey. Even a small workout today is better than nothing. What's one small thing you could do?",
                    isFromAvatar = true,
                    mood = AvatarMood.ENCOURAGING
                )
            }
            lowercaseMessage.contains("tired") || lowercaseMessage.contains("exhausted") -> {
                AvatarMessage(
                    id = Random.nextInt().toString(),
                    content = "Rest is an important part of fitness too! Maybe today is a good day for some gentle stretching or a short walk instead of an intense workout?",
                    isFromAvatar = true,
                    mood = AvatarMood.CALM
                )
            }
            lowercaseMessage.contains("good") || lowercaseMessage.contains("great") || lowercaseMessage.contains("amazing") -> {
                AvatarMessage(
                    id = Random.nextInt().toString(),
                    content = "That's awesome to hear! Positive energy is a great foundation for a productive workout. What are you planning to focus on today?",
                    isFromAvatar = true,
                    mood = AvatarMood.EXCITED
                )
            }
            lowercaseMessage.contains("advice") || lowercaseMessage.contains("help") -> {
                AvatarMessage(
                    id = Random.nextInt().toString(),
                    content = "I'd be happy to help! What specific area would you like advice on? Workout routines, staying motivated, or setting achievable goals?",
                    isFromAvatar = true,
                    mood = AvatarMood.HAPPY
                )
            }
            lowercaseMessage.contains("results") || lowercaseMessage.contains("progress") -> {
                AvatarMessage(
                    id = Random.nextInt().toString(),
                    content = "Remember that meaningful progress takes time. Focus on consistency rather than immediate results. Are you tracking your workouts to see your improvements over time?",
                    isFromAvatar = true,
                    mood = AvatarMood.CALM
                )
            }
            else -> {
                AvatarMessage(
                    id = Random.nextInt().toString(),
                    content = "Thanks for sharing! Remember that every step counts in your fitness journey. What can I help you with today?",
                    isFromAvatar = true,
                    mood = AvatarMood.HAPPY
                )
            }
        }
    }

    // Function to generate suggested responses based on conversation context
    private fun generateSuggestedResponses(
        userMessage: String,
        trigger: ConversationTrigger?
    ): List<String> {
        // If daily check-in, show appropriate replies based on progress
        val currentState = _conversationState.value
        if (currentState.isDailyCheckIn) {
            val checkIn = userSettings.getTodayCheckIn()
            val lowercaseMessage = userMessage.lowercase()
            
            // Check if user just answered the feeling question
            if (lowercaseMessage.contains("feeling good") || lowercaseMessage.contains("feeling great") ||
                lowercaseMessage.contains("feeling tired") || lowercaseMessage.contains("no motivation")) {
                // Show movement question replies
                return listOf(
                    getString(StringKey.YES_DID_WORKOUT),
                    getString(StringKey.NOT_YET),
                    getString(StringKey.PLANNING_TO_LATER),
                    getString(StringKey.REST_DAY_TODAY)
                )
            }
            
            // If movement question answered, show general replies
            if (checkIn?.movementResponse != null) {
                return listOf(
                    getString(StringKey.THAT_HELPS),
                    getString(StringKey.TELL_ME_MORE),
                    getString(StringKey.THANKS)
                )
            }
            
            // Initial feeling question replies
            return listOf(
                getString(StringKey.FEELING_GOOD),
                getString(StringKey.FEELING_TIRED),
                getString(StringKey.NO_MOTIVATION),
                getString(StringKey.FEELING_GREAT)
            )
        }

        val lowercaseMessage = userMessage.lowercase()
        
        return when (trigger) {
            ConversationTrigger.DAILY_CHECKIN -> {
                if (lowercaseMessage.contains("feeling")) {
                    listOf(
                        getString(StringKey.YES_DID_WORKOUT),
                        getString(StringKey.NOT_YET),
                        getString(StringKey.PLANNING_TO_LATER),
                        getString(StringKey.REST_DAY_TODAY)
                    )
                } else {
                    listOf(
                        getString(StringKey.THAT_HELPS),
                        getString(StringKey.TELL_ME_MORE),
                        getString(StringKey.THANKS)
                    )
                }
            }
            ConversationTrigger.WORKOUT_COMPLETED -> {
                listOf(
                    getString(StringKey.THAT_FELT_GREAT),
                    getString(StringKey.IM_PROUD_OF_MYSELF),
                    getString(StringKey.WHATS_NEXT),
                    getString(StringKey.I_NEED_A_REST)
                )
            }
            ConversationTrigger.CHALLENGE_COMPLETED -> {
                listOf(
                    getString(StringKey.I_DID_IT),
                    getString(StringKey.THAT_WAS_TOUGH),
                    getString(StringKey.WHAT_CHALLENGE_NEXT),
                    getString(StringKey.IM_EXHAUSTED)
                )
            }
            ConversationTrigger.INACTIVITY, ConversationTrigger.MISSED_WORKOUT -> {
                listOf(
                    getString(StringKey.ILL_GET_BACK_ON_TRACK),
                    getString(StringKey.IVE_BEEN_BUSY),
                    getString(StringKey.HELP_ME_GET_MOTIVATED),
                    getString(StringKey.I_FORGOT)
                )
            }
            else -> {
                when {
                    lowercaseMessage.contains("not motivated") || lowercaseMessage.contains("don't feel like") -> {
                        listOf(
                            "What would help me get motivated?",
                            "Can you share a success story?",
                            "I need a simple workout for today"
                        )
                    }
                    lowercaseMessage.contains("tired") || lowercaseMessage.contains("exhausted") -> {
                        listOf(
                            "Should I take a rest day?",
                            "What's a light activity I could do?",
                            "How can I improve my energy levels?"
                        )
                    }
                    lowercaseMessage.contains("advice") || lowercaseMessage.contains("help") -> {
                        listOf(
                            "How can I stay consistent?",
                            "What's a good workout for beginners?",
                            "How do I set realistic goals?"
                        )
                    }
                    lowercaseMessage.contains("results") || lowercaseMessage.contains("progress") -> {
                        listOf(
                            "How long until I see results?",
                            "What should I track besides weight?",
                            "How can I measure progress better?"
                        )
                    }
                    else -> {
                        listOf(
                            getString(StringKey.TELL_ME_MORE),
                            getString(StringKey.THAT_HELPS),
                            getString(StringKey.HELP_ME_GET_MOTIVATED),
                            getString(StringKey.THANKS)
                        )
                    }
                }
            }
        }
    }

    // Function to get a random motivational quote
    fun getRandomQuote(): MotivationalQuote {
        return motivationalQuotes.random()
    }

    // Function to reset conversation
    fun resetConversation() {
        _conversationState.update {
            AvatarConversationState(
                messages = listOf(
                    AvatarMessage(
                        id = "welcome",
                        content = getString(StringKey.HI_THERE_FITNESS_BUDDY),
                        isFromAvatar = true,
                        mood = AvatarMood.HAPPY
                    )
                ),
                suggestedResponses = listOf(
                    getString(StringKey.FEELING_GOOD_REPLY),
                    getString(StringKey.FEELING_TIRED_REPLY),
                    getString(StringKey.NO_MOTIVATION_REPLY)
                ),
                trigger = ConversationTrigger.MANUAL,
                isDailyCheckIn = false
            )
        }
    }
}
