package org.awi.fitness.navigation.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.awi.fitness.navigation.BottomBarTab

object HomeTab : BottomBarTab() {
    override val icon = Icons.Default.Home
    override val title = "Home"
    
    @Composable
    override fun TabContent() {
        Text("Home Screen")
    }
} 