package org.awi.fitness

import platform.Foundation.NSTimeZone
import platform.Foundation.NSUserDefaults
import platform.Foundation.defaultTimeZone

actual fun getFcmToken(): String? {
    return NSUserDefaults.standardUserDefaults.stringForKey("fcm_token")
}

actual fun getDeviceTimezone(): String {
    return NSTimeZone.defaultTimeZone.name
}

actual fun getDeviceLanguageCode(): String {
    val langs = platform.Foundation.NSLocale.preferredLanguages
    val first = langs.firstOrNull() as? String ?: "en"
    return first.substringBefore("-").substringBefore("_")
}
