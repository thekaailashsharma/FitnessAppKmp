package org.awi.fitness.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.launch
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.navigation.LocalAppNavigation
import org.awi.fitness.navigation.RootRoute
import org.awi.fitness.repository.SubscriptionRepository
import org.awi.fitness.ui.components.FitnessPulsingDot
import org.awi.fitness.ui.components.SnackbarManager
import org.awi.fitness.ui.components.TajlyHandwritingAnimation
import org.awi.fitness.ui.components.TajlyLogoMark
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
        var startAnimation by remember { mutableStateOf(false) }
        var handwritingComplete by remember { mutableStateOf(false) }
        var showRetry by remember { mutableStateOf(false) }
        var retryMessage by remember { mutableStateOf("") }
        var hasNavigated by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val subscriptionRepository = remember { SubscriptionRepository() }

        val scale by animateFloatAsState(
            targetValue = if (startAnimation) 1.2f else 0.8f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )

        fun checkAuthAndNavigate() {
            scope.launch {
                showRetry = false
                try {
                    // Not logged in → Auth screen
                    if (!authViewModel.checkAuthState()) {
                        appNavigation.navigateTo(RootRoute.Auth)
                        return@launch
                    }

                    // Logged in → check subscription (this is the only gate)
                    val hasPremium = try {
                        subscriptionRepository.checkPremiumAccess()
                    } catch (e: Exception) {
                        retryMessage = "Could not verify subscription. Check your connection."
                        showRetry = true
                        return@launch
                    }

                    val userSettings = UserSettings.getInstance()
                    when {
                        !hasPremium -> appNavigation.navigateTo(RootRoute.Paywall)
                        !userSettings.hasCompletedOnboarding -> appNavigation.navigateTo(RootRoute.Onboarding)
                        else -> appNavigation.navigateTo(RootRoute.Main)
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

        LaunchedEffect(Unit) {
            startAnimation = true
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.scale(scale)) {
                    TajlyLogoMark(size = 96.dp, animated = startAnimation)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (startAnimation) {
                    TajlyHandwritingAnimation(
                        text = languageViewModel.getString(StringKey.APP_NAME),
                        onAnimationComplete = { handwritingComplete = true }
                    )
                } else {
                    Spacer(modifier = Modifier.height(72.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (showRetry) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = retryMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { checkAuthAndNavigate() }) {
                            Text(languageViewModel.getString(StringKey.RETRY))
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(3) {
                            FitnessPulsingDot(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
