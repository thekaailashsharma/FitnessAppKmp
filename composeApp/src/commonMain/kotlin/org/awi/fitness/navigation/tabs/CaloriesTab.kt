package org.awi.fitness.navigation.tabs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.Flame
import org.awi.fitness.navigation.BottomBarTab

object CaloriesTab : BottomBarTab() {
    override val icon = TablerIcons.Flame
    override val title = "Calories"
    
    @Composable
    override fun TabContent() {
        Text("Calories Screen")
    }
}
