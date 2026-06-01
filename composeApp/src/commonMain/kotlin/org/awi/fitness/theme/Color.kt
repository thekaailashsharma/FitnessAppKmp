package org.awi.fitness.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Tajly Brand Palette ──────────────────────────────────────────────────────

// Gold — primary brand color replacing green
val GoldPrimary   = Color(0xFFC9A84C)   // Antique gold — main accent
val GoldBright    = Color(0xFFE8C06A)   // Lighter gold for dark-theme contrast
val GoldDeep      = Color(0xFFA8873A)   // Deep gold for containers / pressed states
val GoldMuted     = Color(0xFF8C6F2E)   // Subdued gold for subtle elements

// Keep GreenAccent as an alias so all 163 import sites get the new color automatically
val GreenAccent   = GoldPrimary

// Neutrals — dark
val BackgroundDark    = Color(0xFF0C0B09)   // Near-black with warm tint
val SurfaceDark       = Color(0xFF181610)   // Warm dark card
val SurfaceVariantDark= Color(0xFF221F17)   // Slightly lighter warm dark
val DarkGray          = Color(0xFF221F17)
val DarkCard          = Color(0xFF181610)
val TextWhite         = Color(0xFFF5F0E8)   // Warm white, not stark
val TextGray          = Color(0xFF8E8880)   // Warm grey

// Neutrals — light
val BackgroundLight   = Color(0xFFFAF7F0)   // Warm cream
val SurfaceLight      = Color(0xFFF5F0E8)   // Slightly off-white
val SurfaceVariantLight = Color(0xFFEDE8DC)

// Input fields
val InputFieldBackground = Color(0xFF181610)
val InputFieldBorder     = Color(0xFF2C2820)

// Buttons
val ButtonDark  = Color(0xFF2A2620)
val ButtonGold  = GoldPrimary

// Chips
val ChipSelectedBackground  = GoldPrimary
val ChipUnselectedBackground= Color(0xFF1A1810)
val ChipSelectedText        = Color(0xFF0C0B09)
val ChipUnselectedText      = Color(0xFF8E8880)

// Icons
val IconLight = Color(0xFFF5F0E8)
val IconGray  = Color(0xFF8E8880)

// Search bar
val SearchBarDark = Color(0xFF1A1810)

// Card aliases kept for backward compat
val GreenCard = GoldPrimary

// Gradients
val mainGradient = Brush.verticalGradient(
    colors = listOf(BackgroundDark, BackgroundDark.copy(alpha = 0.95f))
)

val goldCardGradient = Brush.linearGradient(
    colors = listOf(GoldPrimary, GoldDeep)
)

// Deprecated aliases — kept so any direct hex usages still resolve
@Suppress("unused")
val greenCardGradient = goldCardGradient

val darkCardGradient = Brush.linearGradient(
    colors = listOf(DarkCard, DarkCard.copy(alpha = 0.9f))
)

// ── Material3 Color Schemes ──────────────────────────────────────────────────

val DarkColorScheme = darkColorScheme(
    primary              = GoldPrimary,
    onPrimary            = Color(0xFF0C0B09),
    primaryContainer     = GoldDeep.copy(alpha = 0.25f),
    onPrimaryContainer   = GoldBright,
    secondary            = DarkGray,
    onSecondary          = TextWhite,
    secondaryContainer   = SurfaceDark,
    onSecondaryContainer = TextWhite,
    tertiary             = ButtonDark,
    onTertiary           = TextWhite,
    tertiaryContainer    = ButtonDark,
    onTertiaryContainer  = TextWhite,
    background           = BackgroundDark,
    onBackground         = TextWhite,
    surface              = SurfaceDark,
    onSurface            = TextWhite,
    surfaceVariant       = SurfaceVariantDark,
    onSurfaceVariant     = TextGray,
    outline              = Color(0xFF3D3826),
    error                = Color(0xFFCF6679),
    onError              = Color(0xFF0C0B09),
    errorContainer       = Color(0xFF5C1A24),
    onErrorContainer     = Color(0xFFFFB3BC)
)

val LightColorScheme = lightColorScheme(
    primary              = GoldPrimary,
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = GoldBright.copy(alpha = 0.20f),
    onPrimaryContainer   = GoldMuted,
    secondary            = Color(0xFFD4C4A0),
    onSecondary          = Color(0xFF1A1400),
    secondaryContainer   = SurfaceVariantLight,
    onSecondaryContainer = Color(0xFF1A1400),
    tertiary             = Color(0xFFE8E0D0),
    onTertiary           = Color(0xFF1A1400),
    tertiaryContainer    = SurfaceLight,
    onTertiaryContainer  = Color(0xFF1A1400),
    background           = BackgroundLight,
    onBackground         = Color(0xFF1A1400),
    surface              = SurfaceLight,
    onSurface            = Color(0xFF1A1400),
    surfaceVariant       = SurfaceVariantLight,
    onSurfaceVariant     = Color(0xFF5C5240),
    outline              = Color(0xFFA89878),
    error                = Color(0xFFB3261E),
    onError              = Color(0xFFFFFFFF),
    errorContainer       = Color(0xFFF9DEDC),
    onErrorContainer     = Color(0xFF410E0B)
)
