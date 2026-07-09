package org.awi.fitness.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Firestore document shape for a user's billing/entitlement record, stored in a DEDICATED
 * `fitness_testing_subscriptions` collection keyed by the user's email doc id.
 *
 * Kept separate from the community/profile user doc on purpose: profile PATCHes (social links,
 * onboarding, block list) must never be able to clobber billing state — and an admin toggling
 * [premiumOverride] must never be undone by a client profile write.
 *
 * Sources that write here:
 *  - the app (client) after a successful purchase/restore — everything EXCEPT premiumOverride
 *  - the RevenueCat webhook Cloud Function — server-authoritative status
 *  - an admin (manually in Firestore) — premiumOverride to comp a user all plans for free
 */
@Serializable
data class SubscriptionDocFields(
    // Admin comp switch: when true the user is treated as premium with no payment. Only ever
    //    written by an admin / trusted server — the client never touches this field.
    @SerialName("premiumOverride")
    val premiumOverride: BooleanValue? = null,
    // "active" | "expired" | "none" — server/store authoritative entitlement state.
    @SerialName("status")
    val status: StringValue? = null,
    // Active product identifier (e.g. "tajly_monthly").
    @SerialName("plan")
    val plan: StringValue? = null,
    // Human store label: "App Store" / "Play Store" / "RevenueCat".
    @SerialName("store")
    val store: StringValue? = null,
    @SerialName("willRenew")
    val willRenew: BooleanValue? = null,
    @SerialName("isTrial")
    val isTrial: BooleanValue? = null,
    // Raw expiration timestamp from the store/SDK (formatted best-effort by the UI).
    @SerialName("expiry")
    val expiry: StringValue? = null,
    // Who last wrote this record: "client" | "webhook" | "admin".
    @SerialName("source")
    val source: StringValue? = null,
    @SerialName("updatedAt")
    val updatedAt: IntegerValue? = null,
)

@Serializable
data class SubscriptionDocRequest(
    val fields: SubscriptionDocFields
)
