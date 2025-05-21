package org.awi.fitness.navigation.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.awi.fitness.navigation.BottomBarTab

object ProfileTab : BottomBarTab() {
    override val icon = Icons.Default.Person
    override val title = "Profile"
    
    @Composable
    override fun TabContent() {
        Text("Profile Screen")
    }
} 