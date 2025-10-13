package org.awi.fitness.navigation.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import org.awi.fitness.navigation.BottomBarTab
import org.awi.fitness.ui.screens.ChallengesScreen

object ChallengesTab : BottomBarTab() {
    override val icon = Icons.Default.FavoriteBorder
    override val title = "Challenges"
    
    @Composable
    override fun TabContent() {
        ChallengesScreen()
    }
}
