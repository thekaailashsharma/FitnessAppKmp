package org.awi.fitness.ui.screens

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.Award
import compose.icons.tablericons.CircleCheck
import compose.icons.tablericons.Trophy
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.card_community
import fitnessappkmp.composeapp.generated.resources.card_compete
import fitnessappkmp.composeapp.generated.resources.card_move
import fitnessappkmp.composeapp.generated.resources.ic3d_flag
import fitnessappkmp.composeapp.generated.resources.ic3d_medal
import fitnessappkmp.composeapp.generated.resources.ic3d_star
import org.awi.fitness.model.*
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.*
import org.awi.fitness.viewmodel.ViewModelStore
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

@Composable
fun ChallengesScreen() {
    val navigator = LocalNavigator.currentOrThrow
    val viewModel = ViewModelStore.challenges
    val state by viewModel.state.collectAsState()
    val c = TajlyTheme.colors
    val userSettings = org.awi.fitness.data.UserSettings.getInstance()
    val currentEmail = userSettings.userEmail.orEmpty()

    LaunchedEffect(Unit) {
        viewModel.loadChallenges()
    }

    // Derive user's rank from the first active challenge's leaderboard
    val userRank = remember(state.leaderboard, currentEmail) {
        val idx = state.leaderboard.indexOfFirst { it.userId == currentEmail || it.username == currentEmail.substringBefore("@") }
        if (idx >= 0) "#${idx + 1}" else if (state.leaderboard.isNotEmpty()) "#${state.leaderboard.size + 1}" else "—"
    }

    ProvideGlass {
    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        // Aurora base — warm gold + violet glows on the canvas the glass refracts.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .glassSource()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(280.dp)
                    .blur(90.dp)
                    .background(Brush.radialGradient(listOf(GoldPrimary.copy(alpha = 0.16f), Color.Transparent))),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(240.dp)
                    .blur(90.dp)
                    .background(Brush.radialGradient(listOf(Tajly.Violet.copy(alpha = 0.12f), Color.Transparent))),
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Challenges",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi,
                    )
                    Text(
                        text = "Compete, progress, win.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textMid,
                    )
                }
            }

            item {
                QuickStatsRow(
                    activeChallengesCount = state.activeChallenges.size,
                    completedCount = state.activeChallenges.count {
                        it.progress >= it.target && it.target > 0
                    },
                    rank = userRank
                )
            }

            // ---- My Active Challenges ----
            item {
                ChallengeSectionHeader(
                    title = "Active Challenges",
                    subtitle = "Keep the momentum going!"
                )
            }

            if (state.activeChallenges.isEmpty() && !state.isLoading) {
                item {
                    ChallengesEmptyState(
                        message = "No active challenges yet.\nJoin one below to get started!",
                        icon = Res.drawable.ic3d_flag
                    )
                }
            } else {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        itemsIndexed(state.activeChallenges) { index, challenge ->
                            val pr = state.userProgress[challenge.id]
                            val frac = when {
                                pr != null && pr.targetValue > 0 -> pr.currentValue.toFloat() / pr.targetValue
                                challenge.target > 0 -> challenge.progress.toFloat() / challenge.target
                                else -> 0f
                            }.coerceIn(0f, 1f)
                            ActiveChallengeImageCard(
                                title = challenge.title,
                                fraction = frac,
                                imageUrl = challenge.imageUrl,
                                fallback = challengeFallback(index),
                                onClick = {
                                    viewModel.selectChallenge(challenge)
                                    navigator.push(ChallengeDetailScreen(challenge))
                                },
                                modifier = Modifier.width(268.dp)
                            )
                        }
                    }
                }
            }

            // ---- Available Challenges ----
            item {
                ChallengeSectionHeader(
                    title = "Available Challenges",
                    subtitle = "Start a new journey"
                )
            }

            if (state.availableChallenges.isEmpty() && !state.isLoading) {
                item {
                    ChallengesEmptyState(
                        message = "No available challenges right now.\nCheck back soon!",
                        icon = Res.drawable.ic3d_star
                    )
                }
            } else {
                items(state.availableChallenges) { challenge ->
                    AvailableChallengeRow(
                        challenge = challenge,
                        isJoining = challenge.id in state.joiningChallengeIds,
                        isJoined = challenge.id in state.joinedChallengeIds,
                        onTap = {
                            viewModel.selectChallenge(challenge)
                            navigator.push(ChallengeDetailScreen(challenge))
                        },
                        onJoin = { viewModel.joinChallenge(challenge.id) }
                    )
                }
            }

            // ---- Leaderboard (honest: reflects the first active challenge) ----
            item {
                ChallengeSectionHeader(
                    title = "Leaderboard",
                    subtitle = if (state.activeChallenges.isNotEmpty())
                        "Challenge: ${state.activeChallenges.first().title}"
                    else
                        "See how you rank"
                )
            }

            if (state.leaderboard.isEmpty() && !state.isLoading) {
                item {
                    ChallengesEmptyState(
                        message = if (state.activeChallenges.isEmpty())
                            "Join a challenge to appear on the leaderboard!"
                        else
                            "No entries yet. Be the first!",
                        icon = Res.drawable.ic3d_medal
                    )
                }
            } else {
                if (state.leaderboard.size >= 3) {
                    item {
                        LeaderboardPodium(
                            entries = state.leaderboard,
                            onUserClick = { entry ->
                                navigator.push(
                                    org.awi.fitness.ui.screens.community.CommunityProfileScreen(entry.userId)
                                )
                            },
                        )
                    }
                }
                item {
                    val maxXp = state.leaderboard.maxOfOrNull { it.xp }?.coerceAtLeast(1) ?: 1
                    LeaderboardBars(
                        entries = state.leaderboard,
                        maxXp = maxXp,
                        onUserClick = { entry ->
                            navigator.push(
                                org.awi.fitness.ui.screens.community.CommunityProfileScreen(entry.userId)
                            )
                        },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Loading overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(c.bg.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GoldPrimary)
            }
        }

        // Error snackbar
        state.error?.let { errorMsg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss", color = GoldBright)
                    }
                }
            ) {
                Text(errorMsg)
            }
        }
    }
    }
}

