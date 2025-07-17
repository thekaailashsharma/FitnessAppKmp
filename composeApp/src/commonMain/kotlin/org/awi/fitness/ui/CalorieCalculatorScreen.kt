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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.Activity
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Battery
import compose.icons.tablericons.Flame
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.Ruler
import compose.icons.tablericons.Run
import compose.icons.tablericons.Scale
import compose.icons.tablericons.Social
import compose.icons.tablericons.Walk
import kotlinx.datetime.Clock
import kotlinx.datetime.toLocalDateTime
import org.awi.fitness.data.ActivityLevel
import org.awi.fitness.data.CalorieUiState
import org.awi.fitness.data.Gender
import org.awi.fitness.data.Goal
import org.awi.fitness.data.MeasurementEntry
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.data.WeighInEntry
import org.awi.fitness.repository.GeminiRepository
import org.awi.fitness.repository.MeasurementAnalysis
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
import org.awi.fitness.ui.components.CitationSection
import org.awi.fitness.utils.Citations
import org.awi.fitness.viewmodel.CalorieViewModel
import org.awi.fitness.viewmodel.LanguageViewModel
import kotlin.math.pow

private val IconSelected = GreenAccent
private val IconUnselected = TextGray

private fun Float.formatToString(digits: Int = 1): String {
    val multiplier = pow(digits).toFloat()
    val roundedValue = kotlin.math.round(this * multiplier) / multiplier
    return roundedValue.toString()
}


