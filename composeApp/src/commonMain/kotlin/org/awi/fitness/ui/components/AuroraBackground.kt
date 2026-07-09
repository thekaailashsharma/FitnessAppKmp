package org.awi.fitness.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import org.awi.fitness.theme.TajlyTheme

/**
 * "Gold Aurora" — the app's signature ambient backdrop. Large, soft, pre-feathered
 * gold light-blobs over the theme canvas, plus a gentle vignette. Theme-aware so it
 * never turns to grey mud: dark uses low-opacity warm gold on near-black; light uses
 * high-key desaturated gold washes on warm paper. No imagery, no banding-prone flat
 * fills — this is the calm base that carries the gold subtly on every screen.
 */
@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val c = TajlyTheme.colors
    val dark = c.isDark

    // Palette per theme.
    val goldA = if (dark) Color(0xFFC9A84C).copy(alpha = 0.10f) else Color(0xFFE8C06A).copy(alpha = 0.16f)
    val goldB = if (dark) Color(0xFF8C6F2E).copy(alpha = 0.07f) else Color(0xFFEFE4C8).copy(alpha = 0.14f)
    val halo = if (dark) Color(0xFFE8C06A).copy(alpha = 0.05f) else Color(0xFFF3E6C4).copy(alpha = 0.12f)
    val vignette = if (dark) Color(0xFF080705).copy(alpha = 0.55f) else Color(0xFFE5DFD0).copy(alpha = 0.20f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(c.bg)
            .drawBehind {
                // Blob A — top-left key light.
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(goldA, Color.Transparent),
                        center = Offset(size.width * 0.16f, size.height * 0.10f),
                        radius = size.width * 0.95f,
                    )
                )
                // Blob B — bottom-right amber counter-light.
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(goldB, Color.Transparent),
                        center = Offset(size.width * 0.92f, size.height * 0.82f),
                        radius = size.width * 0.85f,
                    )
                )
                // Blob C — faint central halo behind the hero.
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(halo, Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.28f),
                        radius = size.width * 0.7f,
                    )
                )
                // Vignette — settles focus and hides blob edges.
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Transparent, vignette),
                        center = Offset(size.width * 0.5f, size.height * 0.42f),
                        radius = size.maxDimension * 0.75f,
                    )
                )
            },
        content = content,
    )
}
