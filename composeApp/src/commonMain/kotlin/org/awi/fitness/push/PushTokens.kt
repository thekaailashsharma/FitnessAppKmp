package org.awi.fitness.push

/**
 * Platform push-token provider for retention notifications.
 *
 * Returns the FCM registration token for this device, or null when unavailable —
 * e.g. no Firebase config present, notification permission denied, or (currently on
 * iOS) Firebase Messaging not yet integrated at the Xcode layer.
 *
 * The token is written to the user's `fitness_testing_users` doc so the
 * `tajlyRetentionPush` Cloud Function can deliver personalized nudges.
 */
expect suspend fun currentPushToken(): String?
