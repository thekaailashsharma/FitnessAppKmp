package org.awi.fitness.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.hero_dark_1
import fitnessappkmp.composeapp.generated.resources.hero_dark_2
import fitnessappkmp.composeapp.generated.resources.hero_light_1
import fitnessappkmp.composeapp.generated.resources.hero_light_2
import kotlinx.coroutines.delay
import org.awi.fitness.LegalUrls
import org.awi.fitness.data.Language
import org.awi.fitness.data.StringKey
import org.awi.fitness.navigation.LocalAppNavigation
import org.awi.fitness.navigation.RootRoute
import org.awi.fitness.repository.PeriodType
import org.awi.fitness.repository.SubscriptionPlan
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyColors
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.TajlyLogoMark
import org.awi.fitness.ui.components.glassSource
import org.awi.fitness.ui.components.liquidGlass
import org.awi.fitness.ui.localizedString
import org.awi.fitness.utils.openInAppBrowser
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.SubscriptionViewModel
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

// Theme-independent gold accents (identical in light & dark)
private val Gold = GoldPrimary
private val GoldLight = GoldBright

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
        val c = TajlyTheme.colors
        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(100)
            visible = true
        }

        // Navigate on the success message alone — it's authoritative and set only on real
        // success. (Previously this also required isPremium in the SAME snapshot, but the
        // success message auto-clears after 2s, so if isPremium flipped a beat later they
        // never coincided and the success screen was intermittently skipped.)
        LaunchedEffect(state.successMessage) {
            if (state.successMessage == "PURCHASE_SUCCESS" ||
                state.successMessage == "RESTORE_SUCCESS"
            ) {
                appNavigation.navigateTo(RootRoute.PurchaseSuccess)
            }
        }

        ProvideGlass {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(c.bg)
            ) {
                key(currentLanguageCode) {
                    // ── Single-screen layout (no scroll) ──────────────────────
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Cinematic hero with headline
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
                        ) {
                            HeroSection(language, c)
                        }

                        // Plan cards live in the flexible middle, vertically centered
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(600, delayMillis = 220)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val offering = state.offering
                                if (offering != null) {
                                    val annualPlan = offering.annualPlan
                                    val monthlyPlan = offering.monthlyPlan

                                    // Default to annual (best value) until the user picks.
                                    val annualSelected = state.selectedPlan?.identifier == annualPlan?.identifier ||
                                        state.selectedPlan == null
                                    val monthlySelected = state.selectedPlan?.identifier == monthlyPlan?.identifier

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        if (annualPlan != null) {
                                            PlanCard(
                                                plan = annualPlan,
                                                language = language,
                                                colors = c,
                                                isSelected = annualSelected,
                                                strikethroughPrice = monthlyPlan?.priceString,
                                                onClick = { viewModel.selectPlan(annualPlan) }
                                            )
                                        }
                                        if (monthlyPlan != null) {
                                            PlanCard(
                                                plan = monthlyPlan,
                                                language = language,
                                                colors = c,
                                                isSelected = monthlySelected,
                                                strikethroughPrice = null,
                                                onClick = { viewModel.selectPlan(monthlyPlan) }
                                            )
                                        }
                                    }
                                } else {
                                    // Neutral loading / price-unavailable state — no fabricated prices.
                                    PriceUnavailable(language, c, state.isLoading)
                                }

                                state.errorMessage?.let { msg ->
                                    LaunchedEffect(msg) {
                                        delay(3000)
                                        viewModel.clearMessages()
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = when (msg) {
                                            "CANCELLED" -> localizedString(StringKey.PAYWALL_CANCELLED, language)
                                            "RESTORE_FAILED" -> localizedString(StringKey.PAYWALL_RESTORE_FAILED, language)
                                            else -> localizedString(StringKey.PAYWALL_PURCHASE_FAILED, language)
                                        },
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                }

                                state.successMessage?.let { msg ->
                                    LaunchedEffect(msg) {
                                        delay(2000)
                                        viewModel.clearMessages()
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = when (msg) {
                                            "RESTORE_SUCCESS" -> localizedString(StringKey.PAYWALL_RESTORE_SUCCESS, language)
                                            else -> localizedString(StringKey.PAYWALL_PURCHASE_SUCCESS, language)
                                        },
                                        color = Gold,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                }
                            }
                        }

                        // ── Sticky CTA dock (always visible) ──────────────────
                        StickyCta(
                            language = language,
                            colors = c,
                            price = state.selectedPlan?.priceString,
                            isPurchasing = state.isPurchasing,
                            isRestoring = state.isRestoring,
                            onStartTrial = { if (!state.isPurchasing) viewModel.purchaseSelectedPlan() },
                            onRestore = { viewModel.restorePurchases() }
                        )
                    }

                    PaywallLanguageToggle(
                        selectedLanguage = language,
                        onLanguageSelected = { languageViewModel.setLanguage(it) },
                        colors = c,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .zIndex(1f)
                            .statusBarsPadding()
                            .padding(top = 12.dp, end = 16.dp),
                    )
                }
            }
        }
    }
}

