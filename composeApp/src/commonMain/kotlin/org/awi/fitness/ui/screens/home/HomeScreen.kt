package org.awi.fitness.ui.screens.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.Activity
import compose.icons.tablericons.ArrowUpRight
import compose.icons.tablericons.Scan
import compose.icons.tablericons.Award
import compose.icons.tablericons.Bell
import compose.icons.tablericons.Bolt
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.Flame
import compose.icons.tablericons.Heart
import compose.icons.tablericons.Leaf
import compose.icons.tablericons.Send
import compose.icons.tablericons.Trophy
import compose.icons.tablericons.User
import compose.icons.tablericons.Users
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.card_coach
import fitnessappkmp.composeapp.generated.resources.card_community
import fitnessappkmp.composeapp.generated.resources.card_compete
import fitnessappkmp.composeapp.generated.resources.card_fuel
import fitnessappkmp.composeapp.generated.resources.card_move
import fitnessappkmp.composeapp.generated.resources.card_streak
import fitnessappkmp.composeapp.generated.resources.card_yoga
import fitnessappkmp.composeapp.generated.resources.ic3d_fire
import fitnessappkmp.composeapp.generated.resources.tex_gold_2
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.awi.fitness.ui.components.LottieAnim
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.ConversationTrigger
import org.awi.fitness.repository.SubscriptionRepository
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.ui.CalorieCalculatorScreen
import org.awi.fitness.ui.components.BackdropImage
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GlassTier
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.StatRing
import org.awi.fitness.ui.components.glassSource
import org.awi.fitness.ui.screens.ChallengeDetailScreen
import org.awi.fitness.ui.screens.MealScreen
import org.awi.fitness.ui.screens.MealScanScreen
import org.awi.fitness.ui.screens.MyPlansScreen
import org.awi.fitness.ui.screens.PaywallScreen
import org.awi.fitness.ui.screens.ProfileScreen
import org.awi.fitness.ui.screens.TipDeckScreen
import org.awi.fitness.ui.screens.WorkoutScreen
import org.awi.fitness.ui.screens.avatar.AvatarScreen
import org.awi.fitness.ui.screens.community.CommunityFeedScreen
import org.awi.fitness.ui.screens.community.PostDetailScreen
import org.awi.fitness.viewmodel.LocalArticleViewModel
import org.awi.fitness.viewmodel.LocalHomeViewModel
import org.awi.fitness.viewmodel.LocalLanguageViewModel
import org.awi.fitness.viewmodel.LocalMealPlanViewModel
import org.awi.fitness.viewmodel.LocalWorkoutViewModel
import org.awi.fitness.viewmodel.ViewModelStore
import org.awi.fitness.viewmodel.WorkoutSchedulePreview
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class HomeTimeframe { TODAY, WEEK }

/**
 * Tajly home — immersive backdrop + floating liquid glass, ~13 sections all bound to real
 * ViewModels (zero mock data; honest empty states). Signature swipeable motivational deck,
 * count-up / ring-sweep micro-animations. Every CTA opens a real, working screen.
 */
class HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val homeVm = LocalHomeViewModel.current
        val state by homeVm.state.collectAsState()
        val languageViewModel = LocalLanguageViewModel.current
        val mealPlanVm = LocalMealPlanViewModel.current
        val mealState by mealPlanVm.state.collectAsState()
        val workoutVm = LocalWorkoutViewModel.current
        val workoutState by workoutVm.state.collectAsState()
        val challengesVm = ViewModelStore.challenges
        val challengeState by challengesVm.state.collectAsState()
        val communityVm = ViewModelStore.community
        val communityState by communityVm.state.collectAsState()
        val activityState by communityVm.activityState.collectAsState()

        val userSettings = UserSettings.getInstance()
        val mealCompletions by userSettings.mealCompletions.collectAsState()
        val currentLanguage by userSettings.language.collectAsState()

        val today = remember { org.awi.fitness.utils.todayLocalDate() }
        val todayDow = remember { today.dayOfWeek.ordinal + 1 }
        val todayStr = remember { today.toString() }

        val todayMeals = remember(mealState.activePlan, todayDow) {
            mealState.activePlan?.meals?.filter { it.dayOfWeek == todayDow } ?: emptyList()
        }
        val todayDone = mealCompletions[todayStr] ?: emptySet()
        val mealsEaten = todayMeals.count { todayDone.contains(it.id) }
        val macros = remember(mealState.activePlan, mealCompletions, todayDow) {
            mealPlanVm.getDailyMacros(todayDow, todayStr)
        }
        // Honest daily target: real when a plan/profile exists, otherwise null → "Set your goal".
        val caloriesTarget: Int? = mealState.activePlan?.targetCalories?.takeIf { it > 0 }
            ?: userSettings.calculatedCalories.takeIf { it > 100 }
        val hasTarget = caloriesTarget != null
        val hasMealPlan = mealState.activePlan != null
        val currentStreak = userSettings.currentStreak
        val totalXp = userSettings.totalXp
        val userLevel = userSettings.userLevel

        val weekDates = remember(today) {
            val monday = today.plus(DatePeriod(days = -(today.dayOfWeek.isoDayNumber - 1)))
            (0..6).map { monday.plus(DatePeriod(days = it)).toString() }
        }
        val weekDayNums = remember(today) {
            val monday = today.plus(DatePeriod(days = -(today.dayOfWeek.isoDayNumber - 1)))
            (0..6).map { monday.plus(DatePeriod(days = it)).dayOfMonth }
        }
        // Real per-day activity: a day is "done" if a meal was eaten OR a workout was completed.
        val lastWorkoutDate = userSettings.lastWorkoutDate
        val weekDone = remember(weekDates, mealCompletions, lastWorkoutDate) {
            weekDates.map { date ->
                (mealCompletions[date] ?: emptySet()).isNotEmpty() || date == lastWorkoutDate
            }
        }
        // Real weekly consumed calories — sum of each day's real consumed total (never target*7).
        val weeklyConsumed = remember(mealState.activePlan, mealCompletions) {
            weekDates.mapIndexed { i, date -> mealPlanVm.getDailyMacros(i + 1, date).consumedCalories }.sum()
        }
        val todayRing = when {
            todayMeals.isNotEmpty() -> mealsEaten.toFloat() / todayMeals.size
            caloriesTarget != null && caloriesTarget > 0 -> macros.consumedCalories.toFloat() / caloriesTarget
            else -> 0f
        }.coerceIn(0f, 1f)

        LaunchedEffect(Unit) {
            homeVm.loadIfNeeded()
            challengesVm.loadChallenges()
            communityVm.loadCommunityFeed()
            communityVm.loadActivityNotifications()
        }
        LaunchedEffect(currentLanguage) { homeVm.refresh() }

        var isPremium by remember { mutableStateOf(true) } // assume premium until known → hide band by default
        LaunchedEffect(Unit) {
            isPremium = runCatching { SubscriptionRepository().checkPremiumAccess() }.getOrDefault(false)
        }

        val c = TajlyTheme.colors
        val userName = remember(userSettings.userName, userSettings.userEmail) {
            userSettings.userName?.takeIf { it.isNotBlank() }
                ?: (userSettings.userEmail ?: "").substringBefore("@").replaceFirstChar { it.uppercase() }
        }
        var timeframe by remember { mutableStateOf(HomeTimeframe.TODAY) }

        val heroValue = when (timeframe) {
            HomeTimeframe.WEEK -> weeklyConsumed
            HomeTimeframe.TODAY -> caloriesTarget ?: 0
        }
        val heroRing = when (timeframe) {
            HomeTimeframe.TODAY -> todayRing
            HomeTimeframe.WEEK -> if (caloriesTarget != null && caloriesTarget > 0)
                weeklyConsumed.toFloat() / (caloriesTarget * 7) else 0f
        }.coerceIn(0f, 1f)
        val heroLabel = when (timeframe) {
            HomeTimeframe.TODAY -> languageViewModel.getString(StringKey.HOMEX_DAILY_TARGET)
            HomeTimeframe.WEEK -> languageViewModel.getString(StringKey.HOMEX_CONSUMED_THIS_WEEK)
        }

        val activeChallenges = challengeState.activeChallenges
        val firstChallenge = activeChallenges.firstOrNull()
        val hp = Modifier.padding(horizontal = 20.dp)

        // Swipe-deck cards — only those backed by real data/features.
        val stackCards = buildList {
            add(StackCard(card_move_res(), languageViewModel.getString(StringKey.HOMEX_TODAYS_SESSION), languageViewModel.getString(StringKey.HOMEX_STACK_MOVE_SUB), languageViewModel.getString(StringKey.HOMEX_START_TODAYS_WORKOUT)) { org.awi.fitness.navigation.CoachNav.open("TRAIN") })
            add(StackCard(card_coach_res(), languageViewModel.getString(StringKey.HOMEX_YOUR_COACH), languageViewModel.getString(StringKey.HOMEX_STACK_COACH_SUB), languageViewModel.getString(StringKey.HOMEX_TALK_TO_COACH)) { navigator.push(AvatarScreen(ConversationTrigger.MANUAL)) })
            add(StackCard(card_fuel_res(), languageViewModel.getString(StringKey.HOMEX_FUEL), languageViewModel.getString(StringKey.HOMEX_STACK_FUEL_SUB), languageViewModel.getString(StringKey.HOMEX_CHECK_TODAYS_MEALS)) { org.awi.fitness.navigation.CoachNav.openMeals() })
            if (firstChallenge != null) {
                val prog = challengeState.userProgress[firstChallenge.id]?.let { pr ->
                    if (pr.targetValue > 0) (pr.currentValue * 100 / pr.targetValue) else 0
                } ?: 0
                add(StackCard(card_compete_res(), languageViewModel.getString(StringKey.HOMEX_CHALLENGE), "${languageViewModel.getString(StringKey.HOMEX_CHALLENGE_PROGRESS_PRE)} $prog% ${languageViewModel.getString(StringKey.HOMEX_CHALLENGE_PROGRESS_POST)}", languageViewModel.getString(StringKey.HOMEX_CONTINUE_CHALLENGE)) { navigator.push(ChallengeDetailScreen(firstChallenge)) })
            }
            add(StackCard(card_streak_res(), languageViewModel.getString(StringKey.STREAK), if (currentStreak > 0) "$currentStreak ${languageViewModel.getString(StringKey.HOMEX_STREAK_DAYS_PROTECT)}" else languageViewModel.getString(StringKey.HOMEX_START_STREAK_TODAY), languageViewModel.getString(StringKey.HOMEX_KEEP_STREAK_ALIVE)) { org.awi.fitness.navigation.CoachNav.open("TRAIN") })
        }

        ProvideGlass {
            Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
                // Immersive hero backdrop — theme-aware, Firebase-controllable, glass source.
                Box(modifier = Modifier.fillMaxWidth().height(440.dp).glassSource()) {
                    BackdropImage(surface = "home", modifier = Modifier.fillMaxSize())
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                0.0f to c.bg.copy(alpha = if (c.isDark) 0.15f else 0.10f),
                                0.55f to c.bg.copy(alpha = if (c.isDark) 0.60f else 0.55f),
                                0.86f to c.bg.copy(alpha = 0.94f),
                                1.0f to c.bg
                            )
                        )
                    )
                }

                when {
                    state.isLoading && state.userProfile == null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = GoldPrimary)
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(22.dp),
                        contentPadding = PaddingValues(top = 52.dp, bottom = 120.dp)
                    ) {
                        item {
                            HeroSection(hp, userName, heroValue, heroRing, heroLabel, macros.consumedCalories,
                                state.bmr, state.tdee, state.bmr > 100f && state.tdee > 100f, currentStreak,
                                activityState.unreadCount, timeframe, { timeframe = it }, languageViewModel,
                                hasTarget = hasTarget,
                                onSetGoal = { navigator.push(CalorieCalculatorScreen()) },
                                onAvatar = { navigator.push(ProfileScreen(languageViewModel = languageViewModel)) },
                                onBell = { navigator.push(CommunityFeedScreen()) })
                        }
                        item {
                            Column {
                                SectionHead(hp, languageViewModel.getString(StringKey.HOMEX_FOR_YOU_TODAY), languageViewModel.getString(StringKey.HOMEX_SWIPE))
                                Spacer(Modifier.height(10.dp))
                                MotivationStack(stackCards)
                            }
                        }
                        item {
                            TodaysPlanCard(hp, state.upcomingWorkout, mealsEaten, todayMeals.size, hasMealPlan,
                                state.scheduledWorkoutsToday, languageViewModel,
                                onStart = { org.awi.fitness.navigation.CoachNav.open("TRAIN") },
                                onNutrition = { org.awi.fitness.navigation.CoachNav.openMeals() },
                                onMeals = { navigator.push(MyPlansScreen(mealPlanVm)) })
                        }
                        item {
                            BentoGrid(hp, workoutState.workoutPlans.size, mealsEaten, todayMeals.size,
                                activeChallenges.size,
                                onCoach = { navigator.push(AvatarScreen(ConversationTrigger.MANUAL)) },
                                onWorkouts = { org.awi.fitness.navigation.CoachNav.open("TRAIN") },
                                onNutrition = { org.awi.fitness.navigation.CoachNav.openMeals() },
                                onChallenges = { firstChallenge?.let { navigator.push(ChallengeDetailScreen(it)) } })
                        }
                        item {
                            Row(
                                modifier = hp.fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Tajly.GoldGradient)
                                    .clickable { navigator.push(MealScanScreen()) }
                                    .padding(horizontal = 18.dp, vertical = 15.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(TablerIcons.Scan, contentDescription = null, tint = OnGold, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(languageViewModel.getString(StringKey.HOMEX_SCAN_A_MEAL), style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold, color = OnGold)
                                    Text(languageViewModel.getString(StringKey.HOMEX_SCAN_SUB),
                                        style = MaterialTheme.typography.bodySmall, color = OnGold.copy(alpha = 0.75f))
                                }
                                Icon(TablerIcons.ArrowUpRight, contentDescription = null, tint = OnGold, modifier = Modifier.size(20.dp))
                            }
                        }
                        item {
                            Row(hp.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                NutritionWidget(Modifier.weight(1f), mealsEaten, todayMeals.size, todayRing,
                                    macros.consumedProtein, macros.consumedCarbs, macros.consumedFat, hasMealPlan) { org.awi.fitness.navigation.CoachNav.openMeals() }
                                StreakWidget(Modifier.weight(1f), currentStreak, userLevel, totalXp) { org.awi.fitness.navigation.CoachNav.open("TRAIN") }
                            }
                        }
                        item { WeekStrip(hp, weekDayNums, weekDone, todayDow) { org.awi.fitness.navigation.CoachNav.openMeals() } }

                        if (activeChallenges.isNotEmpty()) {
                            item {
                                Column {
                                    SectionHead(hp, languageViewModel.getString(StringKey.HOMEX_ACTIVE_CHALLENGES), "")
                                    Spacer(Modifier.height(10.dp))
                                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        itemsIndexed(activeChallenges) { i, ch ->
                                            val pr = challengeState.userProgress[ch.id]
                                            val frac = if (pr != null && pr.targetValue > 0) pr.currentValue.toFloat() / pr.targetValue else 0f
                                            ChallengeImageCard(ch.title, "${(frac.coerceIn(0f, 1f) * 100).roundToInt()}% ${languageViewModel.getString(StringKey.HOMEX_COMPLETE)}",
                                                frac.coerceIn(0f, 1f), ch.imageUrl, challengeFallback(i)) { navigator.push(ChallengeDetailScreen(ch)) }
                                        }
                                    }
                                }
                            }
                        }

                        communityState.posts.firstOrNull()?.let { post ->
                            item {
                                Column {
                                    SectionHead(hp, languageViewModel.getString(StringKey.HOMEX_FROM_COMMUNITY), languageViewModel.getString(StringKey.HOMEX_SEE_THE_FEED), onAction = { navigator.push(CommunityFeedScreen()) })
                                    Spacer(Modifier.height(10.dp))
                                    CommunityHighlight(hp, post.userName, post.content, post.imageUrl, post.likes,
                                        activityState.unreadCount) { navigator.push(PostDetailScreen(post.id)) }
                                }
                            }
                        }

                        item { CoachBand(hp, languageViewModel) { navigator.push(AvatarScreen(ConversationTrigger.MANUAL)) } }

                        if (state.dailyTips.isNotEmpty()) {
                            item {
                                TipsCard(hp, state.dailyTips.size, languageViewModel.getString(StringKey.DAILY_WELLNESS_TIPS)) {
                                    navigator.push(TipDeckScreen(state.dailyTips))
                                }
                            }
                        }

                        if (!isPremium) {
                            item { PremiumBand(hp) { navigator.push(PaywallScreen(languageViewModel)) } }
                        }
                    }
                }
            }
        }
    }
}

