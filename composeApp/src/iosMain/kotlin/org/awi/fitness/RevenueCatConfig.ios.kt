package org.awi.fitness

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure

// Always use the App Store (Apple) key so both debug and release route purchases
//    through the real App Store / sandbox. (Test Store is no longer used on iOS.)
actual val revenueCatApiKey: String = REVENUE_CAT_APPLE_API_KEY

actual fun initRevenueCat(apiKey: String) {
    try {
        Purchases.configure(apiKey = apiKey)
    } catch (e: Exception) {
        // RevenueCat not yet configured — will retry on next launch
    }
}
