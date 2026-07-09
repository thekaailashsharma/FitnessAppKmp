package org.awi.fitness.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import org.awi.fitness.data.WeighInEntry
import org.awi.fitness.utils.currentTimeMillis
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.Canvas
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import cafe.adriel.voyager.core.screen.Screen
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertCircle
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.BrandApple
import compose.icons.tablericons.Check
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.Eye
import compose.icons.tablericons.EyeOff
import kotlinx.coroutines.launch
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.FitnessGoal
import org.awi.fitness.model.FitnessLevel
import org.awi.fitness.navigation.LocalAppNavigation
import org.awi.fitness.navigation.RootRoute
import org.awi.fitness.repository.AuthRepository
import org.awi.fitness.repository.CommunityRepository
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.viewmodel.AuthState
import org.awi.fitness.viewmodel.AuthViewModel
import org.awi.fitness.viewmodel.LanguageViewModel
import org.awi.fitness.viewmodel.LocalLanguageViewModel
import org.awi.fitness.data.StringKey
import org.awi.fitness.viewmodel.WorkoutViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.card_fuel
import fitnessappkmp.composeapp.generated.resources.hero_dark_2
import fitnessappkmp.composeapp.generated.resources.ob_body
import fitnessappkmp.composeapp.generated.resources.ob_fig_cycle
import fitnessappkmp.composeapp.generated.resources.ob_fig_hike
import fitnessappkmp.composeapp.generated.resources.ob_fig_run
import fitnessappkmp.composeapp.generated.resources.ob_fig_strength
import fitnessappkmp.composeapp.generated.resources.ob_fig_swim
import fitnessappkmp.composeapp.generated.resources.ob_fig_yoga

private val Gold = GoldPrimary
private val GoldTint = GoldPrimary.copy(alpha = 0.14f)
private val OnGold = Color(0xFF211803)
private val OnbBg = Tajly.Bg
private val FieldBg = Color.White.copy(alpha = 0.05f)
private val FieldBorder = Tajly.HairStrong

private const val TOTAL_STEPS = 7 // 0 intro1, 1 intro2, 2 account, 3 aboutYou, 4 height, 5 weight, 6 activities
private const val HEIGHT_MIN = 140
private const val HEIGHT_MAX = 210
private const val WEIGHT_MIN = 30
private const val WEIGHT_MAX = 200
private const val AGE_MIN = 13
private const val AGE_MAX = 90

