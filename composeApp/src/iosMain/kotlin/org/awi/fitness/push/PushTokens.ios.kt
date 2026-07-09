package org.awi.fitness.push

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.awi.fitness.data.UserSettings
import org.awi.fitness.repository.ChallengesRepository

/**
 * On iOS the FCM token is owned by the Firebase SDK on the Swift side. Swift hands it to
 * Kotlin via [TajlyPush.updateFcmToken]; we cache it in settings and (if already logged in)
 * register it immediately. On login, App.kt calls [currentPushToken] which returns the cached
 * value, so registration also happens the moment the user signs in.
 */
actual suspend fun currentPushToken(): String? {
    val t = UserSettings.getInstance().cachedFcmToken
    println("[PUSH] currentPushToken() -> ${if (t.isNullOrBlank()) "null/empty (Swift hasn't delivered yet)" else "present len=${t.length}"}")
    return t
}

/**
 * Bridge called from Swift (AppDelegate → MessagingDelegate). Exposed to Swift as
 * `TajlyPush().updateFcmToken(token:)`.
 */
object TajlyPush {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun updateFcmToken(token: String) {
        println("[PUSH] TajlyPush.updateFcmToken() received from Swift, len=${token.length}")
        if (token.isBlank()) { println("[PUSH] TajlyPush: blank token, ignoring"); return }
        UserSettings.getInstance().cachedFcmToken = token
        println("[PUSH] TajlyPush: cached token; attempting immediate register")
        // Best-effort immediate register (no-op if not authenticated yet).
        scope.launch {
            runCatching { ChallengesRepository().registerPushToken(token) }
                .onFailure { println("[PUSH] TajlyPush: register threw ${it.message}") }
        }
    }
}
