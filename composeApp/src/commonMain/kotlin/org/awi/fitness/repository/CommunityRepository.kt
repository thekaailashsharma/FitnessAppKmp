package org.awi.fitness.repository

import org.awi.fitness.utils.currentTimeMillis
import org.awi.fitness.utils.toFirestoreDocId
import io.ktor.http.isSuccess
import kotlinx.datetime.Clock
import org.awi.fitness.model.*
import org.awi.fitness.network.ApiService

class CommunityRepository : ApiService() {

    companion object {
        private const val PROJECT_ID = "fitness-admin-73a72"
        private const val BASE_URL =
            "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents"
        private const val POSTS_COLLECTION = "fitness_testing_posts"
        private const val USERS_COLLECTION = "fitness_testing_users"
        private const val FOLLOWS_COLLECTION = "fitness_testing_follows"
        private const val ACTIVITY_COLLECTION = "fitness_testing_activity"
    }

    // ──────────────────────────────────────────────────────────────────
    // Feed
    // ──────────────────────────────────────────────────────────────────

    suspend fun getCommunityFeed(filter: String = "public"): Result<List<CommunityPost>> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val url = "$BASE_URL/$POSTS_COLLECTION?pageSize=20"

            val (response, status) = get<FirestoreListResponse<CommunityPostFirestoreFields>>(url, token)

            if (!status.isSuccess()) {
                return Result.failure(Exception("Failed to load feed (HTTP ${status.value})"))
            }

            val allPosts = response.documents
                ?.map { it.toCommunityPost() }
                ?.sortedByDescending { it.timestamp }
                ?: emptyList()

            val posts = when (filter) {
                "my_posts" -> {
                    val me = userSettings.userEmail ?: ""
                    allPosts.filter { it.userId == me }
                }
                "friends" -> {
                    val followingIds = getFollowedUserIds(token)
                    allPosts.filter { it.userId in followingIds }
                }
                else -> allPosts
            }

