package org.awi.fitness.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.ic3d_crown
import fitnessappkmp.composeapp.generated.resources.ic3d_medal
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.LeaderboardEntry
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.viewmodel.LanguageViewModel
import org.jetbrains.compose.resources.painterResource

// ---------------------------------------------------------------------------
// Ringed avatar atom
// ---------------------------------------------------------------------------
// NOTE: The community team is introducing a shared `RingedAvatar` atom in
// community/CommunityComponents.kt. When that lands, swap the body of this
// `LeaderboardRingedAvatar` to delegate to it (keeping the rank-tier ring). The
// call sites below only depend on this local wrapper, so the swap is one edit.

/** Bronze→silver→gold tier ring for the podium; neutral violet for the rest. */
private fun rankTierRing(rank: Int): Brush = when (rank) {
    1 -> Brush.sweepGradient(listOf(Tajly.Champagne, GoldBright, GoldPrimary, GoldBright, Tajly.Champagne))
    2 -> Brush.sweepGradient(listOf(Color(0xFFE6ECF5), Color(0xFFB8C0CC), Color(0xFF8F97A3), Color(0xFFB8C0CC), Color(0xFFE6ECF5)))
    3 -> Brush.sweepGradient(listOf(Color(0xFFE0BE93), Color(0xFFB08D57), Color(0xFF8A6D3E), Color(0xFFB08D57), Color(0xFFE0BE93)))
    else -> Brush.linearGradient(listOf(Tajly.Violet, Tajly.Violet.copy(alpha = 0.5f)))
}

private fun challengeRankAccent(rank: Int): Color = when (rank) {
    1 -> GoldPrimary
    2 -> Color(0xFFB8C0CC) // silver
    3 -> Color(0xFFB08D57) // bronze
    else -> Tajly.Violet
}

/**
 * A circular avatar wrapped in an active-people tier ring. Local atom kept tiny and
 * swappable for the shared community `RingedAvatar` once it lands.
 */
@Composable
fun LeaderboardRingedAvatar(
    name: String,
    rank: Int,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    ringWidth: Dp = 3.dp,
    onClick: (() -> Unit)? = null,
) {
    val ring = rankTierRing(rank)
    val accent = challengeRankAccent(rank)
    val base = modifier
        .size(size)
        .clip(CircleShape)
        .background(ring, CircleShape)
        .padding(ringWidth)
    val clickableBase = if (onClick != null) {
        val interaction = remember { MutableInteractionSource() }
        base.clip(CircleShape).clickable(
            interactionSource = interaction,
            indication = null,
            onClick = onClick,
        )
    } else base.clip(CircleShape)

    Box(modifier = clickableBase, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.6f))),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Podium arc — top-3 active-people ring
// ---------------------------------------------------------------------------

/**
 * A premium podium arc of the top-3 leaderboard entries: rank 1 raised and centered
 * with a crown, ranks 2 & 3 flanking. Each avatar taps through to the given handler
 * (community profile). Renders nothing when there are fewer than 3 entries.
 */
@Composable
fun LeaderboardPodium(
    entries: List<LeaderboardEntry>,
    modifier: Modifier = Modifier,
    onUserClick: ((LeaderboardEntry) -> Unit)? = null,
) {
    val top = entries.sortedBy { it.rank }.take(3)
    if (top.size < 3) return
    val byRank = top.associateBy { it.rank }
    val first = byRank[1] ?: top[0]
    val second = byRank[2] ?: top[1]
    val third = byRank[3] ?: top[2]

    GlassCard(modifier = modifier.fillMaxWidth(), goldTint = true) {
        // warm gold halo behind the champion
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .size(180.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(listOf(GoldBright.copy(alpha = 0.22f), Color.Transparent)),
                ),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Bottom,
        ) {
            PodiumMember(entry = second, avatarSize = 56.dp, raised = 20.dp, onUserClick = onUserClick)
            PodiumMember(entry = first, avatarSize = 76.dp, raised = 0.dp, isChampion = true, onUserClick = onUserClick)
            PodiumMember(entry = third, avatarSize = 56.dp, raised = 28.dp, onUserClick = onUserClick)
        }
    }
}