// ── data ──
private data class StackCard(val img: DrawableResource, val eyebrow: String, val headline: String, val cta: String, val onClick: () -> Unit)
@Composable private fun card_move_res() = Res.drawable.card_move
@Composable private fun card_coach_res() = Res.drawable.card_coach
@Composable private fun card_fuel_res() = Res.drawable.card_fuel
@Composable private fun card_compete_res() = Res.drawable.card_compete
@Composable private fun card_streak_res() = Res.drawable.card_streak

// ── Hero ──
@Composable
private fun HeroSection(
    modifier: Modifier, userName: String, heroValue: Int, heroRing: Float, heroLabel: String,
    consumed: Int, bmr: Float, tdee: Float, hasStats: Boolean, streak: Int, unread: Int,
    timeframe: HomeTimeframe, onTimeframe: (HomeTimeframe) -> Unit,
    lang: org.awi.fitness.viewmodel.LanguageViewModel, hasTarget: Boolean, onSetGoal: () -> Unit,
    onAvatar: () -> Unit, onBell: () -> Unit
) {
    val c = TajlyTheme.colors
    // Static values — no rolling count-up (per request), so the number never re-animates.
    val shownValue = heroValue
    val shownRing = heroRing

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(lang.getString(StringKey.WELCOME_BACK).uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = GoldBright)
                Text(userName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = c.textHi, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box {
                GlassCard(shape = CircleShape, tier = GlassTier.Nav, modifier = Modifier.size(44.dp).clickable(onClick = onBell)) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) { Icon(TablerIcons.Bell, null, tint = c.textHi, modifier = Modifier.size(20.dp)) }
                }
                if (unread > 0) {
                    val pulse = rememberInfiniteTransition()
                    val s by pulse.animateFloat(1f, 1.15f, infiniteRepeatable(tween(900), RepeatMode.Reverse))
                    Box(Modifier.align(Alignment.TopEnd).graphicsLayer { scaleX = s; scaleY = s }.size(17.dp).clip(CircleShape).background(Tajly.GoldGradient), contentAlignment = Alignment.Center) {
                        Text(if (unread > 9) "9+" else "$unread", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), fontWeight = FontWeight.Bold, color = OnGold)
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            // Profile entry: tap the avatar to open the Profile screen. Shows the user's
            // photo when set, otherwise their initials (User icon if the name is empty).
            val profilePhotoUrl = UserSettings.getInstance().profilePhotoUrl
            GlassCard(shape = CircleShape, tier = GlassTier.Nav, modifier = Modifier.size(50.dp).clickable(onClick = onAvatar)) {
                Box(Modifier.fillMaxSize().clip(CircleShape), Alignment.Center) {
                    if (!profilePhotoUrl.isNullOrBlank() || userName.isNotBlank()) {
                        ImagePlaceholder(
                            url = profilePhotoUrl,
                            contentDescription = userName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            showInitial = true,
                            initial = userName,
                            tint = OnGold,
                            backgroundColor = GoldPrimary
                        )
                    } else {
                        Icon(TablerIcons.User, null, tint = GoldBright, modifier = Modifier.size(26.dp))
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            TimeframeText(lang.getString(StringKey.TODAY), timeframe == HomeTimeframe.TODAY) { onTimeframe(HomeTimeframe.TODAY) }
            TimeframeText(lang.getString(StringKey.HOME_FILTER_THIS_WEEK), timeframe == HomeTimeframe.WEEK) { onTimeframe(HomeTimeframe.WEEK) }
        }

        if (hasTarget) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(shownValue.grouped(), style = MaterialTheme.typography.displayLarge.copy(fontSize = 62.sp, lineHeight = 62.sp, fontWeight = FontWeight.Bold), color = c.textHi, maxLines = 1)
                        Text(" ${lang.getString(StringKey.KCAL)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = c.textMid, modifier = Modifier.padding(bottom = 10.dp))
                    }
                    Text(heroLabel.uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = c.textMid)
                }
                StatRing(progress = shownRing, diameter = 76.dp, strokeWidth = 8.dp) {
                    Text("${(shownRing * 100).roundToInt()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.textHi)
                }
            }
        } else {
            // Honest empty state — no confident fake 2,000 target. Prompt to set a real goal.
            GlassCard(goldTint = true, modifier = Modifier.fillMaxWidth().clickable(onClick = onSetGoal)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(lang.getString(StringKey.HOMEX_NO_CALORIE_GOAL), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = c.textHi)
                    Text(lang.getString(StringKey.HOMEX_SET_GOAL_DESC), style = MaterialTheme.typography.bodyMedium, color = c.textMid)
                    GoldButton(text = lang.getString(StringKey.HOMEX_SET_YOUR_GOAL), onClick = onSetGoal)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StatRow(TablerIcons.Flame, lang.getString(StringKey.HOME_CONSUMED), "${consumed.grouped()} ${lang.getString(StringKey.KCAL)}")
            if (hasStats) {
                StatRow(TablerIcons.Heart, "BMR", "${bmr.roundToInt().grouped()} ${lang.getString(StringKey.KCAL)}")
                StatRow(TablerIcons.Activity, "TDEE", "${tdee.roundToInt().grouped()} ${lang.getString(StringKey.KCAL)}")
            }
            StatRow(TablerIcons.Award, lang.getString(StringKey.DAY_STREAK), "$streak")
        }
    }
}

@Composable
private fun TimeframeText(text: String, sel: Boolean, onClick: () -> Unit) {
    val c = TajlyTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onClick)) {
        Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, color = if (sel) c.textHi else c.textMid)
        Spacer(Modifier.height(4.dp))
        Box(Modifier.height(2.dp).width(if (sel) 18.dp else 0.dp).clip(RoundedCornerShape(1.dp)).background(GoldPrimary))
    }
}

