package org.awi.fitness.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.TajlyTheme

/**
 * Real liquid glass via Haze 1.7.0 — premium depth stack:
 *   drop shadow (floats) → clip → backdrop blur + warm tint → luminance gradient
 *   → inner top highlight (wet bevel) → specular sweep rim (lit top-left).
 *
 * Usage per screen:
 *   ProvideGlass {                     // shared HazeState
 *     Box {
 *        Backdrop(Modifier.glassSource())      // content/photo to be blurred
 *        SomeBar(Modifier.liquidGlass())       // glass surface that blurs it
 *     }
 *   }
 *
 * Without a [ProvideGlass] source it falls back to faux translucent glass, so it is
 * always safe to use. [tier] scales blur/tint/shadow so depth is legible: pills blur
 * least & float highest, hero overlays blur most.
 */
val LocalHazeState = staticCompositionLocalOf<HazeState?> { null }

/** Depth tier — measurably different blur/tint/shadow so layering reads. */
enum class GlassTier { Nav, Card, Hero }

@Composable
fun ProvideGlass(content: @Composable () -> Unit) {
    val state = rememberHazeState()
    CompositionLocalProvider(LocalHazeState provides state, content = content)
}

/** Marks this content as the backdrop that glass surfaces will blur. No-op without ProvideGlass. */
@Composable
fun Modifier.glassSource(): Modifier {
    val state = LocalHazeState.current ?: return this
    return this.hazeSource(state)
}

@Composable
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(22.dp),
    goldTint: Boolean = false,
    tier: GlassTier = GlassTier.Card,
): Modifier {
    val c = TajlyTheme.colors
    val dark = c.isDark
    val state = LocalHazeState.current

    // ── Per-tier knobs (dark reference values). ──
    val blur = when (tier) { GlassTier.Nav -> 16.dp; GlassTier.Card -> 24.dp; GlassTier.Hero -> 34.dp }
    val noise = when (tier) { GlassTier.Nav -> 0.05f; GlassTier.Card -> 0.06f; GlassTier.Hero -> 0.08f }
    val elevation = when (tier) { GlassTier.Nav -> 10.dp; GlassTier.Card -> 16.dp; GlassTier.Hero -> 22.dp }
    // Lower tint = more see-through glass (matches the translucent mockup).
    val tintAlphaDark = when (tier) { GlassTier.Nav -> 0.20f; GlassTier.Card -> 0.28f; GlassTier.Hero -> 0.40f }
    val spotAlphaDark = when (tier) { GlassTier.Nav -> 0.30f; GlassTier.Card -> 0.38f; GlassTier.Hero -> 0.48f }
    val rimPeak = when (tier) { GlassTier.Nav -> 0.50f; GlassTier.Card -> 0.45f; GlassTier.Hero -> 0.35f }
    val hiAlpha = when (tier) { GlassTier.Nav -> 0.60f; GlassTier.Card -> 0.55f; GlassTier.Hero -> 0.45f }

    // ── Theme resolution. Warm tint (never pure black/white → keeps gold temperature). ──
    val tintColor = if (dark) Color(0xFF1B1712) else Color(0xFF2A2010)
    val tintAlpha = if (dark) tintAlphaDark else (tintAlphaDark * 0.34f).coerceIn(0.10f, 0.22f)
    val effBlur = if (dark) blur else blur + 4.dp
    val warmShadow = Color(0xFF3A2E12)
    val ambientColor = (if (dark) Color.Black else warmShadow).copy(alpha = if (dark) 0.28f else 0.08f)
    val spotColor = (if (dark) Color.Black else warmShadow).copy(alpha = if (dark) spotAlphaDark else 0.12f)

    // Luminance gradient (over the material): bright top edge, shaded bottom → reads as curved glass.
    val lumBrush = Brush.verticalGradient(
        0.0f to Color.White.copy(alpha = if (dark) 0.10f else 0.50f),
        0.5f to Color.White.copy(alpha = if (dark) 0.02f else 0.10f),
        1.0f to Color.Black.copy(alpha = if (dark) 0.06f else 0.10f),
    )
    // Inner top highlight (approximated with a top-clamped border gradient) — the "wet" bevel.
    val highlightBrush = Brush.verticalGradient(
        0.0f to Color.White.copy(alpha = if (dark) hiAlpha else 0.90f),
        0.32f to Color.Transparent,
    )
    // Specular rim — angular so light appears to come from top-left and wrap; dark bottom edge in light mode.
    val rimBrush = Brush.sweepGradient(
        listOf(
            Color.White.copy(alpha = rimPeak * 0.15f),
            Color.White.copy(alpha = rimPeak),
            Color.White.copy(alpha = rimPeak * 0.25f),
            (if (dark) Color.White.copy(alpha = rimPeak * 0.06f) else Color.Black.copy(alpha = 0.10f)),
            Color.White.copy(alpha = rimPeak * 0.15f),
        )
    )

    val shadowed = this.shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = ambientColor,
        spotColor = spotColor,
    )
    val clipped = shadowed.clip(shape)
    val material = if (state != null) {
        clipped.hazeEffect(state = state) {
            blurRadius = effBlur
            noiseFactor = if (dark) noise else (noise * 0.6f)
            backgroundColor = c.bg
            tints = buildList {
                if (goldTint) add(HazeTint(GoldPrimary.copy(alpha = if (dark) 0.10f else 0.08f)))
                add(HazeTint(tintColor.copy(alpha = tintAlpha)))
            }
        }
    } else {
        clipped.background(c.glassFill, shape)
    }
    return material
        .background(lumBrush, shape)
        .border(1.2.dp, highlightBrush, shape)
        .border(1.dp, rimBrush, shape)
}
