package org.awi.fitness.navigation.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.awi.fitness.navigation.BottomBarTab

object CaloriesTab : BottomBarTab() {
    override val icon = Icons.Default.Home
    override val title = "Calories"
    
    @Composable
    override fun TabContent() {
        Text("Calories Screen")
    }
} 