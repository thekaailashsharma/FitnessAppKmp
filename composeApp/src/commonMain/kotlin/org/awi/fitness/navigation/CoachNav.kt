package org.awi.fitness.navigation

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Lets the coach chat (pushed on the root navigator, outside MainScreen's tab providers)
 * request a bottom-tab switch. The chat sets a request and pops back to MainScreen, which
 * observes this and switches the tab — a safe way to "open the Train tab" without pushing
 * tab screens that need providers the chat can't see.
 */
object CoachNav {
    /** Tab name to switch to: "HOME" | "TRAIN" | "CHALLENGES" | "COMMUNITY", or null. */
    val requestedTab = MutableStateFlow<String?>(null)

    /** When switching to TRAIN, which sub-tab to show: 0 = Workouts, 1 = Meals. */
    val requestedTrainSubTab = MutableStateFlow<Int?>(null)

    fun open(tab: String) { requestedTab.value = tab }

    /** Open the Train tab on the Workouts sub-tab. */
    fun openWorkouts() { requestedTrainSubTab.value = 0; requestedTab.value = "TRAIN" }

    /** Open the Train tab on the Meals sub-tab. */
    fun openMeals() { requestedTrainSubTab.value = 1; requestedTab.value = "TRAIN" }
}
