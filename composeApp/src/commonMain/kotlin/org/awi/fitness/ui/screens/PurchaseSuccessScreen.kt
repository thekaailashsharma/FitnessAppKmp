package org.awi.fitness.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import compose.icons.TablerIcons
import compose.icons.tablericons.Stars
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.bg_success
import fitnessappkmp.composeapp.generated.resources.ic3d_crown
import kotlinx.coroutines.delay
import org.awi.fitness.data.Language
import org.awi.fitness.data.StringKey
import org.awi.fitness.navigation.LocalAppNavigation
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldDeep
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.ui.components.CelebrationBurst
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.LottieAnim
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.glassSource
import org.awi.fitness.ui.components.liquidGlass
import org.awi.fitness.ui.localizedString
import org.awi.fitness.viewmodel.LanguageViewModel
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class PurchaseSuccessScreen(
    private val languageViewModel: LanguageViewModel,
) : Screen {
    @Composable
    override fun Content() {
        val appNavigation = LocalAppNavigation.current
        val colors = TajlyTheme.colors
        val languageCode by languageViewModel.currentLanguage.collectAsState()
        val language = Language.entries.find { it.code == languageCode } ?: Language.ENGLISH

        // Motion philosophy: no common-source reduced-motion flag is reliably
        // available here, so every stage is spring/tween-eased and the whole
        // sequence stays smooth and self-settling — never abrupt or janky.

        // ---- staged choreography ----
        // 0: nothing  1: glow + burst + confetti ignite  2: crown springs in
        // 3: badge  4: title  5: subtitle + CTA glass card  -> navigate
        var phase by remember { mutableIntStateOf(0) }
        var navigateAway by remember { mutableStateOf(false) }

        // Radial gold glow fades in behind everything.
        val glowAlpha by animateFloatAsState(
            targetValue = if (phase >= 1) 1f else 0f,
            animationSpec = tween(900, easing = FastOutSlowInEasing),
            label = "glow"
        )

        // Crown scales in with a soft spring overshoot, then settles.
        val crownScale by animateFloatAsState(
            targetValue = if (phase >= 2) 1f else 0.3f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "crownScale"
        )
        val crownAlpha by animateFloatAsState(
            targetValue = if (phase >= 2) 1f else 0f,
            animationSpec = tween(500, easing = FastOutSlowInEasing),
            label = "crownAlpha"
        )

        // Badge / title / glass card stagger in with gentle upward drift.
        val badgeProgress by animateFloatAsState(
            targetValue = if (phase >= 3) 1f else 0f,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
            label = "badge"
        )
        val titleProgress by animateFloatAsState(
            targetValue = if (phase >= 4) 1f else 0f,
            animationSpec = tween(650, easing = FastOutSlowInEasing),
            label = "title"
        )
        val cardProgress by animateFloatAsState(
            targetValue = if (phase >= 5) 1f else 0f,
            animationSpec = tween(700, easing = FastOutSlowInEasing),
            label = "card"
        )

        // The ONE allowed continuous motion: a gentle gold halo breathing behind the crown.
        val infinite = rememberInfiniteTransition(label = "ambient")
        val haloScale by infinite.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(
                tween(2600, easing = FastOutSlowInEasing),
                RepeatMode.Reverse
            ),
            label = "halo"
        )

        // ---- timing ----  (preserves overall celebration before auto-navigation)
        LaunchedEffect(Unit) {
            delay(120)
            phase = 1          // glow + burst + confetti
            delay(260)
            phase = 2          // crown springs in
            delay(700)
            phase = 3          // premium badge
            delay(450)
            phase = 4          // title
            delay(450)
            phase = 5          // subtitle + CTA glass card
            delay(3600)        // let the moment breathe, then auto-advance
            navigateAway = true
        }

        // Navigation behavior preserved exactly: fires after the sequence,
        // or immediately when the user taps the CTA (both route through here).
        LaunchedEffect(navigateAway) {
            if (navigateAway) {
                appNavigation.navigateAfterPremium()
            }
        }

        ProvideGlass {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // ---- Cinematic backdrop (blur source for the liquid glass) ----
                Image(
                    painter = painterResource(Res.drawable.bg_success),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .glassSource()
                )

                // Scrim: settle the photo toward the theme canvas so text + crown
                // stay crisp and readable (theme-aware via colors.bg).
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    colors.bg.copy(alpha = 0.55f),
                                    colors.bg.copy(alpha = 0.40f),
                                    colors.bg.copy(alpha = 0.72f),
                                    colors.bg.copy(alpha = 0.94f),
                                )
                            )
                        )
                )

                // Radial gold glow (the celebration "spotlight").
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(glowAlpha)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    GoldPrimary.copy(alpha = if (colors.isDark) 0.30f else 0.22f),
                                    GoldPrimary.copy(alpha = 0.05f),
                                    Color.Transparent
                                ),
                                radius = 900f
                            )
                        )
                )

                // Celebration layers (Compose-native, played once).
                CelebrationBurst(active = phase >= 1)
                PremiumConfetti(active = phase >= 1)

                // One-shot Lottie confetti overlay (behind the text/CTA column).
                if (phase >= 1) {
                    LottieAnim(
                        "lottie_confetti.json",
                        Modifier.fillMaxSize(),
                        iterations = 1,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp, vertical = 40.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    // ---- 3D crown with soft pulsing gold halo ----
                    Box(contentAlignment = Alignment.Center) {
                        // Outer glow halo (blurred radial), breathes continuously.
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .scale(if (phase >= 2) haloScale else 0.95f)
                                .alpha(crownAlpha * (if (colors.isDark) 0.55f else 0.4f))
                                .blur(48.dp)
                                .clip(RoundedCornerShape(120.dp))
                                .background(
                                    Brush.radialGradient(
                                        listOf(GoldBright, GoldPrimary.copy(alpha = 0.2f), Color.Transparent)
                                    )
                                )
                        )
                        // Soft inner ring.
                        Box(
                            modifier = Modifier
                                .size(176.dp)
                                .scale(if (phase >= 2) haloScale else 0.9f)
                                .alpha(crownAlpha * 0.3f)
                                .clip(RoundedCornerShape(88.dp))
                                .background(GoldPrimary.copy(alpha = 0.35f))
                        )
                        // One-shot Lottie crown (keeps the spring-in scale/alpha entrance).
                        LottieAnim(
                            "lottie_crown.json",
                            Modifier
                                .size(96.dp)
                                .graphicsLayer {
                                    scaleX = crownScale
                                    scaleY = crownScale
                                    alpha = crownAlpha
                                },
                            iterations = 1,
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // ---- PREMIUM UNLOCKED gold badge ----
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = badgeProgress
                                scaleX = 0.85f + 0.15f * badgeProgress
                                scaleY = 0.85f + 0.15f * badgeProgress
                                translationY = (1f - badgeProgress) * 16f
                            }
                            .clip(RoundedCornerShape(24.dp))
                            .background(Tajly.GoldGradient)
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✦ ${languageViewModel.getString(StringKey.PS_PREMIUM_UNLOCKED)} ✦",
                            color = OnGold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            letterSpacing = 2.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ---- Title (gold-gradient brush) ----
                    Text(
                        text = localizedString(StringKey.PURCHASE_SUCCESS_HEADLINE, language),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            brush = Brush.linearGradient(
                                listOf(colors.textHi, GoldBright, GoldPrimary, GoldDeep)
                            )
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer {
                            alpha = titleProgress
                            translationY = (1f - titleProgress) * 18f
                        }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // ---- Liquid-glass CTA card (subtitle + button + auto-advance hint) ----
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 440.dp)
                            .graphicsLayer {
                                alpha = cardProgress
                                translationY = (1f - cardProgress) * 24f
                            }
                            .liquidGlass(shape = RoundedCornerShape(28.dp), goldTint = true)
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                    ) {
                        Text(
                            text = localizedString(StringKey.PURCHASE_SUCCESS_SUBTITLE, language),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textHi,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        GoldButton(
                            text = localizedString(StringKey.CONTINUE, language),
                            onClick = { navigateAway = true },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ---- Preparing / auto-advance hint ----
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                TablerIcons.Stars,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = localizedString(StringKey.PURCHASE_SUCCESS_PREPARING, language),
                                color = colors.textMid,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compose-native falling confetti — gold + white particles drifting down over the
 * cinematic backdrop, played once. No external dependency; pure Canvas + animation.
 */
@Composable
private fun PremiumConfetti(active: Boolean, modifier: Modifier = Modifier) {
    if (!active) return

    val pieces = remember {
        val rnd = Random(7)
        List(80) {
            ConfettiPiece(
                xFraction = rnd.nextFloat(),
                startDelay = rnd.nextFloat() * 0.30f,
                fallSpeed = 0.7f + rnd.nextFloat() * 0.6f,
                drift = (rnd.nextFloat() - 0.5f) * 0.16f,
                sizePx = 6f + rnd.nextFloat() * 8f,
                spin = (rnd.nextFloat() - 0.5f) * 540f,
                startRot = rnd.nextFloat() * 360f,
                round = rnd.nextBoolean(),
                color = when (it % 4) {
                    0 -> GoldPrimary
                    1 -> GoldBright
                    2 -> Color.White
                    else -> Tajly.Champagne
                }
            )
        }
    }

    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(4400, easing = LinearEasing),
        label = "confetti"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        pieces.forEach { p ->
            val local = ((progress - p.startDelay) / (1f - p.startDelay)).coerceIn(0f, 1f)
            if (local <= 0f) return@forEach

            // Fade out over the final stretch of the fall.
            val fade = if (local > 0.82f) (1f - (local - 0.82f) / 0.18f).coerceIn(0f, 1f) else 1f
            val travel = (size.height + 120f) * local * p.fallSpeed
            val y = -60f + travel
            val x = size.width * p.xFraction + size.width * p.drift * sin(local * PI.toFloat() * 3.2f)
            val c = p.color.copy(alpha = fade)

            if (p.round) {
                drawCircle(color = c, radius = p.sizePx * 0.5f, center = Offset(x, y))
            } else {
                val rot = p.startRot + p.spin * local
                rotate(degrees = rot, pivot = Offset(x, y)) {
                    drawRect(
                        color = c,
                        topLeft = Offset(x - p.sizePx * 0.5f, y - p.sizePx * 0.35f),
                        size = androidx.compose.ui.geometry.Size(p.sizePx, p.sizePx * 0.7f)
                    )
                }
            }
        }
    }
}

private data class ConfettiPiece(
    val xFraction: Float,
    val startDelay: Float,
    val fallSpeed: Float,
    val drift: Float,
    val sizePx: Float,
    val spin: Float,
    val startRot: Float,
    val round: Boolean,
    val color: Color,
)
