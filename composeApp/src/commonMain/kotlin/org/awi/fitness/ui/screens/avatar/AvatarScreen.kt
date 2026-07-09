package org.awi.fitness.ui.screens.avatar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.DotsVertical
import compose.icons.tablericons.MessageCircle
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.Settings
import compose.icons.tablericons.Star
import compose.icons.tablericons.Users
import kotlinx.coroutines.launch
import org.awi.fitness.model.AvatarMessage
import org.awi.fitness.model.ConversationTrigger
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GlassChip
import org.awi.fitness.ui.components.GoldButton
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import compose.icons.tablericons.Send
import org.awi.fitness.data.UserSettings
import org.awi.fitness.data.StringKey
import org.awi.fitness.viewmodel.LanguageViewModel
import org.jetbrains.compose.resources.painterResource
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.avatar1
import fitnessappkmp.composeapp.generated.resources.avatar2
import fitnessappkmp.composeapp.generated.resources.avatar3
import fitnessappkmp.composeapp.generated.resources.avatar4
import fitnessappkmp.composeapp.generated.resources.avatar5
import fitnessappkmp.composeapp.generated.resources.avatar6
import fitnessappkmp.composeapp.generated.resources.ic3d_heart
import fitnessappkmp.composeapp.generated.resources.ic3d_gym

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
        val userSettings = UserSettings.getInstance()
        val languageViewModel = remember { LanguageViewModel(userSettings.settings) }

        var userInputValue by remember { mutableStateOf("") }
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current

        // Voice input (speech-to-text) — fills the composer as the user talks.
        val voiceInput = remember { org.awi.fitness.voice.VoiceInput() }
        var listening by remember { mutableStateOf(false) }

        val c = TajlyTheme.colors

        // Show error in snackbar if present
        LaunchedEffect(state.error) {
            state.error?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
        }

        // First-run only: if the user has never chosen an avatar, open the picker ONCE.
        // This is a one-shot (guarded by `didAutoOpenPicker`) so that if the user presses back
        // from the picker WITHOUT selecting, we don't immediately re-push it — which is what
        // made the back button feel broken/deceptive. Combined with selectedAvatarId being
        // non-null the instant a choice is made, the picker's back now always works.
        var didAutoOpenPicker by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(state.isLoading) {
            if (!state.isLoading &&
                !state.hasSelectedAvatar &&
                userSettings.selectedAvatarId == null &&
                !didAutoOpenPicker
            ) {
                didAutoOpenPicker = true
                navigator.push(AvatarSelectionScreen())
            }
        }

        // Scroll to bottom when new messages arrive
        LaunchedEffect(state.conversationState.messages.size) {
            if (state.conversationState.messages.isNotEmpty()) {
                listState.animateScrollToItem(state.conversationState.messages.size - 1)
            }
        }

        // Reload avatar when screen appears or settings change
        LaunchedEffect(Unit, state.hasSelectedAvatar) {
            viewModel.selectAvatar(userSettings.selectedAvatarId ?: return@LaunchedEffect)
        }

        // The selected coach drives the name + photo shown throughout the chat.
        val selectedAvatar = org.awi.fitness.data.AvatarData.avatars
            .firstOrNull { it.id == userSettings.selectedAvatarId }
            ?: org.awi.fitness.data.AvatarData.avatars.first()
        val coachName = selectedAvatar.name
        val coachImage = selectedAvatar.imageUrl
        val coachTagline = selectedAvatar.tagline

        Scaffold(
            modifier = Modifier.imePadding(),
            containerColor = c.bg,
            topBar = {
                CoachHeader(
                    title = coachName,
                    subtitle = coachTagline,
                    imageUrl = coachImage,
                    backDesc = languageViewModel.getString(StringKey.BACK),
                    changeCoachLabel = languageViewModel.getString(StringKey.AVATAR_SETTINGS),
                    resetLabel = languageViewModel.getString(StringKey.RESET_CONVERSATION),
                    onBack = { navigator.pop() },
                    onChangeCoach = {
                        coroutineScope.launch {
                            navigator.push(AvatarSelectionScreen())
                        }
                    },
                    onReset = { viewModel.resetConversation() },
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .background(c.bg)
                ) {
                    // Quick reply chips (glass) — driven by suggestedResponses
                    if (state.conversationState.suggestedResponses.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            items(state.conversationState.suggestedResponses) { response ->
                                val chipEnabled = !state.conversationState.isTyping && !state.isLoading
                                GlassChip(
                                    text = response,
                                    selected = false,
                                    onClick = {
                                        if (chipEnabled) {
                                            viewModel.sendUserMessage(response)
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Glass composer + gold send button — pinned at bottom
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = userInputValue,
                                onValueChange = { userInputValue = it },
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(
                                        text = languageViewModel.getString(StringKey.CHAT_WITH_BUDDY_PLACEHOLDER),
                                        color = c.textLow
                                    )
                                },
                                textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textHi),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    cursorColor = GoldPrimary,
                                    focusedTextColor = c.textHi,
                                    unfocusedTextColor = c.textHi
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (userInputValue.isNotBlank()) {
                                            viewModel.sendUserMessage(userInputValue)
                                            userInputValue = ""
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                        }
                                    }
                                ),
                                maxLines = 4
                            )

                            // Mic — talk to the coach (speech-to-text).
                            if (voiceInput.isAvailable()) {
                                IconButton(
                                    onClick = {
                                        if (listening) {
                                            voiceInput.stop(); listening = false
                                        } else {
                                            listening = true
                                            voiceInput.start(
                                                onPartial = { userInputValue = it },
                                                onFinal = { userInputValue = it; listening = false },
                                                onError = { listening = false },
                                            )
                                        }
                                    }
                                ) {
                                    Text(if (listening) "🎙️" else "🎤", fontSize = 18.sp)
                                }
                            }

                            GoldSendButton(
                                enabled = userInputValue.isNotBlank() && !state.isLoading,
                                contentDescription = languageViewModel.getString(StringKey.SEND),
                                onClick = {
                                    if (userInputValue.isNotBlank()) {
                                        viewModel.sendUserMessage(userInputValue)
                                        userInputValue = ""
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    }
                                }
                            )
                        }
                    }
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
                // Premium backdrop behind the conversation (falls back gracefully), with a
                // readable scrim so full-width coach text stays legible.
                org.awi.fitness.ui.components.BackdropImage(
                    surface = "coach",
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                0f to c.bg.copy(alpha = 0.74f),
                                1f to c.bg.copy(alpha = 0.93f),
                            )
                        )
                )
                Column(modifier = Modifier.fillMaxSize()) {
                    // Conversation — full-width coach replies + gold-tinted user bubbles
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val msgs = state.conversationState.messages
                        itemsIndexed(msgs) { index, message ->
                            if (message.isFromAvatar) {
                                // Skip empty coach bubbles (e.g. a reply that was only a card marker).
                                if (message.content.isBlank() && message.card == null) return@itemsIndexed
                                // Group consecutive coach messages: only the FIRST in a run shows
                                // the avatar photo + name; the rest are just aligned text bubbles.
                                val showHeader = index == 0 || !msgs[index - 1].isFromAvatar
                                CoachReply(
                                    message = message,
                                    coachName = coachName,
                                    coachImage = coachImage,
                                    showHeader = showHeader,
                                    onCardAction = { action ->
                                        // Real navigation: leave the chat and open the actual tab.
                                        when (action) {
                                            "workout", "meals" -> {
                                                org.awi.fitness.navigation.CoachNav.open("TRAIN")
                                                navigator.popUntilRoot()
                                            }
                                            "challenges" -> {
                                                org.awi.fitness.navigation.CoachNav.open("CHALLENGES")
                                                navigator.popUntilRoot()
                                            }
                                            "checkin" -> {
                                                viewModel.sendUserMessage("I'd like to do my daily check-in")
                                                keyboardController?.hide()
                                                focusManager.clearFocus()
                                            }
                                        }
                                    },
                                )
                            } else {
                                UserBubble(message = message.content)
                            }
                        }

                        if (state.conversationState.isTyping || state.isLoading) {
                            item {
                                CoachTypingRow(coachImage = coachImage)
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
                                .background(c.bg)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = languageViewModel.getString(StringKey.CONVERSATION_TOPICS),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = c.textHi,
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
                                .background(c.bg)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = languageViewModel.getString(StringKey.MOTIVATIONAL_QUOTES),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = c.textHi,
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
            }
        }
    }
}

