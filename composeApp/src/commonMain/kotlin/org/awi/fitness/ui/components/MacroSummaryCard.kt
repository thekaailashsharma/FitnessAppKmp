package org.awi.fitness.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.viewmodel.DailyMacros

/**
 * Premium glass macro-summary card: a gold calorie ring (consumed vs target, big tabular
 * numbers) flanked by colorful macro stat rows. Theme-aware via [TajlyTheme.colors].
 */
@Composable
fun MacroSummaryCard(
    macros: DailyMacros,
    modifier: Modifier = Modifier
) {
    val c = TajlyTheme.colors
    val calProgress = if (macros.targetCalories > 0)
        (macros.consumedCalories.toFloat() / macros.targetCalories).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = calProgress,
        animationSpec = tween(durationMillis = 900),
        label = "calRing"
    )
    val calPercent = (calProgress * 100).toInt()
    val remaining = (macros.targetCalories - macros.consumedCalories).coerceAtLeast(0)

    GlassCard(modifier = modifier.fillMaxWidth()) {
        // Calories section glow (blue accent — "calories" in the design language)
        Box(
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp)
                .blur(34.dp)
                .background(
                    Brush.radialGradient(listOf(Tajly.Blue.copy(alpha = 0.35f), Color.Transparent))
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Gold calorie ring with big tabular number
            StatRing(
                progress = animatedProgress,
                diameter = 104.dp,
                strokeWidth = 11.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$calPercent",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = c.textHi,
                        fontSize = 26.sp
                    )
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textLow
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${macros.consumedCalories}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldBright
                    )
                    Text(
                        text = " / ${macros.targetCalories} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textMid,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$remaining kcal remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textLow
                )

                Spacer(Modifier.height(14.dp))

                MacroStat(
                    label = "Protein",
                    consumed = macros.consumedProtein,
                    target = macros.targetProtein,
                    color = Tajly.Green
                )
                Spacer(Modifier.height(8.dp))
                MacroStat(
                    label = "Carbs",
                    consumed = macros.consumedCarbs,
                    target = macros.targetCarbs,
                    color = Tajly.Blue
                )
                Spacer(Modifier.height(8.dp))
                MacroStat(
                    label = "Fat",
                    consumed = macros.consumedFat,
                    target = macros.targetFat,
                    color = Tajly.Coral
                )
            }
        }
    }
}

@Composable
private fun MacroStat(
    label: String,
    consumed: Int,
    target: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val c = TajlyTheme.colors
    val progress = if (target > 0) (consumed.toFloat() / target).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 700),
        label = "macro_$label"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textMid
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$consumed",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi
                )
                Text(
                    text = " / ${target}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textLow
                )
            }
        }
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(c.hairStrong)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}