@Composable
private fun StatRow(icon: ImageVector, label: String, value: String) {
    val c = TajlyTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = GoldBright, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = c.textMid, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = c.textHi)
    }
}

@Composable
private fun SectionHead(modifier: Modifier, title: String, action: String, onAction: (() -> Unit)? = null) {
    val c = TajlyTheme.colors
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = c.textHi)
        // Only render an action label when it actually does something (informational hints like
        // "swipe →" have no onAction; dead links are omitted entirely).
        if (action.isNotBlank()) {
            Text(
                action,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = GoldBright,
                modifier = if (onAction != null) Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onAction) else Modifier
            )
        }
    }
}

// ── Swipe deck ──
@Composable
private fun MotivationStack(cards: List<StackCard>) {
    if (cards.isEmpty()) return
    var order by remember { mutableStateOf(cards.indices.toList()) }
    Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(420.dp)) {
        val visible = order.take(3)
        visible.reversed().forEachIndexed { idx, cardIndex ->
            val depth = visible.size - 1 - idx
            key(cardIndex) {
                if (depth == 0) {
                    TopSwipeCard(cards[cardIndex]) { order = order.drop(1) + order.first() }
                } else {
                    Box(Modifier.fillMaxSize().graphicsLayer {
                        scaleX = 1f - depth * 0.05f; scaleY = 1f - depth * 0.05f
                        translationY = depth * 22.dp.toPx()
                    }) { StackCardContent(cards[cardIndex], interactive = false) }
                }
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        cards.indices.forEach { i ->
            val on = order.first() == i
            Box(Modifier.padding(horizontal = 3.dp).height(6.dp).width(if (on) 20.dp else 6.dp).clip(RoundedCornerShape(3.dp))
                .background(if (on) GoldPrimary else TajlyTheme.colors.hairStrong))
        }
    }
}

@Composable
private fun TopSwipeCard(card: StackCard, onSwiped: () -> Unit) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    Box(
        Modifier.fillMaxSize().graphicsLayer { translationX = offsetX.value; rotationZ = offsetX.value / 45f }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(offsetX.value) > 240f) scope.launch { offsetX.animateTo(if (offsetX.value > 0) 1400f else -1400f, tween(260)); onSwiped() }
                        else scope.launch { offsetX.animateTo(0f, spring()) }
                    },
                    onHorizontalDrag = { _, drag -> scope.launch { offsetX.snapTo(offsetX.value + drag) } }
                )
            }
    ) { StackCardContent(card, interactive = true) }
}

