package org.awi.fitness.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Award
import compose.icons.tablericons.Check
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.ChevronUp
import compose.icons.tablericons.FileText
import compose.icons.tablericons.Help
import compose.icons.tablericons.Language
import compose.icons.tablericons.Logout
import compose.icons.tablericons.Photo
import compose.icons.tablericons.ShieldCheck
import compose.icons.tablericons.Trash
import compose.icons.tablericons.User
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.banner_abstract_gold
import fitnessappkmp.composeapp.generated.resources.ic3d_chart
import fitnessappkmp.composeapp.generated.resources.ic3d_fire
import fitnessappkmp.composeapp.generated.resources.ic3d_flash_lightning
import fitnessappkmp.composeapp.generated.resources.ic3d_moon
import fitnessappkmp.composeapp.generated.resources.ic3d_star
import fitnessappkmp.composeapp.generated.resources.ic3d_sun
import fitnessappkmp.composeapp.generated.resources.ic3d_target
import fitnessappkmp.composeapp.generated.resources.ic3d_trophy
import kotlinx.coroutines.launch
import org.awi.fitness.LegalUrls
import org.awi.fitness.data.Language
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.navigation.LocalAppNavigation
import org.awi.fitness.navigation.RootRoute
import org.awi.fitness.repository.AuthRepository
import org.awi.fitness.repository.SubscriptionRepository
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.theme.pressScale
import org.awi.fitness.ui.components.AvatarImage
import org.awi.fitness.ui.components.LottieAnim
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.ImagePlaceholder
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.StatRing
import org.awi.fitness.ui.components.glassSource
import org.awi.fitness.ui.screens.avatar.AvatarSelectionScreen
import org.awi.fitness.utils.openInAppBrowser
import org.awi.fitness.viewmodel.AuthViewModel
import org.awi.fitness.viewmodel.LanguageViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