// ── Localized copy that must be compliance-correct (14-day) & price-driven ──
// The bundled StringKeys still say "7-day"; App-Store compliance requires 14 days,
// so the trial-specific copy is composed here (currency always comes from the plan).

private fun freeTrialHeadline(language: Language): String =
    if (language == Language.DUTCH) "14 dagen gratis" else "14 days free"

private fun thenBilled(language: Language, price: String?): String {
    val p = price ?: return ""
    return if (language == Language.DUTCH) "daarna $p" else "then $p"
}

private fun ctaLabel(language: Language): String =
    if (language == Language.DUTCH) "Start 14-daagse gratis proefperiode" else "Start 14-Day Free Trial"

private fun perMonthSuffix(language: Language): String =
    if (language == Language.DUTCH) "/mnd" else "/mo"

private fun billedAnnually(language: Language): String =
    if (language == Language.DUTCH) "jaarlijks gefactureerd" else "billed annually"

private fun priceUnavailable(language: Language): String =
    if (language == Language.DUTCH) "Prijzen momenteel niet beschikbaar" else "Prices are currently unavailable"

// COMPLIANCE (Apple Guideline 3.1.2): full auto-renew disclosure shown on the paywall.
private fun autoRenewDisclosure(language: Language): String =
    if (language == Language.DUTCH)
        "Het abonnement wordt automatisch verlengd tenzij het minstens 24 uur voor het einde van de periode wordt geannuleerd. Opzeggen kan altijd in je App Store-instellingen."
    else
        "Subscription automatically renews unless cancelled at least 24 hours before the end of the period. Cancel anytime in your App Store settings."

/**
 * Best-effort monthly-equivalent of an annual price, keeping the plan's own currency.
 * Returns null when the price string can't be parsed (then we simply omit the line).
 */
private fun monthlyEquivalent(annualPrice: String): String? {
    val match = Regex("[0-9]+(?:[.,][0-9]+)?").find(annualPrice) ?: return null
    val numStr = match.value
    val usesComma = numStr.contains(',')
    val value = numStr.replace(',', '.').toDoubleOrNull() ?: return null
    if (value <= 0.0) return null
    val cents = (value / 12.0 * 100).roundToInt()
    val sep = if (usesComma) "," else "."
    val numberOut = "${cents / 100}$sep${(cents % 100).toString().padStart(2, '0')}"
    val symbol = annualPrice.substringBefore(numStr).trim()
    return if (symbol.isNotEmpty()) "$symbol$numberOut" else numberOut
}

