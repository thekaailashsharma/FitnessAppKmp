package org.awi.fitness.ui.screens.community

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.At
import compose.icons.tablericons.Camera
import compose.icons.tablericons.Link
import compose.icons.tablericons.World
import kotlinx.coroutines.launch
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.ui.components.statusBarPadding
import org.awi.fitness.utils.rememberImagePickerLauncher
import org.awi.fitness.viewmodel.CommunityViewModel
import org.awi.fitness.viewmodel.LocalLanguageViewModel
import org.awi.fitness.data.StringKey

/**
 * Full-screen profile editor (NOT a bottom sheet) — single column, IME-aware, with a
 * Cancel/Save top bar so the keyboard never hides the bio or the social-link fields.
 * Wires display-name + bio + photo to the same [CommunityViewModel.updateUserProfileWithPhoto]
 * call the profile screen uses.
 */
class EditCommunityProfileScreen(
    private val userId: String,
    private val currentName: String,
    private val currentBio: String,
    private val currentPhotoUrl: String?,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { CommunityViewModel() }
        val profileState by viewModel.profileState.collectAsState()
        val scope = rememberCoroutineScope()
        val c = TajlyTheme.colors
        val languageViewModel = LocalLanguageViewModel.current

        val userSettings = remember { org.awi.fitness.data.UserSettings.getInstance() }
        val savedSocial = remember { (userSettings.socialLinks ?: "").split("\n") }
        var name by remember { mutableStateOf(currentName) }
        var bio by remember { mutableStateOf(currentBio) }
        var website by remember { mutableStateOf(savedSocial.getOrNull(0) ?: "") }
        var instagram by remember { mutableStateOf(savedSocial.getOrNull(1) ?: "") }
        var twitter by remember { mutableStateOf(savedSocial.getOrNull(2) ?: "") }
        var selectedPhotoBytes by remember { mutableStateOf<ByteArray?>(null) }

        val imagePicker = rememberImagePickerLauncher { bytes ->
            if (bytes != null) selectedPhotoBytes = bytes
        }

        // On successful save, pop back to the profile (which reloads on resume).
        LaunchedEffect(profileState.saveProfileSuccess) {
            if (profileState.saveProfileSuccess) {
                viewModel.resetProfileSaveSuccess()
                navigator.pop()
            }
        }

        val isSaving = profileState.isSavingProfile
        val canSave = name.isNotBlank() && !isSaving

        // One-time fade + rise on appearance (no looping motion).
        var appeared by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { appeared = true }
        val enter by animateFloatAsState(
            targetValue = if (appeared) 1f else 0f,
            animationSpec = tween(durationMillis = 320),
            label = "editProfileEnter",
        )
        val cancelInteraction = remember { MutableInteractionSource() }
        val saveInteraction = remember { MutableInteractionSource() }

        fun save() {
            if (!canSave) return
            // Keep the local prefs mirror (for fast prefill), AND persist to Firestore so the
            // links show on the public profile.
            userSettings.socialLinks = listOf(website, instagram, twitter).joinToString("\n")
            scope.launch {
                viewModel.updateUserProfileWithPhoto(
                    displayName = name,
                    bio = bio,
                    existingPhotoUrl = currentPhotoUrl,
                    imageBytes = selectedPhotoBytes,
                    website = website.trim(),
                    instagram = instagram.trim(),
                    twitter = twitter.trim(),
                )
            }
        }

        AuroraBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // ── Cancel / Save top bar ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = languageViewModel.getString(StringKey.COMMON_CANCEL),
                    style = MaterialTheme.typography.titleSmall,
                    color = c.textMid,
                    modifier = Modifier
                        .pressScale(cancelInteraction)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(
                            interactionSource = cancelInteraction,
                            indication = null,
                            enabled = !isSaving,
                        ) { navigator.pop() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                )
                Text(
                    text = languageViewModel.getString(StringKey.EDIT_PROFILE_TITLE),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Box(
                    modifier = Modifier
                        .pressScale(saveInteraction)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Tajly.GoldGradient)
                        .alpha(if (canSave) 1f else 0.45f)
                        .clickable(
                            interactionSource = saveInteraction,
                            indication = null,
                            enabled = canSave,
                        ) { save() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = OnGold,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                        )
                    } else {
                        Text(
                            text = languageViewModel.getString(StringKey.COMMON_SAVE),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnGold,
                        )
                    }
                }
            }

            // ── Scrollable, IME-aware form ───────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = enter
                        translationY = (1f - enter) * 22.dp.toPx()
                    }
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Photo — the focal element.
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Tajly.GoldGradient)
                        .padding(3.dp)
                        .clip(CircleShape)
                        .clickable { imagePicker.launch() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (selectedPhotoBytes != null) {
                        ImagePlaceholder(
                            imageBytes = selectedPhotoBytes,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                        )
                    } else {
                        ImagePlaceholder(
                            url = currentPhotoUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            showInitial = true,
                            initial = name,
                            tint = OnGold,
                            backgroundColor = GoldPrimary,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Tajly.GoldGradient)
                            .border(2.dp, c.bg, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            TablerIcons.Camera,
                            contentDescription = languageViewModel.getString(StringKey.EDIT_CHANGE_PHOTO),
                            tint = OnGold,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
                Text(
                    languageViewModel.getString(StringKey.EDIT_TAP_CHANGE_PHOTO),
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textMid,
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(languageViewModel.getString(StringKey.EDIT_DISPLAY_NAME)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = editFieldColors(c.textHi, c.hairStrong, c.textMid),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 150) bio = it },
                    label = { Text(languageViewModel.getString(StringKey.EDIT_BIO)) },
                    placeholder = { Text(languageViewModel.getString(StringKey.EDIT_BIO_PLACEHOLDER)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = editFieldColors(c.textHi, c.hairStrong, c.textMid),
                    maxLines = 4,
                    supportingText = { Text("${bio.length}/150", color = c.textMid) },
                )

                // ── Social links ─────────────────────────────────────────────
                Text(
                    text = languageViewModel.getString(StringKey.EDIT_SOCIAL_LINKS),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = c.textMid,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
                SocialField(
                    value = website,
                    onValueChange = { website = it },
                    label = languageViewModel.getString(StringKey.EDIT_WEBSITE),
                    placeholder = languageViewModel.getString(StringKey.EDIT_WEBSITE_HINT),
                    icon = TablerIcons.World,
                    fieldColors = editFieldColors(c.textHi, c.hairStrong, c.textMid),
                )
                SocialField(
                    value = instagram,
                    onValueChange = { instagram = it },
                    label = languageViewModel.getString(StringKey.EDIT_INSTAGRAM),
                    placeholder = "@handle",
                    icon = TablerIcons.At,
                    fieldColors = editFieldColors(c.textHi, c.hairStrong, c.textMid),
                )
                SocialField(
                    value = twitter,
                    onValueChange = { twitter = it },
                    label = languageViewModel.getString(StringKey.EDIT_LINK),
                    placeholder = languageViewModel.getString(StringKey.EDIT_ANOTHER_LINK),
                    icon = TablerIcons.Link,
                    fieldColors = editFieldColors(c.textHi, c.hairStrong, c.textMid),
                )
            }
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SocialField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    fieldColors: androidx.compose.material3.TextFieldColors,
) {
    val c = TajlyTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        leadingIcon = { Icon(icon, contentDescription = null, tint = c.textMid, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = fieldColors,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun editFieldColors(
    textColor: Color,
    border: Color,
    label: Color,
) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = textColor,
    unfocusedTextColor = textColor,
    cursorColor = GoldPrimary,
    focusedBorderColor = GoldPrimary,
    unfocusedBorderColor = border,
    focusedLabelColor = GoldPrimary,
    unfocusedLabelColor = label,
)