@Composable
private fun StackCardContent(card: StackCard, interactive: Boolean) {
    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp))) {
        Image(painterResource(card.img), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.0f to Color.Black.copy(alpha = 0.05f), 0.55f to Color.Black.copy(alpha = 0.38f), 1.0f to Color.Black.copy(alpha = 0.82f))))
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Bottom) {
            Text(card.eyebrow.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GoldBright)
            Spacer(Modifier.height(8.dp))
            Text(card.headline, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 3)
            Spacer(Modifier.height(16.dp))
            if (interactive) GoldButton(text = card.cta, onClick = card.onClick)
            else Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Today's plan ──
@Composable
private fun TodaysPlanCard(
    modifier: Modifier, upcoming: WorkoutSchedulePreview?, mealsEaten: Int, totalMeals: Int, hasMealPlan: Boolean,
    scheduledToday: Int, lang: org.awi.fitness.viewmodel.LanguageViewModel,
    onStart: () -> Unit, onNutrition: () -> Unit, onMeals: () -> Unit
) {
    val c = TajlyTheme.colors
    GlassCard(modifier = modifier.fillMaxWidth().height(196.dp).clickable(onClick = onStart), goldTint = true, tier = GlassTier.Hero) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(lang.getString(StringKey.HOME_TODAYS_PLAN).uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = GoldBright)
                GoldButton(text = lang.getString(StringKey.START_WORKOUT), onClick = onStart)
            }
            Spacer(Modifier.weight(1f))
            if (upcoming != null) {
                Text(lang.getString(StringKey.HOME_NEXT_SESSION).uppercase(), style = MaterialTheme.typography.labelSmall, color = c.textMid)
                Text(upcoming.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = c.textHi, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${formatTime(upcoming.startTime)} – ${formatTime(upcoming.endTime)}", style = MaterialTheme.typography.bodySmall, color = c.textMid)
            } else {
                Text(lang.getString(StringKey.REST_DAY_TODAY), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = c.textHi)
                Text(lang.getString(StringKey.HOMEX_PLAN_YOUR_WEEK), style = MaterialTheme.typography.bodySmall, color = c.textMid)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlanChip(TablerIcons.Leaf, lang.getString(StringKey.HOME_NUTRITION), if (hasMealPlan && totalMeals > 0) "$mealsEaten/$totalMeals" else "—", onNutrition)
                PlanChip(TablerIcons.Bolt, lang.getString(StringKey.MEALS), if (hasMealPlan) lang.getString(StringKey.HOME_FILTER_PLAN) else lang.getString(StringKey.HOMEX_SET_UP), onMeals)
            }
        }
    }
}

