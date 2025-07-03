package org.awi.fitness.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.awi.fitness.data.StringKey
import org.awi.fitness.viewmodel.LanguageViewModel

@Composable
fun WeekDaysSelector(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    languageViewModel: LanguageViewModel
) {
    val days = listOf(
        StringKey.MONDAY,
        StringKey.TUESDAY,
        StringKey.WEDNESDAY,
        StringKey.THURSDAY,
        StringKey.FRIDAY,
        StringKey.SATURDAY,
        StringKey.SUNDAY
    )
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEachIndexed { index, day ->
            val isSelected = selectedDay == index + 1
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onDaySelected(index + 1) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = languageViewModel.getString(day),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimary
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 