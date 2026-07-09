package org.awi.fitness.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldDeep
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.Tajly

/** Gold progress ring with a centered content slot. progress in 0f..1f. */
@Composable
fun StatRing(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    content: @Composable () -> Unit = {},
) {
    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(diameter)) {
            val sw = strokeWidth.toPx()
            drawArc(
                color = Color.White.copy(alpha = 0.06f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = sw, cap = StrokeCap.Round),
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(GoldDeep, GoldPrimary, GoldBright, Tajly.Champagne, GoldDeep)),
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                style = Stroke(width = sw, cap = StrokeCap.Round),
            )
        }
        content()
    }
}
