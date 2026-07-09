package org.awi.fitness.ui.screens
import org.awi.fitness.data.StringKey
import org.awi.fitness.viewmodel.LocalLanguageViewModel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.card_fuel
import fitnessappkmp.composeapp.generated.resources.card_hydrate
import fitnessappkmp.composeapp.generated.resources.card_move
import fitnessappkmp.composeapp.generated.resources.card_read
import fitnessappkmp.composeapp.generated.resources.card_sleep
import fitnessappkmp.composeapp.generated.resources.card_yoga
import kotlinx.coroutines.launch
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.viewmodel.DailyTip
import org.awi.fitness.viewmodel.TipIcon
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Instagram-Stories wellness deck. Each tip fills the screen with its own cinematic backdrop;
 * stories AUTO-ADVANCE (~5s each) with animated progress bars, tap the left third to go back and
 * the right two-thirds to skip ahead, and horizontal swipe still works. Ends on an "all caught up"
 * card. Tips are real (shuffled static pool → vary daily, no AI). Backdrop image + text are
 * designed to be swapped for backend-controlled stories next.
 */
class TipDeckScreen(
    private val tips: List<DailyTip>
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val c = TajlyTheme.colors
        val scope = rememberCoroutineScope()
        val totalPages = (tips.size + 1).coerceAtLeast(1)
        val pagerState = rememberPagerState(initialPage = 0, pageCount = { totalPages })
        val progress = remember { Animatable(0f) }

        // Auto-advance the current story, filling its progress bar as it plays.
        LaunchedEffect(pagerState.currentPage) {
            progress.snapTo(0f)
            if (pagerState.currentPage < tips.size) {
                progress.animateTo(1f, tween(5000))
                val next = pagerState.currentPage + 1
                if (next < totalPages) pagerState.animateScrollToPage(next)
            } else {
                progress.snapTo(1f)
            }
        }

        fun go(target: Int) {
            val t = target.coerceIn(0, totalPages - 1)
            if (t != pagerState.currentPage) scope.launch { pagerState.animateScrollToPage(t) }
            else scope.launch { progress.snapTo(0f) } // tap on first → restart timer
        }

        Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                if (page < tips.size) {
                    TipPage(tips[page], page, onPrev = { go(pagerState.currentPage - 1) }, onNext = { go(pagerState.currentPage + 1) })
                } else {
                    CaughtUpPage(c) { navigator.pop() }
                }
            }

            // Segmented story progress bars.
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(totalPages) { i ->
                    val fill = when {
                        i < pagerState.currentPage -> 1f
                        i == pagerState.currentPage -> progress.value
                        else -> 0f
                    }
                    Box(Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.28f))) {
                        Box(Modifier.fillMaxWidth(fill).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(GoldPrimary))
                    }
                }
            }

            Box(
                modifier = Modifier.statusBarsPadding().padding(top = 28.dp, start = 16.dp).size(40.dp)
                    .clip(CircleShape).background(Color.Black.copy(alpha = 0.35f)).clickable { navigator.pop() },
                contentAlignment = Alignment.Center
            ) {
                Icon(TablerIcons.ArrowLeft, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun TipPage(tip: DailyTip, index: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(backdropFor(tip.icon, index)), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0.0f to Color.Black.copy(alpha = 0.28f),
                    0.42f to Color.Black.copy(alpha = 0.12f),
                    0.72f to Color.Black.copy(alpha = 0.62f),
                    1.0f to Color.Black.copy(alpha = 0.9f)
                )
            )
        )
        // Tap zones: left third = back, right two-thirds = next (Instagram-style).
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxHeight().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onPrev))
            Box(Modifier.weight(2f).fillMaxHeight().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onNext))
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp).padding(top = 80.dp, bottom = 56.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(Modifier.clip(RoundedCornerShape(999.dp)).background(Tajly.GoldGradient).padding(horizontal = 14.dp, vertical = 6.dp)) {
                Text(titleFor(tip.icon), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = OnGold)
            }
            Spacer(Modifier.height(16.dp))
            Text(tip.tip, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(20.dp))
            Text(LocalLanguageViewModel.current.getString(StringKey.TIP_TAP_HINT), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun CaughtUpPage(colors: org.awi.fitness.theme.TajlyColors, onDone: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(Modifier.size(88.dp).clip(CircleShape).background(Tajly.GoldGradient), contentAlignment = Alignment.Center) {
                Icon(TablerIcons.Check, null, tint = OnGold, modifier = Modifier.size(44.dp))
            }
            Text(LocalLanguageViewModel.current.getString(StringKey.TIP_CAUGHT_UP), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colors.textHi)
            Text(LocalLanguageViewModel.current.getString(StringKey.TIP_CAUGHT_UP_SUB), style = MaterialTheme.typography.bodyMedium, color = colors.textMid, modifier = Modifier.padding(horizontal = 16.dp))
            Box(Modifier.clip(RoundedCornerShape(16.dp)).background(Tajly.GoldGradient).clickable(onClick = onDone).padding(horizontal = 28.dp, vertical = 14.dp)) {
                Text(LocalLanguageViewModel.current.getString(StringKey.TIP_DONE), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = OnGold)
            }
        }
    }
}

private fun titleFor(icon: TipIcon): String = when (icon) {
    TipIcon.WATER -> "Hydration"
    TipIcon.FOOD -> "Nutrition"
    TipIcon.SLEEP -> "Rest"
    TipIcon.EXERCISE -> "Movement"
    TipIcon.MINDFULNESS -> "Mindfulness"
    TipIcon.HEALTH -> "Wellness"
}

private fun backdropFor(icon: TipIcon, index: Int): DrawableResource = when (icon) {
    TipIcon.WATER -> Res.drawable.card_hydrate
    TipIcon.FOOD -> Res.drawable.card_fuel
    TipIcon.SLEEP -> Res.drawable.card_sleep
    TipIcon.EXERCISE -> Res.drawable.card_move
    TipIcon.MINDFULNESS -> Res.drawable.card_yoga
    TipIcon.HEALTH -> when (index % 3) {
        0 -> Res.drawable.card_read
        1 -> Res.drawable.card_yoga
        else -> Res.drawable.card_move
    }
}
