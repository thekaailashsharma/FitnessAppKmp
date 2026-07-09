package org.awi.fitness.ui.screens.community

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.Users
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.avatar1
import fitnessappkmp.composeapp.generated.resources.avatar2
import fitnessappkmp.composeapp.generated.resources.avatar3
import fitnessappkmp.composeapp.generated.resources.avatar4
import fitnessappkmp.composeapp.generated.resources.avatar5
import fitnessappkmp.composeapp.generated.resources.avatar6
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.ui.components.LottieAnim
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Tajly Community welcome — a living member "constellation". A central gold TAJLY hub gently
 * breathes while a ring of member avatars floats around it, each tethered to the hub (and a
 * few to one another) by hairline gold links that draw in once. It conveys "a community you're
 * joining", not an empty room.
 *
 * The graphic always renders a full mesh: when real member avatar urls are supplied they fill
 * the front nodes; the remaining nodes are drawn with the app's own avatar illustrations /
 * initials as a decorative welcome graphic (no fabricated identities are claimed). Shown once
 * on first community visit and always reachable from the feed's ⓘ button.
 */
class CommunityIntroScreen(private val avatarUrls: List<String>) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val c = TajlyTheme.colors

        // Real members enrich the front nodes; cap so the ring stays legible.
        val urls = remember(avatarUrls) { avatarUrls.filter { it.isNotBlank() }.take(7) }

        AuroraBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(24.dp))

                // ── The living network ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Constellation(urls = urls, modifier = Modifier.fillMaxSize())
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Move together",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Join a community that shows up with you — cheer each other on, share every win.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = c.textMid,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(28.dp))
                GoldButton(
                    text = "Enter the community",
                    onClick = { navigator.pop() },
                )
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

/** One node in the mesh: real avatar url, an app avatar illustration, or an initials disc. */
private data class MeshNode(
    val url: String?,
    val avatar: androidx.compose.ui.graphics.painter.Painter?,
    val initial: String,
    val sizeDp: Dp,
    val radiusFactor: Float,
    val angleJitter: Float,
)

/**
 * The constellation: a breathing gold hub, member avatars scattered on a ring, and hairline
 * gold links that draw in once. Every node's live position (base ring point + gentle float) is
 * computed in composition, so the Canvas links and the avatar Boxes stay perfectly in sync.
 */
