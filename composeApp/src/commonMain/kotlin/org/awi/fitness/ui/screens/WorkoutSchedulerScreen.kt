package org.awi.fitness.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.SimpleIcons
import compose.icons.TablerIcons
import compose.icons.simpleicons.Googlecalendar
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Calendar
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.CalendarOff
import compose.icons.tablericons.CalendarStats
import kotlinx.datetime.*
import org.awi.fitness.data.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class WorkoutSchedulerScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val userSettings = UserSettings.getInstance()
        val navigator = LocalNavigator.currentOrThrow
        val workoutSchedules by userSettings.workoutSchedules.collectAsState()
        
        var selectedDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }
        var showAddDialog by remember { mutableStateOf(false) }
        var selectedSchedule by remember { mutableStateOf<WorkoutSchedule?>(null) }
        var viewMode by remember { mutableStateOf(ViewMode.WEEK) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Workout Schedule") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = TablerIcons.ArrowLeft,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        // Go to Today Button
                        TextButton(
                            onClick = { 
                                selectedDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date 
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                TablerIcons.CalendarStats,
                                contentDescription = "Go to Today",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Today")
                        }
                        
                        IconButton(onClick = { viewMode = if (viewMode == ViewMode.WEEK) ViewMode.MONTH else ViewMode.WEEK }) {
                            Icon(
                                if (viewMode == ViewMode.WEEK) SimpleIcons.Googlecalendar else TablerIcons.CalendarEvent,
                                contentDescription = "Toggle View"
                            )
                        }
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Workout")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Calendar Header
                CalendarHeader(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    viewMode = viewMode
                )

                // Workout List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val schedules = workoutSchedules.filter {
                        val scheduleDate = Instant.fromEpochMilliseconds(it.startTime)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                        scheduleDate == selectedDate
                    }

                    items(schedules) { schedule ->
                        WorkoutCard(
                            schedule = schedule,
                            onEdit = { selectedSchedule = schedule },
                            onDelete = { userSettings.deleteWorkoutSchedule(schedule.id) }
                        )
                    }
                }
            }
        }

        // Add/Edit Dialog
        if (showAddDialog || selectedSchedule != null) {
            WorkoutScheduleDialog(
                schedule = selectedSchedule,
                onDismiss = {
                    showAddDialog = false
                    selectedSchedule = null
                },
                onSave = { schedule ->
                    if (selectedSchedule != null) {
                        userSettings.updateWorkoutSchedule(schedule)
                    } else {
                        userSettings.addWorkoutSchedule(schedule)
                    }
                    showAddDialog = false
                    selectedSchedule = null
                }
            )
        }
    }
}

@Composable
private fun CalendarHeader(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    viewMode: ViewMode
) {
    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Text(
            text = when (viewMode) {
                ViewMode.WEEK -> "Week of ${selectedDate.month} ${selectedDate.year}"
                ViewMode.MONTH -> "${selectedDate.month} ${selectedDate.year}"
            },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val dates = when (viewMode) {
                ViewMode.WEEK -> {
                    val firstDayOfWeek = selectedDate.minus(DatePeriod(days = selectedDate.dayOfWeek.isoDayNumber - 1))
                    (0..6).map { firstDayOfWeek.plus(DatePeriod(days = it)) }
                }
                ViewMode.MONTH -> {
                    val firstDayOfMonth = LocalDate(selectedDate.year, selectedDate.month, 1)
                    val lastDayOfMonth = when (selectedDate.month) {
                        Month.FEBRUARY -> if (selectedDate.year % 4 == 0 && (selectedDate.year % 100 != 0 || selectedDate.year % 400 == 0)) 29 else 28
                        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
                        else -> 31
                    }
                    (0 until lastDayOfMonth).map { firstDayOfMonth.plus(DatePeriod(days = it)) }
                }
            }

            items(dates) { date ->
                DateCell(
                    date = date,
                    isSelected = date == selectedDate,
                    isToday = date == today,
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

@Composable
private fun DateCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfWeek.name.take(1),
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                ),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun WorkoutCard(
    schedule: WorkoutSchedule,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(schedule.color).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = schedule.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        TablerIcons.Calendar,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatTime(schedule.startTime) + " - " + formatTime(schedule.endTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
private fun WorkoutScheduleDialog(
    schedule: WorkoutSchedule?,
    onDismiss: () -> Unit,
    onSave: (WorkoutSchedule) -> Unit
) {
    var title by remember { mutableStateOf(schedule?.title ?: "") }
    var description by remember { mutableStateOf(schedule?.description ?: "") }
    var workoutType by remember { mutableStateOf(schedule?.workoutType ?: WorkoutType.CARDIO) }
    var recurringType by remember { mutableStateOf(schedule?.recurringType ?: RecurringType.NONE) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (schedule == null) "Add Workout" else "Edit Workout") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = {},
                ) {
                    OutlinedTextField(
                        value = workoutType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Workout Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                        modifier = Modifier.menuAnchor()
                    )
                }

                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = {},
                ) {
                    OutlinedTextField(
                        value = recurringType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Recurring") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                        modifier = Modifier.menuAnchor()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newSchedule = WorkoutSchedule(
                        id = schedule?.id ?: Uuid.random().toString(),
                        title = title,
                        description = description,
                        startTime = Clock.System.now().toEpochMilliseconds(),
                        endTime = Clock.System.now().plus(1.hours).toEpochMilliseconds(),
                        workoutType = workoutType,
                        recurringType = recurringType,
                        color = schedule?.color ?: generateRandomColor(),
                        isCompleted = schedule?.isCompleted ?: false
                    )
                    onSave(newSchedule)
                }
            ) {
                Text(if (schedule == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private enum class ViewMode {
    WEEK, MONTH
}

private fun formatTime(timestamp: Long): String {
    val dateTime = Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}

private fun generateRandomColor(): Long {
    val colors = listOf(
        0xFF1976D2, // Blue
        0xFF388E3C, // Green
        0xFFF57C00, // Orange
        0xFF7B1FA2, // Purple
        0xFFC2185B, // Pink
        0xFF00796B, // Teal
        0xFF303F9F  // Indigo
    )
    return colors[Random.nextInt(colors.size)]
}

private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() } 