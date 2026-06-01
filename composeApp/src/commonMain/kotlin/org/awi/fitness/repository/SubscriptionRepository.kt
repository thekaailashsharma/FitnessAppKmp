package org.awi.fitness.repository

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Represents a subscription plan shown in the paywall.
 */
data class SubscriptionPlan(
    val identifier: String,
    val title: String,
    val priceString: String,
    val periodType: PeriodType,
    val isBestValue: Boolean = false,
    val rawPackage: Any? = null
)

enum class PeriodType { MONTHLY, ANNUAL, TRIAL, UNKNOWN }

/**
 * Offering returned from RevenueCat containing available plans.
 */
data class SubscriptionOffering(
    val identifier: String,
    val plans: List<SubscriptionPlan>
) {
    val annualPlan: SubscriptionPlan? get() = plans.firstOrNull { it.periodType == PeriodType.ANNUAL }
    val monthlyPlan: SubscriptionPlan? get() = plans.firstOrNull { it.periodType == PeriodType.MONTHLY }
}

class SubscriptionRepository {

    /**
     * Returns true if the user has an active "premium" entitlement.
     */
    suspend fun checkPremiumAccess(): Boolean = revenueCatCheckPremium()

    /**
     * Fetches available subscription offerings from RevenueCat.
     * Falls back to hardcoded plans on failure so the paywall always renders.
     */
    suspend fun getOfferings(): Result<SubscriptionOffering> = revenueCatGetOfferings()

    /**
     * Initiates purchase of the selected plan.
     */
    suspend fun purchasePlan(plan: SubscriptionPlan): Result<Boolean> = revenueCatPurchasePlan(plan)

    /**
     * Restores previously purchased subscriptions.
     */
    suspend fun restorePurchases(): Result<Boolean> = revenueCatRestorePurchases()

    /**
     * Logs in the user in RevenueCat (for cross-device entitlements).
     */
    suspend fun logIn(userId: String): Result<Unit> = revenueCatLogIn(userId)
}

// ---------------------------------------------------------------------------
// Expect/actual bridge for RevenueCat — platform specifics in androidMain/iosMain
// ---------------------------------------------------------------------------

expect suspend fun revenueCatCheckPremium(): Boolean
expect suspend fun revenueCatGetOfferings(): Result<SubscriptionOffering>
expect suspend fun revenueCatPurchasePlan(plan: SubscriptionPlan): Result<Boolean>
expect suspend fun revenueCatRestorePurchases(): Result<Boolean>
expect suspend fun revenueCatLogIn(userId: String): Result<Unit>
