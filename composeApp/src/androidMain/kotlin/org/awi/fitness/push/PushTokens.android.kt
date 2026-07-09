package org.awi.fitness.push

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Fetches the FCM registration token via Firebase Messaging. Token retrieval does not
 * require the POST_NOTIFICATIONS runtime permission (that only gates display), so this
 * works as soon as google-services.json is present and Firebase is initialized.
 */
actual suspend fun currentPushToken(): String? =
    try {
        suspendCancellableCoroutine { cont ->
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    cont.resume(if (task.isSuccessful) task.result else null)
                }
        }
    } catch (e: Throwable) {
        null
    }
