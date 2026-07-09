package org.awi.fitness.utils

import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import org.awi.fitness.FitnessApp

actual fun openInAppBrowser(url: String) {
    val ctx = FitnessApp.context
    try {
        val tabs = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        tabs.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        tabs.launchUrl(ctx, Uri.parse(url))
    } catch (_: Exception) {
        // Fallback to a plain browser intent if Custom Tabs is unavailable
        val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(fallback)
    }
}
