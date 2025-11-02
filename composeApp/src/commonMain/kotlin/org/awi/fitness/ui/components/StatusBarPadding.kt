package org.awi.fitness.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Extension function to add padding for the status bar
 * This is a simplified version that adds a fixed padding
 */
fun Modifier.statusBarPadding(): Modifier {
    return this.padding(top = 32.dp)
}
