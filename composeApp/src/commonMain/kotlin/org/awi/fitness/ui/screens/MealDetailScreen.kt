package org.awi.fitness.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Check
import compose.icons.tablericons.Circle
import compose.icons.tablericons.Clock
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.Trash
import kotlinx.coroutines.launch
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.Meal
import org.awi.fitness.ui.components.AddEditMealSheet
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.SectionHeader
import org.awi.fitness.ui.components.accentColor
import org.awi.fitness.ui.components.icon3d
import org.awi.fitness.ui.components.localizedName
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.jetbrains.compose.resources.painterResource
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.MealPlanViewModel

class MealDetailScreen(
    private val mealId: String,
    private val viewModel: MealPlanViewModel,
    private val languageViewModel: LanguageViewModel
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val state by viewModel.state.collectAsState()
        val scope = rememberCoroutineScope()
        val dateString = state.selectedDateString
        val userSettings = UserSettings.getInstance()
        val completions by userSettings.mealCompletions.collectAsState()
        val c = TajlyTheme.colors

        val meal = remember(state.activePlan, mealId) {
            state.activePlan?.meals?.find { it.id == mealId }
        }

        if (meal == null) {
            Box(
                modifier = Modifier.fillMaxSize().background(c.bg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return
        }

        val accent = meal.mealSlot.accentColor
        val isCompleted = completions[dateString]?.contains(meal.id) == true
        // Ingredient checks are persisted (survive navigation) by reusing the existing
        // shopping-checks map with a per-meal composite key — no new UserSettings keys.
        val shoppingChecks by userSettings.shoppingChecks.collectAsState()
        var showNotTodayDialog by remember { mutableStateOf(false) }
        var showEditSheet by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showSwapDialog by remember { mutableStateOf(false) }

        AuroraBackground(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = meal.mealSlot.localizedName(languageViewModel),
                            style = MaterialTheme.typography.titleLarge,
                            color = c.textHi,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(TablerIcons.ArrowLeft, contentDescription = null, tint = c.textHi)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showEditSheet = true }) {
                            Icon(
                                TablerIcons.Edit,
                                contentDescription = languageViewModel.getString(StringKey.EDIT),
                                tint = c.textHi
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                TablerIcons.Trash,
                                contentDescription = languageViewModel.getString(StringKey.DELETE),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            bottomBar = {
                // Primary actions always on-screen — soft scrim keeps them legible over aurora.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.35f to c.bg.copy(alpha = 0.85f),
                                1f to c.bg
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GoldButton(
                        text = if (isCompleted) languageViewModel.getString(StringKey.COMPLETED)
                        else languageViewModel.getString(StringKey.MARK_AS_EATEN),
                        onClick = {
                            if (!viewModel.isSelectedDayToday()) {
                                showNotTodayDialog = true
                            } else {
                                viewModel.toggleMealCompletion(meal.id, dateString)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    GlassCard(shape = RoundedCornerShape(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (state.isSwapping) Modifier
                                    else Modifier.clickable { showSwapDialog = true }
                                )
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.isSwapping) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    languageViewModel.getString(StringKey.SWAPPING_MEAL),
                                    color = c.textHi,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Icon(
                                    TablerIcons.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = c.textHi
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    languageViewModel.getString(StringKey.SWAP_THIS_MEAL),
                                    color = c.textHi,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                // ── FOCAL HERO: 3D slot icon + name + prep + headline calories ──
                // Calories is the single hero number; P/C/F grouped below as secondary.
                item {
                    // One-time fade + rise reveal for the focal hero.
                    val reveal = remember { Animatable(0f) }
                    LaunchedEffect(Unit) { reveal.animateTo(1f, tween(500)) }
                    GlassCard(
                        modifier = Modifier.graphicsLayer {
                            alpha = reveal.value
                            translationY = (1f - reveal.value) * 24f
                        },
                        goldTint = true
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(accent.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(meal.mealSlot.icon3d),
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = meal.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = c.textHi
                                    )
                                    if (meal.prepTimeMinutes > 0) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                TablerIcons.Clock,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = c.textMid
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "${meal.prepTimeMinutes} ${languageViewModel.getString(StringKey.MIN_SHORT)} ${languageViewModel.getString(StringKey.PREP_TIME)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = c.textMid
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Headline calorie number — the hero stat (big tabular).
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${meal.calories}",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = c.textHi
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "kcal",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = c.textMid,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Secondary macros — grouped inline, demoted under the hero number.
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MacroInline("${meal.protein}g", "protein", Tajly.Blue, Modifier.weight(1f))
                                MacroInline("${meal.carbs}g", "carbs", Tajly.Green, Modifier.weight(1f))
                                MacroInline("${meal.fat}g", "fat", Tajly.Violet, Modifier.weight(1f))
                            }

                            // Dietary tags — grouped within the hero block.
                            if (meal.dietaryTags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    meal.dietaryTags.forEach { tag ->
                                        // Display-only chips (informational tags, not interactive).
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(c.glassFill, RoundedCornerShape(12.dp))
                                                .border(1.dp, c.hairStrong, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                        ) {
                                            Text(
                                                text = tag,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = c.textMid
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (meal.ingredients.isNotEmpty()) {
                    item {
                        SectionHeader(title = languageViewModel.getString(StringKey.INGREDIENTS))
                    }

                    itemsIndexed(meal.ingredients) { index, ingredient ->
                        val ingredientKey = "meal:${meal.id}:$index"
                        val checked = shoppingChecks.contains(ingredientKey)
                        GlassCard(shape = RoundedCornerShape(14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        userSettings.toggleShoppingCheck(ingredientKey)
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val boxShape = RoundedCornerShape(8.dp)
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(boxShape)
                                        .then(
                                            if (checked) Modifier.background(Tajly.GoldGradient)
                                            else Modifier.border(1.5.dp, c.hairStrong, boxShape)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (checked) {
                                        Icon(
                                            imageVector = TablerIcons.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                            tint = OnGold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = ingredient,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (checked) c.textLow else c.textHi,
                                    textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
                                )
                            }
                        }
                    }
                }

                if (meal.instructions.isNotEmpty()) {
                    item {
                        SectionHeader(title = languageViewModel.getString(StringKey.INSTRUCTIONS))
                    }

                    itemsIndexed(meal.instructions) { index, instruction ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(accent.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    color = accent,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            Text(
                                text = instruction,
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textHi,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        }

        if (showEditSheet) {
            AddEditMealSheet(
                existingMeal = meal,
                dayOfWeek = meal.dayOfWeek,
                languageViewModel = languageViewModel,
                onDismiss = { showEditSheet = false },
                onSave = { updatedMeal ->
                    scope.launch {
                        viewModel.updateMeal(updatedMeal)
                        showEditSheet = false
                    }
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(languageViewModel.getString(StringKey.DELETE_MEAL)) },
                text = { Text(languageViewModel.getString(StringKey.DELETE_MEAL_CONFIRM)) },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            viewModel.removeMeal(meal.id)
                            showDeleteDialog = false
                            navigator.pop()
                        }
                    }) {
                        Text(
                            languageViewModel.getString(StringKey.DELETE),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(languageViewModel.getString(StringKey.CANCEL))
                    }
                },
                containerColor = c.s1
            )
        }

        if (showSwapDialog) {
            AlertDialog(
                onDismissRequest = { showSwapDialog = false },
                title = {
                    Text(
                        languageViewModel.getString(StringKey.SWAP_CONFIRM_TITLE),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Text(
                        languageViewModel.getString(StringKey.SWAP_CONFIRM_DESC),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showSwapDialog = false
                        scope.launch {
                            viewModel.swapMeal(meal)
                        }
                    }) {
                        Text(
                            languageViewModel.getString(StringKey.SWAP_THIS_MEAL),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSwapDialog = false }) {
                        Text(languageViewModel.getString(StringKey.CANCEL))
                    }
                },
                containerColor = c.s1
            )
        }

        if (showNotTodayDialog) {
            AlertDialog(
                onDismissRequest = { showNotTodayDialog = false },
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
                        showNotTodayDialog = false
                    }) {
                        Text(
                            languageViewModel.getString(StringKey.MARK_ANYWAY),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNotTodayDialog = false }) {
                        Text(languageViewModel.getString(StringKey.CANCEL))
                    }
                },
                containerColor = c.s1
            )
        }
    }
}

/** Secondary macro chip — colored dot + tabular value + label, on subtle glass. */
@Composable
private fun MacroInline(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val c = TajlyTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(c.glassFill, shape)
            .border(1.dp, c.hairStrong, shape)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = c.textHi
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = c.textMid
        )
    }
}
