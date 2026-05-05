package org.awi.fitness

expect fun consumeDeepLinkEmail(): String?

object DeepLinkState {
    var pendingEmail: String? = null

    fun consumeEmail(): String? {
        val platformEmail = consumeDeepLinkEmail()
        if (platformEmail != null) return platformEmail

        val email = pendingEmail
        pendingEmail = null
        return email
    }
}
