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
import compose.icons.SimpleIcons
import compose.icons.TablerIcons
import compose.icons.simpleicons.Ifood
import compose.icons.tablericons.*
import org.awi.fitness.ui.CalorieCalculatorScreen

sealed class BottomNavItem(val route: String) {
    object Home : BottomNavItem("home")
    object Workouts : BottomNavItem("workouts")
    object Schedule : BottomNavItem("schedule")
    object Meals : BottomNavItem("meals")
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
                        BottomNavItem.Schedule,
                        BottomNavItem.Meals,
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
                                        BottomNavItem.Schedule -> TablerIcons.CalendarEvent
                                        BottomNavItem.Meals -> SimpleIcons.Ifood
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
                    BottomNavItem.Home.route -> CalorieCalculatorScreen()
                    BottomNavItem.Workouts.route -> WorkoutScreen().Content()
                    BottomNavItem.Schedule.route -> WorkoutSchedulerScreen().Content()
                    BottomNavItem.Meals.route -> MealScreen().Content()
                    BottomNavItem.Profile.route -> CalorieCalculatorScreen() // Temporary
                }
            }
        }
    }
} 