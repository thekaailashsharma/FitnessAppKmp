package org.awi.fitness.navigation.tabs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import compose.icons.TablerIcons
import compose.icons.tablericons.User
import org.awi.fitness.navigation.BottomBarTab

object ProfileTab : BottomBarTab() {
    override val icon = TablerIcons.User
    override val title = "Profile"
    
    @Composable
    override fun TabContent() {
        Text("Profile Screen")
    }
}
