package org.awi.fitness.repository

import io.ktor.http.isSuccess
import org.awi.fitness.model.AppConfig
import org.awi.fitness.model.AppConfigFields
import org.awi.fitness.model.Backdrop
import org.awi.fitness.model.BackdropFields
import org.awi.fitness.model.FirestoreDocument
import org.awi.fitness.model.FirestoreListResponse
import org.awi.fitness.model.toAppConfig
import org.awi.fitness.model.toBackdrop
import org.awi.fitness.network.ApiService

/**
 * Fetches remote-controllable backdrops and app configuration from Firestore.
 * All errors degrade gracefully to safe defaults so callers can always fall back
 * to bundled drawables.
 */
class BackdropRepository : ApiService() {

    companion object {
        private const val PROJECT_ID = "awi-fitness-app"
        private const val BASE_URL =
            "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents"
        private const val BACKDROPS_COLLECTION = "app_backdrops"
        private const val CONFIG_COLLECTION = "app_config"
    }

    suspend fun fetchBackdrops(): Result<List<Backdrop>> {
        return try {
            val token = userSettings.authToken ?: return Result.success(emptyList())
            val url = "$BASE_URL/$BACKDROPS_COLLECTION?pageSize=100"

            val (response, status) = get<FirestoreListResponse<BackdropFields>>(url, token)

            if (!status.isSuccess()) {
                // Collection may not exist yet — degrade to empty list.
                return Result.success(emptyList())
            }

            val backdrops = response.documents
                ?.map { it.toBackdrop() }
                ?: emptyList()

            Result.success(backdrops)
        } catch (e: Exception) {
            // Non-critical path; never surface an error to the UI layer.
            Result.success(emptyList())
        }
    }

    suspend fun fetchConfig(): Result<AppConfig> {
        return try {
            val token = userSettings.authToken ?: return Result.success(AppConfig())
            val url = "$BASE_URL/$CONFIG_COLLECTION/global"

            val (response, status) = get<FirestoreDocument<AppConfigFields>>(url, token)

            if (!status.isSuccess()) {
                return Result.success(AppConfig())
            }

            Result.success(response.toAppConfig())
        } catch (e: Exception) {
            Result.success(AppConfig())
        }
    }
}
