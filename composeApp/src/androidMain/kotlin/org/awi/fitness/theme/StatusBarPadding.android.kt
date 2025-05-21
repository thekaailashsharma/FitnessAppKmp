package org.awi.fitness.theme

import android.app.Activity
import android.view.View
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class AndroidStatusBarController(
    private val window: Window,
    private val view: View
) : StatusBarController {
    override fun setStatusBarColor(color: Color) {
        window.statusBarColor = color.toArgb()
    }

    override fun setStatusBarDarkIcons(darkIcons: Boolean) {
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = darkIcons
        }
    }
}

@Composable
actual fun rememberStatusBarController(): StatusBarController {
    val view = LocalView.current
    val window = (view.context as Activity).window

    return remember(view, window) {
        AndroidStatusBarController(window, view)
    }
}