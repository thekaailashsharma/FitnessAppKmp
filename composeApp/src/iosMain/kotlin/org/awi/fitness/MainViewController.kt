package org.awi.fitness

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController(
    configure = {
        // Allow the interactive pop gesture on every Compose screen so
        // iOS edge-swipe-from-left navigates back through Voyager's stack.
        // The actual gesture interception is wired on the Swift side via
        // the UINavigationController wrapper in ContentView.swift.
    }
) { App() }