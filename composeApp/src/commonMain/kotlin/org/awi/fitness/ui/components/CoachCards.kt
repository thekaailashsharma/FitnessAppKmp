package org.awi.fitness.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.CoachCard
import org.awi.fitness.model.CoachCardType
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.TajlyTheme

/**
 * Renders a coach [CoachCard] as a native, on-brand card that reads REAL user data at render
 * time. Emoji glyphs (not vector icons) keep it dependency-light and playful. Cards with an
 * [CoachCard.action] surface a CTA that routes via [onAction] (e.g. "workout", "meals").
 */
@Composable
fun CoachCardView(
    card: CoachCard,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = remember { UserSettings.getInstance() }
    when (card.type) {
        CoachCardType.STREAK -> {
            val streak = s.currentStreak
            val best = s.longestStreak.coerceAtLeast(streak)
            RingCard(
                emoji = "🔥",
                progress = if (best > 0) streak.toFloat() / best else 0f,
                big = "$streak",
                unit = if (streak == 1) "day streak" else "day streak",
                sub = if (best > streak) "Your best is $best — go beat it" else "This is your best ever",
                modifier = modifier,
            )
        }
        CoachCardType.LEVEL -> {
            val lvl = s.userLevel
            val into = s.totalXp % 500
            RingCard(
                emoji = "⚡",
                progress = into / 500f,
                big = "Lv $lvl",
                unit = "level",
                sub = "${500 - into} XP to Level ${lvl + 1}",
                modifier = modifier,
            )
        }
        CoachCardType.PROGRESS, CoachCardType.WEIGHT_TREND -> WeightCard(s, modifier)
        CoachCardType.WEEK_ACTIVITY -> WeekCard(s, modifier)
        CoachCardType.QUICK_ACTIONS -> QuickActionsCard(onAction, modifier)
        CoachCardType.WORKOUT_SUGGESTION -> ActionCard(
            emoji = "🏋️",
            title = card.title.ifBlank { "Today's session" },
            subtitle = card.subtitle.ifBlank { "A focused workout, tuned to your goal" },
            cta = card.cta.ifBlank { "Start workout" },
            action = card.action.ifBlank { "workout" }, onAction = onAction, modifier = modifier,
        )
        CoachCardType.MEAL_SUGGESTION -> ActionCard(
            emoji = "🥗",
            title = card.title.ifBlank { "Fuel up right" },
            subtitle = card.subtitle.ifBlank { "See meal ideas built around your goal" },
            cta = card.cta.ifBlank { "Open meals" },
            action = card.action.ifBlank { "meals" }, onAction = onAction, modifier = modifier,
        )
        CoachCardType.CHALLENGE -> ActionCard(
            emoji = "🏆",
            title = card.title.ifBlank { "Take on a challenge" },
            subtitle = card.subtitle.ifBlank { "A little competition keeps it fun" },
            cta = card.cta.ifBlank { "View challenges" },
            action = card.action.ifBlank { "challenges" }, onAction = onAction, modifier = modifier,
        )
        CoachCardType.HYDRATION -> InfoCard(
            emoji = "💧",
            title = card.title.ifBlank { "Hydration check" },
            body = card.subtitle.ifBlank { "A glass of water now keeps your energy steady this afternoon." },
            modifier = modifier,
        )
        CoachCardType.BREATHE -> InfoCard(
            emoji = "🌬️",
            title = card.title.ifBlank { "One slow breath" },
            body = card.subtitle.ifBlank { "In for 4, hold for 4, out for 6. Three rounds. Notice the settle." },
            modifier = modifier,
        )
        CoachCardType.TIP -> InfoCard(
            emoji = "💡",
            title = card.title.ifBlank { "Coach tip" },
            body = card.subtitle.ifBlank { "Consistency beats intensity. Small and repeatable wins." },
            modifier = modifier,
        )
        CoachCardType.GOAL -> InfoCard(
            emoji = "🎯",
            title = "Your goal",
            body = card.subtitle.ifBlank { goalWords(s.fitnessGoal) },
            modifier = modifier,
        )
        CoachCardType.MOTIVATION -> InfoCard(
            emoji = "✨",
            title = card.title.ifBlank { "For today" },
            body = card.subtitle.ifBlank { "The only bad workout is the one that didn't happen." },
            modifier = modifier,
        )
        CoachCardType.CELEBRATION -> InfoCard(
            emoji = "🎉",
            title = card.title.ifBlank { "That's a win" },
            body = card.subtitle.ifBlank { "You showed up — that's the whole game. Proud of you." },
            accent = true,
            modifier = modifier,
        )
    }
}

