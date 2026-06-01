package org.awi.fitness.repository

import com.revenuecat.purchases.kmp.models.CustomerInfo

/** True when user has premium — checks `premium` id and any active entitlement (Test Store). */
fun CustomerInfo.hasPremiumAccess(): Boolean {
    if (entitlements["premium"]?.isActive == true) return true
    return entitlements.active.values.any { it.isActive }
}
