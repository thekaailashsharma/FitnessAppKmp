package org.awi.fitness.utils

import java.util.Locale

actual fun deviceLocale(): DeviceLocale {
    val locale = Locale.getDefault()
    return DeviceLocale(
        language = locale.language ?: "",
        region = locale.country ?: ""
    )
}
