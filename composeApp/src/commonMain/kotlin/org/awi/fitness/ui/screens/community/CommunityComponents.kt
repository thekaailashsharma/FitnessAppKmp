package org.awi.fitness.ui.screens.community

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Heart
import compose.icons.tablericons.MessageCircle
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Trophy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.awi.fitness.model.CommunityPost
import org.awi.fitness.theme.GreenAccent
import org.awi.fitness.theme.TextGray
import org.awi.fitness.ui.components.FitnessCard
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.utils.DateUtils

enum class CommunityFeedTab(val filter: String, val label: String) {
    FOR_YOU("public", "For You"),
    FOLLOWING("friends", "Following"),
    YOU("my_posts", "You"),
}

@Composable
fun CommunityAvatar(
    name: String,
    imageUrl: String? = null,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val clickableModifier = if (onClick != null) {
        modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    } else {
        modifier.size(size).clip(CircleShape)
    }

    Box(modifier = clickableModifier) {
        ImagePlaceholder(
            url = imageUrl,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            showInitial = true,
            initial = name,
            tint = MaterialTheme.colorScheme.onPrimary,
            backgroundColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CommunityComposerStrip(
    userName: String,
    profileImageUrl: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CommunityAvatar(name = userName, imageUrl = profileImageUrl, size = 40.dp)
        Text(
            text = "Share your win today…",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = TablerIcons.Plus,
            contentDescription = "Create post",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun CommunityEmptyState(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun CommunityPostCard(
    post: CommunityPost,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLiked: Boolean = false,
) {
    var liked by remember(post.id, isLiked) { mutableStateOf(isLiked) }
    var heartScale by remember { mutableStateOf(1f) }
    val heartScaleAnim = animateFloatAsState(
        targetValue = heartScale,
        animationSpec = spring(dampingRatio = 0.3f, stiffness = 300f)
    )
    val scope = rememberCoroutineScope()
    val displayName = post.userName.ifBlank { post.userId.substringBefore("@") }

    FitnessCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPostClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CommunityAvatar(
                    name = displayName,
                    imageUrl = post.userProfileImage,
                    size = 44.dp,
                    onClick = onUserClick
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = DateUtils.formatTimestamp(post.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
                if (post.isPersonalBest) {
                    CommunityBadgeChip(text = "Personal Best", icon = TablerIcons.Trophy)
                } else if (post.streakDays != null) {
                    CommunityBadgeChip(text = "${post.streakDays}-day streak")
                }
            }

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge
            )

            post.imageUrl?.let { url ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    ImagePlaceholder(
                        url = url,
                        contentDescription = "Post image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (post.calories != null || post.steps != null || post.duration != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    post.calories?.let { CommunityWorkoutStat("🔥", "$it kcal") }
                    post.steps?.let { CommunityWorkoutStat("👣", "$it steps") }
                    post.duration?.let {
                        CommunityWorkoutStat("⏱️", DateUtils.formatSeconds(it))
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CommunityActionChip(
                    icon = TablerIcons.Heart,
                    label = if (post.likes > 0) "${post.likes}" else "Cheer",
                    tint = if (liked) GreenAccent else MaterialTheme.colorScheme.onSurface,
                    iconScale = if (liked) heartScaleAnim.value else 1f,
                    onClick = {
                        liked = !liked
                        heartScale = 1.4f
                        onLikeClick()
                        scope.launch {
                            delay(250)
                            heartScale = 1f
                        }
                    }
                )
                CommunityActionChip(
                    icon = TablerIcons.MessageCircle,
                    label = if (post.comments > 0) "${post.comments}" else "Comment",
                    onClick = onCommentClick
                )
            }
        }
    }
}

@Composable
fun CommunityBadgeChip(
    text: String,
    icon: ImageVector? = null,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GreenAccent.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon != null) {
            Icon(icon, null, tint = GreenAccent, modifier = Modifier.size(14.dp))
        }
        Text(text, style = MaterialTheme.typography.labelSmall, color = GreenAccent)
    }
}

@Composable
fun CommunityWorkoutStat(icon: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(icon, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodySmall, color = TextGray)
    }
}

@Composable
fun CommunityActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    iconScale: Float = 1f,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(18.dp).scale(iconScale)
        )
        Text(label, style = MaterialTheme.typography.labelLarge, color = tint)
    }
}

// Legacy aliases used by PostDetailScreen
@Composable
fun PostCard(
    post: CommunityPost,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onUserClick: () -> Unit,
) = CommunityPostCard(post, onPostClick, onLikeClick, onCommentClick, onUserClick)

@Composable
fun BadgeChip(text: String, icon: ImageVector? = null) = CommunityBadgeChip(text, icon)

@Composable
fun WorkoutStat(icon: String, value: String, singleLine: Boolean = false) =
    CommunityWorkoutStat(icon, value)

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    iconScale: Float = 1f,
) = CommunityActionChip(icon, text, onClick, tint, iconScale)
