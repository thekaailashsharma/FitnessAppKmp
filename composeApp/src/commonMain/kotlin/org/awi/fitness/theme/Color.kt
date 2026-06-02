package org.awi.fitness.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Colors
val BackgroundDark = Color(0xFF0A0A0A)
val GreenAccent = Color(0xFF00B67A)
val DarkGray = Color(0xFF1E1E1E)
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFF8E8E8E)

// Card Colors
val GreenCard = Color(0xFF00B67A)
val DarkCard = Color(0xFF1A1A1A)

// Input Field Colors
val InputFieldBackground = Color(0xFF121212)
val InputFieldBorder = Color(0xFF2C2C2C)

// Button Colors
val ButtonDark = Color(0xFF2A2A2A)
val ButtonGreen = GreenAccent

// Chip Colors
val ChipSelectedBackground = GreenAccent
val ChipUnselectedBackground = Color(0xFF1A1A1A)
val ChipSelectedText = Color(0xFF000000)
val ChipUnselectedText = Color(0xFF8E8E8E)

// Icon Colors
val IconLight = Color(0xFFFFFFFF)
val IconGray = Color(0xFF8E8E8E)

// Search Bar Color
val SearchBarDark = Color(0xFF1A1A1A)

// Gradient Backgrounds
val mainGradient = Brush.verticalGradient(
    colors = listOf(
        BackgroundDark,
        BackgroundDark.copy(alpha = 0.95f)
    )
)

// Card Gradients
val greenCardGradient = Brush.linearGradient(
    colors = listOf(
        GreenCard,
        GreenCard.copy(alpha = 0.9f)
    )
)

val darkCardGradient = Brush.linearGradient(
    colors = listOf(
        DarkCard,
        DarkCard.copy(alpha = 0.9f)
    )
)

val DarkColorScheme = darkColorScheme(
    primary = GreenAccent,
    onPrimary = TextWhite,
    primaryContainer = GreenCard,
    onPrimaryContainer = TextWhite,
    secondary = DarkGray,
    onSecondary = TextWhite,
    secondaryContainer = DarkCard,
    onSecondaryContainer = TextWhite,
    tertiary = ButtonDark,
    onTertiary = TextWhite,
    tertiaryContainer = ButtonDark,
    onTertiaryContainer = TextWhite,
    background = BackgroundDark,
    onBackground = TextWhite,
    surface = DarkCard,
    onSurface = TextWhite,
    surfaceVariant = DarkGray,
    onSurfaceVariant = TextGray,
    outline = TextGray
)

val LightColorScheme = lightColorScheme(
    primary = GreenAccent,
    onPrimary = TextWhite,
    primaryContainer = GreenCard,
    onPrimaryContainer = TextWhite,
    secondary = Color(0xFFE0E0E0),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFFF5F5F5),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = Color(0xFFE8E8E8),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFFF0F0F0),
    onTertiaryContainer = Color(0xFF000000),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF424242),
    outline = Color(0xFF757575)
) 