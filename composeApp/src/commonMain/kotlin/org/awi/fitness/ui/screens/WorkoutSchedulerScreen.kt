package org.awi.fitness.ui.screens
import org.awi.fitness.viewmodel.LocalLanguageViewModel

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.ic3d_calendar
import fitnessappkmp.composeapp.generated.resources.ic3d_gym
import kotlinx.datetime.*
import org.awi.fitness.data.*
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.EmptyState
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GlassChip
import org.awi.fitness.ui.components.GlassTier
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.glassSource
import org.awi.fitness.viewmodel.LanguageViewModel
import org.jetbrains.compose.resources.painterResource
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
        val c = TajlyTheme.colors

        // Dates that have at least one scheduled session — drives the calendar dots.
        val sessionDates = remember(workoutSchedules) {
            workoutSchedules.map {
                Instant.fromEpochMilliseconds(it.startTime)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
            }.toSet()
        }

        val schedules = remember(workoutSchedules, selectedDate) {
            workoutSchedules.filter {
                val scheduleDate = Instant.fromEpochMilliseconds(it.startTime)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                scheduleDate == selectedDate
            }.sortedBy { it.startTime } // light hour-ordered list
        }

        ProvideGlass {
            Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
                AuroraBackground(modifier = Modifier.fillMaxSize().glassSource()) {}

                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            title = { Text(LocalLanguageViewModel.current.getString(StringKey.WORKOUT_SCHEDULE), fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = { navigator.pop() }) {
                                    Icon(
                                        imageVector = TablerIcons.ArrowLeft,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { showAddDialog = true }) {
                                    Icon(TablerIcons.Plus, contentDescription = "Add Workout", tint = GoldPrimary)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                titleContentColor = c.textHi,
                                navigationIconContentColor = c.textHi
                            )
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
                            viewMode = viewMode,
                            onViewModeChange = { viewMode = it },
                            onToday = { selectedDate = org.awi.fitness.utils.todayLocalDate() },
                            sessionDates = sessionDates
                        )

                        if (schedules.isEmpty()) {
                            EmptyState(
                                title = LocalLanguageViewModel.current.getString(StringKey.WKX_NO_WORKOUTS_SCHEDULED),
                                subtitle = LocalLanguageViewModel.current.getString(StringKey.WKX_TAP_TO_PLAN),
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                icon = {
                                    Image(
                                        painter = painterResource(Res.drawable.ic3d_calendar),
                                        contentDescription = null,
                                        modifier = Modifier.size(96.dp)
                                    )
                                },
                                cta = {
                                    GoldButton(
                                        text = LocalLanguageViewModel.current.getString(StringKey.ADD_WORKOUT),
                                        onClick = { showAddDialog = true }
                                    )
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Text(
                                        text = "${selectedDate.prettyDay()} · ${schedules.size} ${if (schedules.size == 1) LocalLanguageViewModel.current.getString(StringKey.WKX_SESSION) else LocalLanguageViewModel.current.getString(StringKey.WKX_SESSIONS)}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = c.textMid
                                    )
                                }
                                itemsIndexed(schedules, key = { _, s -> s.id }) { index, schedule ->
                                    WorkoutCard(
                                        schedule = schedule,
                                        index = index,
                                        onEdit = { selectedSchedule = schedule },
                                        onDelete = { userSettings.deleteWorkoutSchedule(schedule.id) }
                                    )
                                }
                            }
                        }
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
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    onToday: () -> Unit,
    sessionDates: Set<LocalDate>
) {
    val today = remember { org.awi.fitness.utils.todayLocalDate() }
    val c = TajlyTheme.colors

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        goldTint = true,
        tier = GlassTier.Hero
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (viewMode) {
                            ViewMode.WEEK -> LocalLanguageViewModel.current.getString(StringKey.WKX_WEEK_OF)
                            ViewMode.MONTH -> LocalLanguageViewModel.current.getString(StringKey.WKX_MONTH_OF)
                        }.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldBright
                    )
                    Text(
                        text = "${selectedDate.month.pretty()} ${selectedDate.year}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onToday)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        TablerIcons.CalendarStats,
                        contentDescription = "Go to Today",
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        LocalLanguageViewModel.current.getString(StringKey.TODAY),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldPrimary
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassChip(text = LocalLanguageViewModel.current.getString(StringKey.WKX_WEEK), selected = viewMode == ViewMode.WEEK, onClick = { onViewModeChange(ViewMode.WEEK) })
                GlassChip(text = LocalLanguageViewModel.current.getString(StringKey.WKX_MONTH), selected = viewMode == ViewMode.MONTH, onClick = { onViewModeChange(ViewMode.MONTH) })
            }

            Spacer(Modifier.height(14.dp))

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

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(dates) { date ->
                    DateCell(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == today,
                        hasSession = sessionDates.contains(date),
                        onClick = { onDateSelected(date) }
                    )
                }
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
    hasSession: Boolean,
    onClick: () -> Unit
) {
    val c = TajlyTheme.colors
    val contentColor = when {
        isSelected -> OnGold
        isToday -> GoldPrimary
        else -> c.textHi
    }
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .pressScale(interaction)
            .width(46.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .then(
                when {
                    isSelected -> Modifier.background(Tajly.GoldGradient)
                    isToday -> Modifier.background(GoldPrimary.copy(alpha = 0.12f))
                    else -> Modifier.background(c.glassFill, RoundedCornerShape(18.dp))
                }
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = DAY_LABELS_SHORT[date.dayOfWeek] ?: "",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = if (isSelected) OnGold else c.textMid
            )
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor
            )
            // Session/today dot — fixed slot so all cells align.
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isSelected -> OnGold.copy(alpha = if (hasSession) 0.9f else 0f)
                            isToday -> GoldPrimary
                            hasSession -> c.textMid
                            else -> Color.Transparent
                        }
                    )
            )
        }
    }
}

