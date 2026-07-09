package org.awi.fitness.ui.screens
import org.awi.fitness.viewmodel.LocalLanguageViewModel

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Share
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.bg_success
import fitnessappkmp.composeapp.generated.resources.card_compete
import fitnessappkmp.composeapp.generated.resources.ic3d_chart
import fitnessappkmp.composeapp.generated.resources.ic3d_crown
import fitnessappkmp.composeapp.generated.resources.ic3d_flash_lightning
import fitnessappkmp.composeapp.generated.resources.ic3d_medal
import fitnessappkmp.composeapp.generated.resources.ic3d_target
import fitnessappkmp.composeapp.generated.resources.ic3d_tick_check
import fitnessappkmp.composeapp.generated.resources.ic3d_thumb_up
import fitnessappkmp.composeapp.generated.resources.ic3d_trophy
import org.awi.fitness.model.Challenge
import org.awi.fitness.model.ConversationTrigger
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.ui.components.ChallengeDifficultyChip
import org.awi.fitness.ui.components.CountdownChip
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.ui.components.LeaderboardCard
import org.awi.fitness.ui.components.LottieAnim
import org.awi.fitness.ui.components.LeaderboardPodium
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.StatRing
import org.awi.fitness.ui.components.glassSource
import org.awi.fitness.ui.components.liquidGlass
import org.awi.fitness.ui.components.challenge3dIcon
import org.awi.fitness.ui.components.challengeDaysLeft
import org.awi.fitness.ui.screens.avatar.AvatarScreen
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.ViewModelStore
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

class ChallengeDetailScreen(
    private val challenge: Challenge
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val userSettings = UserSettings.getInstance()
        val languageViewModel = remember { LanguageViewModel(userSettings.settings) }
        val c = TajlyTheme.colors

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

        // Progress-ring sweep on appear.
        val progressAnimation by animateFloatAsState(
            targetValue = if (challenge.target > 0) {
                (currentProgress.toFloat() / challenge.target.toFloat()).coerceIn(0f, 1f)
            } else 0f,
            animationSpec = tween(1000, easing = EaseOutCubic),
            label = "progress"
        )

        LaunchedEffect(challenge.id) {
            viewModel.selectChallenge(challenge)
            viewModel.loadLeaderboard(challenge.id)
        }

        val isComplete = showCompletionCelebration ||
                (currentProgress >= challenge.target && challenge.target > 0)

        ProvideGlass {
        Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        // Backdrop the liquid-glass cards refract.
        Box(modifier = Modifier.fillMaxSize().glassSource()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(260.dp)
                    .blur(80.dp)
                    .background(Brush.radialGradient(listOf(GoldPrimary.copy(alpha = 0.14f), Color.Transparent))),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(220.dp)
                    .blur(80.dp)
                    .background(Brush.radialGradient(listOf(GoldBright.copy(alpha = 0.08f), Color.Transparent))),
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
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
                            tint = c.textHi
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = languageViewModel.getString(StringKey.CHALLENGE_DETAILS),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi
                    )
                }
            }

            item {
                ChallengeHeroCard(
                    challenge = challenge,
                    currentProgress = currentProgress,
                    progressFraction = progressAnimation,
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

            if (isComplete) {
                item {
                    CompletionCelebrationCard(
                        challenge = challenge,
                        onCelebrate = {
                            coroutineScope.launch {
                                navigator.push(
                                    AvatarScreen(ConversationTrigger.CHALLENGE_COMPLETED)
                                )
                            }
                        },
                        celebrateLabel = languageViewModel.getString(StringKey.CELEBRATE_WITH_FITNESS_BUDDY),
                        onShare = {
                            coroutineScope.launch {
                                navigator.push(
                                    org.awi.fitness.ui.screens.community.CreatePostScreen(
                                        prefilledContent = "🏆 Just completed the \"${challenge.title}\" challenge! ${challenge.target} ${challenge.unit} done. ${challenge.badgeIcon} #TAJLY #FitnessChallenge"
                                    )
                                )
                            }
                        },
                    )
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
                        text = LocalLanguageViewModel.current.getString(StringKey.LEADERBOARD),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi,
                        modifier = Modifier.weight(1f)
                    )
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = GoldPrimary,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            if (state.leaderboard.isEmpty() && !state.isLoading) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.ic3d_medal),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = LocalLanguageViewModel.current.getString(StringKey.CDX_NO_ENTRIES),
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textMid
                            )
                        }
                    }
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
                items(state.leaderboard) { entry ->
                    LeaderboardCard(
                        entry = entry,
                        onClick = {
                            navigator.push(
                                org.awi.fitness.ui.screens.community.CommunityProfileScreen(entry.userId)
                            )
                        },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
        }
        }
    }
}

// ---------------------------------------------------------------------------
// Private composables
// ---------------------------------------------------------------------------

/**
 * Image hero: the challenge photo (remote or bundled) with a dark scrim, a big centered
 * progress ring, difficulty + countdown chips, and the title. The ring sweeps on appear.
 */
