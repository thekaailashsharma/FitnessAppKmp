package org.awi.fitness.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.awi.fitness.model.*
import org.awi.fitness.theme.GreenAccent
import org.awi.fitness.ui.components.*

@Composable
fun ChallengesScreen() {
    val navigator = LocalNavigator.currentOrThrow
    val scrollState = rememberScrollState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // Hero Section
            HeroSection()
        }
        
        item {
            // Quick Stats
            QuickStatsSection()
        }
        
        item {
            // Active Challenges
            SectionHeader(
                title = "Active Challenges",
                subtitle = "Keep the momentum going!"
            )
        }
        
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(getActiveChallenges()) { challenge ->
                    ChallengeCard(
                        challenge = challenge,
                        onClick = { navigator.push(ChallengeDetailScreen(challenge)) },
                        modifier = Modifier.width(280.dp)
                    )
                }
            }
        }
        
        item {
            // Available Challenges
            SectionHeader(
                title = "Available Challenges",
                subtitle = "Start a new journey"
            )
        }
        
        items(getAvailableChallenges()) { challenge ->
            ChallengeCard(
                challenge = challenge,
                onClick = { navigator.push(ChallengeDetailScreen(challenge)) }
            )
        }
        
        item {
            // Achievements Section
            SectionHeader(
                title = "Recent Achievements",
                subtitle = "Celebrate your wins!"
            )
        }
        
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(getRecentBadges()) { badge ->
                    BadgeCard(
                        badge = badge,
                        isUnlocked = true,
                        modifier = Modifier.width(120.dp)
                    )
                }
            }
        }
        
        item {
            // Leaderboard Section
            SectionHeader(
                title = "Leaderboard",
                subtitle = "See how you rank"
            )
        }
        
        items(getLeaderboardEntries()) { entry ->
            LeaderboardCard(entry = entry)
        }
    }
}

@Composable
private fun HeroSection() {
    val pulseAnimation by animateFloatAsState(
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GreenAccent.copy(alpha = 0.1f),
                            GreenAccent.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Daily Challenge",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Complete 30 squats today",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Progress
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "15/30",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GreenAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "squats",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Animated icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(pulseAnimation)
                        .background(
                            color = GreenAccent.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = GreenAccent,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "XP",
            value = "2,450",
            icon = Icons.Default.Star,
            color = Color(0xFFFFD700),
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Level",
            value = "12",
            icon = Icons.Default.Face,
            color = GreenAccent,
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "Streak",
            value = "7 days",
            icon = Icons.Default.Star,
            color = Color(0xFFFF5722),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

// Mock data functions
private fun getActiveChallenges(): List<Challenge> {
    return listOf(
        Challenge(
            id = "1",
            title = "30-Day Squat Challenge",
            description = "Build lower body strength with daily squats",
            type = ChallengeType.DAILY,
            category = ChallengeCategory.EXERCISE,
            difficulty = ChallengeDifficulty.BEGINNER,
            duration = 30,
            target = 30,
            unit = "squats",
            reward = Reward(xp = 100, title = "Squat Master"),
            isActive = true,
            progress = 15,
            iconName = "🏋️",
            color = 0xFF00B67A
        ),
        Challenge(
            id = "2",
            title = "10K Steps Daily",
            description = "Walk your way to better health",
            type = ChallengeType.DAILY,
            category = ChallengeCategory.STEPS,
            difficulty = ChallengeDifficulty.INTERMEDIATE,
            duration = 7,
            target = 10000,
            unit = "steps",
            reward = Reward(xp = 150, title = "Walker"),
            isActive = true,
            progress = 7500,
            iconName = "🚶",
            color = 0xFF2196F3
        )
    )
}

private fun getAvailableChallenges(): List<Challenge> {
    return listOf(
        Challenge(
            id = "3",
            title = "Plank Challenge",
            description = "Core strength building challenge",
            type = ChallengeType.WEEKLY,
            category = ChallengeCategory.STRENGTH,
            difficulty = ChallengeDifficulty.INTERMEDIATE,
            duration = 7,
            target = 60,
            unit = "seconds",
            reward = Reward(xp = 200, title = "Plank Pro"),
            isActive = false,
            progress = 0,
            iconName = "💪",
            color = 0xFFFF9800
        ),
        Challenge(
            id = "4",
            title = "Meditation Streak",
            description = "Find your inner peace daily",
            type = ChallengeType.MONTHLY,
            category = ChallengeCategory.CONSISTENCY,
            difficulty = ChallengeDifficulty.BEGINNER,
            duration = 30,
            target = 30,
            unit = "sessions",
            reward = Reward(xp = 300, title = "Zen Master"),
            isActive = false,
            progress = 0,
            iconName = "🧘",
            color = 0xFF9C27B0
        )
    )
}

private fun getRecentBadges(): List<Badge> {
    return listOf(
        Badge(
            id = "1",
            name = "First Steps",
            description = "Complete your first challenge",
            iconName = "👟",
            rarity = BadgeRarity.COMMON
        ),
        Badge(
            id = "2",
            name = "Week Warrior",
            description = "Complete 7 challenges in a week",
            iconName = "⚔️",
            rarity = BadgeRarity.RARE
        ),
        Badge(
            id = "3",
            name = "Consistency King",
            description = "30-day streak",
            iconName = "👑",
            rarity = BadgeRarity.EPIC
        )
    )
}

private fun getLeaderboardEntries(): List<LeaderboardEntry> {
    return listOf(
        LeaderboardEntry(
            userId = "1",
            username = "FitnessPro",
            xp = 5420,
            level = 15,
            rank = 1,
            streak = 45
        ),
        LeaderboardEntry(
            userId = "2",
            username = "You",
            xp = 2450,
            level = 12,
            rank = 2,
            streak = 7
        ),
        LeaderboardEntry(
            userId = "3",
            username = "GymBuddy",
            xp = 2100,
            level = 11,
            rank = 3,
            streak = 12
        ),
        LeaderboardEntry(
            userId = "4",
            username = "HealthyLife",
            xp = 1800,
            level = 10,
            rank = 4,
            streak = 5
        )
    )
}
