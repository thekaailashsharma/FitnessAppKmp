package org.awi.fitness.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.FitnessGoal
import org.awi.fitness.model.FitnessLevel
import org.awi.fitness.navigation.LocalAppNavigation
import org.awi.fitness.navigation.RootRoute
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.WorkoutViewModel
import org.awi.fitness.ui.components.TajlyLogoMark

private val Gold = GoldPrimary
private val GoldDim = GoldPrimary.copy(alpha = 0.15f)
private val OnboardingBg = Color(0xFF0C0B09)
private val CardBg = Color(0xFF181610)
private val CardBorder = Color(0xFF2C2820)

class OnboardingScreen(
    private val languageViewModel: LanguageViewModel,
) : Screen {
    @Composable
    override fun Content() {
        val appNavigation = LocalAppNavigation.current
        val workoutViewModel = remember { WorkoutViewModel() }
        val scope = rememberCoroutineScope()

        var step by remember { mutableIntStateOf(0) }
        var userName by remember { mutableStateOf("") }
        var selectedGoal by remember { mutableStateOf<FitnessGoal?>(null) }
        var selectedLevel by remember { mutableStateOf<FitnessLevel?>(null) }
        var workoutDays by remember { mutableIntStateOf(3) }

        val totalSteps = 4 // 0=Welcome 1=Name 2=Goal 3=Level 4=Days

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OnboardingBg)
        ) {
            // Ambient glow that shifts per step
            val glowAlpha by animateFloatAsState(
                targetValue = 0.06f,
                animationSpec = tween(600),
                label = "glow"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Gold.copy(alpha = glowAlpha * 2f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 24.dp)
            ) {
                // Progress dots
                StepDots(currentStep = step, totalSteps = totalSteps)

                Spacer(modifier = Modifier.height(24.dp))

                // Step content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally(tween(350)) { it } + fadeIn(tween(300)))
                                    .togetherWith(slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(200)))
                            } else {
                                (slideInHorizontally(tween(350)) { -it } + fadeIn(tween(300)))
                                    .togetherWith(slideOutHorizontally(tween(300)) { it } + fadeOut(tween(200)))
                            }
                        },
                        label = "onboardingStep",
                        modifier = Modifier.fillMaxSize()
                    ) { currentStep ->
                        // Scrollable steps (potentially tall content)
                        if (currentStep == 0 || currentStep == 2 || currentStep == 3) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                when (currentStep) {
                                    0 -> WelcomeStep()
                                    2 -> GoalStep(selectedGoal) { selectedGoal = it }
                                    else -> LevelStep(selectedLevel) { selectedLevel = it }
                                }
                            }
                        } else {
                            // Compact steps — vertically centred, no scroll needed
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                when (currentStep) {
                                    1 -> NameStep(userName) { userName = it }
                                    else -> DaysStep(workoutDays) { workoutDays = it }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (step > 0) {
                        OutlinedButton(
                            onClick = { step -= 1 },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            enabled = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White.copy(alpha = 0.8f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Text("Back", style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Button(
                        onClick = {
                            when (step) {
                                0 -> step = 1
                                1 -> if (userName.isNotBlank()) step = 2
                                2 -> if (selectedGoal != null) step = 3
                                3 -> if (selectedLevel != null) step = 4
                                else -> {
                                    val goal = selectedGoal ?: return@Button
                                    val level = selectedLevel ?: return@Button
                                    // Save preferences locally — instant, no network call
                                    val settings = UserSettings.getInstance()
                                    if (userName.isNotBlank()) settings.userName = userName.trim()
                                    settings.fitnessGoal = goal.name
                                    settings.fitnessLevel = level.name
                                    settings.workoutDaysPerWeek = workoutDays
                                    settings.hasCompletedOnboarding = true
                                    // Generate workout plan in background after navigation
                                    scope.launch {
                                        try {
                                            workoutViewModel.saveUserGoals(goal, level, workoutDays, "")
                                        } catch (_: Exception) {
                                            // Non-fatal — user can generate plan from Train tab
                                        }
                                    }
                                    appNavigation.navigateTo(RootRoute.Main)
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        enabled = when (step) {
                            1 -> userName.isNotBlank()
                            2 -> selectedGoal != null
                            3 -> selectedLevel != null
                            else -> true
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold)
                    ) {
                        Text(
                            text = if (step >= totalSteps) "Let's go →" else "Continue",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0C0B09)
                        )
                    }
                }
            }
        }
    }
}

// ── Progress dots ────────────────────────────────────────────────────────────

@Composable
private fun StepDots(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps + 1) { index ->
            val isActive = index == currentStep
            val isDone = index < currentStep
            val width by animateDpAsState(
                targetValue = if (isActive) 24.dp else 8.dp,
                animationSpec = spring(dampingRatio = 0.7f),
                label = "dotWidth$index"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        when {
                            isActive -> Gold
                            isDone -> Gold.copy(alpha = 0.4f)
                            else -> Color.White.copy(alpha = 0.15f)
                        }
                    )
            )
        }
    }
}

// ── Step 0: Welcome ──────────────────────────────────────────────────────────

