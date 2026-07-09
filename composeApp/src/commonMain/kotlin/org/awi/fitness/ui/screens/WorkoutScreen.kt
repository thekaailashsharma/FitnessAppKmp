package org.awi.fitness.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.bg_gym_dark
import fitnessappkmp.composeapp.generated.resources.bg_run_dawn
import fitnessappkmp.composeapp.generated.resources.bg_weights
import fitnessappkmp.composeapp.generated.resources.bg_yoga_calm
import fitnessappkmp.composeapp.generated.resources.card_move
import fitnessappkmp.composeapp.generated.resources.ic3d_gym
import kotlinx.coroutines.launch
import org.awi.fitness.data.StringKey
import org.awi.fitness.model.*
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.Motion
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GlassChip
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.SectionHeader
import org.awi.fitness.ui.components.StatRing
import org.awi.fitness.ui.components.glassSource
import org.awi.fitness.utils.fitnessTips
import org.awi.fitness.utils.topFiveTips
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.LocalLanguageViewModel
import org.awi.fitness.viewmodel.LocalWorkoutViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

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
        var showWorkoutCompletionSheet by remember { mutableStateOf(false) }
        val languageViewModel = LocalLanguageViewModel.current
        val c = TajlyTheme.colors

        LaunchedEffect(Unit) {
            viewModel.loadIfNeeded()
        }

        if (showDeleteConfirmation != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = null },
                containerColor = c.s1,
                title = {
                    Text(
                        languageViewModel.getString(StringKey.DELETE_WORKOUT_PLAN),
                        color = c.textHi,
                    )
                },
                text = {
                    Column {
                        Text(
                            languageViewModel.getString(StringKey.DELETE_WORKOUT_CONFIRM),
                            color = c.textMid,
                        )
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
                        Text(
                            languageViewModel.getString(StringKey.CANCEL),
                            color = c.textMid,
                        )
                    }
                }
            )
        }

        ProvideGlass {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(c.bg)
        ) {
            // Cinematic photo hero — melts into the canvas via a bottom scrim.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .glassSource()
            ) {
                Image(
                    painter = painterResource(Res.drawable.bg_weights),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to c.bg.copy(alpha = if (c.isDark) 0.30f else 0.18f),
                                0.55f to c.bg.copy(alpha = if (c.isDark) 0.72f else 0.66f),
                                0.90f to c.bg.copy(alpha = 0.96f),
                                1.0f to c.bg,
                            )
                        )
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp)
            ) {
                // Header with title and actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = LocalLanguageViewModel.current.getString(StringKey.WKX_FOCUS_MODE),
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textLow,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = languageViewModel.getString(StringKey.WORKOUT_PLANS),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = c.textHi
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassIconButton(
                            icon = if (showListView) TablerIcons.LayoutGrid else TablerIcons.LayoutList,
                            contentDescription = if (showListView) languageViewModel.getString(StringKey.SHOW_GRID) else languageViewModel.getString(StringKey.SHOW_LIST),
                            onClick = { showListView = !showListView },
                        )
                        GlassIconButton(
                            icon = TablerIcons.Plus,
                            contentDescription = "Add New Plan",
                            gold = true,
                            onClick = {
                                viewModel.resetPlanSelection()
                                showGoalsSheet = true
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GoldPrimary)
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
                    else -> {
                        // The curated Pre-made plan always leads everyone's list; the
                        // user's own (owner-scoped) plans follow, newest first.
                        val sortedPlans = listOf(premadeWorkoutPlan) +
                            uiState.workoutPlans.sortedByDescending { it.plan.id }

                        if (showListView) {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 24.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(sortedPlans) { planWithExercises ->
                                    val total = planWithExercises.exercises.size
                                    val done = planWithExercises.exercises.count { it.isCompleted }
                                    WorkoutPlanListCard(
                                        plan = planWithExercises.plan,
                                        progress = if (total > 0) done.toFloat() / total else 0f,
                                        isSelected = sortedPlans.indexOf(planWithExercises) == selectedPlanIndex,
                                        isPremade = planWithExercises.plan.id == PREMADE_PLAN_ID,
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
                            // Single scroll for the whole page: the selector carousel is the
                            // first item so it scrolls away with the detail below it (nothing
                            // is pinned except the title bar).
                            val safeIndex = selectedPlanIndex.coerceIn(0, sortedPlans.lastIndex)
                            val selectedPlan = sortedPlans[safeIndex]
                            val isPremadeSelected = selectedPlan.plan.id == PREMADE_PLAN_ID
                            val availableDays = selectedPlan.exercises
                                .map { it.dayOfWeek }
                                .distinct()
                                .sorted()
                            var selectedDay by remember(selectedPlan.plan.id) {
                                mutableStateOf(availableDays.firstOrNull() ?: 1)
                            }
                            val dayNames = listOf("", LocalLanguageViewModel.current.getString(StringKey.MONDAY), LocalLanguageViewModel.current.getString(StringKey.TUESDAY), LocalLanguageViewModel.current.getString(StringKey.WEDNESDAY), LocalLanguageViewModel.current.getString(StringKey.THURSDAY), LocalLanguageViewModel.current.getString(StringKey.FRIDAY), LocalLanguageViewModel.current.getString(StringKey.SATURDAY), LocalLanguageViewModel.current.getString(StringKey.SUNDAY))
                            val exercisesForDay = selectedPlan.exercises
                                .filter { it.dayOfWeek == selectedDay }
                                .sortedWith(compareBy({ it.isCompleted }, { it.orderInDay }))

                            // Completion writes only apply to real (Firestore) plans; the
                            // code-defined pre-made plan is a preview and is never persisted.
                            val toggleExercise: (Exercise, Boolean) -> Unit = { exercise, completed ->
                                if (!isPremadeSelected) {
                                    coroutineScope.launch {
                                        viewModel.setExerciseCompleted(exercise, completed)
                                        kotlinx.coroutines.delay(300)
                                        val updated = viewModel.state.value.workoutPlans
                                            .firstOrNull { it.plan.id == selectedPlan.plan.id }
                                        if (updated != null && completed) {
                                            val dayDone = updated.exercises
                                                .filter { it.dayOfWeek == selectedDay }
                                                .all { it.isCompleted }
                                            if (dayDone) {
                                                showWorkoutCompletionSheet = true
                                                org.awi.fitness.data.UserSettings.getInstance()
                                                    .recordWorkoutCompleted()
                                                org.awi.fitness.viewmodel.ViewModelStore.challenges
                                                    .autoProgressWorkoutChallenges()
                                                org.awi.fitness.viewmodel.ViewModelStore.challenges
                                                    .autoProgressStreakChallenges()
                                            }
                                        }
                                    }
                                }
                            }

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(bottom = 24.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Selector carousel — scrolls with the rest of the page.
                                item {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(sortedPlans) { planWithExercises ->
                                            val total = planWithExercises.exercises.size
                                            val done = planWithExercises.exercises.count { it.isCompleted }
                                            WorkoutPlanCard(
                                                plan = planWithExercises.plan,
                                                progress = if (total > 0) done.toFloat() / total else 0f,
                                                isSelected = sortedPlans.indexOf(planWithExercises) == safeIndex,
                                                isPremade = planWithExercises.plan.id == PREMADE_PLAN_ID,
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
                                }

                                item {
                                    WorkoutDetailHero(
                                        plan = selectedPlan.plan,
                                        exerciseCount = selectedPlan.exercises.size,
                                        dayCount = availableDays.size,
                                        isPremade = isPremadeSelected,
                                        languageViewModel = languageViewModel,
                                    )
                                }

                                if (availableDays.size > 1) {
                                    item {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            contentPadding = PaddingValues(horizontal = 2.dp)
                                        ) {
                                            items(availableDays) { day ->
                                                val dayLabel = if (day in 1..7) dayNames[day] else "Day $day"
                                                val dayExercises = selectedPlan.exercises.filter { it.dayOfWeek == day }
                                                val allDone = dayExercises.isNotEmpty() && dayExercises.all { it.isCompleted }
                                                GlassChip(
                                                    text = if (allDone) "✓ $dayLabel" else dayLabel,
                                                    selected = selectedDay == day,
                                                    onClick = { selectedDay = day },
                                                )
                                            }
                                        }
                                    }
                                }

                                if (exercisesForDay.isNotEmpty()) {
                                    item {
                                        ActiveWorkoutHeader(
                                            plan = selectedPlan.plan,
                                            exercises = exercisesForDay,
                                            progress = exercisesForDay.count { it.isCompleted }
                                                .toFloat() / exercisesForDay.size,
                                        )
                                    }

                                    // Swipeable exercise pager — one big card per exercise.
                                    item {
                                        ExercisePager(
                                            exercises = exercisesForDay,
                                            dayKey = "${selectedPlan.plan.id}-$selectedDay",
                                            allowComplete = !isPremadeSelected,
                                            onCompleteClick = toggleExercise,
                                            languageViewModel = languageViewModel,
                                        )
                                    }

                                    item {
                                        WorkoutStatsStrip(
                                            exercises = exercisesForDay,
                                            plan = selectedPlan.plan,
                                        )
                                    }

                                    // Big primary CTA.
                                    item {
                                        val allDoneForDay = exercisesForDay.all { it.isCompleted }
                                        GoldButton(
                                            text = when {
                                                isPremadeSelected -> LocalLanguageViewModel.current.getString(StringKey.WKX_START_WITH_GOALS)
                                                allDoneForDay -> LocalLanguageViewModel.current.getString(StringKey.WKX_WORKOUT_COMPLETE)
                                                exercisesForDay.any { it.isCompleted } -> LocalLanguageViewModel.current.getString(StringKey.WKX_FINISH_WORKOUT)
                                                else -> LocalLanguageViewModel.current.getString(StringKey.START_WORKOUT)
                                            },
                                            enabled = isPremadeSelected || !allDoneForDay,
                                            onClick = {
                                                if (isPremadeSelected) {
                                                    viewModel.resetPlanSelection()
                                                    showGoalsSheet = true
                                                } else {
                                                    coroutineScope.launch {
                                                        val plan = viewModel.state.value.workoutPlans
                                                            .firstOrNull { it.plan.id == selectedPlan.plan.id } ?: return@launch
                                                        val toComplete = plan.exercises
                                                            .filter { it.dayOfWeek == selectedDay && !it.isCompleted }
                                                        // Robust batch write: parallel, single reload, failure surfaced.
                                                        val result = viewModel.completeExercises(toComplete)
                                                        val updated = viewModel.state.value.workoutPlans
                                                            .firstOrNull { it.plan.id == selectedPlan.plan.id }
                                                        val dayDone = updated?.exercises
                                                            ?.filter { it.dayOfWeek == selectedDay }
                                                            ?.all { it.isCompleted } == true
                                                        if (result.isSuccess && dayDone) {
                                                            showWorkoutCompletionSheet = true
                                                            org.awi.fitness.data.UserSettings.getInstance()
                                                                .recordWorkoutCompleted()
                                                            org.awi.fitness.viewmodel.ViewModelStore.challenges
                                                                .autoProgressWorkoutChallenges()
                                                            org.awi.fitness.viewmodel.ViewModelStore.challenges
                                                                .autoProgressStreakChallenges()
                                                        }
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }

                                    item {
                                        WorkoutTipsCard(
                                            tips = fitnessTips.topFiveTips(),
                                            difficulty = selectedPlan.plan.difficulty,
                                            languageViewModel = languageViewModel
                                        )
                                    }
                                } else {
                                    item {
                                        RestDayCard(
                                            activeChallenges = org.awi.fitness.viewmodel.ViewModelStore.challenges.state.value.activeChallenges
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // The primary CTA now lives inline in the detail flow (no pinned bottom bar).
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

        if (showWorkoutCompletionSheet) {
            org.awi.fitness.ui.components.WorkoutCompletionBottomSheet(
                onChatWithBuddy = {
                    showWorkoutCompletionSheet = false
                    coroutineScope.launch {
                        navigator.push(org.awi.fitness.ui.screens.avatar.AvatarScreen(org.awi.fitness.model.ConversationTrigger.WORKOUT_COMPLETED))
                    }
                },
                onDismiss = { showWorkoutCompletionSheet = false }
            )
        }
    }
}

/** Small glass-backed icon button matching the premium design language. */
@Composable
private fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    gold: Boolean = false,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .pressScale(interaction)
            .size(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (gold) Modifier.background(Tajly.GoldGradient)
                else Modifier.background(c.glassFill, RoundedCornerShape(14.dp))
                    .border(1.dp, c.hairStrong, RoundedCornerShape(14.dp))
            )
            .clickable(interactionSource = interaction, indication = null) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (gold) OnGold else c.textHi,
            modifier = Modifier.size(20.dp),
        )
    }
}

/**
 * Active-workout header: subtitle + current exercise name on the left, a gold rest/progress
 * StatRing on the right (mirrors the mockup's gold ring with day completion %).
 */
@Composable
private fun ActiveWorkoutHeader(
    plan: WorkoutPlan,
    exercises: List<Exercise>,
    progress: Float,
) {
    val c = TajlyTheme.colors
    val current = exercises.firstOrNull { !it.isCompleted } ?: exercises.lastOrNull()
    // One-time entrance + first-appearance ring sweep and count-up on the hero number.
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val animatedProgress by animateFloatAsState(
        targetValue = if (appeared) progress else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "dayProgress",
    )
    val entrance by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(Motion.DurEnter, easing = FastOutSlowInEasing),
        label = "headerEntrance",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = entrance
                translationY = (1f - entrance) * 16.dp.toPx()
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${plan.name} · focus mode",
                style = MaterialTheme.typography.labelMedium,
                color = c.textLow,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = current?.name ?: plan.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = c.textHi,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        StatRing(
            progress = animatedProgress,
            diameter = 64.dp,
            strokeWidth = 7.dp,
        ) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = GoldBright,
            )
        }
    }
}

/**
 * Glass "Working sets" card: header row (# / WEIGHT / REPS / tick) then one row per set.
 * Completed sets show a gold checkmark tick; pending sets show ghost (dim) target values.
 */
@Composable
private fun WorkingSetsCard(
    exercises: List<Exercise>,
    onCompleteClick: (Exercise, Boolean) -> Unit,
) {
    val c = TajlyTheme.colors
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = LocalLanguageViewModel.current.getString(StringKey.WKX_WORKING_SETS),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi,
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = TablerIcons.Activity,
                        contentDescription = null,
                        tint = GoldBright,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Header row
            SetRow(
                col1 = "#",
                weight = LocalLanguageViewModel.current.getString(StringKey.WEIGHT_LABEL).uppercase(),
                reps = LocalLanguageViewModel.current.getString(StringKey.REPS).uppercase(),
                headerStyle = true,
                trailing = { },
            )

            exercises.forEachIndexed { index, exercise ->
                val interaction = remember(exercise.id) { MutableInteractionSource() }
                SetRow(
                    col1 = (index + 1).toString(),
                    weight = "${exercise.sets} sets",
                    reps = "${exercise.reps} reps",
                    name = exercise.name,
                    ghost = !exercise.isCompleted,
                    isLast = index == exercises.lastIndex,
                    trailing = {
                        val checkShape = RoundedCornerShape(9.dp)
                        Box(
                            modifier = Modifier
                                .pressScale(interaction)
                                .size(30.dp)
                                .clip(checkShape)
                                .then(
                                    if (exercise.isCompleted) Modifier.background(Tajly.GoldGradient)
                                    else Modifier
                                        .background(c.glassFill, checkShape)
                                        .border(1.5.dp, c.hairStrong, checkShape)
                                )
                                .clickable(
                                    interactionSource = interaction,
                                    indication = null,
                                ) { onCompleteClick(exercise, !exercise.isCompleted) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (exercise.isCompleted) {
                                Icon(
                                    imageVector = TablerIcons.Check,
                                    contentDescription = "Completed",
                                    tint = OnGold,
                                    modifier = Modifier.size(17.dp),
                                )
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun SetRow(
    col1: String,
    weight: String,
    reps: String,
    name: String? = null,
    headerStyle: Boolean = false,
    ghost: Boolean = false,
    isLast: Boolean = false,
    trailing: @Composable () -> Unit,
) {
    val c = TajlyTheme.colors
    val valueColor = when {
        headerStyle -> c.textLow
        ghost -> c.textLow
        else -> c.textHi
    }
    val labelStyle = if (headerStyle)
        MaterialTheme.typography.labelSmall
    else
        MaterialTheme.typography.bodyMedium

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (headerStyle) 6.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = col1,
                style = labelStyle,
                fontWeight = if (headerStyle) FontWeight.SemiBold else FontWeight.Bold,
                color = if (headerStyle) c.textLow else c.textHi,
                modifier = Modifier.width(24.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                if (name != null) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (ghost) c.textMid else c.textHi,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = weight,
                    style = labelStyle,
                    color = valueColor,
                )
            }
            Text(
                text = reps,
                style = labelStyle,
                color = valueColor,
                modifier = Modifier.width(72.dp),
            )
            Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.CenterEnd) {
                trailing()
            }
        }
        if (!isLast) {
            HorizontalDivider(
                color = if (headerStyle) c.hairStrong else c.hair,
                thickness = 1.dp,
            )
        }
    }
}

/** Glass stats strip with big gold tabular numbers — honest counts only (no estimates). */
@Composable
private fun WorkoutStatsStrip(
    exercises: List<Exercise>,
    plan: WorkoutPlan,
) {
    val c = TajlyTheme.colors
    // Only counts that come directly from the model — no fabricated time/strength.
    val totalSets = exercises.sumOf { it.sets }
    val totalReps = exercises.sumOf { it.sets * it.reps }
    val completedSets = exercises.filter { it.isCompleted }.sumOf { it.sets }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatNumber(value = "${exercises.size}", label = LocalLanguageViewModel.current.getString(StringKey.EXERCISES).lowercase(), gold = false)
            StatNumber(value = totalReps.toString(), label = LocalLanguageViewModel.current.getString(StringKey.WKX_TOTAL_REPS), gold = true)
            StatNumber(value = "$completedSets/$totalSets", label = LocalLanguageViewModel.current.getString(StringKey.WKX_SETS_DONE), gold = true)
        }
    }
}

@Composable
private fun StatNumber(value: String, label: String, gold: Boolean) {
    val c = TajlyTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (gold) GoldBright else c.textHi,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = c.textLow,
        )
    }
}

@Composable
private fun WorkoutPlanCard(
    plan: WorkoutPlan,
    progress: Float,
    isSelected: Boolean,
    isPremade: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    languageViewModel: LanguageViewModel
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(18.dp)
    // Compact selector card (~half the old height): a slim photo strip + a single
    // meta row. The full detail (hero + exercise pager) lives below in the scroll.
    Box(
        modifier = Modifier
            .pressScale(interaction)
            .width(168.dp)
            .clip(shape)
            .then(
                if (isSelected) Modifier.background(c.glassFill, shape)
                    .border(1.5.dp, Tajly.GoldGradient, shape)
                else Modifier.background(c.glassFill, shape)
                    .border(1.dp, c.glassRim, shape)
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Image(
                    painter = painterResource(workoutImage(plan.category)),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to c.bg.copy(alpha = 0.05f),
                                1f to c.bg.copy(alpha = 0.92f),
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .size(26.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(GoldPrimary.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = categoryIcon(plan.category),
                        contentDescription = null,
                        tint = OnGold,
                        modifier = Modifier.size(15.dp),
                    )
                }
                if (!isPremade) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(22.dp)
                    ) {
                        Icon(
                            imageVector = TablerIcons.Trash,
                            contentDescription = "Delete Plan",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DifficultyBadge(plan.difficulty)

                    StatRing(progress = progress, diameter = 32.dp, strokeWidth = 4.dp) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldBright,
                        )
                    }
                }
            }
        }

        if (isPremade) {
            PremadeRibbon(modifier = Modifier.align(Alignment.TopStart))
        }
    }
}

/**
 * Sideways gold ribbon in the top-left corner of a card, reading "PRE-MADE".
 * Rotated -45° and clipped by the parent card's rounded shape.
 */
@Composable
private fun PremadeRibbon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .offset(x = (-34).dp, y = 16.dp)
            .rotate(-45f)
            .background(Tajly.GoldGradient)
            .padding(horizontal = 36.dp, vertical = 3.dp),
    ) {
        Text(
            text = LocalLanguageViewModel.current.getString(StringKey.WKX_PREMADE),
            color = OnGold,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

/** Small gold difficulty pill reused by plan cards. */
@Composable
private fun DifficultyBadge(difficulty: WorkoutDifficulty) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(GoldPrimary.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = GoldBright,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EmptyWorkoutState(
    onSetupClick: () -> Unit,
    languageViewModel: LanguageViewModel
) {
    val c = TajlyTheme.colors
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Tajly.sectionGlow(GoldPrimary)),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic3d_gym),
                contentDescription = null,
                modifier = Modifier.size(84.dp),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = languageViewModel.getString(StringKey.NO_WORKOUT_PLAN),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = c.textHi
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = languageViewModel.getString(StringKey.NO_WORKOUT_PLAN_DESC),
            style = MaterialTheme.typography.bodyMedium,
            color = c.textMid,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        GoldButton(
            text = languageViewModel.getString(StringKey.SET_UP_MY_GOALS),
            onClick = onSetupClick,
        )
    }
}

@Composable
private fun ExerciseDetailsCard(
    exercises: List<Exercise>,
    languageViewModel: LanguageViewModel
) {
    val c = TajlyTheme.colors
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
                title = languageViewModel.getString(StringKey.EXERCISE_DETAILS),
                action = {
                    Icon(
                        imageVector = TablerIcons.ListCheck,
                        contentDescription = null,
                        tint = GoldBright,
                        modifier = Modifier.size(22.dp)
                    )
                },
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (exercise.isCompleted) Modifier.background(Tajly.GoldGradient)
                                        else Modifier
                                            .background(c.glassFill, CircleShape)
                                            .border(1.dp, c.hairStrong, CircleShape)
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (exercise.isCompleted) {
                                    Icon(
                                        imageVector = TablerIcons.Check,
                                        contentDescription = null,
                                        tint = OnGold,
                                        modifier = Modifier.size(16.dp),
                                    )
                                } else {
                                    Text(
                                        text = exercise.orderInDay.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = c.textHi
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = exercise.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = c.textHi,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (exercise.isCompleted) {
                                    Text(
                                        text = languageViewModel.getString(StringKey.COMPLETED),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldBright
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = if (expanded) TablerIcons.ChevronUp else TablerIcons.ChevronDown,
                                contentDescription = null,
                                tint = c.textMid
                            )
                        }
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Text(
                                text = exercise.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textMid
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
                    HorizontalDivider(
                        color = c.hair,
                        modifier = Modifier.padding(vertical = 4.dp)
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
    val c = TajlyTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GoldBright,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = c.textHi
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = c.textLow
        )
    }
}

@Composable
private fun WorkoutTipsCard(
    tips: List<String>,
    difficulty: WorkoutDifficulty,
    languageViewModel: LanguageViewModel
) {
    val c = TajlyTheme.colors
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionHeader(
                title = LocalLanguageViewModel.current.getString(StringKey.WKX_TIPS),
                action = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldBright,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(10.dp))

            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = TablerIcons.Star,
                        contentDescription = null,
                        tint = GoldBright,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textMid
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    error: String?,
    onRetry: () -> Unit,
    languageViewModel: LanguageViewModel
) {
    val c = TajlyTheme.colors
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = error ?: languageViewModel.getString(StringKey.AN_ERROR_OCCURRED),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            GoldButton(
                text = languageViewModel.getString(StringKey.RETRY),
                onClick = onRetry,
            )
        }
    }
}

@Composable
private fun WorkoutPlanListCard(
    plan: WorkoutPlan,
    progress: Float,
    isSelected: Boolean,
    isPremade: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    languageViewModel: LanguageViewModel
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = Modifier
            .pressScale(interaction)
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (isSelected) Modifier.background(c.glassFill, shape)
                    .border(1.5.dp, Tajly.GoldGradient, shape)
                else Modifier.background(c.glassFill, shape)
                    .border(1.dp, c.glassRim, shape)
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
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
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.BottomStart,
                ) {
                    Image(
                        painter = painterResource(workoutImage(plan.category)),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0f to c.bg.copy(alpha = 0.0f),
                                    1f to c.bg.copy(alpha = 0.55f),
                                )
                            )
                    )
                    Icon(
                        imageVector = categoryIcon(plan.category),
                        contentDescription = null,
                        tint = OnGold,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(18.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi
                    )

                    Text(
                        text = plan.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMid,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = TablerIcons.Clock,
                                contentDescription = null,
                                tint = c.textLow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${plan.duration} min/week",
                                style = MaterialTheme.typography.labelSmall,
                                color = c.textLow
                            )
                        }

                        DifficultyBadge(plan.difficulty)
                    }
                }
            }

            StatRing(progress = progress, diameter = 44.dp, strokeWidth = 5.dp) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = GoldBright,
                )
            }

            if (!isPremade) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = TablerIcons.Trash,
                        contentDescription = "Delete Plan",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = if (isSelected) 1f else 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (isPremade) {
            PremadeRibbon(modifier = Modifier.align(Alignment.TopStart))
        }
    }
}

