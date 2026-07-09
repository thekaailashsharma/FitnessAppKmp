package org.awi.fitness.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.BallVolleyball
import compose.icons.tablericons.Check
import compose.icons.tablericons.Run
import compose.icons.tablericons.Scale
import compose.icons.tablericons.Walk
import kotlinx.coroutines.launch
import org.awi.fitness.data.StringKey
import org.awi.fitness.model.FitnessGoal
import org.awi.fitness.model.FitnessLevel
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.Motion
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.WorkoutViewModel

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
    val c = TajlyTheme.colors

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

    // One-time fade + rise for the whole form.
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) { reveal.animateTo(1f, tween(Motion.DurEnter)) }

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) onDismiss() },
        modifier = Modifier.fillMaxSize(),
        sheetState = bottomSheetState,
        dragHandle = null,
        containerColor = Color.Transparent,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        AuroraBackground(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .graphicsLayer {
                        alpha = reveal.value
                        translationY = (1f - reveal.value) * 24.dp.toPx()
                    },
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Spacer(Modifier.height(8.dp))
                // Grabber.
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(44.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(c.hairStrong),
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = languageViewModel.getString(StringKey.UFG_BUILD_YOUR_PLAN).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldBright,
                    )
                    Text(
                        text = languageViewModel.getString(StringKey.FITNESS_GOALS_TITLE),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi,
                    )
                    Text(
                        text = languageViewModel.getString(StringKey.FITNESS_GOALS_SUBTITLE),
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textMid,
                    )
                }

                GoalSelectionSection(
                    selectedGoal = selectedGoal,
                    onGoalSelected = { selectedGoal = it },
                    languageViewModel = languageViewModel,
                )

                AnimatedVisibility(
                    visible = selectedGoal != null,
                    enter = fadeIn() + slideInVertically(),
                ) {
                    FitnessLevelSection(
                        selectedLevel = fitnessLevel,
                        onLevelSelected = { fitnessLevel = it },
                        languageViewModel = languageViewModel,
                    )
                }

                AnimatedVisibility(
                    visible = fitnessLevel != null,
                    enter = fadeIn() + slideInVertically(),
                ) {
                    WorkoutDaysSection(
                        days = preferredWorkoutDays,
                        onDaysChanged = { preferredWorkoutDays = it },
                        languageViewModel = languageViewModel,
                    )
                }

                val canSubmit = selectedGoal != null && fitnessLevel != null && !isLoading
                GoldButton(
                    text = if (isLoading) {
                        languageViewModel.getString(StringKey.GENERATING_WORKOUT_PLAN)
                    } else {
                        languageViewModel.getString(StringKey.START_FITNESS_JOURNEY)
                    },
                    onClick = {
                        if (selectedGoal != null && fitnessLevel != null) {
                            error = null
                            coroutineScope.launch {
                                viewModel.saveUserGoals(
                                    selectedGoal!!,
                                    fitnessLevel!!,
                                    preferredWorkoutDays,
                                    specificRequirements,
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp),
                    enabled = canSubmit,
                    loading = isLoading,
                )
            }

            // Error message — floats above the CTA.
            error?.let {
                GlassCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(20.dp),
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textHi,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// GOAL SELECTION
// ---------------------------------------------------------------------------

@Composable
private fun GoalSelectionSection(
    selectedGoal: FitnessGoal?,
    onGoalSelected: (FitnessGoal) -> Unit,
    languageViewModel: LanguageViewModel,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel(languageViewModel.getString(StringKey.FITNESS_GOAL_QUESTION))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FitnessGoal.values().forEach { goal ->
                GoalCard(
                    goal = goal,
                    isSelected = goal == selectedGoal,
                    onClick = { onGoalSelected(goal) },
                    languageViewModel = languageViewModel,
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
    languageViewModel: LanguageViewModel,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val icon: ImageVector = when (goal) {
        FitnessGoal.WEIGHT_LOSS -> TablerIcons.Scale
        FitnessGoal.MUSCLE_GAIN -> TablerIcons.Walk
        FitnessGoal.ENDURANCE -> TablerIcons.Run
        FitnessGoal.FLEXIBILITY -> TablerIcons.BallVolleyball
    }
    val title = when (goal) {
        FitnessGoal.WEIGHT_LOSS -> languageViewModel.getString(StringKey.GOAL_WEIGHT_LOSS)
        FitnessGoal.MUSCLE_GAIN -> languageViewModel.getString(StringKey.GOAL_MUSCLE_GAIN)
        FitnessGoal.ENDURANCE -> languageViewModel.getString(StringKey.GOAL_ENDURANCE)
        FitnessGoal.FLEXIBILITY -> languageViewModel.getString(StringKey.GOAL_FLEXIBILITY)
    }
    val desc = when (goal) {
        FitnessGoal.WEIGHT_LOSS -> languageViewModel.getString(StringKey.GOAL_WEIGHT_LOSS_DESC)
        FitnessGoal.MUSCLE_GAIN -> languageViewModel.getString(StringKey.GOAL_MUSCLE_GAIN_DESC)
        FitnessGoal.ENDURANCE -> languageViewModel.getString(StringKey.GOAL_ENDURANCE_DESC)
        FitnessGoal.FLEXIBILITY -> languageViewModel.getString(StringKey.GOAL_FLEXIBILITY_DESC)
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .then(
                if (isSelected) Modifier.border(1.5.dp, GoldPrimary, RoundedCornerShape(22.dp))
                else Modifier
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        goldTint = isSelected,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (isSelected) Modifier.background(Tajly.GoldGradient)
                        else Modifier.background(GoldPrimary.copy(alpha = 0.14f))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) OnGold else GoldBright,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi,
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Tajly.GoldGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = TablerIcons.Check,
                        contentDescription = null,
                        tint = OnGold,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// FITNESS LEVEL
// ---------------------------------------------------------------------------

@Composable
private fun FitnessLevelSection(
    selectedLevel: FitnessLevel?,
    onLevelSelected: (FitnessLevel) -> Unit,
    languageViewModel: LanguageViewModel,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel(languageViewModel.getString(StringKey.FITNESS_LEVEL_QUESTION))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FitnessLevel.entries.forEach { level ->
                LevelCard(
                    level = level,
                    isSelected = level == selectedLevel,
                    onClick = { onLevelSelected(level) },
                    modifier = Modifier.weight(1f),
                    languageViewModel = languageViewModel,
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
    languageViewModel: LanguageViewModel,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val label = when (level) {
        FitnessLevel.BEGINNER -> languageViewModel.getString(StringKey.LEVEL_BEGINNER)
        FitnessLevel.INTERMEDIATE -> languageViewModel.getString(StringKey.LEVEL_INTERMEDIATE)
        FitnessLevel.ADVANCED -> languageViewModel.getString(StringKey.LEVEL_ADVANCED)
    }
    GlassCard(
        modifier = modifier
            .pressScale(interaction)
            .then(
                if (isSelected) Modifier.border(1.5.dp, GoldPrimary, RoundedCornerShape(22.dp))
                else Modifier
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        goldTint = isSelected,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) GoldBright else c.textMid,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// WEEKLY DAYS STEPPER
// ---------------------------------------------------------------------------

@Composable
private fun WorkoutDaysSection(
    days: Int,
    onDaysChanged: (Int) -> Unit,
    languageViewModel: LanguageViewModel,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel(languageViewModel.getString(StringKey.WORKOUT_DAYS_QUESTION))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            (2..6).forEach { day ->
                DayButton(
                    day = day,
                    isSelected = day == days,
                    onClick = { onDaysChanged(day) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DayButton(
    day: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val base = modifier
        .pressScale(interaction)
        .height(56.dp)
        .clip(RoundedCornerShape(16.dp))
    val styled = if (isSelected) {
        base.background(Tajly.GoldGradient)
    } else {
        base
            .background(c.glassFill, RoundedCornerShape(16.dp))
            .border(1.dp, c.hairStrong, RoundedCornerShape(16.dp))
    }
    Box(
        modifier = styled.clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) OnGold else c.textHi,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = TajlyTheme.colors.textHi,
    )
}
