package org.awi.fitness.model

import org.awi.fitness.utils.currentTimeMillis
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityPost(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileImage: String? = null,
    val content: String = "",
    val imageUrl: String? = null,
    val workoutCategory: WorkoutCategory? = null,
    val timestamp: Long = currentTimeMillis(),
    val likes: Int = 0,
    val comments: Int = 0,
    val calories: Int? = null,
    val steps: Int? = null,
    val duration: Int? = null, // in seconds
    val isPersonalBest: Boolean = false,
    val badges: List<String> = emptyList(),
    val streakDays: Int? = null
)

@Serializable
data class CommunityComment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileImage: String? = null,
    val content: String = "",
    val timestamp: Long = currentTimeMillis()
)

@Serializable
data class CommunityUser(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val profileImage: String? = null,
    val bio: String? = null,
    val streakDays: Int = 0,
    val level: Int = 1,
    val badges: List<Badge> = emptyList(),
    val isFollowing: Boolean = false
)


@Serializable
data class ActivityNotification(
    val id: String = "",
    val type: CommunityActivityType = CommunityActivityType.LIKE,
    val userId: String = "",
    val userName: String = "",
    val userProfileImage: String? = null,
    val targetId: String = "", // postId or commentId
    val targetContent: String = "",
    val timestamp: Long = currentTimeMillis(),
    val isRead: Boolean = false
)

@Serializable
enum class CommunityActivityType {
    @SerialName("LIKE")
    LIKE,
    
    @SerialName("COMMENT")
    COMMENT,
    
    @SerialName("FOLLOW")
    FOLLOW,
    
    @SerialName("CHALLENGE_INVITE")
    CHALLENGE_INVITE,
    
    @SerialName("BADGE_EARNED")
    BADGE_EARNED,
    
    @SerialName("STREAK_MILESTONE")
    STREAK_MILESTONE
}

@Serializable
data class CommunityPostFirestoreFields(
    @SerialName("id")
    val id: StringValue? = null,
    @SerialName("userId")
    val userId: StringValue? = null,
    @SerialName("userName")
    val userName: StringValue? = null,
    @SerialName("userProfileImage")
    val userProfileImage: StringValue? = null,
    @SerialName("content")
    val content: StringValue? = null,
    @SerialName("imageUrl")
    val imageUrl: StringValue? = null,
    @SerialName("workoutCategory")
    val workoutCategory: StringValue? = null,
    @SerialName("timestamp")
    val timestamp: IntegerValue? = null,
    @SerialName("likes")
    val likes: IntegerValue? = null,
    @SerialName("comments")
    val comments: IntegerValue? = null,
    @SerialName("calories")
    val calories: IntegerValue? = null,
    @SerialName("steps")
    val steps: IntegerValue? = null,
    @SerialName("duration")
    val duration: IntegerValue? = null,
    @SerialName("isPersonalBest")
    val isPersonalBest: BooleanValue? = null,
    @SerialName("streakDays")
    val streakDays: IntegerValue? = null
)

@Serializable
data class CommunityPostFirestoreRequest(
    val fields: CommunityPostFirestoreFields
)

@Serializable
data class CommunityCommentFirestoreFields(
    @SerialName("id")
    val id: StringValue? = null,
    @SerialName("postId")
    val postId: StringValue? = null,
    @SerialName("userId")
    val userId: StringValue? = null,
    @SerialName("userName")
    val userName: StringValue? = null,
    @SerialName("userProfileImage")
    val userProfileImage: StringValue? = null,
    @SerialName("content")
    val content: StringValue? = null,
    @SerialName("timestamp")
    val timestamp: IntegerValue? = null
)

@Serializable
data class CommunityCommentFirestoreRequest(
    val fields: CommunityCommentFirestoreFields
)

@Serializable
data class ActivityNotificationFirestoreFields(
    @SerialName("id")
    val id: StringValue? = null,
    @SerialName("type")
    val type: StringValue? = null,
    @SerialName("userId")
    val userId: StringValue? = null,
    @SerialName("userName")
    val userName: StringValue? = null,
    @SerialName("userProfileImage")
    val userProfileImage: StringValue? = null,
    @SerialName("targetId")
    val targetId: StringValue? = null,
    @SerialName("targetContent")
    val targetContent: StringValue? = null,
    @SerialName("timestamp")
    val timestamp: IntegerValue? = null,
    @SerialName("isRead")
    val isRead: BooleanValue? = null,
    @SerialName("targetUserId")
    val targetUserId: StringValue? = null
)

@Serializable
data class ActivityNotificationFirestoreRequest(
    val fields: ActivityNotificationFirestoreFields
)

