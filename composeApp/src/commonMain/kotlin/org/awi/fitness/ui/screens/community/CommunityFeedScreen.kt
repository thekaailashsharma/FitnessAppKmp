package org.awi.fitness.ui.screens.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.Bell
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Users
import kotlinx.coroutines.launch
import org.awi.fitness.data.UserSettings
import org.awi.fitness.ui.components.statusBarPadding
import org.awi.fitness.utils.CommunityEvents
import org.awi.fitness.viewmodel.CommunityViewModel

class CommunityFeedScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { CommunityViewModel() }
        val state by viewModel.state.collectAsState()
        val activityState by viewModel.activityState.collectAsState()
        val scope = rememberCoroutineScope()
        val userSettings = UserSettings.getInstance()
        val currentUserEmail = userSettings.userEmail.orEmpty()
        val currentUserName = userSettings.userName?.takeIf { it.isNotBlank() }
            ?: currentUserEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        val refreshTick by CommunityEvents.feedRefreshTick.collectAsState()

        var selectedTab by remember { mutableStateOf(CommunityFeedTab.FOR_YOU) }

        LaunchedEffect(selectedTab, refreshTick) {
            viewModel.loadCommunityFeed(selectedTab.filter)
        }

        LaunchedEffect(Unit) {
            viewModel.loadActivityNotifications()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Community",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { navigator.push(FindFriendsScreen()) }) {
                            Icon(TablerIcons.Users, contentDescription = "Find people")
                        }
                        BadgedBox(
                            badge = {
                                if (activityState.unreadCount > 0) {
                                    Badge { Text("${activityState.unreadCount.coerceAtMost(9)}") }
                                }
                            }
                        ) {
                            IconButton(onClick = { navigator.push(ActivityScreen()) }) {
                                Icon(TablerIcons.Bell, contentDescription = "Activity")
                            }
                        }
                        IconButton(onClick = {
                            if (currentUserEmail.isNotBlank()) {
                                navigator.push(CommunityProfileScreen(currentUserEmail))
                            }
                        }) {
                            CommunityAvatar(name = currentUserName, size = 32.dp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    modifier = Modifier.statusBarPadding()
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigator.push(CreatePostScreen()) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(TablerIcons.Plus, contentDescription = "New post")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                CommunityComposerStrip(
                    userName = currentUserName,
                    onClick = { navigator.push(CreatePostScreen()) }
                )

                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    CommunityFeedTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    tab.label,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                when {
                    state.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    state.error != null -> {
                        CommunityEmptyState(
                            title = "Couldn't load feed",
                            message = state.error ?: "Please try again.",
                            actionLabel = "Retry",
                            onAction = {
                                scope.launch { viewModel.loadCommunityFeed(selectedTab.filter) }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    state.posts.isEmpty() -> {
                        val (title, message, action) = when (selectedTab) {
                            CommunityFeedTab.FOR_YOU -> Triple(
                                "Be the first to share",
                                "Start the community by posting a workout win, progress photo, or motivation.",
                                "Create post" to { navigator.push(CreatePostScreen()) }
                            )
                            CommunityFeedTab.FOLLOWING -> Triple(
                                "No posts from people you follow",
                                "Find people to follow and their updates will show up here.",
                                "Find people" to { navigator.push(FindFriendsScreen()) }
                            )
                            CommunityFeedTab.YOU -> Triple(
                                "You haven't posted yet",
                                "Share your first workout or milestone with the community.",
                                "Create post" to { navigator.push(CreatePostScreen()) }
                            )
                        }
                        CommunityEmptyState(
                            title = title,
                            message = message,
                            actionLabel = action.first,
                            onAction = action.second,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 12.dp,
                                bottom = 88.dp
                            )
                        ) {
                            items(state.posts, key = { it.id }) { post ->
                                CommunityPostCard(
                                    post = post,
                                    isLiked = post.id in state.likedPostIds,
                                    onPostClick = { navigator.push(PostDetailScreen(post.id)) },
                                    onLikeClick = { scope.launch { viewModel.likePost(post.id) } },
                                    onCommentClick = { navigator.push(PostDetailScreen(post.id)) },
                                    onUserClick = { navigator.push(CommunityProfileScreen(post.userId)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
