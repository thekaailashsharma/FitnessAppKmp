package org.awi.fitness.repository

import org.awi.fitness.utils.currentTimeMillis
import org.awi.fitness.model.*
import org.awi.fitness.network.ApiService

/**
 * Seeds the "TAJLY" community presence so the feed is never empty on a fresh install.
 *
 * This creates (or refreshes) a single TAJLY user profile plus a few motivational welcome
 * posts using FIXED document ids and PATCH (create-or-update) semantics — so running it
 * any number of times never duplicates content.
 *
 * The posting shape mirrors real user posts exactly (same fields, same collections), so a
 * founder's Firebase Function can later post additional TAJLY updates with the same shape and
 * they will surface automatically in the existing feed query.
 *
 * All errors are swallowed — seeding is best-effort and must never block the feed.
 */
class TajlyRepository : ApiService() {

    companion object {
        private const val PROJECT_ID = "awi-fitness-app"
        private const val BASE_URL =
            "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents"
        private const val POSTS_COLLECTION = "fitness_testing_posts"
        private const val USERS_COLLECTION = "fitness_testing_users"

        // Stable TAJLY identity — shared with the founder's Firebase Function.
        private const val TAJLY_ID = "tajly@tajly.app"
        private const val TAJLY_NAME = "TAJLY"
        private const val TAJLY_USERNAME = "@tajly"
        private const val TAJLY_BIO = "Your fitness home. Move daily, celebrate every win."
    }

    /**
     * Ensures ONLY the TAJLY user profile exists (so the AI Cloud Function's posts render with
     * an author). We no longer seed static welcome posts — those were re-stamped with a fresh
     * timestamp on every load, so they were always pinned to the top and never varied. The real
     * TAJLY posts come from the AI-driven `tajlyAutoPost` Cloud Function. Best-effort.
     */
    suspend fun ensureTajlySeed(): Result<Unit> {
        return try {
            val token = userSettings.authToken ?: return Result.success(Unit)
            // TAJLY user profile — updateMask so we never clobber the profile image or other fields.
            try {
                val userUrl = "$BASE_URL/$USERS_COLLECTION/$TAJLY_ID" +
                    "?updateMask.fieldPaths=email" +
                    "&updateMask.fieldPaths=displayName" +
                    "&updateMask.fieldPaths=username" +
                    "&updateMask.fieldPaths=bio"
                val userRequest = CommunityUserFirestoreRequest(
                    fields = CommunityUserFields(
                        email = StringValue(TAJLY_ID),
                        displayName = StringValue(TAJLY_NAME),
                        username = StringValue(TAJLY_USERNAME),
                        bio = StringValue(TAJLY_BIO),
                    )
                )
                patch<FirestoreDocument<CommunityUserFields>>(userUrl, userRequest, token)
            } catch (e: Exception) {
                println("TajlyRepository.ensureTajlySeed user error: ${e.message}")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            println("TajlyRepository.ensureTajlySeed error: ${e.message}")
            Result.success(Unit)
        }
    }
}
