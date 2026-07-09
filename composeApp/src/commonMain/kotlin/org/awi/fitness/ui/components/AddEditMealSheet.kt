package org.awi.fitness.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Trash
import org.awi.fitness.data.StringKey
import org.awi.fitness.model.Meal
import org.awi.fitness.model.MealSlot
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.viewmodel.LanguageViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private fun slotLabel(slot: MealSlot, lvm: LanguageViewModel): String = when (slot) {
    MealSlot.BREAKFAST -> lvm.getString(StringKey.BREAKFAST)
    MealSlot.LUNCH -> lvm.getString(StringKey.LUNCH)
    MealSlot.DINNER -> lvm.getString(StringKey.DINNER)
    MealSlot.SNACK -> lvm.getString(StringKey.SNACK)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalUuidApi::class)
@Composable
fun AddEditMealSheet(
    existingMeal: Meal?,
    dayOfWeek: Int,
    languageViewModel: LanguageViewModel,
    onDismiss: () -> Unit,
    onSave: (Meal) -> Unit
) {
    val c = TajlyTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEdit = existingMeal != null
    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = c.hairStrong,
        focusedTextColor = c.textHi,
        unfocusedTextColor = c.textHi,
        cursorColor = MaterialTheme.colorScheme.primary
    )

    var name by remember { mutableStateOf(existingMeal?.name ?: "") }
    var selectedSlot by remember { mutableStateOf(existingMeal?.mealSlot ?: MealSlot.BREAKFAST) }
    var calories by remember { mutableStateOf(existingMeal?.calories?.toString() ?: "") }
    var protein by remember { mutableStateOf(existingMeal?.protein?.toString() ?: "") }
    var carbs by remember { mutableStateOf(existingMeal?.carbs?.toString() ?: "") }
    var fat by remember { mutableStateOf(existingMeal?.fat?.toString() ?: "") }
    var prepTime by remember { mutableStateOf(existingMeal?.prepTimeMinutes?.let { if (it > 0) it.toString() else "" } ?: "") }
    var ingredients by remember { mutableStateOf(existingMeal?.ingredients?.toMutableList()?.toList() ?: emptyList()) }
    var newIngredient by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf(existingMeal?.instructions?.toMutableList()?.toList() ?: emptyList()) }
    var newInstruction by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(existingMeal?.dietaryTags?.toSet() ?: emptySet()) }

    val availableTags = listOf("High Protein", "Low Carb", "Gluten Free", "Dairy Free", "Vegan", "Vegetarian", "Keto", "Quick")
    val canSave = name.isNotBlank() && (calories.toIntOrNull() ?: 0) > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxSize(),
        sheetState = sheetState,
        dragHandle = null,
        containerColor = c.bg,
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = if (isEdit) languageViewModel.getString(StringKey.EDIT_MEAL)
                else languageViewModel.getString(StringKey.ADD_MEAL),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = c.textHi
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(languageViewModel.getString(StringKey.MEAL_NAME)) },
                placeholder = { Text(languageViewModel.getString(StringKey.MEAL_NAME_HINT)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = tfColors,
                singleLine = true
            )

            Column {
                SectionLabel(languageViewModel.getString(StringKey.MEAL_SLOT))
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MealSlot.entries.forEach { slot ->
                        GlassChip(
                            text = slotLabel(slot, languageViewModel),
                            selected = selectedSlot == slot,
                            onClick = { selectedSlot = slot }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it.filter { c -> c.isDigit() } },
                    label = { Text(languageViewModel.getString(StringKey.CALORIES_LABEL)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = tfColors
                )
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it.filter { c -> c.isDigit() } },
                    label = { Text(languageViewModel.getString(StringKey.PROTEIN_LABEL)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = tfColors
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it.filter { c -> c.isDigit() } },
                    label = { Text(languageViewModel.getString(StringKey.CARBS_LABEL)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = tfColors
                )
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it.filter { c -> c.isDigit() } },
                    label = { Text(languageViewModel.getString(StringKey.FAT_LABEL)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = tfColors
                )
            }

            OutlinedTextField(
                value = prepTime,
                onValueChange = { prepTime = it.filter { c -> c.isDigit() } },
                label = { Text(languageViewModel.getString(StringKey.PREP_TIME_LABEL)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = tfColors
            )

            Column {
                SectionLabel(languageViewModel.getString(StringKey.INGREDIENTS))
                Spacer(modifier = Modifier.height(8.dp))
                ingredients.forEachIndexed { index, ingredient ->
                    GlassRow {
                        Text(
                            text = ingredient,
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textHi,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { ingredients = ingredients.toMutableList().also { it.removeAt(index) } },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                TablerIcons.Trash,
                                contentDescription = languageViewModel.getString(StringKey.REMOVE),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newIngredient,
                        onValueChange = { newIngredient = it },
                        placeholder = { Text(languageViewModel.getString(StringKey.INGREDIENT_HINT)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = tfColors
                    )
                    GoldAddButton(
                        contentDescription = languageViewModel.getString(StringKey.ADD_INGREDIENT),
                        onClick = {
                            if (newIngredient.isNotBlank()) {
                                ingredients = ingredients + newIngredient.trim()
                                newIngredient = ""
                            }
                        }
                    )
                }
            }

            Column {
                SectionLabel(languageViewModel.getString(StringKey.INSTRUCTIONS))
                Spacer(modifier = Modifier.height(8.dp))
                instructions.forEachIndexed { index, instruction ->
                    GlassRow {
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            text = instruction,
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textHi,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { instructions = instructions.toMutableList().also { it.removeAt(index) } },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                TablerIcons.Trash,
                                contentDescription = languageViewModel.getString(StringKey.REMOVE),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newInstruction,
                        onValueChange = { newInstruction = it },
                        placeholder = { Text(languageViewModel.getString(StringKey.INSTRUCTION_HINT)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = tfColors
                    )
                    GoldAddButton(
                        contentDescription = languageViewModel.getString(StringKey.ADD_INSTRUCTION),
                        onClick = {
                            if (newInstruction.isNotBlank()) {
                                instructions = instructions + newInstruction.trim()
                                newInstruction = ""
                            }
                        }
                    )
                }
            }

            Column {
                SectionLabel(languageViewModel.getString(StringKey.DIETARY_TAGS))
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableTags.forEach { tag ->
                        GlassChip(
                            text = tag,
                            selected = selectedTags.contains(tag),
                            onClick = {
                                selectedTags = if (selectedTags.contains(tag))
                                    selectedTags - tag else selectedTags + tag
                            }
                        )
                    }
                }
            }

            GoldButton(
                text = languageViewModel.getString(StringKey.SAVE_MEAL),
                enabled = canSave,
                onClick = {
                    val meal = Meal(
                        id = existingMeal?.id ?: "manual_${Uuid.random()}",
                        name = name.trim(),
                        mealSlot = selectedSlot,
                        dayOfWeek = existingMeal?.dayOfWeek ?: dayOfWeek,
                        calories = calories.toIntOrNull() ?: 0,
                        protein = protein.toIntOrNull() ?: 0,
                        carbs = carbs.toIntOrNull() ?: 0,
                        fat = fat.toIntOrNull() ?: 0,
                        ingredients = ingredients,
                        instructions = instructions,
                        prepTimeMinutes = prepTime.toIntOrNull() ?: 0,
                        dietaryTags = selectedTags.toList()
                    )
                    onSave(meal)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TajlyTheme.colors.textHi
    )
}

@Composable
private fun GlassRow(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    val c = TajlyTheme.colors
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.glassFill, shape)
            .border(1.dp, c.hairStrong, shape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun GoldAddButton(
    contentDescription: String,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .pressScale(interaction)
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Tajly.GoldGradient)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            TablerIcons.Plus,
            contentDescription = contentDescription,
            tint = OnGold,
            modifier = Modifier.size(22.dp)
        )
    }
}
