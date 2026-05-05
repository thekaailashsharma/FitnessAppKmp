package org.awi.fitness.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.Activity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.awi.fitness.DeepLinkState
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.repository.AuthRepository
import org.awi.fitness.ui.components.FitnessPulsingDot
import org.awi.fitness.ui.components.SnackbarManager
import org.awi.fitness.utils.DateUtils
import org.awi.fitness.viewmodel.AuthViewModel
import org.awi.fitness.viewmodel.LanguageViewModel

class SplashScreen(
    private val authViewModel: AuthViewModel,
    private val languageViewModel: LanguageViewModel,
    private val snackbarManager: SnackbarManager
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var startAnimation by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val authRepository = remember { AuthRepository() }
        
        // Logo scale animation
        val scale by animateFloatAsState(
            targetValue = if (startAnimation) 1.2f else 0.8f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        )
        
        // Text fade animation
        val alpha by animateFloatAsState(
            targetValue = if (startAnimation) 1f else 0f,
            animationSpec = tween(1000, easing = LinearEasing)
        )

        LaunchedEffect(Unit) {
            startAnimation = true
            delay(1000) // Initial animation delay
            
            // Check auth and client status
            if (authViewModel.checkAuthState()) {
                scope.launch {
                    try {
                        val userSettings = UserSettings.getInstance()
                        val email = userSettings.userEmail ?: ""
                        val result = authRepository.getClientByEmail(email)
                        
                        result.fold(
                            onSuccess = { client ->
                                when {
                                    client == null -> {
                                        authViewModel.logout()
                                        snackbarManager.showMessage("You were logged out")
                                        navigator.replace(AuthScreen(authViewModel, languageViewModel))
                                    }
                                    !DateUtils.isDateValid(client.endDate) -> {
                                        authViewModel.logout()
                                        snackbarManager.showMessage("Your plan has expired")
                                        navigator.replace(AuthScreen(authViewModel, languageViewModel))
                                    }
                                    else -> {
                                        navigator.replace(MainScreen())
                                    }
                                }
                            },
                            onFailure = {
                                navigator.replace(MainScreen())
                            }
                        )
                    } catch (e: Exception) {
                        navigator.replace(MainScreen())
                    }
                }
            } else {
                val deepLinkEmail = DeepLinkState.consumeEmail()
                navigator.replace(AuthScreen(authViewModel, languageViewModel, prefillEmail = deepLinkEmail))
            }
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
                Icon(
                    imageVector = TablerIcons.Activity,
                    contentDescription = null,
                    modifier = Modifier
                        .size(96.dp)
                        .scale(scale),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = languageViewModel.getString(StringKey.APP_NAME),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.graphicsLayer { this.alpha = alpha },
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(3) { index ->
                        FitnessPulsingDot(
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
} 