@Composable
private fun PaywallLanguageToggle(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    colors: TajlyColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(3.dp),
    ) {
        Language.entries.forEach { language ->
            val isSelected = language == selectedLanguage
            val label = if (language == Language.ENGLISH) "EN" else "NL"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) Gold else Color.Transparent)
                    .clickable { onLanguageSelected(language) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (isSelected) OnGold else colors.textMid,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

// The same bundled hero backdrops the Home screen surface uses (its "home"
// fallbacks are hero_dark_1 / hero_light_1). The paywall cycles through this set
// every 30s with a cross-fade. Kept in commonMain so it builds on Android + iOS.
private val PaywallBackdrops = listOf(
    Res.drawable.hero_dark_1,
    Res.drawable.hero_light_1,
    Res.drawable.hero_dark_2,
    Res.drawable.hero_light_2,
)

@Composable
private fun HeroSection(language: Language, colors: TajlyColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(228.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Auto-rotating cinematic backdrop — advances every 30s through the
            // Home hero set. Uses delay-driven index (not timestamps) so it is
            // deterministic and platform-agnostic.
            var idx by remember { mutableStateOf(0) }
            LaunchedEffect(Unit) {
                while (true) {
                    delay(30_000)
                    idx = (idx + 1) % PaywallBackdrops.size
                }
            }
            // Cinematic backdrop — marked as the glass source so surfaces over it blur it
            Crossfade(
                targetState = idx,
                animationSpec = tween(900),
                modifier = Modifier.fillMaxSize()
            ) { current ->
                Image(
                    painter = painterResource(PaywallBackdrops[current]),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .glassSource()
                )
            }
            // Scrim melting the photo into the app background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                colors.bg.copy(alpha = 0.35f),
                                colors.bg.copy(alpha = 0.85f),
                                colors.bg
                            )
                        )
                    )
            )
            // Brand logo straddling the seam (logo, not the word mark)
            TajlyLogoMark(
                size = 78.dp,
                modifier = Modifier.offset(y = 38.dp)
            )
        }

        Spacer(modifier = Modifier.height(46.dp))

        Text(
            text = localizedString(StringKey.PAYWALL_TITLE, language),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            ),
            color = colors.textHi,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = localizedString(StringKey.PAYWALL_SUBTITLE, language),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMid,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun PriceUnavailable(language: Language, colors: TajlyColors, isLoading: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlass(shape = RoundedCornerShape(24.dp))
            .padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Gold, strokeWidth = 2.5.dp)
        } else {
            Text(
                text = priceUnavailable(language),
                color = colors.textMid,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    language: Language,
    colors: TajlyColors,
    isSelected: Boolean,
    strikethroughPrice: String?,
    onClick: () -> Unit
) {
    val isAnnual = plan.periodType == PeriodType.ANNUAL
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(150)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .liquidGlass(
                shape = RoundedCornerShape(24.dp),
                goldTint = isSelected,
                tier = if (isSelected) org.awi.fitness.ui.components.GlassTier.Hero
                else org.awi.fitness.ui.components.GlassTier.Card
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Gold else colors.hairStrong,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // "Best Value" ribbon for the annual plan
        if (isAnnual) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .clip(RoundedCornerShape(bottomStart = 12.dp, topEnd = 12.dp))
                    .background(Tajly.GoldGradient)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = localizedString(StringKey.PAYWALL_BEST_VALUE, language),
                    color = OnGold,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = localizedString(
                        if (isAnnual) StringKey.PAYWALL_ANNUAL_LABEL else StringKey.PAYWALL_MONTHLY_LABEL,
                        language
                    ),
                    color = colors.textHi,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (isAnnual) {
                    Text(
                        text = localizedString(StringKey.PAYWALL_ANNUAL_SAVINGS, language),
                        color = Gold,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // COMPLIANCE: full billed price is the most prominent element
                Text(
                    text = plan.priceString,
                    color = colors.textHi,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )

                // Secondary: monthly-equivalent + strikethrough of monthly price (annual only)
                if (isAnnual) {
                    val equiv = monthlyEquivalent(plan.priceString)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (equiv != null) {
                            Text(
                                text = "≈ $equiv${perMonthSuffix(language)}",
                                color = colors.textMid,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "  ·  ${billedAnnually(language)}",
                                color = colors.textLow,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                text = billedAnnually(language),
                                color = colors.textMid,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (strikethroughPrice != null) {
                            Text(
                                text = "  $strikethroughPrice",
                                color = colors.textLow,
                                style = MaterialTheme.typography.bodySmall,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                }
            }

            // Radio-style selector (gold-filled when selected)
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Gold else Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) Gold else colors.textMid.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = TablerIcons.Check,
                        contentDescription = null,
                        tint = OnGold,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StickyCta(
    language: Language,
    colors: TajlyColors,
    price: String?,
    isPurchasing: Boolean,
    isRestoring: Boolean,
    onStartTrial: () -> Unit,
    onRestore: () -> Unit,
) {
    // Gentle one allowed micro-anim: a soft breathing gold glow behind the CTA.
    val glow = rememberInfiniteTransition(label = "ctaGlow")
    val glowAlpha by glow.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
        label = "ctaGlowAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(colors.bg.copy(alpha = 0f), colors.bg.copy(alpha = 0.9f), colors.bg)
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 14-day free trial framing, price straight from the plan object
        Text(
            text = buildString {
                append(freeTrialHeadline(language))
                val then = thenBilled(language, price)
                if (then.isNotEmpty()) append(", ").append(then)
            },
            color = colors.textMid,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(56.dp)
                    .alpha(glowAlpha)
                    .blur(28.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(GoldLight)
            )
            GoldButton(
                text = ctaLabel(language),
                onClick = onStartTrial,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isRestoring,
                loading = isPurchasing
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── Legal footer — REQUIRED for App Store review (Guideline 3.1.2). ──
        // Auto-renew disclosure + functional Terms of Use / Privacy Policy links,
        // Restore kept intact. Readable over the paywall backdrop via the dock scrim.
        Text(
            text = autoRenewDisclosure(language),
            color = colors.textLow,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onRestore,
                enabled = !isRestoring && !isPurchasing,
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
            ) {
                if (isRestoring) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = colors.textMid,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = localizedString(StringKey.PAYWALL_RESTORE_PURCHASES, language),
                        color = colors.textMid,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Text("•", color = colors.textLow, fontSize = 8.sp)

            Text(
                text = "Terms of Use",
                color = Gold,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { openInAppBrowser(LegalUrls.TERMS_OF_USE) }
            )

            Text("•", color = colors.textLow, fontSize = 8.sp)

            Text(
                text = "Privacy Policy",
                color = Gold,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { openInAppBrowser(LegalUrls.PRIVACY_POLICY) }
            )
        }
    }
}
