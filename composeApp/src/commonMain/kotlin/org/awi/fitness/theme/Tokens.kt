package org.awi.fitness.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Dark text/icon color used on top of the gold gradient (works in both themes). */
val OnGold = Color(0xFF211803)

/**
 * Theme-aware surface/text/glass tokens. Switches between a warm near-black dark
 * canvas and a warm cream light canvas — both premium, both gold-accented.
 * Provided via [LocalTajlyColors] in FitnessAppTheme; read through [TajlyTheme.colors].
 */
@Immutable
data class TajlyColors(
    val isDark: Boolean,
    val bg: Color,
    val s0: Color,
    val s1: Color,
    val s2: Color,
    val s3: Color,
    val s4: Color,
    val textHi: Color,
    val textMid: Color,
    val textLow: Color,
    val hair: Color,
    val hairStrong: Color,
    val glassFill: Brush,
    val glassRim: Brush,
)

private val DarkTajly = TajlyColors(
    isDark = true,
    bg = Color(0xFF080706), s0 = Color(0xFF100E0C), s1 = Color(0xFF171411),
    s2 = Color(0xFF201C17), s3 = Color(0xFF2A251F), s4 = Color(0xFF342E27),
    textHi = Color(0xFFF6F1E9), textMid = Color(0xFFA9A294), textLow = Color(0xFF6E685C),
    hair = Color(0x12FFFFFF), hairStrong = Color(0x26FFFFFF),
    glassFill = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.03f))),
    glassRim = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.05f))),
)

private val LightTajly = TajlyColors(
    isDark = false,
    bg = Color(0xFFFAF7F0), s0 = Color(0xFFFFFFFF), s1 = Color(0xFFF4EFE6),
    s2 = Color(0xFFEDE7DA), s3 = Color(0xFFE4DCCB), s4 = Color(0xFFDAD0BB),
    textHi = Color(0xFF1A1400), textMid = Color(0xFF5C5240), textLow = Color(0xFF8A7E66),
    hair = Color(0x14000000), hairStrong = Color(0x24000000),
    glassFill = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.55f))),
    glassRim = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.95f), Color.Black.copy(alpha = 0.06f))),
)

fun tajlyColors(dark: Boolean): TajlyColors = if (dark) DarkTajly else LightTajly

val LocalTajlyColors = staticCompositionLocalOf { DarkTajly }

object TajlyTheme {
    val colors: TajlyColors
        @Composable @ReadOnlyComposable get() = LocalTajlyColors.current
}

/**
 * Theme-INDEPENDENT brand accents (identical in light & dark): gold gradient and the
 * District-style section colors. Surface/text/glass live in [TajlyColors] above.
 * The dark surface/text fields below are kept for any screen intentionally always-dark
 * (e.g. the cinematic paywall) and for backward-compatibility.
 */
object Tajly {
    // Warm near-black surface ladder (dark-only convenience; prefer TajlyTheme.colors)
    val Bg = Color(0xFF080706)
    val S0 = Color(0xFF100E0C)
    val S1 = Color(0xFF171411)
    val S2 = Color(0xFF201C17)
    val S3 = Color(0xFF2A251F)
    val S4 = Color(0xFF342E27)
    val TextHi = Color(0xFFF6F1E9)
    val TextMid = Color(0xFFA9A294)
    val TextLow = Color(0xFF6E685C)
    val Hair = Color(0x12FFFFFF)
    val HairStrong = Color(0x26FFFFFF)

    // Theme-independent accents
    val Champagne = Color(0xFFF4E3B0)
    val Violet = Color(0xFF7C5CFF)
    val Green = Color(0xFF46D17F)
    val Blue = Color(0xFF3B9EFF)
    val Coral = Color(0xFFFF6B5E)
    val Teal = Color(0xFF2BD9C4)
    val Pink = Color(0xFFFF5CA8)

    val GoldGradient = Brush.linearGradient(listOf(Champagne, GoldBright, GoldPrimary, GoldDeep))
    val GlassFill = Brush.linearGradient(
        listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.03f))
    )
    val GlassRim = Brush.linearGradient(
        listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.05f))
    )

    fun sectionGlow(color: Color) = Brush.radialGradient(
        listOf(color.copy(alpha = 0.30f), Color.Transparent)
    )
}
