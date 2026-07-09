package org.awi.fitness.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertCircle
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Blockquote
import compose.icons.tablericons.Bolt
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.CircleCheck
import compose.icons.tablericons.Crown
import compose.icons.tablericons.ExternalLink
import compose.icons.tablericons.EyeOff
import compose.icons.tablericons.InfoCircle
import compose.icons.tablericons.Leaf
import compose.icons.tablericons.List
import compose.icons.tablericons.MessageCircle
import compose.icons.tablericons.Refresh
import compose.icons.tablericons.Scale
import compose.icons.tablericons.Settings
import compose.icons.tablericons.Trophy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.awi.fitness.LegalUrls
import org.awi.fitness.getPlatform
import org.awi.fitness.navigation.LocalAppNavigation
import org.awi.fitness.navigation.RootRoute
import org.awi.fitness.repository.SubscriptionRepository
import org.awi.fitness.repository.SubscriptionStatus
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.OnGold
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GlassTier
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.ProvideGlass
import org.awi.fitness.ui.components.SectionHeader
import org.awi.fitness.ui.components.StatRing
import org.awi.fitness.ui.components.glassSource
import org.awi.fitness.utils.openExternalUrl
import org.awi.fitness.utils.openInAppBrowser

/**
 * Subscription management — the surface a member uses to see their real plan (from
 * RevenueCat's [SubscriptionStatus]) and manage it. Apple/Google are the merchants of
 * record, so every "cancel / invoice / refund" action deep-links out to the store via
 * [openInAppBrowser]; nothing about price, method or renewal is ever fabricated here.
 */
class BillingScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val c = TajlyTheme.colors
        val navigator = LocalNavigator.currentOrThrow
        val appNavigation = LocalAppNavigation.current
        val scope = rememberCoroutineScope()

        val isAndroid = remember { getPlatform().name.startsWith("Android", ignoreCase = true) }

        val repo = remember { SubscriptionRepository() }
        var status by remember { mutableStateOf<SubscriptionStatus?>(null) }
        var loading by remember { mutableStateOf(true) }
        var message by remember { mutableStateOf<String?>(null) }
        var restoring by remember { mutableStateOf(false) }

        // Change-plan (upgrade/downgrade) state — additive, does not affect status loading.
        var offering by remember { mutableStateOf<org.awi.fitness.repository.SubscriptionOffering?>(null) }
        var switching by remember { mutableStateOf(false) }
        var planToSwitch by remember { mutableStateOf<org.awi.fitness.repository.SubscriptionPlan?>(null) }

        // In-app billing history (native — no browser). Loaded on demand.
        var showHistory by remember { mutableStateOf(false) }
        var history by remember { mutableStateOf<List<org.awi.fitness.repository.PurchaseHistoryEntry>?>(null) }

        // Server-driven button visibility (Firestore config/subscription, cached in UserSettings).
        val cfg = remember { org.awi.fitness.data.UserSettings.getInstance() }
        val cfgShowRefund = remember { cfg.subCfgShowRefund }
        val cfgShowInvoices = remember { cfg.subCfgShowInvoices }
        val cfgSupportUrl = remember { cfg.subCfgSupportUrl.ifBlank { LegalUrls.SUPPORT } }

        LaunchedEffect(Unit) {
            status = repo.getSubscriptionStatus()
            loading = false
        }

        // Fetch available plans for the Change-plan section (separate from status loading).
        LaunchedEffect(Unit) {
            offering = repo.getOfferings().getOrNull()
        }

        // Transient toast-style confirmation that auto-clears.
        LaunchedEffect(message) {
            if (message != null) {
                delay(2500)
                message = null
            }
        }

        // One-time fade + rise reveal (never re-plays on scroll).
        var revealed by remember { mutableStateOf(false) }
        LaunchedEffect(loading) { if (!loading) revealed = true }
        val reveal by animateFloatAsState(if (revealed) 1f else 0f, tween(380), label = "billingReveal")

        val storeSubscriptionsUrl = status?.managementUrl
            ?: if (isAndroid) LegalUrls.PLAY_MANAGE_SUBSCRIPTIONS else LegalUrls.APPLE_MANAGE_SUBSCRIPTIONS
        val purchaseHistoryUrl =
            if (isAndroid) LegalUrls.PLAY_PURCHASE_HISTORY else LegalUrls.APPLE_PURCHASE_HISTORY
        val store = storeLabel(status?.storeName, isAndroid)

        val restore: () -> Unit = {
            if (!restoring) {
                restoring = true
                scope.launch {
                    val result = repo.restorePurchases()
                    val ok = result.getOrDefault(false)
                    message = if (ok) "Purchases restored" else "Nothing to restore"
                    if (ok) status = repo.getSubscriptionStatus()
                    restoring = false
                }
            }
        }

        ProvideGlass {
            Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
                AuroraBackground(modifier = Modifier.fillMaxSize().glassSource()) {}

                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Subscription",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = c.textHi
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { navigator.pop() }) {
                                    Icon(TablerIcons.ArrowLeft, contentDescription = "Back", tint = c.textHi)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                    }
                ) { padding ->
                    if (loading) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(padding),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GoldPrimary)
                        }
                    } else {
                        val active = status?.isActive == true

                        // Change-plan targets — every configured plan except the currently-active one.
                        val currentPid = status?.productIdentifier
                        val otherPlans = offering?.plans?.filter {
                            it.storeProductId.isNotBlank() && it.storeProductId != currentPid
                        } ?: emptyList()
                        val showPlanSwitch = active &&
                            org.awi.fitness.data.UserSettings.getInstance().subCfgShowPlanSwitch &&
                            otherPlans.isNotEmpty()

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                                .padding(horizontal = 16.dp)
                                .graphicsLayer {
                                    alpha = reveal
                                    translationY = (1f - reveal) * 22.dp.toPx()
                                },
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
                        ) {
                            // a. Status hero
                            item {
                                StatusHeroCard(
                                    status = status,
                                    store = store,
                                    onGoPremium = { appNavigation.navigateTo(RootRoute.Paywall) }
                                )
                            }

                            // Free-plan benefits — fold "What's included" here to entice upgrade.
                            if (!active) {
                                item { BenefitsCard() }
                            }

                            // a2. Change plan (upgrade / downgrade) — only when a switch target exists.
                            if (showPlanSwitch) {
                                item {
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                            SectionHeader(title = "Change plan")
                                            Spacer(modifier = Modifier.height(6.dp))
                                            otherPlans.forEachIndexed { index, plan ->
                                                if (index > 0) RowDivider()
                                                val bestHint = if (plan.isBestValue) "  ·  BEST VALUE" else ""
                                                BillingRow(
                                                    icon = TablerIcons.Refresh,
                                                    title = plan.title,
                                                    subtitle = plan.priceString + bestHint,
                                                    trailingText = if (switching) "…" else "Switch",
                                                    onClick = if (switching) null else {
                                                        { planToSwitch = plan }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // b. Manage
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                        SectionHeader(title = "Manage")
                                        Spacer(modifier = Modifier.height(6.dp))

                                        BillingRow(
                                            icon = TablerIcons.Settings,
                                            title = "Manage or cancel subscription",
                                            subtitle = "Opens your $store settings.",
                                            trailingIcon = TablerIcons.ExternalLink,
                                            onClick = { openInAppBrowser(storeSubscriptionsUrl) }
                                        )
                                        RowDivider()
                                        BillingRow(
                                            icon = TablerIcons.Refresh,
                                            title = "Restore purchases",
                                            subtitle = "Already subscribed? Restore your Premium.",
                                            trailingIcon = if (restoring) null else TablerIcons.ChevronRight,
                                            trailingText = if (restoring) "…" else null,
                                            onClick = restore
                                        )
                                        if (cfgShowInvoices) {
                                            RowDivider()
                                            BillingRow(
                                                icon = TablerIcons.List,
                                                title = "Billing history",
                                                subtitle = "View your purchases in the app.",
                                                trailingIcon = TablerIcons.ChevronRight,
                                                onClick = {
                                                    scope.launch {
                                                        history = repo.getPurchaseHistory()
                                                        showHistory = true
                                                    }
                                                }
                                            )
                                        }
                                        if (cfgShowRefund) {
                                            RowDivider()
                                            BillingRow(
                                                icon = TablerIcons.Scale,
                                                title = "Request a refund",
                                                subtitle = "Handled by $store.",
                                                trailingIcon = TablerIcons.ExternalLink,
                                                onClick = { openInAppBrowser(purchaseHistoryUrl) }
                                            )
                                        }
                                    }
                                }
                            }

                            // Transient restore message.
                            if (message != null) {
                                item {
                                    Text(
                                        text = message ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GoldBright,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // c. Legal
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                        SectionHeader(title = "Legal")
                                        Spacer(modifier = Modifier.height(6.dp))
                                        BillingRow(
                                            icon = TablerIcons.Blockquote,
                                            title = "Terms of Use",
                                            subtitle = "The agreement for using Tajly Premium.",
                                            trailingIcon = TablerIcons.ExternalLink,
                                            onClick = { openInAppBrowser(LegalUrls.TERMS_OF_USE) }
                                        )
                                        RowDivider()
                                        BillingRow(
                                            icon = TablerIcons.EyeOff,
                                            title = "Privacy Policy",
                                            subtitle = "How we handle your data.",
                                            trailingIcon = TablerIcons.ExternalLink,
                                            onClick = { openInAppBrowser(LegalUrls.PRIVACY_POLICY) }
                                        )
                                        RowDivider()
                                        BillingRow(
                                            icon = TablerIcons.MessageCircle,
                                            title = "Help & Support",
                                            subtitle = "Questions about your plan? We're here.",
                                            trailingIcon = TablerIcons.ExternalLink,
                                            onClick = { openInAppBrowser(cfgSupportUrl) }
                                        )
                                    }
                                }
                            }

                            // d. Fine print
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = TablerIcons.InfoCircle,
                                        contentDescription = null,
                                        tint = c.textLow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Subscriptions are billed through your $store account and " +
                                            "auto-renew unless cancelled at least 24 hours before the end of " +
                                            "the current period. You can manage or cancel anytime in your " +
                                            "$store settings.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = c.textLow
                                    )
                                }
                            }
                        }
                    }
                }

                // Confirm plan switch — store prorates on the next cycle.
                val pending = planToSwitch
                if (pending != null) {
                    AlertDialog(
                        onDismissRequest = { planToSwitch = null },
                        title = { Text("Switch to ${pending.title}?") },
                        text = {
                            Text(
                                "You'll be charged ${pending.priceString} on your next cycle, " +
                                    "prorated by your store."
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                planToSwitch = null
                                scope.launch {
                                    switching = true
                                    val res = repo.switchPlan(pending, status?.productIdentifier)
                                    switching = false
                                    res.fold(
                                        onSuccess = { ok ->
                                            if (ok) {
                                                message = "Plan changed"
                                                status = repo.getSubscriptionStatus()
                                            } else {
                                                message = "Couldn't change your plan."
                                            }
                                        },
                                        onFailure = { e ->
                                            if (e.message != "cancelled") {
                                                message = "Couldn't change your plan. Please try again."
                                            }
                                        }
                                    )
                                }
                            }) { Text("Switch") }
                        },
                        dismissButton = {
                            TextButton(onClick = { planToSwitch = null }) { Text("Cancel") }
                        }
                    )
                }

                // In-app billing history — native list, no browser. The one external action is
                //    the official Apple/Google receipt (the merchant of record issues invoices).
                if (showHistory) {
                    AlertDialog(
                        onDismissRequest = { showHistory = false },
                        title = { Text("Billing history") },
                        text = {
                            val items = history ?: emptyList()
                            if (items.isEmpty()) {
                                Text("No purchases to show yet.", color = c.textMid)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    items.forEach { e ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    e.productId.replace("_", " ")
                                                        .replaceFirstChar { it.uppercase() },
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = c.textHi
                                                )
                                                Text(
                                                    listOf(e.dateLabel, e.status).filter { it.isNotBlank() }
                                                        .joinToString(" · "),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = c.textMid
                                                )
                                            }
                                            if (e.priceLabel.isNotBlank()) {
                                                Text(
                                                    e.priceLabel,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = c.textHi
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Official receipts & invoices are issued by $store.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = c.textLow
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showHistory = false
                                openInAppBrowser(purchaseHistoryUrl)
                            }) { Text("Official receipt") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showHistory = false }) { Text("Close") }
                        }
                    )
                }
            }
        }
    }
}

