package org.awi.fitness.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.data.WeighInEntry
import org.awi.fitness.model.FitnessGoal
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.glassSource
import org.awi.fitness.utils.currentTimeMillis
import kotlin.math.roundToInt

/**
 * Edit Preferences — lets the user revise their physical stats and fitness goal.
 * All values are read from and written back to [UserSettings]; changing weight also
 * appends a [WeighInEntry] so the weight graph stays in sync.
 */
class EditPreferencesScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val c = TajlyTheme.colors
        val navigator = LocalNavigator.currentOrThrow
        val settings = UserSettings.getInstance()
        val languageViewModel = remember { LanguageViewModel(settings.settings) }

        // Seed editable state from real settings, with sensible defaults when unset.
        val initialHeight = settings.profileHeightCm.takeIf { it in 140..210 } ?: 175
        val initialWeight = settings.profileWeightKg.takeIf { it in 30f..200f } ?: 70f
        val initialAge = settings.profileAge.takeIf { it in 13..100 } ?: 25
        val initialGender = settings.profileGender
        val initialGoal = runCatching { FitnessGoal.valueOf(settings.fitnessGoal) }
            .getOrDefault(FitnessGoal.WEIGHT_LOSS)

        var height by remember { mutableStateOf(initialHeight) }
        var weight by remember { mutableStateOf(initialWeight) }
        var age by remember { mutableStateOf(initialAge) }
        var gender by remember { mutableStateOf(initialGender) }
        var goal by remember { mutableStateOf(initialGoal) }

        ProvideGlass {
            Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
                AuroraBackground(modifier = Modifier.fillMaxSize().glassSource()) {}

                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = languageViewModel.getString(StringKey.EDIT_PREFERENCES),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = c.textHi
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { navigator.pop() }) {
                                    Icon(TablerIcons.ArrowLeft, contentDescription = null, tint = c.textHi)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                ) { paddingValues ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
                    ) {
                        item {
                            SectionKicker(languageViewModel.getString(StringKey.SECTION_BODY))
                        }
                        item {
                            StepperCard(
                                title = languageViewModel.getString(StringKey.HEIGHT_LABEL),
                                valueLabel = "$height cm",
                                onDecrement = { if (height > 140) height -= 1 },
                                onIncrement = { if (height < 210) height += 1 }
                            )
                        }
                        item {
                            StepperCard(
                                title = languageViewModel.getString(StringKey.WEIGHT_LABEL),
                                valueLabel = "${weight.roundToInt()} kg",
                                onDecrement = { if (weight > 30f) weight -= 1f },
                                onIncrement = { if (weight < 200f) weight += 1f }
                            )
                        }
                        item {
                            StepperCard(
                                title = languageViewModel.getString(StringKey.AGE),
                                valueLabel = "$age yrs",
                                onDecrement = { if (age > 13) age -= 1 },
                                onIncrement = { if (age < 100) age += 1 }
                            )
                        }

                        item {
                            SectionKicker(languageViewModel.getString(StringKey.BIOLOGICAL_SEX))
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                SelectChip(
                                    label = languageViewModel.getString(StringKey.GENDER_MALE),
                                    selected = gender == "MALE",
                                    modifier = Modifier.weight(1f),
                                    onClick = { gender = "MALE" }
                                )
                                SelectChip(
                                    label = languageViewModel.getString(StringKey.GENDER_FEMALE),
                                    selected = gender == "FEMALE",
                                    modifier = Modifier.weight(1f),
                                    onClick = { gender = "FEMALE" }
                                )
                            }
                        }

                        item {
                            SectionKicker(languageViewModel.getString(StringKey.FITNESS_GOAL_LABEL))
                        }
                        for (option in FitnessGoal.entries) {
                            item {
                                GoalOptionRow(
                                    title = languageViewModel.getString(option.titleKey()),
                                    subtitle = languageViewModel.getString(option.subtitleKey()),
                                    selected = goal == option,
                                    onClick = { goal = option }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            GoldButton(
                                text = languageViewModel.getString(StringKey.SAVE_CHANGES),
                                onClick = {
                                    val weightChanged = weight.roundToInt() != initialWeight.roundToInt()
                                    settings.profileHeightCm = height
                                    settings.profileWeightKg = weight
                                    settings.profileAge = age
                                    settings.profileGender = gender
                                    settings.fitnessGoal = goal.name
                                    if (weightChanged) {
                                        settings.addWeighIn(
                                            WeighInEntry(
                                                weight = weight,
                                                date = currentTimeMillis() / 1000L,
                                                note = "Updated in profile"
                                            )
                                        )
                                    }
                                    navigator.pop()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

// Faithful, localized labels per enum value (no more mislabeling MUSCLE_GAIN as
// "Build strength", ENDURANCE as "Stay active", FLEXIBILITY as "Reduce stress").
private fun FitnessGoal.titleKey(): StringKey = when (this) {
    FitnessGoal.WEIGHT_LOSS -> StringKey.GOAL_WEIGHT_LOSS
    FitnessGoal.MUSCLE_GAIN -> StringKey.GOAL_MUSCLE_GAIN
    FitnessGoal.ENDURANCE -> StringKey.GOAL_ENDURANCE
    FitnessGoal.FLEXIBILITY -> StringKey.GOAL_FLEXIBILITY
}

private fun FitnessGoal.subtitleKey(): StringKey = when (this) {
    FitnessGoal.WEIGHT_LOSS -> StringKey.GOAL_WEIGHT_LOSS_DESC
    FitnessGoal.MUSCLE_GAIN -> StringKey.GOAL_MUSCLE_GAIN_DESC
    FitnessGoal.ENDURANCE -> StringKey.GOAL_ENDURANCE_DESC
    FitnessGoal.FLEXIBILITY -> StringKey.GOAL_FLEXIBILITY_DESC
}

@Composable
private fun SectionKicker(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = GoldBright,
        modifier = Modifier.padding(top = 8.dp, start = 4.dp)
    )
}

@Composable
private fun StepperCard(
    title: String,
    valueLabel: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    val c = TajlyTheme.colors
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = valueLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMid
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StepperButton(glyph = "−", onClick = onDecrement)
                StepperButton(glyph = "+", onClick = onIncrement)
            }
        }
    }
}

@Composable
private fun StepperButton(glyph: String, onClick: () -> Unit) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(40.dp)
            .pressScale(interaction)
            .clip(CircleShape)
            .background(GoldPrimary.copy(alpha = 0.15f))
            .border(1.dp, c.hairStrong, CircleShape)
            .clickable(interactionSource = interaction, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = glyph,
            style = MaterialTheme.typography.titleLarge,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = GoldBright
        )
    }
}

@Composable
private fun SelectChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(16.dp)
    GlassCard(
        modifier = modifier
            .pressScale(interaction)
            .then(
                if (selected) Modifier.border(1.dp, Tajly.GoldGradient, shape) else Modifier
            )
            .clickable(interactionSource = interaction, indication = null) { onClick() },
        shape = shape,
        goldTint = selected
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) GoldBright else c.textHi
            )
        }
    }
}

@Composable
private fun GoalOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(18.dp)
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .then(
                if (selected) Modifier.border(1.dp, Tajly.GoldGradient, shape) else Modifier
            )
            .clickable(interactionSource = interaction, indication = null) { onClick() },
        shape = shape,
        goldTint = selected
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid
                )
            }
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .then(
                        if (selected) Modifier.background(Tajly.GoldGradient)
                        else Modifier.border(1.5.dp, c.hairStrong, CircleShape)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Text(
                        text = "✓",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnGold
                    )
                }
            }
        }
    }
}
