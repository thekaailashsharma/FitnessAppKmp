package org.awi.fitness.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch
import org.awi.fitness.model.ConversationTrigger
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.ui.components.BuddyFab
import org.awi.fitness.ui.components.GlassTier
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.glassSource
import org.awi.fitness.ui.components.liquidGlass
import org.awi.fitness.ui.screens.community.CommunityFeedScreen
import org.awi.fitness.ui.screens.community.CreatePostScreen
import org.awi.fitness.ui.screens.home.HomeScreen
import org.awi.fitness.viewmodel.*

// Tabs: Home · Train · [ + ] · Challenges · Community
// Profile now lives in the Home header (tap the avatar top-right).
private enum class Tab(val icon: ImageVector, val label: String) {
    HOME(TablerIcons.Home, "Home"),
    TRAIN(TablerIcons.Run, "Train"),
    CHALLENGES(TablerIcons.Trophy, "Challenges"),
    COMMUNITY(TablerIcons.Users, "Community"),
}

class MainScreen : Screen {
    @Composable
    override fun Content() {
        var currentTab by rememberSaveable { mutableStateOf(Tab.HOME) }

        // The coach chat can request a tab switch after popping back here (e.g. a card's
        // "Start workout" opens the Train tab). We consume the request and clear it.
        LaunchedEffect(Unit) {
            org.awi.fitness.navigation.CoachNav.requestedTab.collect { req ->
                when (req) {
                    "HOME" -> currentTab = Tab.HOME
                    "TRAIN" -> currentTab = Tab.TRAIN
                    "CHALLENGES" -> currentTab = Tab.CHALLENGES
                    "COMMUNITY" -> currentTab = Tab.COMMUNITY
                }
                if (req != null) org.awi.fitness.navigation.CoachNav.requestedTab.value = null
            }
        }
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val communityViewModel = ViewModelStore.community
        val activityState by communityViewModel.activityState.collectAsState()
        val unreadCount = activityState.unreadCount

        LaunchedEffect(currentTab) {
            if (currentTab == Tab.COMMUNITY) {
                communityViewModel.loadActivityNotifications()
            }
        }

        val c = TajlyTheme.colors
        LaunchedEffect(Unit) { ViewModelStore.backdrop.refreshIfStale() }
        CompositionLocalProvider(
            LocalHomeViewModel provides ViewModelStore.home,
            LocalWorkoutViewModel provides ViewModelStore.workout,
            LocalMealPlanViewModel provides ViewModelStore.mealPlan,
            LocalArticleViewModel provides ViewModelStore.article,
            LocalLanguageViewModel provides ViewModelStore.language,
            org.awi.fitness.viewmodel.LocalBackdropViewModel provides ViewModelStore.backdrop
        ) {
            ProvideGlass {
                Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
                    // Content fills the whole screen and scrolls UNDER the glass bar.
                    Box(modifier = Modifier.fillMaxSize().glassSource()) {
                        when (currentTab) {
                            Tab.HOME -> HomeScreen().Content()
                            Tab.TRAIN -> TrainTabScreen(navigator = navigator)
                            Tab.CHALLENGES -> ChallengesScreen()
                            Tab.COMMUNITY -> CommunityFeedScreen().Content()
                        }
                    }

                    // Buddy FAB floats just above the glass bar (hidden on Community).
                    if (currentTab != Tab.COMMUNITY) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .navigationBarsPadding()
                                .padding(end = 18.dp, bottom = 92.dp)
                        ) {
                            BuddyFab(
                                onClick = {
                                    coroutineScope.launch {
                                        navigator.push(
                                            org.awi.fitness.ui.screens.avatar.AvatarScreen(ConversationTrigger.MANUAL)
                                        )
                                    }
                                }
                            )
                        }
                    }

                    // Floating liquid-glass bottom bar with a center Post action.
                    TajlyBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        onPost = { navigator.push(CreatePostScreen()) },
                        unreadCount = unreadCount,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
private fun TajlyBottomBar(
    currentTab: Tab,
    onTabSelected: (Tab) -> Unit,
    onPost: () -> Unit,
    unreadCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                // Pure liquid glass: floating capsule (corner == height/2) that blurs the
                // screen content behind it via Haze. GlassTier.Nav = least blur / most
                // see-through / highest float + specular rim, per Apple's Liquid Glass.
                .liquidGlass(RoundedCornerShape(33.dp), tier = GlassTier.Nav)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TajlyTabItem(Tab.HOME, currentTab == Tab.HOME, { onTabSelected(Tab.HOME) }, Modifier.weight(1f))
            TajlyTabItem(Tab.TRAIN, currentTab == Tab.TRAIN, { onTabSelected(Tab.TRAIN) }, Modifier.weight(1f))
            CenterPostButton(onClick = onPost, modifier = Modifier.weight(1f))
            TajlyTabItem(Tab.CHALLENGES, currentTab == Tab.CHALLENGES, { onTabSelected(Tab.CHALLENGES) }, Modifier.weight(1f))
            TajlyTabItem(Tab.COMMUNITY, currentTab == Tab.COMMUNITY, { onTabSelected(Tab.COMMUNITY) }, Modifier.weight(1f), badgeCount = unreadCount)
        }
    }
}

/** Center Post action — the single compose entry for the community. */
@Composable
private fun CenterPostButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Tajly.GoldGradient)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = TablerIcons.Plus,
                contentDescription = "Post",
                tint = OnGold,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun TajlyTabItem(
    tab: Tab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
) {
    val c = TajlyTheme.colors
    val indicatorWidth by animateDpAsState(
        targetValue = if (isSelected) 24.dp else 0.dp,
        animationSpec = tween(200),
        label = "indicator"
    )

    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) GoldBright.copy(alpha = 0.12f)
                        else androidx.compose.ui.graphics.Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (badgeCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge {
                                Text(
                                    text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (isSelected) GoldBright else c.textMid,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSelected) GoldBright else c.textMid,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = tab.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 10.sp
                ),
                color = if (isSelected) GoldBright else c.textMid
            )
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(indicatorWidth)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) GoldBright else androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    }
}

// Train tab: Workouts / Meals with a clean TabRow (Challenges is now a top-level tab)
// Uses Box+visibility so each sub-screen is kept alive (no state loss on tab switch)
@Composable
private fun TrainTabScreen(navigator: cafe.adriel.voyager.navigator.Navigator) {
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Workouts", "Meals")

    // Honor a requested sub-tab (e.g. Home "Nutrition" → Meals), then clear it.
    val requestedSub by org.awi.fitness.navigation.CoachNav.requestedTrainSubTab.collectAsState()
    LaunchedEffect(requestedSub) {
        requestedSub?.let {
            selectedIndex = it.coerceIn(0, tabs.lastIndex)
            org.awi.fitness.navigation.CoachNav.requestedTrainSubTab.value = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = GoldBright,
            indicator = { tabPositions ->
                if (selectedIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = GoldBright
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = GoldBright,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Keep all screens in composition so state is preserved on tab switch
        Box(modifier = Modifier.fillMaxSize()) {
            // Workouts
            Box(modifier = Modifier.fillMaxSize().then(
                if (selectedIndex == 0) Modifier else Modifier.size(0.dp)
            )) {
                if (selectedIndex == 0) WorkoutScreen().Content()
            }
            // Meals
            if (selectedIndex == 1) MealScreen().Content()
        }
    }
}
