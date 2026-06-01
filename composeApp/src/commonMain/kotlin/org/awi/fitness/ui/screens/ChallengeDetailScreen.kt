package org.awi.fitness.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Check
import compose.icons.tablericons.Clock
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Share
import compose.icons.tablericons.Star
import compose.icons.tablericons.Trophy
import compose.icons.tablericons.User
import compose.icons.tablericons.X
import org.awi.fitness.model.Challenge
import org.awi.fitness.model.ChallengeDifficulty
import org.awi.fitness.model.ConversationTrigger
import org.awi.fitness.theme.GreenAccent
import org.awi.fitness.ui.components.FitnessButton
import org.awi.fitness.ui.components.LeaderboardCard
import org.awi.fitness.ui.screens.avatar.AvatarScreen
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.viewmodel.ChallengesViewModel
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.ViewModelStore

class ChallengeDetailScreen(
    private val challenge: Challenge
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val userSettings = UserSettings.getInstance()
        val languageViewModel = remember { LanguageViewModel(userSettings.settings) }

        val viewModel = ViewModelStore.challenges
        val state by viewModel.state.collectAsState()

        val currentProgress by remember(state.userProgress, challenge.id, challenge.progress) {
            derivedStateOf {
                state.userProgress[challenge.id]?.currentValue ?: challenge.progress
            }
        }

        val isJoined = challenge.isJoined ||
                state.activeChallenges.any { it.id == challenge.id } ||
                challenge.id in state.joinedChallengeIds

        var showCompletionCelebration by remember { mutableStateOf(false) }

        val progressAnimation by animateFloatAsState(
            targetValue = if (challenge.target > 0) {
                (currentProgress.toFloat() / challenge.target.toFloat()).coerceIn(0f, 1f)
            } else 0f,
            animationSpec = tween(1000, easing = EaseOutCubic),
            label = "progress"
        )

        val pulseAnimation by animateFloatAsState(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        LaunchedEffect(challenge.id) {
            viewModel.selectChallenge(challenge)
            viewModel.loadLeaderboard(challenge.id)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(
                            imageVector = TablerIcons.ArrowLeft,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = languageViewModel.getString(StringKey.CHALLENGE_DETAILS),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            item {
                ChallengeHeroCard(
                    challenge = challenge,
                    currentProgress = currentProgress,
                    progressFraction = progressAnimation,
                    pulseAnimation = pulseAnimation
                )
            }

            if (!isJoined) {
                item {
                    JoinChallengeCard(
                        isLoading = challenge.id in state.joiningChallengeIds,
                        onJoin = { viewModel.joinChallenge(challenge.id) }
                    )
                }
            }

            if (isJoined) {
                item {
                    AutoTrackingCard(challenge = challenge, currentProgress = currentProgress)
                }
            }

            if (showCompletionCelebration ||
                (currentProgress >= challenge.target && challenge.target > 0)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "🎉 Challenge Completed!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            FitnessButton(
                                onClick = {
                                    coroutineScope.launch {
                                        navigator.push(
                                            AvatarScreen(ConversationTrigger.CHALLENGE_COMPLETED)
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(languageViewModel.getString(StringKey.CELEBRATE_WITH_FITNESS_BUDDY))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        navigator.push(
                                            org.awi.fitness.ui.screens.community.CreatePostScreen(
                                                prefilledContent = "🏆 Just completed the \"${challenge.title}\" challenge! ${challenge.target} ${challenge.unit} done. ${challenge.badgeIcon} #TAJLY #FitnessChallenge"
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    contentColor = org.awi.fitness.theme.GreenAccent
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, org.awi.fitness.theme.GreenAccent)
                            ) {
                                Icon(TablerIcons.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Share to Community")
                            }
                        }
                    }
                }
            }

            item { ChallengeInfoSection(challenge = challenge) }

            item { RewardsSection(challenge = challenge) }

            // ---- Leaderboard ----
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Leaderboard",          // TODO: Add to StringKey.kt
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = GreenAccent,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            if (state.leaderboard.isEmpty() && !state.isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏅", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No entries yet — join & be the first!", // TODO: Add to StringKey.kt
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(state.leaderboard) { entry ->
                    LeaderboardCard(entry = entry)
                }
            }

            item { TipsSection() }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ---------------------------------------------------------------------------
// Private composables
// ---------------------------------------------------------------------------

@Composable
private fun ChallengeHeroCard(
    challenge: Challenge,
    currentProgress: Int,
    progressFraction: Float,
    pulseAnimation: Float,
    modifier: Modifier = Modifier
) {
    val color = Color(challenge.color)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(color.copy(alpha = 0.12f), color.copy(alpha = 0.04f))
                    )
                )
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(color = color, shape = CircleShape)
                            .scale(pulseAnimation),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = challenge.iconName.takeIf { it.isNotBlank() } ?: "🏆",
                            fontSize = 32.sp
                        )
                    }

                    DifficultyBadge(difficulty = challenge.difficulty)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = challenge.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$currentProgress ${challenge.unit}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${challenge.target} ${challenge.unit}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Days remaining
                val daysLeft = if (challenge.endDateLong > 0L) {
                    val msLeft = challenge.endDateLong - org.awi.fitness.utils.currentTimeMillis()
                    (msLeft / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                } else -1

                if (daysLeft >= 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            TablerIcons.Clock,
                            contentDescription = null,
                            tint = when {
                                daysLeft == 0 -> Color(0xFFFF5722)
                                daysLeft <= 2 -> Color(0xFFFF9800)
                                else -> color.copy(alpha = 0.8f)
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = when (daysLeft) {
                                0 -> "Ends today!"
                                1 -> "1 day left"
                                else -> "$daysLeft days left"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                daysLeft == 0 -> Color(0xFFFF5722)
                                daysLeft <= 2 -> Color(0xFFFF9800)
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JoinChallengeCard(isLoading: Boolean, onJoin: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Ready to take the challenge?",  // TODO: Add to StringKey.kt
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            FitnessButton(
                onClick = onJoin,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Join Challenge")  // TODO: Add to StringKey.kt
                }
            }
        }
    }
}

@Composable
private fun AutoTrackingCard(challenge: org.awi.fitness.model.Challenge, currentProgress: Int) {
    val (triggerEmoji, triggerText) = when (challenge.targetType) {
        org.awi.fitness.model.ChallengeTargetType.WORKOUTS ->
            "🏋️" to "Progress updates automatically when you complete a workout day in the Train tab."
        org.awi.fitness.model.ChallengeTargetType.MEALS ->
            "🥗" to "Progress updates automatically when you mark a meal as eaten in the Meals tab."
        org.awi.fitness.model.ChallengeTargetType.POSTS ->
            "✍️" to "Progress updates automatically when you create a post in the Community tab."
        org.awi.fitness.model.ChallengeTargetType.STREAK ->
            "🔥" to "Progress updates automatically each day you complete a workout."
        org.awi.fitness.model.ChallengeTargetType.CALORIES ->
            "🔥" to "Progress updates automatically based on your daily calorie tracking."
        org.awi.fitness.model.ChallengeTargetType.STEPS ->
            "👟" to "Progress updates automatically from your step tracking."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(triggerEmoji, style = MaterialTheme.typography.titleLarge)
                Text(
                    "Auto-tracked",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                triggerText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Current progress",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    "$currentProgress / ${challenge.target} ${challenge.unit}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = org.awi.fitness.theme.GreenAccent
                )
            }
        }
    }
}

@Composable
private fun ChallengeInfoSection(challenge: Challenge, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Challenge Info",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow(
                icon = TablerIcons.Check,
                title = "Type",
                value = challenge.type.name.lowercase().replaceFirstChar { it.uppercase() }
            )
            InfoRow(
                icon = TablerIcons.Trophy,
                title = "Target",
                value = "${challenge.target} ${challenge.unit}"
            )
            InfoRow(
                icon = TablerIcons.User,
                title = "Participants",
                value = challenge.participantIds.size.toString()
            )
            if (challenge.createdBy.isNotBlank()) {
                InfoRow(
                    icon = TablerIcons.Star,
                    title = "Created by",
                    value = if (challenge.createdBy == "system") "TAJLY" else challenge.createdBy
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RewardsSection(challenge: Challenge, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Rewards",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            val xp = challenge.reward.xp.takeIf { it > 0 } ?: challenge.xpReward
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    TablerIcons.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+$xp XP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (challenge.reward.title.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        TablerIcons.Trophy,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = challenge.reward.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun TipsSection(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "💡 Tips",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            listOf(
                "Stay consistent with your daily progress",
                "Take breaks when needed to avoid burnout",
                "Celebrate small wins along the way",
                "Track your progress to stay motivated"
            ).forEach { tip ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DifficultyBadge(difficulty: ChallengeDifficulty, modifier: Modifier = Modifier) {
    val (color, text) = when (difficulty) {
        ChallengeDifficulty.BEGINNER -> Color(0xFF4CAF50) to "Easy"
        ChallengeDifficulty.INTERMEDIATE -> Color(0xFFFF9800) to "Medium"
        ChallengeDifficulty.ADVANCED -> Color(0xFFF44336) to "Hard"
    }

    Box(
        modifier = modifier
            .background(color = color.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
