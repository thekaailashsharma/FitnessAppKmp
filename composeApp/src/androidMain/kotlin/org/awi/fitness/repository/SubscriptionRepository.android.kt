package org.awi.fitness.repository

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitLogIn
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.Package
import kotlinx.datetime.toLocalDateTime

actual suspend fun revenueCatCheckPremium(): Boolean {
    return try {
        Purchases.sharedInstance.awaitCustomerInfo().hasPremiumAccess()
    } catch (e: Exception) {
        false
    }
}

actual suspend fun revenueCatGetOfferings(): Result<SubscriptionOffering> {
    return try {
        val offerings = Purchases.sharedInstance.awaitOfferings()
        // Fully dynamic: only ever show what RevenueCat actually returns. No fabricated plans.
        val current = offerings.current
            ?: return Result.failure(Exception("No current offering configured"))

        val plans = current.availablePackages.map { pkg ->
            val isAnnual = isAnnualPackage(pkg)
            SubscriptionPlan(
                identifier = pkg.identifier,
                title = if (isAnnual) "Annual Plan" else "Monthly Plan",
                priceString = pkg.storeProduct.price.formatted,
                periodType = if (isAnnual) PeriodType.ANNUAL else PeriodType.MONTHLY,
                isBestValue = isAnnual,
                rawPackage = pkg,
                storeProductId = pkg.storeProduct.id
            )
        }
        if (plans.isEmpty()) return Result.failure(Exception("No products available"))
        Result.success(SubscriptionOffering("default", plans))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

actual suspend fun revenueCatPurchasePlan(plan: SubscriptionPlan): Result<Boolean> {
    val rawPkg = plan.rawPackage ?: return Result.failure(Exception("No package available"))
    return try {
        val purchase = Purchases.sharedInstance.awaitPurchase(rawPkg as Package)
        if (purchase.customerInfo.hasPremiumAccess()) return Result.success(true)
        val refreshed = Purchases.sharedInstance.awaitCustomerInfo()
        Result.success(refreshed.hasPremiumAccess())
    } catch (e: Exception) {
        if (e.message?.contains("cancel", ignoreCase = true) == true) {
            Result.failure(Exception("cancelled"))
        } else {
            Result.failure(e)
        }
    }
}

actual suspend fun revenueCatRestorePurchases(): Result<Boolean> {
    return try {
        val customerInfo = Purchases.sharedInstance.awaitRestore()
        Result.success(customerInfo.hasPremiumAccess())
    } catch (e: Exception) {
        Result.failure(e)
    }
}

actual suspend fun revenueCatLogIn(userId: String): Result<Unit> {
    return try {
        Purchases.sharedInstance.awaitLogIn(userId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@OptIn(kotlin.time.ExperimentalTime::class)
actual suspend fun revenueCatSubscriptionStatus(): SubscriptionStatus {
    return try {
        val info = Purchases.sharedInstance.awaitCustomerInfo()
        val ent = info.entitlements.all["premium"]?.takeIf { it.isActive }
            ?: info.entitlements.active.values.firstOrNull()
        if (ent == null) {
            SubscriptionStatus(isActive = false, managementUrl = info.managementUrlString)
        } else {
            SubscriptionStatus(
                isActive = true,
                productIdentifier = ent.productIdentifier,
                storeName = storeLabel(ent.store),
                willRenew = ent.willRenew,
                isInTrial = ent.periodType == com.revenuecat.purchases.kmp.models.PeriodType.TRIAL,
                expirationRaw = ent.expirationDate?.toString()?.take(10),
                hasBillingIssue = ent.billingIssueDetectedAt != null,
                isSandbox = ent.isSandbox,
                managementUrl = info.managementUrlString
            )
        }
    } catch (e: Exception) {
        // Total failure (no network AND no cache) — mark unverified so the gate applies grace.
        SubscriptionStatus(isActive = false, verified = false)
    }
}

actual suspend fun revenueCatSwitchPlan(
    newPlan: SubscriptionPlan,
    currentProductId: String?
): Result<Boolean> {
    val rawPkg = newPlan.rawPackage as? Package ?: return Result.failure(Exception("No package available"))
    return try {
        val purchase = if (currentProductId != null) {
            // Google Play upgrade/downgrade: pass the old product + a proration replacement mode.
            Purchases.sharedInstance.awaitPurchase(
                rawPkg,
                oldProductId = currentProductId,
                replacementMode = com.revenuecat.purchases.kmp.models.GoogleReplacementMode.CHARGE_PRORATED_PRICE
            )
        } else {
            Purchases.sharedInstance.awaitPurchase(rawPkg)
        }
        Result.success(purchase.customerInfo.hasPremiumAccess())
    } catch (e: Exception) {
        if (e.message?.contains("cancel", ignoreCase = true) == true) Result.failure(Exception("cancelled"))
        else Result.failure(e)
    }
}

actual suspend fun revenueCatPurchaseHistory(): List<PurchaseHistoryEntry> {
    return try {
        val info = Purchases.sharedInstance.awaitCustomerInfo()
        info.subscriptionsByProductIdentifier.entries
            .sortedByDescending { it.value.purchaseDateMillis ?: 0L }
            .map { (pid, sub) ->
                PurchaseHistoryEntry(
                    productId = pid,
                    dateLabel = sub.purchaseDateMillis?.let { formatHistoryDate(it) } ?: "",
                    priceLabel = sub.price?.formatted ?: "",
                    status = when {
                        sub.periodType == com.revenuecat.purchases.kmp.models.PeriodType.TRIAL -> "Trial"
                        sub.isActive -> "Active"
                        else -> "Expired"
                    }
                )
            }
    } catch (e: Exception) {
        emptyList()
    }
}

private val histMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
private fun formatHistoryDate(millis: Long): String {
    val d = kotlinx.datetime.Instant.fromEpochMilliseconds(millis)
        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
    return "${d.dayOfMonth} ${histMonths[d.monthNumber - 1]} ${d.year}"
}

private fun storeLabel(store: com.revenuecat.purchases.kmp.models.Store): String = when (store.name) {
    "APP_STORE", "MAC_APP_STORE" -> "App Store"
    "PLAY_STORE" -> "Google Play"
    "AMAZON" -> "Amazon Appstore"
    "STRIPE", "RC_BILLING" -> "Web"
    "PROMOTIONAL" -> "Promotional"
    else -> "Store"
}

private fun isAnnualPackage(pkg: Package): Boolean {
    if (pkg.packageType.name.contains("ANNUAL", ignoreCase = true)) return true
    val id = pkg.identifier.lowercase()
    return id.contains("annual") || id.contains("year")
}

