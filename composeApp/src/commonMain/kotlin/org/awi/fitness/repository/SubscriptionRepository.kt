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
    val rawPackage: Any? = null,
    // Store product id (e.g. "tajly_monthly") — matches SubscriptionStatus.productIdentifier so the
    //    UI can tell which plan is the currently-active one when offering a switch.
    val storeProductId: String = "",
    // Free-trial / intro-offer info surfaced from the store product (null when none).
    //    Lets the paywall show trial copy ONLY when a real offer exists.
    val hasFreeTrial: Boolean = false,
    val freeTrialLabel: String? = null
)

enum class PeriodType { MONTHLY, ANNUAL, TRIAL, UNKNOWN }

/**
 * One line of in-app billing history, derived from RevenueCat's CustomerInfo. This is NOT an
 * official invoice (only Apple/Google issue those) — it's a native summary so users see their
 * purchases without leaving the app.
 */
data class PurchaseHistoryEntry(
    val productId: String,
    val dateLabel: String,   // purchase date, pre-formatted (may be empty)
    val priceLabel: String,  // e.g. "€9.99" (may be empty)
    val status: String       // "Active" / "Trial" / "Expired"
)

/**
 * Current subscription state derived from RevenueCat's CustomerInfo — drives the
 * subscription-management screen. All fields are best-effort and safe when there is
 * no active subscription.
 */
data class SubscriptionStatus(
    val isActive: Boolean = false,
    val productIdentifier: String? = null,
    val storeName: String? = null,        // human label: "App Store" / "Play Store" / …
    val willRenew: Boolean = false,
    val isInTrial: Boolean = false,
    val expirationRaw: String? = null,    // raw expiration from the SDK (UI formats best-effort)
    val hasBillingIssue: Boolean = false,
    val isSandbox: Boolean = false,
    val managementUrl: String? = null,    // deep link to the store's manage/cancel page
    // false only when we could NOT reach the store/SDK at all (total failure). A cached
    //    "not premium" still counts as verified=true. Drives the offline grace window.
    val verified: Boolean = true
)

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
     *
     * Access is granted if EITHER the store (RevenueCat) reports premium OR Firestore does — the
     * latter covers an admin comp-override (premiumOverride) and a server-verified active status
     * even when the on-device RevenueCat cache is cold. Store check runs first (fast/cached).
     */
    suspend fun checkPremiumAccess(): Boolean {
        val fromStore = try { revenueCatCheckPremium() } catch (e: Exception) { false }
        if (fromStore) return true
        return SubscriptionSyncRepository().hasFirestoreEntitlement()
    }

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

    /**
     * Rich current-subscription state for the management screen. Never throws.
     */
    suspend fun getSubscriptionStatus(): SubscriptionStatus = revenueCatSubscriptionStatus()

    /**
     * Switches the active subscription to [newPlan] (upgrade/downgrade). Native on both platforms:
     * iOS auto-prorates within the subscription group; Android applies a proration replacement
     * mode against [currentProductId]. Returns true when premium is active afterwards.
     */
    suspend fun switchPlan(newPlan: SubscriptionPlan, currentProductId: String?): Result<Boolean> =
        revenueCatSwitchPlan(newPlan, currentProductId)

    /** Native, in-app billing history (not an official invoice). Never throws; empty on failure. */
    suspend fun getPurchaseHistory(): List<PurchaseHistoryEntry> = revenueCatPurchaseHistory()
}

// ---------------------------------------------------------------------------
// Expect/actual bridge for RevenueCat — platform specifics in androidMain/iosMain
// ---------------------------------------------------------------------------

expect suspend fun revenueCatCheckPremium(): Boolean
expect suspend fun revenueCatGetOfferings(): Result<SubscriptionOffering>
expect suspend fun revenueCatPurchasePlan(plan: SubscriptionPlan): Result<Boolean>
expect suspend fun revenueCatRestorePurchases(): Result<Boolean>
expect suspend fun revenueCatLogIn(userId: String): Result<Unit>
expect suspend fun revenueCatSubscriptionStatus(): SubscriptionStatus
expect suspend fun revenueCatSwitchPlan(newPlan: SubscriptionPlan, currentProductId: String?): Result<Boolean>
expect suspend fun revenueCatPurchaseHistory(): List<PurchaseHistoryEntry>
