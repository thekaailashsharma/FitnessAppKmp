package org.awi.fitness.ui

import org.awi.fitness.utils.currentTimeMillis
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
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
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.Motion
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.CitationSection
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GlassChip
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.StatRing
import org.awi.fitness.ui.components.glassSource
import org.awi.fitness.utils.Citations
import org.awi.fitness.viewmodel.CalorieViewModel
import org.awi.fitness.viewmodel.LanguageViewModel
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.ic3d_fire
import org.jetbrains.compose.resources.painterResource
import kotlin.math.pow

private fun Float.formatToString(digits: Int = 1): String {
    val multiplier = pow(digits).toFloat()
    val roundedValue = kotlin.math.round(this * multiplier) / multiplier
    return roundedValue.toString()
}

/** One-time fade + rise as content appears (re-runs when a tab is (re)shown). */
@Composable
private fun Modifier.appearOnce(): Modifier {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) { anim.animateTo(1f, tween(Motion.DurEnter)) }
    return this.graphicsLayer {
        alpha = anim.value
        translationY = (1f - anim.value) * 24f
    }
}

class CalorieCalculatorScreen() : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = CalorieViewModel()
        val userSettings = UserSettings.getInstance()
        val languageViewModel = remember { LanguageViewModel(userSettings.settings) }
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf(
            languageViewModel.getString(StringKey.CALORIES_TAB),
            languageViewModel.getString(StringKey.WEIGHT_TAB),
            languageViewModel.getString(StringKey.MEASURE_TAB)
        )
        val navigator = LocalNavigator.currentOrThrow
        val c = TajlyTheme.colors

        ProvideGlass {
            Box(modifier = Modifier.fillMaxSize()) {
                AuroraBackground(modifier = Modifier.fillMaxSize().glassSource()) {}

                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    languageViewModel.getString(StringKey.CALORIE_CALCULATOR_TITLE),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { navigator.pop() }) {
                                    Icon(
                                        imageVector = TablerIcons.ArrowLeft,
                                        contentDescription = languageViewModel.getString(StringKey.BACK)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                titleContentColor = c.textHi,
                                navigationIconContentColor = c.textHi
                            )
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp)
                            .padding(top = 4.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        PremiumTabBar(tabs = tabs, selected = selectedTab, onSelect = { selectedTab = it })

                        when (selectedTab) {
                            0 -> CalorieCalculatorContent(viewModel, languageViewModel)
                            1 -> WeightTrackingScreen(languageViewModel)
                            2 -> MeasurementTrackingScreen(languageViewModel)
                        }
                    }
                }
            }
        }
    }

    // ── Slim gold-underline tab bar on glass ──
    @Composable
    private fun PremiumTabBar(tabs: List<String>, selected: Int, onSelect: (Int) -> Unit) {
        GlassCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    PremiumTab(
                        text = title,
                        selected = selected == index,
                        modifier = Modifier.weight(1f)
                    ) { onSelect(index) }
                }
            }
        }
    }

    @Composable
    private fun PremiumTab(
        text: String,
        selected: Boolean,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
    ) {
        val c = TajlyTheme.colors
        val interaction = remember { MutableInteractionSource() }
        Column(
            modifier = modifier
                .pressScale(interaction)
                .clip(RoundedCornerShape(14.dp))
                .clickable(interactionSource = interaction, indication = null, onClick = onClick)
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) c.textHi else c.textMid
            )
            Box(
                Modifier
                    .height(2.5.dp)
                    .width(if (selected) 22.dp else 0.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Tajly.GoldGradient)
            )
        }
    }

    // ── Section label, HomeScreen language ──
    @Composable
    private fun SectionLabel(text: String) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = GoldBright
        )
    }

    // ── Glass text field (transparent Material field over a glass surface) ──
    @Composable
    private fun GlassInputField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        keyboardType: KeyboardType
    ) {
        val c = TajlyTheme.colors
        GlassCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = MaterialTheme.typography.titleMedium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = c.textHi,
                    unfocusedTextColor = c.textHi,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = GoldPrimary,
                    focusedLabelColor = GoldBright,
                    unfocusedLabelColor = c.textMid
                )
            )
        }
    }

    // ── Premium dialog surface (solid, readable over the scrim) ──
    @Composable
    private fun DialogSurface(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
        val c = TajlyTheme.colors
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(c.s1)
                .border(1.dp, c.hairStrong, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
        }
    }

    @Composable
    private fun WeightTrackingScreen(languageViewModel: LanguageViewModel) {
        val settings = remember { UserSettings.getInstance() }
        val weighIns by settings.weighIns.collectAsState()
        var showAddDialog by remember { mutableStateOf(false) }
        var weight by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        val c = TajlyTheme.colors

        Column(
            modifier = Modifier.fillMaxWidth().appearOnce(),
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
                    fontWeight = FontWeight.Bold,
                    color = c.textHi
                )
                GoldButton(
                    text = languageViewModel.getString(StringKey.ADD_WEIGHT),
                    onClick = { showAddDialog = true }
                )
            }

            // Premium weight chart
            if (weighIns.size >= 2) {
                GlassCard(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val latest = weighIns.maxBy { it.date }
                        val oldest = weighIns.minBy { it.date }
                        val diff = latest.weight - oldest.weight
                        val diffText = if (diff > 0) "+${diff.formatToString()}" else diff.formatToString()
                        val diffColor = when {
                            diff > 0 -> Tajly.Coral        // gain
                            diff < 0 -> Tajly.Green         // loss
                            else -> c.textMid
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                languageViewModel.getString(StringKey.PROGRESS_GRAPH),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = c.textHi
                            )
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(diffColor.copy(alpha = 0.14f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    "$diffText kg",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = diffColor
                                )
                            }
                        }
                        WeightGraph(weighIns = weighIns)
                    }
                }
            } else if (weighIns.size == 1) {
                GlassCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        languageViewModel.getString(StringKey.ADD_ONE_MORE_WEIGHT_ENTRY),
                        modifier = Modifier.padding(18.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textMid
                    )
                }
            }

            // Weight entries
            weighIns.sortedByDescending { it.date }.forEach { entry ->
                GlassCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "${entry.weight} kg",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (entry.note.isNotEmpty()) {
                                Text(
                                    entry.note,
                                    color = c.textMid,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Text(
                            formatDate(entry.date),
                            color = c.textLow,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

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
                DialogSurface {
                    Text(
                        languageViewModel.getString(StringKey.ADD_WEIGHT_ENTRY),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi
                    )
                    GlassInputField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = languageViewModel.getString(StringKey.WEIGHT_KG),
                        keyboardType = KeyboardType.Decimal
                    )
                    GlassInputField(
                        value = note,
                        onValueChange = { note = it },
                        label = languageViewModel.getString(StringKey.WEIGHT_NOTE),
                        keyboardType = KeyboardType.Text
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text(languageViewModel.getString(StringKey.CANCEL), color = c.textMid)
                        }
                        Spacer(Modifier.width(8.dp))
                        GoldButton(
                            text = languageViewModel.getString(StringKey.SAVE),
                            enabled = weight.toFloatOrNull() != null,
                            onClick = {
                                weight.toFloatOrNull()?.let { weightValue ->
                                    settings.addWeighIn(
                                        WeighInEntry(
                                            weight = weightValue,
                                            date = currentTimeMillis() / 1000L,
                                            note = note
                                        )
                                    )
                                    showAddDialog = false
                                    weight = ""
                                    note = ""
                                }
                            }
                        )
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
        val c = TajlyTheme.colors

        val repository = remember { GeminiRepository() }
        val analysisScope = rememberCoroutineScope()
        var analysis by remember { mutableStateOf<MeasurementAnalysis?>(null) }
        var isAnalyzing by remember { mutableStateOf(false) }
        var analysisFailed by remember { mutableStateOf(false) }

        val runAnalysis: () -> Unit = {
            if (measurements.isNotEmpty() && !isAnalyzing) {
                isAnalyzing = true
                analysisFailed = false
                analysisScope.launch {
                    repository.analyzeMeasurements(
                        measurements = measurements,
                        weighIns = settings.weighIns.value
                    ).onSuccess {
                        analysis = it
                        isAnalyzing = false
                        analysisFailed = false
                    }.onFailure {
                        isAnalyzing = false
                        analysisFailed = true
                    }
                }
            }
        }

        // Load analysis only once when screen is opened
        LaunchedEffect(Unit) {
            if (measurements.isNotEmpty() && analysis == null) {
                runAnalysis()
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().appearOnce(),
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
                    fontWeight = FontWeight.Bold,
                    color = c.textHi
                )
                GoldButton(
                    text = languageViewModel.getString(StringKey.ADD),
                    onClick = { showAddDialog = true }
                )
            }

            if (isAnalyzing) {
                GlassCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = GoldPrimary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            languageViewModel.getString(StringKey.ANALYZING_YOUR_MEASUREMENTS),
                            color = c.textMid,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Analysis failure — surface a visible error with retry instead of nothing.
            if (analysisFailed && !isAnalyzing) {
                GlassCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            languageViewModel.getString(StringKey.ANALYSIS_FAILED),
                            color = c.textHi,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        GoldButton(
                            text = languageViewModel.getString(StringKey.RETRY),
                            onClick = { runAnalysis() }
                        )
                    }
                }
            }

            // Analysis card
            analysis?.let { measurementAnalysis ->
                GlassCard(shape = RoundedCornerShape(22.dp), goldTint = true, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SectionLabel(languageViewModel.getString(StringKey.ANALYSIS))

                        measurementAnalysis.insights.forEach { insight ->
                            Text(
                                insight,
                                color = c.textHi,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (measurementAnalysis.recommendations.isNotEmpty()) {
                            Text(
                                languageViewModel.getString(StringKey.RECOMMENDATIONS),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldPrimary
                            )
                            measurementAnalysis.recommendations.forEach { recommendation ->
                                Text(
                                    "• $recommendation",
                                    color = c.textMid,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Measurement entries
            measurements.sortedByDescending { it.date }.forEach { entry ->
                GlassCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            formatDate(entry.date),
                            color = c.textLow,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MeasureStat(languageViewModel.getString(StringKey.WAIST), "${entry.waist} cm")
                            MeasureStat(languageViewModel.getString(StringKey.HIPS), "${entry.hips} cm")
                            MeasureStat(languageViewModel.getString(StringKey.ARMS), "${entry.arms} cm")
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            Dialog(onDismissRequest = { showAddDialog = false }) {
                DialogSurface {
                    Text(
                        languageViewModel.getString(StringKey.ADD_MEASUREMENTS),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi
                    )
                    GlassInputField(
                        value = waist,
                        onValueChange = { waist = it },
                        label = languageViewModel.getString(StringKey.WAIST_CM),
                        keyboardType = KeyboardType.Decimal
                    )
                    GlassInputField(
                        value = hips,
                        onValueChange = { hips = it },
                        label = languageViewModel.getString(StringKey.HIPS_CM),
                        keyboardType = KeyboardType.Decimal
                    )
                    GlassInputField(
                        value = arms,
                        onValueChange = { arms = it },
                        label = languageViewModel.getString(StringKey.ARMS_CM),
                        keyboardType = KeyboardType.Decimal
                    )
                    GlassInputField(
                        value = note,
                        onValueChange = { note = it },
                        label = languageViewModel.getString(StringKey.WEIGHT_NOTE),
                        keyboardType = KeyboardType.Text
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text(languageViewModel.getString(StringKey.CANCEL), color = c.textMid)
                        }
                        Spacer(Modifier.width(8.dp))
                        GoldButton(
                            text = languageViewModel.getString(StringKey.SAVE),
                            enabled = waist.toFloatOrNull() != null &&
                                    hips.toFloatOrNull() != null &&
                                    arms.toFloatOrNull() != null,
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
                                            date = currentTimeMillis() / 1000L,
                                            note = note
                                        )
                                    )
                                    showAddDialog = false
                                    waist = ""
                                    hips = ""
                                    arms = ""
                                    note = ""
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun MeasureStat(label: String, value: String) {
        val c = TajlyTheme.colors
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = c.textMid, style = MaterialTheme.typography.labelSmall)
            Text(
                value,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }

    /** Premium gold line + soft area chart. Line reveals once (left→right). */
    @Composable
    private fun WeightGraph(weighIns: List<WeighInEntry>) {
        if (weighIns.isEmpty()) return
        val c = TajlyTheme.colors

        val sortedEntries = weighIns.sortedBy { it.date }
        val minWeight = sortedEntries.minOf { it.weight }
        val maxWeight = sortedEntries.maxOf { it.weight }
        val weightRange = (maxWeight - minWeight).coerceAtLeast(1f)

        val reveal = remember(weighIns.size) { Animatable(0f) }
        LaunchedEffect(weighIns.size) {
            reveal.snapTo(0f)
            reveal.animateTo(1f, tween(900))
        }

        val areaBrush = Brush.verticalGradient(
            listOf(GoldPrimary.copy(alpha = 0.30f), GoldPrimary.copy(alpha = 0.02f))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.7f)
                .padding(top = 8.dp, bottom = 4.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val padTop = 12f
                val padBottom = 12f
                val usableH = size.height - padTop - padBottom
                val points = sortedEntries.mapIndexed { index, entry ->
                    val x = size.width * (index.toFloat() / (sortedEntries.size - 1).coerceAtLeast(1))
                    val y = padTop + usableH * (1 - (entry.weight - minWeight) / weightRange)
                    Offset(x, y)
                }

                // Faint horizontal grid
                val steps = 4
                for (i in 0..steps) {
                    val y = padTop + usableH * (i.toFloat() / steps)
                    drawLine(
                        color = c.hair,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                if (points.isNotEmpty()) {
                    val linePath = Path()
                    val areaPath = Path()
                    linePath.moveTo(points.first().x, points.first().y)
                    areaPath.moveTo(points.first().x, size.height)
                    areaPath.lineTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        linePath.lineTo(points[i].x, points[i].y)
                        areaPath.lineTo(points[i].x, points[i].y)
                    }
                    areaPath.lineTo(points.last().x, size.height)
                    areaPath.close()

                    // Reveal line + area left→right
                    clipRect(right = size.width * reveal.value) {
                        drawPath(path = areaPath, brush = areaBrush)
                        drawPath(
                            path = linePath,
                            color = GoldPrimary,
                            style = Stroke(width = 5f, cap = StrokeCap.Round)
                        )
                        points.forEach { p ->
                            drawCircle(color = c.bg, radius = 7f, center = p)
                            drawCircle(color = GoldPrimary, radius = 4.5f, center = p)
                        }
                    }

                    // Emphasized latest point (fades in with reveal)
                    val last = points.last()
                    drawCircle(
                        color = GoldBright.copy(alpha = 0.22f * reveal.value),
                        radius = 16f,
                        center = last
                    )
                    drawCircle(color = GoldBright.copy(alpha = reveal.value), radius = 6.5f, center = last)
                    drawCircle(color = c.bg.copy(alpha = reveal.value), radius = 2.5f, center = last)
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

        Column(
            modifier = Modifier.fillMaxWidth().appearOnce(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Tajly.Coral.copy(alpha = 0.10f))
                        .border(1.dp, Tajly.Coral.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = error,
                        color = Tajly.Coral,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (!uiState.isCalculated) {
                GoldButton(
                    text = languageViewModel.getString(StringKey.CALCULATE),
                    onClick = viewModel::calculateCalories,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    loading = uiState.isLoading
                )
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            GlassInputField(
                value = uiState.weight,
                onValueChange = onWeightChange,
                label = languageViewModel.getString(StringKey.WEIGHT_KG),
                keyboardType = KeyboardType.Decimal
            )
            GlassInputField(
                value = uiState.height,
                onValueChange = onHeightChange,
                label = languageViewModel.getString(StringKey.HEIGHT_CM),
                keyboardType = KeyboardType.Decimal
            )
            GlassInputField(
                value = uiState.age,
                onValueChange = onAgeChange,
                label = languageViewModel.getString(StringKey.AGE),
                keyboardType = KeyboardType.Number
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel(languageViewModel.getString(StringKey.GENDER))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Gender.entries.forEach { gender ->
                        GlassChip(
                            text = gender.name,
                            selected = gender == uiState.gender,
                            onClick = { onGenderSelect(gender) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel(languageViewModel.getString(StringKey.ACTIVITY_LEVEL))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActivityLevel.entries.forEach { level ->
                        GlassChip(
                            text = level.name.replace("_", " "),
                            selected = level == uiState.activityLevel,
                            onClick = { onActivityLevelSelect(level) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel(languageViewModel.getString(StringKey.GOAL))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Goal.entries.forEach { goal ->
                        GlassChip(
                            text = goal.name.replace("_", " "),
                            selected = goal == uiState.goal,
                            onClick = { onGoalSelect(goal) }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun CalorieResultScreen(
        uiState: CalorieUiState,
        onRecalculate: () -> Unit,
        languageViewModel: LanguageViewModel
    ) {
        val c = TajlyTheme.colors

        // Daily target is the single source of truth: the exact value the ViewModel
        // computed and persisted as calculatedCalories (what Home/MealPlan read).
        val dailyTarget = uiState.calculatedCalories.coerceAtLeast(0)

        // Ring sweep on appear.
        val ring = remember { Animatable(0f) }
        LaunchedEffect(Unit) { ring.animateTo(1f, tween(900)) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── HERO: one big gold number on a gold ring ──
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                tier = org.awi.fitness.ui.components.GlassTier.Hero
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatRing(
                        progress = ring.value,
                        diameter = 200.dp,
                        strokeWidth = 14.dp
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(Res.drawable.ic3d_fire),
                                contentDescription = null,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "$dailyTarget",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 52.sp
                                ),
                                color = GoldPrimary
                            )
                            Text(
                                text = languageViewModel.getString(StringKey.CALORIES_PER_DAY),
                                style = MaterialTheme.typography.labelMedium,
                                color = c.textMid,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ── Demoted supporting stats: BMR · TDEE · goal adjustment ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatPill(
                    modifier = Modifier.weight(1f),
                    label = languageViewModel.getString(StringKey.BASAL_METABOLIC_RATE),
                    value = "${uiState.bmr.toInt()}",
                    unit = "kcal"
                )
                StatPill(
                    modifier = Modifier.weight(1f),
                    label = languageViewModel.getString(StringKey.TOTAL_DAILY_ENERGY),
                    value = "${uiState.tdee.toInt()}",
                    unit = "kcal"
                )
                StatPill(
                    modifier = Modifier.weight(1f),
                    label = languageViewModel.getString(StringKey.GOAL_ADJUSTMENT),
                    value = when (uiState.goal) {
                        Goal.LOSE_WEIGHT -> "-500"
                        Goal.MAINTAIN -> "±0"
                        Goal.GAIN_MUSCLE -> "+500"
                    },
                    unit = "kcal"
                )
            }

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

            GoldButton(
                text = languageViewModel.getString(StringKey.RECALCULATE),
                onClick = onRecalculate,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    /** Compact glass stat for demoted supporting numbers (gold tabular value). */
    @Composable
    private fun StatPill(
        label: String,
        value: String,
        unit: String,
        modifier: Modifier = Modifier
    ) {
        val c = TajlyTheme.colors
        GlassCard(
            modifier = modifier,
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = GoldPrimary,
                    maxLines = 1
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textLow
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textMid,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}
