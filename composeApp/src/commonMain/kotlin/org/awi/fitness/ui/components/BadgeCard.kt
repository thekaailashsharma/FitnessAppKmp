package org.awi.fitness.ui.components

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.ic3d_crown
import fitnessappkmp.composeapp.generated.resources.ic3d_medal
import fitnessappkmp.composeapp.generated.resources.ic3d_star
import fitnessappkmp.composeapp.generated.resources.ic3d_trophy
import org.awi.fitness.model.Badge
import org.awi.fitness.model.BadgeRarity
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.TajlyTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun BadgeCard(
    badge: Badge,
    isUnlocked: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val c = TajlyTheme.colors
    val tier = badgeTier(badge.rarity)
    val shape = RoundedCornerShape(20.dp)

    val glow by animateFloatAsState(
        targetValue = if (isUnlocked) 1f else 0f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "glow",
    )

    Box(
        modifier = modifier
            .size(120.dp)
            .clip(shape)
            .background(c.glassFill, shape)
            .border(
                1.dp,
                if (isUnlocked) tier.color.copy(alpha = 0.40f) else c.hairStrong,
                shape,
            ),
    ) {
        // tier glow (only when unlocked)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .size(70.dp)
                .blur(26.dp)
                .alpha(glow)
                .background(
                    Brush.radialGradient(listOf(tier.color.copy(alpha = 0.55f), Color.Transparent)),
                    CircleShape,
                ),
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // 3D medal — desaturated + dimmed when locked
            Image(
                painter = painterResource(tier.icon),
                contentDescription = badge.name,
                colorFilter = if (isUnlocked) null else ColorFilter.colorMatrix(
                    ColorMatrix().apply { setToSaturation(0f) },
                ),
                modifier = Modifier
                    .size(50.dp)
                    .alpha(if (isUnlocked) 1f else 0.4f),
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = badge.name,
                style = MaterialTheme.typography.labelMedium,
                color = if (isUnlocked) c.textHi else c.textLow,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )

            Spacer(Modifier.height(4.dp))

            // tier label
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(tier.color.copy(alpha = if (isUnlocked) 0.18f else 0.08f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = tier.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isUnlocked) tier.color else c.textLow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                )
            }
        }
    }
}

private data class BadgeTier(
    val label: String,
    val color: Color,
    val icon: DrawableResource,
)

/** Bronze → silver → gold (gold = apex). Common is the entry tier, Legendary the apex crown. */
private fun badgeTier(rarity: BadgeRarity): BadgeTier = when (rarity) {
    BadgeRarity.COMMON -> BadgeTier("Bronze", Color(0xFFB08D57), Res.drawable.ic3d_medal)
    BadgeRarity.RARE -> BadgeTier("Silver", Color(0xFFB8C0CC), Res.drawable.ic3d_star)
    BadgeRarity.EPIC -> BadgeTier("Gold", GoldPrimary, Res.drawable.ic3d_trophy)
    BadgeRarity.LEGENDARY -> BadgeTier("Apex", GoldBright, Res.drawable.ic3d_crown)
}
