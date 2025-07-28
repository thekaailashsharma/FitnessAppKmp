package org.awi.fitness.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    showControls: Boolean = true,
    onReady: () -> Unit = {}
) 