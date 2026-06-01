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
    val createdAt: IntegerValue? = null
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
        level = fields?.level?.value?.toIntOrNull() ?: 1
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
