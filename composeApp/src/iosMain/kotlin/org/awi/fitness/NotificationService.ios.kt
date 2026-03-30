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
