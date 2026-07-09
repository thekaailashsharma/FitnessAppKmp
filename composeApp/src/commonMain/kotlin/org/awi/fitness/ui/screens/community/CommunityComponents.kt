package org.awi.fitness.ui.screens.community

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.DotsVertical
import compose.icons.tablericons.Flag
import compose.icons.tablericons.Ban
import compose.icons.tablericons.MessageCircle
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Trophy
import compose.icons.tablericons.UserPlus
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.ic3d_heart
import fitnessappkmp.composeapp.generated.resources.tajly_logo
import fitnessappkmp.composeapp.generated.resources.ic3d_trophy
import kotlinx.coroutines.launch
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.CommunityPost
import org.awi.fitness.model.CommunityUser
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.utils.DateUtils
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class CommunityFeedTab(val filter: String, val label: String) {
    FOR_YOU("public", "For You"),
    FOLLOWING("friends", "Following"),
    YOU("my_posts", "You"),
}

@Composable
fun CommunityAvatar(
    name: String,
    imageUrl: String? = null,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val base = modifier.size(size).clip(CircleShape)
    val clickableModifier = if (onClick != null) base.clickable(onClick = onClick) else base

    Box(modifier = clickableModifier) {
        if (name.trim().equals("TAJLY", ignoreCase = true)) {
            // The official TAJLY account always shows the brand logo.
            androidx.compose.foundation.Image(
                painter = painterResource(Res.drawable.tajly_logo),
                contentDescription = "TAJLY",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            ImagePlaceholder(
                url = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                showInitial = true,
                initial = name,
                tint = OnGold,
                backgroundColor = GoldPrimary,
            )
        }
    }
}

/**
 * Reusable "active person" atom: a gold gradient ring + faint two-circle halo around an
 * avatar, with an optional short caption below. Generic on purpose (primitives, not models)
 * so it can be reused in the community feed rail AND the challenges leaderboard.
 */
@Composable
fun RingedAvatar(
    name: String,
    imageUrl: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = 58.dp,
    ringActive: Boolean = true,
    label: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val c = TajlyTheme.colors
    val container = if (onClick != null) {
        modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick)
    } else modifier
    Column(
        modifier = container.padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
            // Faint outer halo (second circle of the two-circle halo).
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = this.size.minDimension / 2f
                drawCircle(
                    color = if (ringActive) GoldPrimary.copy(alpha = 0.20f) else c.hairStrong,
                    radius = r - 0.5f,
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }
            // Gold gradient ring → bg gap → avatar (the primary ring).
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(
                        if (ringActive) Tajly.GoldGradient
                        else Brush.linearGradient(listOf(c.hairStrong, c.hairStrong))
                    )
                    .padding(2.5.dp)
                    .clip(CircleShape)
                    .background(c.bg)
                    .padding(2.dp),
            ) {
                ImagePlaceholder(
                    url = imageUrl,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    showInitial = true,
                    initial = name,
                    tint = OnGold,
                    backgroundColor = GoldPrimary,
                )
            }
        }
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = c.textMid,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = size + 8.dp),
            )
        }
    }
}

