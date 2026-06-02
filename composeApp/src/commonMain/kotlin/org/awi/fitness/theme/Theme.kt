package org.awi.fitness.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

const val THEME_ANIM_DURATION = 400

// Shared progress exposed so App.kt can read the same animated value
// 0f = light, 1f = dark
val LocalThemeProgress = compositionLocalOf { 1f }

private fun lerpScheme(light: ColorScheme, dark: ColorScheme, progress: Float): ColorScheme =
    light.copy(
        primary              = lerp(light.primary,              dark.primary,              progress),
        onPrimary            = lerp(light.onPrimary,            dark.onPrimary,            progress),
        primaryContainer     = lerp(light.primaryContainer,     dark.primaryContainer,     progress),
        onPrimaryContainer   = lerp(light.onPrimaryContainer,   dark.onPrimaryContainer,   progress),
        secondary            = lerp(light.secondary,            dark.secondary,            progress),
        onSecondary          = lerp(light.onSecondary,          dark.onSecondary,          progress),
        secondaryContainer   = lerp(light.secondaryContainer,   dark.secondaryContainer,   progress),
        onSecondaryContainer = lerp(light.onSecondaryContainer, dark.onSecondaryContainer, progress),
        tertiary             = lerp(light.tertiary,             dark.tertiary,             progress),
        onTertiary           = lerp(light.onTertiary,           dark.onTertiary,           progress),
        tertiaryContainer    = lerp(light.tertiaryContainer,    dark.tertiaryContainer,    progress),
        onTertiaryContainer  = lerp(light.onTertiaryContainer,  dark.onTertiaryContainer,  progress),
        background           = lerp(light.background,           dark.background,           progress),
        onBackground         = lerp(light.onBackground,         dark.onBackground,         progress),
        surface              = lerp(light.surface,              dark.surface,              progress),
        onSurface            = lerp(light.onSurface,            dark.onSurface,            progress),
        surfaceVariant       = lerp(light.surfaceVariant,       dark.surfaceVariant,       progress),
        onSurfaceVariant     = lerp(light.onSurfaceVariant,     dark.onSurfaceVariant,     progress),
        outline              = lerp(light.outline,              dark.outline,              progress),
        error                = lerp(light.error,                dark.error,                progress),
        onError              = lerp(light.onError,              dark.onError,              progress),
        errorContainer       = lerp(light.errorContainer,       dark.errorContainer,       progress),
        onErrorContainer     = lerp(light.onErrorContainer,     dark.onErrorContainer,     progress),
    )

@Composable
fun FitnessAppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (useDarkTheme) 1f else 0f,
        animationSpec = tween(durationMillis = THEME_ANIM_DURATION, easing = FastOutSlowInEasing),
        label = "themeProgress"
    )

    val animatedScheme = lerpScheme(LightColorScheme, DarkColorScheme, progress)

    CompositionLocalProvider(LocalThemeProgress provides progress) {
        MaterialTheme(
            colorScheme = animatedScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