class OnboardingScreen(
    private val languageViewModel: LanguageViewModel,
) : Screen {
    @Composable
    override fun Content() {
        val appNavigation = LocalAppNavigation.current
        val workoutViewModel = remember { WorkoutViewModel() }
        val authViewModel = remember { AuthViewModel(AuthRepository()) }
        val scope = rememberCoroutineScope()
        val settings = remember { UserSettings.getInstance() }

        // Resume where the user left off if they abandoned onboarding earlier.
        //    If they aren't logged in yet, never resume past the account step.
        val startStep = remember {
            settings.onboardingStep
                .let { if (!settings.isLoggedIn && it > 2) 2 else it }
                .coerceIn(0, TOTAL_STEPS - 1)
        }
        var step by remember { mutableIntStateOf(startStep) }

        // Account fields
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirm by remember { mutableStateOf("") }
        var awaitingSignup by remember { mutableStateOf(false) }
        var formError by remember { mutableStateOf<String?>(null) }

        // Selections — prefill from any previously saved profile so returning users see their values.
        var selectedGoal by remember {
            mutableStateOf(FitnessGoal.entries.firstOrNull { it.name == settings.fitnessGoal })
        }
        var heightCm by remember { mutableIntStateOf(settings.profileHeightCm.takeIf { it > 0 } ?: 175) }
        var weightKg by remember { mutableIntStateOf(settings.profileWeightKg.takeIf { it > 0f }?.roundToInt() ?: 70) }
        var ageYears by remember { mutableIntStateOf(settings.profileAge.takeIf { it > 0 } ?: 25) }
        var gender by remember { mutableStateOf(settings.profileGender.ifBlank { "MALE" }) }
        var activities by remember { mutableStateOf<Set<String>>(emptySet()) }

        val authState by authViewModel.state.collectAsState()

        // Persist the resume point as the user moves through the flow.
        LaunchedEffect(step) {
            if (!settings.hasCompletedOnboarding) settings.onboardingStep = step
        }

        // Advance past account step when sign-up succeeds
        LaunchedEffect(authState) {
            if (awaitingSignup && authState is AuthState.Success) {
                awaitingSignup = false
                step = 3
            } else if (authState is AuthState.Error) {
                awaitingSignup = false
            }
        }

        fun finishOnboarding() {
            val goal = selectedGoal ?: FitnessGoal.WEIGHT_LOSS
            val level = FitnessLevel.BEGINNER
            val days = 3
            val fullName = listOf(firstName, lastName).filter { it.isNotBlank() }
                .joinToString(" ").trim()
            if (fullName.isNotBlank()) settings.userName = fullName
            settings.fitnessGoal = goal.name
            settings.fitnessLevel = level.name
            settings.workoutDaysPerWeek = days
            // Persist physical stats so the calorie calculator + profile prefill correctly.
            settings.profileHeightCm = heightCm
            settings.profileWeightKg = weightKg.toFloat()
            settings.profileAge = ageYears
            settings.profileGender = gender
            settings.hasCompletedOnboarding = true
            settings.onboardingStep = 0
            // Seed weight tracking with the onboarding weight so the graph starts populated.
            settings.addWeighIn(
                WeighInEntry(
                    weight = weightKg.toFloat(),
                    date = currentTimeMillis() / 1000L,
                    note = languageViewModel.getString(StringKey.ONB_STARTING_WEIGHT)
                )
            )
            scope.launch {
                try {
                    workoutViewModel.saveUserGoals(goal, level, days, "")
                } catch (_: Exception) {
                    // Non-fatal — plan can be generated later from the Train tab
                }
            }
            // Best-effort: sync the profile to Firestore (never blocks onboarding).
            scope.launch {
                try {
                    CommunityRepository().saveOnboardingProfile(
                        displayName = fullName,
                        goal = goal.name,
                        heightCm = heightCm,
                        weightKg = weightKg.toFloat(),
                        age = ageYears,
                        gender = gender
                    )
                } catch (_: Exception) {
                }
            }
            // After onboarding the user hits the paywall (subscription gate) before the app.
            appNavigation.navigateTo(RootRoute.Paywall)
        }

        fun submitAccount() {
            if (settings.isLoggedIn) {
                step = 3
                return
            }
            // Client-side validation with clear inline errors.
            formError = when {
                firstName.isBlank() -> languageViewModel.getString(StringKey.ONB_ERR_FIRST_NAME)
                !email.contains("@") || !email.contains(".") -> languageViewModel.getString(StringKey.ONB_ERR_EMAIL)
                password.length < 6 -> languageViewModel.getString(StringKey.ONB_ERR_PASSWORD_LEN)
                password != confirm -> languageViewModel.getString(StringKey.ONB_ERR_PASSWORD_MATCH)
                else -> null
            }
            if (formError != null) return
            authViewModel.updateEmail(email.trim())
            authViewModel.updatePassword(password)
            awaitingSignup = true
            scope.launch { authViewModel.signUp() }
        }

        // Whether Next is allowed on the current step
        val canAdvance = when (step) {
            2 -> settings.isLoggedIn ||
                (firstName.isNotBlank() && email.contains("@") && password.length >= 6 && password == confirm)
            6 -> activities.isNotEmpty()
            else -> true
        }

        fun goNext() {
            when (step) {
                2 -> submitAccount()
                6 -> finishOnboarding()
                else -> if (step < TOTAL_STEPS - 1 && canAdvance) step += 1
            }
        }

        fun goBack() {
            if (step > 0) step -= 1
        }

        val label = when (step) {
            3, 4, 5 -> languageViewModel.getString(StringKey.CONFIRM)
            6 -> languageViewModel.getString(StringKey.ONB_START_TRAINING)
            else -> languageViewModel.getString(StringKey.CONTINUE)
        }
        val overPhoto = step <= 1

        // Horizontal-only draggable coexists with each step's vertical scroll + clickable rows
        //    (orthogonal orientations), so the page swipe works everywhere: right → next, left → back.
        //    Disabled on the account form so it doesn't fight the text fields.
        val swipeAcc = remember { mutableStateOf(0f) }
        val swipeDragState = rememberDraggableState { delta -> swipeAcc.value += delta }

        CompositionLocalProvider(LocalLanguageViewModel provides languageViewModel) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OnbBg)
                .then(
                    if (step != 2) Modifier.draggable(
                        orientation = Orientation.Horizontal,
                        state = swipeDragState,
                        onDragStarted = { swipeAcc.value = 0f },
                        onDragStopped = {
                            val a = swipeAcc.value
                            swipeAcc.value = 0f
                            if (a >= 60f) goNext() else if (a <= -60f) goBack()
                        }
                    ) else Modifier
                )
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(tween(380)) { it } + fadeIn(tween(300)))
                            .togetherWith(slideOutHorizontally(tween(320)) { -it / 3 } + fadeOut(tween(220)))
                    } else {
                        (slideInHorizontally(tween(380)) { -it } + fadeIn(tween(300)))
                            .togetherWith(slideOutHorizontally(tween(320)) { it / 3 } + fadeOut(tween(220)))
                    }
                },
                label = "onbStep",
                modifier = Modifier.fillMaxSize()
            ) { current ->
                when (current) {
                    0 -> IntroStep(
                        image = Res.drawable.hero_dark_2,
                        titleParts = listOf(
                            languageViewModel.getString(StringKey.ONB_INTRO1_P1) to false,
                            languageViewModel.getString(StringKey.ONB_INTRO1_P2) to true,
                            languageViewModel.getString(StringKey.ONB_INTRO1_P3) to false
                        ),
                        subtitle = languageViewModel.getString(StringKey.ONB_INTRO1_SUB),
                        progress = 0.5f
                    )
                    1 -> IntroStep(
                        image = Res.drawable.card_fuel,
                        titleParts = listOf(
                            languageViewModel.getString(StringKey.ONB_INTRO2_P1) to false,
                            languageViewModel.getString(StringKey.ONB_INTRO2_P2) to true,
                            languageViewModel.getString(StringKey.ONB_INTRO2_P3) to false
                        ),
                        subtitle = languageViewModel.getString(StringKey.ONB_INTRO2_SUB),
                        progress = 1f
                    )
                    2 -> CreateAccountStep(
                        firstName = firstName, onFirstName = { firstName = it; formError = null },
                        lastName = lastName, onLastName = { lastName = it },
                        email = email, onEmail = { email = it; formError = null },
                        password = password, onPassword = { password = it; formError = null },
                        confirm = confirm, onConfirm = { confirm = it; formError = null },
                        authState = authState,
                        formError = formError,
                        alreadyLoggedIn = settings.isLoggedIn,
                        onCreate = { submitAccount() }
                    )
                    3 -> AboutYouStep(
                        gender = gender, onGender = { gender = it },
                        age = ageYears, onAge = { ageYears = it }
                    )
                    4 -> MeasureStep(languageViewModel.getString(StringKey.ONB_HEIGHT_Q), languageViewModel.getString(StringKey.ONB_HEIGHT_HINT), heightCm, "CM", HEIGHT_MIN, HEIGHT_MAX) { heightCm = it }
                    5 -> MeasureStep(languageViewModel.getString(StringKey.ONB_WEIGHT_Q), languageViewModel.getString(StringKey.ONB_WEIGHT_HINT), weightKg, "KG", WEIGHT_MIN, WEIGHT_MAX) { weightKg = it }
                    else -> ActivitiesStep(activities) { key ->
                        activities = if (key in activities) activities - key else activities + key
                    }
                }
            }

            // Bottom swipe bar (every step except the account form, which uses its own CTA).
            if (step != 2) {
                SwipeBar(
                    showBack = step > 0,
                    label = label,
                    resetKey = step,
                    enabled = canAdvance,
                    loading = awaitingSignup && authState is AuthState.Loading,
                    overPhoto = overPhoto,
                    onBack = { goBack() },
                    onNext = { goNext() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                )
            }
        }
        }
    }
}

