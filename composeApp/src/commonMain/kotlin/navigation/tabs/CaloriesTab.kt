package navigation.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import navigation.BottomBarTab

object CaloriesTab : BottomBarTab() {
    override val icon = Icons.Default.LocalFireDepartment
    override val title = "Calories"
    
    @Composable
    override fun TabContent() {
        Text("Calories Screen")
    }
} 