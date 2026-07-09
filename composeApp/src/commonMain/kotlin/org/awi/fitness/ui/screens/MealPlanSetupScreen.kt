package org.awi.fitness.ui.screens
import org.awi.fitness.viewmodel.LocalLanguageViewModel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
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
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.ic3d_leaf
import kotlinx.coroutines.launch
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.DietaryType
import org.awi.fitness.model.MealGoal
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GlassChip
import org.awi.fitness.ui.components.GlassTier
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.LottieAnim
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.glassSource
import org.jetbrains.compose.resources.painterResource
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
    val c = TajlyTheme.colors

    var selectedGoal by remember { mutableStateOf<MealGoal?>(null) }
    var selectedDiet by remember { mutableStateOf<DietaryType?>(null) }
    var restrictions by remember { mutableStateOf("") }
    var mealsPerDay by remember { mutableStateOf(3) }
    var useCalculated by remember { mutableStateOf(userSettings.calculatedCalories > 0) }
    var customCalories by remember { mutableStateOf("2000") }

    val calculatedCal = userSettings.calculatedCalories
    val targetCal = if (useCalculated && calculatedCal > 0) calculatedCal else (customCalories.toIntOrNull() ?: 2000)

    // Macro split derived from the selected goal — protein/carb/fat share of total calories.
    val (proteinRatio, carbRatio, fatRatio) = when (selectedGoal) {
        MealGoal.WEIGHT_LOSS -> Triple(0.40, 0.30, 0.30)   // higher protein to preserve muscle in a deficit
        MealGoal.MUSCLE_GAIN -> Triple(0.35, 0.45, 0.20)   // more carbs to fuel training/growth
        MealGoal.MAINTENANCE -> Triple(0.30, 0.40, 0.30)
        MealGoal.GENERAL_HEALTH -> Triple(0.30, 0.40, 0.30)
        null -> Triple(0.30, 0.40, 0.30)
    }
    val targetProtein = (targetCal * proteinRatio / 4).toInt()
    val targetCarbs = (targetCal * carbRatio / 4).toInt()
    val targetFat = (targetCal * fatRatio / 9).toInt()

    val canGenerate = selectedGoal != null && selectedDiet != null

    // Wizard progress — three real choice steps (goal · diet · details).
    val stepsDone = (if (selectedGoal != null) 1 else 0) +
        (if (selectedDiet != null) 1 else 0) +
        (if (selectedDiet != null) 1 else 0)

    ModalBottomSheet(
        onDismissRequest = { if (!state.isGenerating) onDismiss() },
        modifier = Modifier.fillMaxSize(),
        sheetState = sheetState,
        dragHandle = null,
        containerColor = c.bg,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        ProvideGlass {
            Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
                AuroraBackground(modifier = Modifier.fillMaxSize().glassSource()) {}

                if (state.isGenerating) {
                    GeneratingPlanView(languageViewModel, state.generationStep)
                } else {
                    // Scrollable form; primary CTA pinned to the bottom so it's always on-screen.
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp)
                                .padding(top = 24.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(28.dp)
                        ) {
                            // ── FOCAL: title + 3D icon ──
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Tajly.Green.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(Res.drawable.ic3d_leaf),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = languageViewModel.getString(StringKey.CREATE_YOUR_MEAL_PLAN),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = c.textHi
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = languageViewModel.getString(StringKey.CREATE_PLAN_SUBTITLE),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = c.textMid
                                    )
                                }
                            }

                            // Wizard step tracker — how far the choices have progressed.
                            WizardProgress(stepsDone = stepsDone, totalSteps = 3)

                            // ── STEP 1: Goal ──
                            SetupSection(
                                title = languageViewModel.getString(StringKey.WHATS_YOUR_GOAL),
                                active = selectedGoal == null
                            ) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MealGoal.entries.forEach { goal ->
                                        GlassChip(
                                            text = goal.displayName,
                                            selected = selectedGoal == goal,
                                            onClick = { selectedGoal = goal }
                                        )
                                    }
                                }
                            }

                            // ── STEP 2: Diet ──
                            AnimatedVisibility(
                                visible = selectedGoal != null,
                                enter = fadeIn() + slideInVertically()
                            ) {
                                SetupSection(
                                    title = languageViewModel.getString(StringKey.DIETARY_PREFERENCE),
                                    active = selectedGoal != null && selectedDiet == null
                                ) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        DietaryType.entries.forEach { diet ->
                                            GlassChip(
                                                text = diet.displayName,
                                                selected = selectedDiet == diet,
                                                onClick = { selectedDiet = diet }
                                            )
                                        }
                                    }
                                }
                            }

                            // ── STEP 3: details (restrictions · meals-per-day · calories) grouped ──
                            AnimatedVisibility(
                                visible = selectedDiet != null,
                                enter = fadeIn() + slideInVertically()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
                                    // Restrictions — demoted, optional
                                    SetupSection(
                                        title = languageViewModel.getString(StringKey.ANY_ALLERGIES),
                                        active = false
                                    ) {
                                        OutlinedTextField(
                                            value = restrictions,
                                            onValueChange = { restrictions = it },
                                            placeholder = {
                                                Text(
                                                    languageViewModel.getString(StringKey.ALLERGIES_HINT),
                                                    color = c.textLow
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = GoldPrimary,
                                                unfocusedBorderColor = c.hairStrong,
                                                focusedTextColor = c.textHi,
                                                unfocusedTextColor = c.textHi
                                            )
                                        )
                                    }

                                    // Meals per day — glass chip choices
                                    SetupSection(
                                        title = languageViewModel.getString(StringKey.MEALS_PER_DAY),
                                        active = false
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf(3, 4).forEach { count ->
                                                GlassChip(
                                                    text = "$count ${languageViewModel.getString(StringKey.MEALS_PER_DAY).lowercase()}",
                                                    selected = mealsPerDay == count,
                                                    onClick = { mealsPerDay = count }
                                                )
                                            }
                                        }
                                    }

                                    // Calories — radio options as glass rows
                                    SetupSection(
                                        title = languageViewModel.getString(StringKey.DAILY_CALORIES),
                                        active = false
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (calculatedCal > 0) {
                                                CalorieOption(
                                                    text = "${languageViewModel.getString(StringKey.USE_CALCULATED_CALORIES)}: $calculatedCal kcal",
                                                    selected = useCalculated,
                                                    onClick = { useCalculated = true }
                                                )
                                            }
                                            CalorieOption(
                                                text = languageViewModel.getString(StringKey.SET_CUSTOM_TARGET),
                                                selected = !useCalculated || calculatedCal == 0,
                                                onClick = { useCalculated = false }
                                            )
                                            if (!useCalculated || calculatedCal == 0) {
                                                OutlinedTextField(
                                                    value = customCalories,
                                                    onValueChange = { customCalories = it.filter { ch -> ch.isDigit() } },
                                                    label = { Text("kcal", color = c.textMid) },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(14.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = GoldPrimary,
                                                        unfocusedBorderColor = c.hairStrong,
                                                        focusedTextColor = c.textHi,
                                                        unfocusedTextColor = c.textHi
                                                    )
                                                )
                                            }
                                        }
                                    }
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
                        }

                        // ── Pinned primary action ──
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(c.bg)
                                .padding(horizontal = 20.dp)
                                .padding(top = 8.dp, bottom = 20.dp)
                        ) {
                            GoldButton(
                                text = languageViewModel.getString(StringKey.GENERATE_MY_PLAN),
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
                                enabled = canGenerate,
                                loading = state.isGenerating,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Slim gold segmented tracker for the three choice steps. */
@Composable
private fun WizardProgress(stepsDone: Int, totalSteps: Int) {
    val lang = LocalLanguageViewModel.current
    val c = TajlyTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "${lang.getString(StringKey.MEALX_STEP)} ${stepsDone.coerceIn(1, totalSteps)} ${lang.getString(StringKey.MEALX_OF)} $totalSteps".uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = GoldBright
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(totalSteps) { i ->
                val filled = i < stepsDone
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .then(
                            if (filled) Modifier.background(Tajly.GoldGradient)
                            else Modifier.background(c.hairStrong)
                        )
                )
            }
        }
    }
}

