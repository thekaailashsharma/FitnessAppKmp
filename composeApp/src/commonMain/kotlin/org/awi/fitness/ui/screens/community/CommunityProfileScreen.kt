package org.awi.fitness.ui.screens.community

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.At
import compose.icons.tablericons.Camera
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Link
import compose.icons.tablericons.Photo
import compose.icons.tablericons.UserCheck
import compose.icons.tablericons.World
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.banner_abstract_gold
import fitnessappkmp.composeapp.generated.resources.banner_gym_1
import fitnessappkmp.composeapp.generated.resources.banner_track_1
import kotlinx.coroutines.launch
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.Motion
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.glassSource
import org.awi.fitness.ui.components.liquidGlass
import org.awi.fitness.ui.components.statusBarPadding
import org.awi.fitness.utils.openInAppBrowser
import org.awi.fitness.utils.rememberImagePickerLauncher
import org.awi.fitness.viewmodel.CommunityViewModel
import org.awi.fitness.viewmodel.LocalLanguageViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/** The free curated cover options offered on the profile. */
private val CuratedBanners: List<DrawableResource> = listOf(
    Res.drawable.banner_abstract_gold,
    Res.drawable.banner_gym_1,
    Res.drawable.banner_track_1,
)

class CommunityProfileScreen(private val userId: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val languageViewModel = LocalLanguageViewModel.current
        val viewModel = rememberScreenModel { CommunityViewModel() }
        val profileState by viewModel.profileState.collectAsState()
        val scope = rememberCoroutineScope()
        val userSettings = UserSettings.getInstance()
        val currentUserEmail = userSettings.userEmail.orEmpty()
        val isOwnProfile = userId == currentUserEmail
        val c = TajlyTheme.colors

        val bannerSettings = remember { org.awi.fitness.data.UserSettings.getInstance() }
        var selectedBanner by remember {
            mutableStateOf(
                CuratedBanners.getOrNull(bannerSettings.communityBanner?.toIntOrNull() ?: 0) ?: CuratedBanners.first()
            )
        }
        var showBannerPicker by remember { mutableStateOf(false) }

        LaunchedEffect(userId) {
            viewModel.loadUserProfile(userId)
        }

        // Reset the one-shot save flag (fired by the quick avatar edit).
        LaunchedEffect(profileState.saveProfileSuccess) {
            if (profileState.saveProfileSuccess) {
                viewModel.resetProfileSaveSuccess()
            }
        }

        // Avatar quick-edit: pick a photo → persist via the SAME update call as the editor,
        // preserving current name/bio.
        val avatarPicker = rememberImagePickerLauncher { bytes ->
            if (bytes != null) {
                scope.launch {
                    val u = viewModel.profileState.value.user
                    viewModel.updateUserProfileWithPhoto(
                        u?.name ?: "",
                        u?.bio ?: "",
                        u?.profileImage,
                        bytes,
                    )
                }
            }
        }

        ProvideGlass {
            Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
                when {
                    profileState.isLoading && profileState.user == null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GoldPrimary)
                        }
                    }

                    profileState.user != null -> {
                        val user = profileState.user!!
                        LazyColumn(modifier = Modifier.fillMaxSize()) {

                            // ── Hero header — editable cover + focal avatar ──────────
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(236.dp)) {
                                    // Editable cover banner (blurred backdrop for the glass buttons).
                                    Box(modifier = Modifier.fillMaxWidth().height(176.dp)) {
                                        Image(
                                            painter = painterResource(selectedBanner),
                                            contentDescription = languageViewModel.getString(StringKey.COMMUNITY_COVER),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().glassSource(),
                                        )
                                        // Scrim so the avatar/name read cleanly and the cover
                                        // melts into the canvas.
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color.Black.copy(alpha = 0.10f), c.bg)
                                                    )
                                                )
                                        )
                                    }

                                    // Back button (glass)
                                    Box(
                                        modifier = Modifier
                                            .statusBarPadding()
                                            .padding(12.dp)
                                            .align(Alignment.TopStart),
                                    ) {
                                        ProfileGlassIconButton(onClick = { navigator.pop() }) {
                                            Icon(
                                                TablerIcons.ArrowLeft,
                                                contentDescription = languageViewModel.getString(StringKey.BACK),
                                                tint = c.textHi,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }

                                    // Change-cover button (own profile, glass)
                                    if (isOwnProfile) {
                                        Box(
                                            modifier = Modifier
                                                .statusBarPadding()
                                                .padding(12.dp)
                                                .align(Alignment.TopEnd),
                                        ) {
                                            ProfileGlassIconButton(onClick = { showBannerPicker = !showBannerPicker }) {
                                                Icon(
                                                    TablerIcons.Photo,
                                                    contentDescription = languageViewModel.getString(StringKey.COMMUNITY_CHANGE_COVER),
                                                    tint = c.textHi,
                                                    modifier = Modifier.size(20.dp),
                                                )
                                            }
                                        }
                                    }

                                    // Avatar — gold-rimmed focal element; edit affordance on the RING.
                                    Box(
                                        modifier = Modifier
                                            .size(108.dp)
                                            .align(Alignment.BottomCenter),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(Tajly.GoldGradient)
                                                .padding(3.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, c.bg, CircleShape),
                                        ) {
                                            ImagePlaceholder(
                                                url = user.profileImage,
                                                contentDescription = user.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                                showInitial = true,
                                                initial = user.name,
                                                tint = OnGold,
                                                backgroundColor = GoldPrimary,
                                            )
                                        }
                                        if (isOwnProfile) {
                                            // Small camera badge seated on the ring edge (never over the face).
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Tajly.GoldGradient)
                                                    .border(2.dp, c.bg, CircleShape)
                                                    .clickable { avatarPicker.launch() },
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    TablerIcons.Camera,
                                                    contentDescription = languageViewModel.getString(StringKey.EDIT_CHANGE_PHOTO),
                                                    tint = OnGold,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // ── Curated cover picker (own profile, on demand) ────────
                            if (isOwnProfile && showBannerPicker) {
                                item {
                                    BannerPickerRow(
                                        selected = selectedBanner,
                                        onSelect = {
                                            selectedBanner = it
                                            bannerSettings.communityBanner = CuratedBanners.indexOf(it).toString()
                                            showBannerPicker = false
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 8.dp),
                                    )
                                }
                            }

                            // ── Name / username / bio — grouped identity ─────────────
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = c.textHi,
                                    )
                                    Text(
                                        text = user.username,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = c.textMid,
                                    )
                                    if (!user.bio.isNullOrBlank()) {
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            text = user.bio,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = c.textMid,
                                            textAlign = TextAlign.Center,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    } else if (isOwnProfile) {
                                        TextButton(onClick = {
                                            navigator.push(
                                                EditCommunityProfileScreen(
                                                    userId = userId,
                                                    currentName = user.name,
                                                    currentBio = user.bio ?: "",
                                                    currentPhotoUrl = user.profileImage,
                                                )
                                            )
                                        }) {
                                            Text("+ Add a bio", color = GoldPrimary)
                                        }
                                    }
                                }
                            }

                            // ── Stats — grouped in a single glass card ───────────────
                            item {
                                GlassCard(
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 8.dp)
                                        .enterOnce(),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 18.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                    ) {
                                        ProfileStatChip(value = "${profileState.userPosts.size}", label = languageViewModel.getString(StringKey.COMMUNITY_POSTS))
                                        ProfileStatDivider()
                                        ProfileStatChip(value = "${user.followersCount}", label = "Followers")
                                        ProfileStatDivider()
                                        ProfileStatChip(value = "${user.followingCount}", label = "Following")
                                        ProfileStatDivider()
                                        ProfileStatChip(
                                            value = if (user.streakDays > 0) "${user.streakDays}" else "—",
                                            label = languageViewModel.getString(StringKey.COMMUNITY_DAY_STREAK),
                                            valueColor = if (user.streakDays > 0) GoldPrimary else null,
                                        )
                                        ProfileStatDivider()
                                        ProfileStatChip(value = "Lv ${user.level}", label = languageViewModel.getString(StringKey.COMMUNITY_LEVEL))
                                    }
                                }
                            }

                            // ── Social links — tappable rows (open in in-app browser) ─
                            val hasLinks = !user.website.isNullOrBlank() ||
                                !user.instagram.isNullOrBlank() ||
                                !user.twitter.isNullOrBlank()
                            if (hasLinks) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp, vertical = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        user.website?.takeIf { it.isNotBlank() }?.let {
                                            SocialLinkRow(
                                                icon = TablerIcons.World,
                                                label = it,
                                                onClick = { openInAppBrowser(normalizeWebsiteUrl(it)) },
                                            )
                                        }
                                        user.instagram?.takeIf { it.isNotBlank() }?.let {
                                            SocialLinkRow(
                                                icon = TablerIcons.At,
                                                label = it,
                                                onClick = { openInAppBrowser(normalizeInstagramUrl(it)) },
                                            )
                                        }
                                        user.twitter?.takeIf { it.isNotBlank() }?.let {
                                            SocialLinkRow(
                                                icon = TablerIcons.Link,
                                                label = it,
                                                onClick = { openInAppBrowser(normalizeWebsiteUrl(it)) },
                                            )
                                        }
                                    }
                                }
                            }

                            // ── Primary action — single gold CTA per context ─────────
                            if (!isOwnProfile) {
                                item {
                                    val following = user.isFollowing
                                    if (following) {
                                        GlassActionPill(
                                            text = languageViewModel.getString(StringKey.COMMUNITY_FOLLOWING),
                                            icon = TablerIcons.UserCheck,
                                            onClick = { scope.launch { viewModel.toggleFollowUser(user.id) } },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp, vertical = 8.dp),
                                        )
                                    } else {
                                        GoldButton(
                                            text = languageViewModel.getString(StringKey.COMMUNITY_FOLLOW),
                                            onClick = { scope.launch { viewModel.toggleFollowUser(user.id) } },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp, vertical = 8.dp),
                                        )
                                    }
                                }
                            } else {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        // Primary: new post (gold). Secondary: edit profile (glass).
                                        GoldButton(
                                            text = languageViewModel.getString(StringKey.COMMUNITY_NEW_POST),
                                            onClick = { navigator.push(CreatePostScreen()) },
                                            modifier = Modifier.weight(1f),
                                        )
                                        GlassActionPill(
                                            text = languageViewModel.getString(StringKey.EDIT_PROFILE_TITLE),
                                            icon = TablerIcons.Edit,
                                            onClick = {
                                                navigator.push(
                                                    EditCommunityProfileScreen(
                                                        userId = userId,
                                                        currentName = user.name,
                                                        currentBio = user.bio ?: "",
                                                        currentPhotoUrl = user.profileImage,
                                                    )
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }

                            // ── Posts section header ─────────────────────────────────
                            item {
                                Text(
                                    text = languageViewModel.getString(StringKey.COMMUNITY_POSTS),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = c.textHi,
                                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
                                )
                            }

                            if (profileState.userPosts.isEmpty()) {
                                item {
                                    CommunityEmptyState(
                                        title = if (isOwnProfile) languageViewModel.getString(StringKey.COMMUNITY_NO_POSTS_YET) else languageViewModel.getString(StringKey.COMMUNITY_NO_POSTS),
                                        message = if (isOwnProfile)
                                            languageViewModel.getString(StringKey.COMMUNITY_SHARE_FIRST_WIN)
                                        else
                                            "${user.name} hasn't shared anything yet.",
                                        actionLabel = if (isOwnProfile) languageViewModel.getString(StringKey.COMMUNITY_CREATE_POST) else null,
                                        onAction = if (isOwnProfile) {
                                            { navigator.push(CreatePostScreen()) }
                                        } else null,
                                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                    )
                                }
                            } else {
                                items(profileState.userPosts, key = { it.id }) { post ->
                                    CommunityPostCard(
                                        post = post,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                        onPostClick = { navigator.push(PostDetailScreen(post.id)) },
                                        onLikeClick = { scope.launch { viewModel.likePost(post.id) } },
                                        onCommentClick = { navigator.push(PostDetailScreen(post.id)) },
                                        onUserClick = { },
                                    )
                                }
                            }

                            item { Spacer(Modifier.height(100.dp)) }
                        }
                    }

                    else -> {
                        CommunityEmptyState(
                            title = languageViewModel.getString(StringKey.COMMUNITY_PROFILE_UNAVAILABLE),
                            message = profileState.error ?: languageViewModel.getString(StringKey.COMMUNITY_PROFILE_LOAD_FAIL),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

/** One-shot fade + rise entrance for the identity stats card. */
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

/** Row of curated cover options; the active one gets a gold ring. */
@Composable
private fun BannerPickerRow(
    selected: DrawableResource,
    onSelect: (DrawableResource) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TajlyTheme.colors
    val languageViewModel = LocalLanguageViewModel.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CuratedBanners.forEach { banner ->
            val isSel = banner == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isSel) Modifier.border(2.dp, GoldPrimary, RoundedCornerShape(12.dp))
                        else Modifier.border(1.dp, c.hairStrong, RoundedCornerShape(12.dp))
                    )
                    .clickable { onSelect(banner) },
            ) {
                Image(
                    painter = painterResource(banner),
                    contentDescription = languageViewModel.getString(StringKey.COMMUNITY_COVER_OPTION),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                )
            }
        }
    }
}

