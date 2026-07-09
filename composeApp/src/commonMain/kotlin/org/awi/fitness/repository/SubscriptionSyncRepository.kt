package org.awi.fitness.repository

import io.ktor.http.isSuccess
import org.awi.fitness.model.BooleanValue
import org.awi.fitness.model.FirestoreDocument
import org.awi.fitness.model.IntegerValue
import org.awi.fitness.model.StringValue
import org.awi.fitness.model.SubscriptionDocFields
import org.awi.fitness.model.SubscriptionDocRequest
import org.awi.fitness.network.ApiService
import org.awi.fitness.repository.ClientRepository.Companion.PROJECT_ID
import org.awi.fitness.utils.currentTimeMillis
import org.awi.fitness.utils.toFirestoreDocId

/**
 * Reads/writes the user's billing record in the dedicated `fitness_testing_subscriptions`
 * collection. Kept out of the profile doc so profile writes can never clobber billing state.
 *
 *  - [hasFirestoreEntitlement] powers the admin comp-override (premiumOverride) and lets a
 *    verified server status grant access even when the on-device RevenueCat cache is cold.
 *  - [writeSnapshot] mirrors the current store subscription into Firestore after purchase/restore
 *    so the founder can see who is subscribed to what without opening RevenueCat/App Store Connect.
 *
 * All methods are best-effort and never throw.
 */
class SubscriptionSyncRepository : ApiService() {

    companion object {
        private const val BASE_URL =
            "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents"
        private const val COLLECTION = "fitness_testing_subscriptions"
    }

    /** True when an admin has comped this user (premiumOverride) OR the server marks them active. */
    suspend fun hasFirestoreEntitlement(): Boolean {
        val token = userSettings.authToken ?: return false
        val email = userSettings.userEmail ?: return false
        return try {
            val url = "$BASE_URL/$COLLECTION/${toFirestoreDocId(email)}"
            val (resp, status) = get<FirestoreDocument<SubscriptionDocFields>>(url, token)
            if (!status.isSuccess()) return false
            val f = resp.fields ?: return false
            f.premiumOverride?.value == true || f.status?.value == "active"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Mirrors the current store subscription into Firestore. Uses updateMask so it ONLY writes
     * billing fields — never premiumOverride — so an admin comp is never overwritten by the client.
     */
    suspend fun writeSnapshot(status: SubscriptionStatus) {
        val token = userSettings.authToken ?: return
        val email = userSettings.userEmail ?: return
        try {
            val url = "$BASE_URL/$COLLECTION/${toFirestoreDocId(email)}" +
                "?updateMask.fieldPaths=status" +
                "&updateMask.fieldPaths=plan" +
                "&updateMask.fieldPaths=store" +
                "&updateMask.fieldPaths=willRenew" +
                "&updateMask.fieldPaths=isTrial" +
                "&updateMask.fieldPaths=expiry" +
                "&updateMask.fieldPaths=source" +
                "&updateMask.fieldPaths=updatedAt"
            val req = SubscriptionDocRequest(
                SubscriptionDocFields(
                    status = StringValue(if (status.isActive) "active" else "none"),
                    plan = status.productIdentifier?.takeIf { it.isNotBlank() }?.let { StringValue(it) },
                    store = status.storeName?.takeIf { it.isNotBlank() }?.let { StringValue(it) },
                    willRenew = BooleanValue(status.willRenew),
                    isTrial = BooleanValue(status.isInTrial),
                    expiry = status.expirationRaw?.takeIf { it.isNotBlank() }?.let { StringValue(it) },
                    source = StringValue("client"),
                    updatedAt = IntegerValue(currentTimeMillis().toString()),
                )
            )
            patch<FirestoreDocument<SubscriptionDocFields>>(url, req, token)
        } catch (e: Exception) {
            println("SubscriptionSyncRepository.writeSnapshot error: ${e.message}")
        }
    }
}
