package org.awi.fitness.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import org.awi.fitness.model.*
import kotlinx.coroutines.launch
import org.awi.fitness.data.StringKey
import org.awi.fitness.utils.fitnessTips
import org.awi.fitness.utils.topFiveTips
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.LocalLanguageViewModel
import org.awi.fitness.viewmodel.LocalWorkoutViewModel
import org.awi.fitness.ui.components.CitationSection
import org.awi.fitness.utils.Citations

class WorkoutScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = LocalWorkoutViewModel.current
        val uiState by viewModel.state.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        var showGoalsSheet by remember { mutableStateOf(false) }
        var selectedPlanIndex by remember { mutableStateOf(0) }
        var showDeleteConfirmation by remember { mutableStateOf<String?>(null) }
        var showListView by remember { mutableStateOf(false) }
        var isDeleting by remember { mutableStateOf(false) }
        val languageViewModel = LocalLanguageViewModel.current

        LaunchedEffect(Unit) {
            viewModel.loadIfNeeded()
        }

        if (showDeleteConfirmation != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = null },
                title = { Text(languageViewModel.getString(StringKey.DELETE_WORKOUT_PLAN)) },
                text = { 
                    Column {
                        Text(languageViewModel.getString(StringKey.DELETE_WORKOUT_CONFIRM))
                        if (isDeleting) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isDeleting = true
                            coroutineScope.launch {
                                viewModel.deleteWorkoutPlan(showDeleteConfirmation!!)
                                isDeleting = false
                                showDeleteConfirmation = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = !isDeleting
                    ) {
                        Text(languageViewModel.getString(StringKey.DELETE))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmation = null },
                        enabled = !isDeleting
                    ) {
                        Text(languageViewModel.getString(StringKey.CANCEL))
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Header with title and add button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = languageViewModel.getString(StringKey.WORKOUT_PLANS),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { showListView = !showListView },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (showListView) TablerIcons.LayoutGrid else TablerIcons.LayoutList,
                            contentDescription = if (showListView) languageViewModel.getString(StringKey.SHOW_GRID) else languageViewModel.getString(StringKey.SHOW_LIST)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showListView) languageViewModel.getString(StringKey.SHOW_GRID) else languageViewModel.getString(StringKey.SHOW_LIST))
                    }
                    
                    IconButton(
                        onClick = { showGoalsSheet = !showGoalsSheet }
                    ) {
                        Icon(
                            imageVector = TablerIcons.Plus,
                            contentDescription = "Add New Plan",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

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
                    ErrorState(
                        error = uiState.error,
                        onRetry = {
                            coroutineScope.launch {
                                viewModel.loadWorkoutPlans()
                            }
                        },
                        languageViewModel = languageViewModel
                    )
                }
                uiState.workoutPlans.isEmpty() -> {
                    EmptyWorkoutState(
                        onSetupClick = { showGoalsSheet = true },
                        languageViewModel = languageViewModel
                    )
                }
                else -> {
                    val sortedPlans = uiState.workoutPlans.sortedByDescending { it.plan.id }
                    
                    if (showListView) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(sortedPlans) { planWithExercises ->
                                WorkoutPlanListCard(
                                    plan = planWithExercises.plan,
                                    isSelected = sortedPlans.indexOf(planWithExercises) == selectedPlanIndex,
                                    onClick = {
                                        selectedPlanIndex = sortedPlans.indexOf(planWithExercises)
                                        showListView = false // Switch to grid view
                                    },
                                    onDeleteClick = {
                                        showDeleteConfirmation = planWithExercises.plan.id
                                    },
                                    languageViewModel = languageViewModel
                                )
                            }
                        }
                    } else {
                        // Workout Plans Carousel
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(sortedPlans) { planWithExercises ->
                                WorkoutPlanCard(
                                    plan = planWithExercises.plan,
                                    isSelected = sortedPlans.indexOf(planWithExercises) == selectedPlanIndex,
                                    onClick = {
                                        selectedPlanIndex = sortedPlans.indexOf(planWithExercises)
                                    },
                                    onDeleteClick = {
                                        showDeleteConfirmation = planWithExercises.plan.id
                                    },
                                    languageViewModel = languageViewModel
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Selected Plan Content
                        if (selectedPlanIndex < sortedPlans.size) {
                            val selectedPlan = sortedPlans[selectedPlanIndex]
                            val exercisesForDay = selectedPlan.exercises.sortedBy { it.orderInDay }
                            
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (exercisesForDay.isNotEmpty()) {
                                    item {
                                        EnhancedWorkoutCard(
                                            workoutPlan = selectedPlan.plan,
                                            exercises = exercisesForDay.sortedWith(
                                                compareBy({ it.isCompleted }, { it.orderInDay })
                                            ),
                                            onExerciseClick = { 
                                                // Navigate to progress screen
                                            },
                                            onCompleteClick = { exercise, completed ->
                                                coroutineScope.launch {
                                                    viewModel.setExerciseCompleted(exercise, completed)
                                                }
                                            },
                                            languageViewModel = languageViewModel
                                        )
                                    }

                                    item {
                                        ExerciseDetailsCard(
                                            exercises = exercisesForDay.sortedWith(
                                                compareBy({ it.isCompleted }, { it.orderInDay })
                                            ),
                                            languageViewModel = languageViewModel
                                        )
                                    }

                                    item {
                                        WorkoutTipsCard(
                                            tips = fitnessTips.topFiveTips(),
                                            difficulty = selectedPlan.plan.difficulty,
                                            languageViewModel = languageViewModel
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showGoalsSheet) {
            UserFitnessGoalsBottomSheet(
                viewModel = viewModel,
                onGoalsSet = {
                    coroutineScope.launch {
                        viewModel.loadWorkoutPlans()
                    }
                },
                onDismiss = { showGoalsSheet = false },
                languageViewModel = languageViewModel
            )
        }
    }
}

@Composable
private fun WorkoutPlanCard(
    plan: WorkoutPlan,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    languageViewModel: LanguageViewModel
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (plan.category) {
                        WorkoutCategory.STRENGTH -> TablerIcons.Walk
                        WorkoutCategory.CARDIO -> TablerIcons.Run
                        WorkoutCategory.HIIT -> TablerIcons.Flame
                        WorkoutCategory.FLEXIBILITY -> TablerIcons.BallVolleyball
                        WorkoutCategory.YOGA -> TablerIcons.Package
                    },
                    contentDescription = null,
                    tint = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = TablerIcons.Trash,
                        contentDescription = "Delete Plan",
                        tint = if (isSelected)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = plan.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = plan.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = TablerIcons.Clock,
                        contentDescription = null,
                        tint = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${plan.duration} ${languageViewModel.getString(StringKey.WEEKS)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = TablerIcons.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyWorkoutState(
    onSetupClick: () -> Unit,
    languageViewModel: LanguageViewModel
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
            text = languageViewModel.getString(StringKey.NO_WORKOUT_PLAN),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = languageViewModel.getString(StringKey.NO_WORKOUT_PLAN_DESC),
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
            Text(languageViewModel.getString(StringKey.SET_UP_MY_GOALS))
        }
    }
}

@Composable
private fun ExerciseDetailsCard(
    exercises: List<Exercise>,
    languageViewModel: LanguageViewModel
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = languageViewModel.getString(StringKey.EXERCISE_DETAILS),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = TablerIcons.Pacman,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = exercise.orderInDay.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = exercise.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (exercise.isCompleted) {
                                    Text(
                                        text = languageViewModel.getString(StringKey.COMPLETED),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
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
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MetricItem(
                                    icon = TablerIcons.Repeat,
                                    label = languageViewModel.getString(StringKey.SETS),
                                    value = exercise.sets.toString()
                                )
                                MetricItem(
                                    icon = TablerIcons.RotateClockwise,
                                    label = languageViewModel.getString(StringKey.REPS),
                                    value = exercise.reps.toString()
                                )
                                MetricItem(
                                    icon = TablerIcons.Clock,
                                    label = languageViewModel.getString(StringKey.REST),
                                    value = "${exercise.restTime}s"
                                )
                            }
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
private fun MetricItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun WorkoutTipsCard(
    tips: List<String>,
    difficulty: WorkoutDifficulty,
    languageViewModel: LanguageViewModel
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = languageViewModel.getString(StringKey.AI_POWERED_TIPS),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = TablerIcons.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
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
    onCompleteClick: (Exercise, Boolean) -> Unit,
    languageViewModel: LanguageViewModel
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
            // Header Section with AI Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = workoutPlan.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                    label = languageViewModel.getString(StringKey.DURATION),
                    value = "${workoutPlan.duration} min"
                )
                WorkoutStat(
                    icon = TablerIcons.ShieldCheck,
                    label = languageViewModel.getString(StringKey.CATEGORY),
                    value = workoutPlan.category.name.lowercase().replaceFirstChar { it.uppercase() }
                )
                WorkoutStat(
                    icon = TablerIcons.Walk,
                    label = languageViewModel.getString(StringKey.EXERCISES),
                    value = exercises.size.toString()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Exercise Preview
            Text(
                text = languageViewModel.getString(StringKey.TODAYS_EXERCISES),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            exercises.forEach { exercise ->
                ExerciseRow(
                    exercise = exercise,
                    workoutCategory = workoutPlan.category,
                    onCompleteClick = { completed ->
                        onCompleteClick(exercise, completed)
                    }
                )
            }
        }
    }
}

@Composable
private fun ExerciseRow(
    exercise: Exercise,
    workoutCategory: WorkoutCategory,
    onCompleteClick: (Boolean) -> Unit
) {
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
                imageVector = when (workoutCategory) {
                    WorkoutCategory.STRENGTH -> TablerIcons.Walk
                    WorkoutCategory.CARDIO -> TablerIcons.Run
                    WorkoutCategory.HIIT -> TablerIcons.Flame
                    WorkoutCategory.FLEXIBILITY -> TablerIcons.BallVolleyball
                    WorkoutCategory.YOGA -> TablerIcons.Pacman
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (exercise.isCompleted) 0.5f else 0.7f),
                modifier = Modifier.size(20.dp)
            )
            
            Column {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = if (exercise.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (exercise.isCompleted) 0.5f else 1f)
                )
                Text(
                    text = "${exercise.sets} sets × ${exercise.reps} reps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
        
        Checkbox(
            checked = exercise.isCompleted,
            onCheckedChange = { completed ->
                onCompleteClick(completed)
            },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        )
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

@Composable
private fun ErrorState(
    error: String?,
    onRetry: () -> Unit,
    languageViewModel: LanguageViewModel
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = error ?: languageViewModel.getString(StringKey.AN_ERROR_OCCURRED),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onRetry) {
                Text(languageViewModel.getString(StringKey.RETRY))
            }
        }
    }
} 

@Composable
private fun WorkoutPlanListCard(
    plan: WorkoutPlan,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    languageViewModel: LanguageViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon
                Surface(
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = when (plan.category) {
                            WorkoutCategory.STRENGTH -> TablerIcons.Walk
                            WorkoutCategory.CARDIO -> TablerIcons.Run
                            WorkoutCategory.HIIT -> TablerIcons.Flame
                            WorkoutCategory.FLEXIBILITY -> TablerIcons.BallVolleyball
                            WorkoutCategory.YOGA -> TablerIcons.Package
                        },
                        contentDescription = null,
                        tint = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(12.dp)
                    )
                }

                // Text Content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = plan.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        // Duration
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = TablerIcons.Clock,
                                contentDescription = null,
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${plan.duration} ${languageViewModel.getString(StringKey.WEEKS)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        // Difficulty
                        Surface(
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = plan.difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Delete Button
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = TablerIcons.Trash,
                    contentDescription = "Delete Plan",
                    tint = if (isSelected)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
} 