@Composable
private fun ChallengeHeroCard(
    challenge: Challenge,
    currentProgress: Int,
    progressFraction: Float,
    modifier: Modifier = Modifier
) {
    val pct = (progressFraction * 100).roundToInt()
    val daysLeft = challengeDaysLeft(challenge)
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(shape),
    ) {
        if (!challenge.imageUrl.isNullOrBlank()) {
            ImagePlaceholder(
                url = challenge.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Image(
                painter = painterResource(Res.drawable.card_compete),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        // legibility scrim
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0.0f to Color.Black.copy(alpha = 0.35f),
                    0.45f to Color.Black.copy(alpha = 0.30f),
                    1.0f to Color.Black.copy(alpha = 0.90f),
                ),
            ),
        )

        // top chips
        Row(
            modifier = Modifier.align(Alignment.TopStart).fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Only show a real difficulty; hide the chip when the template didn't specify one.
            if (challenge.hasDifficulty) {
                ChallengeDifficultyChip(difficulty = challenge.difficulty)
            } else {
                Spacer(modifier = Modifier.width(0.dp))
            }
            if (daysLeft >= 0) CountdownChip(daysLeft = daysLeft)
        }

        // centered progress ring
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StatRing(progress = progressFraction, diameter = 168.dp, strokeWidth = 13.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldBright,
                    )
                    Text(
                        text = "$currentProgress / ${challenge.target} ${challenge.unit}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
            }
        }

        // bottom title block
        Column(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(20.dp),
        ) {
            Text(
                text = LocalLanguageViewModel.current.getString(StringKey.CDX_CHALLENGE_LABEL),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = GoldBright,
                letterSpacing = 1.5.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = challenge.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.80f),
                lineHeight = 20.sp,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun JoinChallengeCard(isLoading: Boolean, onJoin: () -> Unit) {
    val c = TajlyTheme.colors
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = LocalLanguageViewModel.current.getString(StringKey.CDX_READY),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = c.textHi
            )
            GoldButton(
                text = LocalLanguageViewModel.current.getString(StringKey.CDX_JOIN_CHALLENGE),
                onClick = onJoin,
                loading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CompletionCelebrationCard(
    challenge: Challenge,
    onCelebrate: () -> Unit,
    celebrateLabel: String,
    onShare: () -> Unit,
) {
    val c = TajlyTheme.colors
    val shape = RoundedCornerShape(24.dp)

    // one-shot pop-in for the whole card (a single, tasteful entrance)
    val entrance = remember { Animatable(0.9f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(1f, tween(420, easing = EaseOutCubic))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(entrance.value)
            // real liquid glass surface + gold tint
            .liquidGlass(shape = shape, goldTint = true),
    ) {
        // subtle success-photo backdrop wash inside the glass
        Image(
            painter = painterResource(Res.drawable.bg_success),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize().alpha(if (c.isDark) 0.30f else 0.22f),
        )
        // legibility scrim
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(c.bg.copy(alpha = 0.20f), c.bg.copy(alpha = 0.72f)),
                    ),
                ),
        )
        // warm gold halo
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(220.dp)
                .blur(60.dp)
                .background(Brush.radialGradient(listOf(GoldBright.copy(alpha = 0.38f), Color.Transparent))),
        )

        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero: animated trophy crowned (one-shot Lottie)
            Box(contentAlignment = Alignment.TopCenter) {
                LottieAnim(
                    "lottie_trophy.json",
                    Modifier.size(72.dp),
                    iterations = 1,
                )
                Image(
                    painter = painterResource(Res.drawable.ic3d_crown),
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .offset(y = (-22).dp),
                )
            }
            Text(
                text = LocalLanguageViewModel.current.getString(StringKey.CDX_YOU_WON),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = c.textHi,
            )
            Text(
                text = "${challenge.title} · +${challenge.reward.xp.takeIf { it > 0 } ?: challenge.xpReward} XP",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textMid,
            )

            Spacer(Modifier.height(4.dp))

            // Prominent SHARE — the hero CTA so wins post beautifully
            val shareInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Tajly.GoldGradient)
                    .clickable(interactionSource = shareInteraction, indication = null) { onShare() }
                    .heightIn(min = 54.dp)
                    .padding(horizontal = 22.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = TablerIcons.Share,
                        contentDescription = null,
                        tint = OnGold,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = LocalLanguageViewModel.current.getString(StringKey.SHARE_TO_COMMUNITY),
                        color = OnGold,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Secondary — Celebrate with the fitness buddy (glass)
            val celebrateInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(shape = RoundedCornerShape(16.dp))
                    .clickable(interactionSource = celebrateInteraction, indication = null) { onCelebrate() }
                    .heightIn(min = 52.dp)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(Res.drawable.ic3d_thumb_up),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = celebrateLabel,
                        color = GoldBright,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // One-shot celebratory confetti over the whole card.
        LottieAnim(
            "lottie_confetti.json",
            Modifier.matchParentSize(),
            iterations = 1,
        )
    }
}