/** Human store label — prefers RevenueCat's, falls back to the running platform's store. */
private fun storeLabel(storeName: String?, isAndroid: Boolean): String =
    storeName?.takeIf { it.isNotBlank() } ?: if (isAndroid) "Google Play" else "the App Store"

@Composable
private fun StatusHeroCard(
    status: SubscriptionStatus?,
    store: String,
    onGoPremium: () -> Unit
) {
    val c = TajlyTheme.colors
    val active = status?.isActive == true

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Tajly.GoldGradient, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        goldTint = true,
        tier = GlassTier.Hero
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Tajly.GoldGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = TablerIcons.Crown,
                        contentDescription = null,
                        tint = OnGold,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current plan".uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textMid
                    )
                    Text(
                        text = if (active) "Premium" else "Free plan",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (active) GoldBright else c.textHi
                    )
                }
                if (active) {
                    StatRing(progress = 1f, diameter = 46.dp, strokeWidth = 5.dp) {
                        Icon(
                            imageVector = TablerIcons.CircleCheck,
                            contentDescription = null,
                            tint = GoldBright,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            if (active) {
                Spacer(modifier = Modifier.height(6.dp))
                val exp = status?.expirationRaw
                val line = when {
                    status?.isInTrial == true ->
                        if (exp != null) "Free trial — ends $exp" else "You're on a free trial."
                    status?.willRenew == true ->
                        if (exp != null) "Renews on $exp" else "Auto-renews with your plan."
                    else ->
                        if (exp != null) "Your plan ends $exp (won't renew)" else "Your plan won't renew."
                }
                HairLine()
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = c.textHi
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Billed through $store",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMid
                )

                if (status?.isSandbox == true) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SandboxChip()
                }

                if (status?.hasBillingIssue == true) {
                    Spacer(modifier = Modifier.height(14.dp))
                    BillingIssueBanner()
                }
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                HairLine()
                Text(
                    text = "Unlock AI coaching, unlimited personalised plans and every challenge.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMid
                )
                Spacer(modifier = Modifier.height(18.dp))
                GoldButton(
                    text = "Go Premium",
                    onClick = onGoPremium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BenefitsCard() {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            SectionHeader(title = "What's included")
            Spacer(modifier = Modifier.height(14.dp))
            BenefitRow(TablerIcons.Bolt, "AI coaching", "Ask your coach anything, anytime")
            Spacer(modifier = Modifier.height(14.dp))
            BenefitRow(TablerIcons.Leaf, "Unlimited plans", "Personalised workout & meal plans")
            Spacer(modifier = Modifier.height(14.dp))
            BenefitRow(TablerIcons.Trophy, "Every challenge", "Join challenges and keep your streak")
        }
    }
}

@Composable
private fun SandboxChip() {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(GoldPrimary.copy(alpha = 0.16f))
            .border(1.dp, GoldPrimary.copy(alpha = 0.5f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = "SANDBOX",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = GoldBright
        )
    }
}

@Composable
private fun BillingIssueBanner() {
    val coral = Color(0xFFFF6B6B)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(coral.copy(alpha = 0.12f))
            .border(1.dp, coral.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = TablerIcons.AlertCircle,
            contentDescription = null,
            tint = coral,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = "There's a billing issue with your subscription. Update your payment method to keep Premium.",
            style = MaterialTheme.typography.bodySmall,
            color = TajlyTheme.colors.textHi
        )
    }
}

