package org.awi.fitness.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.Star
import compose.icons.tablericons.Trophy
import compose.icons.tablericons.User
import org.awi.fitness.model.*
import org.awi.fitness.theme.GreenAccent
import org.awi.fitness.ui.components.*
import org.awi.fitness.viewmodel.ViewModelStore

@Composable
fun ChallengesScreen() {
    val navigator = LocalNavigator.currentOrThrow
    val viewModel = ViewModelStore.challenges
    val state by viewModel.state.collectAsState()
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

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                HeroSection(firstActiveChallenge = state.activeChallenges.firstOrNull())
            }

            item {
                QuickStatsSection(
                    activeChallengesCount = state.activeChallenges.size,
                    completedCount = state.activeChallenges.count {
                        it.progress >= it.target && it.target > 0
                    },
                    rank = userRank
                )
            }

            // ---- My Active Challenges ----
            item {
                SectionHeader(
                    title = "Active Challenges",         // TODO: Add to StringKey.kt
                    subtitle = "Keep the momentum going!"
                )
            }

            if (state.activeChallenges.isEmpty() && !state.isLoading) {
                item {
                    ChallengesEmptyState(
                        message = "No active challenges yet.\nJoin one below to get started!",
                        icon = "🏁"
                    )
                }
            } else {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(state.activeChallenges) { challenge ->
                            ChallengeCard(
                                challenge = challenge,
                                onClick = {
                                    viewModel.selectChallenge(challenge)
                                    navigator.push(ChallengeDetailScreen(challenge))
                                },
                                modifier = Modifier.width(280.dp)
                            )
                        }
                    }
                }
            }

            // ---- Available Challenges ----
            item {
                SectionHeader(
                    title = "Available Challenges",     // TODO: Add to StringKey.kt
                    subtitle = "Start a new journey"
                )
            }

            if (state.availableChallenges.isEmpty() && !state.isLoading) {
                item {
                    ChallengesEmptyState(
                        message = "No available challenges right now.\nCheck back soon!",
                        icon = "✨"
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

            // ---- Recent Achievements (static badges — achievements not in this sprint's scope) ----
            item {
                SectionHeader(
                    title = "Recent Achievements",
                    subtitle = "Celebrate your wins!"
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(staticBadges()) { badge ->
                        val isUnlocked = when (badge.id) {
                            "1" -> state.activeChallenges.isNotEmpty() || state.joinedChallengeIds.isNotEmpty()
                            "2" -> state.activeChallenges.count { it.progress >= it.target && it.target > 0 } >= 1
                            "3" -> state.activeChallenges.size >= 2
                            else -> false
                        }
                        BadgeCard(
                            badge = badge,
                            isUnlocked = isUnlocked,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }
            }

            // ---- Leaderboard (for first active challenge) ----
            item {
                SectionHeader(
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
                        icon = "🏅"
                    )
                }
            } else {
                items(state.leaderboard) { entry ->
                    LeaderboardCard(entry = entry)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Loading overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenAccent)
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
                        Text("Dismiss", color = GreenAccent)
                    }
                }
            ) {
                Text(errorMsg)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Private composables
// ---------------------------------------------------------------------------

@Composable
private fun HeroSection(firstActiveChallenge: Challenge?) {
    val pulseAnimation by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val title = firstActiveChallenge?.title ?: "Daily Challenge"
    val subtitle = firstActiveChallenge?.description ?: "Join a challenge to get started"
    val progressText = if (firstActiveChallenge != null && firstActiveChallenge.target > 0) {
        "${firstActiveChallenge.progress}/${firstActiveChallenge.target} ${firstActiveChallenge.unit}"
    } else {
        "Start your journey!"
    }
    val accentColor = if (firstActiveChallenge != null) Color(firstActiveChallenge.color) else GreenAccent

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.12f),
                            accentColor.copy(alpha = 0.04f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = progressText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseAnimation)
                        .background(color = accentColor.copy(alpha = 0.2f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = firstActiveChallenge?.iconName?.takeIf { it.isNotBlank() } ?: "🏆",
                        fontSize = 36.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsSection(activeChallengesCount: Int, completedCount: Int, rank: String = "—") {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Active",
            value = activeChallengesCount.toString(),
            icon = TablerIcons.Trophy,
            color = GreenAccent,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Completed",
            value = completedCount.toString(),
            icon = TablerIcons.Check,
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Rank",
            value = rank,
            icon = TablerIcons.Star,
            color = Color(0xFFFFD700),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onTap
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(challenge.color).copy(alpha = 0.08f),
                            Color(challenge.color).copy(alpha = 0.02f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(challenge.color).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = challenge.iconName.takeIf { it.isNotBlank() } ?: "🏆",
                        fontSize = 22.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = challenge.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TypeBadge(type = challenge.type)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "⭐", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "+${challenge.reward.xp} XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                Button(
                    onClick = { if (!isJoining && !isJoined) onJoin() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isJoined) Color(0xFF4CAF50) else GreenAccent,
                        disabledContainerColor = if (isJoined) Color(0xFF4CAF50) else GreenAccent.copy(alpha = 0.5f)
                    ),
                    enabled = !isJoining && !isJoined,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    when {
                        isJoining -> CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        isJoined -> Icon(
                            TablerIcons.Check,
                            contentDescription = "Joined",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        else -> Text("Join", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeBadge(type: ChallengeType) {
    val (color, label) = when (type) {
        ChallengeType.DAILY -> Color(0xFF4CAF50) to "Daily"
        ChallengeType.WEEKLY -> Color(0xFF2196F3) to "Weekly"
        ChallengeType.MONTHLY -> Color(0xFF9C27B0) to "Monthly"
        ChallengeType.CUSTOM -> Color(0xFFFF9800) to "Custom"
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ChallengesEmptyState(message: String, icon: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 40.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Static badges (achievements not in this sprint's Firestore scope)
// ---------------------------------------------------------------------------

private fun staticBadges(): List<Badge> = listOf(
    Badge(id = "1", name = "First Steps", description = "Complete your first challenge", iconName = "👟", rarity = BadgeRarity.COMMON),
    Badge(id = "2", name = "Week Warrior", description = "Complete 7 challenges in a week", iconName = "⚔️", rarity = BadgeRarity.RARE),
    Badge(id = "3", name = "Consistency King", description = "30-day streak", iconName = "👑", rarity = BadgeRarity.EPIC)
)