// ---------------------------------------------------------------------------
// Private composables
// ---------------------------------------------------------------------------

/** Rotating bundled backdrop for active challenges lacking a remote image. */
private fun challengeFallback(index: Int): DrawableResource =
    listOf(Res.drawable.card_compete, Res.drawable.card_community, Res.drawable.card_move)[index % 3]

@Composable
private fun QuickStatsRow(activeChallengesCount: Int, completedCount: Int, rank: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatTile(
            icon = TablerIcons.Trophy,
            value = activeChallengesCount.toString(),
            label = "Active",
            accent = Tajly.Violet,
            modifier = Modifier.weight(1f)
        )
        QuickStatTile(
            icon = TablerIcons.CircleCheck,
            value = completedCount.toString(),
            label = "Completed",
            accent = Tajly.Green,
            modifier = Modifier.weight(1f)
        )
        QuickStatTile(
            icon = TablerIcons.Award,
            value = rank,
            label = "Your rank",
            accent = GoldPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickStatTile(
    icon: ImageVector,
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val c = TajlyTheme.colors
    GlassCard(modifier = modifier, shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = c.textHi,
                maxLines = 1,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = c.textMid,
                maxLines = 1,
            )
        }
    }
}

/** Full-bleed image-backdrop card for an active challenge: dark scrim + title + gold progress. */
@Composable
private fun ActiveChallengeImageCard(
    title: String,
    fraction: Float,
    imageUrl: String?,
    fallback: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(22.dp)

    // Gold bar fills once on appear.
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val fill by animateFloatAsState(
        targetValue = if (shown) fraction else 0f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "barFill",
    )
    val pct = (fraction * 100).roundToInt()

    Box(
        modifier = modifier
            .height(158.dp)
            .pressScale(interaction)
            .clip(shape)
            .clickable(interactionSource = interaction, indication = null) { onClick() },
    ) {
        if (!imageUrl.isNullOrBlank()) {
            ImagePlaceholder(
                url = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Image(
                painter = painterResource(fallback),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0.2f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.84f),
                ),
            ),
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$pct% complete",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fill)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(Tajly.GoldGradient),
                )
            }
        }
    }
}

@Composable
private fun ChallengeSectionHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    val c = TajlyTheme.colors
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = c.textHi
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = c.textMid
        )
    }
}

