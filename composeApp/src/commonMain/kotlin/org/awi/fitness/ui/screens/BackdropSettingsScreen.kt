package org.awi.fitness.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Check
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.glassSource
import org.awi.fitness.viewmodel.BackdropOption
import org.awi.fitness.viewmodel.HomeBackdropCatalog
import org.awi.fitness.viewmodel.LocalBackdropViewModel
import org.jetbrains.compose.resources.painterResource

/**
 * Backdrop picker — a 2-column grid of live-preview cards. Tapping one persists it as the
 * user's Home backdrop (via [org.awi.fitness.viewmodel.BackdropViewModel.selectHomeBackdrop])
 * and Home updates immediately. When the user hasn't chosen, the theme-aware default is shown
 * as selected so the grid always mirrors what Home is actually rendering.
 */
class BackdropSettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val c = TajlyTheme.colors
        val navigator = LocalNavigator.currentOrThrow
        val backdropViewModel = LocalBackdropViewModel.current

        val selectedId by backdropViewModel.homeBackdropSelection.collectAsState()
        val effectiveId = selectedId ?: backdropViewModel.defaultHomeBackdropId()

        ProvideGlass {
            Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
                AuroraBackground(modifier = Modifier.fillMaxSize().glassSource()) {}

                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Backdrop",
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
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 40.dp)
                    ) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = "Pick the image that greets you on Home.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textMid,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(HomeBackdropCatalog.options, key = { it.id }) { option ->
                            BackdropPreviewCard(
                                option = option,
                                selected = option.id == effectiveId,
                                onClick = { backdropViewModel.selectHomeBackdrop(option.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BackdropPreviewCard(
    option: BackdropOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val c = TajlyTheme.colors
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(shape)
            .clickable(onClick = onClick)
            .then(
                if (selected) {
                    Modifier.border(width = 3.dp, color = GoldPrimary, shape = shape)
                } else {
                    Modifier.border(width = 1.dp, color = c.hairStrong, shape = shape)
                }
            )
    ) {
        Image(
            painter = painterResource(option.res),
            contentDescription = option.label,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom scrim so the label is always legible over any image.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        1.0f to Color(0xFF060606).copy(alpha = 0.72f)
                    )
                )
        )

        Text(
            text = option.label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = TablerIcons.Check,
                    contentDescription = null,
                    tint = OnGold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