// ── Intro photo step ─────────────────────────────────────────────────────────

@Composable
private fun IntroStep(
    image: DrawableResource,
    titleParts: List<Pair<String, Boolean>>,
    subtitle: String,
    progress: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Legibility scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.15f),
                        0.45f to Color.Transparent,
                        0.7f to Color.Black.copy(alpha = 0.55f),
                        1f to Color.Black.copy(alpha = 0.9f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Page dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                repeat(2) { i ->
                    val active = (progress >= 1f && i == 1) || (progress < 1f && i == 0)
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (active) 22.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (active) Gold else Color.White.copy(alpha = 0.35f))
                    )
                }
            }

            Text(
                text = buildAnnotatedString {
                    titleParts.forEach { (t, bold) ->
                        if (bold) {
                            withStyle(SpanStyle(fontWeight = FontWeight.Black, color = GoldBright)) { append(t) }
                        } else {
                            withStyle(SpanStyle(fontWeight = FontWeight.Light, color = Color.White)) { append(t) }
                        }
                    }
                },
                fontSize = 40.sp,
                lineHeight = 46.sp
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.75f),
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(20.dp))
            // Thin gold progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                val animated by animateFloatAsState(progress, tween(500), label = "introProgress")
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animated)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(Tajly.GoldGradient)
                )
            }
        }
    }
}

