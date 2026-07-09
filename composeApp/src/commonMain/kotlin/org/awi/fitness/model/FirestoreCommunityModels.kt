package org.awi.fitness.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Firestore serializable models for community features.
// All collections use the fitness_testing_ prefix to avoid conflicts.

@Serializable
data class CommunityUserFields(
    @SerialName("email")
    val email: StringValue? = null,
    @SerialName("displayName")
    val displayName: StringValue? = null,
    @SerialName("profileImage")
    val profileImage: StringValue? = null,
    @SerialName("username")
    val username: StringValue? = null,
    @SerialName("bio")
    val bio: StringValue? = null,
    @SerialName("streakDays")
    val streakDays: IntegerValue? = null,
    @SerialName("level")
    val level: IntegerValue? = null,
    @SerialName("createdAt")
    val createdAt: IntegerValue? = null,
    // Onboarding profile fields (merged onto the same doc via PATCH). All optional so
    //    partial saves never wipe existing community fields.
    @SerialName("goal")
    val goal: StringValue? = null,
    @SerialName("heightCm")
    val heightCm: IntegerValue? = null,
    @SerialName("weightKg")
    val weightKg: StringValue? = null,
    @SerialName("age")
    val age: IntegerValue? = null,
    @SerialName("gender")
    val gender: StringValue? = null,
    // Social links (persisted onto the same doc via merge PATCH). Optional.
    @SerialName("website")
    val website: StringValue? = null,
    @SerialName("instagram")
    val instagram: StringValue? = null,
    @SerialName("twitter")
    val twitter: StringValue? = null
)

@Serializable
data class CommunityUserFirestoreRequest(
    val fields: CommunityUserFields
)

fun FirestoreDocument<CommunityUserFields>.toCommunityUser(): CommunityUser {
    val email = fields?.email?.value?.takeIf { it.isNotEmpty() }
        ?: name?.split("/")?.last() ?: ""
    val displayName = fields?.displayName?.value?.takeIf { it.isNotEmpty() } ?: email
    return CommunityUser(
        id = email,
        name = displayName,
        username = fields?.username?.value?.takeIf { it.isNotEmpty() }
            ?: "@${email.substringBefore("@")}",
        profileImage = fields?.profileImage?.value?.takeIf { it.isNotEmpty() },
        bio = fields?.bio?.value?.takeIf { it.isNotEmpty() },
        streakDays = fields?.streakDays?.value?.toIntOrNull() ?: 0,
        level = fields?.level?.value?.toIntOrNull() ?: 1,
        website = fields?.website?.value?.takeIf { it.isNotEmpty() },
        instagram = fields?.instagram?.value?.takeIf { it.isNotEmpty() },
        twitter = fields?.twitter?.value?.takeIf { it.isNotEmpty() }
    )
}

@Serializable
data class FollowFields(
    @SerialName("followerId")
    val followerId: StringValue? = null,
    @SerialName("followingId")
    val followingId: StringValue? = null,
    @SerialName("timestamp")
    val timestamp: IntegerValue? = null
)

@Serializable
data class FollowFirestoreRequest(
    val fields: FollowFields
)

@Serializable
data class LikeFields(
    @SerialName("userId")
    val userId: StringValue? = null,
    @SerialName("postId")
    val postId: StringValue? = null,
    @SerialName("timestamp")
    val timestamp: IntegerValue? = null
)

@Serializable
data class LikeFirestoreRequest(
    val fields: LikeFields
)

// Minimal response type for Firestore DELETE operations which return {}.
@Serializable
data class FirestoreDeleteResponse(
    val name: String? = null,
    val error: FirestoreError? = null
)

// Post report (App Store UGC compliance, Guideline 1.2) — written to fitness_testing_reports.
@Serializable
data class ReportFirestoreRequest(
    val fields: ReportFields
)

@Serializable
data class ReportFields(
    @SerialName("postId") val postId: StringValue? = null,
    @SerialName("postAuthorId") val postAuthorId: StringValue? = null,
    @SerialName("reporterId") val reporterId: StringValue? = null,
    @SerialName("reason") val reason: StringValue? = null,
    @SerialName("createdAt") val createdAt: IntegerValue? = null,
    @SerialName("status") val status: StringValue? = null
)
