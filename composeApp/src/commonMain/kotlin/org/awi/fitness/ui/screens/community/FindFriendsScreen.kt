package org.awi.fitness.ui.screens.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import org.awi.fitness.theme.GreenAccent
import org.awi.fitness.theme.TextGray
import org.awi.fitness.ui.components.statusBarPadding
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.ui.components.FitnessCard
import org.awi.fitness.viewmodel.CommunityViewModel

class FindFriendsScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { CommunityViewModel() }
        val findFriendsState by viewModel.findFriendsState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        
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
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Find Friends",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = TablerIcons.ArrowLeft,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.statusBarPadding()
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search bar
                TextField(
                    value = findFriendsState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = {
                        Text("Search for friends...")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = TablerIcons.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        // Handle search
                    })
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tabs
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        SegmentedButton(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = tabs.size
                            ),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primary,
                                activeContentColor = Color.Black,
                                inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                inactiveContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(title)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (findFriendsState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (findFriendsState.error != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = findFriendsState.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    // Search results
                    AnimatedVisibility(
                        visible = findFriendsState.searchQuery.isNotBlank(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (findFriendsState.searchResults.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No users found matching '${findFriendsState.searchQuery}'",
                                    color = TextGray
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(findFriendsState.searchResults) { user ->
                                    UserCard(
                                        user = user,
                                        onUserClick = {
                                            navigator.push(CommunityProfileScreen(user.id))
                                        },
                                        onFollowClick = {
                                            coroutineScope.launch {
                                                viewModel.followUser(user.id)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Tab content
                    AnimatedVisibility(
                        visible = findFriendsState.searchQuery.isBlank(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        when (findFriendsState.activeTab) {
                            "all" -> {
                                if (findFriendsState.suggestedUsers.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No users found",
                                            color = TextGray
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(findFriendsState.suggestedUsers) { user ->
                                            UserCard(
                                                user = user,
                                                onUserClick = {
                                                    navigator.push(CommunityProfileScreen(user.id))
                                                },
                                                onFollowClick = {
                                                    coroutineScope.launch {
                                                        viewModel.followUser(user.id)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            "friends" -> {
                                val friends = findFriendsState.suggestedUsers.filter { it.isFollowing }
                                if (friends.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "You're not following anyone yet",
                                            color = TextGray
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(friends) { user ->
                                            UserCard(
                                                user = user,
                                                onUserClick = {
                                                    navigator.push(CommunityProfileScreen(user.id))
                                                },
                                                onFollowClick = {
                                                    coroutineScope.launch {
                                                        viewModel.followUser(user.id)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            "suggested" -> {
                                val suggested = findFriendsState.suggestedUsers.filter { !it.isFollowing }
                                if (suggested.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No suggested users at the moment",
                                            color = TextGray
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(suggested) { user ->
                                            UserCard(
                                                user = user,
                                                onUserClick = {
                                                    navigator.push(CommunityProfileScreen(user.id))
                                                },
                                                onFollowClick = {
                                                    coroutineScope.launch {
                                                        viewModel.followUser(user.id)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(
    user: CommunityUser,
    onUserClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    FitnessCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUserClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // User info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile image
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                ) {
                    if (user.profileImage != null) {
                        ImagePlaceholder(
                            url = user.profileImage,
                            contentDescription = "Profile Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            showInitial = true,
                            initial = user.name
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.first().toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Stats row
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(
                            value = user.streakDays.toString(),
                            label = "Streak",
                            icon = "🔥"
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        StatItem(
                            value = user.level.toString(),
                            label = "Level",
                            icon = "🏆"
                        )
                    }
                }
                
                // Follow button
                if (user.isFollowing) {
                    Button(
                        onClick = { onFollowClick() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = TablerIcons.Check,
                            contentDescription = "Following",
                            tint = GreenAccent
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Following")
                    }
                } else {
                    Button(
                        onClick = { onFollowClick() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenAccent,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = TablerIcons.UserPlus,
                            contentDescription = "Follow"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Follow")
                    }
                }
            }
            
            // Badges section
            if (user.badges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Show main activity or badge
                val mainBadge = user.badges.first()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mainBadge.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Workout",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GreenAccent
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    icon: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextGray
        )
    }
}