// ── Create account step ──────────────────────────────────────────────────────

@Composable
private fun CreateAccountStep(
    firstName: String, onFirstName: (String) -> Unit,
    lastName: String, onLastName: (String) -> Unit,
    email: String, onEmail: (String) -> Unit,
    password: String, onPassword: (String) -> Unit,
    confirm: String, onConfirm: (String) -> Unit,
    authState: AuthState,
    formError: String?,
    alreadyLoggedIn: Boolean,
    onCreate: () -> Unit
) {
    val appNav = LocalAppNavigation.current
    val lang = LocalLanguageViewModel.current
    Box(modifier = Modifier.fillMaxSize().background(OnbBg)) {
        // Faint hero photo at top
        Image(
            painter = painterResource(Res.drawable.hero_dark_2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.28f,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .align(Alignment.TopCenter)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, OnbBg)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScrollCompat()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 24.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(48.dp))
            Text(
                lang.getString(StringKey.ONB_LETS_GET_STARTED),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                lang.getString(StringKey.ONB_CREATE_ACCOUNT_SUB),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TightField(firstName, onFirstName, lang.getString(StringKey.ONB_FIRST_NAME), Modifier.weight(1f),
                    caps = KeyboardCapitalization.Words)
                TightField(lastName, onLastName, lang.getString(StringKey.ONB_LAST_NAME), Modifier.weight(1f),
                    caps = KeyboardCapitalization.Words)
            }
            TightField(email, onEmail, lang.getString(StringKey.EMAIL), Modifier.fillMaxWidth(),
                keyboard = KeyboardOptions(imeAction = ImeAction.Next))
            PasswordField(password, onPassword, lang.getString(StringKey.PASSWORD))
            PasswordField(confirm, onConfirm, lang.getString(StringKey.ONB_CONFIRM_PASSWORD))

            val errorText = formError ?: (authState as? AuthState.Error)?.message
            if (errorText != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        TablerIcons.AlertCircle,
                        contentDescription = null,
                        tint = Tajly.Coral,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        errorText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Tajly.Coral
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            GoldButton(
                text = if (alreadyLoggedIn) lang.getString(StringKey.CONTINUE) else lang.getString(StringKey.CREATE_ACCOUNT),
                onClick = onCreate,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                loading = authState is AuthState.Loading
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    lang.getString(StringKey.ALREADY_HAVE_ACCOUNT) + " ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    lang.getString(StringKey.SIGN_IN),
                    style = MaterialTheme.typography.bodySmall,
                    color = Gold,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { appNav.navigateTo(RootRoute.Auth) }
                )
            }
        }
    }
}

