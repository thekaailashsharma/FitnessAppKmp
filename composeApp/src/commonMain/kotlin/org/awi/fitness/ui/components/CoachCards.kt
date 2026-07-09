package org.awi.fitness.ui.components
import org.awi.fitness.data.tr
import org.awi.fitness.data.StringKey
import org.awi.fitness.viewmodel.LocalLanguageViewModel

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
    val lang = LocalLanguageViewModel.current
    when (card.type) {
        CoachCardType.STREAK -> {
            val streak = s.currentStreak
            val best = s.longestStreak.coerceAtLeast(streak)
            RingCard(
                emoji = "🔥",
                progress = if (best > 0) streak.toFloat() / best else 0f,
                big = "$streak",
                unit = lang.getString(StringKey.CC_DAY_STREAK),
                sub = if (best > streak) "${lang.getString(StringKey.CC_YOUR_BEST_PRE)} $best ${lang.getString(StringKey.CC_YOUR_BEST_POST)}" else lang.getString(StringKey.CC_BEST_EVER),
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
                unit = lang.getString(StringKey.CC_LEVEL_UNIT),
                sub = "${500 - into} ${lang.getString(StringKey.CC_XP_TO_LEVEL)} ${lvl + 1}",
                modifier = modifier,
            )
        }
        CoachCardType.PROGRESS, CoachCardType.WEIGHT_TREND -> WeightCard(s, modifier)
        CoachCardType.WEEK_ACTIVITY -> WeekCard(s, modifier)
        CoachCardType.QUICK_ACTIONS -> QuickActionsCard(onAction, modifier)
        CoachCardType.WORKOUT_SUGGESTION -> ActionCard(
            emoji = "🏋️",
            title = card.title.ifBlank { lang.getString(StringKey.CC_WORKOUT_TITLE) },
            subtitle = card.subtitle.ifBlank { lang.getString(StringKey.CC_WORKOUT_SUB) },
            cta = card.cta.ifBlank { lang.getString(StringKey.START_WORKOUT) },
            action = card.action.ifBlank { "workout" }, onAction = onAction, modifier = modifier,
        )
        CoachCardType.MEAL_SUGGESTION -> ActionCard(
            emoji = "🥗",
            title = card.title.ifBlank { lang.getString(StringKey.CC_MEAL_TITLE) },
            subtitle = card.subtitle.ifBlank { lang.getString(StringKey.CC_MEAL_SUB) },
            cta = card.cta.ifBlank { lang.getString(StringKey.CC_OPEN_MEALS) },
            action = card.action.ifBlank { "meals" }, onAction = onAction, modifier = modifier,
        )
        CoachCardType.CHALLENGE -> ActionCard(
            emoji = "🏆",
            title = card.title.ifBlank { lang.getString(StringKey.CC_CHAL_TITLE) },
            subtitle = card.subtitle.ifBlank { lang.getString(StringKey.CC_CHAL_SUB) },
            cta = card.cta.ifBlank { lang.getString(StringKey.CC_VIEW_CHALLENGES) },
            action = card.action.ifBlank { "challenges" }, onAction = onAction, modifier = modifier,
        )
        CoachCardType.HYDRATION -> InfoCard(
            emoji = "💧",
            title = card.title.ifBlank { lang.getString(StringKey.CC_HYDRATION_TITLE) },
            body = card.subtitle.ifBlank { lang.getString(StringKey.CC_HYDRATION_BODY) },
            modifier = modifier,
        )
        CoachCardType.BREATHE -> InfoCard(
            emoji = "🌬️",
            title = card.title.ifBlank { lang.getString(StringKey.CC_BREATHE_TITLE) },
            body = card.subtitle.ifBlank { lang.getString(StringKey.CC_BREATHE_BODY) },
            modifier = modifier,
        )
        CoachCardType.TIP -> InfoCard(
            emoji = "💡",
            title = card.title.ifBlank { lang.getString(StringKey.CC_TIP_TITLE) },
            body = card.subtitle.ifBlank { lang.getString(StringKey.CC_TIP_BODY) },
            modifier = modifier,
        )
        CoachCardType.GOAL -> InfoCard(
            emoji = "🎯",
            title = lang.getString(StringKey.CC_YOUR_GOAL),
            body = card.subtitle.ifBlank { goalWords(s.fitnessGoal) },
            modifier = modifier,
        )
        CoachCardType.MOTIVATION -> InfoCard(
            emoji = "✨",
            title = card.title.ifBlank { lang.getString(StringKey.CC_MOTIVATION_TITLE) },
            body = card.subtitle.ifBlank { lang.getString(StringKey.CC_MOTIVATION_BODY) },
            modifier = modifier,
        )
        CoachCardType.CELEBRATION -> InfoCard(
            emoji = "🎉",
            title = card.title.ifBlank { lang.getString(StringKey.CC_CELEBRATION_TITLE) },
            body = card.subtitle.ifBlank { lang.getString(StringKey.CC_CELEBRATION_BODY) },
            accent = true,
            modifier = modifier,
        )
    }
}

