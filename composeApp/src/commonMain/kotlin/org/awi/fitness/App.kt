package org.awi.fitness

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import kotlinx.coroutines.launch
import org.awi.fitness.data.Language
import org.awi.fitness.data.UserSettings
import org.awi.fitness.data.createSettings
import org.awi.fitness.navigation.AppNavigationController
import org.awi.fitness.navigation.LocalAppNavigation
import org.awi.fitness.navigation.RootRoute
import org.awi.fitness.repository.AuthRepository
import org.awi.fitness.repository.ConfigRepository
import org.awi.fitness.repository.revenueCatLogIn
import androidx.compose.foundation.isSystemInDarkTheme
import org.awi.fitness.theme.FitnessAppTheme
import org.awi.fitness.ui.StatusBarPadding
import org.awi.fitness.ui.components.FitnessSnackbar
import org.awi.fitness.ui.components.rememberSnackbarManager
import org.awi.fitness.ui.screens.AuthScreen
import org.awi.fitness.ui.screens.MainScreen
import org.awi.fitness.ui.screens.PaywallScreen
import org.awi.fitness.ui.screens.OnboardingScreen
import org.awi.fitness.ui.screens.PurchaseSuccessScreen
import org.awi.fitness.ui.screens.SplashScreen
import org.awi.fitness.viewmodel.AuthViewModel
import org.awi.fitness.viewmodel.LanguageViewModel

sealed class BottomNavItem(val route: String) {
    object Calories : BottomNavItem("calories")
    object Workouts : BottomNavItem("workouts")
    object Profile : BottomNavItem("profile")
    object Home : BottomNavItem("home")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun App() {
    val authViewModel = remember { initAuth() }
    val userSettings = remember { UserSettings.getInstance() }
    val currentLanguage = userSettings.language.collectAsState()
    val languageViewModel = remember {
        LanguageViewModel(createSettings()).apply {
            setLanguage(Language.entries.find { it.code == currentLanguage.value } ?: Language.ENGLISH)
        }
    }

    val isDarkThemeState = userSettings.isDarkThemeFlow.collectAsState()
    // null = user has never overridden → follow device system setting
    // true/false = user explicitly chose in ProfileScreen → respect that choice
    val systemDark = isSystemInDarkTheme()
    val isDarkTheme = isDarkThemeState.value ?: systemDark

    val isLoggedIn by userSettings.isLoggedInFlow.collectAsState()
    val scope = rememberCoroutineScope()

    // Initialize RevenueCat on first composition
    LaunchedEffect(Unit) {
        try {
            initRevenueCat(revenueCatApiKey)
        } catch (e: Exception) {
            // RevenueCat init failed — non-fatal
        }
    }

    // React to login state: fetch config and log in to RevenueCat
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            scope.launch {
                try {
                    // Fetch Gemini API key from Firestore
                    ConfigRepository().fetchAndStoreGeminiKey()
                } catch (e: Exception) {
                    // Non-fatal — hardcoded key will be used as fallback
                }

                try {
                    // Log user into RevenueCat for cross-device entitlements
                    val email = userSettings.userEmail
                    if (!email.isNullOrBlank()) {
                        revenueCatLogIn(email)
                    }
                } catch (e: Exception) {
                    // Non-fatal
                }
            }
        }
    }

    LaunchedEffect(currentLanguage) {
        languageViewModel.setLanguage(
            Language.entries.find { it.code == currentLanguage.value } ?: Language.ENGLISH
        )
    }

    val snackbarManager = rememberSnackbarManager(scope)
    val snackbarHostState = remember { SnackbarHostState() }
    val appNavigation = remember { AppNavigationController() }

    FitnessAppTheme(useDarkTheme = isDarkTheme) {
        StatusBarPadding(
            color = MaterialTheme.colorScheme.background,
            darkIcons = !isDarkTheme
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(LocalAppNavigation provides appNavigation) {
                        when (appNavigation.currentRoute) {
                            RootRoute.Splash -> SplashScreen(
                                authViewModel,
                                languageViewModel,
                                snackbarManager
                            ).Content()

                            RootRoute.Auth -> AuthScreen(authViewModel, languageViewModel).Content()

                            RootRoute.Paywall -> PaywallScreen(
                                languageViewModel = languageViewModel,
                                authViewModel = authViewModel
                            ).Content()

                            RootRoute.PurchaseSuccess -> PurchaseSuccessScreen(
                                languageViewModel = languageViewModel
                            ).Content()

                            RootRoute.Onboarding -> OnboardingScreen(
                                languageViewModel = languageViewModel
                            ).Content()

                            RootRoute.Main -> Navigator(MainScreen()) { navigator ->
                                SlideTransition(navigator)
                            }
                        }
                    }
                }

                FitnessSnackbar(
                    snackbarHostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

fun initAuth(): AuthViewModel {
    val authRepository = AuthRepository()
    return AuthViewModel(authRepository)
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
