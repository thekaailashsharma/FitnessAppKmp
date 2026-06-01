package org.awi.fitness.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.delay
import org.awi.fitness.data.Language
import org.awi.fitness.data.StringKey
import org.awi.fitness.navigation.LocalAppNavigation
import org.awi.fitness.navigation.RootRoute
import org.awi.fitness.repository.PeriodType
import org.awi.fitness.repository.SubscriptionPlan
import org.awi.fitness.ui.localizedString
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.SubscriptionViewModel

private val GreenAccent = org.awi.fitness.theme.GoldPrimary
private val GreenAccentDim = org.awi.fitness.theme.GoldPrimary.copy(alpha = 0.15f)
private val BackgroundDark = Color(0xFF0C0B09)
private val SurfaceDark = Color(0xFF181610)
private val SurfaceElevated = Color(0xFF221F17)
private val TextPrimary = Color(0xFFF5F0E8)
private val TextSecondary = Color(0xFF8E8880)
private val GoldBadge = org.awi.fitness.theme.GoldBright

class PaywallScreen(
    private val languageViewModel: LanguageViewModel,
    private val authViewModel: org.awi.fitness.viewmodel.AuthViewModel? = null
) : Screen {
    @Composable
    override fun Content() {
        val appNavigation = LocalAppNavigation.current
        val viewModel = remember { SubscriptionViewModel() }
        val state by viewModel.state.collectAsState()
        val currentLanguageCode by languageViewModel.currentLanguage.collectAsState()
        val language = Language.entries.find { it.code == currentLanguageCode } ?: Language.ENGLISH
        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(100)
            visible = true
        }

        LaunchedEffect(state.isPremium, state.successMessage) {
            if (state.isPremium && (
                    state.successMessage == "PURCHASE_SUCCESS" ||
                        state.successMessage == "RESTORE_SUCCESS"
                    )
            ) {
                appNavigation.navigateTo(RootRoute.PurchaseSuccess)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
        ) {
            key(currentLanguageCode) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
                    ) {
                        HeroSection(language)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(600, delayMillis = 200))
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                            FeatureItem(
                                icon = TablerIcons.Stars,
                                text = localizedString(StringKey.PAYWALL_FEATURE_AI_BUDDY, language)
                            )
                            FeatureItem(
                                icon = TablerIcons.Users,
                                text = localizedString(StringKey.PAYWALL_FEATURE_COMMUNITY, language)
                            )
                            FeatureItem(
                                icon = TablerIcons.Trophy,
                                text = localizedString(StringKey.PAYWALL_FEATURE_CHALLENGES, language)
                            )
                            FeatureItem(
                                icon = TablerIcons.ChartLine,
                                text = localizedString(StringKey.PAYWALL_FEATURE_PERSONALIZED, language)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(600, delayMillis = 350))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val annualPlan = state.offering?.annualPlan
                                ?: SubscriptionPlan("annual", "Annual Plan", "€79.99", PeriodType.ANNUAL, true)
                            PlanCard(
                                plan = annualPlan,
                                language = language,
                                isSelected = state.selectedPlan?.identifier == annualPlan.identifier,
                                onClick = { viewModel.selectPlan(annualPlan) }
                            )

                            val monthlyPlan = state.offering?.monthlyPlan
                                ?: SubscriptionPlan("monthly", "Monthly Plan", "€9.99", PeriodType.MONTHLY, false)
                            PlanCard(
                                plan = monthlyPlan,
                                language = language,
                                isSelected = state.selectedPlan?.identifier == monthlyPlan.identifier,
                                onClick = { viewModel.selectPlan(monthlyPlan) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedVisibility(visible = visible, enter = fadeIn(tween(600, delayMillis = 400))) {
                        Text(
                            text = buildTrialNote(language, state.selectedPlan?.periodType),
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(600, delayMillis = 450))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = {
                                    if (!state.isPurchasing) {
                                        viewModel.purchaseSelectedPlan()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenAccent,
                                    contentColor = Color.White
                                ),
                                enabled = !state.isPurchasing && !state.isRestoring
                            ) {
                                if (state.isPurchasing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = localizedString(StringKey.PAYWALL_FREE_TRIAL_CTA, language),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = localizedString(StringKey.CANCEL_ANYTIME, language),
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { viewModel.restorePurchases() },
                                    enabled = !state.isRestoring && !state.isPurchasing
                                ) {
                                    if (state.isRestoring) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = TextSecondary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = localizedString(StringKey.PAYWALL_RESTORE_PURCHASES, language),
                                            color = TextSecondary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                Text("•", color = TextSecondary, fontSize = 10.sp)

                                Text(
                                    text = localizedString(StringKey.PAYWALL_TERMS, language),
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.clickable { /* Open terms */ }
                                )
                            }
                        }
                    }

                    state.errorMessage?.let { msg ->
                        LaunchedEffect(msg) {
                            delay(3000)
                            viewModel.clearMessages()
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (msg) {
                                "CANCELLED" -> localizedString(StringKey.PAYWALL_CANCELLED, language)
                                "RESTORE_FAILED" -> localizedString(StringKey.PAYWALL_RESTORE_FAILED, language)
                                else -> localizedString(StringKey.PAYWALL_PURCHASE_FAILED, language)
                            },
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }

                    state.successMessage?.let { msg ->
                        LaunchedEffect(msg) {
                            delay(2000)
                            viewModel.clearMessages()
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (msg) {
                                "RESTORE_SUCCESS" -> localizedString(StringKey.PAYWALL_RESTORE_SUCCESS, language)
                                else -> localizedString(StringKey.PAYWALL_PURCHASE_SUCCESS, language)
                            },
                            color = GreenAccent,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }

            PaywallLanguageToggle(
                selectedLanguage = language,
                onLanguageSelected = { languageViewModel.setLanguage(it) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(1f)
                    .statusBarsPadding()
                    .padding(top = 12.dp, end = 16.dp),
            )
        }
    }
}

private fun buildTrialNote(language: Language, periodType: PeriodType?): String {
    val thenKey = if (periodType == PeriodType.ANNUAL) StringKey.THEN_PER_YEAR else StringKey.THEN_PER_MONTH
    return "${localizedString(StringKey.FREE_TRIAL_DAYS, language)}, ${localizedString(thenKey, language)}"
}

@Composable
private fun PaywallLanguageToggle(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF21262D))
            .border(1.dp, GreenAccent.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(3.dp),
    ) {
        Language.entries.forEach { language ->
            val isSelected = language == selectedLanguage
            val label = if (language == Language.ENGLISH) "EN" else "NL"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) GreenAccent else Color.Transparent)
                    .clickable { onLanguageSelected(language) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else TextSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun HeroSection(language: Language) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2A200A),
                        Color(0xFF1A1408),
                        BackgroundDark
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(GreenAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = TablerIcons.Activity,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = localizedString(StringKey.PAYWALL_TITLE, language),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = localizedString(StringKey.PAYWALL_SUBTITLE, language),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GreenAccentDim),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(150)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) SurfaceElevated else SurfaceDark)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) GreenAccent else Color(0xFF30363D),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected) GreenAccent else Color.Transparent
                            )
                            .border(
                                width = 2.dp,
                                color = if (isSelected) GreenAccent else Color(0xFF30363D),
                                shape = RoundedCornerShape(50)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = TablerIcons.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = localizedString(
                                if (plan.periodType == PeriodType.ANNUAL)
                                    StringKey.PAYWALL_ANNUAL_LABEL
                                else
                                    StringKey.PAYWALL_MONTHLY_LABEL,
                                language
                            ),
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = localizedString(
                                if (plan.periodType == PeriodType.ANNUAL)
                                    StringKey.PAYWALL_ANNUAL_PRICE
                                else
                                    StringKey.PAYWALL_MONTHLY_PRICE,
                                language
                            ),
                            color = GreenAccent,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (plan.isBestValue) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldBadge)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = localizedString(StringKey.PAYWALL_BEST_VALUE, language),
                            color = Color(0xFF1A1200),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (plan.isBestValue) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = TablerIcons.CircleCheck,
                        contentDescription = null,
                        tint = GreenAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = localizedString(StringKey.PAYWALL_ANNUAL_SAVINGS, language),
                        color = GreenAccent,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