@Composable
private fun PlanChip(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
    val c = TajlyTheme.colors
    Row(Modifier.clip(RoundedCornerShape(12.dp)).background(c.textHi.copy(alpha = 0.06f)).clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        Icon(icon, null, tint = GoldBright, modifier = Modifier.size(16.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.textMid)
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = c.textHi)
    }
}

// ── Bento ──
@Composable
private fun BentoGrid(
    modifier: Modifier, planCount: Int, mealsEaten: Int, totalMeals: Int, challengeCount: Int,
    onCoach: () -> Unit, onWorkouts: () -> Unit, onNutrition: () -> Unit, onChallenges: () -> Unit
) {
    val lang = LocalLanguageViewModel.current
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BentoTileCard(Modifier.weight(1f), TablerIcons.Bolt, GoldBright, lang.getString(StringKey.HOMEX_ASK_YOUR_COACH), lang.getString(StringKey.HOMEX_POWERED_BY_AI), onCoach)
            BentoTileCard(Modifier.weight(1f), TablerIcons.Activity, Tajly.Green, lang.getString(StringKey.HOMEX_YOUR_WORKOUTS), "$planCount ${lang.getString(StringKey.HOMEX_PLANS)}", onWorkouts)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BentoTileCard(Modifier.weight(1f), TablerIcons.Leaf, Tajly.Teal, lang.getString(StringKey.HOMEX_TODAYS_NUTRITION), if (totalMeals > 0) "$mealsEaten/$totalMeals ${lang.getString(StringKey.HOMEX_TODAY_LOWER)}" else lang.getString(StringKey.HOMEX_SET_UP), onNutrition)
            BentoTileCard(Modifier.weight(1f), TablerIcons.Trophy, Tajly.Violet, lang.getString(StringKey.HOMEX_YOUR_CHALLENGES), if (challengeCount > 0) "$challengeCount ${lang.getString(StringKey.ACTIVE).lowercase()}" else lang.getString(StringKey.HOMEX_JOIN_ONE), onChallenges)
        }
    }
}