@Composable
private fun TightField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    caps: KeyboardCapitalization = KeyboardCapitalization.None,
    keyboard: KeyboardOptions = KeyboardOptions(capitalization = caps, imeAction = ImeAction.Next)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.35f)) },
        singleLine = true,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
        colors = fieldColors(),
        keyboardOptions = keyboard.copy(capitalization = caps)
    )
}

@Composable
private fun PasswordField(value: String, onChange: (String) -> Unit, placeholder: String) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.35f)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
        colors = fieldColors(),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        trailingIcon = {
            Icon(
                imageVector = if (visible) TablerIcons.EyeOff else TablerIcons.Eye,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.size(20.dp).clickable { visible = !visible }
            )
        }
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Gold,
    unfocusedBorderColor = FieldBorder,
    cursorColor = Gold,
    focusedContainerColor = FieldBg,
    unfocusedContainerColor = FieldBg
)

// verticalScroll shim to keep imports tidy
@Composable
private fun Modifier.verticalScrollCompat(): Modifier {
    val scroll = rememberScrollState()
    return this.verticalScroll(scroll)
}

// ── Goal step (single-select) ────────────────────────────────────────────────

private data class GoalRow(
    val goal: FitnessGoal,
    val res: DrawableResource,
    val titleKey: StringKey,
    val subtitleKey: StringKey
)

private val goalRows = listOf(
    GoalRow(FitnessGoal.WEIGHT_LOSS, Res.drawable.ob_fig_run, StringKey.ONB_GOAL_LOSE_T, StringKey.ONB_GOAL_LOSE_S),
    GoalRow(FitnessGoal.MUSCLE_GAIN, Res.drawable.ob_fig_strength, StringKey.ONB_GOAL_STRENGTH_T, StringKey.ONB_GOAL_STRENGTH_S),
    GoalRow(FitnessGoal.ENDURANCE, Res.drawable.ob_fig_cycle, StringKey.ONB_GOAL_ACTIVE_T, StringKey.ONB_GOAL_ACTIVE_S),
    GoalRow(FitnessGoal.ENDURANCE, Res.drawable.ob_fig_swim, StringKey.ONB_GOAL_ENERGY_T, StringKey.ONB_GOAL_ENERGY_S),
    GoalRow(FitnessGoal.FLEXIBILITY, Res.drawable.ob_fig_yoga, StringKey.ONB_GOAL_STRESS_T, StringKey.ONB_GOAL_STRESS_S),
)

@Composable
private fun GoalStep(selected: FitnessGoal?, onSelect: (FitnessGoal) -> Unit) {
    // Track the exact row title so visually-distinct rows sharing an enum stay independent.
    val lang = LocalLanguageViewModel.current
    var selectedKey by remember { mutableStateOf<StringKey?>(null) }
    StepScaffold(title = lang.getString(StringKey.WHATS_YOUR_GOAL), subtitle = lang.getString(StringKey.ONB_GOAL_SUB)) {
        goalRows.forEach { row ->
            SelectRow(
                res = row.res,
                title = lang.getString(row.titleKey),
                subtitle = lang.getString(row.subtitleKey),
                selected = selectedKey == row.titleKey,
                onClick = {
                    selectedKey = row.titleKey
                    onSelect(row.goal)
                }
            )
        }
    }
}

// ── Activities step (multi-select) ───────────────────────────────────────────

private data class ActivityRow(
    val key: String,
    val res: DrawableResource,
    val titleKey: StringKey,
    val subtitleKey: StringKey
)

