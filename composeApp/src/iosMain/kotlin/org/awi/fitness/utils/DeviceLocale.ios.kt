package org.awi.fitness.utils

import platform.Foundation.NSLocale
import platform.Foundation.countryCode
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual fun deviceLocale(): DeviceLocale {
    val locale = NSLocale.currentLocale
    return DeviceLocale(
        language = locale.languageCode ?: "",
        region = locale.countryCode ?: ""
    )
}
