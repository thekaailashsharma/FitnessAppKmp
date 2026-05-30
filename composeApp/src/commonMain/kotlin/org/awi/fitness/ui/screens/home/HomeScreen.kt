package org.awi.fitness.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import org.awi.fitness.ui.CalorieCalculatorScreen
import org.awi.fitness.ui.screens.ProfileScreen
import org.awi.fitness.viewmodel.HomeViewModel
import kotlin.math.roundToInt
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.Meal
import org.awi.fitness.model.MealSlot
import org.awi.fitness.ui.screens.WorkoutSchedulerScreen
import org.awi.fitness.viewmodel.DailyTip
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.LocalHomeViewModel
import org.awi.fitness.viewmodel.LocalLanguageViewModel
import org.awi.fitness.viewmodel.LocalMealPlanViewModel
import org.awi.fitness.viewmodel.TipIcon
import org.awi.fitness.viewmodel.WorkoutSchedulePreview
import kotlinx.datetime.Clock

class HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = LocalHomeViewModel.current
        val state by viewModel.state.collectAsState()
        val languageViewModel = LocalLanguageViewModel.current
        val mealPlanViewModel = LocalMealPlanViewModel.current
        val mealState by mealPlanViewModel.state.collectAsState()
        val userSettings = UserSettings.getInstance()
        val mealCompletions by userSettings.mealCompletions.collectAsState()
        val currentLanguage by userSettings.language.collectAsState()

        val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
        val todayDow = remember { today.dayOfWeek.ordinal + 1 }
        val todayStr = remember { today.toString() }

        val todayMeals = remember(mealState.activePlan, todayDow) {
            mealState.activePlan?.meals
                ?.filter { it.dayOfWeek == todayDow }
                ?.sortedBy { m -> when (m.mealSlot) { MealSlot.BREAKFAST -> 0; MealSlot.LUNCH -> 1; MealSlot.DINNER -> 2; MealSlot.SNACK -> 3 } }
                ?: emptyList()
        }
        val todayDone = mealCompletions[todayStr] ?: emptySet()
        val mealsEaten = todayMeals.count { todayDone.contains(it.id) }
        val caloriesConsumed = todayMeals.filter { todayDone.contains(it.id) }.sumOf { it.calories }
        val caloriesTarget = mealState.activePlan?.targetCalories ?: 0
        val hasMealPlan = mealState.activePlan != null

        LaunchedEffect(Unit) {
            viewModel.loadIfNeeded()
        }

        // Refresh daily tips whenever language changes
        LaunchedEffect(currentLanguage) {
            viewModel.refresh()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = languageViewModel.getString(StringKey.APP_NAME),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        IconButton(onClick = { navigator.push(ProfileScreen(languageViewModel = languageViewModel)) }) {
                            Icon(
                                imageVector = TablerIcons.User,
                                contentDescription = languageViewModel.getString(StringKey.PROFILE)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { paddingValues ->
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.error ?: languageViewModel.getString(StringKey.AN_ERROR_OCCURRED),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            WelcomeSection(
                                userName = state.userProfile?.email ?: languageViewModel.getString(StringKey.FITNESS_ENTHUSIAST),
                                languageViewModel = languageViewModel
                            )
                        }

                        item {
                            QuickStatsSection(
                                scheduledToday = state.scheduledWorkoutsToday,
                                caloriesGoal = state.caloriesGoal,
                                mealsEaten = mealsEaten,
                                totalMeals = todayMeals.size,
                                languageViewModel = languageViewModel
                            )
                        }

                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                NavigationCard(
                                    title = languageViewModel.getString(StringKey.CALORIE_CALCULATOR),
                                    description = languageViewModel.getString(StringKey.CALCULATE_DAILY_CALORIES),
                                    icon = TablerIcons.Calculator,
                                    onClick = { navigator.push(CalorieCalculatorScreen()) }
                                )
                                NavigationCard(
                                    title = languageViewModel.getString(StringKey.WORKOUT_SCHEDULE),
                                    description = languageViewModel.getString(StringKey.PLAN_MANAGE_WORKOUT),
                                    icon = TablerIcons.CalendarEvent,
                                    onClick = { navigator.push(WorkoutSchedulerScreen()) }
                                )
                            }
                        }

                        // Fitness Stats or CTA
                        item {
                            if (state.bmr > 0f || state.tdee > 0f) {
                                FitnessStatsSection(
                                    bmr = state.bmr,
                                    tdee = state.tdee,
                                    languageViewModel = languageViewModel
                                )
                            } else {
                                CalculateNeedsCta(
                                    languageViewModel = languageViewModel,
                                    onClick = { navigator.push(CalorieCalculatorScreen()) }
                                )
                            }
                        }

                        if (hasMealPlan && todayMeals.isNotEmpty()) {
                            item {
                                TodaysMealsCard(
                                    meals = todayMeals,
                                    eaten = mealsEaten,
                                    caloriesConsumed = caloriesConsumed,
                                    caloriesTarget = caloriesTarget,
                                    languageViewModel = languageViewModel
                                )
                            }
                        }

                        // Upcoming workout
                        if (state.upcomingWorkout != null) {
                            item {
                                UpcomingWorkoutCard(
                                    workout = state.upcomingWorkout!!,
                                    onClick = { navigator.push(WorkoutSchedulerScreen()) },
                                    languageViewModel = languageViewModel
                                )
                            }
                        }

                        // Daily Tips
                        if (state.dailyTips.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = languageViewModel.getString(StringKey.DAILY_WELLNESS_TIPS),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.refresh() }
                                    ) {
                                        Icon(
                                            imageVector = TablerIcons.Refresh,
                                            contentDescription = languageViewModel.getString(StringKey.REFRESH_TIPS),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            
                            item {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    items(state.dailyTips) { tip ->
                                        TipCard(tip = tip)
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeSection(
    userName: String,
    languageViewModel: LanguageViewModel
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "${languageViewModel.getString(StringKey.WELCOME_BACK)},",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Text(
            text = userName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QuickStatsSection(
    scheduledToday: Int,
    caloriesGoal: Int,
    mealsEaten: Int,
    totalMeals: Int,
    languageViewModel: LanguageViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            value = if (totalMeals > 0) "$mealsEaten/$totalMeals" else "—",
            label = languageViewModel.getString(StringKey.MEALS_PROGRESS),
            icon = TablerIcons.Fold,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "$scheduledToday",
            label = languageViewModel.getString(StringKey.TODAY),
            icon = TablerIcons.CalendarEvent,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = if (caloriesGoal > 0) "$caloriesGoal" else "—",
            label = languageViewModel.getString(StringKey.CALORIE_GOAL),
            icon = TablerIcons.Flame,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun NavigationCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun FitnessStatsSection(
    bmr: Float,
    tdee: Float,
    languageViewModel: LanguageViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = languageViewModel.getString(StringKey.FITNESS_STATS),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = languageViewModel.getString(StringKey.BMR),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = bmr.roundToInt().toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = languageViewModel.getString(StringKey.TDEE),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = tdee.roundToInt().toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CalculateNeedsCta(
    languageViewModel: LanguageViewModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = TablerIcons.Calculator,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = languageViewModel.getString(StringKey.CALCULATE_YOUR_NEEDS),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = languageViewModel.getString(StringKey.CALCULATE_YOUR_NEEDS_DESC),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = TablerIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TodaysMealsCard(
    meals: List<Meal>,
    eaten: Int,
    caloriesConsumed: Int,
    caloriesTarget: Int,
    languageViewModel: LanguageViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = languageViewModel.getString(StringKey.TODAYS_MEALS),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "$eaten/${meals.size} ${languageViewModel.getString(StringKey.MEALS_EATEN)}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (caloriesTarget > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                val progress = (caloriesConsumed.toFloat() / caloriesTarget).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$caloriesConsumed / $caloriesTarget ${languageViewModel.getString(StringKey.KCAL)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            meals.take(4).forEach { meal ->
                val slotColor = when (meal.mealSlot) {
                    MealSlot.BREAKFAST -> Color(0xFFFF9800)
                    MealSlot.LUNCH -> Color(0xFF4CAF50)
                    MealSlot.DINNER -> Color(0xFF2196F3)
                    MealSlot.SNACK -> Color(0xFF9C27B0)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(slotColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = meal.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${meal.calories} ${languageViewModel.getString(StringKey.KCAL)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            if (meals.size > 4) {
                Text(
                    text = "+${meals.size - 4} ${languageViewModel.getString(StringKey.SHOW_MORE).lowercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun UpcomingWorkoutCard(
    workout: WorkoutSchedulePreview,
    onClick: () -> Unit,
    languageViewModel: LanguageViewModel,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(workout.color).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = languageViewModel.getString(StringKey.UPCOMING_WORKOUT),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = CircleShape,
                    color = Color(workout.color).copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            TablerIcons.CalendarEvent,
                            contentDescription = null,
                            tint = Color(workout.color),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = workout.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    TablerIcons.Clock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatTime(workout.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun TipCard(
    tip: DailyTip
) {
    Card(
        modifier = Modifier.width(200.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color(tip.color).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = when (tip.icon) {
                    TipIcon.WATER -> TablerIcons.Glass
                    TipIcon.FOOD -> TablerIcons.Fold
                    TipIcon.SLEEP -> TablerIcons.Moon
                    TipIcon.EXERCISE -> TablerIcons.Run
                    TipIcon.MINDFULNESS -> TablerIcons.Heart
                    TipIcon.HEALTH -> TablerIcons.Heart
                },
                contentDescription = null,
                tint = Color(tip.color),
                modifier = Modifier.size(24.dp)
            )
            
            Text(
                text = tip.tip,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val dateTime = Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}