private val activityRows = listOf(
    ActivityRow("run", Res.drawable.ob_fig_run, StringKey.ONB_ACT_RUN_T, StringKey.ONB_ACT_RUN_S),
    ActivityRow("cycle", Res.drawable.ob_fig_cycle, StringKey.ONB_ACT_CYCLE_T, StringKey.ONB_ACT_CYCLE_S),
    ActivityRow("swim", Res.drawable.ob_fig_swim, StringKey.ONB_ACT_SWIM_T, StringKey.ONB_ACT_SWIM_S),
    ActivityRow("yoga", Res.drawable.ob_fig_yoga, StringKey.YOGA, StringKey.ONB_ACT_YOGA_S),
    ActivityRow("hike", Res.drawable.ob_fig_hike, StringKey.ONB_ACT_HIKE_T, StringKey.ONB_ACT_HIKE_S),
)

@Composable
private fun ActivitiesStep(selected: Set<String>, onToggle: (String) -> Unit) {
    val lang = LocalLanguageViewModel.current
    StepScaffold(title = lang.getString(StringKey.ONB_PICK_ACTIVITIES), subtitle = lang.getString(StringKey.ONB_ACTIVITIES_SUB)) {
        activityRows.forEach { row ->
            SelectRow(
                res = row.res,
                title = lang.getString(row.titleKey),
                subtitle = lang.getString(row.subtitleKey),
                selected = row.key in selected,
                onClick = { onToggle(row.key) }
            )
        }
    }
}

// ── About you step (biological sex + age) ────────────────────────────────────

@Composable
private fun AboutYouStep(
    gender: String,
    onGender: (String) -> Unit,
    age: Int,
    onAge: (Int) -> Unit
) {
    val lang = LocalLanguageViewModel.current
    StepScaffold(
        title = lang.getString(StringKey.ONB_ABOUT_YOU),
        subtitle = lang.getString(StringKey.ONB_ABOUT_YOU_SUB)
    ) {
        Text(
            lang.getString(StringKey.BIOLOGICAL_SEX),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = GoldBright,
            modifier = Modifier.padding(top = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GenderCard(lang.getString(StringKey.GENDER_MALE), "♂", gender == "MALE", Modifier.weight(1f)) { onGender("MALE") }
            GenderCard(lang.getString(StringKey.GENDER_FEMALE), "♀", gender == "FEMALE", Modifier.weight(1f)) { onGender("FEMALE") }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            lang.getString(StringKey.AGE),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = GoldBright
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(FieldBg)
                .border(1.dp, FieldBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StepperButton("–") { if (age > AGE_MIN) onAge(age - 1) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$age", fontSize = 46.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(lang.getString(StringKey.ONB_YEARS), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
            }
            StepperButton("+") { if (age < AGE_MAX) onAge(age + 1) }
        }
    }
}

@Composable
private fun GenderCard(
    label: String,
    symbol: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) GoldTint else FieldBg)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) Gold else FieldBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(symbol, fontSize = 34.sp, color = if (selected) GoldBright else Color.White)
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) GoldBright else Color.White
        )
    }
}

@Composable
private fun StepperButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Gold.copy(alpha = 0.16f))
            .border(1.dp, Gold.copy(alpha = 0.4f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = GoldBright)
    }
}

// ── Shared select scaffold + row ─────────────────────────────────────────────

@Composable
private fun StepScaffold(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnbBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 110.dp)
            .verticalScrollCompat(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun SelectRow(
    res: DrawableResource,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        if (selected) 1.015f else 1f,
        spring(dampingRatio = 0.7f),
        label = "rowScale"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) GoldTint else FieldBg)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) Gold else FieldBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (selected) Gold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(res),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(44.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) GoldBright else Color.White
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
        SquareCheckbox(selected)
    }
}

@Composable
private fun SquareCheckbox(checked: Boolean) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) Gold else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (checked) Gold else Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                TablerIcons.Check,
                contentDescription = null,
                tint = OnGold,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ── Height step (draggable ruler) ────────────────────────────────────────────

