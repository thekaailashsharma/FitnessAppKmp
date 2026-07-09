package org.awi.fitness.utils

/**
 * Opens [url] in the OS default handler (NOT an in-app browser). For store deep-links this hands
 * the URL to the native app — e.g. apps.apple.com/account/subscriptions opens the App Store's own
 * Manage Subscriptions UI, and play.google.com/store/account/subscriptions opens the Play app.
 * Use this for store/subscription management links; use openInAppBrowser for web content (legal/help).
 */
expect fun openExternalUrl(url: String)
