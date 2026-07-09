package org.awi.fitness.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.launch
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.navigation.LocalAppNavigation
import org.awi.fitness.navigation.PremiumGate
import org.awi.fitness.navigation.RootRoute
import org.awi.fitness.navigation.evaluatePremiumGate
import org.awi.fitness.repository.SubscriptionRepository
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.ui.components.FitnessPulsingDot
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.SnackbarManager
import org.awi.fitness.ui.components.TajlyDrawOnLogo
import org.awi.fitness.viewmodel.AuthViewModel
import org.awi.fitness.viewmodel.LanguageViewModel

class SplashScreen(
    private val authViewModel: AuthViewModel,
    private val languageViewModel: LanguageViewModel,
    private val snackbarManager: SnackbarManager
) : Screen {

    @Composable
    override fun Content() {
        val appNavigation = LocalAppNavigation.current
        var handwritingComplete by remember { mutableStateOf(false) }
        var showRetry by remember { mutableStateOf(false) }
        var retryMessage by remember { mutableStateOf("") }
        var hasNavigated by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val subscriptionRepository = remember { SubscriptionRepository() }
        val c = TajlyTheme.colors

        // ── Routing logic — unchanged ───────────────────────────────────────────
        fun checkAuthAndNavigate() {
            scope.launch {
                showRetry = false
                try {
                    // Not logged in → the merged onboarding (which includes account creation;
                    //    the "Sign in" link inside it routes existing users to Auth).
                    if (!authViewModel.checkAuthState()) {
                        appNavigation.navigateTo(RootRoute.Onboarding)
                        return@launch
                    }

                    // Logged in → evaluate the premium gate (active / billing-issue / locked, with
                    //    offline grace) — the single source of truth shared with the foreground gate.
                    val userSettings = UserSettings.getInstance()
                    val gate = try {
                        evaluatePremiumGate(userSettings, subscriptionRepository)
                    } catch (e: Exception) {
                        retryMessage = "Could not verify subscription. Check your connection."
                        showRetry = true
                        return@launch
                    }
                    when (gate) {
                        PremiumGate.LOCKED -> appNavigation.navigateTo(RootRoute.Paywall)
                        PremiumGate.BILLING_ISSUE -> appNavigation.navigateTo(RootRoute.FixPayment)
                        PremiumGate.ALLOWED ->
                            if (!userSettings.hasCompletedOnboarding) appNavigation.navigateTo(RootRoute.Onboarding)
                            else appNavigation.navigateTo(RootRoute.Main)
                    }
                } catch (e: Exception) {
                    retryMessage = "Something went wrong. Please try again."
                    showRetry = true
                }
            }
        }

        LaunchedEffect(handwritingComplete, showRetry) {
            if (handwritingComplete && !showRetry && !hasNavigated) {
                hasNavigated = true
                checkAuthAndNavigate()
            }
        }

        // ── Premium visual ──────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
            // ambient gold glow behind the wordmark
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.85f)
                    .height(240.dp)
                    .blur(64.dp)
                    .background(Brush.radialGradient(listOf(GoldPrimary.copy(alpha = 0.18f), Color.Transparent)))
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TajlyDrawOnLogo(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                    onAnimationComplete = { handwritingComplete = true }
                )

                Spacer(modifier = Modifier.height(36.dp))

                if (showRetry) {
                    GlassCard(modifier = Modifier.padding(horizontal = 32.dp)) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = retryMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textMid,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            GoldButton(
                                text = languageViewModel.getString(StringKey.RETRY),
                                onClick = { checkAuthAndNavigate() }
                            )
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(3) {
                            FitnessPulsingDot(color = GoldPrimary)
                        }
                    }
                }
            }
        }
    }
}
