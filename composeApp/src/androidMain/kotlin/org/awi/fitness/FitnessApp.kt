package org.awi.fitness

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class FitnessApp : Application() {
    companion object {
        lateinit var context: Context
            private set

        // Must match CHANNEL_ID in tajly-functions and the manifest default channel meta-data.
        const val REMINDERS_CHANNEL_ID = "tajly_reminders"
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        createRemindersChannel()
    }

    // On Android 8+ (API 26) a notification channel MUST exist or FCM notifications are
    // silently dropped. Create it up-front so retention pushes always display.
    private fun createRemindersChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                REMINDERS_CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily motivation, fueling and streak reminders"
            }
            manager?.createNotificationChannel(channel)
        }
    }
}
