package org.awi.fitness.ui.screens.avatar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.Settings
import kotlinx.coroutines.launch
import org.awi.fitness.model.AvatarMood
import org.awi.fitness.theme.GreenAccent
import org.awi.fitness.model.ConversationTrigger
import org.awi.fitness.ui.components.Avatar
import org.awi.fitness.ui.components.AvatarActionBar
import org.awi.fitness.ui.components.MessageBubble
import org.awi.fitness.ui.components.QuickReplyChip
import org.awi.fitness.ui.components.QuoteCard
import org.awi.fitness.ui.components.TopicCard
import org.awi.fitness.ui.components.TypingIndicator
import org.awi.fitness.ui.components.statusBarPadding
import org.awi.fitness.viewmodel.AvatarViewModel

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import compose.icons.tablericons.Send
import org.awi.fitness.data.UserSettings

class AvatarScreen(
    private val trigger: ConversationTrigger? = null
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { AvatarViewModel(trigger) }
        val state by viewModel.state.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()
        val listState = rememberLazyListState()
        val userSettings = UserSettings.getInstance() // Fix: Get userSettings here
        
        var isAvatarAnimating by remember { mutableStateOf(false) }
        var userInputValue by remember { mutableStateOf("") } // Fix: Defined here

        
        // Show error in snackbar if present
        LaunchedEffect(state.error) {
            state.error?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }

        // Check for avatar selection and redirect if needed
        LaunchedEffect(state.hasSelectedAvatar, state.isLoading) {
            if (!state.isLoading && !state.hasSelectedAvatar) {
                navigator.push(AvatarSelectionScreen())
            }
        }
        
        // Scroll to bottom when new messages arrive
        LaunchedEffect(state.conversationState.messages.size) {
            if (state.conversationState.messages.isNotEmpty()) {
                listState.animateScrollToItem(state.conversationState.messages.size - 1)
            }
        }
        
        // Animate avatar when mood changes
        LaunchedEffect(state.conversationState.currentMood) {
            isAvatarAnimating = true
            kotlinx.coroutines.delay(500)
            isAvatarAnimating = false
        }
        
        // Reload avatar when screen appears or settings change
        LaunchedEffect(Unit, state.hasSelectedAvatar) {
            viewModel.selectAvatar(userSettings.selectedAvatarId ?: return@LaunchedEffect)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Fitness Buddy",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = TablerIcons.ArrowLeft,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            coroutineScope.launch {
                                navigator.push(AvatarSelectionScreen())
                            }
                        }) {
                            Icon(
                                imageVector = TablerIcons.Settings,
                                contentDescription = "Avatar Settings"
                            )
                        }
                        IconButton(onClick = { viewModel.resetConversation() }) {
                            Icon(
                                imageVector = TablerIcons.Refresh,
                                contentDescription = "Reset Conversation"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.statusBarPadding()
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Quick reply chips - prominent display
                    if (state.conversationState.suggestedResponses.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(state.conversationState.suggestedResponses) { response ->
                                QuickReplyChip(
                                    text = response,
                                    onClick = {
                                        viewModel.sendUserMessage(response)
                                    },
                                    enabled = !state.conversationState.isTyping && !state.isLoading
                                )
                            }
                        }
                    }

                    // Text Input Field
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = userInputValue,
                            onValueChange = { userInputValue = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Chat with your buddy...") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (userInputValue.isNotBlank()) {
                                        viewModel.sendUserMessage(userInputValue)
                                        userInputValue = ""
                                    }
                                }
                            )
                        )
                        
                        IconButton(
                            onClick = {
                                if (userInputValue.isNotBlank()) {
                                    viewModel.sendUserMessage(userInputValue)
                                    userInputValue = ""
                                }
                            },
                            enabled = userInputValue.isNotBlank() && !state.isLoading
                        ) {
                            Icon(
                                imageVector = TablerIcons.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Action bar
                    AvatarActionBar(
                        onTopicsClick = { viewModel.toggleTopicsVisibility() },
                        onQuotesClick = { viewModel.toggleQuotesVisibility() },
                        showTopics = state.showTopics,
                        showQuotes = state.showQuotes
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        modifier = Modifier.padding(16.dp),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Text(text = data.visuals.message)
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar - larger display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Avatar(
                            mood = state.conversationState.currentMood,
                            isAnimating = isAvatarAnimating,
                            modifier = Modifier.size(120.dp),
                            avatarImageUrl = state.selectedAvatar?.imageUrl
                        )
                    }
                    
                    // Messages
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.conversationState.messages) { message ->
                            MessageBubble(
                                message = message.content,
                                isFromAvatar = message.isFromAvatar
                            )
                        }
                        
                        if (state.conversationState.isTyping || state.isLoading) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    TypingIndicator()
                                }
                            }
                        }
                    }
                    
                    // Topics panel
                    AnimatedVisibility(
                        visible = state.showTopics,
                        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it },
                        exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { it }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Conversation Topics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            viewModel.getConversationTopics().forEach { topic ->
                                TopicCard(
                                    topic = topic,
                                    onClick = {
                                        viewModel.selectTopic(topic)
                                    }
                                )
                            }
                        }
                    }
                    
                    // Quotes panel
                    AnimatedVisibility(
                        visible = state.showQuotes,
                        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it },
                        exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { it }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Motivational Quotes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            viewModel.getMotivationalQuotes().forEach { quote ->
                                QuoteCard(
                                    quote = quote,
                                    onClick = {
                                        viewModel.shareQuote(quote)
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Removed full screen loading indicator
            }
        }
    }
}

private val CircleShape = RoundedCornerShape(percent = 50)
