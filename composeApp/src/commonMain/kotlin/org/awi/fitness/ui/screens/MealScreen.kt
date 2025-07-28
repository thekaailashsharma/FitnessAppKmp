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
import org.awi.fitness.data.StringKey
import org.awi.fitness.viewmodel.LanguageViewModel
import androidx.compose.ui.graphics.Color
import org.awi.fitness.ui.components.VideoPlayer
import org.awi.fitness.ui.components.VideoList

class MealScreen : Screen {
    @Composable
    override fun Content() {
        val userSettings = UserSettings.getInstance()
        val viewModel = remember { MealViewModel() }
        val state by viewModel.state.collectAsState()
        val languageViewModel = remember { LanguageViewModel(userSettings.settings) }

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
                        text = languageViewModel.getString(StringKey.MEAL_PLANS),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = languageViewModel.getString(StringKey.PERSONALIZED_MEALS),
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
                                text = state.error ?: languageViewModel.getString(StringKey.UNKNOWN_ERROR),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    state.meals.isEmpty() -> {
                        EmptyMealState(languageViewModel)
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
                                    onExpandClick = { expanded = !expanded },
                                    languageViewModel = languageViewModel
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
private fun EmptyMealState(languageViewModel: LanguageViewModel) {
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
            text = languageViewModel.getString(StringKey.NO_MEAL_PLANS),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = languageViewModel.getString(StringKey.NO_MEAL_PLANS_DESC),
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
    onExpandClick: () -> Unit,
    languageViewModel: LanguageViewModel
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                        contentDescription = if (expanded) languageViewModel.getString(StringKey.SHOW_LESS) else languageViewModel.getString(StringKey.SHOW_MORE),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    MarkdownText(
                        markdown = meal.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

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

                    if (meal.ingredients.isNotEmpty()) {
                        Text(
                            text = languageViewModel.getString(StringKey.INGREDIENTS),
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

                    if (meal.instructions.isNotEmpty()) {
                        Text(
                            text = languageViewModel.getString(StringKey.INSTRUCTIONS),
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

                    if (meal.videos.isNotEmpty()) {
                        VideoList(
                            videos = meal.videos,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )
                    }
                }
            }
        }
    }
} 