            Result.success(posts)
        } catch (e: Exception) {
            println("CommunityRepository.getCommunityFeed error: ${e.message}")
            Result.failure(e)
        }
    }

    // Fetch a single post by ID (used when navigating from notifications).
    suspend fun getPost(postId: String): Result<CommunityPost> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val url = "$BASE_URL/$POSTS_COLLECTION/$postId"

            val (response, status) = get<FirestoreDocument<CommunityPostFirestoreFields>>(url, token)

            if (status.isSuccess()) {
                Result.success(response.toCommunityPost())
            } else {
                Result.failure(Exception("Post not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Comments
    // ──────────────────────────────────────────────────────────────────

    suspend fun getPostComments(postId: String): Result<List<CommunityComment>> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val url = "$BASE_URL/$POSTS_COLLECTION/$postId/comments"

            val (response, status) = get<FirestoreListResponse<CommunityCommentFirestoreFields>>(url, token)

            if (status.isSuccess()) {
                val comments = response.documents
                    ?.map { it.toCommunityComment() }
                    ?.sortedBy { it.timestamp }
                    ?: emptyList()
                Result.success(comments)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addComment(postId: String, content: String): Result<CommunityComment> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val currentEmail = userSettings.userEmail ?: return Result.failure(Exception("Not authenticated"))
            val currentName = userSettings.userName ?: currentEmail.substringBefore("@")

            val url = "$BASE_URL/$POSTS_COLLECTION/$postId/comments"

            val request = CommunityCommentFirestoreRequest(
                fields = CommunityCommentFirestoreFields(
                    postId = StringValue(postId),
                    userId = StringValue(currentEmail),
                    userName = StringValue(currentName),
                    content = StringValue(content),
                    timestamp = IntegerValue(currentTimeMillis().toString())
                )
            )

            val (response, status) = post<FirestoreDocument<CommunityCommentFirestoreFields>>(url, request, token)

            if (status.isSuccess()) {
                // Also notify the post author if it is not the commenter
                notifyPostAuthorAboutComment(postId, content, currentEmail, currentName, token)
                Result.success(response.toCommunityComment())
            } else {
                Result.failure(Exception("Failed to add comment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Likes
    // ──────────────────────────────────────────────────────────────────

    // Returns true if the post is now liked, false if unliked.
    suspend fun likePost(postId: String): Result<Boolean> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val currentEmail = userSettings.userEmail ?: return Result.failure(Exception("Not authenticated"))

            // Use a URL-safe document ID derived from the user's email.
            val docId = currentEmail.replace("@", "--at--").replace(".", "-")
            val likeUrl = "$BASE_URL/$POSTS_COLLECTION/$postId/likes/$docId"

            // Check whether the like already exists.
            val (_, checkStatus) = get<FirestoreDocument<LikeFields>>(likeUrl, token)

            return if (checkStatus.isSuccess()) {
                // Already liked → delete the like document.
                delete<FirestoreDeleteResponse>(likeUrl, token)
                Result.success(false)
            } else {
                // Not yet liked → create the like document.
                val request = LikeFirestoreRequest(
                    fields = LikeFields(
                        userId = StringValue(currentEmail),
                        postId = StringValue(postId),
                        timestamp = IntegerValue(currentTimeMillis().toString())
                    )
                )
                patch<FirestoreDocument<LikeFields>>(likeUrl, request, token)

                // Queue an activity notification for the post author.
                notifyPostAuthorAboutLike(postId, currentEmail, token)

                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLikedPostIds(postIds: List<String>): Set<String> {
        val token = userSettings.authToken ?: return emptySet()
        val email = userSettings.userEmail ?: return emptySet()
        val safeEmail = toFirestoreDocId(email)
        val liked = mutableSetOf<String>()
        for (postId in postIds) {
            val likeUrl = "$BASE_URL/$POSTS_COLLECTION/$postId/likes/$safeEmail"
            val (_, status) = get<FirestoreDocument<LikeFields>>(likeUrl, token)
            if (status.isSuccess()) liked.add(postId)
        }
        return liked
    }

    suspend fun getUserPosts(userId: String): Result<List<CommunityPost>> {
        return getCommunityFeed("public").map { posts ->
            posts.filter { it.userId == userId }
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Create Post
    // ──────────────────────────────────────────────────────────────────

    suspend fun createPost(
        content: String,
        imageUrl: String? = null,
        workoutCategory: WorkoutCategory? = null,
        calories: Int? = null,
        steps: Int? = null,
        duration: Int? = null
    ): Result<CommunityPost> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val currentEmail = userSettings.userEmail ?: return Result.failure(Exception("Not authenticated"))
            val currentName = userSettings.userName?.takeIf { it.isNotBlank() }
                ?: currentEmail.substringBefore("@").replaceFirstChar { it.uppercase() }

            val profileImage = getCurrentUserProfileImage(token, currentEmail)

            val url = "$BASE_URL/$POSTS_COLLECTION"
            val timestamp = currentTimeMillis()

            val request = CommunityPostFirestoreRequest(
                fields = CommunityPostFirestoreFields(
                    userId = StringValue(currentEmail),
                    userName = StringValue(currentName),
                    userProfileImage = profileImage?.let { StringValue(it) },
                    content = StringValue(content),
                    // Image upload not yet supported; store empty string or URL as-is.
                    imageUrl = imageUrl?.takeIf { it.isNotEmpty() }?.let { StringValue(it) },
                    workoutCategory = workoutCategory?.let { StringValue(it.name) },
                    timestamp = IntegerValue(timestamp.toString()),
                    likes = IntegerValue("0"),
                    comments = IntegerValue("0"),
                    isPersonalBest = BooleanValue(false),
                    calories = calories?.let { IntegerValue(it.toString()) },
                    steps = steps?.let { IntegerValue(it.toString()) },
                    duration = duration?.let { IntegerValue(it.toString()) }
                )
            )

            val (response, status) = post<FirestoreDocument<CommunityPostFirestoreFields>>(url, request, token)

            if (status.isSuccess()) {
                Result.success(response.toCommunityPost())
            } else {
                Result.failure(Exception("Failed to create post (HTTP ${status.value})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Firebase Storage image upload
    // ──────────────────────────────────────────────────────────────────

    /**
     * Uploads [bytes] to Firebase Storage and returns the public download URL, or null on failure.
     */
    suspend fun uploadImageToStorage(bytes: ByteArray, fileName: String): String? {
        return try {
            val token = userSettings.authToken ?: return null
            val bucket = "$PROJECT_ID.firebasestorage.app"
            val objectPath = "community/$fileName"
            val encodedObjectPath = objectPath.replace("/", "%2F")
            val uploadUrl = "https://firebasestorage.googleapis.com/v0/b/$bucket/o" +
                    "?uploadType=media&name=$encodedObjectPath"

            val (responseText, status) = postBytesRaw(uploadUrl, bytes, "image/jpeg", token)
            println("Firebase Storage upload status: $status")
            println("Firebase Storage upload response: $responseText")

            if (!status.isSuccess()) {
                println("Upload failed with status $status")
                return null
            }

            // Firebase Storage returns downloadTokens in the JSON response
            val tokenRegex = Regex(""""downloadTokens"\s*:\s*"([^"]+)"""")
            val downloadToken = tokenRegex.find(responseText)?.groupValues?.get(1)

            if (downloadToken != null) {
                "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$encodedObjectPath?alt=media&token=$downloadToken"
            } else {
                // No token — use the object name from the response for the URL
                val nameRegex = Regex(""""name"\s*:\s*"([^"]+)"""")
                val objectName = nameRegex.find(responseText)?.groupValues?.get(1)
                val finalEncodedPath = objectName?.replace("/", "%2F") ?: encodedObjectPath
                println("No downloadToken found, constructing URL from name: $objectName")
                "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$finalEncodedPath?alt=media"
            }
        } catch (e: Exception) {
            println("CommunityRepository.uploadImageToStorage error: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Users / Follow
    // ──────────────────────────────────────────────────────────────────

    suspend fun getSuggestedUsers(): Result<List<CommunityUser>> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val currentEmail = userSettings.userEmail ?: ""

            val url = "$BASE_URL/$USERS_COLLECTION?pageSize=50"
            val (response, status) = get<FirestoreListResponse<CommunityUserFields>>(url, token)

            if (!status.isSuccess()) {
                return Result.failure(Exception("Failed to load users"))
            }

            val followingIds = getFollowedUserIds(token)

            val users = response.documents
                ?.map { it.toCommunityUser() }
                ?.filter { it.id != currentEmail }
                ?.map { user -> user.copy(isFollowing = user.id in followingIds) }
                ?: emptyList()

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun followUser(targetUserId: String): Result<Boolean> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val currentEmail = userSettings.userEmail ?: return Result.failure(Exception("Not authenticated"))
            val currentName = userSettings.userName ?: currentEmail.substringBefore("@")

            val url = "$BASE_URL/$FOLLOWS_COLLECTION"

            val request = FollowFirestoreRequest(
                fields = FollowFields(
                    followerId = StringValue(currentEmail),
                    followingId = StringValue(targetUserId),
                    timestamp = IntegerValue(currentTimeMillis().toString())
                )
            )

            val (_, status) = post<FirestoreDocument<FollowFields>>(url, request, token)

            if (status.isSuccess()) {
                // Notify the followed user.
                createActivityNotification(
                    type = CommunityActivityType.FOLLOW,
                    targetUserId = targetUserId,
                    actorUserId = currentEmail,
                    actorUserName = currentName,
                    targetId = currentEmail,
                    targetContent = "You have a new follower!",
                    token = token
                )
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to follow user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unfollowUser(targetUserId: String): Result<Unit> = runCatching {
        val token = userSettings.authToken ?: error("Not authenticated")
        val currentEmail = userSettings.userEmail ?: error("No user email")
        val safeCurrentEmail = toFirestoreDocId(currentEmail)
        val safeTargetId = toFirestoreDocId(targetUserId)

        // Delete the follow document
        val followUrl = "$BASE_URL/$FOLLOWS_COLLECTION/${safeCurrentEmail}_${safeTargetId}"
        delete<FirestoreDeleteResponse>(followUrl, token)
    }

    suspend fun getUserProfile(userId: String): Result<CommunityUser> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val url = "$BASE_URL/$USERS_COLLECTION/${toFirestoreDocId(userId)}"

            val (response, status) = get<FirestoreDocument<CommunityUserFields>>(url, token)

            val followingIds = getFollowedUserIds(token)

            return if (status.isSuccess() && response.fields != null) {
                val user = response.toCommunityUser().copy(isFollowing = userId in followingIds)
                Result.success(user)
            } else {
                // Return a minimal placeholder profile rather than failing.
                Result.success(
                    CommunityUser(
                        id = userId,
                        name = userId.substringBefore("@"),
                        username = "@${userId.substringBefore("@")}",
                        isFollowing = userId in followingIds
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Activity / Notifications
    // ──────────────────────────────────────────────────────────────────

    suspend fun getActivityNotifications(): Result<List<ActivityNotification>> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val currentEmail = userSettings.userEmail ?: return Result.success(emptyList())

            val url = "$BASE_URL/$ACTIVITY_COLLECTION?pageSize=30"
            val (response, status) = get<FirestoreListResponse<ActivityNotificationFirestoreFields>>(url, token)

            if (!status.isSuccess()) {
                // Collection may not exist yet — return empty list gracefully.
                return Result.success(emptyList())
            }

            val notifications = response.documents
                ?.filter { doc -> doc.fields?.targetUserId?.value == currentEmail }
                ?.map { it.toActivityNotification() }
                ?.sortedByDescending { it.timestamp }
                ?: emptyList()

            Result.success(notifications)
        } catch (e: Exception) {
            // Non-critical path; return empty list on any error.
            Result.success(emptyList())
        }
    }

    suspend fun markNotificationAsRead(notificationId: String): Result<Boolean> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val url = "$BASE_URL/$ACTIVITY_COLLECTION/$notificationId?updateMask.fieldPaths=isRead"

            val request = ActivityNotificationFirestoreRequest(
                fields = ActivityNotificationFirestoreFields(isRead = BooleanValue(true))
            )

            val (_, status) = patch<FirestoreDocument<ActivityNotificationFirestoreFields>>(url, request, token)
            Result.success(status.isSuccess())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // User Profile Upsert
    // ──────────────────────────────────────────────────────────────────

    // Creates or updates the current user's profile document in fitness_testing_users.
    // Should be called once after login to make the user discoverable.
    suspend fun ensureUserProfileExists(): Result<Unit> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val currentEmail = userSettings.userEmail ?: return Result.failure(Exception("Not authenticated"))
            val currentName = userSettings.userName ?: currentEmail.substringBefore("@")

            val url = "$BASE_URL/$USERS_COLLECTION/${toFirestoreDocId(currentEmail)}"

            // PATCH creates the document if it does not exist, or updates it if it does.
            val request = CommunityUserFirestoreRequest(
                fields = CommunityUserFields(
                    email = StringValue(currentEmail),
                    displayName = StringValue(currentName),
                    username = StringValue("@${currentEmail.substringBefore("@")}"),
                    streakDays = IntegerValue("0"),
                    level = IntegerValue("1"),
                    createdAt = IntegerValue(currentTimeMillis().toString())
                )
            )

            patch<FirestoreDocument<CommunityUserFields>>(url, request, token)
            Result.success(Unit)
        } catch (e: Exception) {
            // Non-critical; log and continue.
            println("CommunityRepository.ensureUserProfileExists error: ${e.message}")
            Result.success(Unit)
        }
    }

    suspend fun updateUserProfile(displayName: String, bio: String, profileImageUrl: String?): Result<Unit> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val currentEmail = userSettings.userEmail ?: return Result.failure(Exception("Not authenticated"))
            val url = "$BASE_URL/$USERS_COLLECTION/${toFirestoreDocId(currentEmail)}"

            val fields = CommunityUserFields(
                email = StringValue(currentEmail),
                displayName = StringValue(displayName.ifBlank { currentEmail.substringBefore("@") }),
                username = StringValue("@${currentEmail.substringBefore("@")}"),
                profileImage = profileImageUrl?.let { StringValue(it) }
            )
            val request = CommunityUserFirestoreRequest(fields = fields)
            patch<FirestoreDocument<CommunityUserFields>>(url, request, token)

            // Update local cache
            userSettings.userName = displayName.ifBlank { null }
            userSettings.userBio = bio.ifBlank { null }
            profileImageUrl?.let { userSettings.profilePhotoUrl = it }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getCurrentUserProfileImage(token: String, email: String): String? {
        return try {
            val url = "$BASE_URL/$USERS_COLLECTION/${toFirestoreDocId(email)}"
            val (response, status) = get<FirestoreDocument<CommunityUserFields>>(url, token)
            if (status.isSuccess()) {
                response.fields?.profileImage?.value?.takeIf { it.isNotEmpty() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Private Helpers
    // ──────────────────────────────────────────────────────────────────

    private suspend fun getFollowedUserIds(token: String): Set<String> {
        val currentEmail = userSettings.userEmail ?: return emptySet()
        return try {
            val url = "$BASE_URL/$FOLLOWS_COLLECTION?pageSize=100"
            val (response, status) = get<FirestoreListResponse<FollowFields>>(url, token)
            if (status.isSuccess()) {
                response.documents
                    ?.mapNotNull { it.fields }
                    ?.filter { it.followerId?.value == currentEmail }
                    ?.mapNotNull { it.followingId?.value }
                    ?.toSet() ?: emptySet()
            } else {
                emptySet()
            }
        } catch (e: Exception) {
            emptySet()
        }
    }

    private suspend fun createActivityNotification(
        type: CommunityActivityType,
        targetUserId: String,
        actorUserId: String,
        actorUserName: String,
        targetId: String,
        targetContent: String,
        token: String
    ) {
        // Do not notify users about their own actions.
        if (targetUserId == actorUserId) return

        try {
            val url = "$BASE_URL/$ACTIVITY_COLLECTION"
            val request = ActivityNotificationFirestoreRequest(
                fields = ActivityNotificationFirestoreFields(
                    type = StringValue(type.name),
                    userId = StringValue(actorUserId),
                    userName = StringValue(actorUserName),
                    targetId = StringValue(targetId),
                    targetContent = StringValue(targetContent),
                    targetUserId = StringValue(targetUserId),
                    timestamp = IntegerValue(currentTimeMillis().toString()),
                    isRead = BooleanValue(false)
                )
            )
            post<FirestoreDeleteResponse>(url, request, token)
        } catch (e: Exception) {
            println("CommunityRepository.createActivityNotification error: ${e.message}")
        }
    }

    // Looks up post author then posts a LIKE notification for them.
    private suspend fun notifyPostAuthorAboutLike(postId: String, likerEmail: String, token: String) {
        try {
            val likerName = userSettings.userName ?: likerEmail.substringBefore("@")
            val (postDoc, postStatus) = get<FirestoreDocument<CommunityPostFirestoreFields>>(
                "$BASE_URL/$POSTS_COLLECTION/$postId", token
            )
            if (!postStatus.isSuccess()) return
            val postAuthor = postDoc.fields?.userId?.value ?: return
            val postContent = postDoc.fields?.content?.value ?: ""

            createActivityNotification(
                type = CommunityActivityType.LIKE,
                targetUserId = postAuthor,
                actorUserId = likerEmail,
                actorUserName = likerName,
                targetId = postId,
                targetContent = postContent,
                token = token
            )
        } catch (e: Exception) {
            println("CommunityRepository.notifyPostAuthorAboutLike error: ${e.message}")
        }
    }

    // Looks up post author then posts a COMMENT notification for them.
    private suspend fun notifyPostAuthorAboutComment(
        postId: String,
        commentContent: String,
        commenterEmail: String,
        commenterName: String,
        token: String
    ) {
        try {
            val (postDoc, postStatus) = get<FirestoreDocument<CommunityPostFirestoreFields>>(
                "$BASE_URL/$POSTS_COLLECTION/$postId", token
            )
            if (!postStatus.isSuccess()) return
            val postAuthor = postDoc.fields?.userId?.value ?: return

            createActivityNotification(
                type = CommunityActivityType.COMMENT,
                targetUserId = postAuthor,
                actorUserId = commenterEmail,
                actorUserName = commenterName,
                targetId = postId,
                targetContent = commentContent,
                token = token
            )
        } catch (e: Exception) {
            println("CommunityRepository.notifyPostAuthorAboutComment error: ${e.message}")
        }
    }
}