class ProfileScreen(private val languageViewModel: LanguageViewModel) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val c = TajlyTheme.colors
        val userSettings = UserSettings.getInstance()
        val currentLanguage by userSettings.language.collectAsState()
        val scope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow
        val appNavigation = LocalAppNavigation.current
        var showDeleteConfirmation by remember { mutableStateOf(false) }
        val authViewModel = remember { AuthViewModel(AuthRepository()) }

        // Real entitlement check — preserves SubscriptionRepository.checkPremiumAccess()
        var isPremium by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            isPremium = SubscriptionRepository().checkPremiumAccess()
        }

        // ── One-time premium micro-motion, hoisted to Content so it never re-plays on
        //    scroll (LazyColumn disposal can't reset it): hero fade+rise, XP count-up,
        //    level ring sweep — all fire exactly once on first entry.
        val totalXp = userSettings.totalXp
        val xpInto = if (totalXp >= 0) totalXp % 500 else 0
        var revealed by remember { mutableStateOf(false) }
        var xpTarget by remember { mutableStateOf(0) }
        var ringTarget by remember { mutableStateOf(0f) }
        LaunchedEffect(Unit) {
            revealed = true
            xpTarget = totalXp
            ringTarget = (xpInto / 500f).coerceIn(0f, 1f)
        }
        val heroAlpha by animateFloatAsState(if (revealed) 1f else 0f, tween(360), label = "heroReveal")
        val animatedXp by animateIntAsState(xpTarget, tween(900), label = "xpCount")
        val animatedRing by animateFloatAsState(ringTarget, tween(900), label = "levelRing")

        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                containerColor = c.s1,
                titleContentColor = c.textHi,
                textContentColor = c.textMid,
                title = {
                    Text(
                        text = languageViewModel.getString(StringKey.DELETE_ACCOUNT_CONFIRMATION),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Text(
                        text = languageViewModel.getString(StringKey.DELETE_ACCOUNT_DESCRIPTION),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                authViewModel.deleteAccount().onSuccess {
                                    appNavigation.navigateTo(RootRoute.Onboarding)
                                }
                            }
                        }
                    ) {
                        Text(
                            text = languageViewModel.getString(StringKey.CONFIRM),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
                        Text(
                            text = languageViewModel.getString(StringKey.CANCEL_DELETE),
                            color = c.textMid
                        )
                    }
                }
            )
        }

        val hp = Modifier.padding(horizontal = 20.dp)

        ProvideGlass {
            Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // ── 1. Cinematic hero: the user's own photo, name & email, melted into bg. ──
                    item {
                        ProfileHero(
                            name = userSettings.userName?.takeIf { it.isNotBlank() }
                                ?: languageViewModel.getString(StringKey.FITNESS_ENTHUSIAST),
                            email = userSettings.userEmail ?: "",
                            photoUrl = userSettings.profilePhotoUrl,
                            title = languageViewModel.getString(StringKey.PROFILE),
                            level = userSettings.userLevel,
                            streak = userSettings.currentStreak,
                            revealAlpha = heroAlpha
                        )
                    }

                    // ── 2. Structured glass premium card — real entitlement, billing / paywall. ──
                    item {
                        PremiumCard(
                            modifier = hp,
                            isPremium = isPremium,
                            languageViewModel = languageViewModel,
                            onOpenBilling = { navigator.push(BillingScreen()) },
                            onUpgrade = { appNavigation.navigateTo(RootRoute.Paywall) }
                        )
                    }

                    // ── 3. Progress — level ring (sweeps once) + count-up XP + real stat rows. ──
                    item {
                        ProgressSection(
                            modifier = hp,
                            animatedXp = animatedXp,
                            xpToNext = (500 - xpInto).coerceIn(0, 500),
                            animatedRing = animatedRing,
                            streak = userSettings.currentStreak,
                            level = userSettings.userLevel,
                            languageViewModel = languageViewModel
                        )
                    }

                    // ── 4. Fitness stats — only real values, never dead "--". ──
                    item {
                        StatsSection(
                            modifier = hp,
                            bmr = userSettings.bmr,
                            tdee = userSettings.tdee,
                            caloriesGoal = userSettings.calculatedCalories,
                            languageViewModel = languageViewModel
                        )
                    }

                    // ── 5. Settings — theme toggle, change avatar, language (editorial rows). ──
                    item {
                        SettingsSection(
                            modifier = hp,
                            languageViewModel = languageViewModel,
                            currentLanguage = currentLanguage,
                            coroutineScope = scope,
                            navigator = navigator
                        )
                    }

                    // ── 5b. Legal & Support — Terms, Privacy, Help (App Store compliance). ──
                    item {
                        LegalSection(modifier = hp, languageViewModel = languageViewModel)
                    }

                    // ── 6. Account — logout + delete, demoted to the very bottom, low emphasis. ──
                    item {
                        AccountSection(
                            modifier = hp,
                            languageViewModel = languageViewModel,
                            onDelete = { showDeleteConfirmation = true },
                            onLogout = {
                                userSettings.clearUserData()
                                authViewModel.logout()
                                appNavigation.navigateTo(RootRoute.Onboarding)
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }

                // Floating back affordance over the hero
                IconButton(
                    onClick = { navigator.pop() },
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(start = 8.dp, top = 8.dp)
                ) {
                    Icon(
                        imageVector = TablerIcons.ArrowLeft,
                        contentDescription = languageViewModel.getString(StringKey.BACK),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileHero(
    name: String,
    email: String,
    photoUrl: String?,
    title: String,
    level: Int,
    streak: Int,
    revealAlpha: Float,
) {
    val c = TajlyTheme.colors
    Box(modifier = Modifier.fillMaxWidth()) {
        // Confined cinematic gold backdrop — marked as the glass source so surfaces blur it.
        Image(
            painter = painterResource(Res.drawable.banner_abstract_gold),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .glassSource()
        )
        // 4-stop scrim melting the photo into the app background.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    Brush.verticalGradient(
                        0.0f to c.bg.copy(alpha = 0.30f),
                        0.45f to c.bg.copy(alpha = 0.55f),
                        0.80f to c.bg.copy(alpha = 0.92f),
                        1.0f to c.bg
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                // One-time fade + rise on the whole hero block.
                .graphicsLayer {
                    alpha = revealAlpha
                    translationY = (1f - revealAlpha) * 24.dp.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = GoldBright
            )
            Spacer(modifier = Modifier.height(20.dp))

            // The USER'S own photo (network) with initials fallback — never the AI buddy.
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Tajly.GoldGradient)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(c.s1),
                contentAlignment = Alignment.Center
            ) {
                ImagePlaceholder(
                    url = photoUrl,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    showInitial = true,
                    initial = name,
                    tint = OnGold,
                    backgroundColor = GoldPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = c.textHi,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (email.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMid,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Two quiet gold-glass badges — level & streak, honest settings-backed values.
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroBadge(Res.drawable.ic3d_trophy, "Level $level")
                HeroBadge(Res.drawable.ic3d_fire, if (streak > 0) "$streak day streak" else "No streak yet")
                LottieAnim("lottie_flame.json", Modifier.size(30.dp))
            }
        }
    }
}

@Composable
private fun HeroBadge(icon: DrawableResource, text: String) {
    val c = TajlyTheme.colors
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(c.glassFill, CircleShape)
            .border(1.dp, c.hairStrong, CircleShape)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = c.textHi
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Premium (structured glass hero card)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PremiumCard(
    modifier: Modifier = Modifier,
    isPremium: Boolean,
    languageViewModel: LanguageViewModel,
    onOpenBilling: () -> Unit,
    onUpgrade: () -> Unit,
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(22.dp)
    GlassCard(
        shape = shape,
        goldTint = true,
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .border(1.dp, Tajly.GoldGradient, shape)
            .clickable(interactionSource = interaction, indication = null) { onOpenBilling() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gold crown badge — premium, structured (monoline-consistent circular badge).
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Tajly.GoldGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = TablerIcons.Award,
                    contentDescription = null,
                    tint = OnGold,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Gold kicker (product name)
                Text(
                    text = languageViewModel.getString(StringKey.BILLING_PREMIUM).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldBright
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isPremium)
                        languageViewModel.getString(StringKey.SUBSCRIPTION_ACTIVE)
                    else languageViewModel.getString(StringKey.GO_PREMIUM),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = c.textHi
                )
                Text(
                    text = if (isPremium)
                        languageViewModel.getString(StringKey.BILLING_MANAGE_SUBSCRIPTION)
                    else languageViewModel.getString(StringKey.PREMIUM_TAGLINE),
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            if (isPremium) {
                // Active status pill — gold, positive.
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.18f))
                        .border(1.dp, GoldPrimary.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = languageViewModel.getString(StringKey.BILLING_ACTIVE),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldBright
                    )
                }
            } else {
                // Gold upgrade action — pushes the paywall.
                val upgradeInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .pressScale(upgradeInteraction)
                        .clip(CircleShape)
                        .background(Tajly.GoldGradient)
                        .clickable(interactionSource = upgradeInteraction, indication = null) { onUpgrade() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = languageViewModel.getString(StringKey.BILLING_UPGRADE_PLAN),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnGold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Progress (level ring + count-up XP + editorial stat rows)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProgressSection(
    modifier: Modifier = Modifier,
    animatedXp: Int,
    xpToNext: Int,
    animatedRing: Float,
    streak: Int,
    level: Int,
    languageViewModel: LanguageViewModel
) {
    val c = TajlyTheme.colors
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Kicker(languageViewModel.getString(StringKey.PROGRESS))
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = animatedXp.grouped(),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = 40.sp,
                                lineHeight = 40.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = c.textHi,
                            maxLines = 1
                        )
                        Text(
                            text = " XP",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = c.textMid,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        text = languageViewModel.getString(StringKey.TOTAL_EARNED),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textMid
                    )
                }
                // Level ring — sweeps once to xp-into-level progress.
                StatRing(progress = animatedRing, diameter = 76.dp, strokeWidth = 8.dp) {
                    Text(
                        text = "Lv.$level",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = c.textHi
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                StatRow(
                    Res.drawable.ic3d_fire,
                    languageViewModel.getString(StringKey.STREAK),
                    if (streak > 0) "${streak}d" else "0"
                )
                StatRow(
                    Res.drawable.ic3d_trophy,
                    languageViewModel.getString(StringKey.LEVEL),
                    "Lv.$level"
                )
                StatRow(
                    Res.drawable.ic3d_star,
                    languageViewModel.getString(StringKey.TO_NEXT_LEVEL),
                    "$xpToNext XP"
                )
            }
        }
    }
}

@Composable
private fun StatsSection(
    modifier: Modifier = Modifier,
    bmr: Float,
    tdee: Float,
    caloriesGoal: Int,
    languageViewModel: LanguageViewModel
) {
    val c = TajlyTheme.colors
    val kcal = languageViewModel.getString(StringKey.KCAL)

    // Only build rows for values that are REAL — no dead "--".
    data class StatRowData(val icon: DrawableResource, val label: String, val value: String)
    val rows = buildList {
        if (bmr > 100f) add(
            StatRowData(Res.drawable.ic3d_chart, languageViewModel.getString(StringKey.BMR), "${bmr.roundToInt()} $kcal")
        )
        if (tdee > 100f) add(
            StatRowData(Res.drawable.ic3d_flash_lightning, languageViewModel.getString(StringKey.TDEE), "${tdee.roundToInt()} $kcal")
        )
        if (caloriesGoal > 100) add(
            StatRowData(Res.drawable.ic3d_target, languageViewModel.getString(StringKey.GOAL), "$caloriesGoal $kcal")
        )
    }

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Kicker(languageViewModel.getString(StringKey.FITNESS_STATS))
            Spacer(modifier = Modifier.height(16.dp))
            if (rows.isEmpty()) {
                // Teaching state instead of dead "--".
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ic3d_target),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = languageViewModel.getString(StringKey.SET_GOALS_UNLOCK_STATS),
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.textMid,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    rows.forEach { StatRow(it.icon, it.label, it.value) }
                }
            }
        }
    }
}

