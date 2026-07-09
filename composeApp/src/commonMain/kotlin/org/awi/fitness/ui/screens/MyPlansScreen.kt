package org.awi.fitness.ui.screens
import org.awi.fitness.viewmodel.LocalLanguageViewModel

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Flame
import compose.icons.tablericons.Leaf
import compose.icons.tablericons.Trash
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.ic3d_leaf
import kotlinx.coroutines.launch
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.MealPlan
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.EmptyState
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GlassTier
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.LottieAnim
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.glassSource
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.MealPlanViewModel
import org.jetbrains.compose.resources.painterResource

class MyPlansScreen(
    private val viewModel: MealPlanViewModel
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val state by viewModel.state.collectAsState()
        val scope = rememberCoroutineScope()
        val userSettings = UserSettings.getInstance()
        val languageViewModel = remember { LanguageViewModel(userSettings.settings) }
        val c = TajlyTheme.colors
        var showSetupSheet by remember { mutableStateOf(false) }
        var planToDelete by remember { mutableStateOf<MealPlan?>(null) }

        ProvideGlass {
            Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
                AuroraBackground(modifier = Modifier.fillMaxSize().glassSource()) {}

                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = languageViewModel.getString(StringKey.MY_PLANS),
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
                    if (state.plans.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyState(
                                title = languageViewModel.getString(StringKey.MY_PLANS),
                                subtitle = languageViewModel.getString(StringKey.CREATE_PLAN_SUBTITLE),
                                icon = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        LottieAnim("lottie_empty_box.json", Modifier.size(120.dp))
                                        Image(
                                            painter = painterResource(Res.drawable.ic3d_leaf),
                                            contentDescription = null,
                                            modifier = Modifier.size(72.dp)
                                        )
                                    }
                                },
                                cta = {
                                    GoldButton(
                                        text = languageViewModel.getString(StringKey.CREATE_NEW_PLAN),
                                        onClick = { showSetupSheet = true }
                                    )
                                }
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
                        ) {
                            item {
                                GeneratePlanCta(onClick = { showSetupSheet = true })
                            }
                            item {
                                Text(
                                    text = languageViewModel.getString(StringKey.MY_PLANS).uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = c.textMid
                                )
                            }
                            itemsIndexed(state.plans, key = { _, p -> p.id }) { index, plan ->
                                PlanCard(
                                    plan = plan,
                                    index = index,
                                    languageViewModel = languageViewModel,
                                    onSetActive = {
                                        scope.launch { viewModel.setActivePlan(plan.id) }
                                    },
                                    onDelete = { planToDelete = plan }
                                )
                            }
                        }
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

        planToDelete?.let { plan ->
            AlertDialog(
                onDismissRequest = { planToDelete = null },
                title = { Text(languageViewModel.getString(StringKey.DELETE_PLAN)) },
                text = { Text(languageViewModel.getString(StringKey.DELETE_PLAN_CONFIRM)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                viewModel.deletePlan(plan.id)
                                planToDelete = null
                            }
                        }
                    ) {
                        Text(
                            languageViewModel.getString(StringKey.CONFIRM),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { planToDelete = null }) {
                        Text(languageViewModel.getString(StringKey.CANCEL))
                    }
                },
                containerColor = c.s1
            )
        }
    }
}

/** Gold hero CTA — the primary way to spin up a fresh AI meal plan. */
@Composable
private fun GeneratePlanCta(onClick: () -> Unit) {
    val lang = LocalLanguageViewModel.current
    val c = TajlyTheme.colors
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        goldTint = true,
        tier = GlassTier.Hero
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Tajly.GoldGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(TablerIcons.Leaf, contentDescription = null, tint = OnGold, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lang.getString(StringKey.MEALX_GENERATE_NEW_PLAN),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = lang.getString(StringKey.MEALX_FRESH_MEALS),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid
                )
            }
            Spacer(Modifier.width(12.dp))
            GoldButton(text = lang.getString(StringKey.MEALX_GENERATE), onClick = onClick)
        }
    }
}

@Composable
private fun PlanCard(
    plan: MealPlan,
    index: Int,
    languageViewModel: LanguageViewModel,
    onSetActive: () -> Unit,
    onDelete: () -> Unit
) {
    val c = TajlyTheme.colors
    val shape = RoundedCornerShape(20.dp)

    // One-time fade + rise on appearance.
    var appeared by remember(plan.id) { mutableStateOf(false) }
    LaunchedEffect(plan.id) { appeared = true }
    val enter by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 320, delayMillis = index * 55),
        label = "planEnter"
    )
    val interaction = remember { MutableInteractionSource() }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = enter
                translationY = (1f - enter) * 22.dp.toPx()
            }
            .pressScale(interaction)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = { if (!plan.isActive) onSetActive() }
            )
            .then(
                if (plan.isActive) Modifier.border(1.5.dp, Tajly.GoldGradient, shape)
                else Modifier
            ),
        shape = shape
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // ── FOCAL GROUP: icon + name + active state ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Tajly.Green.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ic3d_leaf),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi
                    )
                    if (plan.createdAt.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = plan.createdAt,
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textLow
                        )
                    }
                }

                // Active = gold badge (focal accent); inactive defers to the CTA below.
                if (plan.isActive) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Tajly.GoldGradient)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = languageViewModel.getString(StringKey.ACTIVE),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnGold
                        )
                    }
                }
            }

            // ── META CHIPS: calories + dietary type ──
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetaChip(
                    icon = TablerIcons.Flame,
                    text = "${plan.targetCalories} kcal",
                    tint = GoldBright
                )
                MetaChip(
                    icon = TablerIcons.Leaf,
                    text = plan.dietaryType.displayName,
                    tint = Tajly.Green
                )
            }

            // ── PRIMARY ACTION (inactive only) ──
            if (!plan.isActive) {
                Spacer(modifier = Modifier.height(16.dp))
                GoldButton(
                    text = languageViewModel.getString(StringKey.SET_ACTIVE),
                    onClick = onSetActive,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── DEMOTED: destructive action, low emphasis at the foot ──
            Spacer(modifier = Modifier.height(if (plan.isActive) 10.dp else 8.dp))
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onDelete)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    TablerIcons.Trash,
                    contentDescription = languageViewModel.getString(StringKey.DELETE_PLAN),
                    tint = c.textLow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = languageViewModel.getString(StringKey.DELETE_PLAN),
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textLow
                )
            }
        }
    }
}

@Composable
private fun MetaChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color
) {
    val c = TajlyTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(tint.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = c.textHi
        )
    }
}
