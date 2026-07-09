package org.awi.fitness.viewmodel
import org.awi.fitness.data.tr
import org.awi.fitness.data.StringKey

import cafe.adriel.voyager.core.model.StateScreenModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.AvatarConversationState
import org.awi.fitness.model.AvatarMessage
import org.awi.fitness.model.AvatarMood
import org.awi.fitness.model.AvatarSelection
import org.awi.fitness.model.ConversationTopic
import org.awi.fitness.model.ConversationTrigger
import org.awi.fitness.model.MotivationalQuote
import org.awi.fitness.repository.AvatarRepository
import kotlin.random.Random

class AvatarViewModel(
    private val initialTrigger: ConversationTrigger? = null
) : StateScreenModel<AvatarViewModel.AvatarUIState>(AvatarUIState()) {
    
    // Create a coroutine scope for this ViewModel
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    data class AvatarUIState(
        val conversationState: AvatarConversationState = AvatarConversationState(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val showTopics: Boolean = false,
        val showQuotes: Boolean = false,
        val availableAvatars: List<AvatarSelection> = emptyList(),
        val selectedAvatar: AvatarSelection? = null,
        val hasSelectedAvatar: Boolean = false
    )
    
    private val avatarRepository = AvatarRepository()
    private val userSettings = UserSettings.getInstance()
    
    init {
        loadInitialState()
        loadAvatars()
        initialTrigger?.let {
            avatarRepository.initializeWithTrigger(it)
        }
    }
    
    private fun loadInitialState() {
        viewModelScope.launch {
            try {
                mutableState.update { currentState ->
                    currentState.copy(
                        conversationState = avatarRepository.conversationState.value,
                        isLoading = false,
                        hasSelectedAvatar = userSettings.selectedAvatarId != null
                    )
                }
            } catch (e: Exception) {
                mutableState.update { currentState ->
                    currentState.copy(
                        error = "${tr(StringKey.VME_INIT_AVATAR_FAILED)}: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadAvatars() {
        val avatars = org.awi.fitness.data.AvatarData.avatars
        val selectedId = userSettings.selectedAvatarId
        val updatedAvatars = avatars.map { it.copy(isSelected = it.id == selectedId) }
        mutableState.update { 
            it.copy(
                availableAvatars = updatedAvatars, 
                selectedAvatar = updatedAvatars.firstOrNull { avatar -> avatar.isSelected },
                hasSelectedAvatar = selectedId != null
            ) 
        }
    }

    fun selectAvatar(avatarId: String) {
        userSettings.selectedAvatarId = avatarId
        loadAvatars() // Reload to update selection state
        
        // Trigger a reload of conversation state if needed
        viewModelScope.launch {
            // We can just update the UI state to reflect any changes if necessary
            // The repository state is already being observed in loadInitialState
        }
    }

    
    fun sendUserMessage(message: String) {
        if (message.isBlank()) return
        
        // IMMEDIATELY add user message to UI (optimistic update)
        val userMessage = AvatarMessage(
            id = Random.nextInt().toString(),
            content = message,
            isFromAvatar = false
        )
        
        mutableState.update { currentState ->
            currentState.copy(
                conversationState = currentState.conversationState.copy(
                    messages = currentState.conversationState.messages + userMessage,
                    suggestedResponses = emptyList(),
                    isTyping = true // Show typing indicator
                ),
                isLoading = true
            )
        }
        
        viewModelScope.launch {
            try {
                avatarRepository.sendUserMessage(message)
                mutableState.update { currentState ->
                    currentState.copy(
                        conversationState = avatarRepository.conversationState.value,
                        isLoading = false,
                        showTopics = false,
                        showQuotes = false
                    )
                }
            } catch (e: Exception) {
                mutableState.update { currentState ->
                    currentState.copy(
                        error = "${tr(StringKey.VME_SEND_MESSAGE_FAILED)}: ${e.message}",
                        isLoading = false,
                        conversationState = currentState.conversationState.copy(isTyping = false)
                    )
                }
            }
        }
    }
    
    fun toggleTopicsVisibility() {
        mutableState.update { currentState ->
            currentState.copy(
                showTopics = !currentState.showTopics,
                showQuotes = false
            )
        }
    }
    
    fun toggleQuotesVisibility() {
        mutableState.update { currentState ->
            currentState.copy(
                showQuotes = !currentState.showQuotes,
                showTopics = false
            )
        }
    }
    
    fun selectTopic(topic: ConversationTopic) {
        mutableState.update { currentState ->
            currentState.copy(showTopics = false)
        }
        sendUserMessage("Let's talk about: ${topic.title}")
    }
    
    fun shareQuote(quote: MotivationalQuote) {
        mutableState.update { currentState ->
            currentState.copy(showQuotes = false)
        }
        sendUserMessage("I like this quote: \"${quote.content}\" - ${quote.author}")
    }
    
    fun resetConversation() {
        viewModelScope.launch {
            avatarRepository.resetConversation()
            mutableState.update { currentState ->
                currentState.copy(
                    conversationState = avatarRepository.conversationState.value,
                    showTopics = false,
                    showQuotes = false
                )
            }
        }
    }
    
    fun clearError() {
        mutableState.update { it.copy(error = null) }
    }
    
    // Helper function to get conversation topics
    fun getConversationTopics(): List<ConversationTopic> {
        return avatarRepository.conversationTopics
    }
    
    // Helper function to get motivational quotes
    fun getMotivationalQuotes(): List<MotivationalQuote> {
        return avatarRepository.motivationalQuotes
    }
}