/** Quiet glass pill for secondary/demoted actions (Edit, Following). */
@Composable
private fun GlassActionPill(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .pressScale(interaction)
            .liquidGlass(shape = RoundedCornerShape(16.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .heightIn(min = 52.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = c.textHi, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = c.textHi, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun ProfileGlassIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .pressScale(interaction)
            .size(40.dp)
            .liquidGlass(shape = CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun ProfileStatChip(
    value: String,
    label: String,
    valueColor: Color? = null,
) {
    val c = TajlyTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor ?: c.textHi,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = c.textMid,
        )
    }
}

/** Tappable social-link row (glass) that opens the link in the in-app browser. */
@Composable
private fun SocialLinkRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .liquidGlass(shape = RoundedCornerShape(14.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = c.textHi,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            TablerIcons.ChevronRight,
            contentDescription = null,
            tint = c.textMid,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** Ensures a website string has a scheme so the in-app browser can open it. */
private fun normalizeWebsiteUrl(raw: String): String {
    val trimmed = raw.trim()
    return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed
    else "https://$trimmed"
}

/** Turns an Instagram handle (or URL) into a full instagram.com profile URL. */
private fun normalizeInstagramUrl(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    val handle = trimmed.removePrefix("@")
    return "https://instagram.com/$handle"
}

@Composable
private fun ProfileStatDivider() {
    val c = TajlyTheme.colors
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(c.hairStrong),
    )
}
