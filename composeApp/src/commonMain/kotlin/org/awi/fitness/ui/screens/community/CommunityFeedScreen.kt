package org.awi.fitness.ui.screens.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.Bell
import compose.icons.tablericons.Heart
import compose.icons.tablericons.MessageCircle
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Search
import compose.icons.tablericons.Share
import compose.icons.tablericons.Trophy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.model.CommunityPost
import org.awi.fitness.repository.CommunityRepository
import org.awi.fitness.theme.GreenAccent
import org.awi.fitness.theme.TextGray
import org.awi.fitness.ui.components.statusBarPadding
import org.awi.fitness.ui.components.FitnessCard
import org.awi.fitness.utils.DateUtils
import org.awi.fitness.viewmodel.CommunityViewModel

class CommunityFeedScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { CommunityViewModel() }
        val state by viewModel.state.collectAsState()
        val communityRepository = remember { CommunityRepository() }
        val coroutineScope = rememberCoroutineScope()

        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Public", "Friends", "My Posts")

        LaunchedEffect(selectedTab) {
            val filter = when (selectedTab) {
                0 -> "public"
                1 -> "friends"
                2 -> "my_posts"
                else -> "public"
            }
            // Load feed with filter
            viewModel.loadCommunityFeed(filter)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Community Feed",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            navigator.push(FindFriendsScreen())
                        }) {
                            Icon(
                                imageVector = TablerIcons.Search,
                                contentDescription = "Search"
                            )
                        }
                        IconButton(onClick = {
                            navigator.push(ActivityScreen())
                        }) {
                            Icon(
                                imageVector = TablerIcons.Bell,
                                contentDescription = "Notifications"
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
            floatingActionButton = {
                androidx.compose.material3.FloatingActionButton(
                    onClick = {
                        navigator.push(CreatePostScreen())
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = TablerIcons.Plus,
                        contentDescription = "Create Post"
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Tabs
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        SegmentedButton(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = tabs.size
                            ),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primary,
                                activeContentColor = Color.Black,
                                inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                inactiveContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(title)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (state.error != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else if (state.posts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No posts found",
                            color = TextGray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Use mock data for demonstration
                        items(communityRepository.mockPosts) { post ->
                            androidx.compose.animation.AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)) { it / 5 },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                            PostCard(
                                post = post,
                                onPostClick = {
                                    navigator.push(PostDetailScreen(post.id))
                                },
                                onLikeClick = {
                                    coroutineScope.launch {
                                        viewModel.likePost(post.id)
                                    }
                                },
                                onCommentClick = {
                                    navigator.push(PostDetailScreen(post.id))
                                },
                                onShareClick = {
                                    // Handle share click
                                },
                                onUserClick = {
                                    navigator.push(CommunityProfileScreen(post.userId))
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

@Composable
fun PostCard(
    post: CommunityPost,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onUserClick: () -> Unit
) {
    var liked by remember { mutableStateOf(false) }
    var heartScale by remember { mutableStateOf(1f) }
    val heartScaleAnim = animateFloatAsState(
        targetValue = heartScale,
        animationSpec = spring(dampingRatio = 0.3f, stiffness = 300f)
    )
    val coroutineScope = rememberCoroutineScope()

    FitnessCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPostClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // User info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile image
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onUserClick() }
                ) {
                    if (post.userProfileImage != null) {
                        ImagePlaceholder(
                            url = post.userProfileImage,
                            contentDescription = "Profile Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            showInitial = true,
                            initial = post.userName
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = post.userName.first().toString(),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = post.userName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // Badges or streak
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (post.isPersonalBest) {
                            BadgeChip(text = "Personal Best", icon = TablerIcons.Trophy)
                        } else if (post.streakDays != null) {
                            BadgeChip(text = "${post.streakDays}-day streak")
                        } else if (post.badges.isNotEmpty()) {
                            BadgeChip(text = post.badges.first())
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post content
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )

            Spacer(modifier = Modifier.height(12.dp))

            // Post image if available
            if (post.imageUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    ImagePlaceholder(
                        url = post.imageUrl,
                        contentDescription = "Post Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Workout stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                post.calories?.let {
                    WorkoutStat(
                        icon = "🔥",
                        value = "Calories: $it kcal"
                    )
                }

                post.steps?.let {
                    WorkoutStat(
                        icon = "👣",
                        value = "Steps: $it"
                    )
                }

                post.duration?.let {
                    val formattedTime = DateUtils.formatSeconds(it)
                    WorkoutStat(
                        icon = "⏱️",
                        value = "Time: $formattedTime",
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    icon = TablerIcons.Heart,
                    text = "Cheer",
                    onClick = {
                        liked = !liked
                        heartScale = 1.5f
                        onLikeClick()
                        // Reset scale after animation completes
                        coroutineScope.launch {
                            delay(300)
                            heartScale = 1f
                        }
                    },
                    tint = if (liked) GreenAccent else MaterialTheme.colorScheme.onSurface,
                    iconScale = if (liked) heartScaleAnim.value else 1f
                )

                ActionButton(
                    icon = TablerIcons.MessageCircle,
                    text = "Comment",
                    onClick = onCommentClick
                )

                ActionButton(
                    icon = TablerIcons.Share,
                    text = "Share",
                    onClick = onShareClick
                )
            }
        }
    }
}

@Composable
fun BadgeChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GreenAccent.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = GreenAccent
        )
    }
}

@Composable
fun WorkoutStat(
    icon: String,
    value: String,
    singleLine: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (singleLine) 1 else 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    iconScale: Float = 1f
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tint,
            modifier = Modifier
                .size(20.dp)
                .scale(iconScale)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = tint
        )
    }
}
