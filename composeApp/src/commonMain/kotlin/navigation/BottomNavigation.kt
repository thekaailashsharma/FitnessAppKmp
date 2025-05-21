package navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import navigation.tabs.CaloriesTab
import navigation.tabs.HomeTab
import navigation.tabs.ProfileTab
import navigation.tabs.WorkoutsTab

@Composable
fun BottomNavigation() {
    TabNavigator(HomeTab) {
        Column {
            CurrentTab()
            NavigationBar(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth()
            ) {
                TabNavigationItem(HomeTab)
                TabNavigationItem(WorkoutsTab)
                TabNavigationItem(CaloriesTab)
                TabNavigationItem(ProfileTab)
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    
    NavigationBarItem(
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab },
        icon = { tab.options.icon?.let { Icon(painter = it, contentDescription = tab.options.title) } },
        label = { Text(tab.options.title) }
    )
} 