@Composable
private fun WelcomeStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 24.dp)
    ) {
        // Pulsing logo with glow ring
        Box(contentAlignment = Alignment.Center) {
            val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
                initialValue = 1f, targetValue = 1.08f,
                animationSpec = infiniteRepeatable(tween(1800, easing = EaseInOut), RepeatMode.Reverse),
                label = "pulseScale"
            )
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(pulse)
                    .background(
                        Brush.radialGradient(listOf(Gold.copy(0.18f), Color.Transparent)),
                        CircleShape
                    )
            )
            TajlyLogoMark(size = 96.dp, animated = true)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Welcome to TAJLY",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(listOf(Color.White, GoldBright))
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Your personal fitness companion.\nLet's set you up for success.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )
        }

        // Feature pills
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            listOf(
                "🏋️" to "Personalised workout plans",
                "🥗" to "AI-generated meal plans",
                "🏆" to "Challenges & community"
            ).forEach { (emoji, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(emoji, fontSize = 22.sp)
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ── Step 1: Name ─────────────────────────────────────────────────────────────

@Composable
private fun NameStep(name: String, onNameChange: (String) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, bottom = 24.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(GoldDim, CircleShape)
                .border(1.dp, Gold.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("👋", fontSize = 34.sp)
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "What's your name?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "We'll personalise everything just for you.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = {
                Text(
                    "Your first name",
                    color = Color.White.copy(alpha = 0.35f),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold,
                unfocusedBorderColor = CardBorder,
                cursorColor = Gold,
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )
    }
}

// ── Step 2: Goal ─────────────────────────────────────────────────────────────

private data class GoalOption(
    val goal: FitnessGoal,
    val emoji: String,
    val title: String,
    val subtitle: String
)

private val goalOptions = listOf(
    GoalOption(FitnessGoal.WEIGHT_LOSS, "🔥", "Lose Weight", "Burn fat and feel lighter"),
    GoalOption(FitnessGoal.MUSCLE_GAIN, "💪", "Build Muscle", "Get stronger and more defined"),
    GoalOption(FitnessGoal.ENDURANCE, "🏃", "Boost Endurance", "Run farther, last longer"),
    GoalOption(FitnessGoal.FLEXIBILITY, "🧘", "Stay Flexible", "Move freely, reduce injury"),
)

@Composable
private fun GoalStep(selected: FitnessGoal?, onSelect: (FitnessGoal) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "What's your goal?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "We'll build your plan around this.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        goalOptions.forEach { option ->
            val isSelected = selected == option.goal
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.02f else 1f,
                animationSpec = spring(dampingRatio = 0.7f),
                label = "goalScale"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) GoldDim else CardBg)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) Gold else CardBorder,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(option.goal) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (isSelected) Gold.copy(0.2f) else Color.White.copy(0.05f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(option.emoji, fontSize = 26.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        option.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Gold else Color.White
                    )
                    Text(
                        option.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            TablerIcons.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Step 3: Level ────────────────────────────────────────────────────────────

private data class LevelOption(
    val level: FitnessLevel,
    val emoji: String,
    val title: String,
    val subtitle: String
)

private val levelOptions = listOf(
    LevelOption(FitnessLevel.BEGINNER, "🌱", "Beginner", "New to working out or getting back in"),
    LevelOption(FitnessLevel.INTERMEDIATE, "⚡", "Intermediate", "Training regularly for 6+ months"),
    LevelOption(FitnessLevel.ADVANCED, "🔥", "Advanced", "High intensity, competitive athlete"),
)

@Composable
private fun LevelStep(selected: FitnessLevel?, onSelect: (FitnessLevel) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Your fitness level?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Honest answers give better plans.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        levelOptions.forEach { option ->
            val isSelected = selected == option.level
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.02f else 1f,
                animationSpec = spring(dampingRatio = 0.7f),
                label = "levelScale"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) GoldDim else CardBg)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) Gold else CardBorder,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelect(option.level) }
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(option.emoji, fontSize = 30.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        option.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Gold else Color.White
                    )
                    Text(
                        option.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            TablerIcons.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Step 4: Days ─────────────────────────────────────────────────────────────

@Composable
private fun DaysStep(days: Int, onChange: (Int) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Days per week?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "We'll build a plan that fits your schedule.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big selected number display
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(GoldDim, CircleShape)
                .border(2.dp, Gold.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = days.toString(),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Gold
                )
                Text(
                    "days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gold.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day selector pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            (2..6).forEach { day ->
                val isSelected = day == days
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1f,
                    animationSpec = spring(dampingRatio = 0.7f),
                    label = "dayScale$day"
                )
                Box(
                    modifier = Modifier
                        .scale(scale)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Gold else CardBg)
                        .border(1.dp, if (isSelected) Gold else CardBorder, CircleShape)
                        .clickable { onChange(day) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        day.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Commitment message
        val message = when (days) {
            2 -> "Light commitment — great for beginners 🌱"
            3 -> "Balanced routine — ideal for most people ⚖️"
            4 -> "Strong commitment — solid results ahead 💪"
            5 -> "High dedication — you mean business 🔥"
            else -> "Elite level — maximum gains ahead 🏆"
        }
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = Gold.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(GoldDim)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}
