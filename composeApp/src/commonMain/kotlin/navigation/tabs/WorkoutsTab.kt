package navigation.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import navigation.BottomBarTab

object WorkoutsTab : BottomBarTab() {
    override val icon = Icons.Default.FitnessCenter
    override val title = "Workouts"
    
    @Composable
    override fun TabContent() {
        Text("Workouts Screen")
    }
} 