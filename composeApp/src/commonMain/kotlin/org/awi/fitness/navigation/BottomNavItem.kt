package org.awi.fitness.navigation

import compose.icons.TablerIcons
import compose.icons.tablericons.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val title: String
) {
    data object Home : BottomNavItem("home", TablerIcons.Home, "Home")
    data object Workouts : BottomNavItem("workouts", TablerIcons.Activity, "Workouts")
    data object Community : BottomNavItem("community", TablerIcons.Users, "Community")
    data object Calories : BottomNavItem("calories", TablerIcons.Calculator, "Calories")
    data object Profile : BottomNavItem("profile", TablerIcons.User, "Profile")
    
    // Buddy is not in the bottom nav anymore, but we keep the route for navigation
    data object Buddy : BottomNavItem("buddy", TablerIcons.MessageCircle, "Buddy")
} 