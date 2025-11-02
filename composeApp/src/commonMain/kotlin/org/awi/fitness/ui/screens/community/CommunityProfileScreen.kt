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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Heart
import compose.icons.tablericons.MessageCircle
import compose.icons.tablericons.Photo
import compose.icons.tablericons.Share
import compose.icons.tablericons.Trophy
import compose.icons.tablericons.UserPlus
import org.awi.fitness.model.Badge
import org.awi.fitness.model.CommunityPost
import org.awi.fitness.theme.GreenAccent
import org.awi.fitness.theme.TextGray
import org.awi.fitness.ui.components.statusBarPadding
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.ui.components.FitnessCard
import org.awi.fitness.viewmodel.CommunityViewModel

class CommunityProfileScreen(private val userId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { CommunityViewModel() }
        val profileState by viewModel.profileState.collectAsState()
        
        var selectedTabIndex by remember { mutableStateOf(0) }
        val tabs = listOf("Achievements", "Photos", "Challenges")
        
        LaunchedEffect(userId) {
            viewModel.loadUserProfile(userId)
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = profileState.user?.name ?: "Profile",
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
            if (profileState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (profileState.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profileState.error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                profileState.user?.let { user ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // Profile header
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                GreenAccent.copy(alpha = 0.7f),
                                                MaterialTheme.colorScheme.background
                                            )
                                        )
                                    )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp)
                                ) {
                                    // Profile image
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
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
                                                    style = MaterialTheme.typography.headlineLarge
                                                )
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    
                                    Text(
                                        text = user.username,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextGray
                                    )
                                }
                            }
                        }
                        
                        // Stats row
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatBox(
                                    value = user.streakDays.toString(),
                                    label = "Day Streak",
                                    icon = "🔥"
                                )
                                
                                Divider(
                                    modifier = Modifier
                                        .height(40.dp)
                                        .width(1.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                )
                                
                                StatBox(
                                    value = user.level.toString(),
                                    label = "Rank Level",
                                    icon = "🏆"
                                )
                            }
                        }
                        
                        // Badges section
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "Badges",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                if (user.badges.isEmpty()) {
                                    Text(
                                        text = "No badges yet",
                                        color = TextGray,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        items(user.badges) { badge ->
                                            BadgeItem(badge = badge)
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Follow button
                        item {
                            var isFollowing by remember { mutableStateOf(user.isFollowing) }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isFollowing) {
                                    Button(
                                        onClick = { isFollowing = false },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    ) {
                                        Icon(
                                            imageVector = TablerIcons.UserPlus,
                                            contentDescription = "Following",
                                            tint = GreenAccent
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Following")
                                    }
                                } else {
                                    Button(
                                        onClick = { isFollowing = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GreenAccent,
                                            contentColor = Color.Black
                                        ),
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    ) {
                                        Icon(
                                            imageVector = TablerIcons.UserPlus,
                                            contentDescription = "Follow"
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Follow")
                                    }
                                }
                            }
                        }
                        
                        // Tabs
                        item {
                            TabRow(
                                selectedTabIndex = selectedTabIndex,
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ) {
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTabIndex == index,
                                        onClick = { selectedTabIndex = index },
                                        text = {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (selectedTabIndex == index) 
                                                        FontWeight.Bold 
                                                    else 
                                                        FontWeight.Normal
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Tab content
                        when (selectedTabIndex) {
                            0 -> {
                                // Achievements tab (posts)
                                if (profileState.userPosts.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No posts yet",
                                                color = TextGray
                                            )
                                        }
                                    }
                                } else {
                                    items(profileState.userPosts) { post ->
                                        PostItem(
                                            post = post,
                                            onPostClick = {
                                                navigator.push(PostDetailScreen(post.id))
                                            }
                                        )
                                    }
                                }
                            }
                            1 -> {
                                // Photos tab
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No photos yet",
                                            color = TextGray
                                        )
                                    }
                                }
                            }
                            2 -> {
                                // Challenges tab
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No challenges yet",
                                            color = TextGray
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

@Composable
fun StatBox(
    value: String,
    label: String,
    icon: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray
        )
    }
}

@Composable
fun BadgeItem(badge: Badge) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            if (badge.iconName != null) {
                ImagePlaceholder(
                    url = badge.iconName,
                    contentDescription = badge.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = when (badge.name) {
                        "5K Finisher", "5K Runner" -> TablerIcons.Trophy
                        "Early Riser" -> TablerIcons.Photo
                        "New PR" -> TablerIcons.Trophy
                        "Cardio King" -> TablerIcons.Heart
                        else -> TablerIcons.Trophy
                    },
                    contentDescription = badge.name,
                    tint = GreenAccent,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = badge.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
fun PostItem(
    post: CommunityPost,
    onPostClick: () -> Unit
) {
    FitnessCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onPostClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Post image if available
            if (post.imageUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    ImagePlaceholder(
                        url = post.imageUrl,
                        contentDescription = "Post Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Workout stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                post.calories?.let {
                    WorkoutStat(
                        icon = "🔥",
                        value = "Calories: $it kcal"
                    )
                }
                
                post.steps?.let {
                    WorkoutStat(
                        icon = "👣",
                        value = "Steps: $it"
                    )
                }
                
                post.duration?.let {
                    val formattedTime = org.awi.fitness.utils.DateUtils.formatSeconds(it)
                    WorkoutStat(
                        icon = "⏱️",
                        value = "Time: $formattedTime"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = TablerIcons.Heart,
                        contentDescription = "Likes",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "${post.likes}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = TablerIcons.MessageCircle,
                        contentDescription = "Comments",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "${post.comments}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = TablerIcons.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
