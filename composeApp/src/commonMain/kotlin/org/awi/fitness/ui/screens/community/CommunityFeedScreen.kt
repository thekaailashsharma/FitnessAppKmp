package org.awi.fitness.ui.screens.community

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.zIndex
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.bg_gym_dark
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.Bell
import compose.icons.tablericons.InfoCircle
import compose.icons.tablericons.Users
import kotlinx.coroutines.launch
import org.awi.fitness.data.UserSettings
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.EmptyState
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.LottieAnim
import org.awi.fitness.ui.components.ProvideGlass
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
        val findFriendsState by viewModel.findFriendsState.collectAsState()
        val scope = rememberCoroutineScope()
        val userSettings = UserSettings.getInstance()
        val currentUserEmail = userSettings.userEmail.orEmpty()
        val currentUserName = userSettings.userName?.takeIf { it.isNotBlank() }
            ?: currentUserEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
        val refreshTick by CommunityEvents.feedRefreshTick.collectAsState()
        val c = TajlyTheme.colors

        var selectedTab by remember { mutableStateOf(CommunityFeedTab.FOR_YOU) }

        LaunchedEffect(selectedTab, refreshTick) {
            viewModel.loadCommunityFeed(selectedTab.filter)
        }

        LaunchedEffect(Unit) {
            viewModel.loadActivityNotifications()
            // Real "people to follow" suggestions that power the members rail + empty state.
            viewModel.loadSuggestedUsers()
        }

        val pullState = rememberPullToRefreshState()

        // People-to-follow suggestions from the real suggestion engine (getSuggestedUsers).
        // Prioritise members the user does NOT already follow so the rail always feels useful.
        val suggestedMembers = remember(findFriendsState.suggestedUsers) {
            val all = findFriendsState.suggestedUsers.filter { it.id.isNotBlank() }
            (all.filter { !it.isFollowing } + all.filter { it.isFollowing }).take(12)
        }

        // Real member avatars for the constellation intro — derived from the loaded feed
        // (no fabricated identities). Blank/null profile images are dropped.
        val introAvatarUrls = remember(state.posts) {
            state.posts.mapNotNull { it.userProfileImage?.takeIf { url -> url.isNotBlank() } }
                .distinct()
                .take(8)
        }

        // First community visit → show the constellation intro once.
        LaunchedEffect(Unit) {
            if (!userSettings.communityIntroSeen) {
                userSettings.communityIntroSeen = true
                navigator.push(CommunityIntroScreen(introAvatarUrls))
            }
        }

        ProvideGlass {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(c.bg),
            ) {
                // Report/block confirmation toast (auto-dismisses).
                state.actionMessage?.let { msg ->
                    LaunchedEffect(msg) {
                        kotlinx.coroutines.delay(2600)
                        viewModel.clearActionMessage()
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .zIndex(10f)
                            .navigationBarsPadding()
                            .padding(start = 24.dp, end = 24.dp, bottom = 96.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(c.s1)
                            .border(1.dp, c.hairStrong, RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text(msg, style = MaterialTheme.typography.bodyMedium, color = c.textHi)
                    }
                }
                PullToRefreshBox(
                    isRefreshing = state.isLoading && state.posts.isNotEmpty(),
                    onRefresh = { scope.launch { viewModel.loadCommunityFeed(selectedTab.filter) } },
                    state = pullState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 120.dp),
                    ) {
                        // Hero + composer + tabs are the first list items, so the photo backdrop
                        //    scrolls away as the user scrolls up through the feed.
                        item(key = "hero") {
                            CommunityHero(
                                unreadCount = activityState.unreadCount,
                                currentUserName = currentUserName,
                                onIntro = { navigator.push(CommunityIntroScreen(introAvatarUrls)) },
                                onFindPeople = { navigator.push(FindFriendsScreen()) },
                                onActivity = { navigator.push(ActivityScreen()) },
                                onProfile = {
                                    if (currentUserEmail.isNotBlank()) {
                                        navigator.push(CommunityProfileScreen(currentUserEmail))
                                    }
                                },
                            )
                        }
                        item(key = "composer") {
                            CommunityComposerStrip(
                                userName = currentUserName,
                                onClick = { navigator.push(CreatePostScreen()) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                        item(key = "tabs") {
                            CommunityTabRow(
                                selectedTab = selectedTab,
                                onSelect = { selectedTab = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
                            )
                        }

                        when {
                            state.isLoading && state.posts.isEmpty() -> {
                                item(key = "loading") {
                                    Box(
                                        Modifier.fillMaxWidth().fillParentMaxHeight(0.7f),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(color = GoldPrimary)
                                    }
                                }
                            }

                            state.error != null -> {
                                item(key = "error") {
                                    Box(Modifier.fillParentMaxHeight(0.7f)) {
                                        CommunityErrorState(
                                            topInset = 0.dp,
                                            message = state.error ?: "Please try again.",
                                            onRetry = {
                                                scope.launch { viewModel.loadCommunityFeed(selectedTab.filter) }
                                            },
                                        )
                                    }
                                }
                            }

                            state.posts.isEmpty() -> {
                                item(key = "empty") {
                                    Box(Modifier.fillParentMaxHeight(0.85f)) {
                                        CommunityEmptyState(
                                            tab = selectedTab,
                                            emptyMessage = state.emptyMessage,
                                            topInset = 0.dp,
                                            suggestedMembers = suggestedMembers,
                                            onCreate = { navigator.push(CreatePostScreen()) },
                                            onFindPeople = { navigator.push(FindFriendsScreen()) },
                                            onOpenProfile = { userId ->
                                                navigator.push(CommunityProfileScreen(userId))
                                            },
                                            onFollow = { userId ->
                                                scope.launch { viewModel.followUser(userId) }
                                            },
                                        )
                                    }
                                }
                            }

                            else -> {
                                if (suggestedMembers.isNotEmpty()) {
                                    item(key = "suggested_rail") {
                                        SuggestedMembersRail(
                                            users = suggestedMembers,
                                            onOpenProfile = { userId ->
                                                navigator.push(CommunityProfileScreen(userId))
                                            },
                                            onFollow = { userId ->
                                                scope.launch { viewModel.followUser(userId) }
                                            },
                                        )
                                    }
                                }
                                items(state.posts, key = { it.id }) { post ->
                                    val myEmail = org.awi.fitness.data.UserSettings.getInstance().userEmail
                                    CommunityPostCard(
                                        post = post,
                                        isLiked = post.id in state.likedPostIds,
                                        isOwnPost = post.userId == myEmail,
                                        onPostClick = { navigator.push(PostDetailScreen(post.id)) },
                                        onLikeClick = { scope.launch { viewModel.likePost(post.id) } },
                                        onCommentClick = { navigator.push(PostDetailScreen(post.id)) },
                                        onUserClick = { navigator.push(CommunityProfileScreen(post.userId)) },
                                        onReport = { scope.launch { viewModel.reportPost(post.id, post.userId) } },
                                        onBlock = { viewModel.blockUser(post.userId) },
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Fitness-photo hero header: a real gym backdrop under a dark scrim, with the app title +
 * action icons up top and a motivational headline + subline at the bottom. This is the fresh,
 * energetic "community" feel — the icons stay wired to exactly the same destinations.
 */
@Composable
private fun CommunityHero(
    unreadCount: Int,
    currentUserName: String,
    onIntro: () -> Unit,
    onFindPeople: () -> Unit,
    onActivity: () -> Unit,
    onProfile: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        Image(
            painter = painterResource(Res.drawable.bg_gym_dark),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Dark scrim so white copy + icons stay legible over any photo.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.10f),
                            Color.Black.copy(alpha = 0.82f),
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // ---- Top row: label + action icons over the photo ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "TAJLY COMMUNITY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.weight(1f),
                )
                HeroIconButton(onClick = onIntro) {
                    Icon(TablerIcons.InfoCircle, "About the community", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                HeroIconButton(onClick = onFindPeople) {
                    Icon(TablerIcons.Users, "Find people", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                Box {
                    HeroIconButton(onClick = onActivity) {
                        Icon(TablerIcons.Bell, "Activity", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Tajly.GoldGradient),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${unreadCount.coerceAtMost(9)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnGold,
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                CommunityAvatar(
                    name = currentUserName,
                    imageUrl = org.awi.fitness.data.UserSettings.getInstance().profilePhotoUrl,
                    size = 38.dp,
                    onClick = onProfile,
                )
            }

            Spacer(Modifier.weight(1f))

            // ---- Motivational copy (look only, not a feature) ----
            Text(
                text = "Your crew",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Show up. Cheer each other on. Grow together.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.82f),
            )
        }
    }
}

/** Compact translucent icon button sitting on top of the hero photo. */
@Composable
private fun HeroIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .pressScale(interaction)
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.16f))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

/** Compact segmented control (For You / Following / You) — small, modern, gold active pill. */
@Composable
private fun CommunityTabRow(
    selectedTab: CommunityFeedTab,
    onSelect: (CommunityFeedTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TajlyTheme.colors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(c.s1)
            .border(1.dp, c.hair, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CommunityFeedTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            val interaction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .then(if (selected) Modifier.background(Tajly.GoldGradient) else Modifier)
                    .clickable(interactionSource = interaction, indication = null) { onSelect(tab) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) OnGold else c.textMid,
                )
            }
        }
    }
}

/** Per-tab copy + hero animation + glow for the redesigned empty state. */
private data class EmptyStateSpec(
    val lottie: String,
    val glow: Color,
    val title: String,
    val message: String,
    val primaryLabel: String,
    val primaryAction: () -> Unit,
    val secondaryLabel: String?,
    val secondaryAction: (() -> Unit)?,
)

private fun emptyStateFor(
    tab: CommunityFeedTab,
    emptyMessage: String?,
    onCreate: () -> Unit,
    onFindPeople: () -> Unit,
): EmptyStateSpec = when (tab) {
    CommunityFeedTab.FOR_YOU -> EmptyStateSpec(
        lottie = "lottie_running.json",
        glow = Tajly.Teal,
        title = "Your community starts here",
        message = "Share a workout win, a progress photo, or a note that keeps someone going. Or find people to follow.",
        primaryLabel = "Share your first post",
        primaryAction = onCreate,
        secondaryLabel = "Find people to follow",
        secondaryAction = onFindPeople,
    )
    CommunityFeedTab.FOLLOWING -> EmptyStateSpec(
        lottie = "lottie_heart.json",
        glow = Tajly.Pink,
        title = "Fill your feed with people you follow",
        message = emptyMessage ?: "Follow members and their workouts, wins and milestones will show up right here.",
        primaryLabel = "Find people to follow",
        primaryAction = onFindPeople,
        secondaryLabel = "Share a post",
        secondaryAction = onCreate,
    )
    CommunityFeedTab.YOU -> EmptyStateSpec(
        lottie = "lottie_trophy.json",
        glow = Tajly.Violet,
        title = "You haven't posted yet",
        message = "Share your first workout or milestone — the community loves to cheer new wins on.",
        primaryLabel = "Create your first post",
        primaryAction = onCreate,
        secondaryLabel = null,
        secondaryAction = null,
    )
}

/**
 * Redesigned empty state — a single glowing Lottie hero, warm copy, a gold primary CTA and a
 * calm secondary action, then (when we have them) real suggested members to follow so an empty
 * feed still feels alive and inviting. Scrolls so pull-to-refresh keeps working.
 */
@Composable
private fun CommunityEmptyState(
    tab: CommunityFeedTab,
    emptyMessage: String?,
    topInset: androidx.compose.ui.unit.Dp,
    suggestedMembers: List<org.awi.fitness.model.CommunityUser>,
    onCreate: () -> Unit,
    onFindPeople: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onFollow: (String) -> Unit,
) {
    val c = TajlyTheme.colors
    val spec = emptyStateFor(tab, emptyMessage, onCreate, onFindPeople)
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(top = topInset + 24.dp, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Glowing hero animation.
        Box(
            modifier = Modifier
                .size(168.dp)
                .clip(CircleShape)
                .background(Tajly.sectionGlow(spec.glow)),
            contentAlignment = Alignment.Center,
        ) {
            LottieAnim(spec.lottie, Modifier.size(120.dp))
        }
        Spacer(Modifier.height(22.dp))
        Text(
            text = spec.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = c.textHi,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = spec.message,
            style = MaterialTheme.typography.bodyMedium,
            color = c.textMid,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 36.dp),
        )
        Spacer(Modifier.height(24.dp))
        GoldButton(
            text = spec.primaryLabel,
            onClick = spec.primaryAction,
            modifier = Modifier.padding(horizontal = 40.dp).fillMaxWidth(),
        )
        if (spec.secondaryLabel != null && spec.secondaryAction != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = spec.secondaryLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = GoldPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = spec.secondaryAction)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        // Real people-to-follow, so a brand-new feed still feels like a community.
        if (suggestedMembers.isNotEmpty()) {
            Spacer(Modifier.height(36.dp))
            SuggestedMembersRail(
                users = suggestedMembers,
                onOpenProfile = onOpenProfile,
                onFollow = onFollow,
                title = "People to follow",
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

/** Graceful error state — a hero animation, the message, and a real retry. */
@Composable
private fun CommunityErrorState(
    topInset: androidx.compose.ui.unit.Dp,
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(top = topInset),
        contentAlignment = Alignment.Center,
    ) {
        EmptyState(
            title = "Couldn't load the feed",
            subtitle = message,
            icon = {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Tajly.sectionGlow(Tajly.Coral)),
                    contentAlignment = Alignment.Center,
                ) {
                    LottieAnim("lottie_empty_box.json", Modifier.size(96.dp))
                }
            },
            cta = { GoldButton(text = "Retry", onClick = onRetry) },
        )
    }
}
