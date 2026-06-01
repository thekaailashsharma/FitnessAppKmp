package org.awi.fitness.ui.screens.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch
import org.awi.fitness.data.UserSettings
import org.awi.fitness.theme.GreenAccent
import org.awi.fitness.theme.TextGray
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.ui.components.statusBarPadding
import org.awi.fitness.utils.rememberImagePickerLauncher
import org.awi.fitness.viewmodel.CommunityViewModel

class CommunityProfileScreen(private val userId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { CommunityViewModel() }
        val profileState by viewModel.profileState.collectAsState()
        val scope = rememberCoroutineScope()
        val userSettings = UserSettings.getInstance()
        val currentUserEmail = userSettings.userEmail.orEmpty()
        val isOwnProfile = userId == currentUserEmail

        var showEditSheet by remember { mutableStateOf(false) }

        LaunchedEffect(userId) {
            viewModel.loadUserProfile(userId)
        }

        // Reset success flag
        LaunchedEffect(profileState.saveProfileSuccess) {
            if (profileState.saveProfileSuccess) showEditSheet = false
        }

        if (showEditSheet && isOwnProfile) {
            EditProfileBottomSheet(
                currentName = profileState.user?.name ?: userSettings.userName ?: "",
                currentBio = profileState.user?.bio ?: userSettings.userBio ?: "",
                currentPhotoUrl = profileState.user?.profileImage ?: userSettings.profilePhotoUrl,
                isSaving = profileState.isSavingProfile,
                onSave = { name, bio, photoUrl, imageBytes ->
                    scope.launch { viewModel.updateUserProfileWithPhoto(name, bio, photoUrl, imageBytes) }
                },
                onDismiss = { showEditSheet = false }
            )
        }

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when {
                profileState.isLoading && profileState.user == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenAccent)
                    }
                }

                profileState.user != null -> {
                    val user = profileState.user!!
                    LazyColumn(modifier = Modifier.fillMaxSize()) {

                        // ── Hero header ──────────────────────────────────────────
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                                // Cover gradient
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    GreenAccent.copy(alpha = 0.8f),
                                                    Color(0xFF006B48),
                                                    Color(0xFF004030)
                                                )
                                            )
                                        )
                                )

                                // Back button
                                IconButton(
                                    onClick = { navigator.pop() },
                                    modifier = Modifier
                                        .statusBarPadding()
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.25f), CircleShape)
                                ) {
                                    Icon(
                                        TablerIcons.ArrowLeft,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }

                                // Edit button (own profile)
                                if (isOwnProfile) {
                                    IconButton(
                                        onClick = { showEditSheet = true },
                                        modifier = Modifier
                                            .statusBarPadding()
                                            .padding(8.dp)
                                            .align(Alignment.TopEnd)
                                            .background(Color.Black.copy(alpha = 0.25f), CircleShape)
                                    ) {
                                        Icon(
                                            TablerIcons.Edit,
                                            contentDescription = "Edit profile",
                                            tint = Color.White
                                        )
                                    }
                                }

                                // Avatar — overlaps cover + content
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .align(Alignment.BottomCenter)
                                        .clip(CircleShape)
                                        .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                                ) {
                                    ImagePlaceholder(
                                        url = user.profileImage,
                                        contentDescription = user.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        showInitial = true,
                                        initial = user.name,
                                        tint = Color.White,
                                        backgroundColor = GreenAccent
                                    )
                                    // Camera overlay for own profile
                                    if (isOwnProfile) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.25f), CircleShape)
                                                .clickable { showEditSheet = true },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                TablerIcons.Camera,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── Name / username / bio ───────────────────────────────
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = user.username,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextGray
                                )
                                if (!user.bio.isNullOrBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = user.bio,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else if (isOwnProfile) {
                                    TextButton(onClick = { showEditSheet = true }) {
                                        Text("+ Add a bio", color = GreenAccent)
                                    }
                                }
                            }
                        }

                        // ── Stats row ────────────────────────────────────────────
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ProfileStatChip(value = "${profileState.userPosts.size}", label = "Posts")
                                ProfileStatDivider()
                                ProfileStatChip(
                                    value = if (user.streakDays > 0) "${user.streakDays}" else "—",
                                    label = "Day streak",
                                    valueColor = if (user.streakDays > 0) GreenAccent else null
                                )
                                ProfileStatDivider()
                                ProfileStatChip(value = "Lv ${user.level}", label = "Level")
                            }
                        }

                        // ── Follow / Message buttons (other's profile) ───────────
                        if (!isOwnProfile) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            scope.launch { viewModel.toggleFollowUser(user.id) }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (user.isFollowing) MaterialTheme.colorScheme.surfaceVariant else GreenAccent,
                                            contentColor = if (user.isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else Color.Black
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            if (user.isFollowing) TablerIcons.UserCheck else TablerIcons.UserPlus,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            if (user.isFollowing) "Following" else "Follow",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        } else {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showEditSheet = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GreenAccent)
                                    ) {
                                        Icon(
                                            TablerIcons.Edit,
                                            contentDescription = null,
                                            tint = GreenAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("Edit Profile", color = GreenAccent, fontWeight = FontWeight.SemiBold)
                                    }
                                    OutlinedButton(
                                        onClick = { navigator.push(CreatePostScreen()) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Icon(
                                            TablerIcons.Plus,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("New Post", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        // ── Posts section ─────────────────────────────────────────
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f))
                                Icon(
                                    TablerIcons.LayoutGrid,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp).size(18.dp)
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f))
                            }
                        }

                        if (profileState.userPosts.isEmpty()) {
                            item {
                                CommunityEmptyState(
                                    title = if (isOwnProfile) "No posts yet" else "No posts",
                                    message = if (isOwnProfile)
                                        "Share your first workout win with the community."
                                    else
                                        "${user.name} hasn't shared anything yet.",
                                    actionLabel = if (isOwnProfile) "Create post" else null,
                                    onAction = if (isOwnProfile) {
                                        { navigator.push(CreatePostScreen()) }
                                    } else null,
                                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
                                )
                            }
                        } else {
                            items(profileState.userPosts, key = { it.id }) { post ->
                                CommunityPostCard(
                                    post = post,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    onPostClick = { navigator.push(PostDetailScreen(post.id)) },
                                    onLikeClick = { scope.launch { viewModel.likePost(post.id) } },
                                    onCommentClick = { navigator.push(PostDetailScreen(post.id)) },
                                    onUserClick = { }
                                )
                            }
                        }

                        item { Spacer(Modifier.height(100.dp)) }
                    }
                }

                else -> {
                    CommunityEmptyState(
                        title = "Profile unavailable",
                        message = profileState.error ?: "Couldn't load this profile.",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileBottomSheet(
    currentName: String,
    currentBio: String,
    currentPhotoUrl: String?,
    isSaving: Boolean,
    onSave: (name: String, bio: String, photoUrl: String?, imageBytes: ByteArray?) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }
    var bio by remember { mutableStateOf(currentBio) }
    var selectedPhotoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var photoUrl by remember { mutableStateOf(currentPhotoUrl) }

    val imagePicker = rememberImagePickerLauncher { bytes ->
        selectedPhotoBytes = bytes
        // Photo bytes will be uploaded on save via CommunityRepository
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Edit Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }

            // Profile photo picker
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .border(2.dp, GreenAccent, CircleShape)
                    .clickable { imagePicker.launch() },
                contentAlignment = Alignment.Center
            ) {
                if (selectedPhotoBytes != null) {
                    ImagePlaceholder(
                        imageBytes = selectedPhotoBytes,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    ImagePlaceholder(
                        url = photoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        showInitial = true,
                        initial = name,
                        tint = androidx.compose.ui.graphics.Color.White,
                        backgroundColor = GreenAccent
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        TablerIcons.Camera,
                        contentDescription = "Change photo",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                "Tap to change photo",
                style = MaterialTheme.typography.labelSmall,
                color = TextGray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { if (it.length <= 150) bio = it },
                label = { Text("Bio") },
                placeholder = { Text("Tell the community about yourself…") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4,
                supportingText = { Text("${bio.length}/150", color = TextGray) }
            )

            Button(
                onClick = { onSave(name, bio, photoUrl, selectedPhotoBytes) },
                enabled = !isSaving && name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = androidx.compose.ui.graphics.Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save", color = androidx.compose.ui.graphics.Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProfileStatChip(
    value: String,
    label: String,
    valueColor: Color? = null,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextGray
        )
    }
}

@Composable
private fun ProfileStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    )
}