@Composable
private fun MeasureStep(
    title: String,
    subtitle: String,
    value: Int,
    unit: String,
    min: Int,
    max: Int,
    onChange: (Int) -> Unit
) {
    val density = LocalDensity.current
    val pxPerUnit = with(density) { 8.dp.toPx() }
    var accumulated by remember { mutableStateOf(0f) }
    val glow = 0.14f + ((value - min).toFloat() / (max - min)) * 0.2f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnbBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 110.dp)
    ) {
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Body (bigger) + gold glow + unit pill + big value
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.85f)
                        .aspectRatio(0.5f)
                        .blur(40.dp)
                        .background(
                            Brush.radialGradient(listOf(Gold.copy(alpha = glow), Color.Transparent)),
                            CircleShape
                        )
                )
                Image(
                    painter = painterResource(Res.drawable.ob_body),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxHeight(0.98f)
                )
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Tajly.GoldGradient)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(unit, color = OnGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("$value", fontSize = 56.sp, fontWeight = FontWeight.Black, color = GoldBright)
                }
            }

            // Draggable vertical ruler
            MeasureRuler(
                value = value,
                min = min,
                max = max,
                pxPerUnit = pxPerUnit,
                modifier = Modifier
                    .width(88.dp)
                    .fillMaxHeight()
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            accumulated += -delta // drag up => higher value
                            val steps = (accumulated / pxPerUnit).toInt()
                            if (steps != 0) {
                                val next = (value + steps).coerceIn(min, max)
                                accumulated -= steps * pxPerUnit
                                if (next != value) onChange(next)
                            }
                        }
                    )
            )
        }
    }
}