@Composable
private fun AutoTrackingCard(challenge: Challenge, currentProgress: Int) {
    val c = TajlyTheme.colors
    val (icon, triggerText) = when (challenge.targetType) {
        org.awi.fitness.model.ChallengeTargetType.WORKOUTS ->
            challenge3dIcon(challenge) to LocalLanguageViewModel.current.getString(StringKey.CDX_AUTO_WORKOUT)
        org.awi.fitness.model.ChallengeTargetType.MEALS ->
            challenge3dIcon(challenge) to LocalLanguageViewModel.current.getString(StringKey.CDX_AUTO_MEAL)
        org.awi.fitness.model.ChallengeTargetType.POSTS ->
            challenge3dIcon(challenge) to LocalLanguageViewModel.current.getString(StringKey.CDX_AUTO_POST)
        org.awi.fitness.model.ChallengeTargetType.STREAK ->
            challenge3dIcon(challenge) to LocalLanguageViewModel.current.getString(StringKey.CDX_AUTO_CHECKIN)
        // No tracking source exists for these types; kept only for exhaustiveness (never seeded).
        org.awi.fitness.model.ChallengeTargetType.CALORIES,
        org.awi.fitness.model.ChallengeTargetType.STEPS ->
            challenge3dIcon(challenge) to LocalLanguageViewModel.current.getString(StringKey.CDX_AUTO_GENERIC)
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    LocalLanguageViewModel.current.getString(StringKey.CDX_AUTO_TRACKED),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi
                )
            }
            Text(
                triggerText,
                style = MaterialTheme.typography.bodyMedium,
                color = c.textMid
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    LocalLanguageViewModel.current.getString(StringKey.CDX_CURRENT_PROGRESS),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textLow
                )
                Text(
                    "$currentProgress / ${challenge.target} ${challenge.unit}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GoldBright
                )
            }
        }
    }
}

@Composable
private fun ChallengeInfoSection(challenge: Challenge, modifier: Modifier = Modifier) {
    val c = TajlyTheme.colors
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = LocalLanguageViewModel.current.getString(StringKey.CHALLENGE_INFO),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = c.textHi
            )
            Spacer(modifier = Modifier.height(14.dp))
            InfoRow(
                icon = Res.drawable.ic3d_tick_check,
                title = LocalLanguageViewModel.current.getString(StringKey.CDX_TYPE),
                value = challenge.type.name.lowercase().replaceFirstChar { it.uppercase() }
            )
            InfoRow(
                icon = Res.drawable.ic3d_target,
                title = LocalLanguageViewModel.current.getString(StringKey.CDX_TARGET),
                value = "${challenge.target} ${challenge.unit}"
            )
            // Only show a real difficulty / duration; hide when the template didn't specify one.
            if (challenge.hasDifficulty) {
                InfoRow(
                    icon = Res.drawable.ic3d_chart,
                    title = LocalLanguageViewModel.current.getString(StringKey.CDX_DIFFICULTY),
                    value = challenge.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }
                )
            }
            if (challenge.duration > 0) {
                InfoRow(
                    icon = Res.drawable.ic3d_tick_check,
                    title = LocalLanguageViewModel.current.getString(StringKey.DURATION),
                    value = "${challenge.duration} ${if (challenge.duration == 1) LocalLanguageViewModel.current.getString(StringKey.CDX_DAY) else LocalLanguageViewModel.current.getString(StringKey.DAYS)}"
                )
            }
            InfoRow(
                icon = Res.drawable.ic3d_thumb_up,
                title = LocalLanguageViewModel.current.getString(StringKey.CDX_PARTICIPANTS),
                value = challenge.participantIds.size.toString()
            )
            if (challenge.createdBy.isNotBlank()) {
                InfoRow(
                    icon = Res.drawable.ic3d_chart,
                    title = LocalLanguageViewModel.current.getString(StringKey.CDX_CREATED_BY),
                    value = if (challenge.createdBy == "system") "TAJLY" else challenge.createdBy
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: DrawableResource,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val c = TajlyTheme.colors
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = c.textMid,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = c.textHi
        )
    }
}

@Composable
private fun RewardsSection(challenge: Challenge, modifier: Modifier = Modifier) {
    val c = TajlyTheme.colors
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = LocalLanguageViewModel.current.getString(StringKey.REWARDS),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = c.textHi
            )
            Spacer(modifier = Modifier.height(14.dp))

            val xp = challenge.reward.xp.takeIf { it > 0 } ?: challenge.xpReward
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(Res.drawable.ic3d_flash_lightning),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "+$xp XP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi
                )
            }

            // Badge reward — the 3D badge the challenge grants toward achievements.
            if (challenge.badgeIcon.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = challenge.badgeIcon,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = LocalLanguageViewModel.current.getString(StringKey.CDX_CHALLENGE_BADGE),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi
                    )
                }
            }
        }
    }
}