/** Section accent per workout type — colorful identity, gold stays reserved for CTA/time. */
private fun workoutTypeColor(type: WorkoutType): Color = when (type) {
    WorkoutType.CARDIO -> Tajly.Coral
    WorkoutType.STRENGTH -> Tajly.Violet
    WorkoutType.FLEXIBILITY -> Tajly.Teal
    WorkoutType.HIIT -> Tajly.Pink
    WorkoutType.YOGA -> Tajly.Green
    WorkoutType.OTHER -> Tajly.Blue
}

@Composable
private fun WorkoutCard(
    schedule: WorkoutSchedule,
    index: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val c = TajlyTheme.colors
    val accent = workoutTypeColor(schedule.workoutType)

    // One-time fade + rise on appearance (replays when the day's list changes).
    var appeared by remember(schedule.id) { mutableStateOf(false) }
    LaunchedEffect(schedule.id) { appeared = true }
    val enter by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 320, delayMillis = index * 55),
        label = "cardEnter"
    )
    val interaction = remember { MutableInteractionSource() }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = enter
                translationY = (1f - enter) * 22.dp.toPx()
            }
            .pressScale(interaction)
            .clickable(interactionSource = interaction, indication = null, onClick = onEdit),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left accent rail — colorful type identity.
            Box(
                modifier = Modifier
                    .padding(vertical = 14.dp)
                    .padding(start = 14.dp)
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent)
            )

            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Tajly.sectionGlow(accent)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.ic3d_gym),
                            contentDescription = null,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = schedule.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = c.textHi
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                TablerIcons.Clock,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = GoldPrimary
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = formatTime(schedule.startTime) + " – " + formatTime(schedule.endTime),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = GoldPrimary
                            )
                        }
                    }

                    // Type chip — quiet, colored by section.
                    Text(
                        text = schedule.workoutType.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(accent.copy(alpha = 0.14f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                if (schedule.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = schedule.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textMid
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(TablerIcons.Edit, contentDescription = "Edit", tint = c.textMid, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(TablerIcons.Trash, contentDescription = "Delete", tint = Tajly.Coral, modifier = Modifier.size(18.dp))
                    }
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
            GoldButton(
                text = if (schedule == null) languageViewModel.getString(StringKey.ADD) else languageViewModel.getString(StringKey.SAVE),
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
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(languageViewModel.getString(StringKey.CANCEL), color = TajlyTheme.colors.textMid)
            }
        }
    )
}

private enum class ViewMode {
    WEEK, MONTH
}

private fun Month.pretty(): String =
    name.lowercase().replaceFirstChar { it.uppercase() }

private fun LocalDate.prettyDay(): String {
    val dow = dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    return "$dow, ${month.pretty()} $dayOfMonth"
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
