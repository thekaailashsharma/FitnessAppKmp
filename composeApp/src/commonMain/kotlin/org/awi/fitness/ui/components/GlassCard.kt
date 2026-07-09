package org.awi.fitness.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Liquid-glass surface. Real backdrop blur (Haze) when the screen wraps content in
 * [ProvideGlass] + marks its backdrop with [glassSource]; otherwise a faux translucent
 * fill. Theme-aware, with a gradient specular rim. See [liquidGlass].
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    goldTint: Boolean = false,
    tier: GlassTier = GlassTier.Card,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.liquidGlass(shape = shape, goldTint = goldTint, tier = tier),
        content = content,
    )
}
