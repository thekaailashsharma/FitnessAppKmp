package org.awi.fitness

/**
 * Public legal / support links surfaced in the paywall and subscription-management screen.
 *
 * ⚠️ Apple Guideline 3.1.2 requires FUNCTIONAL Terms of Use (EULA) + Privacy Policy links on
 *    any screen that sells a subscription. Replace the placeholder URLs below with your real
 *    hosted pages BEFORE submitting to App Review. These are public (not secrets), so they live
 *    in source rather than local.properties.
 *
 * The store deep-links (manage/cancel, purchase history, refunds) are correct as-is — those are
 * Apple/Google system endpoints, not ours.
 */
object LegalUrls {
    // TODO: replace with real hosted pages before submission.
    const val TERMS_OF_USE = "https://tajly.app/terms"          // EULA / Terms of Use
    const val PRIVACY_POLICY = "https://tajly.app/privacy"
    const val SUPPORT = "https://tajly.app/support"

    // Apple system endpoints (do not change) — used on iOS.
    const val APPLE_MANAGE_SUBSCRIPTIONS = "https://apps.apple.com/account/subscriptions"
    const val APPLE_PURCHASE_HISTORY = "https://reportaproblem.apple.com"   // invoices, receipts & refunds

    // Google Play system endpoints (do not change) — used on Android.
    const val PLAY_MANAGE_SUBSCRIPTIONS = "https://play.google.com/store/account/subscriptions"
    const val PLAY_PURCHASE_HISTORY = "https://play.google.com/store/account/orderhistory"
}