@Composable
private fun RestDayCard(activeChallenges: List<org.awi.fitness.model.Challenge>) {
    val c = TajlyTheme.colors
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Tajly.sectionGlow(GoldPrimary)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = TablerIcons.Leaf,
                    contentDescription = null,
                    tint = GoldBright,
                    modifier = Modifier.size(40.dp),
                )
            }
            Text(
                text = LocalLanguageViewModel.current.getString(StringKey.WKX_REST_DAY),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = c.textHi
            )
            Text(
                text = LocalLanguageViewModel.current.getString(StringKey.WKX_REST_DAY_MESSAGE),
                style = MaterialTheme.typography.bodyLarge,
                color = c.textMid,
                textAlign = TextAlign.Center
            )

            if (activeChallenges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(c.s2)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        LocalLanguageViewModel.current.getString(StringKey.ACTIVE_CHALLENGES),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi,
                    )
                    activeChallenges.take(2).forEach { challenge ->
                        val progress = if (challenge.target > 0)
                            challenge.progress.toFloat() / challenge.target.toFloat()
                        else 0f
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                challenge.iconName.takeIf { it.isNotBlank() } ?: "🏆",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    challenge.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = c.textHi,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                LinearProgressIndicator(
                                    progress = { progress.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = GoldPrimary,
                                    trackColor = c.s4
                                )
                            }
                            Text(
                                "${challenge.progress}/${challenge.target}",
                                style = MaterialTheme.typography.labelSmall,
                                color = c.textLow
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun categoryIcon(category: WorkoutCategory): ImageVector = when (category) {
    WorkoutCategory.STRENGTH -> TablerIcons.Bolt
    WorkoutCategory.CARDIO -> TablerIcons.Run
    WorkoutCategory.HIIT -> TablerIcons.Flame
    WorkoutCategory.FLEXIBILITY -> TablerIcons.Activity
    WorkoutCategory.YOGA -> TablerIcons.Leaf
}

/** Firestore-independent id for the always-present curated plan. */
private const val PREMADE_PLAN_ID = "premade_foundations"

/**
 * Curated, code-defined bodyweight starter plan shown at the front of every user's
 * list. Real exercises across three days — no equipment, no fabricated data. It is
 * never written to Firestore, so it always appears regardless of the owner filter.
 */
private val premadeWorkoutPlan: WorkoutPlanWithExercises = run {
    val plan = WorkoutPlan(
        id = PREMADE_PLAN_ID,
        name = "Foundations",
        description = "A no-equipment full-body starter — build strength, control and consistency with bodyweight basics.",
        difficulty = WorkoutDifficulty.BEGINNER,
        duration = 90, // weekly training time in minutes (3 sessions ~30 min) — matches the "min/week" unit
        category = WorkoutCategory.STRENGTH,
    )
    fun ex(day: Int, order: Int, name: String, desc: String, sets: Int, reps: Int, rest: Int) =
        Exercise(
            id = "${PREMADE_PLAN_ID}_d${day}_$order",
            planId = PREMADE_PLAN_ID,
            name = name,
            description = desc,
            sets = sets,
            reps = reps,
            restTime = rest,
            dayOfWeek = day,
            orderInDay = order,
        )
    val exercises = listOf(
        // Day 1 — Full body
        ex(1, 1, "Push-Ups", "Hands under shoulders, body in a straight line; lower until your chest is just above the floor, then press back up.", 3, 12, 60),
        ex(1, 2, "Bodyweight Squats", "Feet shoulder-width, chest up; sit your hips back and down until your thighs are parallel, then drive through your heels.", 3, 15, 60),
        ex(1, 3, "Glute Bridges", "Lie on your back with knees bent; squeeze your glutes to lift your hips into a straight line, then lower slowly.", 3, 15, 45),
        ex(1, 4, "Plank", "Forearms and toes on the floor, core braced; hold a straight line from head to heels (reps = seconds held).", 3, 40, 45),
        // Day 2 — Core & lower body
        ex(2, 1, "Reverse Lunges", "Step one foot back and lower until both knees reach about 90°, then push through the front heel to stand. Reps per leg.", 3, 12, 60),
        ex(2, 2, "Superman Hold", "Lie face down; lift your arms, chest and legs off the floor, squeeze your back, then lower under control.", 3, 12, 45),
        ex(2, 3, "Mountain Climbers", "From a plank, drive your knees toward your chest one at a time at a steady pace (reps = total knee drives).", 3, 20, 45),
        ex(2, 4, "Bicycle Crunches", "On your back, bring an opposite elbow toward the opposite knee while extending the other leg; alternate with control.", 3, 20, 45),
        // Day 3 — Conditioning
        ex(3, 1, "Jumping Jacks", "Jump your feet out while raising your arms overhead, then back in; keep a light, steady rhythm.", 3, 30, 30),
        ex(3, 2, "High Knees", "Run in place, driving your knees to hip height while staying light on the balls of your feet.", 3, 25, 30),
        ex(3, 3, "Burpees", "Squat, kick back to a plank, do a push-up, jump your feet in and explode up. Move at a controlled pace.", 3, 10, 60),
        ex(3, 4, "Dead Bug", "On your back with arms up and knees at 90°, lower an opposite arm and leg, then return. Keep your lower back flat.", 3, 12, 45),
    )
    WorkoutPlanWithExercises(plan, exercises)
}

/** Maps a workout category to an existing drawable — food-delivery-style imagery. */
private fun workoutImage(category: WorkoutCategory): DrawableResource = when (category) {
    WorkoutCategory.STRENGTH -> Res.drawable.bg_weights
    WorkoutCategory.CARDIO -> Res.drawable.bg_run_dawn
    WorkoutCategory.HIIT -> Res.drawable.bg_gym_dark
    WorkoutCategory.FLEXIBILITY -> Res.drawable.card_move
    WorkoutCategory.YOGA -> Res.drawable.bg_yoga_calm
}

/** Premium hero for the selected plan: full-bleed photo, scrim, title and quick stats. */
@Composable
private fun WorkoutDetailHero(
    plan: WorkoutPlan,
    exerciseCount: Int,
    dayCount: Int,
    isPremade: Boolean,
    languageViewModel: LanguageViewModel,
) {
    val c = TajlyTheme.colors
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp)
            .clip(shape)
            .border(1.dp, c.glassRim, shape),
    ) {
        Image(
            painter = painterResource(workoutImage(plan.category)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to c.bg.copy(alpha = 0.10f),
                        0.55f to c.bg.copy(alpha = 0.50f),
                        1f to c.bg.copy(alpha = 0.94f),
                    )
                )
        )
        if (isPremade) {
            PremadeRibbon(modifier = Modifier.align(Alignment.TopStart))
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DifficultyBadge(plan.difficulty)
            Text(
                text = plan.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = c.textHi,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HeroStat(TablerIcons.Clock, "${plan.duration} min/week")
                HeroStat(TablerIcons.ListCheck, "$exerciseCount exercises")
                HeroStat(TablerIcons.Activity, if (dayCount <= 1) "1 day" else "$dayCount days")
            }
        }
    }
}