private fun goalWords(g: String): String = when (g.uppercase()) {
    "WEIGHT_LOSS" -> tr(StringKey.CC_GOAL_WEIGHT_LOSS)
    "MUSCLE_GAIN" -> tr(StringKey.CC_GOAL_MUSCLE)
    "ENDURANCE" -> tr(StringKey.CC_GOAL_ENDURANCE)
    "FLEXIBILITY" -> tr(StringKey.CC_GOAL_FLEXIBILITY)
    else -> tr(StringKey.CC_GOAL_GENERIC)
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
    val lang = LocalLanguageViewModel.current
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
                Text(lang.getString(StringKey.CC_YOUR_PROGRESS), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.textHi)
                if (start != null && current != null) {
                    val delta = start - current
                    val txt = when {
                        delta >= 0.5f -> "${lang.getString(StringKey.CC_DOWN_PRE)} ${delta.toInt()}kg ${lang.getString(StringKey.CC_SINCE_STARTED)} 💪"
                        delta <= -0.5f -> "${lang.getString(StringKey.CC_UP_PRE)} ${(-delta).toInt()}kg ${lang.getString(StringKey.CC_OF_WORK_IN)}"
                        else -> lang.getString(StringKey.CC_HOLDING_STEADY)
                    }
                    Text(txt, style = MaterialTheme.typography.bodySmall, color = c.textMid)
                    Text("${lang.getString(StringKey.CC_START_LABEL)} ${start.toInt()}kg · ${lang.getString(StringKey.CC_NOW_LABEL)} ${current.toInt()}kg", style = MaterialTheme.typography.labelSmall, color = c.textLow)
                } else {
                    Text(lang.getString(StringKey.CC_LOG_WEIGH_IN), style = MaterialTheme.typography.bodySmall, color = c.textMid)
                }
            }
        }
    }
}

@Composable
private fun WeekCard(s: UserSettings, modifier: Modifier) {
    val c = TajlyTheme.colors
    val lang = LocalLanguageViewModel.current
    val done = s.currentStreak.coerceIn(0, 7)
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(lang.getString(StringKey.HOME_FILTER_THIS_WEEK), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.textHi)
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
    val lang = LocalLanguageViewModel.current
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(lang.getString(StringKey.CC_QUICK_ACTIONS), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.textHi)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassChip(text = "🏋️ ${lang.getString(StringKey.WORKOUTS)}", selected = false, onClick = { onAction("workout") })
                GlassChip(text = "🥗 ${lang.getString(StringKey.MEALS)}", selected = false, onClick = { onAction("meals") })
                GlassChip(text = "🏆 ${lang.getString(StringKey.CHALLENGES)}", selected = false, onClick = { onAction("challenges") })
            }
        }
    }
}
