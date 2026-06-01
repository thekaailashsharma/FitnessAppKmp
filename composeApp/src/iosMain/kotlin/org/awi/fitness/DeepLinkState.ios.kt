package org.awi.fitness

import platform.Foundation.NSUserDefaults

actual fun consumeDeepLinkEmail(): String? {
    val defaults = NSUserDefaults.standardUserDefaults
    val email = defaults.stringForKey("deep_link_email")
    if (!email.isNullOrBlank()) {
        defaults.removeObjectForKey("deep_link_email")
        return email
    }
    return null
}
