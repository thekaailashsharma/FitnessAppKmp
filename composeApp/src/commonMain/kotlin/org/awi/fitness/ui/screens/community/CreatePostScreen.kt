package org.awi.fitness.ui.screens.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.ChevronUp
import compose.icons.tablericons.Photo
import compose.icons.tablericons.World
import compose.icons.tablericons.X
import kotlinx.coroutines.launch
import org.awi.fitness.data.UserSettings
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GlassChip
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.ui.components.statusBarPadding
import org.awi.fitness.utils.CommunityEvents
import org.awi.fitness.utils.rememberImagePickerLauncher
import org.awi.fitness.viewmodel.CommunityViewModel

class CreatePostScreen(
    private val prefilledContent: String = ""
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { CommunityViewModel() }
        val createPostState by viewModel.createPostState.collectAsState()
        val scope = rememberCoroutineScope()
        val userSettings = UserSettings.getInstance()
        val currentUserEmail = userSettings.userEmail.orEmpty()
        val currentUserName = userSettings.userName?.takeIf { it.isNotBlank() }
            ?: currentUserEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        val c = TajlyTheme.colors

        var showStats by remember { mutableStateOf(false) }
        var caloriesOpen by remember { mutableStateOf(false) }
        var stepsOpen by remember { mutableStateOf(false) }
        var durationOpen by remember { mutableStateOf(false) }
        var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

        // One-time fade + rise on appearance (no looping motion).
        var appeared by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { appeared = true }
        val enter by animateFloatAsState(
            targetValue = if (appeared) 1f else 0f,
            animationSpec = tween(durationMillis = 320),
            label = "createPostEnter",
        )

        val imagePicker = rememberImagePickerLauncher { bytes ->
            selectedImageBytes = bytes
            viewModel.updateCreatePostImageBytes(bytes)
        }

        LaunchedEffect(Unit) { viewModel.resetCreatePostState() }

        LaunchedEffect(prefilledContent) {
            if (prefilledContent.isNotBlank()) {
                viewModel.updateCreatePostContent(prefilledContent)
            }
        }

        val canPost = createPostState.content.isNotBlank() && !createPostState.isLoading
        val submit: () -> Unit = {
            scope.launch {
                viewModel.createPost()
                if (viewModel.createPostState.value.isSuccess) {
                    // Pop FIRST before any further state emissions
                    // to avoid Voyager lifecycle crash on Android
                    navigator.pop()
                    CommunityEvents.requestFeedRefresh()
                }
            }
        }

        AuroraBackground(modifier = Modifier.fillMaxSize()) {
            // Community section glow (teal) behind the header.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Tajly.sectionGlow(Tajly.Teal)),
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // ---- Glass top bar ----
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarPadding()
                        .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlassIconButton(onClick = { navigator.pop() }) {
                        Icon(
                            TablerIcons.ArrowLeft,
                            contentDescription = "Back",
                            tint = c.textHi,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "New post",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi,
                        modifier = Modifier.weight(1f),
                    )
                    if (createPostState.isLoading) {
                        CircularProgressIndicator(
                            color = GoldPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer {
                            alpha = enter
                            translationY = (1f - enter) * 22.dp.toPx()
                        }
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // ---- Author glass card ----
                    GlassCard(shape = RoundedCornerShape(20.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CommunityAvatar(name = currentUserName, imageUrl = org.awi.fitness.data.UserSettings.getInstance().profilePhotoUrl, size = 44.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    currentUserName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = c.textHi,
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        TablerIcons.World,
                                        contentDescription = null,
                                        tint = Tajly.Teal,
                                        modifier = Modifier.size(13.dp),
                                    )
                                    Text(
                                        "Visible to the community",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = c.textMid,
                                    )
                                }
                            }
                        }
                    }

                    // ---- Content composer ----
                    OutlinedTextField(
                        value = createPostState.content,
                        onValueChange = { viewModel.updateCreatePostContent(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp),
                        placeholder = {
                            Text("What did you accomplish today?", color = c.textLow)
                        },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        shape = RoundedCornerShape(16.dp),
                        colors = glassTextFieldColors(c.textHi, c.glassFill, c.hairStrong),
                    )

                    createPostState.error?.let { error ->
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }

                    AnimatedVisibility(visible = selectedImageBytes != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp)),
                        ) {
                            ImagePlaceholder(
                                imageBytes = selectedImageBytes,
                                contentDescription = "Selected photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .clickable {
                                        selectedImageBytes = null
                                        viewModel.updateCreatePostImageBytes(null)
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    TablerIcons.X,
                                    contentDescription = "Remove photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }

                    // ---- Add photo (glass) ----
                    GlassActionRow(
                        onClick = { imagePicker.launch() },
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Tajly.sectionGlow(Tajly.Teal)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                TablerIcons.Photo,
                                contentDescription = null,
                                tint = Tajly.Teal,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Text(
                            "Add photo",
                            style = MaterialTheme.typography.bodyLarge,
                            color = c.textHi,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // ---- Workout stats toggle (glass) ----
                    GlassActionRow(onClick = { showStats = !showStats }) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Tajly.sectionGlow(Tajly.Violet)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                if (showStats) TablerIcons.ChevronUp else TablerIcons.ChevronDown,
                                contentDescription = null,
                                tint = Tajly.Violet,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Text(
                            "Add workout stats (optional)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = c.textHi,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    AnimatedVisibility(
                        visible = showStats,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Pick which stats to attach — gold chip = active field.
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                GlassChip(
                                    text = "Calories",
                                    selected = caloriesOpen || createPostState.calories != null,
                                    onClick = {
                                        caloriesOpen = !caloriesOpen
                                        if (!caloriesOpen) viewModel.updateCreatePostCalories(null)
                                    },
                                )
                                GlassChip(
                                    text = "Steps",
                                    selected = stepsOpen || createPostState.steps != null,
                                    onClick = {
                                        stepsOpen = !stepsOpen
                                        if (!stepsOpen) viewModel.updateCreatePostSteps(null)
                                    },
                                )
                                GlassChip(
                                    text = "Duration",
                                    selected = durationOpen || createPostState.duration != null,
                                    onClick = {
                                        durationOpen = !durationOpen
                                        if (!durationOpen) viewModel.updateCreatePostDuration(null)
                                    },
                                )
                            }
                            AnimatedVisibility(visible = caloriesOpen) {
                                StatField(
                                    label = "Calories",
                                    value = createPostState.calories?.toString() ?: "",
                                    onValueChange = { viewModel.updateCreatePostCalories(it.toIntOrNull()) },
                                )
                            }
                            AnimatedVisibility(visible = stepsOpen) {
                                StatField(
                                    label = "Steps",
                                    value = createPostState.steps?.toString() ?: "",
                                    onValueChange = { viewModel.updateCreatePostSteps(it.toIntOrNull()) },
                                )
                            }
                            AnimatedVisibility(visible = durationOpen) {
                                StatField(
                                    label = "Duration (minutes)",
                                    value = createPostState.duration?.let { it / 60 }?.toString() ?: "",
                                    onValueChange = { minutes ->
                                        viewModel.updateCreatePostDuration(minutes.toIntOrNull()?.times(60))
                                    },
                                )
                            }
                        }
                    }
                }

                // ---- Primary on-screen action: gold Share button ----
                GoldButton(
                    text = "Share with community",
                    onClick = submit,
                    enabled = canPost,
                    loading = createPostState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun GlassIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .pressScale(interaction)
            .size(40.dp)
            .clip(CircleShape)
            .background(c.glassFill, CircleShape)
            .border(1.dp, c.hairStrong, CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun GlassActionRow(
    onClick: () -> Unit,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    GlassCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun glassTextFieldColors(
    textColor: Color,
    fill: androidx.compose.ui.graphics.Brush,
    border: Color,
) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = textColor,
    unfocusedTextColor = textColor,
    cursorColor = GoldPrimary,
    focusedBorderColor = GoldPrimary,
    unfocusedBorderColor = border,
    focusedLabelColor = GoldPrimary,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val c = TajlyTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = glassTextFieldColors(c.textHi, c.glassFill, c.hairStrong),
    )
}
