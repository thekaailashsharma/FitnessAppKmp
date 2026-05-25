package org.awi.fitness

import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.NSUserDefaults
import platform.Foundation.defaultTimeZone
import platform.Foundation.preferredLanguages

actual fun getFcmToken(): String? {
    return NSUserDefaults.standardUserDefaults.stringForKey("fcm_token")
}

actual fun getDeviceTimezone(): String {
    return NSTimeZone.defaultTimeZone.name
}

actual fun getDeviceLanguageCode(): String {
    val first = NSLocale.preferredLanguages.firstOrNull() as? String ?: "en"
    return first.substringBefore("-").substringBefore("_")
}