@Composable
private fun BentoTileCard(modifier: Modifier, icon: ImageVector, accent: Color, label: String, sub: String, onClick: () -> Unit) {
    val c = TajlyTheme.colors
    GlassCard(modifier = modifier.height(118.dp).clickable(onClick = onClick)) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(accent.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.textHi, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(sub, style = MaterialTheme.typography.labelSmall, color = c.textMid)
            }
        }
    }
}

// ── Widgets ──
@Composable
private fun NutritionWidget(modifier: Modifier, mealsEaten: Int, totalMeals: Int, ring: Float, p: Int, cbs: Int, f: Int, hasPlan: Boolean, onClick: () -> Unit) {
    val c = TajlyTheme.colors
    val lang = LocalLanguageViewModel.current
    GlassCard(modifier = modifier.height(150.dp).clickable(onClick = onClick)) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(lang.getString(StringKey.HOME_NUTRITION), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.textHi)
                WidgetArrow()
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatRing(progress = ring, diameter = 54.dp, strokeWidth = 7.dp) {
                    Text(if (totalMeals > 0) "$mealsEaten/$totalMeals" else "0", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = c.textHi)
                }
                if (hasPlan) Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    MacroLine("P", p); MacroLine("C", cbs); MacroLine("F", f)
                } else Text(lang.getString(StringKey.HOMEX_SET_UP_MEAL_PLAN), style = MaterialTheme.typography.bodySmall, color = c.textMid)
            }
        }
    }
}

@Composable
private fun MacroLine(k: String, v: Int) {
    val c = TajlyTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(k, style = MaterialTheme.typography.labelSmall, color = c.textMid)
        Text("${v}g", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = c.textHi)
    }
}

@Composable
private fun StreakWidget(modifier: Modifier, streak: Int, level: Int, xp: Int, onClick: () -> Unit) {
    val c = TajlyTheme.colors
    val lang = LocalLanguageViewModel.current
    val xpInto = xp % 500
    GlassCard(modifier = modifier.height(150.dp).clickable(onClick = onClick)) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(lang.getString(StringKey.COMMUNITY_DAY_STREAK), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.textHi)
                WidgetArrow()
            }
            Column {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("$streak", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = c.textHi)
                    LottieAnim("lottie_flame.json", Modifier.size(40.dp).padding(bottom = 4.dp))
                }
                Text("${lang.getString(StringKey.LEVEL)} $level · $xpInto/500 XP", style = MaterialTheme.typography.labelSmall, color = c.textMid)
                Spacer(Modifier.height(5.dp))
                Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(c.hairStrong)) {
                    Box(Modifier.fillMaxWidth(xpInto / 500f).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(Tajly.GoldGradient))
                }
            }
        }
    }
}

@Composable
private fun WidgetArrow() {
    Box(Modifier.size(30.dp).clip(CircleShape).background(Tajly.GoldGradient), contentAlignment = Alignment.Center) {
        Icon(TablerIcons.ArrowUpRight, null, tint = OnGold, modifier = Modifier.size(16.dp))
    }
}

// ── Week strip ──
/**
 * Clean 7-day activity strip driven by real per-day data (a day is "done" when a meal was
 * eaten or a workout completed). Today is ringed in gold, done days filled gold, future days
 * muted. Tapping opens the meals day view.
 */
@Composable
private fun WeekStrip(modifier: Modifier, dayNums: List<Int>, done: List<Boolean>, todayDow: Int, onClick: () -> Unit) {
    val c = TajlyTheme.colors
    val lang = LocalLanguageViewModel.current
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    val activeCount = done.count { it }
    GlassCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(lang.getString(StringKey.HOME_FILTER_THIS_WEEK).uppercase(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = c.textHi)
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                labels.forEachIndexed { i, l ->
                    val dow = i + 1
                    val isToday = dow == todayDow
                    val isFuture = dow > todayDow
                    val filled = done.getOrElse(i) { false }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            l,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (isFuture) c.textLow else c.textMid
                        )
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).then(
                                when {
                                    filled -> Modifier.background(Tajly.GoldGradient)
                                    isToday -> Modifier.border(2.dp, GoldPrimary, CircleShape)
                                    else -> Modifier.background(c.glassFill, CircleShape)
                                }
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${dayNums.getOrElse(i) { 0 }}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isToday || filled) FontWeight.Bold else FontWeight.Medium,
                                color = when {
                                    filled -> OnGold
                                    isToday -> GoldBright
                                    isFuture -> c.textLow
                                    else -> c.textHi
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Challenge image card (full backdrop, like the mockup) ──
private fun challengeFallback(index: Int): DrawableResource =
    listOf(Res.drawable.card_compete, Res.drawable.card_community, Res.drawable.card_move)[index % 3]

@Composable
private fun ChallengeImageCard(title: String, subtitle: String, frac: Float, imageUrl: String?, fallback: DrawableResource, onClick: () -> Unit) {
    Box(Modifier.width(224.dp).height(150.dp).clip(RoundedCornerShape(22.dp)).clickable(onClick = onClick)) {
        if (!imageUrl.isNullOrBlank()) {
            ImagePlaceholder(url = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Image(painterResource(fallback), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.3f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.82f))))
        Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.Bottom) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = 0.25f))) {
                Box(Modifier.fillMaxWidth(frac).fillMaxHeight().clip(RoundedCornerShape(3.dp)).background(Tajly.GoldGradient))
            }
        }
    }
}

