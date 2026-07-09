package org.awi.fitness.ui.screens.community
import org.awi.fitness.data.StringKey
import org.awi.fitness.viewmodel.LocalLanguageViewModel

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Bell
import compose.icons.tablericons.Heart
import compose.icons.tablericons.MessageCircle
import compose.icons.tablericons.Star
import compose.icons.tablericons.Trophy
import compose.icons.tablericons.UserPlus
import kotlinx.coroutines.launch
import org.awi.fitness.model.ActivityNotification
import org.awi.fitness.model.CommunityActivityType
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.ui.components.statusBarPadding
import org.awi.fitness.utils.DateUtils
import org.awi.fitness.viewmodel.CommunityViewModel
import org.awi.fitness.viewmodel.ViewModelStore

class ActivityScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { CommunityViewModel() }
        val activityState by viewModel.activityState.collectAsState()
        val scope = rememberCoroutineScope()
        val c = TajlyTheme.colors

        LaunchedEffect(Unit) {
            viewModel.loadActivityNotifications()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(c.bg),
        ) {
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
                        text = LocalLanguageViewModel.current.getString(StringKey.CMX_ACTIVITY),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi,
                        modifier = Modifier.weight(1f),
                    )
                }

                when {
                    activityState.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GoldPrimary)
                        }
                    }

                    activityState.error != null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = activityState.error ?: LocalLanguageViewModel.current.getString(StringKey.CMX_UNKNOWN_ERROR),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    activityState.notifications.isEmpty() -> {
                        CommunityEmptyState(
                            title = LocalLanguageViewModel.current.getString(StringKey.NO_ACTIVITY_YET),
                            message = LocalLanguageViewModel.current.getString(StringKey.CMX_NO_ACTIVITY_MESSAGE),
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(activityState.notifications, key = { it.id }) { notification ->
                                NotificationItem(
                                    notification = notification,
                                    viewModel = viewModel,
                                    onNotificationClick = {
                                        scope.launch {
                                            viewModel.markNotificationAsRead(notification.id)
                                        }
                                        when (notification.type) {
                                            CommunityActivityType.LIKE, CommunityActivityType.COMMENT -> {
                                                if (notification.targetId.isNotBlank()) {
                                                    navigator.push(PostDetailScreen(notification.targetId))
                                                }
                                            }
                                            CommunityActivityType.FOLLOW -> {
                                                if (notification.userId.isNotBlank()) {
                                                    navigator.push(CommunityProfileScreen(notification.userId))
                                                }
                                            }
                                            else -> { /* badge/streak/challenge — no nav needed */ }
                                        }
                                    }
                                )
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
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
fun NotificationItem(
    notification: ActivityNotification,
    viewModel: CommunityViewModel,
    onNotificationClick: () -> Unit
) {
    val c = TajlyTheme.colors
    val scope = rememberCoroutineScope()
    val cardInteraction = remember { MutableInteractionSource() }

    // Accent color per notification type (quiet canvas, one colored leading badge).
    val accent = when (notification.type) {
        CommunityActivityType.LIKE -> Tajly.Coral
        CommunityActivityType.COMMENT -> Tajly.Blue
        CommunityActivityType.FOLLOW -> Tajly.Green
        CommunityActivityType.CHALLENGE_INVITE -> Tajly.Pink
        CommunityActivityType.BADGE_EARNED -> GoldPrimary
        CommunityActivityType.STREAK_MILESTONE -> GoldPrimary
    }

    GlassCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(cardInteraction)
            .clickable(interactionSource = cardInteraction, indication = null, onClick = onNotificationClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Leading badge: profile image, or colored type icon.
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (notification.userProfileImage != null) Color.Transparent
                        else accent.copy(alpha = 0.18f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (notification.userProfileImage != null) {
                    ImagePlaceholder(
                        url = notification.userProfileImage,
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = when (notification.type) {
                            CommunityActivityType.LIKE -> TablerIcons.Heart
                            CommunityActivityType.COMMENT -> TablerIcons.MessageCircle
                            CommunityActivityType.FOLLOW -> TablerIcons.UserPlus
                            CommunityActivityType.CHALLENGE_INVITE -> TablerIcons.Trophy
                            CommunityActivityType.BADGE_EARNED -> TablerIcons.Star
                            CommunityActivityType.STREAK_MILESTONE -> TablerIcons.Bell
                        },
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Notification content
                when (notification.type) {
                    CommunityActivityType.LIKE -> {
                        Text(
                            text = "${notification.userName} ${LocalLanguageViewModel.current.getString(StringKey.CMX_CHEERED_YOUR_POST)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = c.textHi,
                        )
                        if (notification.targetContent.isNotBlank()) {
                            Text(
                                text = "\"${notification.targetContent}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textMid,
                                maxLines = 2
                            )
                        }
                    }
                    CommunityActivityType.COMMENT -> {
                        Text(
                            text = "${notification.userName} ${LocalLanguageViewModel.current.getString(StringKey.COMMENTED_ON_YOUR_POST)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = c.textHi,
                        )
                        if (notification.targetContent.isNotBlank()) {
                            Text(
                                text = "\"${notification.targetContent}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textMid,
                                maxLines = 2
                            )
                        }
                    }
                    CommunityActivityType.FOLLOW -> {
                        Text(
                            text = "${notification.userName} ${LocalLanguageViewModel.current.getString(StringKey.CMX_STARTED_FOLLOWING_YOU)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = c.textHi,
                        )
                    }
                    CommunityActivityType.CHALLENGE_INVITE -> {
                        Text(
                            text = LocalLanguageViewModel.current.getString(StringKey.CMX_CHALLENGE_INVITE),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = c.textHi,
                        )
                        if (notification.targetContent.isNotBlank()) {
                            Text(
                                text = notification.targetContent,
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textMid,
                            )
                        }
                    }
                    CommunityActivityType.BADGE_EARNED -> {
                        Text(
                            text = "${LocalLanguageViewModel.current.getString(StringKey.EARNED_BADGE)} '${notification.targetContent}' ${LocalLanguageViewModel.current.getString(StringKey.CMX_BADGE_SUFFIX)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = c.textHi,
                        )
                        Text(
                            text = LocalLanguageViewModel.current.getString(StringKey.CONGRATULATIONS),
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textMid,
                        )
                    }
                    CommunityActivityType.STREAK_MILESTONE -> {
                        Text(
                            text = "${LocalLanguageViewModel.current.getString(StringKey.ON_A_STREAK)} ${notification.targetContent}!",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = c.textHi,
                        )
                        Text(
                            text = LocalLanguageViewModel.current.getString(StringKey.KEEP_IT_UP),
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textMid,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time — demoted, single relative stamp.
                Text(
                    text = DateUtils.formatTimestampRelative(notification.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textLow
                )

                // Challenge buttons
                if (notification.type == CommunityActivityType.CHALLENGE_INVITE && !notification.isRead) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Primary CTA: Accept (gold). Decline demoted to glass pill.
                        GoldButton(
                            text = LocalLanguageViewModel.current.getString(StringKey.ACCEPT),
                            onClick = {
                                scope.launch {
                                    notification.targetId.takeIf { it.isNotBlank() }?.let { challengeId ->
                                        ViewModelStore.challenges.joinChallenge(challengeId)
                                    }
                                    viewModel.markNotificationAsRead(notification.id)
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                        GlassTextPill(
                            text = LocalLanguageViewModel.current.getString(StringKey.DECLINE),
                            onClick = {
                                scope.launch {
                                    viewModel.markNotificationAsRead(notification.id)
                                }
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Unread indicator — gold dot (rationed gold = "needs attention").
            // Subtle one-note pulse to read as "live" without being noisy.
            if (!notification.isRead) {
                Spacer(modifier = Modifier.width(10.dp))
                val pulse = rememberInfiniteTransition()
                val dotScale by pulse.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.25f,
                    animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                )
                Box(
                    modifier = Modifier
                        .graphicsLayer { scaleX = dotScale; scaleY = dotScale }
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Tajly.GoldGradient)
                )
            }
        }
    }
}

@Composable
private fun GlassTextPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .pressScale(interaction)
            .clip(RoundedCornerShape(16.dp))
            .background(c.glassFill, RoundedCornerShape(16.dp))
            .border(1.dp, c.hairStrong, RoundedCornerShape(16.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .height(52.dp)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = c.textHi,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleSmall,
        )
    }
}
