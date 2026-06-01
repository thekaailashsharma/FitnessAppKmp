package org.awi.fitness.navigation.tabs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.Home
import org.awi.fitness.navigation.BottomBarTab

object HomeTab : BottomBarTab() {
    override val icon = TablerIcons.Home
    override val title = "Home"
    
    @Composable
    override fun TabContent() {
        Text("Home Screen")
    }
}
