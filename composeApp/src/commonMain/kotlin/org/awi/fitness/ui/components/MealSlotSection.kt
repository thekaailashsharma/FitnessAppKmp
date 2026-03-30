package org.awi.fitness.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.Circle
import compose.icons.tablericons.Clock
import compose.icons.tablericons.Fold
import compose.icons.tablericons.Moon
import compose.icons.tablericons.Star
import compose.icons.tablericons.Sun
import org.awi.fitness.data.StringKey
import org.awi.fitness.model.Meal
import org.awi.fitness.model.MealSlot
import org.awi.fitness.viewmodel.LanguageViewModel

val MealSlot.accentColor: Color
    get() = when (this) {
        MealSlot.BREAKFAST -> Color(0xFFFFB74D)
        MealSlot.LUNCH -> Color(0xFF66BB6A)
        MealSlot.DINNER -> Color(0xFF5C6BC0)
        MealSlot.SNACK -> Color(0xFFAB47BC)
    }

val MealSlot.slotIcon: ImageVector
    get() = when (this) {
        MealSlot.BREAKFAST -> TablerIcons.Sun
        MealSlot.LUNCH -> TablerIcons.Fold
        MealSlot.DINNER -> TablerIcons.Moon
        MealSlot.SNACK -> TablerIcons.Star
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
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = slot.slotIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = slot.accentColor
            )
            Text(
                text = languageViewModel?.let { slot.localizedName(it) } ?: slot.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        meals.forEach { meal ->
            MealItemCard(
                meal = meal,
                accentColor = slot.accentColor,
                isCompleted = completedMealIds.contains(meal.id),
                onClick = { onMealClick(meal) },
                onToggleCompletion = { onToggleCompletion(meal) }
            )
            Spacer(modifier = Modifier.height(8.dp))
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    if (isCompleted) accentColor.copy(alpha = 0.3f) else accentColor
                )
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${meal.calories} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${meal.protein}g protein",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (meal.prepTimeMinutes > 0) {
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = TablerIcons.Clock,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${meal.prepTimeMinutes}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable(onClick = onToggleCompletion),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted) TablerIcons.Check else TablerIcons.Circle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isCompleted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}
