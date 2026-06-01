package org.awi.fitness.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import org.awi.fitness.model.FitnessGoal
import org.awi.fitness.model.FitnessLevel
import org.awi.fitness.viewmodel.WorkoutViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch
import org.awi.fitness.data.StringKey
import org.awi.fitness.viewmodel.LanguageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFitnessGoalsBottomSheet(
    languageViewModel: LanguageViewModel,
    viewModel: WorkoutViewModel,
    onGoalsSet: () -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedGoal by remember { mutableStateOf<FitnessGoal?>(null) }
    var fitnessLevel by remember { mutableStateOf<FitnessLevel?>(null) }
    var preferredWorkoutDays by remember { mutableStateOf(3) }
    var specificRequirements by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by viewModel.state.collectAsState()

    // Handle state changes
    LaunchedEffect(state) {
        if (!state.isLoading && state.selectedPlanId != null) {
            // Success case
            onGoalsSet()
            onDismiss()
        }
        isLoading = state.isLoading
        error = state.error
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) onDismiss() },
        modifier = Modifier.fillMaxSize(),
        sheetState = bottomSheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Column {
                        Text(
                            languageViewModel.getString(StringKey.FITNESS_GOALS_TITLE),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            languageViewModel.getString(StringKey.FITNESS_GOALS_SUBTITLE),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }

                GoalSelectionSection(selectedGoal, onGoalSelected = { selectedGoal = it }, languageViewModel = languageViewModel)
                
                AnimatedVisibility(
                    visible = selectedGoal != null,
                    enter = fadeIn() + slideInHorizontally()
                ) {
                    FitnessLevelSection(fitnessLevel, onLevelSelected = { fitnessLevel = it }, languageViewModel = languageViewModel)
                }

                AnimatedVisibility(
                    visible = fitnessLevel != null,
                    enter = fadeIn() + slideInHorizontally()
                ) {
                    WorkoutDaysSection(preferredWorkoutDays, onDaysChanged = { preferredWorkoutDays = it }, languageViewModel = languageViewModel)
                }

                Button(
                    onClick = {
                        if (selectedGoal != null && fitnessLevel != null) {
                            error = null
                            coroutineScope.launch {
                                viewModel.saveUserGoals(
                                    selectedGoal!!,
                                    fitnessLevel!!,
                                    preferredWorkoutDays,
                                    specificRequirements
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                        .animateContentSize(),
                    enabled = selectedGoal != null && fitnessLevel != null && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(languageViewModel.getString(StringKey.START_FITNESS_JOURNEY))
                    }
                }
            }

            // Loading overlay
            this@ModalBottomSheet.AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            // Error message
            this@ModalBottomSheet.AnimatedVisibility(
                visible = error != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                error?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalSelectionSection(
    selectedGoal: FitnessGoal?,
    onGoalSelected: (FitnessGoal) -> Unit,
    languageViewModel: LanguageViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            languageViewModel.getString(StringKey.FITNESS_GOAL_QUESTION),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FitnessGoal.values().forEach { goal ->
                GoalCard(
                    goal = goal,
                    isSelected = goal == selectedGoal,
                    onClick = { onGoalSelected(goal) },
                    languageViewModel = languageViewModel
                )
            }
        }
    }
}

@Composable
private fun GoalCard(
    goal: FitnessGoal,
    isSelected: Boolean,
    onClick: () -> Unit,
    languageViewModel: LanguageViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = when (goal) {
                    FitnessGoal.WEIGHT_LOSS -> TablerIcons.Scale
                    FitnessGoal.MUSCLE_GAIN -> TablerIcons.Walk
                    FitnessGoal.ENDURANCE -> TablerIcons.Run
                    FitnessGoal.FLEXIBILITY -> TablerIcons.BallVolleyball
                },
                contentDescription = null,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column {
                Text(
                    text = when (goal) {
                        FitnessGoal.WEIGHT_LOSS -> languageViewModel.getString(StringKey.GOAL_WEIGHT_LOSS)
                        FitnessGoal.MUSCLE_GAIN -> languageViewModel.getString(StringKey.GOAL_MUSCLE_GAIN)
                        FitnessGoal.ENDURANCE -> languageViewModel.getString(StringKey.GOAL_ENDURANCE)
                        FitnessGoal.FLEXIBILITY -> languageViewModel.getString(StringKey.GOAL_FLEXIBILITY)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when (goal) {
                        FitnessGoal.WEIGHT_LOSS -> languageViewModel.getString(StringKey.GOAL_WEIGHT_LOSS_DESC)
                        FitnessGoal.MUSCLE_GAIN -> languageViewModel.getString(StringKey.GOAL_MUSCLE_GAIN_DESC)
                        FitnessGoal.ENDURANCE -> languageViewModel.getString(StringKey.GOAL_ENDURANCE_DESC)
                        FitnessGoal.FLEXIBILITY -> languageViewModel.getString(StringKey.GOAL_FLEXIBILITY_DESC)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun FitnessLevelSection(
    selectedLevel: FitnessLevel?,
    onLevelSelected: (FitnessLevel) -> Unit,
    languageViewModel: LanguageViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            languageViewModel.getString(StringKey.FITNESS_LEVEL_QUESTION),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FitnessLevel.entries.forEach { level ->
                LevelCard(
                    level = level,
                    isSelected = level == selectedLevel,
                    onClick = { onLevelSelected(level) },
                    modifier = Modifier.weight(1f),
                    languageViewModel = languageViewModel
                )
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: FitnessLevel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    languageViewModel: LanguageViewModel
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (level) {
                    FitnessLevel.BEGINNER -> languageViewModel.getString(StringKey.LEVEL_BEGINNER)
                    FitnessLevel.INTERMEDIATE -> languageViewModel.getString(StringKey.LEVEL_INTERMEDIATE)
                    FitnessLevel.ADVANCED -> languageViewModel.getString(StringKey.LEVEL_ADVANCED)
                },
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WorkoutDaysSection(
    days: Int,
    onDaysChanged: (Int) -> Unit,
    languageViewModel: LanguageViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            languageViewModel.getString(StringKey.WORKOUT_DAYS_QUESTION),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            (2..6).forEach { day ->
                DayButton(
                    day = day,
                    isSelected = day == days,
                    onClick = { onDaysChanged(day) }
                )
            }
        }
    }
}

@Composable
private fun DayButton(
    day: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.size(48.dp)
    ) {
        Text(
            text = day.toString(),
            color = if (isSelected) 
                MaterialTheme.colorScheme.onPrimary
            else 
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SpecificRequirementsSection(
    requirements: String,
    onRequirementsChanged: (String) -> Unit,
    languageViewModel: LanguageViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            languageViewModel.getString(StringKey.SPECIFIC_REQUIREMENTS_QUESTION),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        OutlinedTextField(
            value = requirements,
            onValueChange = onRequirementsChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    languageViewModel.getString(StringKey.SPECIFIC_REQUIREMENTS_HINT),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            ),
            maxLines = 3
        )
    }
}
