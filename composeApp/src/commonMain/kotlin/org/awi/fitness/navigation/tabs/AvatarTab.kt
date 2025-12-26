package org.awi.fitness.navigation.tabs

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.TablerIcons
import compose.icons.tablericons.MessageCircle
import org.awi.fitness.navigation.BottomBarTab
import org.awi.fitness.ui.screens.avatar.AvatarScreen

object AvatarTab : BottomBarTab() {
    override val icon: ImageVector = TablerIcons.MessageCircle
    override val title: String = "Buddy"
    
    @Composable
    override fun TabContent() {
        AvatarScreen().Content()
    }
}
