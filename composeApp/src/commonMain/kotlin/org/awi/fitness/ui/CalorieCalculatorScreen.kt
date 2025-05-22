package org.awi.fitness.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.TablerIcons
import compose.icons.tablericons.Activity
import compose.icons.tablericons.Battery
import compose.icons.tablericons.Flame
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.Run
import compose.icons.tablericons.Scale
import compose.icons.tablericons.Social
import compose.icons.tablericons.Walk
import org.awi.fitness.data.ActivityLevel
import org.awi.fitness.data.CalorieUiState
import org.awi.fitness.data.Gender
import org.awi.fitness.data.Goal
import org.awi.fitness.theme.BackgroundDark
import org.awi.fitness.theme.ChipSelectedText
import org.awi.fitness.theme.ChipUnselectedBackground
import org.awi.fitness.theme.ChipUnselectedText
import org.awi.fitness.theme.DarkCard
import org.awi.fitness.theme.GreenAccent
import org.awi.fitness.theme.InputFieldBackground
import org.awi.fitness.theme.InputFieldBorder
import org.awi.fitness.theme.TextGray
import org.awi.fitness.theme.TextWhite
import org.awi.fitness.viewmodel.CalorieViewModel

private val IconSelected = GreenAccent
private val IconUnselected = TextGray

@Composable
fun CalorieCalculatorScreen(
    viewModel: CalorieViewModel = remember { CalorieViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (!uiState.isCalculated) {
                CalorieInputForm(
                    uiState = uiState,
                    onWeightChange = viewModel::updateWeight,
                    onHeightChange = viewModel::updateHeight,
                    onAgeChange = viewModel::updateAge,
                    onGenderSelect = viewModel::updateGender,
                    onActivityLevelSelect = viewModel::updateActivityLevel,
                    onGoalSelect = viewModel::updateGoal
                )
            } else {
                CalorieResultScreen(
                    uiState = uiState,
                    onRecalculate = viewModel::resetCalculation
                )
            }
        }
        
        if (!uiState.isCalculated) {
            Button(
                onClick = viewModel::calculateCalories,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenAccent,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    "Calculate",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun CalorieInputForm(
    uiState: CalorieUiState,
    onWeightChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onGenderSelect: (Gender) -> Unit,
    onActivityLevelSelect: (ActivityLevel) -> Unit,
    onGoalSelect: (Goal) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Calorie Calculator",
            style = MaterialTheme.typography.headlineMedium,
            color = TextWhite
        )

        CalorieInputField(
            value = uiState.weight,
            onValueChange = onWeightChange,
            label = "Weight (kg)",
            keyboardType = KeyboardType.Decimal
        )

        CalorieInputField(
            value = uiState.height,
            onValueChange = onHeightChange,
            label = "Height (cm)",
            keyboardType = KeyboardType.Decimal
        )

        CalorieInputField(
            value = uiState.age,
            onValueChange = onAgeChange,
            label = "Age",
            keyboardType = KeyboardType.Number
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Gender",
                color = TextWhite,
                style = MaterialTheme.typography.titleLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Gender.entries.forEach { gender ->
                    GenderChip(
                        gender = gender,
                        selected = gender == uiState.gender,
                        onSelect = { onGenderSelect(gender) }
                    )
                }
            }
        }

        ActivityLevelSelector(
            selectedLevel = uiState.activityLevel,
            onLevelSelected = onActivityLevelSelect
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Goal",
                color = TextWhite,
                style = MaterialTheme.typography.titleLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Goal.entries.forEach { goal ->
                    FilterChip(
                        selected = goal == uiState.goal,
                        onClick = { onGoalSelect(goal) },
                        modifier = Modifier.weight(1f),
                        label = { 
                        Text(
                            goal.name.replace("_", " "),
                                style = MaterialTheme.typography.bodyMedium
                        )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenAccent,
                            containerColor = ChipUnselectedBackground,
                            selectedLabelColor = ChipSelectedText,
                            labelColor = ChipUnselectedText
                        )
                    )
                }
            }
        }

        uiState.error?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CalorieInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = TextGray,
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = TextWhite,
                focusedTextColor = TextWhite,
                unfocusedContainerColor = InputFieldBackground,
                focusedContainerColor = InputFieldBackground,
                unfocusedBorderColor = InputFieldBorder,
                focusedBorderColor = InputFieldBorder,
                cursorColor = TextWhite
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ActivityLevelChip(
    level: ActivityLevel,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onSelect,
        modifier = modifier
            .height(40.dp),
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = GreenAccent,
            containerColor = ChipUnselectedBackground,
            selectedLabelColor = ChipSelectedText,
            labelColor = ChipUnselectedText
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = if (selected) GreenAccent else InputFieldBorder,
            borderWidth = 1.dp,
            enabled = true,
            selected = selected
        ),
        leadingIcon = {
            Icon(
                imageVector = when (level) {
                    ActivityLevel.SEDENTARY -> TablerIcons.Social
                    ActivityLevel.LIGHTLY_ACTIVE -> TablerIcons.Walk
                    ActivityLevel.MODERATELY_ACTIVE -> TablerIcons.Run
                    ActivityLevel.VERY_ACTIVE -> TablerIcons.Activity
                    ActivityLevel.SUPER_ACTIVE -> TablerIcons.Flame
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (selected) IconSelected else IconUnselected
            )
        },
        label = {
        Text(
            text = level.name.replace("_", " "),
                style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
    )
}

@Composable
private fun GenderChip(
    gender: Gender,
    selected: Boolean,
    onSelect: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onSelect,
        modifier = Modifier
            .height(40.dp),
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = GreenAccent,
            containerColor = ChipUnselectedBackground,
            selectedLabelColor = ChipSelectedText,
            labelColor = ChipUnselectedText
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = if (selected) GreenAccent else InputFieldBorder,
            borderWidth = 1.dp,
            enabled = true,
            selected = true
        ),
        label = {
        Text(
            text = gender.name,
                style = MaterialTheme.typography.bodyMedium
        )
    }
    )
}

