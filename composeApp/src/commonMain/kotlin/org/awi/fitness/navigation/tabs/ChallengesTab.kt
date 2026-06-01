package org.awi.fitness.navigation.tabs

import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.Trophy
import org.awi.fitness.navigation.BottomBarTab
import org.awi.fitness.ui.screens.ChallengesScreen

object ChallengesTab : BottomBarTab() {
    override val icon = TablerIcons.Trophy
    override val title = "Challenges"
    
    @Composable
    override fun TabContent() {
        ChallengesScreen()
    }
}
