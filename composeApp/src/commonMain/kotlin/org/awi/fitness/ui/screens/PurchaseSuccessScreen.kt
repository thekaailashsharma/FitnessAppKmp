package org.awi.fitness.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import compose.icons.TablerIcons
import compose.icons.tablericons.Stars
import kotlinx.coroutines.delay
import org.awi.fitness.data.Language
import org.awi.fitness.data.StringKey
import org.awi.fitness.navigation.LocalAppNavigation
import org.awi.fitness.ui.components.CelebrationBurst
import org.awi.fitness.ui.components.TajlyDark
import org.awi.fitness.ui.components.TajlyGold
import org.awi.fitness.ui.components.TajlyGreen
import org.awi.fitness.ui.components.TajlyHandwritingAnimation
import org.awi.fitness.ui.components.TajlyLogoMark
import org.awi.fitness.ui.localizedString
import org.awi.fitness.viewmodel.LanguageViewModel

class PurchaseSuccessScreen(
    private val languageViewModel: LanguageViewModel,
) : Screen {
    @Composable
    override fun Content() {
        val appNavigation = LocalAppNavigation.current
        val languageCode by languageViewModel.currentLanguage.collectAsState()
        val language = Language.entries.find { it.code == languageCode } ?: Language.ENGLISH

        var phase by remember { mutableIntStateOf(0) }
        var navigateAway by remember { mutableStateOf(false) }

        val logoScale by animateFloatAsState(
            targetValue = if (phase >= 1) 1f else 0.2f,
            animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
            label = "logoScale"
        )
        val flashAlpha by animateFloatAsState(
            targetValue = if (phase >= 1) 0f else 0.6f,
            animationSpec = tween(400),
            label = "flash"
        )
        val badgeAlpha by animateFloatAsState(
            targetValue = if (phase >= 4) 1f else 0f,
            animationSpec = tween(600),
            label = "badge"
        )
        val subtitleAlpha by animateFloatAsState(
            targetValue = if (phase >= 3) 1f else 0f,
            animationSpec = tween(700),
            label = "subtitle"
        )

        val infinite = rememberInfiniteTransition(label = "pulse")
        val ringScale by infinite.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "ring"
        )

        LaunchedEffect(Unit) {
            delay(100)
            phase = 1
            delay(500)
            phase = 2
            delay(3500)
            phase = 3
            delay(600)
            phase = 4
            delay(1800)
            navigateAway = true
        }

        LaunchedEffect(navigateAway) {
            if (navigateAway) {
                appNavigation.navigateAfterPremium()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF004D32), TajlyDark, Color.Black),
                        radius = 1000f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(flashAlpha)
                    .background(Color.White)
            )

            CelebrationBurst(active = phase >= 1)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(ringScale)
                            .alpha(if (phase >= 1) 0.35f else 0f)
                            .clip(RoundedCornerShape(100.dp))
                            .background(TajlyGreen.copy(alpha = 0.45f))
                    )
                    Box(modifier = Modifier.scale(logoScale)) {
                        TajlyLogoMark(size = 128.dp, animated = phase >= 2)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                if (phase >= 2) {
                    TajlyHandwritingAnimation(text = localizedString(StringKey.APP_NAME, language))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .alpha(badgeAlpha)
                        .clip(RoundedCornerShape(20.dp))
                        .background(TajlyGold.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "✦ PREMIUM UNLOCKED ✦",
                        color = TajlyGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = localizedString(StringKey.PURCHASE_SUCCESS_SUBTITLE, language),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(subtitleAlpha)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.alpha(subtitleAlpha)
                ) {
                    Icon(TablerIcons.Stars, null, tint = TajlyGold, modifier = Modifier.size(18.dp))
                    Text(
                        text = localizedString(StringKey.PURCHASE_SUCCESS_PREPARING, language),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
