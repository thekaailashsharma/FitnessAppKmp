package org.awi.fitness

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure

// Always use the Google Play key so both debug and release route purchases through real Play
//    billing / license-tester testing. (Test Store is no longer used on Android.)
actual val revenueCatApiKey: String = REVENUE_CAT_GOOGLE_API_KEY

actual fun initRevenueCat(apiKey: String) {
    Purchases.configure(apiKey = apiKey)
}
