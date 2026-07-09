package org.awi.fitness.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale

/**
 * District-style bento tile: glass surface + a section-tinted radial glow + an icon slot + label.
 * The icon slot lets callers pass an Image(painterResource(...)) without this component depending on assets.
 */
@Composable
fun BentoTile(
    title: String,
    accent: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    icon: @Composable () -> Unit = {},
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(20.dp)
    var base = modifier.pressScale(interaction).clip(shape)
    if (onClick != null) {
        base = base.clickable(interactionSource = interaction, indication = null) { onClick() }
    }
    Box(
        modifier = base
            .background(c.glassFill, shape)
            .border(1.dp, c.hairStrong, shape)
            .heightIn(min = 112.dp),
    ) {
        // section glow
        Box(
            modifier = Modifier
                .size(96.dp)
                .padding(8.dp)
                .blur(26.dp)
                .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.55f), Color.Transparent))),
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) { icon() }
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = c.textHi)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textMid)
                }
            }
        }
    }
}
