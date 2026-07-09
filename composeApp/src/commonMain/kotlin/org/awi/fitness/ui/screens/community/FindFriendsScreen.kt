package org.awi.fitness.ui.screens.community
import org.awi.fitness.data.StringKey
import org.awi.fitness.viewmodel.LocalLanguageViewModel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Check
import compose.icons.tablericons.Search
import compose.icons.tablericons.UserPlus
import kotlinx.coroutines.launch
import org.awi.fitness.model.CommunityUser
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.ui.components.statusBarPadding
import org.awi.fitness.viewmodel.CommunityViewModel

class FindFriendsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { CommunityViewModel() }
        val findFriendsState by viewModel.findFriendsState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        val c = TajlyTheme.colors

        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("All", "Friends", "Suggested")

        LaunchedEffect(Unit) {
            viewModel.loadSuggestedUsers()
        }

        LaunchedEffect(selectedTab) {
            val filter = when (selectedTab) {
                0 -> "all"
                1 -> "friends"
                2 -> "suggested"
                else -> "all"
            }
            viewModel.updateFriendsTab(filter)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(c.bg),
        ) {
            // Community section glow (teal) behind the header.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Tajly.sectionGlow(Tajly.Teal)),
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // ---- Glass top bar ----
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarPadding()
                        .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlassIconButton(onClick = { navigator.pop() }) {
                        Icon(
                            TablerIcons.ArrowLeft,
                            contentDescription = "Back",
                            tint = c.textHi,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = LocalLanguageViewModel.current.getString(StringKey.FIND_FRIENDS),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi,
                        modifier = Modifier.weight(1f),
                    )
                }

                // ---- Search — the focal element ----
                TextField(
                    value = findFriendsState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = {
                        Text(LocalLanguageViewModel.current.getString(StringKey.CMX_SEARCH_FOR_FRIENDS), color = c.textLow)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = TablerIcons.Search,
                            contentDescription = "Search",
                            tint = c.textMid,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = c.s2,
                        unfocusedContainerColor = c.s2,
                        disabledContainerColor = c.s2,
                        focusedTextColor = c.textHi,
                        unfocusedTextColor = c.textHi,
                        cursorColor = GoldPrimary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        // Run the real, full-collection Firestore search.
                        coroutineScope.launch { viewModel.performUserSearch() }
                    })
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ---- Slim gold-underline tabs (hidden while searching) ----
                AnimatedVisibility(
                    visible = findFriendsState.searchQuery.isBlank(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        tabs.forEachIndexed { index, title ->
                            UnderlineTab(
                                text = title,
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when {
                    findFriendsState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GoldPrimary)
                        }
                    }

                    findFriendsState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = findFriendsState.error ?: "Unknown error",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    else -> {
                        // Search results
                        AnimatedVisibility(
                            visible = findFriendsState.searchQuery.isNotBlank(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            if (findFriendsState.searchResults.isEmpty()) {
                                FriendsEmpty("${LocalLanguageViewModel.current.getString(StringKey.NO_USERS_FOUND_MATCHING)} '${findFriendsState.searchQuery}'")
                            } else {
                                UserList(
                                    users = findFriendsState.searchResults,
                                    onUserClick = { navigator.push(CommunityProfileScreen(it.id)) },
                                    onFollowClick = { user ->
                                        coroutineScope.launch { viewModel.toggleFollowUser(user.id) }
                                    },
                                )
                            }
                        }

                        // Tab content
                        AnimatedVisibility(
                            visible = findFriendsState.searchQuery.isBlank(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            val users = when (findFriendsState.activeTab) {
                                "friends" -> findFriendsState.suggestedUsers.filter { it.isFollowing }
                                "suggested" -> findFriendsState.suggestedUsers.filter { !it.isFollowing }
                                else -> findFriendsState.suggestedUsers
                            }
                            val emptyMessage = when (findFriendsState.activeTab) {
                                "friends" -> LocalLanguageViewModel.current.getString(StringKey.NOT_FOLLOWING_ANYONE_YET)
                                "suggested" -> LocalLanguageViewModel.current.getString(StringKey.NO_SUGGESTED_USERS)
                                else -> LocalLanguageViewModel.current.getString(StringKey.NO_USERS_FOUND)
                            }
                            if (users.isEmpty()) {
                                FriendsEmpty(emptyMessage)
                            } else {
                                UserList(
                                    users = users,
                                    onUserClick = { navigator.push(CommunityProfileScreen(it.id)) },
                                    onFollowClick = { user ->
                                        coroutineScope.launch { viewModel.toggleFollowUser(user.id) }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Slim gold-underline tab (matches the home hero timeframe selector). */
@Composable
private fun UnderlineTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) c.textHi else c.textMid,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(if (selected) 18.dp else 0.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(GoldPrimary),
        )
    }
}

@Composable
private fun FriendsEmpty(message: String) {
    val c = TajlyTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = c.textMid)
    }
}

@Composable
private fun UserList(
    users: List<CommunityUser>,
    onUserClick: (CommunityUser) -> Unit,
    onFollowClick: (CommunityUser) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(users, key = { it.id }) { user ->
            UserCard(
                user = user,
                onUserClick = { onUserClick(user) },
                onFollowClick = { onFollowClick(user) },
            )
        }
    }
}

@Composable
private fun GlassIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .pressScale(interaction)
            .size(40.dp)
            .clip(CircleShape)
            .background(c.glassFill, CircleShape)
            .border(1.dp, c.hairStrong, CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
fun UserCard(
    user: CommunityUser,
    onUserClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    val c = TajlyTheme.colors
    val cardInteraction = remember { MutableInteractionSource() }
    GlassCard(
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(cardInteraction)
            .clickable(interactionSource = cardInteraction, indication = null, onClick = onUserClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar (reuse community avatar — gold initial fallback)
            CommunityAvatar(
                name = user.name,
                imageUrl = user.profileImage,
                size = 56.dp,
                onClick = onUserClick,
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Identity + compact stats (grouped, demoted under name)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi,
                    maxLines = 1,
                )
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatItem(value = user.streakDays.toString(), label = "streak", icon = "🔥")
                    Spacer(modifier = Modifier.width(14.dp))
                    StatItem(value = user.level.toString(), label = "lvl", icon = "🏆")
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Primary action: Follow = gold; Following = demoted glass pill.
            FollowPill(isFollowing = user.isFollowing, onClick = onFollowClick)
        }
    }
}

@Composable
private fun FollowPill(isFollowing: Boolean, onClick: () -> Unit) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val base = Modifier
        .pressScale(interaction)
        .clip(CircleShape)
    val styled = if (isFollowing) {
        base
            .background(c.glassFill, CircleShape)
            .border(1.dp, c.hairStrong, CircleShape)
    } else {
        base.background(Tajly.GoldGradient)
    }
    Row(
        modifier = styled
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = if (isFollowing) TablerIcons.Check else TablerIcons.UserPlus,
            contentDescription = if (isFollowing) "Following" else "Follow",
            tint = if (isFollowing) c.textMid else OnGold,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = if (isFollowing) "Following" else "Follow",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (isFollowing) c.textMid else OnGold,
        )
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    icon: String
) {
    val c = TajlyTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = c.textHi,
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = c.textMid
        )
    }
}
