package org.awi.fitness.utils

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openExternalUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    // Open externally so iOS routes store URLs to the native App Store / Settings UI
    //    instead of an in-app Safari sheet.
    UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
}
