package org.awi.fitness.ui.screens.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Heart
import compose.icons.tablericons.Send
import compose.icons.tablericons.Share
import kotlinx.coroutines.launch

import org.awi.fitness.model.CommunityComment
import org.awi.fitness.theme.GreenAccent
import org.awi.fitness.theme.TextGray
import org.awi.fitness.ui.components.statusBarPadding
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.ui.components.FitnessCard
import org.awi.fitness.utils.DateUtils
import org.awi.fitness.viewmodel.CommunityViewModel

class PostDetailScreen(private val postId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { CommunityViewModel() }
        val postDetailState by viewModel.postDetailState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        
        LaunchedEffect(postId) {
            viewModel.loadPostDetail(postId)
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Post",
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
                if (postDetailState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (postDetailState.error != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = postDetailState.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    postDetailState.post?.let { post ->
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Post card
                            item {
                                FitnessCard(
                                    modifier = Modifier.fillMaxWidth()
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
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        navigator.push(CommunityProfileScreen(post.userId))
                                                    }
                                            ) {
                                                if (post.userProfileImage != null) {
                                                    ImagePlaceholder(
                                                        url = post.userProfileImage,
                                                        contentDescription = "Profile Image",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize(),
                                                        showInitial = true,
                                                        initial = post.userName
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(MaterialTheme.colorScheme.primary),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = post.userName.first().toString(),
                                                            color = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.width(12.dp))
                                            
                                            Column {
                                                Text(
                                                    text = post.userName,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                                
                                                Text(
                                                    text = DateUtils.formatTimestamp(post.timestamp),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextGray
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
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
                                                    .height(200.dp)
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
                                                val formattedTime = DateUtils.formatSeconds(it)
                                                WorkoutStat(
                                                    icon = "⏱️",
                                                    value = "Time: $formattedTime"
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        // Action buttons
                                        var liked by remember { mutableStateOf(false) }
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            ActionButton(
                                                icon = TablerIcons.Heart,
                                                text = "${post.likes} Cheers",
                                                onClick = {
                                                    liked = !liked
                                                    coroutineScope.launch {
                                                        viewModel.likePost(post.id)
                                                    }
                                                },
                                                tint = if (liked) GreenAccent else MaterialTheme.colorScheme.onSurface
                                            )
                                            
                                            ActionButton(
                                                icon = TablerIcons.Share,
                                                text = "Share",
                                                onClick = { /* Handle share click */ }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Comments header
                            item {
                                Text(
                                    text = "Comments (${post.comments})",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            // Comments
                            if (postDetailState.comments.isEmpty()) {
                                item {
                                    Text(
                                        text = "No comments yet. Be the first to comment!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextGray,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                }
                            } else {
                                items(postDetailState.comments) { comment ->
                                    CommentItem(comment = comment) {
                                        navigator.push(CommunityProfileScreen(comment.userId))
                                    }
                                }
                            }
                            
                            // Add extra space at the bottom for the comment input
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                        
                        // Comment input
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextField(
                                    value = postDetailState.newCommentText,
                                    onValueChange = { viewModel.updateNewCommentText(it) },
                                    placeholder = {
                                        Text(text = "Add a comment...")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(24.dp)),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    maxLines = 3
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                IconButton(
                                    onClick = {
                                        if (postDetailState.newCommentText.isNotBlank()) {
                                            coroutineScope.launch {
                                                viewModel.addComment()
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = TablerIcons.Send,
                                        contentDescription = "Send",
                                        tint = MaterialTheme.colorScheme.onPrimary
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

@Composable
fun CommentItem(
    comment: CommunityComment,
    onUserClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Profile image
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onUserClick() }
        ) {
            if (comment.userProfileImage != null) {
                ImagePlaceholder(
                    url = comment.userProfileImage,
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    showInitial = true,
                    initial = comment.userName
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = comment.userName.first().toString(),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.userName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = DateUtils.formatTimestamp(comment.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