private fun goalWords(g: String): String = when (g.uppercase()) {
    "WEIGHT_LOSS" -> "Lose weight — lighter, stronger, steadier."
    "MUSCLE_GAIN" -> "Build muscle — get stronger and more defined."
    "ENDURANCE" -> "Build endurance — move more, every day."
    "FLEXIBILITY" -> "Move freely — mobility and calm."
    else -> "Get fitter — one steady step at a time."
}

@Composable
private fun EmojiChip(emoji: String, accent: Boolean = false) {
    val c = TajlyTheme.colors
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (accent) GoldPrimary.copy(alpha = 0.22f) else c.s2),
        contentAlignment = Alignment.Center,
    ) { Text(emoji, fontSize = 22.sp) }
}

@Composable
private fun RingCard(emoji: String, progress: Float, big: String, unit: String, sub: String, modifier: Modifier) {
    val c = TajlyTheme.colors
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StatRing(progress = progress.coerceIn(0f, 1f), diameter = 56.dp, strokeWidth = 6.dp) {
                Text(emoji, fontSize = 20.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(big, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = c.textHi)
                    Text(unit, style = MaterialTheme.typography.labelMedium, color = c.textLow, modifier = Modifier.padding(bottom = 4.dp))
                }
                Text(sub, style = MaterialTheme.typography.bodySmall, color = c.textMid, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ActionCard(emoji: String, title: String, subtitle: String, cta: String, action: String, onAction: (String) -> Unit, modifier: Modifier) {
    val c = TajlyTheme.colors
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EmojiChip(emoji, accent = true)
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.textHi)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textMid, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            GoldButton(text = cta, onClick = { onAction(action) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun InfoCard(emoji: String, title: String, body: String, accent: Boolean = false, modifier: Modifier = Modifier) {
    val c = TajlyTheme.colors
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EmojiChip(emoji, accent = accent)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (accent) GoldBright else c.textHi)
                Text(body, style = MaterialTheme.typography.bodySmall, color = c.textMid)
            }
        }
    }
}

@Composable
private fun WeightCard(s: UserSettings, modifier: Modifier) {
    val c = TajlyTheme.colors
    val weighIns = s.weighIns.value
    val start = weighIns.firstOrNull()?.weight ?: s.profileWeightKg.takeIf { it > 0f }
    val current = weighIns.lastOrNull()?.weight ?: s.profileWeightKg.takeIf { it > 0f }
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EmojiChip("📈", accent = true)
            Column(modifier = Modifier.weight(1f)) {
                Text("Your progress", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.textHi)
                if (start != null && current != null) {
                    val delta = start - current
                    val txt = when {
                        delta >= 0.5f -> "Down ${delta.toInt()}kg since you started 💪"
                        delta <= -0.5f -> "Up ${(-delta).toInt()}kg of work in"
                        else -> "Holding steady — consistency is the win"
                    }
                    Text(txt, style = MaterialTheme.typography.bodySmall, color = c.textMid)
                    Text("Start ${start.toInt()}kg · Now ${current.toInt()}kg", style = MaterialTheme.typography.labelSmall, color = c.textLow)
                } else {
                    Text("Log a weigh-in to see your trend here", style = MaterialTheme.typography.bodySmall, color = c.textMid)
                }
            }
        }
    }
}

@Composable
private fun WeekCard(s: UserSettings, modifier: Modifier) {
    val c = TajlyTheme.colors
    val done = s.currentStreak.coerceIn(0, 7)
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("This week", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.textHi)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val days = listOf("M", "T", "W", "T", "F", "S", "S")
                days.forEachIndexed { i, d ->
                    val active = i < done
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier.size(24.dp).clip(CircleShape)
                                .background(if (active) GoldPrimary else c.s2),
                            contentAlignment = Alignment.Center,
                        ) { if (active) Text("✓", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Black) }
                        Text(d, style = MaterialTheme.typography.labelSmall, color = c.textLow)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsCard(onAction: (String) -> Unit, modifier: Modifier) {
    val c = TajlyTheme.colors
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Quick actions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.textHi)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassChip(text = "🏋️ Workout", selected = false, onClick = { onAction("workout") })
                GlassChip(text = "🥗 Meals", selected = false, onClick = { onAction("meals") })
                GlassChip(text = "🏆 Challenges", selected = false, onClick = { onAction("challenges") })
            }
        }
    }
}
