package org.awi.fitness.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

interface StatusBarController {
    fun setStatusBarColor(color: Color)
    fun setStatusBarDarkIcons(darkIcons: Boolean)
}

@Composable
expect fun rememberStatusBarController(): StatusBarController

@Composable
fun StatusBarEffect(
    color: Color,
    darkIcons: Boolean = color.luminance() > 0.5
) {
    val controller = rememberStatusBarController()
    controller.setStatusBarColor(color)
    controller.setStatusBarDarkIcons(darkIcons)
}