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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.Activity
import kotlinx.coroutines.delay
import org.awi.fitness.data.StringKey
import org.awi.fitness.ui.components.FitnessPulsingDot
import org.awi.fitness.viewmodel.AuthViewModel
import org.awi.fitness.viewmodel.LanguageViewModel

class SplashScreen(
    private val authViewModel: AuthViewModel,
    private val languageViewModel: LanguageViewModel
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var startAnimation by remember { mutableStateOf(false) }
        
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
            delay(2000)
            // Check auth state before navigating
            if (authViewModel.checkAuthState()) {
                navigator.replace(MainScreen())
            } else {
                navigator.replace(AuthScreen(authViewModel, languageViewModel))
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Animated Logo
                    Icon(
                        imageVector = TablerIcons.Activity,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .scale(scale),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Animated Text
                    Text(
                        text = languageViewModel.getString(StringKey.APP_NAME),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.graphicsLayer(alpha = alpha)
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // Loading Indicator
                    FitnessPulsingDot(
                        color = MaterialTheme.colorScheme.primary,
                        size = 16f
                    )
                }
            }
        }
    }
} 