@Composable
private fun HeroStat(icon: ImageVector, text: String) {
    val c = TajlyTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = GoldBright, modifier = Modifier.size(15.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = c.textMid,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Swipeable exercise experience: a horizontal pager of big per-exercise cards with
 * description, sets/reps/rest and a mark-complete toggle. Page dots track position.
 */
@Composable
private fun ExercisePager(
    exercises: List<Exercise>,
    dayKey: String,
    allowComplete: Boolean,
    onCompleteClick: (Exercise, Boolean) -> Unit,
    languageViewModel: LanguageViewModel,
) {
    val c = TajlyTheme.colors
    key(dayKey) {
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { exercises.size })
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 12.dp,
                contentPadding = PaddingValues(end = 36.dp),
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                val exercise = exercises[page]
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Exercise ${page + 1} of ${exercises.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = c.textLow,
                            )
                            if (exercise.isCompleted) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        imageVector = TablerIcons.Check,
                                        contentDescription = null,
                                        tint = GoldBright,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = languageViewModel.getString(StringKey.COMPLETED),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = GoldBright,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = exercise.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = c.textHi,
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = exercise.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textMid,
                            minLines = 3,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            MetricItem(
                                icon = TablerIcons.Repeat,
                                label = languageViewModel.getString(StringKey.SETS),
                                value = exercise.sets.toString(),
                            )
                            MetricItem(
                                icon = TablerIcons.RotateClockwise,
                                label = languageViewModel.getString(StringKey.REPS),
                                value = exercise.reps.toString(),
                            )
                            MetricItem(
                                icon = TablerIcons.Clock,
                                label = languageViewModel.getString(StringKey.REST),
                                value = "${exercise.restTime}s",
                            )
                        }

                        if (allowComplete) {
                            Spacer(modifier = Modifier.height(18.dp))
                            val toggleShape = RoundedCornerShape(14.dp)
                            val toggleInteraction = remember(exercise.id) { MutableInteractionSource() }
                            Box(
                                modifier = Modifier
                                    .pressScale(toggleInteraction)
                                    .fillMaxWidth()
                                    .clip(toggleShape)
                                    .then(
                                        if (exercise.isCompleted) Modifier.background(Tajly.GoldGradient)
                                        else Modifier
                                            .background(c.glassFill, toggleShape)
                                            .border(1.5.dp, c.hairStrong, toggleShape)
                                    )
                                    .clickable(
                                        interactionSource = toggleInteraction,
                                        indication = null,
                                    ) { onCompleteClick(exercise, !exercise.isCompleted) }
                                    .padding(vertical = 13.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = if (exercise.isCompleted) "Completed" else "Mark complete",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (exercise.isCompleted) OnGold else c.textHi,
                                )
                            }
                        }
                    }
                }
            }

            // Page indicator dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                exercises.indices.forEach { i ->
                    val selected = pagerState.currentPage == i
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (selected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (selected) GoldBright else c.hairStrong)
                    )
                }
            }
        }
    }
}
