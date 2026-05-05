package org.awi.fitness.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import compose.icons.SimpleIcons
import compose.icons.TablerIcons
import compose.icons.simpleicons.Ifood
import compose.icons.tablericons.*
import org.awi.fitness.ui.screens.home.HomeScreen
import org.awi.fitness.viewmodel.LocalArticleViewModel
import org.awi.fitness.viewmodel.LocalHomeViewModel
import org.awi.fitness.viewmodel.LocalLanguageViewModel
import org.awi.fitness.viewmodel.LocalMealPlanViewModel
import org.awi.fitness.viewmodel.LocalWorkoutViewModel
import org.awi.fitness.viewmodel.ViewModelStore

sealed class BottomNavItem(val route: String) {
    object Home : BottomNavItem("home")
    object Workouts : BottomNavItem("workouts")
    object Meals : BottomNavItem("meals")
    object Discover : BottomNavItem("discover")
}

class MainScreen : Screen {
    @Composable
    override fun Content() {
        var currentRoute by rememberSaveable { mutableStateOf(BottomNavItem.Home.route) }

        CompositionLocalProvider(
            LocalHomeViewModel provides ViewModelStore.home,
            LocalWorkoutViewModel provides ViewModelStore.workout,
            LocalMealPlanViewModel provides ViewModelStore.mealPlan,
            LocalArticleViewModel provides ViewModelStore.article,
            LocalLanguageViewModel provides ViewModelStore.language
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        listOf(
                            BottomNavItem.Home,
                            BottomNavItem.Workouts,
                            BottomNavItem.Meals,
                            BottomNavItem.Discover
                        ).forEach { item ->
                            NavigationBarItem(
                                selected = currentRoute == item.route,
                                onClick = { currentRoute = item.route },
                                icon = {
                                    Icon(
                                        imageVector = when (item) {
                                            BottomNavItem.Home -> TablerIcons.Home
                                            BottomNavItem.Workouts -> TablerIcons.Walk
                                            BottomNavItem.Meals -> SimpleIcons.Ifood
                                            BottomNavItem.Discover -> TablerIcons.World
                                        },
                                        contentDescription = item.route
                                    )
                                },
                                label = {
                                    val langVm = LocalLanguageViewModel.current
                                    Text(text = when (item) {
                                        BottomNavItem.Home -> langVm.getString(org.awi.fitness.data.StringKey.HOME)
                                        BottomNavItem.Workouts -> langVm.getString(org.awi.fitness.data.StringKey.WORKOUTS)
                                        BottomNavItem.Meals -> langVm.getString(org.awi.fitness.data.StringKey.MEALS)
                                        BottomNavItem.Discover -> langVm.getString(org.awi.fitness.data.StringKey.DISCOVER)
                                    })
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                        BottomNavItem.Home.route -> HomeScreen().Content()
                        BottomNavItem.Workouts.route -> WorkoutScreen().Content()
                        BottomNavItem.Meals.route -> MealScreen().Content()
                        BottomNavItem.Discover.route -> DiscoverScreen().Content()
                    }
                }
            }
        }
    }
}