/** image-2 stat row: 3D asset + label (weight) + value. */
@Composable
private fun StatRow(icon: DrawableResource, label: String, value: String) {
    val c = TajlyTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = c.textMid,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = c.textHi
        )
    }
}

@Composable
private fun Kicker(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = GoldBright
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Settings (editorial rows with circular icon badges)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSection(
    modifier: Modifier = Modifier,
    languageViewModel: LanguageViewModel,
    currentLanguage: String,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    navigator: cafe.adriel.voyager.navigator.Navigator
) {
    val c = TajlyTheme.colors

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Theme Toggle — preserves UserSettings.isDarkTheme
        var isDarkTheme by remember { mutableStateOf(UserSettings.getInstance().isDarkTheme ?: false) }
        EditorialSettingRow(
            badgeTint = GoldPrimary,
            badge = {
                Image(
                    painter = painterResource(if (isDarkTheme) Res.drawable.ic3d_moon else Res.drawable.ic3d_sun),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = languageViewModel.getString(StringKey.DARK_THEME),
            trailing = {
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = {
                        isDarkTheme = it
                        UserSettings.getInstance().isDarkTheme = it
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OnGold,
                        checkedTrackColor = GoldPrimary,
                        uncheckedThumbColor = c.textMid,
                        uncheckedTrackColor = c.s3,
                        uncheckedBorderColor = c.hairStrong
                    )
                )
            }
        )

        // Edit Preferences — height, weight, age, sex & fitness goal → EditPreferencesScreen
        EditorialSettingRow(
            badgeTint = GoldPrimary,
            badge = {
                Icon(
                    imageVector = TablerIcons.User,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
            },
            title = languageViewModel.getString(StringKey.EDIT_PREFERENCES),
            onClick = { navigator.push(EditPreferencesScreen()) },
            trailing = {
                Icon(
                    imageVector = TablerIcons.ChevronRight,
                    contentDescription = null,
                    tint = c.textMid,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        // Achievements — badges & milestones → AchievementsScreen
        EditorialSettingRow(
            badgeTint = GoldPrimary,
            badge = {
                Icon(
                    imageVector = TablerIcons.Award,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
            },
            title = languageViewModel.getString(StringKey.ACHIEVEMENTS),
            onClick = { navigator.push(AchievementsScreen()) },
            trailing = {
                Icon(
                    imageVector = TablerIcons.ChevronRight,
                    contentDescription = null,
                    tint = c.textMid,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        // Backdrop — pick the Home dashboard hero image → BackdropSettingsScreen
        EditorialSettingRow(
            badgeTint = GoldPrimary,
            badge = {
                Icon(
                    imageVector = TablerIcons.Photo,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
            },
            title = languageViewModel.getString(StringKey.BACKDROP),
            onClick = { navigator.push(BackdropSettingsScreen()) },
            trailing = {
                Icon(
                    imageVector = TablerIcons.ChevronRight,
                    contentDescription = null,
                    tint = c.textMid,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        // Change Avatar — preserves navigation to AvatarSelectionScreen
        EditorialSettingRow(
            badgeTint = Tajly.Violet,
            badge = {
                AvatarImage(
                    mood = org.awi.fitness.model.AvatarMood.HAPPY,
                    size = 30.dp
                )
            },
            title = languageViewModel.getString(StringKey.CHANGE_AVATAR),
            onClick = {
                coroutineScope.launch {
                    navigator.push(AvatarSelectionScreen())
                }
            },
            trailing = {
                Icon(
                    imageVector = TablerIcons.ChevronRight,
                    contentDescription = null,
                    tint = c.textMid,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        // Language Selector — preserves languageViewModel.setLanguage
        var expanded by remember { mutableStateOf(false) }
        val selectedLanguage = Language.entries.find { it.code == currentLanguage } ?: Language.ENGLISH
        EditorialSettingRow(
            badgeTint = Tajly.Blue,
            badge = {
                Icon(
                    imageVector = TablerIcons.Language,
                    contentDescription = null,
                    tint = Tajly.Blue,
                    modifier = Modifier.size(24.dp)
                )
            },
            title = languageViewModel.getString(StringKey.LANGUAGE),
            trailing = {
                Box {
                    Row(
                        modifier = Modifier.clickable { expanded = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = selectedLanguage.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textHi
                        )
                        Icon(
                            imageVector = if (expanded) TablerIcons.ChevronUp else TablerIcons.ChevronDown,
                            contentDescription = null,
                            tint = c.textMid,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        Language.entries.forEach { language ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = language.displayName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    languageViewModel.setLanguage(language)
                                    expanded = false
                                },
                                leadingIcon = if (language == selectedLanguage) {
                                    {
                                        Icon(
                                            imageVector = TablerIcons.Check,
                                            contentDescription = null,
                                            tint = GoldPrimary
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Legal & Support (App Store Guideline 3.1.2 — surfaced outside the paywall too)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LegalSection(
    modifier: Modifier = Modifier,
    languageViewModel: LanguageViewModel
) {
    val c = TajlyTheme.colors

    @Composable
    fun chevron() {
        Icon(
            imageVector = TablerIcons.ChevronRight,
            contentDescription = null,
            tint = c.textMid,
            modifier = Modifier.size(20.dp)
        )
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Terms of Use (EULA)
        EditorialSettingRow(
            badgeTint = GoldPrimary,
            badge = {
                Icon(
                    imageVector = TablerIcons.FileText,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
            },
            title = languageViewModel.getString(StringKey.TERMS_OF_USE),
            onClick = { openInAppBrowser(LegalUrls.TERMS_OF_USE) },
            trailing = { chevron() }
        )

        // Privacy Policy
        EditorialSettingRow(
            badgeTint = Tajly.Blue,
            badge = {
                Icon(
                    imageVector = TablerIcons.ShieldCheck,
                    contentDescription = null,
                    tint = Tajly.Blue,
                    modifier = Modifier.size(24.dp)
                )
            },
            title = languageViewModel.getString(StringKey.PRIVACY_POLICY),
            onClick = { openInAppBrowser(LegalUrls.PRIVACY_POLICY) },
            trailing = { chevron() }
        )

        // Help & Support
        EditorialSettingRow(
            badgeTint = Tajly.Violet,
            badge = {
                Icon(
                    imageVector = TablerIcons.Help,
                    contentDescription = null,
                    tint = Tajly.Violet,
                    modifier = Modifier.size(24.dp)
                )
            },
            title = languageViewModel.getString(StringKey.HELP_SUPPORT),
            onClick = { openInAppBrowser(LegalUrls.SUPPORT) },
            trailing = { chevron() }
        )
    }
}

@Composable
private fun EditorialSettingRow(
    badgeTint: Color,
    badge: @Composable () -> Unit,
    title: String,
    trailing: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val c = TajlyTheme.colors
    val interaction = remember { MutableInteractionSource() }
    var cardModifier = Modifier.fillMaxWidth()
    if (onClick != null) {
        cardModifier = cardModifier
            .pressScale(interaction)
            .clickable(interactionSource = interaction, indication = null) { onClick() }
    }
    GlassCard(modifier = cardModifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape).background(badgeTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { badge() }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = c.textHi,
                modifier = Modifier.weight(1f)
            )
            trailing()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Account (demoted, quiet)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Account actions — intentionally DEMOTED to the very bottom, low emphasis (retention).
 * Logout + Delete are plain, quiet rows — reachable but never the focus of the screen.
 */
@Composable
private fun AccountSection(
    modifier: Modifier = Modifier,
    languageViewModel: LanguageViewModel,
    onDelete: () -> Unit,
    onLogout: () -> Unit
) {
    val c = TajlyTheme.colors
    val error = MaterialTheme.colorScheme.error
    Column(modifier = modifier.fillMaxWidth()) {
        // Logout — quiet, low-emphasis text row (no gold CTA)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onLogout() }
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = TablerIcons.Logout,
                contentDescription = null,
                tint = c.textMid,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = languageViewModel.getString(StringKey.LOGOUT),
                style = MaterialTheme.typography.bodyMedium,
                color = c.textMid
            )
        }

        // Delete account — quietest, destructive tint only on the text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onDelete() }
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = TablerIcons.Trash,
                contentDescription = null,
                tint = error.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = languageViewModel.getString(StringKey.DELETE_ACCOUNT),
                style = MaterialTheme.typography.bodyMedium,
                color = error.copy(alpha = 0.75f)
            )
        }
    }
}

// ── helpers ──
private fun Int.grouped(): String {
    val neg = this < 0
    val s = kotlin.math.abs(this).toString()
    val sb = StringBuilder()
    val n = s.length
    for (i in 0 until n) { if (i > 0 && (n - i) % 3 == 0) sb.append(','); sb.append(s[i]) }
    return if (neg) "-$sb" else sb.toString()
}
