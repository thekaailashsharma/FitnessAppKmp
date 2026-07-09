package org.awi.fitness.utils

import android.content.Intent
import android.net.Uri
import org.awi.fitness.FitnessApp

actual fun openExternalUrl(url: String) {
    val ctx = FitnessApp.context
    try {
        // ACTION_VIEW lets the Play Store app handle its own subscription/order URLs natively.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(intent)
    } catch (_: Exception) {
        // No handler — silently ignore.
    }
}
