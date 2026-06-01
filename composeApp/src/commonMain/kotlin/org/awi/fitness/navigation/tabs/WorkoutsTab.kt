package org.awi.fitness.navigation.tabs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import compose.icons.FeatherIcons
import compose.icons.feathericons.Zap
import org.awi.fitness.navigation.BottomBarTab

object WorkoutsTab : BottomBarTab() {
    override val icon = FeatherIcons.Zap
    override val title = "Workouts"
    
    @Composable
    override fun TabContent() {
        Text("Workouts Screen")
    }
} 