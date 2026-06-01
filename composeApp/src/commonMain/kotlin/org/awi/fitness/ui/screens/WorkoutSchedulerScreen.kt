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
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.SimpleIcons
import compose.icons.simpleicons.Googlecalendar
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Calendar
import compose.icons.tablericons.CalendarEvent
import compose.icons.tablericons.CalendarOff
import compose.icons.tablericons.CalendarStats
import kotlinx.datetime.*
import org.awi.fitness.data.*
import org.awi.fitness.viewmodel.LanguageViewModel
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
        
        var selectedDate by remember { mutableStateOf(org.awi.fitness.utils.todayLocalDate()) }
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
                        TextButton(
                            onClick = { 
                                selectedDate = org.awi.fitness.utils.todayLocalDate() 
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
                            Icon(TablerIcons.Plus, contentDescription = "Add Workout")
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
                CalendarHeader(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    viewMode = viewMode
                )

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

        if (showAddDialog || selectedSchedule != null) {
            WorkoutScheduleDialog(
                schedule = selectedSchedule,
                selectedDate = selectedDate,
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
                },
                languageViewModel = LanguageViewModel(userSettings.settings)
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
    val today = remember { org.awi.fitness.utils.todayLocalDate() }
    
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

private val DAY_LABELS_SHORT = mapOf(
    DayOfWeek.MONDAY to "Mo",
    DayOfWeek.TUESDAY to "Tu",
    DayOfWeek.WEDNESDAY to "We",
    DayOfWeek.THURSDAY to "Th",
    DayOfWeek.FRIDAY to "Fr",
    DayOfWeek.SATURDAY to "Sa",
    DayOfWeek.SUNDAY to "Su"
)

@Composable
private fun DateCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
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
                text = DAY_LABELS_SHORT[date.dayOfWeek] ?: "",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
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
                    Icon(TablerIcons.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(TablerIcons.Trash, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
private fun WorkoutScheduleDialog(
    schedule: WorkoutSchedule?,
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (WorkoutSchedule) -> Unit,
    languageViewModel: LanguageViewModel
) {
    var title by remember { mutableStateOf(schedule?.title ?: "") }
    var description by remember { mutableStateOf(schedule?.description ?: "") }
    var workoutType by remember { mutableStateOf(schedule?.workoutType ?: WorkoutType.CARDIO) }
    var recurringType by remember { mutableStateOf(schedule?.recurringType ?: RecurringType.NONE) }
    var workoutTypeExpanded by remember { mutableStateOf(false) }
    var recurringTypeExpanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text(if (schedule == null) languageViewModel.getString(StringKey.ADD) else languageViewModel.getString(StringKey.SAVE)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(languageViewModel.getString(StringKey.TITLE)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(languageViewModel.getString(StringKey.DESCRIPTION)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = workoutTypeExpanded,
                    onExpandedChange = { workoutTypeExpanded = it },
                ) {
                    OutlinedTextField(
                        value = languageViewModel.getString(when (workoutType) {
                            WorkoutType.CARDIO -> StringKey.CARDIO
                            WorkoutType.STRENGTH -> StringKey.STRENGTH
                            WorkoutType.FLEXIBILITY -> StringKey.FLEXIBILITY
                            WorkoutType.HIIT -> StringKey.HIIT
                            WorkoutType.YOGA -> StringKey.YOGA
                            WorkoutType.OTHER -> StringKey.OTHER
                        }),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(languageViewModel.getString(StringKey.WORKOUT_TYPE)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = workoutTypeExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = workoutTypeExpanded,
                        onDismissRequest = { workoutTypeExpanded = false }
                    ) {
                        WorkoutType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(languageViewModel.getString(when (type) {
                                        WorkoutType.CARDIO -> StringKey.CARDIO
                                        WorkoutType.STRENGTH -> StringKey.STRENGTH
                                        WorkoutType.FLEXIBILITY -> StringKey.FLEXIBILITY
                                        WorkoutType.HIIT -> StringKey.HIIT
                                        WorkoutType.YOGA -> StringKey.YOGA
                                        WorkoutType.OTHER -> StringKey.OTHER
                                    }))
                                },
                                onClick = {
                                    workoutType = type
                                    workoutTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = recurringTypeExpanded,
                    onExpandedChange = { recurringTypeExpanded = it },
                ) {
                    OutlinedTextField(
                        value = languageViewModel.getString(when (recurringType) {
                            RecurringType.NONE -> StringKey.NONE
                            RecurringType.DAILY -> StringKey.DAILY
                            RecurringType.WEEKLY -> StringKey.WEEKLY
                            RecurringType.MONTHLY -> StringKey.MONTHLY
                        }),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(languageViewModel.getString(StringKey.RECURRING)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recurringTypeExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = recurringTypeExpanded,
                        onDismissRequest = { recurringTypeExpanded = false }
                    ) {
                        RecurringType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(languageViewModel.getString(when (type) {
                                        RecurringType.NONE -> StringKey.NONE
                                        RecurringType.DAILY -> StringKey.DAILY
                                        RecurringType.WEEKLY -> StringKey.WEEKLY
                                        RecurringType.MONTHLY -> StringKey.MONTHLY
                                    }))
                                },
                                onClick = {
                                    recurringType = type
                                    recurringTypeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val now = org.awi.fitness.utils.currentInstant().toLocalDateTime(TimeZone.currentSystemDefault())
                    val scheduledDateTime = LocalDateTime(
                        selectedDate.year, selectedDate.monthNumber, selectedDate.dayOfMonth,
                        now.hour, now.minute, now.second
                    )
                    val scheduledInstant = scheduledDateTime.toInstant(TimeZone.currentSystemDefault())

                    val startMs = if (schedule != null) schedule.startTime else scheduledInstant.toEpochMilliseconds()
                    val endMs = if (schedule != null) schedule.endTime else scheduledInstant.plus(1.hours).toEpochMilliseconds()

                    val newSchedule = WorkoutSchedule(
                        id = schedule?.id ?: Uuid.random().toString(),
                        title = title,
                        description = description,
                        startTime = startMs,
                        endTime = endMs,
                        workoutType = workoutType,
                        recurringType = recurringType,
                        color = schedule?.color ?: generateRandomColor(),
                        isCompleted = schedule?.isCompleted ?: false
                    )
                    onSave(newSchedule)
                }
            ) {
                Text(if (schedule == null) languageViewModel.getString(StringKey.ADD) else languageViewModel.getString(StringKey.SAVE))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(languageViewModel.getString(StringKey.CANCEL))
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
        0xFF1976D2,
        0xFF388E3C,
        0xFFF57C00,
        0xFF7B1FA2,
        0xFFC2185B,
        0xFF00796B,
        0xFF303F9F
    )
    return colors[Random.nextInt(colors.size)]
}
