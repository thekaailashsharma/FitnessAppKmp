package org.awi.fitness.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import org.awi.fitness.model.*
import org.awi.fitness.ui.components.WeekDaysSelector
import org.awi.fitness.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch

class WorkoutScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = remember { WorkoutViewModel() }
        val uiState by viewModel.state.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        var showGoalsSheet by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            println("Loading workout plans...")
            viewModel.loadWorkoutPlans()
            println("Workout plans loaded, state: ${uiState.workoutPlans.size} plans")
        }

        if (showGoalsSheet) {
            UserFitnessGoalsBottomSheet(
                viewModel = viewModel,
                onGoalsSet = {
                    coroutineScope.launch {
                        viewModel.loadWorkoutPlans()
                    }
                },
                onDismiss = { showGoalsSheet = false }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Workout Schedule",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Row {
                    IconButton(
                        onClick = { showGoalsSheet = true }
                    ) {
                        Icon(
                            imageVector = TablerIcons.Edit,
                            contentDescription = "Edit Goals",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            uiState.selectedPlanId?.let { planId ->
                                // Navigate to progress screen
                            }
                        },
                        enabled = uiState.selectedPlanId != null
                    ) {
                        Icon(
                            imageVector = TablerIcons.ChartLine,
                            contentDescription = "View Progress",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            WeekDaysSelector(
                selectedDay = uiState.selectedDayOfWeek,
                onDaySelected = viewModel::updateSelectedDay
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "An error occurred",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                uiState.workoutPlans.isEmpty() -> {
                    EmptyWorkoutState(
                        onSetupClick = {
                            // Navigate to goals screen
                        }
                    )
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.workoutPlans) { planWithExercises ->
                            EnhancedWorkoutCard(
                                workoutPlan = planWithExercises.plan,
                                exercises = planWithExercises.exercises,
                                onExerciseClick = { 
                                    // Navigate to progress screen
                                },
                                onCompleteClick = { exerciseId, completed ->
                                    coroutineScope.launch {
                                        viewModel.setExerciseCompleted(exerciseId, completed)
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            ExerciseDetailsCard(
                                exercises = planWithExercises.exercises
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            
                            WorkoutTipsCard(
                                difficulty = planWithExercises.plan.difficulty
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyWorkoutState(
    onSetupClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = TablerIcons.Walk,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Workout Plan Yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "Set up your fitness goals to get a personalized plan",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onSetupClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Set Up My Goals")
        }
    }
}

@Composable
private fun ExerciseDetailsCard(
    exercises: List<Exercise>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Exercise Details",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            exercises.forEach { exercise ->
                var expanded by remember { mutableStateOf(false) }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = if (expanded) TablerIcons.ChevronUp else TablerIcons.ChevronDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    AnimatedVisibility(visible = expanded) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = exercise.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            ExerciseMetrics(exercise)
                        }
                    }
                }
                
                if (exercise != exercises.last()) {
                    Divider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseMetrics(exercise: Exercise) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MetricItem("Sets", exercise.sets.toString())
        MetricItem("Reps", exercise.reps.toString())
        MetricItem("Rest", "${exercise.restTime}s")
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun WorkoutTipsCard(difficulty: WorkoutDifficulty) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Pro Tips",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val tips = when (difficulty) {
                WorkoutDifficulty.BEGINNER -> listOf(
                    "Start slowly and focus on form",
                    "Take longer rest periods if needed",
                    "Stay hydrated throughout your workout"
                )
                WorkoutDifficulty.INTERMEDIATE -> listOf(
                    "Challenge yourself with proper progression",
                    "Mix up your routine to avoid plateaus",
                    "Focus on mind-muscle connection"
                )
                WorkoutDifficulty.ADVANCED -> listOf(
                    "Incorporate advanced techniques",
                    "Monitor your recovery closely",
                    "Track your progress meticulously"
                )
            }
            
            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = TablerIcons.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedWorkoutCard(
    workoutPlan: WorkoutPlan,
    exercises: List<Exercise>,
    onExerciseClick: () -> Unit,
    onCompleteClick: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = workoutPlan.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = workoutPlan.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                IconButton(onClick = onExerciseClick) {
                    Icon(
                        imageVector = TablerIcons.PlayerPlay,
                        contentDescription = "Start Workout",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Workout Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WorkoutStat(
                    icon = TablerIcons.Clock,
                    label = "Duration",
                    value = "${exercises.size * 5} min"
                )
                WorkoutStat(
                    icon = TablerIcons.Flame,
                    label = "Calories",
                    value = "${exercises.size * 12} kcal"
                )
                WorkoutStat(
                    icon = TablerIcons.Walk,
                    label = "Exercises",
                    value = exercises.size.toString()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Exercise Preview
            Text(
                text = "Today's Exercises",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            exercises.take(3).forEach { exercise ->
                var isCompleted by remember { mutableStateOf(exercise.isCompleted) }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (workoutPlan.category) {
                                WorkoutCategory.STRENGTH -> TablerIcons.Walk
                                WorkoutCategory.CARDIO -> TablerIcons.Run
                                WorkoutCategory.HIIT -> TablerIcons.Flame
                                WorkoutCategory.FLEXIBILITY -> TablerIcons.BallVolleyball
                                WorkoutCategory.YOGA -> TablerIcons.Dna
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isCompleted) 0.5f else 1f)
                        )
                    }
                    
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { completed ->
                            isCompleted = completed
                            onCompleteClick(exercise.id, completed)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            if (exercises.size > 3) {
                TextButton(
                    onClick = onExerciseClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("View All ${exercises.size} Exercises")
                }
            }
        }
    }
}

@Composable
private fun WorkoutStat(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
} 