package org.awi.fitness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertCircle
import kotlinx.coroutines.launch
import org.awi.fitness.LegalUrls
import org.awi.fitness.data.UserSettings
import org.awi.fitness.getPlatform
import org.awi.fitness.navigation.LocalAppNavigation
import org.awi.fitness.navigation.PremiumGate
import org.awi.fitness.navigation.RootRoute
import org.awi.fitness.navigation.evaluatePremiumGate
import org.awi.fitness.repository.AuthRepository
import org.awi.fitness.repository.SubscriptionRepository
import org.awi.fitness.repository.SubscriptionStatus
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.glassSource

/**
 * Restricted screen shown when the subscription is active but the store reported a billing issue
 * (payment retry / grace period). The user cannot reach the app until they fix payment — per the
 * chosen "restrict to a fix-payment screen" policy. Not a paywall (they already paid).
 */
@Composable
fun FixPaymentScreen() {
    val appNav = LocalAppNavigation.current
    val settings = remember { UserSettings.getInstance() }
    val repo = remember { SubscriptionRepository() }
    val scope = rememberCoroutineScope()
    val c = TajlyTheme.colors
    val isAndroid = remember { getPlatform().name.startsWith("Android", ignoreCase = true) }
    val storeManageUrl = if (isAndroid) LegalUrls.PLAY_MANAGE_SUBSCRIPTIONS else LegalUrls.APPLE_MANAGE_SUBSCRIPTIONS

    var status by remember { mutableStateOf<SubscriptionStatus?>(null) }
    var checking by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { status = repo.getSubscriptionStatus() }

    fun recheck() {
        scope.launch {
            checking = true
            message = null
            when (evaluatePremiumGate(settings, repo)) {
                PremiumGate.ALLOWED -> appNav.navigateTo(RootRoute.Main)
                PremiumGate.LOCKED -> appNav.navigateTo(RootRoute.Paywall)
                PremiumGate.BILLING_ISSUE ->
                    message = "Still unresolved — it can take a few minutes after you update your card."
            }
            checking = false
        }
    }

    val storeLabel = status?.storeName ?: if (isAndroid) "Google Play" else "the App Store"

    ProvideGlass {
        Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
            AuroraBackground(modifier = Modifier.fillMaxSize().glassSource()) {}

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(GoldBright.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        TablerIcons.AlertCircle,
                        contentDescription = null,
                        tint = GoldBright,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))
                Text(
                    "Payment problem",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "We couldn't renew your Tajly Premium. Update your payment method in $storeLabel to keep your access.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMid,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(28.dp))
                GoldButton(
                    text = "Update payment method",
                    onClick = { org.awi.fitness.utils.openInAppBrowser(status?.managementUrl ?: storeManageUrl) },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                )

                Spacer(Modifier.height(12.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        FixRow("I've updated it — check again", enabled = !checking) { recheck() }
                        FixRow("Restore purchases", enabled = !checking) {
                            scope.launch {
                                checking = true
                                repo.restorePurchases()
                                recheck()
                            }
                        }
                        FixRow("Log out", enabled = !checking) {
                            AuthRepository().logout()
                            appNav.navigateTo(RootRoute.Onboarding)
                        }
                    }
                }

                if (checking) {
                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicator(color = GoldBright, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                }
                message?.let {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Tajly.Coral,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun FixRow(text: String, enabled: Boolean, onClick: () -> Unit) {
    val c = TajlyTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.androidx_clickableFix(onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (enabled) c.textHi else c.textLow
        )
    }
}

private fun Modifier.androidx_clickableFix(onClick: () -> Unit): Modifier =
    clickable(onClick = onClick)
