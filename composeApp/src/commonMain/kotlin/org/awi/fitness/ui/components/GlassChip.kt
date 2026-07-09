package org.awi.fitness.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale

/** Glass filter/selection chip. Selected = gold; unselected = subtle glass. Theme-aware. */
@Composable
fun GlassChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val base = modifier
        .pressScale(interaction)
        .clip(CircleShape)
    val styled = if (selected) {
        base.background(Tajly.GoldGradient)
    } else {
        base
            .background(c.glassFill, CircleShape)
            .border(1.dp, c.hairStrong, CircleShape)
    }
    Box(
        modifier = styled
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .padding(horizontal = 15.dp, vertical = 9.dp),
    ) {
        Text(
            text = text,
            color = if (selected) OnGold else c.textMid,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
