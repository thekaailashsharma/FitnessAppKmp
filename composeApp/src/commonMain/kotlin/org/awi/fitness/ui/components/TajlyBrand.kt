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
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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

/**
 * District-style draw-on TAJLY logo: construction lines draw in, the chunky
 * custom letterforms stroke on (via PathMeasure), then fill with solid gold.
 * Drives [onAnimationComplete] after the sequence (same contract the splash routing expects).
 * Custom original glyphs — not a trace of any third-party logo.
 */
@Composable
fun TajlyDrawOnLogo(
    modifier: Modifier = Modifier,
    onAnimationComplete: () -> Unit = {},
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(120)
        started = true
        delay(2900)
        onAnimationComplete()
    }
    val lines by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing), label = "cl",
    )
    val draw by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(1400, delayMillis = 500, easing = FastOutSlowInEasing), label = "draw",
    )
    val fill by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(700, delayMillis = 1750, easing = FastOutSlowInEasing), label = "fill",
    )

    val goldStroke = Brush.linearGradient(listOf(Color(0xFFF4E3B0), TajlyGoldBright, TajlyGold))
    val goldFill = Brush.linearGradient(listOf(TajlyGoldBright, TajlyGold, Color(0xFFA8873A)))

    Canvas(modifier = modifier.fillMaxWidth().height(168.dp)) {
        val sx = size.width / 760f
        val sy = size.height / 220f
        fun bx(v: Float) = v * sx
        fun by(v: Float) = v * sy

        // construction lines (draw in first)
        val cl = arrayOf(
            floatArrayOf(118f, -34f, 150f, 246f),
            floatArrayOf(38f, 118f, 744f, 118f),
            floatArrayOf(-24f, 150f, 788f, 140f),
            floatArrayOf(430f, -44f, 468f, 254f),
            floatArrayOf(696f, -20f, 660f, 216f),
            floatArrayOf(248f, 254f, 222f, 16f),
            floatArrayOf(566f, 248f, 602f, 24f),
        )
        cl.forEach { a ->
            val s = Offset(bx(a[0]), by(a[1]))
            val e = Offset(bx(a[2]), by(a[3]))
            drawLine(
                color = TajlyGold.copy(alpha = 0.45f * lines),
                start = s,
                end = Offset(s.x + (e.x - s.x) * lines, s.y + (e.y - s.y) * lines),
                strokeWidth = 1.5f,
            )
        }

        // chunky letterforms (custom)
        val letters = buildList {
            add(Path().apply {
                moveTo(bx(52f), by(46f)); lineTo(bx(196f), by(46f)); lineTo(bx(196f), by(80f))
                lineTo(bx(142f), by(80f)); lineTo(bx(142f), by(172f)); lineTo(bx(106f), by(172f))
                lineTo(bx(106f), by(80f)); lineTo(bx(52f), by(80f)); close()
            })
            add(Path().apply {
                fillType = PathFillType.EvenOdd
                moveTo(bx(208f), by(172f)); lineTo(bx(262f), by(46f)); lineTo(bx(304f), by(46f))
                lineTo(bx(358f), by(172f)); lineTo(bx(320f), by(172f)); lineTo(bx(309f), by(140f))
                lineTo(bx(257f), by(140f)); lineTo(bx(246f), by(172f)); close()
                moveTo(bx(283f), by(86f)); lineTo(bx(300f), by(128f)); lineTo(bx(266f), by(128f)); close()
            })
            add(Path().apply {
                moveTo(bx(430f), by(46f)); lineTo(bx(464f), by(46f)); lineTo(bx(464f), by(140f))
                quadraticTo(bx(464f), by(172f), bx(430f), by(172f)); lineTo(bx(404f), by(172f))
                quadraticTo(bx(376f), by(172f), bx(376f), by(148f)); lineTo(bx(376f), by(134f))
                lineTo(bx(406f), by(134f)); lineTo(bx(406f), by(146f))
                quadraticTo(bx(406f), by(150f), bx(412f), by(150f)); lineTo(bx(424f), by(150f))
                quadraticTo(bx(434f), by(150f), bx(434f), by(140f)); lineTo(bx(434f), by(46f)); close()
            })
            add(Path().apply {
                moveTo(bx(496f), by(46f)); lineTo(bx(530f), by(46f)); lineTo(bx(530f), by(140f))
                lineTo(bx(592f), by(140f)); lineTo(bx(592f), by(172f)); lineTo(bx(496f), by(172f)); close()
            })
            add(Path().apply {
                moveTo(bx(600f), by(46f)); lineTo(bx(638f), by(46f)); lineTo(bx(670f), by(104f))
                lineTo(bx(702f), by(46f)); lineTo(bx(740f), by(46f)); lineTo(bx(688f), by(122f))
                lineTo(bx(688f), by(172f)); lineTo(bx(652f), by(172f)); lineTo(bx(652f), by(122f)); close()
            })
        }

        // fill (fades in under the outline)
        if (fill > 0f) {
            letters.forEach { drawPath(it, brush = goldFill, alpha = (fill * 0.92f).coerceIn(0f, 1f)) }
        }
        // outline draws on
        val pm = PathMeasure()
        letters.forEach { path ->
            pm.setPath(path, false)
            val seg = Path()
            pm.getSegment(0f, pm.length * draw, seg, true)
            drawPath(seg, brush = goldStroke, style = Stroke(width = 2.6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}