/** Compact single-row composer pill — tap to open the create-post screen. */
@Composable
fun CommunityComposerStrip(
    userName: String,
    profileImageUrl: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    // The composer is always the signed-in user, so read the LIVE profile photo (freshest
    // local value) and fall back to any explicitly-passed URL — never a stale snapshot.
    val liveProfileImageUrl = profileImageUrl ?: UserSettings.getInstance().profilePhotoUrl
    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .clip(RoundedCornerShape(24.dp))
            .background(c.s1)
            .border(1.dp, c.hair, RoundedCornerShape(24.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CommunityAvatar(name = userName, imageUrl = liveProfileImageUrl, size = 32.dp)
        Text(
            text = "Share your win…",
            style = MaterialTheme.typography.bodyMedium,
            color = c.textMid,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Tajly.GoldGradient),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = TablerIcons.Plus,
                contentDescription = "Create post",
                tint = OnGold,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * Horizontal "people to follow" rail driven by the real suggestion engine
 * ([CommunityViewModel.loadSuggestedUsers] → [org.awi.fitness.viewmodel.FindFriendsState.suggestedUsers]).
 * Every avatar opens that member's profile; every Follow pill calls [CommunityViewModel.followUser].
 * This is the social-network "grow your circle" surface — no fabricated identities.
 */
@Composable
fun SuggestedMembersRail(
    users: List<CommunityUser>,
    onOpenProfile: (String) -> Unit,
    onFollow: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Suggested members",
) {
    val c = TajlyTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = c.textMid,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(users, key = { it.id }) { user ->
                SuggestedMemberChip(
                    user = user,
                    onOpenProfile = { onOpenProfile(user.id) },
                    onFollow = { onFollow(user.id) },
                )
            }
        }
    }
}

/** A single vertical member card in the suggested rail: ringed avatar, name, and a real follow pill. */
@Composable
fun SuggestedMemberChip(
    user: CommunityUser,
    onOpenProfile: () -> Unit,
    onFollow: () -> Unit,
) {
    val c = TajlyTheme.colors
    val name = user.name.ifBlank { user.username.ifBlank { user.id.substringBefore("@") } }
    Column(
        modifier = Modifier
            .width(132.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(c.s1)
            .border(1.dp, c.hair, RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        RingedAvatar(
            name = name,
            imageUrl = user.profileImage,
            size = 48.dp,
            onClick = onOpenProfile,
        )
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = c.textHi,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        val sub = if (user.streakDays > 0) "${user.streakDays}-day streak" else "Lvl ${user.level}"
        Text(
            text = sub,
            style = MaterialTheme.typography.labelSmall,
            color = c.textLow,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(2.dp))
        MemberFollowPill(
            isFollowing = user.isFollowing,
            onClick = { if (!user.isFollowing) onFollow() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Follow = gold gradient CTA; Following = calm confirmed glass pill (no re-trigger). */
@Composable
fun MemberFollowPill(
    isFollowing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val base = modifier
        .pressScale(interaction)
        .clip(CircleShape)
    val styled = if (isFollowing) {
        base.background(c.glassFill, CircleShape)
    } else {
        base.background(Tajly.GoldGradient)
    }
    Row(
        modifier = styled
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
    ) {
        Icon(
            imageVector = if (isFollowing) TablerIcons.Check else TablerIcons.UserPlus,
            contentDescription = if (isFollowing) "Following" else "Follow",
            tint = if (isFollowing) c.textMid else OnGold,
            modifier = Modifier.size(15.dp),
        )
        Text(
            text = if (isFollowing) "Following" else "Follow",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isFollowing) c.textMid else OnGold,
        )
    }
}

/**
 * Legacy empty-state shim kept for compatibility. Renders a premium glass empty card.
 * The feed screen prefers the shared [org.awi.fitness.ui.components.EmptyState] directly.
 */
@Composable
fun CommunityEmptyState(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        org.awi.fitness.ui.components.EmptyState(
            title = title,
            subtitle = message,
            icon = {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Tajly.sectionGlow(Tajly.Teal)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ic3d_heart),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(56.dp),
                    )
                }
            },
            cta = if (actionLabel != null && onAction != null) {
                { org.awi.fitness.ui.components.GoldButton(text = actionLabel, onClick = onAction) }
            } else null,
        )
    }
}

/**
 * Compact, modern post card: clean light surface, 16dp radius, hairline border, 14dp padding,
 * 40dp avatar, ~180dp media, tight action row. Gold used only as an accent (kudos, badge).
 */
@Composable
fun CommunityPostCard(
    post: CommunityPost,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLiked: Boolean = false,
    isOwnPost: Boolean = false,
    onReport: (() -> Unit)? = null,
    onBlock: (() -> Unit)? = null,
) {
    val c = TajlyTheme.colors
    val displayName = post.userName.ifBlank { post.userId.substringBefore("@") }
    val cardInteraction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(cardInteraction)
            .clip(RoundedCornerShape(16.dp))
            .background(c.s1)
            .border(1.dp, c.hair, RoundedCornerShape(16.dp))
            .clickable(interactionSource = cardInteraction, indication = null, onClick = onPostClick),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            // ── Header — avatar + name + time ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CommunityAvatar(
                    name = displayName,
                    imageUrl = post.userProfileImage,
                    size = 40.dp,
                    onClick = onUserClick,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textHi,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = DateUtils.formatTimestamp(post.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textLow,
                    )
                }
                if (post.isPersonalBest) {
                    CommunityBadgeChip(text = "PB", icon = TablerIcons.Trophy)
                } else if (post.streakDays != null) {
                    CommunityBadgeChip(text = "${post.streakDays}d")
                }

                // Moderation menu (App Store UGC compliance) — report/block on others' posts.
                if (!isOwnPost && (onReport != null || onBlock != null)) {
                    var menuOpen by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = TablerIcons.DotsVertical,
                                contentDescription = "More",
                                tint = c.textLow,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            onReport?.let { report ->
                                DropdownMenuItem(
                                    text = { Text("Report post", color = c.textHi) },
                                    leadingIcon = { Icon(TablerIcons.Flag, contentDescription = null, tint = c.textMid) },
                                    onClick = { menuOpen = false; report() },
                                )
                            }
                            onBlock?.let { block ->
                                DropdownMenuItem(
                                    text = { Text("Block user", color = c.textHi) },
                                    leadingIcon = { Icon(TablerIcons.Ban, contentDescription = null, tint = c.textMid) },
                                    onClick = { menuOpen = false; block() },
                                )
                            }
                        }
                    }
                }
            }

            // ── Content — compact readable body ──────────────────────────
            if (post.content.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textHi,
                )
            }

            // ── Media — rounded, compact ─────────────────────────────────
            post.imageUrl?.let { url ->
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp)),
                ) {
                    ImagePlaceholder(
                        url = url,
                        contentDescription = "Post image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // ── Workout stats ────────────────────────────────────────────
            if (post.calories != null || post.steps != null || post.duration != null) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    post.calories?.let { CommunityWorkoutStat("🔥", "$it kcal") }
                    post.steps?.let { CommunityWorkoutStat("👣", "$it steps") }
                    post.duration?.let { CommunityWorkoutStat("⏱️", DateUtils.formatSeconds(it)) }
                }
            }

            // ── Compact action row with a subtle divider ─────────────────
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = c.hair)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KudosButton(
                    likes = post.likes,
                    isLiked = isLiked,
                    onLikeClick = onLikeClick,
                )
                FlatIconAction(
                    icon = TablerIcons.MessageCircle,
                    label = if (post.comments > 0) "${post.comments}" else "Comment",
                    onClick = onCommentClick,
                )
            }
        }
    }
}

