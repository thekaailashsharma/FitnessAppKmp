package org.awi.fitness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import cafe.adriel.voyager.core.screen.Screen
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.Refresh
import org.awi.fitness.data.StringKey
import org.awi.fitness.model.Article
import org.awi.fitness.viewmodel.LocalArticleViewModel
import org.awi.fitness.viewmodel.LocalLanguageViewModel

class DiscoverScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = LocalArticleViewModel.current
        val languageViewModel = LocalLanguageViewModel.current
        val state by viewModel.state.collectAsState()
        val uriHandler = LocalUriHandler.current

        LaunchedEffect(Unit) {
            viewModel.loadIfNeeded()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = languageViewModel.getString(StringKey.DISCOVER),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(
                                imageVector = TablerIcons.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { paddingValues ->
            when {
                state.isLoading && state.articles.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                state.articles.isEmpty() && state.hasLoadedOnce -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.error ?: languageViewModel.getString(StringKey.NO_ARTICLES_FOUND),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        itemsIndexed(state.articles) { index, article ->
                            if (index == 0) {
                                HeroArticleCard(
                                    article = article,
                                    onClick = {
                                        try { uriHandler.openUri(article.link) } catch (_: Exception) {}
                                    }
                                )
                            } else {
                                CompactArticleCard(
                                    article = article,
                                    onClick = {
                                        try { uriHandler.openUri(article.link) } catch (_: Exception) {}
                                    }
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

private fun categorizeArticle(article: Article): String {
    val text = (article.title + " " + article.description).lowercase()
    return when {
        text.contains("workout") || text.contains("exercise") || text.contains("training") || text.contains("gym") || text.contains("fitness") -> "Fitness"
        text.contains("nutrition") || text.contains("diet") || text.contains("food") || text.contains("eat") || text.contains("meal") || text.contains("voeding") -> "Nutrition"
        text.contains("mental") || text.contains("stress") || text.contains("sleep") || text.contains("mindful") || text.contains("anxiety") -> "Wellness"
        text.contains("weight") || text.contains("fat") || text.contains("lean") || text.contains("obesity") || text.contains("afvallen") -> "Weight"
        text.contains("heart") || text.contains("cardio") || text.contains("blood") || text.contains("diabetes") -> "Health"
        else -> "Health"
    }
}

private fun categoryColor(category: String): Color {
    return when (category) {
        "Fitness" -> org.awi.fitness.theme.GoldPrimary
        "Nutrition" -> Color(0xFFFF9800)
        "Wellness" -> Color(0xFF9C27B0)
        "Weight" -> Color(0xFF2196F3)
        "Health" -> Color(0xFFE91E63)
        else -> Color(0xFF607D8B)
    }
}

private fun formatRelativeTime(pubDate: String): String {
    if (pubDate.isBlank()) return ""
    return pubDate.take(16)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeroArticleCard(
    article: Article,
    onClick: () -> Unit
) {
    val category = categorizeArticle(article)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryChip(category)
                Icon(
                    imageVector = TablerIcons.ArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (article.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = article.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SourceBadge(article.source)
                if (article.pubDate.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatRelativeTime(article.pubDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactArticleCard(
    article: Article,
    onClick: () -> Unit
) {
    val category = categorizeArticle(article)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(categoryColor(category))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryChip(category, small = true)
                    SourceBadge(article.source, small = true)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = TablerIcons.ArrowRight,
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .padding(top = 4.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun CategoryChip(category: String, small: Boolean = false) {
    val color = categoryColor(category)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(if (small) 4.dp else 6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(
                horizontal = if (small) 6.dp else 8.dp,
                vertical = if (small) 2.dp else 3.dp
            )
    ) {
        Text(
            text = category,
            style = if (small) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SourceBadge(source: String, small: Boolean = false) {
    Text(
        text = source,
        style = if (small) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