@Composable
private fun HairLine() {
    val c = TajlyTheme.colors
    Column {
        Spacer(modifier = Modifier.height(14.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(c.hair))
        Spacer(modifier = Modifier.height(14.dp))
    }
}

/** A real Premium benefit — gold circular badge + title + honest one-liner. */
@Composable
private fun BenefitRow(icon: ImageVector, title: String, subtitle: String) {
    val c = TajlyTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldBright,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = c.textHi
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = c.textMid
            )
        }
    }
}

@Composable
private fun BillingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?,
    trailingText: String? = null,
    trailingIcon: ImageVector? = null,
) {
    val c = TajlyTheme.colors
    var rowModifier = Modifier.fillMaxWidth()
    if (onClick != null) {
        rowModifier = rowModifier.clip(RoundedCornerShape(12.dp)).clickable { onClick() }
    }
    Row(
        modifier = rowModifier.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = c.textHi
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = c.textMid
            )
        }
        when {
            trailingText != null -> Text(
                text = trailingText,
                style = MaterialTheme.typography.bodyMedium,
                color = c.textMid
            )
            trailingIcon != null -> Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = c.textLow,
                modifier = Modifier.size(18.dp)
            )
            onClick != null -> Icon(
                imageVector = TablerIcons.ChevronRight,
                contentDescription = null,
                tint = c.textLow
            )
        }
    }
}

@Composable
private fun RowDivider() {
    val c = TajlyTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .height(1.dp)
            .background(c.hair)
    )
}
