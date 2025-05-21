package org.awi.fitness.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import platform.UIKit.*

class IOSStatusBarController : StatusBarController {
    override fun setStatusBarColor(color: Color) {
        // iOS doesn't support direct status bar color setting
        // Instead, we handle this via background color in the UI
    }

    override fun setStatusBarDarkIcons(darkIcons: Boolean) {
        UIApplication.sharedApplication.apply {
            setStatusBarStyle(
                if (darkIcons) UIStatusBarStyleDarkContent else UIStatusBarStyleLightContent,
                true
            )
        }
    }
}

@Composable
actual fun rememberStatusBarController(): StatusBarController {
    return remember { IOSStatusBarController() }
} 