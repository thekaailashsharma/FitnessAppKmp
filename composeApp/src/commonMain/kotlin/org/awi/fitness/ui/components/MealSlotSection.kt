package org.awi.fitness.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.Clock
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.ic3d_leaf
import fitnessappkmp.composeapp.generated.resources.ic3d_moon
import fitnessappkmp.composeapp.generated.resources.ic3d_star
import fitnessappkmp.composeapp.generated.resources.ic3d_sun
import org.awi.fitness.data.StringKey
import org.awi.fitness.model.Meal
import org.awi.fitness.model.MealSlot
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.awi.fitness.viewmodel.LanguageViewModel

/** District-style section accent color per slot (theme-independent brand accents). */
val MealSlot.accentColor: Color
    get() = when (this) {
        MealSlot.BREAKFAST -> Tajly.Coral   // warm sunrise
        MealSlot.LUNCH -> Tajly.Green        // fresh / meals
        MealSlot.DINNER -> Tajly.Violet      // evening
        MealSlot.SNACK -> Tajly.Pink         // treat
    }

/** Colorful 3D studio icon per slot. */
val MealSlot.icon3d: DrawableResource
    get() = when (this) {
        MealSlot.BREAKFAST -> Res.drawable.ic3d_sun
        MealSlot.LUNCH -> Res.drawable.ic3d_leaf
        MealSlot.DINNER -> Res.drawable.ic3d_moon
        MealSlot.SNACK -> Res.drawable.ic3d_star
    }

fun MealSlot.localizedName(lvm: LanguageViewModel): String = when (this) {
    MealSlot.BREAKFAST -> lvm.getString(StringKey.BREAKFAST)
    MealSlot.LUNCH -> lvm.getString(StringKey.LUNCH)
    MealSlot.DINNER -> lvm.getString(StringKey.DINNER)
    MealSlot.SNACK -> lvm.getString(StringKey.SNACK)
}

@Composable
fun MealSlotSection(
    slot: MealSlot,
    meals: List<Meal>,
    completedMealIds: Set<String>,
    onMealClick: (Meal) -> Unit,
    onToggleCompletion: (Meal) -> Unit,
    languageViewModel: LanguageViewModel? = null,
    modifier: Modifier = Modifier
) {
    val c = TajlyTheme.colors
    val accent = slot.accentColor

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 3D icon on a glass tile with a section-colored glow
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(c.glassFill, RoundedCornerShape(12.dp))
                    .border(1.dp, c.hairStrong, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .blur(18.dp)
                        .background(
                            Brush.radialGradient(listOf(accent.copy(alpha = 0.6f), Color.Transparent))
                        )
                )
                Image(
                    painter = painterResource(slot.icon3d),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                text = languageViewModel?.let { slot.localizedName(it) } ?: slot.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = c.textHi
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        meals.forEach { meal ->
            MealItemCard(
                meal = meal,
                accentColor = accent,
                isCompleted = completedMealIds.contains(meal.id),
                onClick = { onMealClick(meal) },
                onToggleCompletion = { onToggleCompletion(meal) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun MealItemCard(
    meal: Meal,
    accentColor: Color,
    isCompleted: Boolean,
    onClick: () -> Unit,
    onToggleCompletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .clip(shape)
            .background(c.glassFill, shape)
            .border(1.dp, c.hairStrong, shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Section-colored accent rail
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .width(4.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isCompleted) accentColor.copy(alpha = 0.35f) else accentColor)
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) c.textMid else c.textHi,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${meal.calories} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textMid
                    )
                    Dot(c.textLow)
                    Text(
                        text = "${meal.protein}g protein",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMid
                    )
                    if (meal.prepTimeMinutes > 0) {
                        Dot(c.textLow)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = TablerIcons.Clock,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = c.textLow
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${meal.prepTimeMinutes}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textLow
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // GOLD completion check
            val checkInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .pressScale(checkInteraction)
                    .size(34.dp)
                    .clip(CircleShape)
                    .then(
                        if (isCompleted) Modifier.background(Tajly.GoldGradient)
                        else Modifier.border(2.dp, c.hairStrong, CircleShape)
                    )
                    .clickable(
                        interactionSource = checkInteraction,
                        indication = null,
                        onClick = onToggleCompletion
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = TablerIcons.Check,
                        contentDescription = "Completed",
                        tint = OnGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(3.dp)
            .clip(CircleShape)
            .background(color)
    )
}
