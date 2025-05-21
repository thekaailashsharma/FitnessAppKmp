package org.awi.fitness.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.awi.fitness.theme.StatusBarEffect

@Composable
fun StatusBarPadding(
    color: Color,
    darkIcons: Boolean = color.luminance() > 0.5,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    StatusBarEffect(color, darkIcons)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            content()
        }
    }
} 