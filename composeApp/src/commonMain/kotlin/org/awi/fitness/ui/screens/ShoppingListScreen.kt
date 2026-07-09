package org.awi.fitness.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Check
import compose.icons.tablericons.ShoppingCart
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.MealPlan
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GlassTier
import org.awi.fitness.ui.components.LottieAnim
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.StatRing
import org.awi.fitness.ui.components.glassSource
import org.awi.fitness.viewmodel.LanguageViewModel

class ShoppingListScreen(
    private val plan: MealPlan,
    private val languageViewModel: LanguageViewModel
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userSettings = UserSettings.getInstance()
        val checks by userSettings.shoppingChecks.collectAsState()
        val shoppingList = plan.shoppingList
        val c = TajlyTheme.colors

        val allItems = remember(shoppingList) { shoppingList.values.flatten() }
        val totalItems = allItems.size
        val checkedCount = allItems.count { checks.contains(it) }
        val progress = if (totalItems > 0) checkedCount.toFloat() / totalItems else 0f

        ProvideGlass {
            Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
                AuroraBackground(modifier = Modifier.fillMaxSize().glassSource()) {}

                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = languageViewModel.getString(StringKey.SHOPPING_LIST),
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
                    if (shoppingList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyShoppingState()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 28.dp)
                        ) {
                            item {
                                ShoppingProgressCard(
                                    checked = checkedCount,
                                    total = totalItems,
                                    progress = progress
                                )
                            }
                            shoppingList.forEach { (category, items) ->
                                item {
                                    Text(
                                        text = category.uppercase(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldBright,
                                        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                                    )
                                }
                                items(items) { item ->
                                    ShoppingItem(
                                        text = item,
                                        isChecked = checks.contains(item),
                                        onClick = { userSettings.toggleShoppingCheck(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingProgressCard(checked: Int, total: Int, progress: Float) {
    val c = TajlyTheme.colors
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        goldTint = true,
        tier = GlassTier.Hero
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Your list".uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldBright
                )
                Text(
                    text = "$checked of $total gathered",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi
                )
                Text(
                    text = if (total > 0 && checked == total) "All set — you're ready to cook"
                    else "Tap an item to check it off",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid
                )
            }
            StatRing(progress = progress, diameter = 68.dp, strokeWidth = 8.dp) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi
                )
            }
        }
    }
}

@Composable
private fun ShoppingItem(
    text: String,
    isChecked: Boolean,
    onClick: () -> Unit
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    // One-time check "tick" — the box's contents scale in when checked.
    val tick by animateFloatAsState(targetValue = if (isChecked) 1f else 0f, label = "shoppingTick")
    GlassCard(modifier = Modifier.fillMaxWidth().pressScale(interaction)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .androidx_clickable(interaction, onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val boxShape = RoundedCornerShape(8.dp)
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(boxShape)
                    .then(
                        if (isChecked) Modifier.background(Tajly.GoldGradient)
                        else Modifier.border(1.5.dp, c.hairStrong, boxShape)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isChecked) {
                    Icon(
                        imageVector = TablerIcons.Check,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp).scale(tick),
                        tint = OnGold
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isChecked) c.textLow else c.textHi,
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
            )
        }
    }
}

@Composable
private fun EmptyShoppingState() {
    val c = TajlyTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(32.dp)
    ) {
        LottieAnim("lottie_empty_box.json", Modifier.size(110.dp))
        Box(
            modifier = Modifier.size(76.dp).clip(CircleShape).background(GoldBright.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = TablerIcons.ShoppingCart,
                contentDescription = null,
                tint = GoldBright,
                modifier = Modifier.size(38.dp)
            )
        }
        Text(
            text = "No shopping list yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = c.textHi
        )
        Text(
            text = "This plan doesn't have ingredients to gather.",
            style = MaterialTheme.typography.bodyMedium,
            color = c.textMid
        )
    }
}

/** Qualified clickable helper to keep the foundation import explicit at the call site. */
private fun Modifier.androidx_clickable(
    interaction: MutableInteractionSource,
    onClick: () -> Unit
): Modifier = clickable(
    interactionSource = interaction,
    indication = null,
    onClick = onClick
)