@Composable
private fun MeasureRuler(value: Int, min: Int, max: Int, pxPerUnit: Float, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val numberStyle = TextStyle(color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
    Canvas(modifier = modifier) {
        val centerY = size.height / 2f
        val rightX = size.width
        val range = 30
        for (offset in -range..range) {
            val v = value + offset
            if (v < min - 1 || v > max + 1) continue
            val y = centerY + offset * pxPerUnit
            if (y < 0 || y > size.height) continue
            val major = v % 10 == 0
            val medium = v % 5 == 0
            val lineLen = when {
                major -> size.width * 0.55f
                medium -> size.width * 0.4f
                else -> size.width * 0.25f
            }
            val fade = (1f - kotlin.math.abs(offset) / (range.toFloat() + 4f)).coerceIn(0.15f, 1f)
            drawLine(
                color = Color.White.copy(alpha = 0.35f * fade),
                start = Offset(rightX - lineLen, y),
                end = Offset(rightX, y),
                strokeWidth = if (major) 2.5f else 1.5f
            )
            if (major) {
                val text = measurer.measure(v.toString(), numberStyle)
                drawText(
                    textLayoutResult = text,
                    topLeft = Offset(
                        rightX - lineLen - text.size.width - 8f,
                        y - text.size.height / 2f
                    )
                )
            }
        }
        // Gold pointer at center
        val ptX = rightX - size.width * 0.62f
        drawLine(
            color = GoldPrimary,
            start = Offset(ptX, centerY),
            end = Offset(rightX, centerY),
            strokeWidth = 4f
        )
        val tri = Path().apply {
            moveTo(ptX, centerY)
            lineTo(ptX - 16f, centerY - 12f)
            lineTo(ptX - 16f, centerY + 12f)
            close()
        }
        drawPath(tri, GoldBright)
    }
}

// ── Swipe bar (bottom control) ───────────────────────────────────────────────

@Composable
private fun SwipeBar(
    showBack: Boolean,
    label: String,
    enabled: Boolean,
    loading: Boolean,
    overPhoto: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    // Reset key: snap the thumb home whenever this changes (the step index). Several steps
    // share the same `label` ("Confirm"), so keying the reset on the step index instead of
    // the label guarantees the thumb returns to the start on every step.
    resetKey: Any = label,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Back — neutral glass circle (tap to go back)
        if (showBack) {
            CircleButton(
                onClick = onBack,
                background = Color.White.copy(alpha = 0.12f),
                border = Color.White.copy(alpha = 0.25f)
            ) {
                Icon(TablerIcons.ArrowLeft, contentDescription = "Back", tint = Color.White,
                    modifier = Modifier.size(22.dp))
            }
        }

        // Slide-to-continue track: the gold thumb physically follows the finger,
        //    and releasing past ~60% of the track commits to the next step.
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    if (overPhoto) Color.Black.copy(alpha = 0.38f)
                    else Color.White.copy(alpha = 0.08f)
                )
                .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(32.dp))
        ) {
            val density = LocalDensity.current
            val pad = with(density) { 4.dp.toPx() }
            val thumbSize = 56.dp
            val thumbPx = with(density) { thumbSize.toPx() }
            val trackPx = with(density) { maxWidth.toPx() }
            val maxTravel = (trackPx - thumbPx - pad * 2f).coerceAtLeast(1f)

            val offsetX = remember { Animatable(0f) }
            val scope = rememberCoroutineScope()
            val progress = (offsetX.value / maxTravel).coerceIn(0f, 1f)

            // Snap the thumb home whenever the step changes. Keyed on `resetKey` (the step
            // index) rather than `label`, because several steps share the label "Confirm" —
            // keying on the label alone left the thumb parked at the end of the track across
            // those steps after a busy transition (e.g. right after signup), which read as the
            // gold arrow "disappearing".
            LaunchedEffect(resetKey) { offsetX.snapTo(0f) }
            // If a remeasure shrinks the track, keep the thumb inside it so it can never sit
            // off-screen (belt-and-braces with the clamp on the thumb's offset below).
            LaunchedEffect(maxTravel) {
                if (offsetX.value > maxTravel) offsetX.snapTo(0f)
            }

            val dragState = rememberDraggableState { delta ->
                scope.launch {
                    offsetX.snapTo((offsetX.value + delta).coerceIn(0f, maxTravel))
                }
            }

            // Centered label across the whole track — fades as you slide.
            Box(
                modifier = Modifier.matchParentSize().padding(horizontal = thumbSize),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = (1f - progress).coerceIn(0.15f, 1f)),
                    modifier = Modifier.alpha(if (enabled || loading) 1f else 0.5f)
                )
            }

            // Chevron hint on the right — fades out as the thumb advances.
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
                    .alpha((1f - progress * 1.6f).coerceIn(0f, 1f)),
                horizontalArrangement = Arrangement.spacedBy((-6).dp)
            ) {
                listOf(0.9f, 0.55f, 0.25f).forEach { a ->
                    Icon(TablerIcons.ChevronRight, contentDescription = null,
                        tint = Gold.copy(alpha = a), modifier = Modifier.size(20.dp))
                }
            }

            // The gold thumb — draggable; commits past 60%.
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    // Clamp to the track: the gold thumb can never be pushed off the visible
                    // edge, no matter what value the drag animatable transiently holds.
                    .offset { IntOffset((offsetX.value.coerceIn(0f, maxTravel) + pad).roundToInt(), 0) }
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(Tajly.GoldGradient)
                    .alpha(if (enabled && !loading) 1f else 0.5f)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = dragState,
                        enabled = !loading,
                        onDragStopped = {
                            val p = offsetX.value / maxTravel
                            if (p >= 0.6f && enabled && !loading) {
                                scope.launch {
                                    offsetX.animateTo(maxTravel, tween(140))
                                    onNext()
                                    offsetX.snapTo(0f)
                                }
                            } else {
                                scope.launch { offsetX.animateTo(0f, spring()) }
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (loading) {
                    CircularProgressIndicator(color = OnGold, strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp))
                } else {
                    Icon(TablerIcons.ArrowRight, contentDescription = "Next", tint = OnGold,
                        modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
private fun CircleButton(
    onClick: () -> Unit,
    background: Color,
    border: Color,
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .pressScale(interaction)
            .size(56.dp)
            .clip(CircleShape)
            .background(background)
            .border(1.dp, border, CircleShape)
            .clickable(interactionSource = interaction, indication = null) { onClick() },
        contentAlignment = Alignment.Center
    ) { content() }
}
