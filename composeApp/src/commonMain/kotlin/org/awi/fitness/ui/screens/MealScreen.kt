package org.awi.fitness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.Fold
import compose.icons.tablericons.List
import compose.icons.tablericons.Plus
import compose.icons.tablericons.ShoppingCart
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Star
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.Meal
import org.awi.fitness.ui.components.AddEditMealSheet
import org.awi.fitness.ui.components.FitnessButton
import org.awi.fitness.ui.components.MacroSummaryCard
import org.awi.fitness.ui.components.MealSlotSection
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.LocalLanguageViewModel
import org.awi.fitness.viewmodel.LocalMealPlanViewModel
import org.awi.fitness.viewmodel.MealPlanViewModel

class MealScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userSettings = UserSettings.getInstance()
        val viewModel = LocalMealPlanViewModel.current
        val languageViewModel = LocalLanguageViewModel.current
        val state by viewModel.state.collectAsState()
        val completions by userSettings.mealCompletions.collectAsState()
        var showSetupSheet by remember { mutableStateOf(false) }
        var showAddMealSheet by remember { mutableStateOf(false) }
        var showAddMenu by remember { mutableStateOf(false) }
        var mealToConfirm by remember { mutableStateOf<Meal?>(null) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            viewModel.loadIfNeeded()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                state.activePlan == null && !state.isLoading -> {
                    EmptyMealPlanState(
                        languageViewModel = languageViewModel,
                        onCreateClick = { showSetupSheet = true }
                    )
                }

                else -> {
                    val plan = state.activePlan!!
                    val selectedDay = state.selectedDayOfWeek
                    val dateString = state.selectedDateString
                    val slots = viewModel.getSlotsForDay(selectedDay)
                    val macros = viewModel.getDailyMacros(selectedDay, dateString)
                    val completedForDate = completions[dateString] ?: emptySet()

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = languageViewModel.getString(StringKey.MEAL_PLAN),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Row {
                                    Box {
                                        IconButton(onClick = { showAddMenu = true }) {
                                            Icon(
                                                imageVector = TablerIcons.Plus,
                                                contentDescription = languageViewModel.getString(StringKey.ADD_MEAL),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showAddMenu,
                                            onDismissRequest = { showAddMenu = false },
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(languageViewModel.getString(StringKey.GENERATE_WITH_AI)) },
                                                onClick = {
                                                    showAddMenu = false
                                                    showSetupSheet = true
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        TablerIcons.Star,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(languageViewModel.getString(StringKey.ADD_MANUALLY)) },
                                                onClick = {
                                                    showAddMenu = false
                                                    showAddMealSheet = true
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        TablerIcons.Edit,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            )
                                        }
                                    }
                                    IconButton(onClick = { navigator.push(MyPlansScreen(viewModel)) }) {
                                        Icon(
                                            imageVector = TablerIcons.List,
                                            contentDescription = languageViewModel.getString(StringKey.MY_PLANS),
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    IconButton(onClick = { navigator.push(ShoppingListScreen(plan, languageViewModel)) }) {
                                        Icon(
                                            imageVector = TablerIcons.ShoppingCart,
                                            contentDescription = languageViewModel.getString(StringKey.SHOPPING_LIST),
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            DaySelector(
                                selectedDay = selectedDay,
                                todayDay = state.todayDayOfWeek,
                                onDaySelected = { viewModel.selectDay(it) }
                            )
                        }

                        item {
                            MacroSummaryCard(macros = macros)
                        }

                        items(slots) { slot ->
                            val mealsForSlot = viewModel.getMealsForSlot(selectedDay, slot)
                            MealSlotSection(
                                slot = slot,
                                meals = mealsForSlot,
                                completedMealIds = completedForDate,
                                onMealClick = { meal ->
                                    navigator.push(MealDetailScreen(meal.id, viewModel, languageViewModel))
                                },
                                onToggleCompletion = { meal ->
                                    if (!viewModel.isSelectedDayToday()) {
                                        mealToConfirm = meal
                                    } else {
                                        viewModel.toggleMealCompletion(meal.id, dateString)
                                    }
                                },
                                languageViewModel = languageViewModel
                            )
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
        }

        if (showSetupSheet) {
            MealPlanSetupScreen(
                viewModel = viewModel,
                languageViewModel = languageViewModel,
                onDismiss = { showSetupSheet = false }
            )
        }

        if (showAddMealSheet && state.activePlan != null) {
            AddEditMealSheet(
                existingMeal = null,
                dayOfWeek = state.selectedDayOfWeek,
                languageViewModel = languageViewModel,
                onDismiss = { showAddMealSheet = false },
                onSave = { meal ->
                    scope.launch {
                        viewModel.addManualMeal(meal)
                        showAddMealSheet = false
                    }
                }
            )
        }

        mealToConfirm?.let { meal ->
            val dateString = state.selectedDateString
            AlertDialog(
                onDismissRequest = { mealToConfirm = null },
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
                        mealToConfirm = null
                    }) {
                        Text(
                            languageViewModel.getString(StringKey.MARK_ANYWAY),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mealToConfirm = null }) {
                        Text(languageViewModel.getString(StringKey.CANCEL))
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
private fun EmptyMealPlanState(
    languageViewModel: LanguageViewModel,
    onCreateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = TablerIcons.Fold,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = languageViewModel.getString(StringKey.PLAN_MEALS_WITH_AI),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = languageViewModel.getString(StringKey.PLAN_MEALS_WITH_AI_DESC),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        FitnessButton(
            onClick = onCreateClick,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(languageViewModel.getString(StringKey.CREATE_MY_PLAN))
        }
    }
}

@Composable
private fun DaySelector(
    selectedDay: Int,
    todayDay: Int,
    onDaySelected: (Int) -> Unit
) {
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val todayDayOfWeek = today.dayOfWeek.ordinal + 1

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        dayLabels.forEachIndexed { index, label ->
            val day = index + 1
            val isSelected = day == selectedDay
            val isToday = day == todayDay

            val diff = day - todayDayOfWeek
            val date = today.plus(DatePeriod(days = diff))
            val dateNumber = date.dayOfMonth

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onDaySelected(day) }
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$dateNumber",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
