package org.awi.fitness.ui.components

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.ic3d_chart
import fitnessappkmp.composeapp.generated.resources.ic3d_fire
import fitnessappkmp.composeapp.generated.resources.ic3d_flag
import fitnessappkmp.composeapp.generated.resources.ic3d_flash_lightning
import fitnessappkmp.composeapp.generated.resources.ic3d_gym
import fitnessappkmp.composeapp.generated.resources.ic3d_leaf
import fitnessappkmp.composeapp.generated.resources.ic3d_target
import fitnessappkmp.composeapp.generated.resources.ic3d_trophy
import fitnessappkmp.composeapp.generated.resources.ic3d_water_glass
import org.awi.fitness.model.Challenge
import org.awi.fitness.model.ChallengeDifficulty
import org.awi.fitness.model.ChallengeTargetType
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.Image

// ---------------------------------------------------------------------------
// Shared challenge-domain helpers (used across challenge components)
// ---------------------------------------------------------------------------

/** Maps a challenge to a colorful 3D studio-render icon (design-system asset). */
internal fun challenge3dIcon(challenge: Challenge): DrawableResource = when (challenge.targetType) {
    ChallengeTargetType.WORKOUTS -> Res.drawable.ic3d_gym
    ChallengeTargetType.MEALS -> Res.drawable.ic3d_leaf
    ChallengeTargetType.STREAK -> Res.drawable.ic3d_fire
    ChallengeTargetType.CALORIES -> Res.drawable.ic3d_flash_lightning
    ChallengeTargetType.STEPS -> Res.drawable.ic3d_target
    ChallengeTargetType.POSTS -> Res.drawable.ic3d_chart
}

/** Per-target section accent — colorful identity glow (gold stays reserved for win/active). */
internal fun challengeAccent(challenge: Challenge): Color = when (challenge.targetType) {
    ChallengeTargetType.WORKOUTS -> Tajly.Violet
    ChallengeTargetType.MEALS -> Tajly.Green
    ChallengeTargetType.STREAK -> Tajly.Coral
    ChallengeTargetType.CALORIES -> Tajly.Blue
    ChallengeTargetType.STEPS -> Tajly.Teal
    ChallengeTargetType.POSTS -> Tajly.Pink
}

/** Days remaining for a challenge, or -1 if no end date. */
internal fun challengeDaysLeft(challenge: Challenge): Int =
    if (challenge.endDateLong > 0L) {
        val msLeft = challenge.endDateLong - org.awi.fitness.utils.currentTimeMillis()
        (msLeft / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    } else -1

/** A colorful 3D icon sitting on a glass tile with a soft section glow. */
@Composable
internal fun Challenge3dBadge(
    challenge: Challenge,
    diameter: Int = 56,
    iconSize: Int = 40,
    modifier: Modifier = Modifier,
) {
    val accent = challengeAccent(challenge)
    Box(modifier = modifier.size(diameter.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size((diameter - 6).dp)
                .blur(20.dp)
                .background(
                    Brush.radialGradient(listOf(accent.copy(alpha = 0.6f), Color.Transparent)),
                    CircleShape,
                ),
        )
        Image(
            painter = painterResource(challenge3dIcon(challenge)),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(iconSize.dp),
        )
    }
}

/** Gold-tinted countdown chip. Tightens to coral/amber as the deadline nears. */
@Composable
internal fun CountdownChip(daysLeft: Int, modifier: Modifier = Modifier) {
    if (daysLeft < 0) return
    val color = when {
        daysLeft == 0 -> Tajly.Coral
        daysLeft <= 2 -> Color(0xFFFF9800)
        else -> GoldBright
    }
    val label = when (daysLeft) {
        0 -> "Ends today"
        1 -> "1 day left"
        else -> "$daysLeft days left"
    }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.30f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ---------------------------------------------------------------------------
// Active-challenge glass card (carousel)
// ---------------------------------------------------------------------------

@Composable
fun ChallengeCard(
    challenge: Challenge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TajlyTheme.colors
    val accent = challengeAccent(challenge)
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(22.dp)

    val progress = if (challenge.target > 0) {
        (challenge.progress.toFloat() / challenge.target.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val progressAnimation by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "progress",
    )

    Box(
        modifier = modifier
            .pressScale(interaction)
            .clip(shape)
            .background(c.glassFill, shape)
            .border(1.dp, c.glassRim, shape)
            .clickable(interactionSource = interaction, indication = null) { onClick() },
    ) {
        // section glow, top-trailing
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(120.dp)
                .blur(40.dp)
                .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.35f), Color.Transparent))),
        )
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Challenge3dBadge(challenge = challenge, diameter = 56, iconSize = 42)
                ChallengeDifficultyChip(difficulty = challenge.difficulty)
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = challenge.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = c.textHi,
                maxLines = 1,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodySmall,
                color = c.textMid,
                lineHeight = 18.sp,
                maxLines = 2,
            )

            Spacer(Modifier.height(16.dp))

            // big tabular progress numbers
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = challenge.progress.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldBright,
                )
                Text(
                    text = " / ${challenge.target} ${challenge.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMid,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            Spacer(Modifier.height(8.dp))

            // gold progress track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressAnimation)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Tajly.GoldGradient),
                )
            }

            val daysLeft = challengeDaysLeft(challenge)
            if (daysLeft >= 0) {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CountdownChip(daysLeft = daysLeft)
                    XpPill(xp = challenge.reward.xp.takeIf { it > 0 } ?: challenge.xpReward)
                }
            } else {
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    XpPill(xp = challenge.reward.xp.takeIf { it > 0 } ?: challenge.xpReward)
                }
            }
        }
    }
}

@Composable
internal fun XpPill(xp: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(GoldPrimary.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.ic3d_flash_lightning),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "+$xp XP",
            style = MaterialTheme.typography.labelMedium,
            color = GoldBright,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
internal fun ChallengeDifficultyChip(
    difficulty: ChallengeDifficulty,
    modifier: Modifier = Modifier,
) {
    val (color, text) = when (difficulty) {
        ChallengeDifficulty.BEGINNER -> Tajly.Green to "Easy"
        ChallengeDifficulty.INTERMEDIATE -> Color(0xFFFF9800) to "Medium"
        ChallengeDifficulty.ADVANCED -> Tajly.Coral to "Hard"
    }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.30f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