@Composable
private fun PodiumMember(
    entry: LeaderboardEntry,
    avatarSize: Dp,
    raised: Dp,
    isChampion: Boolean = false,
    onUserClick: ((LeaderboardEntry) -> Unit)? = null,
) {
    val c = TajlyTheme.colors
    Column(
        modifier = Modifier
            .width(if (isChampion) 96.dp else 84.dp)
            .padding(top = raised),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            LeaderboardRingedAvatar(
                name = entry.username,
                rank = entry.rank,
                size = avatarSize,
                ringWidth = if (isChampion) 4.dp else 3.dp,
                onClick = onUserClick?.let { { it(entry) } },
            )
            if (isChampion) {
                Image(
                    painter = painterResource(Res.drawable.ic3d_crown),
                    contentDescription = "Champion",
                    modifier = Modifier
                        .size(34.dp)
                        .offset(y = (-20).dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = entry.username,
            style = MaterialTheme.typography.labelMedium,
            color = c.textHi,
            fontWeight = if (isChampion) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = entry.xp.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = if (isChampion) GoldBright else c.textMid,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = " XP",
                style = MaterialTheme.typography.labelSmall,
                color = c.textLow,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Leaderboard row
// ---------------------------------------------------------------------------

@Composable
fun LeaderboardCard(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val c = TajlyTheme.colors
    val userSettings = UserSettings.getInstance()
    val languageViewModel = remember { LanguageViewModel(userSettings.settings) }

    // Champions (top-3) get a subtle gold-tinted surface; gold = earned.
    val isPodium = entry.rank in 1..3
    val shape = RoundedCornerShape(16.dp)

    val rowBase = modifier
        .fillMaxWidth()
        .liquidGlass(shape = shape, goldTint = isPodium)
    val rowModifier = if (onClick != null) {
        val interaction = remember { MutableInteractionSource() }
        rowBase.clickable(interactionSource = interaction, indication = null, onClick = onClick)
    } else rowBase

    Row(
        modifier = rowModifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RankIndicator(rank = entry.rank)

        Spacer(Modifier.width(12.dp))

        LeaderboardRingedAvatar(name = entry.username, rank = entry.rank, size = 44.dp)

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.username,
                style = MaterialTheme.typography.bodyMedium,
                color = c.textHi,
                fontWeight = if (isPodium) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${languageViewModel.getString(StringKey.LEVEL_SHORT)}${entry.level}",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textMid,
                )
                if (entry.streak > 0) {
                    Text("  ·  ", style = MaterialTheme.typography.labelSmall, color = c.textLow)
                    Text("🔥", fontSize = 11.sp)
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "${entry.streak} ${languageViewModel.getString(StringKey.DAYS)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textMid,
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        // big tabular value
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = entry.xp.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPodium) GoldBright else c.textHi,
            )
            Text(
                text = "XP",
                style = MaterialTheme.typography.labelSmall,
                color = c.textLow,
            )
        }
    }
}

/** Rank chip: 3D medal/crown for the podium, tabular # for the rest. */
@Composable
private fun RankIndicator(rank: Int, modifier: Modifier = Modifier) {
    val c = TajlyTheme.colors
    Box(
        modifier = modifier.size(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (rank) {
            1 -> Image(
                painter = painterResource(Res.drawable.ic3d_crown),
                contentDescription = "1st",
                modifier = Modifier.size(34.dp),
            )
            2, 3 -> Image(
                painter = painterResource(Res.drawable.ic3d_medal),
                contentDescription = "$rank",
                modifier = Modifier.size(30.dp),
            )
            else -> Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(c.glassFill, CircleShape)
                    .border(1.dp, c.hairStrong, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textMid,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