@Composable
private fun Constellation(
    urls: List<String>,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "constellation")
    // Global phase drives each node's gentle, continuous float.
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase",
    )
    // Slow breathing of the hub.
    val breath by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath",
    )
    // One-time link + avatar draw-in.
    val draw = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        draw.animateTo(1f, tween(1100, easing = FastOutSlowInEasing))
    }

    // Decorative fallback content (app's own avatar illustrations + initials) so the mesh is
    // always full and beautiful. Real member urls, when present, fill the front nodes.
    val avatarPainters = listOf(
        painterResource(Res.drawable.avatar1),
        painterResource(Res.drawable.avatar2),
        painterResource(Res.drawable.avatar3),
        painterResource(Res.drawable.avatar4),
        painterResource(Res.drawable.avatar5),
        painterResource(Res.drawable.avatar6),
    )
    val initials = listOf("A", "J", "M", "S", "R", "K", "L")
    val sizes = listOf(58, 48, 54, 44, 56, 50, 52).map { it.dp }
    val radii = listOf(0.80f, 1.10f, 0.92f, 1.12f, 0.82f, 1.02f, 0.96f)
    val jitter = listOf(0.00f, 0.13f, -0.10f, 0.09f, -0.15f, 0.06f, -0.07f)

    val slots = 7
    val nodes = remember(urls) {
        List(slots) { i ->
            val url = urls.getOrNull(i)
            MeshNode(
                url = url,
                avatar = if (url == null) avatarPainters[i % avatarPainters.size] else null,
                initial = initials[i % initials.size],
                sizeDp = sizes[i % sizes.size],
                radiusFactor = radii[i % radii.size],
                angleJitter = jitter[i % jitter.size],
            )
        }
    }

    val density = LocalDensity.current
    val floatAmpPx = with(density) { 6.dp.toPx() }

    BoxWithConstraints(modifier = modifier) {
        val wPx = with(density) { maxWidth.toPx() }
        val hPx = with(density) { maxHeight.toPx() }
        val center = Offset(wPx / 2f, hPx / 2f)
        val ringRadius = min(wPx, hPx) * 0.36f

        val n = nodes.size
        // Live centre of each node (scattered ring position + soft float).
        val positions = List(n) { i ->
            val node = nodes[i]
            val angle = (2.0 * PI * i / n) - PI / 2.0 + node.angleJitter // start near top
            val r = ringRadius * node.radiusFactor
            val baseX = center.x + (cos(angle) * r).toFloat()
            val baseY = center.y + (sin(angle) * r).toFloat()
            val fx = (sin(phase + i * 1.3f) * floatAmpPx * 0.6f)
            val fy = (cos(phase * 0.85f + i * 1.7f) * floatAmpPx)
            Offset(baseX + fx, baseY + fy)
        }

        val lineColor = GoldPrimary
        val hairPx = with(density) { 1.dp.toPx() }

        // ── Hairline links (draw in once, from the hub outward) ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val p = draw.value
            // Hub → each member.
            positions.forEach { pos ->
                val end = Offset(
                    center.x + (pos.x - center.x) * p,
                    center.y + (pos.y - center.y) * p,
                )
                drawLine(
                    color = lineColor.copy(alpha = 0.26f * p),
                    start = center,
                    end = end,
                    strokeWidth = hairPx,
                )
            }
            // A few member → member links for a woven, "network" feel (tasteful, sparse).
            for (i in 0 until n step 2) {
                val a = positions[i]
                val b = positions[(i + 1) % n]
                val end = Offset(a.x + (b.x - a.x) * p, a.y + (b.y - a.y) * p)
                drawLine(
                    color = lineColor.copy(alpha = 0.14f * p),
                    start = a,
                    end = end,
                    strokeWidth = hairPx,
                )
            }
        }

        // ── Member avatar nodes ──
        positions.forEachIndexed { i, pos ->
            val node = nodes[i]
            val half = with(density) { node.sizeDp.toPx() } / 2f
            val xDp = with(density) { (pos.x - half).toDp() }
            val yDp = with(density) { (pos.y - half).toDp() }
            Box(
                modifier = Modifier
                    .offset(x = xDp, y = yDp)
                    .size(node.sizeDp)
                    .alpha(draw.value),
            ) {
                AvatarNode(node = node)
            }
        }

        // ── Central hub, breathing, above the lines ──
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            LottieAnim("lottie_star_burst.json", Modifier.size(120.dp))
            BreathingHub(scale = breath)
        }
    }
}

/** A gold-ringed circular node: real avatar url, app avatar illustration, or an initials disc. */
@Composable
private fun AvatarNode(node: MeshNode) {
    val c = TajlyTheme.colors
    Box(
        modifier = Modifier
            .size(node.sizeDp)
            .clip(CircleShape)
            .background(Tajly.GoldGradient) // gold ring
            .padding(2.dp)
            .clip(CircleShape)
            .background(c.bg) // bg gap
            .padding(1.5.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            node.url != null -> ImagePlaceholder(
                url = node.url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                showInitial = true,
                initial = node.initial,
                tint = OnGold,
                backgroundColor = GoldPrimary,
            )
            node.avatar != null -> Image(
                painter = node.avatar,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
            )
            else -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(GoldPrimary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = node.initial,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnGold,
                )
            }
        }
    }
}

/** The glowing gold TAJLY hub: soft radial halo + solid gold core with a community glyph. */
@Composable
private fun BreathingHub(scale: Float = 1f) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(
                Brush.radialGradient(
                    listOf(
                        GoldBright.copy(alpha = 0.35f),
                        GoldPrimary.copy(alpha = 0.12f),
                        Color.Transparent,
                    ),
                ),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Tajly.GoldGradient),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = TablerIcons.Users,
                contentDescription = "Tajly community",
                tint = OnGold,
                modifier = Modifier.size(34.dp),
            )
        }
    }
}
