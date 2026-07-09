package org.awi.fitness.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.Clock
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Egg
import compose.icons.tablericons.Leaf
import compose.icons.tablericons.List
import compose.icons.tablericons.Mug
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Scan
import compose.icons.tablericons.ShoppingCart
import compose.icons.tablericons.Star
import compose.icons.tablericons.ToolsKitchen2
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.ic3d_leaf
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.Meal
import org.awi.fitness.model.MealSlot
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.AddEditMealSheet
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.EmptyState
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.LottieAnim
import org.awi.fitness.ui.components.StatRing
import org.awi.fitness.ui.components.localizedName
import org.jetbrains.compose.resources.painterResource
import org.awi.fitness.viewmodel.DailyMacros
import org.awi.fitness.viewmodel.ScannedMealEntry
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.LocalLanguageViewModel
import org.awi.fitness.viewmodel.LocalMealPlanViewModel

class MealScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userSettings = UserSettings.getInstance()
        val viewModel = LocalMealPlanViewModel.current
        val languageViewModel = LocalLanguageViewModel.current
        val state by viewModel.state.collectAsState()
        val completions by userSettings.mealCompletions.collectAsState()
        var showSetupSheet by remember { mutableStateOf(false) }
        var showAddMealSheet by remember { mutableStateOf(false) }
        var showAddMenu by remember { mutableStateOf(false) }
        var mealToConfirm by remember { mutableStateOf<Meal?>(null) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            viewModel.loadIfNeeded()
        }

        AuroraBackground(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading || state.isGenerating -> {
                    MealLoadingState(
                        isGenerating = state.isGenerating,
                        languageViewModel = languageViewModel
                    )
                }

                state.activePlan == null -> {
                    EmptyMealPlanState(
                        languageViewModel = languageViewModel,
                        onCreateClick = { showSetupSheet = true }
                    )
                }

                else -> {
                    val plan = state.activePlan!!
                    val selectedDay = state.selectedDayOfWeek
                    val dateString = state.selectedDateString
                    val slots = viewModel.getSlotsForDay(selectedDay)
                    val macros = viewModel.getDailyMacros(selectedDay, dateString)
                    val completedForDate = completions[dateString] ?: emptySet()

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        // Extra bottom padding so the last meal clears the floating bottom nav bar.
                        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
                    ) {
                        item {
                            MealHeader(
                                languageViewModel = languageViewModel,
                                showAddMenu = showAddMenu,
                                onAddMenuExpand = { showAddMenu = true },
                                onAddMenuDismiss = { showAddMenu = false },
                                onGenerateAi = {
                                    showAddMenu = false
                                    showSetupSheet = true
                                },
                                onAddManually = {
                                    showAddMenu = false
                                    showAddMealSheet = true
                                },
                                onMyPlans = { navigator.push(MyPlansScreen(viewModel)) },
                                onShopping = { navigator.push(ShoppingListScreen(plan, languageViewModel)) }
                            )
                        }

                        item {
                            DaySelector(
                                selectedDay = selectedDay,
                                todayDay = state.todayDayOfWeek,
                                onDaySelected = { viewModel.selectDay(it) }
                            )
                        }

                        item {
                            DayFuelCard(macros = macros, languageViewModel = languageViewModel)
                        }

                        item {
                            // Prominent "Scan a meal" entry — visual, wired to the existing
                            // AI-generate flow (opens the meal-plan setup sheet). No new nav.
                            ScanMealEntry(onClick = { navigator.push(MealScanScreen()) })
                        }

                        items(slots) { slot ->
                            val mealsForSlot = viewModel.getMealsForSlot(selectedDay, slot)
                            SlotSection(
                                slot = slot,
                                meals = mealsForSlot,
                                completedMealIds = completedForDate,
                                languageViewModel = languageViewModel,
                                onMealClick = { meal ->
                                    navigator.push(MealDetailScreen(meal.id, viewModel, languageViewModel))
                                },
                                onToggleCompletion = { meal ->
                                    if (!viewModel.isSelectedDayToday()) {
                                        mealToConfirm = meal
                                    } else {
                                        viewModel.toggleMealCompletion(meal.id, dateString)
                                    }
                                }
                            )
                        }

                        val scannedMeals = viewModel.getScannedMealsForDate(dateString)
                        if (scannedMeals.isNotEmpty()) {
                            item {
                                ScannedMealsSection(scannedMeals = scannedMeals)
                            }
                        }

                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
        }

        if (showSetupSheet) {
            MealPlanSetupScreen(
                viewModel = viewModel,
                languageViewModel = languageViewModel,
                onDismiss = { showSetupSheet = false }
            )
        }

        if (showAddMealSheet && state.activePlan != null) {
            AddEditMealSheet(
                existingMeal = null,
                dayOfWeek = state.selectedDayOfWeek,
                languageViewModel = languageViewModel,
                onDismiss = { showAddMealSheet = false },
                onSave = { meal ->
                    scope.launch {
                        viewModel.addManualMeal(meal)
                        showAddMealSheet = false
                    }
                }
            )
        }

        mealToConfirm?.let { meal ->
            val dateString = state.selectedDateString
            AlertDialog(
                onDismissRequest = { mealToConfirm = null },
                title = {
                    Text(
                        languageViewModel.getString(StringKey.NOT_TODAY_TITLE),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Text(
                        languageViewModel.getString(StringKey.NOT_TODAY_DESC),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.toggleMealCompletion(meal.id, dateString)
                        mealToConfirm = null
                    }) {
                        Text(
                            languageViewModel.getString(StringKey.MARK_ANYWAY),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mealToConfirm = null }) {
                        Text(languageViewModel.getString(StringKey.CANCEL))
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
private fun MealHeader(
    languageViewModel: LanguageViewModel,
    showAddMenu: Boolean,
    onAddMenuExpand: () -> Unit,
    onAddMenuDismiss: () -> Unit,
    onGenerateAi: () -> Unit,
    onAddManually: () -> Unit,
    onMyPlans: () -> Unit,
    onShopping: () -> Unit
) {
    val c = TajlyTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "NUTRITION",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Tajly.Champagne
            )
            Text(
                text = languageViewModel.getString(StringKey.MEAL_PLAN),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = c.textHi
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box {
                // Primary add action — gold glass tile
                GlassActionButton(
                    icon = { tint ->
                        Icon(
                            imageVector = TablerIcons.Plus,
                            contentDescription = languageViewModel.getString(StringKey.ADD_MEAL),
                            tint = tint,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    gold = true,
                    onClick = onAddMenuExpand
                )
                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = onAddMenuDismiss,
                    containerColor = c.s2
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                languageViewModel.getString(StringKey.GENERATE_WITH_AI),
                                color = c.textHi
                            )
                        },
                        onClick = onGenerateAi,
                        leadingIcon = {
                            Icon(
                                TablerIcons.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                languageViewModel.getString(StringKey.ADD_MANUALLY),
                                color = c.textHi
                            )
                        },
                        onClick = onAddManually,
                        leadingIcon = {
                            Icon(
                                TablerIcons.Edit,
                                contentDescription = null,
                                tint = c.textMid,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }
            GlassActionButton(
                icon = { tint ->
                    Icon(
                        imageVector = TablerIcons.List,
                        contentDescription = languageViewModel.getString(StringKey.MY_PLANS),
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = onMyPlans
            )
            GlassActionButton(
                icon = { tint ->
                    Icon(
                        imageVector = TablerIcons.ShoppingCart,
                        contentDescription = languageViewModel.getString(StringKey.SHOPPING_LIST),
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                },
                onClick = onShopping
            )
        }
    }
}

@Composable
private fun GlassActionButton(
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gold: Boolean = false
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(13.dp)
    val base = modifier
        .pressScale(interaction)
        .size(42.dp)
        .clip(shape)
    val styled = if (gold) {
        base.background(Tajly.GoldGradient)
    } else {
        base.background(c.glassFill, shape).border(1.dp, c.hairStrong, shape)
    }
    Box(
        modifier = styled.clickable(
            interactionSource = interaction,
            indication = null,
            onClick = onClick
        ),
        contentAlignment = Alignment.Center
    ) {
        icon(if (gold) OnGold else c.textHi)
    }
}

/** Compact "Scan a meal" pill — slim so it doesn't dominate the meal list. Opens the AI flow. */
@Composable
private fun ScanMealEntry(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .clip(RoundedCornerShape(14.dp))
            .background(Tajly.GoldGradient)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = TablerIcons.Scan,
            contentDescription = null,
            tint = OnGold,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Scan a meal",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = OnGold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "AI",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = OnGold.copy(alpha = 0.65f)
        )
    }
}

/** Top card: the day's calorie ring (consumed / target) + P/C/F bars (Blue/Green/Violet). */
@Composable
private fun DayFuelCard(
    macros: DailyMacros,
    languageViewModel: LanguageViewModel
) {
    val c = TajlyTheme.colors
    val calProgress = if (macros.targetCalories > 0)
        (macros.consumedCalories.toFloat() / macros.targetCalories).coerceIn(0f, 1f) else 0f
    val remaining = (macros.targetCalories - macros.consumedCalories).coerceAtLeast(0)

    // One-time gold ring sweep (re-sweeps only when the value actually changes).
    val ring = remember { Animatable(0f) }
    LaunchedEffect(calProgress) { ring.animateTo(calProgress, tween(800)) }
    val percent = (calProgress * 100).toInt()

    GlassCard(modifier = Modifier.fillMaxWidth(), goldTint = true) {
        // Soft gold halo behind the ring.
        Box(
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp)
                .blur(38.dp)
                .background(
                    Brush.radialGradient(listOf(Tajly.Champagne.copy(alpha = 0.30f), Color.Transparent))
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            StatRing(
                progress = ring.value,
                diameter = 104.dp,
                strokeWidth = 11.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$percent",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = c.textHi,
                        fontSize = 26.sp
                    )
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textLow
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TODAY'S FUEL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textMid
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${macros.consumedCalories}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Tajly.Champagne
                    )
                    Text(
                        text = " / ${macros.targetCalories} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textMid,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    text = "$remaining kcal remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textLow
                )

                Spacer(Modifier.height(14.dp))

                MacroBar("Protein", macros.consumedProtein, macros.targetProtein, Tajly.Blue)
                Spacer(Modifier.height(8.dp))
                MacroBar("Carbs", macros.consumedCarbs, macros.targetCarbs, Tajly.Green)
                Spacer(Modifier.height(8.dp))
                MacroBar("Fat", macros.consumedFat, macros.targetFat, Tajly.Violet)
            }
        }
    }
}

@Composable
private fun MacroBar(
    label: String,
    consumed: Int,
    target: Int,
    color: Color
) {
    val c = TajlyTheme.colors
    val progress = if (target > 0) (consumed.toFloat() / target).coerceIn(0f, 1f) else 0f
    val bar = remember { Animatable(0f) }
    LaunchedEffect(progress) { bar.animateTo(progress, tween(700)) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textMid
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$consumed",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi
                )
                Text(
                    text = " / ${target}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textLow
                )
            }
        }
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(c.hairStrong)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(bar.value)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

/** Monoline food icon per slot — the illustration for the meal cards. */
private fun MealSlot.monoIcon(): ImageVector = when (this) {
    MealSlot.BREAKFAST -> TablerIcons.Egg
    MealSlot.LUNCH -> TablerIcons.Leaf
    MealSlot.DINNER -> TablerIcons.ToolsKitchen2
    MealSlot.SNACK -> TablerIcons.Mug
}

@Composable
private fun SlotSection(
    slot: MealSlot,
    meals: List<Meal>,
    completedMealIds: Set<String>,
    languageViewModel: LanguageViewModel,
    onMealClick: (Meal) -> Unit,
    onToggleCompletion: (Meal) -> Unit
) {
    val c = TajlyTheme.colors
    val accent = slot.accentColorLocal()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = slot.monoIcon(),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = slot.localizedName(languageViewModel),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = c.textHi,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${meals.count { completedMealIds.contains(it.id) }}/${meals.size}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = c.textMid
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        meals.forEach { meal ->
            MealCard(
                meal = meal,
                slot = slot,
                accent = accent,
                slotLabel = slot.localizedName(languageViewModel),
                isCompleted = completedMealIds.contains(meal.id),
                onClick = { onMealClick(meal) },
                onToggleCompletion = { onToggleCompletion(meal) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/** Illustrated glass meal card — mesh/gold accent tile + monoline food icon, no photos. */
@Composable
private fun MealCard(
    meal: Meal,
    slot: MealSlot,
    accent: Color,
    slotLabel: String,
    isCompleted: Boolean,
    onClick: () -> Unit,
    onToggleCompletion: () -> Unit
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }

    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressScale(interaction)
                .clickable(interactionSource = interaction, indication = null, onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Illustrated tile: soft accent mesh behind a monoline food icon.
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(accent.copy(alpha = 0.22f), Tajly.Champagne.copy(alpha = 0.12f))
                        )
                    )
                    .border(1.dp, c.hairStrong, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = slot.monoIcon(),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = slotLabel.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accent
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) c.textMid else c.textHi,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(5.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${meal.calories} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textMid
                    )
                    Dot(c.textLow)
                    Text(
                        text = "${meal.protein}g protein",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMid
                    )
                    if (meal.prepTimeMinutes > 0) {
                        Dot(c.textLow)
                        Icon(
                            imageVector = TablerIcons.Clock,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = c.textLow
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "${meal.prepTimeMinutes}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textLow
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Clear SQUARE check to mark eaten.
            val checkInteraction = remember { MutableInteractionSource() }
            val checkShape = RoundedCornerShape(9.dp)
            Box(
                modifier = Modifier
                    .pressScale(checkInteraction)
                    .size(34.dp)
                    .clip(checkShape)
                    .then(
                        if (isCompleted) Modifier.background(Tajly.GoldGradient)
                        else Modifier.background(c.glassFill, checkShape).border(1.5.dp, c.hairStrong, checkShape)
                    )
                    .clickable(
                        interactionSource = checkInteraction,
                        indication = null,
                        onClick = onToggleCompletion
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = TablerIcons.Check,
                        contentDescription = "Eaten",
                        tint = OnGold,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}

/** Scanned meals logged via the AI meal-scan — shown per day and counted in the day's fuel. */
@Composable
private fun ScannedMealsSection(scannedMeals: List<ScannedMealEntry>) {
    val c = TajlyTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Tajly.Champagne.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = TablerIcons.Scan,
                    contentDescription = null,
                    tint = Tajly.Champagne,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "Scanned",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = c.textHi,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${scannedMeals.size}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = c.textMid
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        scannedMeals.forEach { scan ->
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Tajly.Champagne.copy(alpha = 0.22f), Tajly.Champagne.copy(alpha = 0.10f))
                                )
                            )
                            .border(1.dp, c.hairStrong, RoundedCornerShape(15.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = TablerIcons.Scan,
                            contentDescription = null,
                            tint = Tajly.Champagne,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SCANNED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Tajly.Champagne
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = scan.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = c.textHi,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(5.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${scan.calories} kcal",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = c.textMid
                            )
                            Dot(c.textLow)
                            Text(
                                text = "${scan.protein}g protein",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textMid
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(3.dp)
            .clip(CircleShape)
            .background(color)
    )
}

/** Slot accent — local mirror of the shared brand accents (theme-independent). */
private fun MealSlot.accentColorLocal(): Color = when (this) {
    MealSlot.BREAKFAST -> Tajly.Coral
    MealSlot.LUNCH -> Tajly.Green
    MealSlot.DINNER -> Tajly.Violet
    MealSlot.SNACK -> Tajly.Pink
}

@Composable
private fun MealLoadingState(
    isGenerating: Boolean,
    languageViewModel: LanguageViewModel
) {
    val c = TajlyTheme.colors
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(modifier = Modifier.padding(horizontal = 40.dp)) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .blur(28.dp)
                            .background(
                                Brush.radialGradient(listOf(Tajly.Champagne.copy(alpha = 0.5f), Color.Transparent))
                            )
                    )
                    if (isGenerating) {
                        Image(
                            painter = painterResource(Res.drawable.ic3d_leaf),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                if (isGenerating) {
                    Spacer(Modifier.height(18.dp))
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = languageViewModel.getString(StringKey.PLAN_MEALS_WITH_AI),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = languageViewModel.getString(StringKey.PLAN_MEALS_WITH_AI_DESC),
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMid
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMealPlanState(
    languageViewModel: LanguageViewModel,
    onCreateClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        EmptyState(
            title = languageViewModel.getString(StringKey.PLAN_MEALS_WITH_AI),
            subtitle = languageViewModel.getString(StringKey.PLAN_MEALS_WITH_AI_DESC),
            icon = {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .blur(34.dp)
                            .background(
                                Brush.radialGradient(listOf(Tajly.Champagne.copy(alpha = 0.5f), Color.Transparent))
                            )
                    )
                    Image(
                        painter = painterResource(Res.drawable.ic3d_leaf),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )
                }
            },
            cta = {
                GoldButton(
                    text = languageViewModel.getString(StringKey.CREATE_MY_PLAN),
                    onClick = onCreateClick
                )
            }
        )
    }
}

@Composable
private fun DaySelector(
    selectedDay: Int,
    todayDay: Int,
    onDaySelected: (Int) -> Unit
) {
    val c = TajlyTheme.colors
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val today = org.awi.fitness.utils.todayLocalDate()
    val todayDayOfWeek = today.dayOfWeek.ordinal + 1

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        dayLabels.forEachIndexed { index, label ->
            val day = index + 1
            val isSelected = day == selectedDay
            val isToday = day == todayDay

            val diff = day - todayDayOfWeek
            val date = today.plus(DatePeriod(days = diff))
            val dateNumber = date.dayOfMonth

            val interaction = remember { MutableInteractionSource() }

            // Slim gold underline tab: weekday + date, gold text + underline when selected,
            // a small gold dot marks today.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .pressScale(interaction)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = interaction,
                        indication = null
                    ) { onDaySelected(day) }
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Tajly.Champagne else c.textLow
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$dateNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = when {
                        isSelected -> c.textHi
                        isToday -> Tajly.Champagne
                        else -> c.textMid
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .width(if (isSelected) 20.dp else if (isToday) 6.dp else 0.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .then(
                            if (isSelected) Modifier.background(Tajly.GoldGradient)
                            else if (isToday) Modifier.background(Tajly.Champagne)
                            else Modifier
                        )
                )
            }
        }
    }
}
