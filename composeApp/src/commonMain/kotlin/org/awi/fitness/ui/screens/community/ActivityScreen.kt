package org.awi.fitness.ui.screens.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Bell
import compose.icons.tablericons.Heart
import compose.icons.tablericons.MessageCircle
import compose.icons.tablericons.Star
import compose.icons.tablericons.Trophy
import compose.icons.tablericons.UserPlus
import kotlinx.coroutines.delay
import org.awi.fitness.model.ActivityNotification
import org.awi.fitness.model.CommunityActivityType
import org.awi.fitness.theme.GreenAccent
import org.awi.fitness.theme.TextGray
import org.awi.fitness.ui.components.statusBarPadding
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.utils.DateUtils
import org.awi.fitness.viewmodel.CommunityViewModel
import org.awi.fitness.viewmodel.ViewModelStore

class ActivityScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { CommunityViewModel() }
        val activityState by viewModel.activityState.collectAsState()
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            viewModel.loadActivityNotifications()
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Activity",
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFFEFBE6).copy(alpha = 0.1f))
            ) {
                if (activityState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (activityState.error != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activityState.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else if (activityState.notifications.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No activity yet",
                            color = TextGray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                    ) {
                        items(activityState.notifications) { notification ->
                            NotificationItem(
                                notification = notification,
                                viewModel = viewModel,
                                onNotificationClick = {
                                    scope.launch {
                                        viewModel.markNotificationAsRead(notification.id)
                                    }
                                    when (notification.type) {
                                        CommunityActivityType.LIKE, CommunityActivityType.COMMENT -> {
                                            if (notification.targetId.isNotBlank()) {
                                                navigator.push(PostDetailScreen(notification.targetId))
                                            }
                                        }
                                        CommunityActivityType.FOLLOW -> {
                                            if (notification.userId.isNotBlank()) {
                                                navigator.push(CommunityProfileScreen(notification.userId))
                                            }
                                        }
                                        else -> { /* badge/streak/challenge — no nav needed */ }
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

@Composable
fun NotificationItem(
    notification: ActivityNotification,
    viewModel: CommunityViewModel,
    onNotificationClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNotificationClick() }
            .background(
                if (!notification.isRead) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
                else 
                    Color.Transparent
            )
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image or icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (notification.type) {
                            CommunityActivityType.BADGE_EARNED, CommunityActivityType.STREAK_MILESTONE -> GreenAccent
                            else -> if (notification.userProfileImage == null) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                Color.Transparent
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (notification.userProfileImage != null) {
                    ImagePlaceholder(
                        url = notification.userProfileImage,
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = when (notification.type) {
                            CommunityActivityType.LIKE -> TablerIcons.Heart
                            CommunityActivityType.COMMENT -> TablerIcons.MessageCircle
                            CommunityActivityType.FOLLOW -> TablerIcons.UserPlus
                            CommunityActivityType.CHALLENGE_INVITE -> TablerIcons.Trophy
                            CommunityActivityType.BADGE_EARNED -> TablerIcons.Star
                            CommunityActivityType.STREAK_MILESTONE -> TablerIcons.Bell
                        },
                        contentDescription = null,
                        tint = when (notification.type) {
                            CommunityActivityType.BADGE_EARNED, CommunityActivityType.STREAK_MILESTONE -> Color.Black
                            else -> MaterialTheme.colorScheme.onPrimary
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Notification content
                when (notification.type) {
                    CommunityActivityType.LIKE -> {
                        Text(
                            text = "${notification.userName} cheered your post",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        if (notification.targetContent.isNotBlank()) {
                            Text(
                                text = "\"${notification.targetContent}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 2
                            )
                        }
                    }
                    CommunityActivityType.COMMENT -> {
                        Text(
                            text = "${notification.userName} commented on your post",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        if (notification.targetContent.isNotBlank()) {
                            Text(
                                text = "\"${notification.targetContent}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 2
                            )
                        }
                    }
                    CommunityActivityType.FOLLOW -> {
                        Text(
                            text = "${notification.userName} started following you",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    CommunityActivityType.CHALLENGE_INVITE -> {
                        Text(
                            text = "You're invited to a challenge",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        if (notification.targetContent.isNotBlank()) {
                            Text(
                                text = notification.targetContent,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    CommunityActivityType.BADGE_EARNED -> {
                        Text(
                            text = "You've earned the '${notification.targetContent}' badge!",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Congratulations!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    CommunityActivityType.STREAK_MILESTONE -> {
                        Text(
                            text = "You're on a ${notification.targetContent}!",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Keep it up!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Time
                Text(
                    text = DateUtils.formatTimestampRelative(notification.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
                
                // Challenge buttons
                if (notification.type == CommunityActivityType.CHALLENGE_INVITE && !notification.isRead) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.markNotificationAsRead(notification.id)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Decline")
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    notification.targetId.takeIf { it.isNotBlank() }?.let { challengeId ->
                                        ViewModelStore.challenges.joinChallenge(challengeId)
                                    }
                                    viewModel.markNotificationAsRead(notification.id)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Accept")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Timestamp
            Text(
                text = DateUtils.formatTimestampShort(notification.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = TextGray
            )
        }
    }
    
    Divider(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
