package org.awi.fitness.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.Achievement
import org.awi.fitness.model.AchievementCatalog
import org.awi.fitness.model.ConversationTrigger
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.BadgeCard
import org.awi.fitness.ui.components.DailyCheckInBanner
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.screens.avatar.AvatarScreen

/**
 * Achievements — the trophy room. Lists the user's earned + locked badges (real,
 * milestone-driven) on the app's premium AuroraBackground + glass style. No constructor
 * args so it can be pushed from anywhere (e.g. a Profile entry row).
 */
class AchievementsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val c = TajlyTheme.colors
        val userSettings = remember { UserSettings.getInstance() }
        val earned by userSettings.earnedBadgeIdsFlow.collectAsState()

        val level = userSettings.userLevel
        val totalXp = userSettings.totalXp
        val streak = userSettings.currentStreak
        val checkedInToday = userSettings.isCheckInCompletedToday()

        val achievements = AchievementCatalog.all
        val earnedCount = achievements.count { it.badge.id in earned }

        AuroraBackground {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = TablerIcons.ArrowLeft,
                                contentDescription = "Back",
                                tint = c.textHi,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Achievements",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = c.textHi,
                        )
                    }
                }

                // Stats header — real level / XP / streak.
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth(), goldTint = true) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            StatCell("Level", level.toString())
                            StatCell("Total XP", totalXp.toString())
                            StatCell("Streak", "$streak d")
                            StatCell("Badges", "$earnedCount/${achievements.size}")
                        }
                    }
                }

                // Daily check-in prompt (grants streak + XP) — only when not done today.
                if (!checkedInToday) {
                    item {
                        DailyCheckInBanner(
                            onClick = {
                                navigator.push(AvatarScreen(ConversationTrigger.DAILY_CHECKIN))
                            },
                        )
                    }
                }

                item {
                    Text(
                        text = "Badges",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi,
                    )
                }

                // Grid of badges (2 per row), earned in full color, locked desaturated.
                items(achievements.chunked(2)) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        rowItems.forEach { achievement ->
                            AchievementColumn(
                                achievement = achievement,
                                isUnlocked = achievement.badge.id in earned,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun StatCell(label: String, value: String) {
    val c = TajlyTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = GoldBright,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = c.textMid,
        )
    }
}

@Composable
private fun AchievementColumn(
    achievement: Achievement,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier,
) {
    val c = TajlyTheme.colors
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BadgeCard(badge = achievement.badge, isUnlocked = isUnlocked)
        Text(
            text = if (isUnlocked) achievement.badge.description else achievement.requirement,
            style = MaterialTheme.typography.labelSmall,
            color = if (isUnlocked) c.textMid else c.textLow,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}
