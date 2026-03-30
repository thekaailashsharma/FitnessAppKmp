package org.awi.fitness.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.Circle
import compose.icons.tablericons.Flame
import compose.icons.tablericons.Fold
import compose.icons.tablericons.ShoppingCart
import compose.icons.tablericons.Target
import kotlinx.coroutines.launch
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.DietaryType
import org.awi.fitness.model.MealGoal
import org.awi.fitness.ui.components.FitnessButton
import org.awi.fitness.viewmodel.GenerationStep
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.MealPlanViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MealPlanSetupScreen(
    viewModel: MealPlanViewModel,
    languageViewModel: LanguageViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsState()
    val userSettings = UserSettings.getInstance()

    var selectedGoal by remember { mutableStateOf<MealGoal?>(null) }
    var selectedDiet by remember { mutableStateOf<DietaryType?>(null) }
    var restrictions by remember { mutableStateOf("") }
    var mealsPerDay by remember { mutableStateOf(3) }
    var useCalculated by remember { mutableStateOf(userSettings.calculatedCalories > 0) }
    var customCalories by remember { mutableStateOf("2000") }

    val calculatedCal = userSettings.calculatedCalories
    val targetCal = if (useCalculated && calculatedCal > 0) calculatedCal else (customCalories.toIntOrNull() ?: 2000)

    val targetProtein = (targetCal * 0.30 / 4).toInt()
    val targetCarbs = (targetCal * 0.40 / 4).toInt()
    val targetFat = (targetCal * 0.30 / 9).toInt()

    ModalBottomSheet(
        onDismissRequest = { if (!state.isGenerating) onDismiss() },
        modifier = Modifier.fillMaxSize(),
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isGenerating) {
                GeneratingPlanView(languageViewModel, state.generationStep)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Title
                    Column {
                        Text(
                            text = languageViewModel.getString(StringKey.CREATE_YOUR_MEAL_PLAN),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = languageViewModel.getString(StringKey.CREATE_PLAN_SUBTITLE),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }

                    // Goal selection
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Column {
                            Text(
                                text = languageViewModel.getString(StringKey.WHATS_YOUR_GOAL),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MealGoal.entries.forEach { goal ->
                                    SelectableChip(
                                        text = goal.displayName,
                                        isSelected = selectedGoal == goal,
                                        onClick = { selectedGoal = goal }
                                    )
                                }
                            }
                        }
                    }

                    // Dietary preference
                    AnimatedVisibility(
                        visible = selectedGoal != null,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Column {
                            Text(
                                text = languageViewModel.getString(StringKey.DIETARY_PREFERENCE),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DietaryType.entries.forEach { diet ->
                                    SelectableChip(
                                        text = diet.displayName,
                                        isSelected = selectedDiet == diet,
                                        onClick = { selectedDiet = diet }
                                    )
                                }
                            }
                        }
                    }

                    // Restrictions
                    AnimatedVisibility(
                        visible = selectedDiet != null,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Column {
                            Text(
                                text = languageViewModel.getString(StringKey.ANY_ALLERGIES),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = restrictions,
                                onValueChange = { restrictions = it },
                                placeholder = {
                                    Text(
                                        languageViewModel.getString(StringKey.ALLERGIES_HINT),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    // Meals per day
                    AnimatedVisibility(
                        visible = selectedDiet != null,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Column {
                            Text(
                                text = languageViewModel.getString(StringKey.MEALS_PER_DAY),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                listOf(3, 4).forEach { count ->
                                    FilledTonalButton(
                                        onClick = { mealsPerDay = count },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = if (mealsPerDay == count)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (mealsPerDay == count)
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            "$count ${languageViewModel.getString(StringKey.MEALS_PER_DAY).lowercase()}",
                                            fontWeight = if (mealsPerDay == count) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Calories
                    AnimatedVisibility(
                        visible = selectedDiet != null,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Column {
                            Text(
                                text = languageViewModel.getString(StringKey.DAILY_CALORIES),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (calculatedCal > 0) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (useCalculated) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { useCalculated = true }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (useCalculated) TablerIcons.Check else TablerIcons.Circle,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = if (useCalculated) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${languageViewModel.getString(StringKey.USE_CALCULATED_CALORIES)}: $calculatedCal kcal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (!useCalculated || calculatedCal == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { useCalculated = false }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (!useCalculated || calculatedCal == 0) TablerIcons.Check else TablerIcons.Circle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (!useCalculated || calculatedCal == 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = languageViewModel.getString(StringKey.SET_CUSTOM_TARGET),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (!useCalculated || calculatedCal == 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customCalories,
                                    onValueChange = { customCalories = it.filter { c -> c.isDigit() } },
                                    label = { Text("kcal") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                        }
                    }

                    // Generate button
                    AnimatedVisibility(
                        visible = selectedGoal != null && selectedDiet != null,
                        enter = fadeIn() + slideInVertically()
                    ) {
                    FitnessButton(
                        onClick = {
                            scope.launch {
                                val result = viewModel.generateAndSavePlan(
                                    goal = selectedGoal!!,
                                    dietaryType = selectedDiet!!,
                                    restrictions = restrictions,
                                    mealsPerDay = mealsPerDay,
                                    targetCalories = targetCal,
                                    targetProtein = targetProtein,
                                    targetCarbs = targetCarbs,
                                    targetFat = targetFat
                                )
                                if (result.isSuccess) {
                                    onDismiss()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        loading = state.isGenerating
                    ) {
                        Text(languageViewModel.getString(StringKey.GENERATE_MY_PLAN))
                    }
                    }

                    // Error
                    state.error?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun GeneratingPlanView(
    languageViewModel: LanguageViewModel,
    generationStep: GenerationStep
) {
    val steps = listOf(
        Triple(StringKey.STEP_ANALYZING_PREFERENCES, TablerIcons.Target, GenerationStep.ANALYZING_PREFERENCES),
        Triple(StringKey.STEP_GENERATING_MEALS, TablerIcons.Fold, GenerationStep.CALLING_AI),
        Triple(StringKey.STEP_BALANCING_NUTRITION, TablerIcons.Flame, GenerationStep.PARSING_RESPONSE),
        Triple(StringKey.STEP_SAVING_PLAN, TablerIcons.ShoppingCart, GenerationStep.SAVING_PLAN),
    )

    val currentIndex = steps.indexOfFirst { it.third == generationStep }.coerceAtLeast(0)

    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            Box(
                modifier = Modifier
                    .size((90 * pulseScale).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha))
            )
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = steps[currentIndex].second,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = languageViewModel.getString(StringKey.CREATING_YOUR_PLAN),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = languageViewModel.getString(steps[currentIndex].first),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            steps.forEachIndexed { index, (stringKey, icon, _) ->
                val isCompleted = index < currentIndex
                val isCurrent = index == currentIndex

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isCurrent) Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            else Modifier
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    isCurrent -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isCompleted -> Icon(
                                imageVector = TablerIcons.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            isCurrent -> CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            else -> Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            isCurrent -> MaterialTheme.colorScheme.onBackground
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = languageViewModel.getString(stringKey),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant
                            isCurrent -> MaterialTheme.colorScheme.onBackground
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
