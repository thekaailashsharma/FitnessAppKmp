package org.awi.fitness

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import kotlinx.coroutines.launch
import org.awi.fitness.data.Language
import org.awi.fitness.data.UserSettings
import org.awi.fitness.data.createSettings
import org.awi.fitness.repository.AuthRepository
import org.awi.fitness.repository.ClientRepository
import org.awi.fitness.repository.ConfigRepository
import org.awi.fitness.theme.FitnessAppTheme
import org.awi.fitness.ui.StatusBarPadding
import org.awi.fitness.ui.components.FitnessSnackbar
import org.awi.fitness.ui.components.SnackbarManager
import org.awi.fitness.ui.components.rememberSnackbarManager
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

    // Use remember for theme state
    val isSystemInDarkTheme = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(isSystemInDarkTheme) }

    // Effect to handle language changes
    LaunchedEffect(currentLanguage) {
        languageViewModel.setLanguage(
            Language.entries.find { it.code == currentLanguage.value } ?: Language.ENGLISH
        )
    }

    // Snackbar setup
    val scope = rememberCoroutineScope()

    // Sync FCM token, timezone, lastActiveAt to Firestore
    LaunchedEffect(Unit) {
        println("[AppConfig] App LaunchedEffect: isLoggedIn=${userSettings.isLoggedIn}")
        if (userSettings.isLoggedIn) {
            // Fetch remote config first — runs independently, not affected by FCM sync
            try {
                ConfigRepository().fetchAndApplyConfig()
            } catch (_: Exception) { }

            // Sync FCM token, timezone to Firestore
            try {
                val email = userSettings.userEmail ?: return@LaunchedEffect
                val repo = ClientRepository()
                val client = repo.getClientByEmail(email).getOrNull() ?: return@LaunchedEffect
                val fcmToken = getFcmToken()
                val timezone = getDeviceTimezone()
                val lang = currentLanguage.value ?: "en"
                repo.updateNotificationFields(
                    clientId = client.id,
                    fcmToken = fcmToken,
                    timezone = timezone,
                    language = lang
                )
            } catch (_: Exception) { }
        }
    }
    val snackbarManager = rememberSnackbarManager(scope)
    val snackbarHostState = remember { SnackbarHostState() }

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
                    Navigator(SplashScreen(authViewModel, languageViewModel, snackbarManager)) { navigator ->
                        SlideTransition(navigator)
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