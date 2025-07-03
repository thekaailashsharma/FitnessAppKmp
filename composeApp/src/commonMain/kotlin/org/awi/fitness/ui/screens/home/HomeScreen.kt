package org.awi.fitness.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.style.TextAlign
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
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.ui.screens.WorkoutSchedulerScreen
import org.awi.fitness.viewmodel.DailyTip
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.TipIcon
import org.awi.fitness.viewmodel.WorkoutSchedulePreview

class HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val userSettings = UserSettings.getInstance()
        val languageViewModel = LanguageViewModel(userSettings.settings)
        val viewModel = remember { HomeViewModel() }
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

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
                        // Welcome Section
                        item {
                            WelcomeSection(
                                userName = state.userProfile?.email ?: languageViewModel.getString(StringKey.FITNESS_ENTHUSIAST),
                                languageViewModel = languageViewModel
                            )
                        }

                        // Quick Stats
                        item {
                            QuickStatsSection(
                                completedWorkouts = state.completedWorkouts,
                                totalWorkouts = state.totalWorkouts,
                                caloriesGoal = state.caloriesGoal,
                                scheduledToday = state.scheduledWorkoutsToday,
                                languageViewModel = languageViewModel
                            )
                        }

                        // Navigation Cards
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

                        // Fitness Stats
                        item {
                            FitnessStatsSection(
                                bmr = state.bmr,
                                tdee = state.tdee,
                                languageViewModel = languageViewModel
                            )
                        }

                        // Recent Workouts Preview
                        if (state.workoutPlans.isNotEmpty()) {
                            item {
                                Text(
                                    text = languageViewModel.getString(StringKey.RECENT_WORKOUTS),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(minOf(3, state.workoutPlans.size)) { index ->
                                val plan = state.workoutPlans[index]
                                WorkoutPreviewCard(
                                    name = plan.plan.name,
                                    description = plan.plan.description,
                                    completedExercises = plan.exercises.count { it.isCompleted },
                                    totalExercises = plan.exercises.size,
                                    languageViewModel = languageViewModel
                                )
                            }
                        }

                        // Daily Tips Section
                        if (state.dailyTips.isNotEmpty()) {
                            item {
                                Text(
                                    text = languageViewModel.getString(StringKey.DAILY_WELLNESS_TIPS),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
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

                        // Bottom Spacing
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
    completedWorkouts: Int,
    totalWorkouts: Int,
    caloriesGoal: Int,
    scheduledToday: Int,
    languageViewModel: LanguageViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            value = "$completedWorkouts/$totalWorkouts",
            label = languageViewModel.getString(StringKey.WORKOUTS_COMPLETED),
            icon = TablerIcons.Walk,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "$scheduledToday",
            label = languageViewModel.getString(StringKey.TODAY),
            icon = TablerIcons.CalendarEvent,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "$caloriesGoal",
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
private fun WorkoutPreviewCard(
    name: String,
    description: String,
    completedExercises: Int,
    totalExercises: Int,
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
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = if (totalExercises > 0) completedExercises.toFloat() / totalExercises else 0f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            Text(
                text = "$completedExercises of $totalExercises ${languageViewModel.getString(StringKey.EXERCISES_COMPLETED)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun UpcomingWorkoutCard(
    workout: WorkoutSchedulePreview,
    onClick: () -> Unit
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
                    text = "Upcoming Workout",
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
private fun TipCard(tip: DailyTip) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .heightIn(min = 120.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(tip.color).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(tip.color).copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (tip.icon) {
                            TipIcon.WATER -> TablerIcons.Glass
                            TipIcon.FOOD -> TablerIcons.BrandApple
                            TipIcon.SLEEP -> TablerIcons.Moon
                            TipIcon.EXERCISE -> TablerIcons.Walk
                            TipIcon.MINDFULNESS -> TablerIcons.BrandAndroid
                            TipIcon.HEALTH -> TablerIcons.Heart
                        },
                        contentDescription = null,
                        tint = Color(tip.color),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Text(
                text = tip.tip,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val dateTime = Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
} 