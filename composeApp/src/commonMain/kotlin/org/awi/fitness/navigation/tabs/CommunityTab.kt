package org.awi.fitness.navigation.tabs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import compose.icons.TablerIcons
import compose.icons.tablericons.Users
import org.awi.fitness.navigation.BottomBarTab
import org.awi.fitness.ui.screens.community.CommunityFeedScreen

class CommunityTab : BottomBarTab() {
    override val icon: ImageVector = TablerIcons.Users
    override val title: String = "Community"
    
    @Composable
    override fun TabContent() {
        val communityFeedScreen = remember { CommunityFeedScreen() }
        Navigator(communityFeedScreen) { navigator ->
            SlideTransition(navigator)
        }
    }
}
