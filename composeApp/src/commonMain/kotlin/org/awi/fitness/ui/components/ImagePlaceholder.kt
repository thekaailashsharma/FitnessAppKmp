package org.awi.fitness.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import compose.icons.TablerIcons
import compose.icons.tablericons.Photo

/**
 * A placeholder component for images that will be replaced with actual image loading later
 */
@Composable
fun ImagePlaceholder(
    modifier: Modifier = Modifier,
    url: String? = null,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showInitial: Boolean = false,
    initial: String = "",
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (showInitial && initial.isNotEmpty()) {
            Text(
                text = initial.first().toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = tint
            )
        } else {
            Icon(
                imageVector = TablerIcons.Photo,
                contentDescription = contentDescription,
                tint = tint.copy(alpha = 0.5f)
            )
        }
    }
}
