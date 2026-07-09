package org.awi.fitness.utils

/**
 * Lightweight snapshot of the device's current locale.
 * No runtime permissions required on any platform.
 */
data class DeviceLocale(
    val language: String,
    val region: String
)

/** Returns the device's current language / region codes. */
expect fun deviceLocale(): DeviceLocale