/** Gold coach orb — a solid gold gradient disc with a gentle breathing glow + inner highlight. */
@Composable
private fun GoldMascot(size: androidx.compose.ui.unit.Dp) {
    val transition = rememberInfiniteTransition()
    val breathe by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1900), RepeatMode.Reverse)
    )
    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer { scaleX = breathe; scaleY = breathe }
            .clip(RoundedCornerShape(percent = 50))
            .background(
                Brush.radialGradient(
                    colors = listOf(GoldBright, GoldPrimary, Color(0xFF8C6F2E))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.38f)
                .clip(RoundedCornerShape(percent = 50))
                .background(Color.White.copy(alpha = 0.22f))
        )
    }
}

/** Header: gold mascot + "Coach" + "AI · honest with your data", with back / settings / reset actions. */
@Composable
private fun CoachHeader(
    title: String,
    subtitle: String,
    imageUrl: String?,
    backDesc: String,
    changeCoachLabel: String,
    resetLabel: String,
    onBack: () -> Unit,
    onChangeCoach: () -> Unit,
    onReset: () -> Unit,
) {
    val c = TajlyTheme.colors
    // Decluttered header: just Back · coach identity · one overflow menu.
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.bg)
            .statusBarPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = TablerIcons.ArrowLeft,
                contentDescription = backDesc,
                tint = c.textHi
            )
        }

        CoachAvatar(imageUrl = imageUrl, size = 40.dp)

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = c.textHi
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(
                    imageVector = TablerIcons.DotsVertical,
                    contentDescription = "More",
                    tint = c.textMid
                )
            }
            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
            ) {
                DropdownMenuItem(
                    text = { Text(changeCoachLabel, color = c.textHi) },
                    leadingIcon = {
                        Icon(TablerIcons.Users, contentDescription = null, tint = c.textMid)
                    },
                    onClick = { menuOpen = false; onChangeCoach() },
                )
                DropdownMenuItem(
                    text = { Text(resetLabel, color = c.textHi) },
                    leadingIcon = {
                        Icon(TablerIcons.Refresh, contentDescription = null, tint = c.textMid)
                    },
                    onClick = { menuOpen = false; onReset() },
                )
            }
        }
    }
}

