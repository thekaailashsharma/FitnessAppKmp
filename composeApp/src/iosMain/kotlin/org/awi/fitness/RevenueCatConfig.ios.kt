@file:OptIn(kotlin.experimental.ExperimentalNativeApi::class)

package org.awi.fitness

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import kotlin.native.Platform

actual val revenueCatApiKey: String =
    if (Platform.isDebugBinary) REVENUE_CAT_TEST_STORE_API_KEY else REVENUE_CAT_APPLE_API_KEY

actual fun initRevenueCat(apiKey: String) {
    try {
        Purchases.configure(apiKey = apiKey)
    } catch (e: Exception) {
        // RevenueCat not yet configured — will retry on next launch
    }
}
