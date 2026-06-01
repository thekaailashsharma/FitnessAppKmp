package org.awi.fitness.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import org.awi.fitness.theme.GreenAccent
import org.awi.fitness.ui.components.BuddyFab
import org.awi.fitness.ui.screens.community.CommunityFeedScreen
import org.awi.fitness.ui.screens.home.HomeScreen
import org.awi.fitness.viewmodel.*

// 4 tabs: Home · Train · Community · Profile
// Challenges, Meals, Discover live inside their parent tabs or Profile
private enum class Tab(val icon: ImageVector, val label: String) {
    HOME(TablerIcons.Home, "Home"),
    TRAIN(TablerIcons.Run, "Train"),
    COMMUNITY(TablerIcons.Users, "Community"),
    PROFILE(TablerIcons.User, "Profile"),
}

class MainScreen : Screen {
    @Composable
    override fun Content() {
        var currentTab by rememberSaveable { mutableStateOf(Tab.HOME) }
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val langVm = ViewModelStore.language
        val communityViewModel = ViewModelStore.community
        val activityState by communityViewModel.activityState.collectAsState()
        val unreadCount = activityState.unreadCount

        LaunchedEffect(currentTab) {
            if (currentTab == Tab.COMMUNITY) {
                communityViewModel.loadActivityNotifications()
            }
        }

        CompositionLocalProvider(
            LocalHomeViewModel provides ViewModelStore.home,
            LocalWorkoutViewModel provides ViewModelStore.workout,
            LocalMealPlanViewModel provides ViewModelStore.mealPlan,
            LocalArticleViewModel provides ViewModelStore.article,
            LocalLanguageViewModel provides ViewModelStore.language
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                floatingActionButton = {
                    if (currentTab != Tab.COMMUNITY) {
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
                },
                bottomBar = {
                    TajlyBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        unreadCount = unreadCount
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (currentTab) {
                        Tab.HOME -> HomeScreen().Content()
                        Tab.TRAIN -> TrainTabScreen(navigator = navigator)
                        Tab.COMMUNITY -> CommunityFeedScreen().Content()
                        Tab.PROFILE -> ProfileTabScreen(langVm)
                    }
                }
            }
        }
    }
}

@Composable
private fun TajlyBottomBar(
    currentTab: Tab,
    onTabSelected: (Tab) -> Unit,
    unreadCount: Int = 0,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Tab.entries.forEach { tab ->
                TajlyTabItem(
                    tab = tab,
                    isSelected = currentTab == tab,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.weight(1f),
                    badgeCount = if (tab == Tab.COMMUNITY) unreadCount else 0
                )
            }
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
                        if (isSelected) GreenAccent.copy(alpha = 0.12f)
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
                            tint = if (isSelected) GreenAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSelected) GreenAccent else MaterialTheme.colorScheme.onSurfaceVariant,
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
                color = if (isSelected) GreenAccent else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(indicatorWidth)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) GreenAccent else androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    }
}

// Train tab: Workouts / Meals / Challenges with a clean TabRow
// Uses Box+visibility so each sub-screen is kept alive (no state loss on tab switch)
@Composable
private fun TrainTabScreen(navigator: cafe.adriel.voyager.navigator.Navigator) {
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Workouts", "Meals", "Challenges")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = GreenAccent,
            indicator = { tabPositions ->
                if (selectedIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = GreenAccent
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
                    selectedContentColor = GreenAccent,
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
            // Challenges
            if (selectedIndex == 2) ChallengesScreen()
        }
    }
}

// Profile tab — wraps the existing ProfileScreen without requiring back navigation
@Composable
private fun ProfileTabScreen(langVm: org.awi.fitness.viewmodel.LanguageViewModel) {
    ProfileScreen(languageViewModel = langVm).Content()
}
