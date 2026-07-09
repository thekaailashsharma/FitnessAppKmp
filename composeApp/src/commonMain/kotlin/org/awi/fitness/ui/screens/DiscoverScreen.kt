package org.awi.fitness.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import cafe.adriel.voyager.core.screen.Screen
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.Refresh
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.ic3d_bulb
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.awi.fitness.data.StringKey
import org.awi.fitness.model.Article
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.Motion
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.EmptyState
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GlassChip
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.ui.components.SectionHeader
import org.jetbrains.compose.resources.painterResource

class DiscoverScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = org.awi.fitness.viewmodel.LocalArticleViewModel.current
        val languageViewModel = org.awi.fitness.viewmodel.LocalLanguageViewModel.current
        val state by viewModel.state.collectAsState()
        val uriHandler = LocalUriHandler.current
        val c = TajlyTheme.colors

        // Local, UI-only category filter. Does NOT touch data flow — the full
        // article list is always preserved; this only narrows what is shown.
        var selectedFilter by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            viewModel.loadIfNeeded()
        }

        // One-time fade + rise for the header.
        val reveal = remember { Animatable(0f) }
        LaunchedEffect(Unit) { reveal.animateTo(1f, tween(Motion.DurEnter)) }

        AuroraBackground(modifier = Modifier.fillMaxSize()) {
            val filtered = remember(state.articles, selectedFilter) {
                val f = selectedFilter
                if (f == null) state.articles
                else state.articles.filter { categorizeArticle(it) == f }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 56.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                // HEADER — title + refresh affordance.
                item {
                    DiscoverHeader(
                        eyebrow = "Fresh reads".uppercase(),
                        title = languageViewModel.getString(StringKey.DISCOVER),
                        onRefresh = { viewModel.refresh() },
                        modifier = Modifier.graphicsLayer {
                            alpha = reveal.value
                            translationY = (1f - reveal.value) * 24.dp.toPx()
                        },
                    )
                }

                // GLASS FILTER CHIPS — actually narrow the loaded list.
                item {
                    Spacer(Modifier.height(18.dp))
                    FilterChipsRow(
                        selected = selectedFilter,
                        onSelect = { cat ->
                            selectedFilter = if (selectedFilter == cat) null else cat
                        },
                    )
                }

                // ARTICLES
                when {
                    state.isLoading && state.articles.isEmpty() -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(220.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = GoldPrimary)
                            }
                        }
                    }
                    state.articles.isEmpty() && state.hasLoadedOnce -> {
                        item {
                            EmptyState(
                                title = state.error
                                    ?: languageViewModel.getString(StringKey.NO_ARTICLES_FOUND),
                                subtitle = languageViewModel.getString(StringKey.REFRESH_TIPS),
                                modifier = Modifier.padding(top = 12.dp),
                                icon = {
                                    Image(
                                        painter = painterResource(Res.drawable.ic3d_bulb),
                                        contentDescription = null,
                                        modifier = Modifier.size(72.dp),
                                    )
                                },
                                cta = {
                                    GlassChip(
                                        text = languageViewModel.getString(StringKey.REFRESH_TIPS),
                                        selected = true,
                                        onClick = { viewModel.refresh() },
                                    )
                                },
                            )
                        }
                    }
                    else -> {
                        item {
                            Spacer(Modifier.height(26.dp))
                            SectionHeader(
                                title = languageViewModel.getString(StringKey.DISCOVER),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                action = {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .clickable { viewModel.refresh() }
                                            .padding(4.dp),
                                    ) {
                                        Icon(
                                            imageVector = TablerIcons.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = GoldPrimary,
                                        )
                                    }
                                },
                            )
                            Spacer(Modifier.height(14.dp))
                        }

                        if (filtered.isEmpty()) {
                            item {
                                Text(
                                    text = languageViewModel.getString(StringKey.NO_ARTICLES_FOUND),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = c.textMid,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 32.dp),
                                )
                            }
                        } else {
                            itemsIndexed(filtered) { index, article ->
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    if (index == 0) {
                                        HeroArticleCard(
                                            article = article,
                                            onClick = {
                                                try { uriHandler.openUri(article.link) } catch (_: Exception) {}
                                            },
                                        )
                                    } else {
                                        CompactArticleCard(
                                            article = article,
                                            onClick = {
                                                try { uriHandler.openUri(article.link) } catch (_: Exception) {}
                                            },
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// HEADER
// ---------------------------------------------------------------------------

@Composable
private fun DiscoverHeader(
    eyebrow: String,
    title: String,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TajlyTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = GoldBright,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = c.textHi,
            )
        }
        // Refresh affordance.
        val interaction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .pressScale(interaction)
                .size(44.dp)
                .clip(CircleShape)
                .background(c.glassFill, CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(GoldPrimary.copy(alpha = 0.30f), Color.Transparent),
                    )
                )
                .clickable(interactionSource = interaction, indication = null) { onRefresh() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = TablerIcons.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = GoldPrimary,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// FILTER CHIPS
// ---------------------------------------------------------------------------

private val DiscoverFilters = listOf("Fitness", "Nutrition", "Wellness", "Weight", "Health")

@Composable
private fun FilterChipsRow(
    selected: String?,
    onSelect: (String) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            GlassChip(
                text = "All",
                selected = selected == null,
                onClick = { selected?.let(onSelect) },
            )
        }
        items(DiscoverFilters) { cat ->
            GlassChip(
                text = cat,
                selected = selected == cat,
                onClick = { onSelect(cat) },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// CATEGORY LOGIC (preserved verbatim)
// ---------------------------------------------------------------------------

private fun categorizeArticle(article: Article): String {
    val text = (article.title + " " + article.description).lowercase()
    return when {
        text.contains("workout") || text.contains("exercise") || text.contains("training") || text.contains("gym") || text.contains("fitness") -> "Fitness"
        text.contains("nutrition") || text.contains("diet") || text.contains("food") || text.contains("eat") || text.contains("meal") || text.contains("voeding") -> "Nutrition"
        text.contains("mental") || text.contains("stress") || text.contains("sleep") || text.contains("mindful") || text.contains("anxiety") -> "Wellness"
        text.contains("weight") || text.contains("fat") || text.contains("lean") || text.contains("obesity") || text.contains("afvallen") -> "Weight"
        text.contains("heart") || text.contains("cardio") || text.contains("blood") || text.contains("diabetes") -> "Health"
        // Default to a neutral bucket instead of miscategorizing everything as "Health".
        else -> "General"
    }
}

private fun categoryColor(category: String): Color {
    return when (category) {
        "Fitness" -> GoldPrimary
        "Nutrition" -> Tajly.Green
        "Wellness" -> Tajly.Violet
        "Weight" -> Tajly.Teal
        "Health" -> Tajly.Pink
        else -> GoldPrimary
    }
}

/**
 * Turns an RSS pubDate into a compact relative time ("just now", "2h ago", "3d ago").
 * Handles the RFC-822 form Google News emits ("Wed, 02 Oct 2024 13:00:00 GMT") and
 * ISO-8601. Falls back to a trimmed raw string if the date can't be parsed.
 */
private fun formatRelativeTime(pubDate: String): String {
    if (pubDate.isBlank()) return ""

    val publishedMillis = parsePubDateMillis(pubDate) ?: return pubDate.take(16)
    val now = org.awi.fitness.utils.currentTimeMillis()
    val diff = now - publishedMillis
    if (diff < 0) return "just now"

    val minutes = diff / 60_000L
    val hours = minutes / 60L
    val days = hours / 24L
    val weeks = days / 7L

    return when {
        minutes < 1L -> "just now"
        minutes < 60L -> "${minutes}m ago"
        hours < 24L -> "${hours}h ago"
        days < 7L -> "${days}d ago"
        weeks < 5L -> "${weeks}w ago"
        else -> "${days / 30L}mo ago"
    }
}

private val rfc822Months = mapOf(
    "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4, "may" to 5, "jun" to 6,
    "jul" to 7, "aug" to 8, "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12
)

private fun parsePubDateMillis(pubDate: String): Long? {
    // Try ISO-8601 first (e.g. "2024-10-02T13:00:00Z").
    runCatching { Instant.parse(pubDate.trim()) }.getOrNull()?.let { return it.toEpochMilliseconds() }

    // RFC-822 / RFC-1123: "Wed, 02 Oct 2024 13:00:00 GMT"
    return runCatching {
        val cleaned = pubDate.trim().substringAfter(",", pubDate.trim()).trim()
        val parts = cleaned.split(Regex("\\s+"))
        if (parts.size < 4) return null
        val day = parts[0].toInt()
        val month = rfc822Months[parts[1].lowercase().take(3)] ?: return null
        val year = parts[2].toInt()
        val timeParts = parts[3].split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
        val second = timeParts.getOrNull(2)?.toIntOrNull() ?: 0
        // RSS times are almost always GMT/UTC; treat them as such.
        LocalDateTime(year, month, day, hour, minute, second)
            .toInstant(TimeZone.UTC)
            .toEpochMilliseconds()
    }.getOrNull()
}

// ---------------------------------------------------------------------------
// ARTICLE CARDS
// ---------------------------------------------------------------------------

@Composable
private fun HeroArticleCard(
    article: Article,
    onClick: () -> Unit,
) {
    val c = TajlyTheme.colors
    val category = categorizeArticle(article)
    val accent = categoryColor(category)
    val interaction = remember { MutableInteractionSource() }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column {
            // Real cover image when available.
            if (!article.imageUrl.isNullOrBlank()) {
                Box {
                    ImagePlaceholder(
                        url = article.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                    )
                    // Bottom scrim so the chip reads cleanly over any image.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                Brush.verticalGradient(
                                    0.55f to Color.Transparent,
                                    1f to Color.Black.copy(alpha = 0.45f),
                                )
                            )
                    )
                    Box(modifier = Modifier.align(Alignment.BottomStart).padding(14.dp)) {
                        CategoryChip(category)
                    }
                }
            } else {
                // Section-tinted glow in the corner for a premium feel.
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .padding(8.dp)
                        .blur(30.dp)
                        .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.40f), Color.Transparent)))
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                if (article.imageUrl.isNullOrBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CategoryChip(category)
                        Icon(
                            imageVector = TablerIcons.ArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = GoldPrimary,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                }

                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                if (article.description.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = article.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textMid,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SourceBadge(article.source)
                    if (article.pubDate.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = formatRelativeTime(article.pubDate),
                            style = MaterialTheme.typography.labelSmall,
                            color = c.textLow,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(
                        imageVector = TablerIcons.ArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = GoldPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactArticleCard(
    article: Article,
    onClick: () -> Unit,
) {
    val c = TajlyTheme.colors
    val category = categorizeArticle(article)
    val interaction = remember { MutableInteractionSource() }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(categoryColor(category)),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textHi,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CategoryChip(category, small = true)
                    SourceBadge(article.source, small = true)
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = TablerIcons.ArrowRight,
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .padding(top = 4.dp),
                tint = c.textLow,
            )
        }
    }
}

@Composable
private fun CategoryChip(category: String, small: Boolean = false) {
    val color = categoryColor(category)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(if (small) 5.dp else 7.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(
                horizontal = if (small) 6.dp else 8.dp,
                vertical = if (small) 2.dp else 3.dp,
            ),
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SourceBadge(source: String, small: Boolean = false) {
    val c = TajlyTheme.colors
    Text(
        text = source,
        style = MaterialTheme.typography.labelSmall,
        color = c.textLow,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
