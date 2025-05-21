package org.awi.fitness.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.awi.fitness.navigation.BottomNavItem
import org.awi.fitness.theme.*

@Composable
fun BottomBar(
    currentRoute: String,
    onNavigate: (BottomNavItem) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark),
        color = BackgroundDark,
        elevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                BottomNavItem.Home,
                BottomNavItem.Workouts,
                BottomNavItem.Calories,
                BottomNavItem.Profile
            ).forEach { item ->
                BottomNavButton(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = { onNavigate(item) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavButton(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    if (isSelected) YellowAccent.copy(alpha = 0.1f)
                    else BackgroundDark
                )
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = if (isSelected) YellowAccent else DarkGray,
                modifier = Modifier.size(24.dp)
            )
        }
        
        AnimatedVisibility(visible = isSelected) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.caption,
                color = if (isSelected) YellowAccent else DarkGray
            )
        }
    }
} 