class CalorieCalculatorScreen() : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = CalorieViewModel()
        val userSettings = UserSettings.getInstance()
        val languageViewModel = remember { LanguageViewModel(userSettings.settings) }
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Calories", "Weight", "Measure")
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(languageViewModel.getString(StringKey.CALORIE_CALCULATOR_TITLE)) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = TablerIcons.ArrowLeft,
                                contentDescription = languageViewModel.getString(StringKey.BACK)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BackgroundDark,
                        titleContentColor = TextWhite,
                        navigationIconContentColor = TextWhite
                    )
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDark)
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                        .padding(16.dp)
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = DarkCard,
                        contentColor = GreenAccent
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            )
                        }
                    }

                    when (selectedTab) {
                        0 -> CalorieCalculatorContent(viewModel, languageViewModel)
                        1 -> WeightTrackingScreen(languageViewModel)
                        2 -> MeasurementTrackingScreen(languageViewModel)
                    }
                }
            }
        }
    }

    @Composable
    private fun WeightTrackingScreen(languageViewModel: LanguageViewModel) {
        val settings = remember { UserSettings.getInstance() }
        val weighIns by settings.weighIns.collectAsState()
        var showAddDialog by remember { mutableStateOf(false) }
        var weight by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    languageViewModel.getString(StringKey.WEIGHT_TRACKING),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextWhite
                )

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenAccent,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        TablerIcons.Scale,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        languageViewModel.getString(StringKey.ADD_WEIGHT),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Weight graph
            if (weighIns.size >= 2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                languageViewModel.getString(StringKey.PROGRESS_GRAPH),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextWhite
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val latest = weighIns.maxBy { it.date }
                                val oldest = weighIns.minBy { it.date }
                                val diff = latest.weight - oldest.weight
                                val diffText = if (diff > 0) {
                                    "+${diff.formatToString()}"
                                } else {
                                    diff.formatToString()
                                }
                                val diffColor = when {
                                    diff > 0 -> Color.Red.copy(alpha = 0.8f)
                                    diff < 0 -> GreenAccent
                                    else -> TextWhite
                                }

                                Text(
                                    "$diffText kg",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = diffColor
                                )
                            }
                        }
                        WeightGraph(weighIns = weighIns)
                    }
                }
            } else if (weighIns.size == 1) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Text(
                        languageViewModel.getString(StringKey.ADD_ONE_MORE_WEIGHT_ENTRY),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray
                    )
                }
            }

            // Weight entries
            weighIns.sortedByDescending { it.date }.forEach { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "${entry.weight} kg",
                                color = GreenAccent,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (entry.note.isNotEmpty()) {
                                Text(
                                    entry.note,
                                    color = TextWhite,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Text(
                            formatDate(entry.date),
                            color = TextGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Citations for weight tracking
            CitationSection(
                citations = listOf(
                    Citations.WEIGHT_LOSS_RATE,
                    Citations.MEASUREMENT_FREQUENCY
                ),
                languageViewModel = languageViewModel,
                showDisclaimer = true
            )
        }

        if (showAddDialog) {
            Dialog(onDismissRequest = { showAddDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            languageViewModel.getString(StringKey.ADD_WEIGHT_ENTRY),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextWhite
                        )

                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text(languageViewModel.getString(StringKey.WEIGHT_KG)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = TextWhite,
                                focusedTextColor = TextWhite,
                                unfocusedContainerColor = InputFieldBackground,
                                focusedContainerColor = InputFieldBackground,
                                unfocusedBorderColor = InputFieldBorder,
                                focusedBorderColor = InputFieldBorder
                            )
                        )

                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text(languageViewModel.getString(StringKey.WEIGHT_NOTE)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = TextWhite,
                                focusedTextColor = TextWhite,
                                unfocusedContainerColor = InputFieldBackground,
                                focusedContainerColor = InputFieldBackground,
                                unfocusedBorderColor = InputFieldBorder,
                                focusedBorderColor = InputFieldBorder
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showAddDialog = false }) {
                                Text(languageViewModel.getString(StringKey.CANCEL), color = TextGray)
                            }
                            Button(
                                onClick = {
                                    weight.toFloatOrNull()?.let { weightValue ->
                                        settings.addWeighIn(
                                            WeighInEntry(
                                                weight = weightValue,
                                                date = Clock.System.now().epochSeconds,
                                                note = note
                                            )
                                        )
                                        showAddDialog = false
                                        weight = ""
                                        note = ""
                                    }
                                },
                                enabled = weight.toFloatOrNull() != null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenAccent,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(languageViewModel.getString(StringKey.SAVE))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun MeasurementTrackingScreen(languageViewModel: LanguageViewModel) {
        val settings = remember { UserSettings.getInstance() }
        val measurements by settings.measurements.collectAsState()
        var showAddDialog by remember { mutableStateOf(false) }
        var waist by remember { mutableStateOf("") }
        var hips by remember { mutableStateOf("") }
        var arms by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }

        val repository = remember { GeminiRepository() }
        var analysis by remember { mutableStateOf<MeasurementAnalysis?>(null) }
        var isAnalyzing by remember { mutableStateOf(false) }

        // Load analysis only once when screen is opened
        LaunchedEffect(Unit) {
            if (measurements.isNotEmpty() && analysis == null) {
                isAnalyzing = true
                repository.analyzeMeasurements(
                    measurements = measurements,
                    weighIns = settings.weighIns.value
                ).onSuccess {
                    analysis = it
                    isAnalyzing = false
                }.onFailure {
                    isAnalyzing = false
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    languageViewModel.getString(StringKey.MEASUREMENTS),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextWhite
                )

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenAccent,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        TablerIcons.Ruler,
                        null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        languageViewModel.getString(StringKey.ADD),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (isAnalyzing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = GreenAccent,
                            strokeWidth = 2.dp
                        )
                        Text(
                            languageViewModel.getString(StringKey.ANALYZING_YOUR_MEASUREMENTS),
                            color = TextWhite,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Analysis card
            analysis?.let { measurementAnalysis ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            languageViewModel.getString(StringKey.ANALYSIS),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextWhite
                        )

                        measurementAnalysis.insights.forEach { insight ->
                            Text(
                                insight,
                                color = TextWhite,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (measurementAnalysis.recommendations.isNotEmpty()) {
                            Text(
                                languageViewModel.getString(StringKey.RECOMMENDATIONS),
                                style = MaterialTheme.typography.titleMedium,
                                color = GreenAccent
                            )
                            measurementAnalysis.recommendations.forEach { recommendation ->
                                Text(
                                    "• $recommendation",
                                    color = TextWhite,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Measurement entries
            measurements.sortedByDescending { it.date }.forEach { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
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
                            Text(
                                formatDate(entry.date),
                                color = TextGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    languageViewModel.getString(StringKey.WAIST),
                                    color = TextGray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "${entry.waist} cm",
                                    color = GreenAccent,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Column {
                                Text(
                                    languageViewModel.getString(StringKey.HIPS),
                                    color = TextGray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "${entry.hips} cm",
                                    color = GreenAccent,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Column {
                                Text(
                                    languageViewModel.getString(StringKey.ARMS),
                                    color = TextGray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "${entry.arms} cm",
                                    color = GreenAccent,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            Dialog(onDismissRequest = { showAddDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            languageViewModel.getString(StringKey.ADD_MEASUREMENTS),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextWhite
                        )

                        OutlinedTextField(
                            value = waist,
                            onValueChange = { waist = it },
                            label = { Text(languageViewModel.getString(StringKey.WAIST_CM)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = TextWhite,
                                focusedTextColor = TextWhite,
                                unfocusedContainerColor = InputFieldBackground,
                                focusedContainerColor = InputFieldBackground,
                                unfocusedBorderColor = InputFieldBorder,
                                focusedBorderColor = InputFieldBorder
                            )
                        )

                        OutlinedTextField(
                            value = hips,
                            onValueChange = { hips = it },
                            label = { Text(languageViewModel.getString(StringKey.HIPS_CM)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = TextWhite,
                                focusedTextColor = TextWhite,
                                unfocusedContainerColor = InputFieldBackground,
                                focusedContainerColor = InputFieldBackground,
                                unfocusedBorderColor = InputFieldBorder,
                                focusedBorderColor = InputFieldBorder
                            )
                        )

                        OutlinedTextField(
                            value = arms,
                            onValueChange = { arms = it },
                            label = { Text(languageViewModel.getString(StringKey.ARMS_CM)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = TextWhite,
                                focusedTextColor = TextWhite,
                                unfocusedContainerColor = InputFieldBackground,
                                focusedContainerColor = InputFieldBackground,
                                unfocusedBorderColor = InputFieldBorder,
                                focusedBorderColor = InputFieldBorder
                            )
                        )

                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text(languageViewModel.getString(StringKey.WEIGHT_NOTE)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedTextColor = TextWhite,
                                focusedTextColor = TextWhite,
                                unfocusedContainerColor = InputFieldBackground,
                                focusedContainerColor = InputFieldBackground,
                                unfocusedBorderColor = InputFieldBorder,
                                focusedBorderColor = InputFieldBorder
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showAddDialog = false }) {
                                Text(languageViewModel.getString(StringKey.CANCEL), color = TextGray)
                            }
                            Button(
                                onClick = {
                                    val waistValue = waist.toFloatOrNull()
                                    val hipsValue = hips.toFloatOrNull()
                                    val armsValue = arms.toFloatOrNull()

                                    if (waistValue != null && hipsValue != null && armsValue != null) {
                                        settings.addMeasurement(
                                            MeasurementEntry(
                                                waist = waistValue,
                                                hips = hipsValue,
                                                arms = armsValue,
                                                date = Clock.System.now().epochSeconds,
                                                note = note
                                            )
                                        )
                                        showAddDialog = false
                                        waist = ""
                                        hips = ""
                                        arms = ""
                                        note = ""
                                    }
                                },
                                enabled = waist.toFloatOrNull() != null &&
                                        hips.toFloatOrNull() != null &&
                                        arms.toFloatOrNull() != null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenAccent,
                                    contentColor = Color.Black
                                )
                            ) {
                                Text(languageViewModel.getString(StringKey.SAVE))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun WeightGraph(weighIns: List<WeighInEntry>) {
        if (weighIns.isEmpty()) return

        val sortedEntries = weighIns.sortedBy { it.date }
        val minWeight = sortedEntries.minOf { it.weight }
        val maxWeight = sortedEntries.maxOf { it.weight }
        val weightRange = (maxWeight - minWeight).coerceAtLeast(1f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f)
                .padding(
                    start = 32.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 32.dp
                )
        ) {
            // Y-axis labels
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val steps = 5
                for (i in steps downTo 0) {
                    val weight = minWeight + (weightRange * (i.toFloat() / steps))
                    Text(
                        weight.formatToString(),
                        color = TextGray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            // Graph
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp)
            ) {
                val path = Path()
                val points = sortedEntries.mapIndexed { index, entry ->
                    val x =
                        size.width * (index.toFloat() / (sortedEntries.size - 1).coerceAtLeast(1))
                    val y = size.height * (1 - (entry.weight - minWeight) / weightRange)
                    Offset(x, y)
                }

                // Draw horizontal grid lines
                val steps = 5
                for (i in 0..steps) {
                    val y = size.height * (i.toFloat() / steps)
                    drawLine(
                        color = Color.White.copy(alpha = 0.1f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                // Draw the line
                if (points.isNotEmpty()) {
                    path.moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        path.lineTo(points[i].x, points[i].y)
                    }

                    drawPath(
                        path = path,
                        color = GreenAccent,
                        style = Stroke(width = 3f)
                    )
                }

                // Draw points
                points.forEach { point ->
                    drawCircle(
                        color = GreenAccent,
                        radius = 6f,
                        center = point
                    )
                }
            }

            // Weight values above points
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                sortedEntries.forEach { entry ->
                    Text(
                        entry.weight.formatToString(),
                        color = GreenAccent,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.offset(y = (-8).dp)
                    )
                }
            }
        }
    }

    private fun formatDate(timestamp: Long): String {
        val date = kotlinx.datetime.Instant.fromEpochSeconds(timestamp)
            .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        return "${date.dayOfMonth}/${date.monthNumber}/${date.year}"
    }

    @Composable
    private fun CalorieCalculatorContent(viewModel: CalorieViewModel, languageViewModel: LanguageViewModel) {
        val uiState by viewModel.uiState.collectAsState()

        if (!uiState.isCalculated) {
            CalorieInputForm(
                uiState = uiState,
                onWeightChange = viewModel::updateWeight,
                onHeightChange = viewModel::updateHeight,
                onAgeChange = viewModel::updateAge,
                onGenderSelect = viewModel::updateGender,
                onActivityLevelSelect = viewModel::updateActivityLevel,
                onGoalSelect = viewModel::updateGoal,
                languageViewModel = languageViewModel
            )
        } else {
            CalorieResultScreen(
                uiState = uiState,
                onRecalculate = viewModel::resetCalculation,
                languageViewModel = languageViewModel
            )
        }

        // Show error message if any
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Red.copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
            ) {
                Text(
                    text = error,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (!uiState.isCalculated) {
            Button(
                onClick = viewModel::calculateCalories,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenAccent,
                    contentColor = Color.Black
                ),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        languageViewModel.getString(StringKey.CALCULATE),
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
        onGoalSelect: (Goal) -> Unit,
        languageViewModel: LanguageViewModel
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = languageViewModel.getString(StringKey.CALORIE_CALCULATOR_TITLE),
                style = MaterialTheme.typography.headlineMedium,
                color = TextWhite
            )

            CalorieInputField(
                value = uiState.weight,
                onValueChange = onWeightChange,
                label = languageViewModel.getString(StringKey.WEIGHT_KG),
                keyboardType = KeyboardType.Decimal
            )

            CalorieInputField(
                value = uiState.height,
                onValueChange = onHeightChange,
                label = languageViewModel.getString(StringKey.HEIGHT_CM),
                keyboardType = KeyboardType.Decimal
            )

            CalorieInputField(
                value = uiState.age,
                onValueChange = onAgeChange,
                label = languageViewModel.getString(StringKey.AGE),
                keyboardType = KeyboardType.Number
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    languageViewModel.getString(StringKey.GENDER),
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
                onLevelSelected = onActivityLevelSelect,
                languageViewModel = languageViewModel
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    languageViewModel.getString(StringKey.GOAL),
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
        onRecalculate: () -> Unit,
        languageViewModel: LanguageViewModel
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Results section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DarkCard
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoCard(
                        icon = TablerIcons.Flame,
                        title = languageViewModel.getString(StringKey.BASAL_METABOLIC_RATE),
                        value = "${uiState.bmr.toInt()} kcal",
                        description = languageViewModel.getString(StringKey.BMR_DESC)
                    )

                    InfoCard(
                        icon = TablerIcons.Battery,
                        title = languageViewModel.getString(StringKey.TOTAL_DAILY_ENERGY),
                        value = "${uiState.tdee.toInt()} kcal",
                        description = languageViewModel.getString(StringKey.TDEE_DESC)
                    )

                    InfoCard(
                        icon = TablerIcons.Scale,
                        title = languageViewModel.getString(StringKey.GOAL_ADJUSTMENT),
                        value = when (uiState.goal) {
                            Goal.LOSE_WEIGHT -> "-500 kcal"
                            Goal.MAINTAIN -> "±0 kcal"
                            Goal.GAIN_MUSCLE -> "+500 kcal"
                        },
                        description = languageViewModel.getString(StringKey.GOAL_ADJUSTMENT_DESC)
                    )
                }
            }

            // Citations section
            CitationSection(
                citations = listOf(
                    Citations.BMR_CALCULATION,
                    Citations.ACTIVITY_LEVEL_MULTIPLIERS,
                    Citations.CALORIE_ADJUSTMENT,
                    Citations.MACRO_DISTRIBUTION,
                    Citations.PROTEIN_REQUIREMENTS
                ),
                languageViewModel = languageViewModel
            )

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
                Text(languageViewModel.getString(StringKey.RECALCULATE))
            }
        }
    }

    @Composable
    private fun MedicalCitations(languageViewModel: LanguageViewModel) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = DarkCard
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = languageViewModel.getString(StringKey.MEDICAL_CITATIONS),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite
                )

                // BMR Citation
                Text(
                    text = languageViewModel.getString(StringKey.BMR_CITATION),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )

                // Activity Level Citation
                Text(
                    text = languageViewModel.getString(StringKey.ACTIVITY_LEVEL_CITATION),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )

                // Calorie Adjustment Citation
                Text(
                    text = languageViewModel.getString(StringKey.CALORIE_ADJUSTMENT_CITATION),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
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
        showDetails: Boolean,
        languageViewModel: LanguageViewModel
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
                    text = languageViewModel.getString(StringKey.CALORIES_PER_DAY),
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
        onLevelSelected: (ActivityLevel) -> Unit,
        languageViewModel: LanguageViewModel
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                languageViewModel.getString(StringKey.ACTIVITY_LEVEL),
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
}