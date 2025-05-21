package org.awi.fitness

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.awi.fitness.navigation.BottomNavItem
import org.awi.fitness.theme.BackgroundDark
import org.awi.fitness.ui.CalorieCalculatorScreen
import org.awi.fitness.ui.components.BottomBar
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    var currentRoute by remember { mutableStateOf(BottomNavItem.Calories.route) }

    MaterialTheme {
        Scaffold(
            backgroundColor = BackgroundDark,
            bottomBar = {
                BottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { item ->
                        currentRoute = item.route
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentRoute) {
                    BottomNavItem.Calories.route -> CalorieCalculatorScreen()
                    // Add other screens here
                    else -> CalorieCalculatorScreen() // Temporary default
                }
            }
        }
    }
}