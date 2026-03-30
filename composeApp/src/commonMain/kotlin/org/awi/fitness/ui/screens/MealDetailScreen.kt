package org.awi.fitness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Check
import compose.icons.tablericons.Circle
import compose.icons.tablericons.Clock
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.Trash
import kotlinx.coroutines.launch
import org.awi.fitness.ui.components.AddEditMealSheet
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.Meal
import org.awi.fitness.ui.components.FitnessButton
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.MealPlanViewModel

class MealDetailScreen(
    private val mealId: String,
    private val viewModel: MealPlanViewModel,
    private val languageViewModel: LanguageViewModel
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val state by viewModel.state.collectAsState()
        val scope = rememberCoroutineScope()
        val dateString = state.selectedDateString
        val userSettings = UserSettings.getInstance()
        val completions by userSettings.mealCompletions.collectAsState()

        val meal = remember(state.activePlan, mealId) {
            state.activePlan?.meals?.find { it.id == mealId }
        }

        if (meal == null) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return
        }

        val isCompleted = completions[dateString]?.contains(meal.id) == true
        val checkedIngredients = remember { mutableStateOf(setOf<Int>()) }
        var showNotTodayDialog by remember { mutableStateOf(false) }
        var showEditSheet by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showSwapDialog by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = meal.name,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(TablerIcons.ArrowLeft, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showEditSheet = true }) {
                            Icon(
                                TablerIcons.Edit,
                                contentDescription = languageViewModel.getString(StringKey.EDIT),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                TablerIcons.Trash,
                                contentDescription = languageViewModel.getString(StringKey.DELETE),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MacroPill("${meal.calories}", "kcal", Modifier.weight(1f))
                        MacroPill("${meal.protein}g", "protein", Modifier.weight(1f))
                        MacroPill("${meal.carbs}g", "carbs", Modifier.weight(1f))
                        MacroPill("${meal.fat}g", "fat", Modifier.weight(1f))
                    }
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (meal.prepTimeMinutes > 0) {
                            Icon(
                                TablerIcons.Clock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${meal.prepTimeMinutes} ${languageViewModel.getString(StringKey.MIN_SHORT)} ${languageViewModel.getString(StringKey.PREP_TIME)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        meal.dietaryTags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                if (meal.ingredients.isNotEmpty()) {
                    item {
                        Text(
                            text = languageViewModel.getString(StringKey.INGREDIENTS),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    itemsIndexed(meal.ingredients) { index, ingredient ->
                        val checked = checkedIngredients.value.contains(index)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (checked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = {
                                        checkedIngredients.value = if (checked)
                                            checkedIngredients.value - index
                                        else
                                            checkedIngredients.value + index
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (checked) TablerIcons.Check else TablerIcons.Circle,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (checked) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = ingredient,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }
                    }
                }

                if (meal.instructions.isNotEmpty()) {
                    item {
                        Text(
                            text = languageViewModel.getString(StringKey.INSTRUCTIONS),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    itemsIndexed(meal.instructions) { index, instruction ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${index + 1}.",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = instruction,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showSwapDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isSwapping,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isSwapping) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(languageViewModel.getString(StringKey.SWAPPING_MEAL))
                        } else {
                            Icon(TablerIcons.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(languageViewModel.getString(StringKey.SWAP_THIS_MEAL))
                        }
                    }
                }

                item {
                    FitnessButton(
                        onClick = {
                            if (!viewModel.isSelectedDayToday()) {
                                showNotTodayDialog = true
                            } else {
                                viewModel.toggleMealCompletion(meal.id, dateString)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isCompleted) languageViewModel.getString(StringKey.COMPLETED)
                            else languageViewModel.getString(StringKey.MARK_AS_EATEN)
                        )
                    }
                }
            }
        }

        if (showEditSheet) {
            AddEditMealSheet(
                existingMeal = meal,
                dayOfWeek = meal.dayOfWeek,
                languageViewModel = languageViewModel,
                onDismiss = { showEditSheet = false },
                onSave = { updatedMeal ->
                    scope.launch {
                        viewModel.updateMeal(updatedMeal)
                        showEditSheet = false
                    }
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(languageViewModel.getString(StringKey.DELETE_MEAL)) },
                text = { Text(languageViewModel.getString(StringKey.DELETE_MEAL_CONFIRM)) },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            viewModel.removeMeal(meal.id)
                            showDeleteDialog = false
                            navigator.pop()
                        }
                    }) {
                        Text(
                            languageViewModel.getString(StringKey.DELETE),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(languageViewModel.getString(StringKey.CANCEL))
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        if (showSwapDialog) {
            AlertDialog(
                onDismissRequest = { showSwapDialog = false },
                title = {
                    Text(
                        languageViewModel.getString(StringKey.SWAP_CONFIRM_TITLE),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Text(
                        languageViewModel.getString(StringKey.SWAP_CONFIRM_DESC),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showSwapDialog = false
                        scope.launch {
                            viewModel.swapMeal(meal)
                        }
                    }) {
                        Text(
                            languageViewModel.getString(StringKey.SWAP_THIS_MEAL),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSwapDialog = false }) {
                        Text(languageViewModel.getString(StringKey.CANCEL))
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        if (showNotTodayDialog) {
            AlertDialog(
                onDismissRequest = { showNotTodayDialog = false },
                title = {
                    Text(
                        languageViewModel.getString(StringKey.NOT_TODAY_TITLE),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Text(
                        languageViewModel.getString(StringKey.NOT_TODAY_DESC),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.toggleMealCompletion(meal.id, dateString)
                        showNotTodayDialog = false
                    }) {
                        Text(
                            languageViewModel.getString(StringKey.MARK_ANYWAY),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNotTodayDialog = false }) {
                        Text(languageViewModel.getString(StringKey.CANCEL))
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
private fun MacroPill(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
