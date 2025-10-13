package org.awi.fitness.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.awi.fitness.model.Badge
import org.awi.fitness.model.BadgeRarity

@Composable
fun BadgeCard(
    badge: Badge,
    isUnlocked: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    val glowAnimation by animateFloatAsState(
        targetValue = if (isUnlocked) 1f else 0.3f,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "glow"
    )
    
    val rarityColor = getRarityColor(badge.rarity)
    val alpha = if (isUnlocked) 1f else 0.5f
    
    Card(
        modifier = modifier
            .size(120.dp)
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = alpha)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUnlocked) 6.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            rarityColor.copy(alpha = 0.1f * glowAnimation),
                            Color.Transparent
                        ),
                        radius = 80f
                    )
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Badge icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = rarityColor.copy(alpha = alpha),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = rarityColor.copy(alpha = alpha * 0.8f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge.iconName,
                        fontSize = 20.sp,
                        color = Color.White.copy(alpha = alpha)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Badge name
                Text(
                    text = badge.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Rarity indicator
                RarityIndicator(
                    rarity = badge.rarity,
                    alpha = alpha
                )
            }
        }
    }
}

@Composable
private fun RarityIndicator(
    rarity: BadgeRarity,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (rarity) {
        BadgeRarity.COMMON -> Color(0xFF9E9E9E) to "Common"
        BadgeRarity.RARE -> Color(0xFF2196F3) to "Rare"
        BadgeRarity.EPIC -> Color(0xFF9C27B0) to "Epic"
        BadgeRarity.LEGENDARY -> Color(0xFFFFD700) to "Legendary"
    }
    
    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.2f * alpha),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = alpha),
            fontWeight = FontWeight.Bold,
            fontSize = 8.sp
        )
    }
}

private fun getRarityColor(rarity: BadgeRarity): Color {
    return when (rarity) {
        BadgeRarity.COMMON -> Color(0xFF9E9E9E)
        BadgeRarity.RARE -> Color(0xFF2196F3)
        BadgeRarity.EPIC -> Color(0xFF9C27B0)
        BadgeRarity.LEGENDARY -> Color(0xFFFFD700)
    }
}
