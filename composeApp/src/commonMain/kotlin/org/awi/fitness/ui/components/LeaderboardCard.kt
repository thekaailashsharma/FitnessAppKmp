package org.awi.fitness.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.awi.fitness.model.LeaderboardEntry
import org.awi.fitness.theme.GreenAccent

@Composable
fun LeaderboardCard(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier
) {
    val slideInAnimation by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 600,
            delayMillis = entry.rank * 100,
            easing = EaseOutCubic
        ),
        label = "slideIn"
    )
    
    val isCurrentUser = entry.rank <= 3 // Mock current user detection
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentUser) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank indicator
            RankIndicator(rank = entry.rank)
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Avatar
            AvatarSection(
                avatar = entry.avatar,
                username = entry.username,
                isCurrentUser = isCurrentUser
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Stats
            StatsSection(
                xp = entry.xp,
                level = entry.level,
                streak = entry.streak
            )
        }
    }
}

@Composable
private fun RankIndicator(
    rank: Int,
    modifier: Modifier = Modifier
) {
    val (color, icon) = when (rank) {
        1 -> Color(0xFFFFD700) to "🥇"
        2 -> Color(0xFFC0C0C0) to "🥈"
        3 -> Color(0xFFCD7F32) to "🥉"
        else -> MaterialTheme.colorScheme.outline to "#$rank"
    }
    
    Box(
        modifier = modifier
            .size(40.dp)
            .background(
                color = color.copy(alpha = 0.2f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (rank <= 3) {
            Text(
                text = icon,
                fontSize = 20.sp
            )
        } else {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AvatarSection(
    avatar: String?,
    username: String,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GreenAccent,
                            GreenAccent.copy(alpha = 0.7f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = username.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Username
        Column {
            Text(
                text = username,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Medium
            )
            
            if (isCurrentUser) {
                Text(
                    text = "You",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StatsSection(
    xp: Int,
    level: Int,
    streak: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        // XP and Level
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$xp XP",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .background(
                        color = GreenAccent.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Lv.$level",
                    style = MaterialTheme.typography.labelSmall,
                    color = GreenAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Streak
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔥",
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "$streak days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