/** Section wrapper: gold eyebrow title when it's the active step, muted otherwise. */
@Composable
private fun SetupSection(
    title: String,
    active: Boolean,
    content: @Composable () -> Unit
) {
    val c = TajlyTheme.colors
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (active) GoldBright else c.textHi
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun CalorieOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val c = TajlyTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(c.glassFill, shape)
            .then(
                if (selected) Modifier.border(1.5.dp, GoldPrimary, shape)
                else Modifier.border(1.dp, c.hairStrong, shape)
            )
            .androidx_clickable(onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (selected) TablerIcons.Check else TablerIcons.Circle,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (selected) GoldPrimary else c.textLow
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = c.textHi
        )
    }
}

@Composable
private fun GeneratingPlanView(
    languageViewModel: LanguageViewModel,
    generationStep: GenerationStep
) {
    val c = TajlyTheme.colors
    val steps = listOf(
        Triple(StringKey.STEP_ANALYZING_PREFERENCES, TablerIcons.Target, GenerationStep.ANALYZING_PREFERENCES),
        Triple(StringKey.STEP_GENERATING_MEALS, TablerIcons.Fold, GenerationStep.CALLING_AI),
        Triple(StringKey.STEP_BALANCING_NUTRITION, TablerIcons.Flame, GenerationStep.PARSING_RESPONSE),
        Triple(StringKey.STEP_SAVING_PLAN, TablerIcons.ShoppingCart, GenerationStep.SAVING_PLAN),
    )

    val currentIndex = steps.indexOfFirst { it.third == generationStep }.coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Static gold emblem — no looping motion; the honest live progress is the step list below.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.12f))
            )
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = steps[currentIndex].second,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = GoldPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LottieAnim("lottie_food.json", Modifier.size(96.dp))

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = languageViewModel.getString(StringKey.CREATING_YOUR_PLAN),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = c.textHi
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = languageViewModel.getString(steps[currentIndex].first),
            style = MaterialTheme.typography.bodyMedium,
            color = GoldBright
        )

        Spacer(modifier = Modifier.height(40.dp))

        GlassCard(shape = RoundedCornerShape(20.dp), tier = GlassTier.Hero) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                steps.forEachIndexed { index, (stringKey, icon, _) ->
                    val isCompleted = index < currentIndex
                    val isCurrent = index == currentIndex
                    // One-time check tick when a step completes.
                    val tick by animateFloatAsState(
                        targetValue = if (isCompleted) 1f else 0f,
                        label = "stepTick"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isCurrent) Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GoldPrimary.copy(alpha = 0.10f))
                                else Modifier
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .then(
                                    when {
                                        isCompleted -> Modifier.background(Tajly.GoldGradient)
                                        isCurrent -> Modifier.background(GoldPrimary.copy(alpha = 0.18f))
                                        else -> Modifier.background(c.s2)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isCompleted -> Icon(
                                    imageVector = TablerIcons.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp).scale(tick),
                                    tint = OnGold
                                )
                                isCurrent -> CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = GoldPrimary,
                                    strokeWidth = 2.dp
                                )
                                else -> Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = c.textLow
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = languageViewModel.getString(stringKey),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                            color = when {
                                isCompleted -> c.textHi
                                isCurrent -> c.textHi
                                else -> c.textLow
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Qualified clickable helper to keep the foundation import explicit at the call site. */
private fun Modifier.androidx_clickable(onClick: () -> Unit): Modifier =
    clickable(onClick = onClick)
