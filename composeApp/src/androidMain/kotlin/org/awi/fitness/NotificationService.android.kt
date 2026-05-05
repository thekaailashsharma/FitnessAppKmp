package org.awi.fitness

import android.content.Context
import java.util.TimeZone

actual fun getFcmToken(): String? {
    return FitnessApp.context
        .getSharedPreferences("fcm", Context.MODE_PRIVATE)
        .getString("fcm_token", null)
}

actual fun getDeviceTimezone(): String {
    return TimeZone.getDefault().id
}

actual fun getDeviceLanguageCode(): String {
    return java.util.Locale.getDefault().language
}
