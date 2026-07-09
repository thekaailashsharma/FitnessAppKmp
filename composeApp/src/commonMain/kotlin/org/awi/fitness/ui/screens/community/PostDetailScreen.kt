package org.awi.fitness.ui.screens.community

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Ban
import compose.icons.tablericons.DotsVertical
import compose.icons.tablericons.Flag
import compose.icons.tablericons.Send
import compose.icons.tablericons.Share
import kotlinx.coroutines.launch
import org.awi.fitness.model.CommunityComment
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.Motion
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GlassTier
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.glassSource
import org.awi.fitness.ui.components.liquidGlass
import org.awi.fitness.ui.components.statusBarPadding
import org.awi.fitness.utils.DateUtils
import org.awi.fitness.utils.openInAppBrowser
import org.awi.fitness.viewmodel.CommunityViewModel

class PostDetailScreen(private val postId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { CommunityViewModel() }
        val postDetailState by viewModel.postDetailState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        val c = TajlyTheme.colors

        LaunchedEffect(postId) {
            viewModel.loadPostDetail(postId)
        }

        ProvideGlass {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(c.bg),
            ) {
                // Report/block confirmation toast (auto-dismisses).
                postDetailState.actionMessage?.let { msg ->
                    LaunchedEffect(msg) {
                        kotlinx.coroutines.delay(2600)
                        viewModel.clearDetailActionMessage()
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .zIndex(10f)
                            .navigationBarsPadding()
                            .padding(start = 24.dp, end = 24.dp, bottom = 96.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(c.s1)
                            .border(1.dp, c.hairStrong, RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text(msg, style = MaterialTheme.typography.bodyMedium, color = c.textHi)
                    }
                }
                // Warm gold glow behind the header — keeps the section on-brand and gives
                // the glass surfaces something premium to blur.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .glassSource()
                        .background(Tajly.sectionGlow(GoldPrimary)),
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
                            text = "Post",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = c.textHi,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    when {
                        postDetailState.isLoading -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = GoldPrimary)
                            }
                        }

                        postDetailState.error != null -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = postDetailState.error ?: "Unknown error",
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }

                        else -> {
                            postDetailState.post?.let { post ->
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    // ---- Post glass card ----
                                    item {
                                        GlassCard(
                                            shape = RoundedCornerShape(22.dp),
                                            tier = GlassTier.Hero,
                                            modifier = Modifier.fillMaxWidth().enterOnce(),
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                            ) {
                                                // Author row
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                ) {
                                                    CommunityAvatar(
                                                        name = post.userName.ifBlank { post.userId },
                                                        imageUrl = post.userProfileImage,
                                                        size = 48.dp,
                                                        onClick = {
                                                            navigator.push(CommunityProfileScreen(post.userId))
                                                        },
                                                    )
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = post.userName,
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = c.textHi,
                                                        )
                                                        Text(
                                                            text = DateUtils.formatTimestamp(post.timestamp),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = c.textLow,
                                                        )
                                                    }
                                                }

                                                // Content
                                                Text(
                                                    text = post.content,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = c.textHi,
                                                )

                                                // Image (only when present)
                                                if (post.imageUrl != null) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(220.dp)
                                                            .clip(RoundedCornerShape(16.dp)),
                                                    ) {
                                                        ImagePlaceholder(
                                                            url = post.imageUrl,
                                                            contentDescription = "Post Image",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize(),
                                                        )
                                                    }
                                                }

                                                // Workout stat chips
                                                if (post.calories != null || post.steps != null || post.duration != null) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    ) {
                                                        post.calories?.let {
                                                            WorkoutStatChip(icon = "🔥", value = "$it kcal")
                                                        }
                                                        post.steps?.let {
                                                            WorkoutStatChip(icon = "👣", value = "$it steps")
                                                        }
                                                        post.duration?.let {
                                                            WorkoutStatChip(
                                                                icon = "⏱️",
                                                                value = DateUtils.formatSeconds(it),
                                                            )
                                                        }
                                                    }
                                                }

                                                HorizontalDivider(color = c.hair)

                                                // Kudos + Share footer
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    KudosButton(
                                                        likes = post.likes,
                                                        isLiked = postDetailState.likedByMe,
                                                        onLikeClick = {
                                                            coroutineScope.launch {
                                                                viewModel.likePost(post.id)
                                                            }
                                                        },
                                                    )
                                                    CommunityActionChip(
                                                        icon = TablerIcons.Share,
                                                        label = "Share",
                                                        onClick = {
                                                            // No native share util in-scope — open the
                                                            // post's canonical link in the in-app browser
                                                            // (proper post text + link as the target).
                                                            runCatching {
                                                                openInAppBrowser(
                                                                    "https://tajly.app/post/${post.id}",
                                                                )
                                                            }
                                                        },
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Comments header
                                    item {
                                        Text(
                                            text = "Comments (${post.comments})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = c.textHi,
                                            modifier = Modifier.padding(vertical = 4.dp),
                                        )
                                    }

                                    // Comments / empty
                                    if (postDetailState.comments.isEmpty()) {
                                        item {
                                            Text(
                                                text = "No comments yet. Be the first to comment!",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = c.textMid,
                                                modifier = Modifier.padding(vertical = 12.dp),
                                            )
                                        }
                                    } else {
                                        items(postDetailState.comments, key = { it.id }) { comment ->
                                            val myEmail = org.awi.fitness.data.UserSettings.getInstance().userEmail
                                            CommentItem(
                                                comment = comment,
                                                isOwnComment = comment.userId == myEmail,
                                                onUserClick = { navigator.push(CommunityProfileScreen(comment.userId)) },
                                                onReport = {
                                                    coroutineScope.launch {
                                                        viewModel.reportComment(comment.id, comment.userId, post.id)
                                                    }
                                                },
                                                onBlock = { viewModel.blockUserFromDetail(comment.userId) },
                                            )
                                        }
                                    }

                                    item { Spacer(Modifier.height(80.dp)) }
                                }

                                // ---- Glass comment composer ----
                                Column(modifier = Modifier.fillMaxWidth().background(c.bg)) {
                                    HorizontalDivider(color = c.hair)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .liquidGlass(shape = RoundedCornerShape(24.dp)),
                                        ) {
                                            TextField(
                                                value = postDetailState.newCommentText,
                                                onValueChange = { viewModel.updateNewCommentText(it) },
                                                placeholder = {
                                                    Text("Add a comment…", color = c.textLow)
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = TextFieldDefaults.colors(
                                                    focusedContainerColor = Color.Transparent,
                                                    unfocusedContainerColor = Color.Transparent,
                                                    disabledContainerColor = Color.Transparent,
                                                    focusedTextColor = c.textHi,
                                                    unfocusedTextColor = c.textHi,
                                                    cursorColor = GoldPrimary,
                                                    focusedIndicatorColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent,
                                                ),
                                                maxLines = 3,
                                            )
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        val canSend = postDetailState.newCommentText.isNotBlank()
                                        val sendInteraction = remember { MutableInteractionSource() }
                                        Box(
                                            modifier = Modifier
                                                .pressScale(sendInteraction)
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .then(
                                                    if (canSend) {
                                                        Modifier.background(Tajly.GoldGradient)
                                                    } else {
                                                        Modifier
                                                            .background(c.glassFill, CircleShape)
                                                            .border(1.dp, c.hairStrong, CircleShape)
                                                    },
                                                )
                                                .clickable(
                                                    interactionSource = sendInteraction,
                                                    indication = null,
                                                    enabled = canSend,
                                                ) {
                                                    coroutineScope.launch {
                                                        viewModel.addComment()
                                                    }
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = TablerIcons.Send,
                                                contentDescription = "Send",
                                                tint = if (canSend) OnGold else c.textMid,
                                                modifier = Modifier.size(20.dp),
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

/** One-shot fade + rise entrance for the hero post card. */
@Composable
private fun Modifier.enterOnce(): Modifier {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val p by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(Motion.DurEnter),
        label = "enterOnce",
    )
    return this.graphicsLayer {
        alpha = p
        translationY = (1f - p) * 14.dp.toPx()
    }
}

/** Subtle glass-tinted stat chip (🔥 / 👣 / ⏱️) shown only where a value is present. */
@Composable
private fun WorkoutStatChip(icon: String, value: String) {
    val c = TajlyTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(c.textHi.copy(alpha = 0.06f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(icon, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = c.textHi,
        )
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
fun CommentItem(
    comment: CommunityComment,
    isOwnComment: Boolean = false,
    onUserClick: () -> Unit,
    onReport: () -> Unit = {},
    onBlock: () -> Unit = {},
) {
    val c = TajlyTheme.colors
    var menuOpen by remember { mutableStateOf(false) }
    GlassCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            CommunityAvatar(
                name = comment.userName,
                imageUrl = comment.userProfileImage,
                size = 40.dp,
                onClick = onUserClick,
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.userName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = DateUtils.formatTimestamp(comment.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textLow,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textHi,
                )
            }

            // Moderation menu — hidden on your own comments (nothing to report/block there).
            if (!isOwnComment) {
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(TablerIcons.DotsVertical, contentDescription = "Options", tint = c.textLow)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Report comment") },
                            leadingIcon = { Icon(TablerIcons.Flag, contentDescription = null, tint = c.textMid) },
                            onClick = { menuOpen = false; onReport() },
                        )
                        DropdownMenuItem(
                            text = { Text("Block user") },
                            leadingIcon = { Icon(TablerIcons.Ban, contentDescription = null, tint = c.textMid) },
                            onClick = { menuOpen = false; onBlock() },
                        )
                    }
                }
            }
        }
    }
}
