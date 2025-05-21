package org.awi.fitness.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Colors
val BackgroundDark = Color(0xFF000000)
val YellowAccent = Color(0xFFFFF176)
val DarkGray = Color(0xFF1E1E1E)
val TextWhite = Color(0xFFFFFFFF)
val TextGray = Color(0xFF8E8E8E)

// Card Colors
val YellowCard = YellowAccent
val DarkCard = Color(0xFF1A1A1A)

// Input Field Colors
val InputFieldBackground = Color(0xFF121212)
val InputFieldBorder = Color(0xFF2C2C2C)

// Button Colors
val ButtonYellow = YellowAccent
val ButtonText = Color(0xFF000000)

// Chip Colors
val ChipSelectedBackground = YellowAccent
val ChipUnselectedBackground = Color(0xFF1A1A1A)
val ChipSelectedText = Color(0xFF000000)
val ChipUnselectedText = Color(0xFF8E8E8E)

// Icon Colors
val IconSelected = Color(0xFF000000)
val IconUnselected = Color(0xFF8E8E8E)

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
val yellowCardGradient = Brush.linearGradient(
    colors = listOf(
        YellowCard,
        YellowCard.copy(alpha = 0.9f)
    )
)

val darkCardGradient = Brush.linearGradient(
    colors = listOf(
        DarkCard,
        DarkCard.copy(alpha = 0.9f)
    )
) 