/**
 * GOLD kudos with a Compose-native micro-interaction: an outline heart that fills with the
 * gold gradient, springs, and emits a short one-shot gold particle burst — played only on an
 * affirmative kudos (never on un-kudos). No dependencies; drawn entirely with Canvas.
 */
@Composable
fun KudosButton(
    likes: Int,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
) {
    val c = TajlyTheme.colors
    var liked by remember(isLiked) { mutableStateOf(isLiked) }
    val scope = rememberCoroutineScope()
    val burst = remember { Animatable(0f) }
    val heartScale = remember { Animatable(1f) }
    val interaction = remember { MutableInteractionSource() }

    // Reflect the optimistic toggle on top of the server count.
    val count = likes + when {
        liked && !isLiked -> 1
        !liked && isLiked -> -1
        else -> 0
    }
    val label = if (count > 0) "$count" else "Kudos"

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null) {
                val nowLiked = !liked
                liked = nowLiked
                onLikeClick()
                scope.launch {
                    if (nowLiked) {
                        burst.snapTo(0f)
                        launch {
                            heartScale.animateTo(1.4f, spring(dampingRatio = 0.35f, stiffness = 420f))
                            heartScale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 320f))
                        }
                        burst.animateTo(1f, tween(300))
                    } else {
                        heartScale.snapTo(1f)
                    }
                }
            }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(20.dp)) {
            // One-shot gold particle burst.
            val p = burst.value
            if (p > 0f && p < 1f) {
                Canvas(modifier = Modifier.size(28.dp)) {
                    val n = 7
                    val maxR = size.minDimension * 0.55f
                    repeat(n) { i ->
                        val ang = (i.toFloat() / n) * 2f * PI.toFloat() - PI.toFloat() / 2f
                        val dist = maxR * p
                        drawCircle(
                            color = GoldPrimary,
                            radius = (1f - p) * 3f,
                            center = Offset(center.x + cos(ang) * dist, center.y + sin(ang) * dist),
                            alpha = 1f - p,
                        )
                    }
                    drawCircle(
                        color = GoldPrimary,
                        radius = maxR * p,
                        style = Stroke(width = (1f - p) * 3f),
                        alpha = (1f - p) * 0.7f,
                    )
                }
            }
            Canvas(modifier = Modifier.size(18.dp).scale(heartScale.value)) {
                val path = heartPath(size)
                if (liked) {
                    drawPath(path, brush = Tajly.GoldGradient)
                } else {
                    drawPath(path, color = c.textMid, style = Stroke(width = 1.6.dp.toPx()))
                }
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (liked) FontWeight.SemiBold else FontWeight.Normal,
            color = if (liked) GoldPrimary else c.textMid,
        )
    }
}

