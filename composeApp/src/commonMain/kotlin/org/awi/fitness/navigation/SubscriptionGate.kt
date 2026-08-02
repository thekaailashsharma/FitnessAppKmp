package org.awi.fitness.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.launch
import org.awi.fitness.data.UserSettings
import org.awi.fitness.repository.FeatureGatingRepository
import org.awi.fitness.repository.SubscriptionRepository
import org.awi.fitness.utils.currentTimeMillis

/** Outcome of a premium check — decides which route a paid surface should sit on. */
enum class PremiumGate { ALLOWED, BILLING_ISSUE, LOCKED }

/**
 * Single source of truth for "can this user be inside the app right now".
 * - active, no billing issue        → ALLOWED
 * - active but store billing issue   → BILLING_ISSUE (send to Fix-Payment)
 * - confirmed not premium            → LOCKED (send to Paywall)
 * - couldn't verify (offline/no cache) → grace window: ALLOWED if last online verify is
 *     within [premiumGraceHours], else LOCKED.
 * Records the last online-verified timestamp for the grace window.
 */
suspend fun evaluatePremiumGate(
    settings: UserSettings = UserSettings.getInstance(),
    repo: SubscriptionRepository = SubscriptionRepository(),
): PremiumGate {
    // CRITICAL: ensure RevenueCat is identified as THIS user before reading entitlement.
    //    configure() starts anonymous and logIn is deferred at launch, so without this the gate
    //    can read the anonymous customer (no premium) and wrongly LOCK an active subscriber.
    //    logIn is idempotent (no-ops if already the same user), so this is cheap on every check.
    val email = settings.userEmail
    if (settings.isLoggedIn && !email.isNullOrBlank()) {
        try { repo.logIn(email) } catch (_: Exception) {}
    }
    val status = repo.getSubscriptionStatus()
    val now = currentTimeMillis()
    return when {
        status.verified && status.isActive && status.hasBillingIssue -> {
            settings.premiumLastVerifiedAt = now
            PremiumGate.BILLING_ISSUE
        }
        status.verified && status.isActive -> {
            settings.premiumLastVerifiedAt = now
            PremiumGate.ALLOWED
        }
        status.verified -> PremiumGate.LOCKED // verified & not active
        else -> {
            // Total verification failure → honour the offline grace window.
            val graceMs = settings.premiumGraceHours.toLong() * 3_600_000L
            val last = settings.premiumLastVerifiedAt
            if (last > 0L && now - last < graceMs) PremiumGate.ALLOWED else PremiumGate.LOCKED
        }
    }
}

/**
 * Re-verifies entitlement every time the app returns to the foreground and re-routes if the
 * subscription expired / hit a billing issue / recovered — so the app can't be used past expiry.
 * Renders nothing; only guards when the user is on a paid surface (Main / FixPayment).
 */
@Composable
fun SubscriptionGate(appNavigation: AppNavigationController) {
    val settings = remember { UserSettings.getInstance() }
    val repo = remember { SubscriptionRepository() }
    val scope = rememberCoroutineScope()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        scope.launch {
            val route = appNavigation.currentRoute
            if (route != RootRoute.Main && route != RootRoute.FixPayment) return@launch
            // Throttle on Main: if premium was verified very recently, skip the network re-check on
            //    quick foregrounds (avoids nagging + redundant calls). FixPayment always re-checks
            //    so recovery is instant. A real subscription can't expire within this window; only
            //    time-compressed sandbox subs do, and they'll be caught on the next check.
            if (route == RootRoute.Main) {
                val since = currentTimeMillis() - settings.premiumLastVerifiedAt
                if (settings.premiumLastVerifiedAt > 0L && since < 10 * 60_000L) return@launch
            }
            when (evaluatePremiumGate(settings, repo)) {
                PremiumGate.BILLING_ISSUE ->
                    if (route != RootRoute.FixPayment) appNavigation.navigateTo(RootRoute.FixPayment)
                PremiumGate.LOCKED ->
                    // Freemium: if the paywall is dismissible, a non-premium user is allowed to
                    //    stay in the app on the free tier — don't bounce them out on every resume.
                    //    Only hard-gate to the paywall when it's configured non-dismissible.
                    if (!FeatureGatingRepository.currentConfig().paywall.dismissible) {
                        appNavigation.navigateTo(RootRoute.Paywall)
                    }
                PremiumGate.ALLOWED ->
                    if (route == RootRoute.FixPayment) appNavigation.navigateTo(RootRoute.Main)
            }
        }
    }
}
