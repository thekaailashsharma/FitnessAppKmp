package org.awi.fitness.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.Program
import org.awi.fitness.viewmodel.MealViewModel
import org.awi.fitness.ui.components.MarkdownText

class MealScreen : Screen {
    @Composable
    override fun Content() {
        val userSettings = UserSettings.getInstance()
        val viewModel = remember { MealViewModel() }
        val state by viewModel.state.collectAsState()

        LaunchedEffect(Unit) {
            userSettings.userEmail?.let { email ->
                viewModel.loadMealsByEmail(email)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Fixed Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "Meal Plans",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Personalized meals assigned by your trainer",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    state.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.error ?: "Unknown error occurred",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    state.meals.isEmpty() -> {
                        EmptyMealState()
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(state.meals) { meal ->
                                var expanded by remember { mutableStateOf(false) }
                                MealCard(
                                    meal = meal,
                                    expanded = expanded,
                                    onExpandClick = { expanded = !expanded }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMealState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = FeatherIcons.Coffee,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Meal Plans Yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Your trainer hasn't assigned any meal plans yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun MealCard(
    meal: Program,
    expanded: Boolean,
    onExpandClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpandClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Always visible content
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Meal Type Tag
                    meal.mealType?.let {
                        AssistChip(
                            onClick = { },
                            label = { Text(it) },
                            leadingIcon = {
                                Icon(
                                    FeatherIcons.Coffee,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                leadingIconContentColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    
                    Text(
                        text = meal.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onExpandClick) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Show less" else "Show more",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expandable content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // Description using MarkdownText
                    MarkdownText(
                        markdown = meal.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Dietary Tags
                    if (meal.dietaryTags.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            meal.dietaryTags.take(3).forEach { tag ->
                                AssistChip(
                                    onClick = { },
                                    label = { Text(tag) },
                                    leadingIcon = {
                                        Icon(
                                            FeatherIcons.Tag,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        leadingIconContentColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Ingredients
                    if (meal.ingredients.isNotEmpty()) {
                        Text(
                            text = "Ingredients",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        meal.ingredients.forEach { ingredient ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    FeatherIcons.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = ingredient,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (meal.ingredients.isNotEmpty() && meal.instructions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Instructions
                    if (meal.instructions.isNotEmpty()) {
                        Text(
                            text = "Instructions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        meal.instructions.forEachIndexed { index, instruction ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "${index + 1}.",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = instruction,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 