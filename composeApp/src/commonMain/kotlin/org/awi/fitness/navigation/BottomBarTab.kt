package org.awi.fitness.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

abstract class BottomBarTab : Tab {
    abstract val icon: ImageVector
    abstract val title: String
    
    @Composable
    override fun Content() {
        TabContent()
    }
    
    @Composable
    abstract fun TabContent()
    
    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = 0u,
            title = title,
            icon = rememberVectorPainter(icon)
        )
} 