// Extension functions for converting models to Firestore requests
fun CommunityPost.toFirestoreRequest(): CommunityPostFirestoreRequest {
    return CommunityPostFirestoreRequest(
        fields = CommunityPostFirestoreFields(
            id = StringValue(id),
            userId = StringValue(userId),
            userName = StringValue(userName),
            userProfileImage = userProfileImage?.let { StringValue(it) },
            content = StringValue(content),
            imageUrl = imageUrl?.let { StringValue(it) },
            workoutCategory = workoutCategory?.let { StringValue(it.name) },
            timestamp = IntegerValue(timestamp.toString()),
            likes = IntegerValue(likes.toString()),
            comments = IntegerValue(comments.toString()),
            calories = calories?.let { IntegerValue(it.toString()) },
            steps = steps?.let { IntegerValue(it.toString()) },
            duration = duration?.let { IntegerValue(it.toString()) },
            isPersonalBest = BooleanValue(isPersonalBest),
            streakDays = streakDays?.let { IntegerValue(it.toString()) }
        )
    )
}

fun CommunityComment.toFirestoreRequest(): CommunityCommentFirestoreRequest {
    return CommunityCommentFirestoreRequest(
        fields = CommunityCommentFirestoreFields(
            id = StringValue(id),
            postId = StringValue(postId),
            userId = StringValue(userId),
            userName = StringValue(userName),
            userProfileImage = userProfileImage?.let { StringValue(it) },
            content = StringValue(content),
            timestamp = IntegerValue(timestamp.toString())
        )
    )
}

fun ActivityNotification.toFirestoreRequest(): ActivityNotificationFirestoreRequest {
    return ActivityNotificationFirestoreRequest(
        fields = ActivityNotificationFirestoreFields(
            id = StringValue(id),
            type = StringValue(type.name),
            userId = StringValue(userId),
            userName = StringValue(userName),
            userProfileImage = userProfileImage?.let { StringValue(it) },
            targetId = StringValue(targetId),
            targetContent = StringValue(targetContent),
            timestamp = IntegerValue(timestamp.toString()),
            isRead = BooleanValue(isRead)
        )
    )
}

// Extension functions for converting Firestore responses to models
fun FirestoreDocument<CommunityPostFirestoreFields>.toCommunityPost(): CommunityPost {
    val documentId = name?.split("/")?.last() ?: ""
    return CommunityPost(
        id = documentId,
        userId = fields?.userId?.value ?: "",
        userName = fields?.userName?.value ?: "",
        userProfileImage = fields?.userProfileImage?.value,
        content = fields?.content?.value ?: "",
        imageUrl = fields?.imageUrl?.value?.takeIf { it.isNotEmpty() },
        workoutCategory = fields?.workoutCategory?.value?.let {
            runCatching { WorkoutCategory.valueOf(it) }.getOrNull()
        },
        timestamp = fields?.timestamp?.value?.toLongOrNull() ?: currentTimeMillis(),
        likes = fields?.likes?.value?.toIntOrNull() ?: 0,
        comments = fields?.comments?.value?.toIntOrNull() ?: 0,
        calories = fields?.calories?.value?.toIntOrNull(),
        steps = fields?.steps?.value?.toIntOrNull(),
        duration = fields?.duration?.value?.toIntOrNull(),
        isPersonalBest = fields?.isPersonalBest?.value ?: false,
        badges = emptyList(),
        streakDays = fields?.streakDays?.value?.toIntOrNull()
    )
}

fun FirestoreDocument<CommunityCommentFirestoreFields>.toCommunityComment(): CommunityComment {
    val documentId = name?.split("/")?.last() ?: ""
    return CommunityComment(
        id = documentId,
        postId = fields?.postId?.value ?: "",
        userId = fields?.userId?.value ?: "",
        userName = fields?.userName?.value ?: "",
        userProfileImage = fields?.userProfileImage?.value?.takeIf { it.isNotEmpty() },
        content = fields?.content?.value ?: "",
        timestamp = fields?.timestamp?.value?.toLongOrNull() ?: currentTimeMillis()
    )
}

fun FirestoreDocument<ActivityNotificationFirestoreFields>.toActivityNotification(): ActivityNotification {
    val documentId = name?.split("/")?.last() ?: ""
    return ActivityNotification(
        id = documentId,
        type = fields?.type?.value?.let {
            runCatching { CommunityActivityType.valueOf(it) }.getOrNull()
        } ?: CommunityActivityType.LIKE,
        userId = fields?.userId?.value ?: "",
        userName = fields?.userName?.value ?: "",
        userProfileImage = fields?.userProfileImage?.value?.takeIf { it.isNotEmpty() },
        targetId = fields?.targetId?.value ?: "",
        targetContent = fields?.targetContent?.value ?: "",
        timestamp = fields?.timestamp?.value?.toLongOrNull() ?: currentTimeMillis(),
        isRead = fields?.isRead?.value ?: false
    )
}