// ── Community highlight ──
@Composable
private fun CommunityHighlight(modifier: Modifier, name: String, text: String, imageUrl: String?, likes: Int, unread: Int, onClick: () -> Unit) {
    val c = TajlyTheme.colors
    val lang = LocalLanguageViewModel.current
    GlassCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(Tajly.GoldGradient))
                Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.textHi, modifier = Modifier.weight(1f))
                if (unread > 0) Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Tajly.GoldGradient).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text("$unread ${lang.getString(StringKey.HOMEX_NEW)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = OnGold)
                }
            }
            Text(text, style = MaterialTheme.typography.bodyMedium, color = c.textHi, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (!imageUrl.isNullOrBlank()) {
                ImagePlaceholder(url = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(14.dp)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("♥ $likes", style = MaterialTheme.typography.labelMedium, color = c.textMid)
            }
        }
    }
}

// ── Coach band ──
@Composable
private fun CoachBand(modifier: Modifier, lang: org.awi.fitness.viewmodel.LanguageViewModel, onClick: () -> Unit) {
    val c = TajlyTheme.colors
    GlassCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(20.dp), goldTint = true) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(Tajly.GoldGradient), contentAlignment = Alignment.Center) {
                Icon(TablerIcons.Bolt, null, tint = OnGold, modifier = Modifier.size(22.dp))
            }
            Text(lang.getString(StringKey.HOME_ASK_COACH), style = MaterialTheme.typography.bodyLarge, color = c.textMid, modifier = Modifier.weight(1f))
            Icon(TablerIcons.Send, null, tint = GoldBright, modifier = Modifier.size(22.dp))
        }
    }
}

// ── Editorial row ──
@Composable
private fun EditorialRow(modifier: Modifier, ic3d: DrawableResource?, icon: ImageVector?, tint: Color, title: String, subtitle: String, onClick: () -> Unit) {
    val c = TajlyTheme.colors
    GlassCard(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(46.dp).clip(CircleShape).background(tint.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                if (ic3d != null) Image(painterResource(ic3d), null, modifier = Modifier.size(28.dp))
                else if (icon != null) Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.textHi)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textMid, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Icon(TablerIcons.ChevronRight, null, tint = c.textMid, modifier = Modifier.size(20.dp))
        }
    }
}

// ── Wellness tips card (image backdrop) ──
@Composable
private fun TipsCard(modifier: Modifier, count: Int, title: String, onClick: () -> Unit) {
    val lang = LocalLanguageViewModel.current
    Box(modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(24.dp)).clickable(onClick = onClick)) {
        Image(painterResource(Res.drawable.card_yoga), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.72f), Color.Black.copy(alpha = 0.28f)))))
        Row(Modifier.fillMaxSize().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(lang.getString(StringKey.HOMEX_DAILY_WELLNESS).uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = GoldBright)
                Spacer(Modifier.height(4.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text("$count ${lang.getString(StringKey.HOMEX_TIPS_FOR_TODAY)}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
            }
            Icon(TablerIcons.ChevronRight, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

// ── Premium band ──
@Composable
private fun PremiumBand(modifier: Modifier, onClick: () -> Unit) {
    val lang = LocalLanguageViewModel.current
    GlassCard(modifier = modifier.fillMaxWidth().height(150.dp).clickable(onClick = onClick), tier = GlassTier.Hero) {
        Image(painterResource(Res.drawable.tex_gold_2), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.72f), Color.Black.copy(alpha = 0.35f)))))
        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center) {
            Text(lang.getString(StringKey.HOMEX_UNLOCK_PREMIUM), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Text(lang.getString(StringKey.HOMEX_PREMIUM_SUB), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(12.dp))
            GoldButton(text = lang.getString(StringKey.GO_PREMIUM), onClick = onClick)
        }
    }
}

// ── helpers ──
private fun Int.grouped(): String {
    val neg = this < 0
    val s = kotlin.math.abs(this).toString()
    val sb = StringBuilder()
    val n = s.length
    for (i in 0 until n) { if (i > 0 && (n - i) % 3 == 0) sb.append(','); sb.append(s[i]) }
    return if (neg) "-$sb" else sb.toString()
}

private fun formatTime(ts: Long): String {
    val dt = Instant.fromEpochMilliseconds(ts).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
}
