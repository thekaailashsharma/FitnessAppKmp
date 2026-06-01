package org.awi.fitness.ui.screens.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import compose.icons.tablericons.X
import kotlinx.coroutines.launch
import org.awi.fitness.data.UserSettings
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

        var showStats by remember { mutableStateOf(false) }
        var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

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

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("New post", fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(TablerIcons.ArrowLeft, contentDescription = "Back")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    viewModel.createPost()
                                    if (viewModel.createPostState.value.isSuccess) {
                                        // Pop FIRST before any further state emissions
                                        // to avoid Voyager lifecycle crash on Android
                                        navigator.pop()
                                        CommunityEvents.requestFeedRefresh()
                                    }
                                }
                            },
                            enabled = createPostState.content.isNotBlank() && !createPostState.isLoading
                        ) {
                            if (createPostState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Post", fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier.statusBarPadding()
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CommunityAvatar(name = currentUserName, size = 44.dp)
                    Column {
                        Text(currentUserName, fontWeight = FontWeight.Bold)
                        Text(
                            "Visible to the community",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                OutlinedTextField(
                    value = createPostState.content,
                    onValueChange = { viewModel.updateCreatePostContent(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp),
                    placeholder = { Text("What did you accomplish today?") },
                    shape = RoundedCornerShape(12.dp)
                )

                createPostState.error?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error)
                }

                AnimatedVisibility(visible = selectedImageBytes != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        ImagePlaceholder(
                            imageBytes = selectedImageBytes,
                            contentDescription = "Selected photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = {
                                selectedImageBytes = null
                                viewModel.updateCreatePostImageBytes(null)
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    CircleShape
                                )
                        ) {
                            Icon(TablerIcons.X, contentDescription = "Remove photo")
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { imagePicker.launch() }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(TablerIcons.Photo, null, tint = MaterialTheme.colorScheme.primary)
                    Text("Add photo", style = MaterialTheme.typography.bodyLarge)
                }

                Surface(
                    onClick = { showStats = !showStats },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Add workout stats (optional)", fontWeight = FontWeight.Medium)
                        Icon(
                            if (showStats) TablerIcons.ChevronUp else TablerIcons.ChevronDown,
                            contentDescription = null
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showStats,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatField(
                            label = "Calories",
                            value = createPostState.calories?.toString() ?: "",
                            onValueChange = { viewModel.updateCreatePostCalories(it.toIntOrNull()) }
                        )
                        StatField(
                            label = "Steps",
                            value = createPostState.steps?.toString() ?: "",
                            onValueChange = { viewModel.updateCreatePostSteps(it.toIntOrNull()) }
                        )
                        StatField(
                            label = "Duration (minutes)",
                            value = createPostState.duration?.let { it / 60 }?.toString() ?: "",
                            onValueChange = { minutes ->
                                viewModel.updateCreatePostDuration(minutes.toIntOrNull()?.times(60))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}