@Composable
private fun CalorieResultScreen(
    uiState: CalorieUiState,
    onRecalculate: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) { showDetails = true }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CalorieCircle(
            calories = uiState.calculatedCalories,
            showDetails = showDetails
        )

        AnimatedVisibility(
            visible = showDetails,
            enter = fadeIn() + expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoCard(
                    icon = TablerIcons.Flame,
                    title = "Basal Metabolic Rate (BMR)",
                    value = "${uiState.bmr.toInt()} kcal",
                    description = "The calories your body burns at complete rest"
                )

                InfoCard(
                    icon = TablerIcons.Battery,
                    title = "Total Daily Energy Expenditure",
                    value = "${uiState.tdee.toInt()} kcal",
                    description = "Your BMR adjusted for activity level"
                )

                InfoCard(
                    icon = TablerIcons.Scale,
                    title = "Goal Adjustment",
                    value = when (uiState.goal) {
                        Goal.LOSE_WEIGHT -> "-500 kcal"
                        Goal.MAINTAIN -> "±0 kcal"
                        Goal.GAIN_MUSCLE -> "+500 kcal"
                    },
                    description = "Calorie adjustment based on your selected goal"
                )
            }
        }

        Button(
            onClick = onRecalculate,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenAccent,
                contentColor = Color.Black
            )
        ) {
            Icon(
                imageVector = TablerIcons.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Recalculate")
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    title: String,
    value: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = GreenAccent,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
            }
        }
    }
}

@Composable
private fun CalorieCircle(
    calories: Int,
    showDetails: Boolean
) {
    Box(
        modifier = Modifier
            .size(300.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = 1f,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 24.dp,
            color = DarkCard.copy(alpha = 0.3f)
        )
        
        val animatedProgress by animateFloatAsState(
            targetValue = if (showDetails) 1f else 0f,
            animationSpec = tween(1500, easing = FastOutSlowInEasing),
            label = "progress"
        )
        
        CircularProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 24.dp,
            color = GreenAccent
        )

        Column(
            modifier = Modifier.scale(
                animateFloatAsState(
                    targetValue = if (showDetails) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "scale"
                ).value
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val animatedCalories by animateIntAsState(
                targetValue = if (showDetails) calories else 0,
                animationSpec = tween(1500, easing = FastOutSlowInEasing),
                label = "calories"
            )
            
            Text(
                text = "$animatedCalories",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = GreenAccent
            )
            Text(
                text = "kcal/day",
                style = MaterialTheme.typography.titleMedium,
                color = GreenAccent,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActivityLevelSelector(
    selectedLevel: ActivityLevel,
    onLevelSelected: (ActivityLevel) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Activity Level",
            color = TextWhite,
            style = MaterialTheme.typography.titleLarge
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActivityLevel.entries.forEach { level ->
                ActivityLevelChip(
                    level = level,
                    selected = level == selectedLevel,
                    onSelect = { onLevelSelected(level) },
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }
    }
} 