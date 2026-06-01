package org.awi.fitness

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure

actual val revenueCatApiKey: String =
    if (BuildConfig.DEBUG) REVENUE_CAT_TEST_STORE_API_KEY else REVENUE_CAT_GOOGLE_API_KEY

actual fun initRevenueCat(apiKey: String) {
    Purchases.configure(apiKey = apiKey)
}