/**
 * Full-width coach reply: small gold mascot avatar + label, then full-width body text (no bubble).
 * When the reply suggests a workout, an inline glass "generative" card with a gold CTA is rendered.
 */
@Composable
private fun CoachReply(
    message: AvatarMessage,
    coachName: String,
    coachImage: String?,
    showHeader: Boolean,
    onCardAction: (String) -> Unit,
) {
    val c = TajlyTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        if (showHeader) {
            CoachAvatar(imageUrl = coachImage, size = 32.dp)
            Spacer(modifier = Modifier.width(12.dp))
        } else {
            // Align continuation bubbles under the text column (avatar 32 + gap 12).
            Spacer(modifier = Modifier.width(44.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            if (showHeader) {
                Text(
                    text = coachName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            if (message.content.isNotBlank()) {
                org.awi.fitness.ui.components.MarkdownText(
                    markdown = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = c.textHi,
                )
            }
            message.card?.let { card ->
                Spacer(modifier = Modifier.height(10.dp))
                org.awi.fitness.ui.components.CoachCardView(card = card, onAction = onCardAction)
            }
        }
    }
}

/** The selected coach's photo (avatar1..6) in a circle; falls back to the gold orb. */
@Composable
private fun CoachAvatar(imageUrl: String?, size: androidx.compose.ui.unit.Dp) {
    val res = when (imageUrl) {
        "avatar1" -> Res.drawable.avatar1
        "avatar2" -> Res.drawable.avatar2
        "avatar3" -> Res.drawable.avatar3
        "avatar4" -> Res.drawable.avatar4
        "avatar5" -> Res.drawable.avatar5
        "avatar6" -> Res.drawable.avatar6
        else -> null
    }
    if (res != null) {
        Image(
            painter = painterResource(res),
            contentDescription = null,
            modifier = Modifier.size(size).clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        GoldMascot(size = size)
    }
}

/** Inline generative glass card — "Suggested · Leg day", gym mascot, and a gold "Start workout" CTA. */
@Composable
private fun InlineWorkoutCard(
    modifier: Modifier = Modifier,
    onStartWorkout: () -> Unit,
) {
    val c = TajlyTheme.colors
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Suggested",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textMid
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Leg day · 5 exercises",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi
                    )
                }
                Image(
                    painter = painterResource(Res.drawable.ic3d_gym),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            GoldButton(
                text = "Start workout",
                onClick = onStartWorkout,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Small gold-tinted, right-aligned user bubble. */
@Composable
private fun UserBubble(message: String) {
    val c = TajlyTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                .background(GoldPrimary.copy(alpha = if (c.isDark) 0.20f else 0.16f))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = c.textHi
            )
        }
    }
}

/** Typing indicator laid out as a full-width coach row (mascot + animated dots). */
@Composable
private fun CoachTypingRow(coachImage: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoachAvatar(imageUrl = coachImage, size = 32.dp)
        Spacer(modifier = Modifier.width(8.dp))
        TypingIndicator()
    }
}

/** Gold gradient send button used in the glass composer. */
@Composable
private fun GoldSendButton(
    enabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .pressScale(interaction)
            .size(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (enabled) Modifier.background(Tajly.GoldGradient)
                else Modifier.background(GoldPrimary.copy(alpha = 0.25f))
            )
            .androidxClickable(enabled, interaction, onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = TablerIcons.Send,
            contentDescription = contentDescription,
            tint = if (enabled) OnGold else OnGold.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

private fun Modifier.androidxClickable(
    enabled: Boolean,
    interaction: MutableInteractionSource,
    onClick: () -> Unit,
): Modifier = this.clickable(
    interactionSource = interaction,
    indication = null,
    enabled = enabled,
) { onClick() }

/** Heuristic: does the coach reply suggest loading/starting a workout? Drives the inline card only. */
private fun suggestsWorkout(content: String): Boolean {
    val t = content.lowercase()
    return ("workout" in t || "leg" in t || "push" in t || "train" in t || "session" in t) &&
        ("load" in t || "start" in t || "want me to" in t || "ready" in t || "push legs" in t || "let's" in t)
}
