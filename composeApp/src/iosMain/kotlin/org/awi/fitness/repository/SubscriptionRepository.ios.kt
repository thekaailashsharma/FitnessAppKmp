package org.awi.fitness.repository

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitLogIn
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.Package

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
        val current = offerings.current
            ?: return Result.success(hardcodedFallbackOffering())

        val plans = current.availablePackages.map { pkg ->
            val isAnnual = isAnnualPackage(pkg)
            SubscriptionPlan(
                identifier = pkg.identifier,
                title = if (isAnnual) "Annual Plan" else "Monthly Plan",
                priceString = pkg.storeProduct.price.formatted,
                periodType = if (isAnnual) PeriodType.ANNUAL else PeriodType.MONTHLY,
                isBestValue = isAnnual,
                rawPackage = pkg
            )
        }
        Result.success(SubscriptionOffering("default", plans))
    } catch (e: Exception) {
        Result.success(hardcodedFallbackOffering())
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

private fun isAnnualPackage(pkg: Package): Boolean {
    if (pkg.packageType.name.contains("ANNUAL", ignoreCase = true)) return true
    val id = pkg.identifier.lowercase()
    return id.contains("annual") || id.contains("year")
}

private fun hardcodedFallbackOffering() = SubscriptionOffering(
    identifier = "default",
    plans = listOf(
        SubscriptionPlan("annual", "Annual Plan", "€79.99", PeriodType.ANNUAL, isBestValue = true),
        SubscriptionPlan("monthly", "Monthly Plan", "€9.99", PeriodType.MONTHLY, isBestValue = false)
    )
)