/** Heart path fitted into [size] (used for the outline→filled kudos heart). */
private fun heartPath(size: Size): Path {
    val w = size.width
    val h = size.height
    return Path().apply {
        moveTo(0.5f * w, 0.86f * h)
        cubicTo(0.16f * w, 0.62f * h, 0.02f * w, 0.42f * h, 0.02f * w, 0.28f * h)
        cubicTo(0.02f * w, 0.12f * h, 0.16f * w, 0.05f * h, 0.28f * w, 0.05f * h)
        cubicTo(0.40f * w, 0.05f * h, 0.47f * w, 0.14f * h, 0.5f * w, 0.20f * h)
        cubicTo(0.53f * w, 0.14f * h, 0.60f * w, 0.05f * h, 0.72f * w, 0.05f * h)
        cubicTo(0.84f * w, 0.05f * h, 0.98f * w, 0.12f * h, 0.98f * w, 0.28f * h)
        cubicTo(0.98f * w, 0.42f * h, 0.84f * w, 0.62f * h, 0.5f * w, 0.86f * h)
        close()
    }
}

/** Flat, icon-only action (comment). No pill background — pure declutter. */
@Composable
private fun FlatIconAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(icon, contentDescription = label, tint = c.textMid, modifier = Modifier.size(20.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.textMid)
    }
}

@Composable
fun CommunityBadgeChip(
    text: String,
    icon: ImageVector? = null,
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Tajly.GoldGradient)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon == TablerIcons.Trophy) {
            // Use the 3D trophy for personal-best badges.
            Image(
                painter = painterResource(Res.drawable.ic3d_trophy),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(15.dp),
            )
        } else if (icon != null) {
            Icon(icon, null, tint = OnGold, modifier = Modifier.size(13.dp))
        }
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = OnGold,
        )
    }
}

@Composable
fun CommunityWorkoutStat(icon: String, value: String) {
    val c = TajlyTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(icon, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodySmall, color = c.textMid)
    }
}

@Composable
fun CommunityActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color.Unspecified,
    iconScale: Float = 1f,
) {
    val c = TajlyTheme.colors
    val resolvedTint = if (tint == Color.Unspecified) c.textMid else tint
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .pressScale(interaction)
            .clip(CircleShape)
            .background(c.glassFill, CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = resolvedTint,
            modifier = Modifier.size(18.dp).scale(iconScale),
        )
        Text(label, style = MaterialTheme.typography.labelLarge, color = resolvedTint)
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
    tint: Color = Color.Unspecified,
    iconScale: Float = 1f,
) = CommunityActionChip(icon, text, onClick, tint, iconScale)
