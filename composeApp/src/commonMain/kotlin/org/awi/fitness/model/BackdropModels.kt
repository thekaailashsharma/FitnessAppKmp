package org.awi.fitness.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ──────────────────────────────────────────────────────────────────
// Firestore field wrappers (reuse leaf value types from FirestoreModels.kt)
// ──────────────────────────────────────────────────────────────────

@Serializable
data class BackdropFields(
    @SerialName("id")
    val id: StringValue? = null,
    @SerialName("imageUrl")
    val imageUrl: StringValue? = null,
    @SerialName("theme")
    val theme: StringValue? = null,
    @SerialName("timeOfDay")
    val timeOfDay: ArrayValueWrapper? = null,
    @SerialName("regions")
    val regions: ArrayValueWrapper? = null,
    @SerialName("langs")
    val langs: ArrayValueWrapper? = null,
    @SerialName("surfaces")
    val surfaces: ArrayValueWrapper? = null,
    @SerialName("weight")
    val weight: IntegerValue? = null,
    @SerialName("active")
    val active: BooleanValue? = null,
    @SerialName("updatedAt")
    val updatedAt: IntegerValue? = null
)

@Serializable
data class AppConfigFields(
    @SerialName("backdropsEnabled")
    val backdropsEnabled: BooleanValue? = null,
    @SerialName("rotationPolicy")
    val rotationPolicy: StringValue? = null,
    @SerialName("cacheTtlHours")
    val cacheTtlHours: IntegerValue? = null,
    @SerialName("minAppVersion")
    val minAppVersion: StringValue? = null
)

// ──────────────────────────────────────────────────────────────────
// Flat domain models (also serializable so they can be cached locally)
// ──────────────────────────────────────────────────────────────────

@Serializable
data class Backdrop(
    val id: String,
    val imageUrl: String,
    val theme: String,
    val timeOfDay: List<String>,
    val regions: List<String>,
    val langs: List<String>,
    val surfaces: List<String>,
    val weight: Int,
    val active: Boolean
)

@Serializable
data class AppConfig(
    val backdropsEnabled: Boolean = true,
    val rotationPolicy: String = "daily",
    val cacheTtlHours: Int = 12
)

// ──────────────────────────────────────────────────────────────────
// Mappers
// ──────────────────────────────────────────────────────────────────

private fun ArrayValueWrapper?.toStringList(): List<String> =
    this?.arrayValue?.values?.mapNotNull { it.value } ?: emptyList()

fun FirestoreDocument<BackdropFields>.toBackdrop(): Backdrop {
    val documentId = name?.split("/")?.last() ?: ""
    return Backdrop(
        id = fields?.id?.value?.takeIf { it.isNotBlank() } ?: documentId,
        imageUrl = fields?.imageUrl?.value ?: "",
        theme = fields?.theme?.value?.takeIf { it.isNotBlank() } ?: "both",
        timeOfDay = fields?.timeOfDay.toStringList(),
        regions = fields?.regions.toStringList(),
        langs = fields?.langs.toStringList(),
        surfaces = fields?.surfaces.toStringList(),
        weight = fields?.weight?.value?.toIntOrNull() ?: 1,
        active = fields?.active?.value ?: false
    )
}

fun FirestoreDocument<AppConfigFields>.toAppConfig(): AppConfig {
    return AppConfig(
        backdropsEnabled = fields?.backdropsEnabled?.value ?: true,
        rotationPolicy = fields?.rotationPolicy?.value?.takeIf { it.isNotBlank() } ?: "daily",
        cacheTtlHours = fields?.cacheTtlHours?.value?.toIntOrNull() ?: 12
    )
}