@Composable
private fun AvailableChallengeRow(
    challenge: Challenge,
    isJoining: Boolean,
    isJoined: Boolean,
    onTap: () -> Unit,
    onJoin: () -> Unit
) {
    val c = TajlyTheme.colors
    val accent = challengeAccent(challenge)
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.glassFill, shape)
            .border(1.dp, c.hairStrong, shape)
            .clickableNoRipple(onTap),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(110.dp)
                .blur(40.dp)
                .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.28f), Color.Transparent))),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Challenge3dBadge(challenge = challenge, diameter = 52, iconSize = 40)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi,
                    maxLines = 1
                )
                Text(
                    text = challenge.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid,
                    maxLines = 2
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeChip(type = challenge.type)
                    XpPill(xp = challenge.reward.xp.takeIf { it > 0 } ?: challenge.xpReward)
                }
            }

            // Gold Join CTA (preserves join / joining / joined states)
            JoinPill(
                isJoining = isJoining,
                isJoined = isJoined,
                onJoin = { if (!isJoining && !isJoined) onJoin() },
            )
        }
    }
}

@Composable
private fun JoinPill(
    isJoining: Boolean,
    isJoined: Boolean,
    onJoin: () -> Unit,
) {
    val c = TajlyTheme.colors
    when {
        isJoined -> Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Tajly.Green.copy(alpha = 0.18f))
                .border(1.dp, Tajly.Green.copy(alpha = 0.4f), CircleShape)
                .padding(horizontal = 14.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = TablerIcons.CircleCheck,
                    contentDescription = "Joined",
                    tint = Tajly.Green,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text("Joined", color = Tajly.Green, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
        }
        isJoining -> Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(c.glassFill, CircleShape)
                .border(1.dp, c.hairStrong, CircleShape)
                .padding(horizontal = 18.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = GoldBright,
            )
        }
        else -> GoldButton(
            text = "Join",
            onClick = onJoin,
        )
    }
}

@Composable
private fun TypeChip(type: ChallengeType) {
    val (color, label) = when (type) {
        ChallengeType.DAILY -> Tajly.Green to "Daily"
        ChallengeType.WEEKLY -> Tajly.Blue to "Weekly"
        ChallengeType.MONTHLY -> Tajly.Violet to "Monthly"
        ChallengeType.CUSTOM -> Color(0xFFFF9800) to "Custom"
    }
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---------------------------------------------------------------------------
// Leaderboard — slim gold bars (magnitude = entry.xp, which holds currentValue)
// ---------------------------------------------------------------------------

@Composable
private fun LeaderboardBars(
    entries: List<LeaderboardEntry>,
    maxXp: Int,
    onUserClick: (LeaderboardEntry) -> Unit,
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            entries.forEach { entry ->
                LeaderboardBarRow(
                    entry = entry,
                    fraction = (entry.xp.toFloat() / maxXp).coerceIn(0f, 1f),
                    onClick = { onUserClick(entry) },
                )
            }
        }
    }
}

@Composable
private fun LeaderboardBarRow(
    entry: LeaderboardEntry,
    fraction: Float,
    onClick: () -> Unit,
) {
    val c = TajlyTheme.colors
    val isPodium = entry.rank in 1..3

    // Bar fills once on appear.
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val fill by animateFloatAsState(
        targetValue = if (shown) fraction else 0f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "lbFill",
    )

    Row(
        modifier = Modifier.fillMaxWidth().clickableNoRipple(onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "#${entry.rank}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isPodium) GoldBright else c.textMid,
            modifier = Modifier.width(30.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textHi,
                    fontWeight = if (isPodium) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = entry.xp.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isPodium) GoldBright else c.textHi,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(c.hairStrong),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fill)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(Tajly.GoldGradient),
                )
            }
        }
    }
}

@Composable
private fun ChallengesEmptyState(message: String, icon: DrawableResource) {
    val parts = message.split("\n", limit = 2)
    EmptyState(
        title = parts.firstOrNull().orEmpty(),
        subtitle = parts.getOrNull(1).orEmpty(),
        modifier = Modifier.fillMaxWidth(),
        icon = {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
            )
        },
    )
}

// Small ripple-free clickable helper (keeps the glass surface clean).
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = this.then(
    Modifier.composed {
        val interaction = remember { MutableInteractionSource() }
        Modifier.clickable(
            interactionSource = interaction,
            indication = null,
        ) { onClick() }
    }
)
