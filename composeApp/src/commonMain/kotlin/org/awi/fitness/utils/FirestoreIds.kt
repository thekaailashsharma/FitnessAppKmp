package org.awi.fitness.utils

/** Safe Firestore document id for values like email addresses.
 *  Uses a single consistent encoding: replace special chars with underscored tokens.
 *  kailashps.1011@gmail.com → kailashps_DOT_1011_AT_gmail_DOT_com
 */
fun toFirestoreDocId(value: String): String =
    value
        .replace(".", "_DOT_")
        .replace("@", "_AT_")
        .replace("/", "_SLASH_")

/** Public @handle for a community profile.
 *  Prefers a slug of the user's chosen display name (e.g. "Nandini Singh" → "@nandinisingh")
 *  so the handle matches the identity the user set in onboarding, and only falls back to the
 *  email local-part when no usable name exists. Purely a display string — the identity key
 *  everywhere remains the email/userId, so this is safe to derive from the name.
 */
fun communityHandle(displayName: String?, email: String): String {
    val slug = displayName
        ?.lowercase()
        ?.filter { it.isLetterOrDigit() }
        ?.takeIf { it.isNotBlank() }
    return "@${slug ?: email.substringBefore("@")}"
}
