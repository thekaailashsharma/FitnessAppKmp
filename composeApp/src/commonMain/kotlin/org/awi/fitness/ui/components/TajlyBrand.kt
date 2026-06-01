package org.awi.fitness.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.tajly_logo
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

val TajlyGold  = Color(0xFFC9A84C)   // Antique gold — matches GoldPrimary
val TajlyGoldBright = Color(0xFFE8C06A)
val TajlyDark  = Color(0xFF0C0B09)
// Kept for any legacy references
val TajlyGreen = TajlyGold

@Composable
fun TajlyLogoMark(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 120.dp,
    animated: Boolean = false,
) {
    val scale = if (animated) {
        val infinite = rememberInfiniteTransition(label = "logoPulse")
        val s by infinite.animateFloat(
            initialValue = 1f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "scale"
        )
        s
    } else {
        1f
    }

    Image(
        painter = painterResource(Res.drawable.tajly_logo),
        contentDescription = "TAJLY",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(RoundedCornerShape(size * 0.22f))
    )
}

@Composable
fun TajlyHandwritingAnimation(
    modifier: Modifier = Modifier,
    text: String = "TAJLY",
    onAnimationComplete: () -> Unit = {},
) {
    var started by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150)
        started = true
        delay(3200)
        onAnimationComplete()
    }

    val reveal by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(2800, easing = FastOutSlowInEasing),
        label = "reveal"
    )
    val underline by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(1200, delayMillis = 1800, easing = FastOutSlowInEasing),
        label = "underline"
    )
    val shimmer by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val letterTilts = remember(text) {
        text.mapIndexed { i, _ -> (-5f + i * 2.2f) }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.drawWithContent {
                    clipRect(right = size.width * reveal) {
                        this@drawWithContent.drawContent()
                    }
                }
            ) {
                text.forEachIndexed { index, char ->
                    val letterRevealThreshold = (index + 1) / text.length.toFloat()
                    val letterVisible = reveal >= letterRevealThreshold * 0.85f
                    val bounce by animateFloatAsState(
                        targetValue = if (letterVisible) 0f else 28f,
                        animationSpec = spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMediumLow),
                        label = "bounce$index"
                    )
                    val letterAlpha by animateFloatAsState(
                        targetValue = if (letterVisible) 1f else 0f,
                        animationSpec = tween(180),
                        label = "alpha$index"
                    )

                    Text(
                        text = char.toString(),
                        style = TextStyle(
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            letterSpacing = 3.sp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White,
                                    TajlyGoldBright,
                                    TajlyGold
                                )
                            )
                        ),
                        modifier = Modifier
                            .offset(y = bounce.dp)
                            .graphicsLayer {
                                alpha = letterAlpha
                                rotationZ = letterTilts[index]
                            }
                    )
                }
            }

            if (reveal in 0.02f..0.98f) {
                Canvas(
                    modifier = Modifier
                        .width(240.dp)
                        .height(72.dp)
                ) {
                    val x = size.width * reveal
                    val y = size.height * 0.55f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.White, TajlyGold, Color.Transparent),
                            center = Offset(x, y),
                            radius = 22f
                        )
                    )
                    drawCircle(color = Color.White, radius = 5f, center = Offset(x, y))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Canvas(
            modifier = Modifier
                .width(220.dp)
                .height(14.dp)
        ) {
            val path = Path().apply {
                moveTo(0f, size.height * 0.65f)
                quadraticTo(size.width * 0.3f, size.height * 0.05f, size.width * 0.55f, size.height * 0.55f)
                quadraticTo(size.width * 0.8f, size.height * 0.98f, size.width * underline, size.height * 0.42f)
            }
            drawPath(
                path = path,
                color = TajlyGold.copy(alpha = 0.9f),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.4f * shimmer),
                radius = 7f,
                center = Offset(size.width * reveal.coerceIn(0f, 1f) * 0.92f, size.height * 0.48f)
            )
        }
    }
}

@Composable
fun CelebrationBurst(active: Boolean, modifier: Modifier = Modifier) {
    if (!active) return
    val particles = remember {
        List(60) {
            BurstParticle(
                angle = it * (360f / 60f) + (it % 7) * 3f,
                distance = 80f + (it % 5) * 35f,
                size = 3f + (it % 4) * 2f,
                color = when (it % 4) {
                    0 -> TajlyGold
                    1 -> TajlyGoldBright
                    2 -> Color.White
                    else -> Color(0xFFD4A84B)
                }
            )
        }
    }
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(2000, easing = FastOutSlowInEasing),
        label = "burst"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height * 0.38f)
        particles.forEach { p ->
            val rad = p.angle * PI.toFloat() / 180f
            val dist = p.distance * progress
            drawCircle(
                color = p.color.copy(alpha = 1f - progress * 0.75f),
                radius = p.size,
                center = Offset(
                    center.x + cos(rad) * dist,
                    center.y + sin(rad) * dist
                )
            )
        }
        drawCircle(
            color = TajlyGold.copy(alpha = (1f - progress) * 0.45f),
            radius = 40f + progress * 200f,
            center = center,
            style = Stroke(width = 4f)
        )
    }
}

private data class BurstParticle(
    val angle: Float,
    val distance: Float,
    val size: Float,
    val color: Color,
)
