package org.awi.fitness.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.awi.fitness.data.UserSettings

enum class RootRoute {
    Splash,
    Auth,
    Paywall,
    PurchaseSuccess,
    Onboarding,
    Main,
    FixPayment,
}

class AppNavigationController(initial: RootRoute = RootRoute.Splash) {
    var currentRoute by mutableStateOf(initial)
        private set

    fun navigateTo(route: RootRoute) {
        currentRoute = route
    }

    fun navigateAfterPremium() {
        val settings = UserSettings.getInstance()
        navigateTo(
            if (settings.hasCompletedOnboarding) RootRoute.Main else RootRoute.Onboarding
        )
    }
}

val LocalAppNavigation = compositionLocalOf<AppNavigationController> {
    error("AppNavigationController not provided")
}
