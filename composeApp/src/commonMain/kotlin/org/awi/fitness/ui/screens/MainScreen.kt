package org.awi.fitness.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import compose.icons.TablerIcons
import compose.icons.tablericons.Flame
import compose.icons.tablericons.Home
import compose.icons.tablericons.User
import compose.icons.tablericons.Walk
import org.awi.fitness.ui.CalorieCalculatorScreen

sealed class BottomNavItem(val route: String) {
    object Home : BottomNavItem("home")
    object Workouts : BottomNavItem("workouts")
    object Calories : BottomNavItem("calories")
    object Profile : BottomNavItem("profile")
}

class MainScreen : Screen {
    @Composable
    override fun Content() {
        var currentRoute by remember { mutableStateOf(BottomNavItem.Home.route) }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar {
                    listOf(
                        BottomNavItem.Home,
                        BottomNavItem.Workouts,
                        BottomNavItem.Calories,
                        BottomNavItem.Profile
                    ).forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = { currentRoute = item.route },
                            icon = {
                                androidx.compose.material3.Icon(
                                    imageVector = when (item) {
                                        BottomNavItem.Home -> TablerIcons.Home
                                        BottomNavItem.Workouts -> TablerIcons.Walk
                                        BottomNavItem.Calories -> TablerIcons.Flame
                                        BottomNavItem.Profile -> TablerIcons.User
                                    },
                                    contentDescription = item.route
                                )
                            },
                            label = { Text(text = item.route.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
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