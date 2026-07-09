package org.awi.fitness.repository

import org.awi.fitness.utils.communityHandle
import org.awi.fitness.utils.compressImage
import org.awi.fitness.utils.currentTimeMillis
import org.awi.fitness.utils.toFirestoreDocId
import io.ktor.http.isSuccess
import kotlinx.datetime.Clock
import org.awi.fitness.model.*
import org.awi.fitness.network.ApiService

class CommunityRepository : ApiService() {

    companion object {
        private const val PROJECT_ID = "awi-fitness-app"
        private const val BASE_URL =
            "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents"
        private const val POSTS_COLLECTION = "fitness_testing_posts"
        private const val USERS_COLLECTION = "fitness_testing_users"
        private const val FOLLOWS_COLLECTION = "fitness_testing_follows"
        private const val ACTIVITY_COLLECTION = "fitness_testing_activity"
        private const val REPORTS_COLLECTION = "fitness_testing_reports"
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

            // Hide posts from users this person has blocked (UGC compliance, Guideline 1.2).
            val blocked = userSettings.blockedUserIds
            val allPosts = response.documents
                ?.map { it.toCommunityPost() }
                ?.filter { it.userId !in blocked }
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

            // Override each post's embedded (snapshot-at-creation) avatar with the author's
            // CURRENT profile image so a photo change propagates to every post on next load.
            Result.success(applyLiveAvatars(posts, token))
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
                // Resolve the author's live avatar rather than the snapshot stored on the post.
                val post = response.toCommunityPost()
                Result.success(applyLiveAvatars(listOf(post), token).first())
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
            // Filter objectionable content before it reaches Firestore (App Store 1.2).
            (org.awi.fitness.utils.ContentModeration.check(content) as? org.awi.fitness.utils.ContentModeration.Result.Blocked)
                ?.let { return Result.failure(Exception(it.message)) }
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
                // Bump the post's comment counter, then notify the author (if not the commenter).
                adjustPostCounter(postId, "comments", 1, token)
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

            // Use a URL-safe document ID derived from the user's email — MUST match the
            // encoding used by getLikedPostIds so like state round-trips correctly.
            val docId = toFirestoreDocId(currentEmail)
            val likeUrl = "$BASE_URL/$POSTS_COLLECTION/$postId/likes/$docId"

            // Check whether the like already exists.
            val (_, checkStatus) = get<FirestoreDocument<LikeFields>>(likeUrl, token)

            return if (checkStatus.isSuccess()) {
                // Already liked → delete the like document and decrement the post's counter.
                delete<FirestoreDeleteResponse>(likeUrl, token)
                adjustPostCounter(postId, "likes", -1, token)
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

                // Bump the post's like counter, then queue an activity notification.
                adjustPostCounter(postId, "likes", 1, token)
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
            // Filter objectionable content before it reaches Firestore (App Store 1.2).
            (org.awi.fitness.utils.ContentModeration.check(content) as? org.awi.fitness.utils.ContentModeration.Result.Blocked)
                ?.let { return Result.failure(Exception(it.message)) }
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
     * The image is downscaled to [maxDim] px on its longest edge and re-encoded as JPEG before
     * upload (~512 for avatars, ~1080 for post images) so uploads stay fast. Compression is
     * best-effort — it falls back to the original bytes if it fails.
     */
    suspend fun uploadImageToStorage(bytes: ByteArray, fileName: String, maxDim: Int = 1080): String? {
        return try {
            val token = userSettings.authToken ?: return null
            val bucket = "$PROJECT_ID.firebasestorage.app"
            val objectPath = "community/$fileName"
            val encodedObjectPath = objectPath.replace("/", "%2F")
            val uploadUrl = "https://firebasestorage.googleapis.com/v0/b/$bucket/o" +
                    "?uploadType=media&name=$encodedObjectPath"

            val compressed = compressImage(bytes, maxDim, 80)
            val (responseText, status) = postBytesRaw(uploadUrl, compressed, "image/jpeg", token)
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

            // Write the follow doc at a DETERMINISTIC id so unfollow can delete the exact
            // same document. PATCH upserts (create-or-update) at that id.
            val docId = "${toFirestoreDocId(currentEmail)}_${toFirestoreDocId(targetUserId)}"
            val url = "$BASE_URL/$FOLLOWS_COLLECTION/$docId"

            val request = FollowFirestoreRequest(
                fields = FollowFields(
                    followerId = StringValue(currentEmail),
                    followingId = StringValue(targetUserId),
                    timestamp = IntegerValue(currentTimeMillis().toString())
                )
            )

            val (_, status) = patch<FirestoreDocument<FollowFields>>(url, request, token)

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
            val (followersCount, followingCount) = getFollowCounts(userId, token)

            return if (status.isSuccess() && response.fields != null) {
                val user = response.toCommunityUser().copy(
                    isFollowing = userId in followingIds,
                    followersCount = followersCount,
                    followingCount = followingCount
                )
                Result.success(user)
            } else {
                // Return a minimal placeholder profile rather than failing.
                Result.success(
                    CommunityUser(
                        id = userId,
                        name = userId.substringBefore("@"),
                        username = "@${userId.substringBefore("@")}",
                        isFollowing = userId in followingIds,
                        followersCount = followersCount,
                        followingCount = followingCount
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

            // updateMask so this ONLY writes the discoverable-profile fields and never
            // clobbers anything else on the doc (fcmToken, streakDays, level, goal, weight…).
            // A full-doc PATCH here previously wiped the push token and RESET streak/level
            // to 0/1 on every login. Creates the doc if missing, updates in place if present.
            val url = "$BASE_URL/$USERS_COLLECTION/${toFirestoreDocId(currentEmail)}" +
                "?updateMask.fieldPaths=email" +
                "&updateMask.fieldPaths=displayName" +
                "&updateMask.fieldPaths=username"
            val request = CommunityUserFirestoreRequest(
                fields = CommunityUserFields(
                    email = StringValue(currentEmail),
                    displayName = StringValue(currentName),
                    username = StringValue(communityHandle(currentName, currentEmail)),
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

    /**
     * Report a post as objectionable (App Store UGC compliance, Guideline 1.2). Writes a
     * report doc to `fitness_testing_reports` for moderation review. Best-effort.
     */
    suspend fun reportPost(postId: String, postAuthorId: String, reason: String): Result<Unit> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val reporter = userSettings.userEmail ?: return Result.failure(Exception("Not authenticated"))
            val reportId = "${postId}_${toFirestoreDocId(reporter)}"
            val url = "$BASE_URL/$REPORTS_COLLECTION/$reportId"
            val body = ReportFirestoreRequest(
                fields = ReportFields(
                    postId = StringValue(postId),
                    postAuthorId = StringValue(postAuthorId),
                    reporterId = StringValue(reporter),
                    reason = StringValue(reason),
                    createdAt = IntegerValue(currentTimeMillis().toString()),
                    status = StringValue("pending")
                )
            )
            val (_, status) = patch<FirestoreResponse<ReportFields>>(url, body, token)
            if (status.isSuccess()) Result.success(Unit)
            else Result.failure(Exception("Failed to submit report"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Report a comment as objectionable (App Store UGC compliance, Guideline 1.2). Writes to
     * `fitness_testing_reports` with a comment-scoped id so it never collides with a post report.
     */
    suspend fun reportComment(commentId: String, commentAuthorId: String, parentPostId: String, reason: String): Result<Unit> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val reporter = userSettings.userEmail ?: return Result.failure(Exception("Not authenticated"))
            val reportId = "comment_${commentId}_${toFirestoreDocId(reporter)}"
            val url = "$BASE_URL/$REPORTS_COLLECTION/$reportId"
            val body = ReportFirestoreRequest(
                fields = ReportFields(
                    postId = StringValue(parentPostId),
                    postAuthorId = StringValue(commentAuthorId),
                    reporterId = StringValue(reporter),
                    reason = StringValue("Comment: $reason"),
                    createdAt = IntegerValue(currentTimeMillis().toString()),
                    status = StringValue("pending")
                )
            )
            val (_, status) = patch<FirestoreResponse<ReportFields>>(url, body, token)
            if (status.isSuccess()) Result.success(Unit) else Result.failure(Exception("Failed to submit report"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Persists the onboarding profile (name, goal, physical stats) onto the user's
    //    fitness_testing_users doc. PATCH here replaces the whole document, so we first
    //    read the existing doc and merge, keeping community fields (streak/level/bio/image).
    //    Best-effort: never throws, so it can't block onboarding completion.
    suspend fun saveOnboardingProfile(
        displayName: String,
        goal: String,
        heightCm: Int,
        weightKg: Float,
        age: Int,
        gender: String
    ): Result<Unit> {
        return try {
            val token = userSettings.authToken ?: return Result.success(Unit)
            val currentEmail = userSettings.userEmail ?: return Result.success(Unit)
            val resolvedName = displayName.ifBlank { userSettings.userName ?: currentEmail.substringBefore("@") }
            val url = "$BASE_URL/$USERS_COLLECTION/${toFirestoreDocId(currentEmail)}"

            val existing = try {
                val (resp, status) = get<FirestoreDocument<CommunityUserFields>>(url, token)
                if (status.isSuccess()) resp.fields else null
            } catch (e: Exception) {
                null
            }

            val merged = CommunityUserFields(
                email = StringValue(currentEmail),
                displayName = StringValue(resolvedName),
                username = existing?.username ?: StringValue(communityHandle(resolvedName, currentEmail)),
                profileImage = existing?.profileImage,
                bio = existing?.bio,
                streakDays = existing?.streakDays ?: IntegerValue("0"),
                level = existing?.level ?: IntegerValue("1"),
                createdAt = existing?.createdAt ?: IntegerValue(currentTimeMillis().toString()),
                goal = StringValue(goal),
                heightCm = IntegerValue(heightCm.toString()),
                weightKg = StringValue(weightKg.toString()),
                age = IntegerValue(age.toString()),
                gender = StringValue(gender)
            )
            patch<FirestoreDocument<CommunityUserFields>>(url, CommunityUserFirestoreRequest(merged), token)
            Result.success(Unit)
        } catch (e: Exception) {
            println("CommunityRepository.saveOnboardingProfile error: ${e.message}")
            Result.success(Unit)
        }
    }

    // Edits the current user's profile. Reads the existing doc and MERGES so that fields not
    //    being edited here (streak/level/goal/height/weight/age/gender/createdAt) are never wiped.
    //    Persists bio and, when provided, the social links. Null social-link args mean "keep the
    //    existing value" (so the quick avatar edit never clears links); blank means "clear it".
    suspend fun updateUserProfile(
        displayName: String,
        bio: String,
        profileImageUrl: String?,
        website: String? = null,
        instagram: String? = null,
        twitter: String? = null
    ): Result<Unit> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val currentEmail = userSettings.userEmail ?: return Result.failure(Exception("Not authenticated"))
            val url = "$BASE_URL/$USERS_COLLECTION/${toFirestoreDocId(currentEmail)}"

            val existing = try {
                val (resp, status) = get<FirestoreDocument<CommunityUserFields>>(url, token)
                if (status.isSuccess()) resp.fields else null
            } catch (e: Exception) {
                null
            }

            val resolvedName = displayName.ifBlank {
                existing?.displayName?.value ?: currentEmail.substringBefore("@")
            }

            // Merge helper: a provided non-null arg wins (blank clears); null keeps the old value.
            fun mergeStr(new: String?, old: StringValue?): StringValue? =
                if (new != null) new.takeIf { it.isNotBlank() }?.let { StringValue(it) } else old

            val merged = CommunityUserFields(
                email = StringValue(currentEmail),
                displayName = StringValue(resolvedName),
                username = existing?.username ?: StringValue(communityHandle(resolvedName, currentEmail)),
                profileImage = profileImageUrl?.let { StringValue(it) } ?: existing?.profileImage,
                bio = bio.takeIf { it.isNotBlank() }?.let { StringValue(it) },
                streakDays = existing?.streakDays ?: IntegerValue("0"),
                level = existing?.level ?: IntegerValue("1"),
                createdAt = existing?.createdAt ?: IntegerValue(currentTimeMillis().toString()),
                goal = existing?.goal,
                heightCm = existing?.heightCm,
                weightKg = existing?.weightKg,
                age = existing?.age,
                gender = existing?.gender,
                website = mergeStr(website, existing?.website),
                instagram = mergeStr(instagram, existing?.instagram),
                twitter = mergeStr(twitter, existing?.twitter)
            )
            patch<FirestoreDocument<CommunityUserFields>>(url, CommunityUserFirestoreRequest(merged), token)

            // Update local cache
            userSettings.userName = displayName.ifBlank { null }
            userSettings.userBio = bio.ifBlank { null }
            profileImageUrl?.let { userSettings.profilePhotoUrl = it }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Real friend search: fetches the users collection (paged) and filters by a case-insensitive
    // substring match on displayName / username. Used as the primary search path; callers may fall
    // back to an in-memory filter of already-loaded suggestions if this fails.
    suspend fun searchUsers(query: String): Result<List<CommunityUser>> {
        return try {
            val token = userSettings.authToken ?: return Result.failure(Exception("Not authenticated"))
            val currentEmail = userSettings.userEmail ?: ""
            val q = query.trim().lowercase()
            if (q.isEmpty()) return Result.success(emptyList())

            val url = "$BASE_URL/$USERS_COLLECTION?pageSize=200"
            val (response, status) = get<FirestoreListResponse<CommunityUserFields>>(url, token)
            if (!status.isSuccess()) return Result.failure(Exception("Search failed"))

            val followingIds = getFollowedUserIds(token)
            val users = response.documents
                ?.map { it.toCommunityUser() }
                ?.filter { it.id != currentEmail }
                ?.filter { user ->
                    user.name.lowercase().contains(q) ||
                        user.username.lowercase().removePrefix("@").contains(q)
                }
                ?.map { user -> user.copy(isFollowing = user.id in followingIds) }
                ?: emptyList()

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Resolves the CURRENT profile image for every author in [posts] and overrides the stale
    // snapshot stored on each post. Efficient: collects DISTINCT userIds, fetches each user doc
    // once (cached in a map for the call), and prefers the freshest LOCAL value for the current
    // user. A missing/blank live value leaves the post's existing avatar untouched.
    private suspend fun applyLiveAvatars(posts: List<CommunityPost>, token: String): List<CommunityPost> {
        if (posts.isEmpty()) return posts

        val currentEmail = userSettings.userEmail
        val localPhoto = userSettings.profilePhotoUrl?.takeIf { it.isNotBlank() }

        val avatarByUserId = HashMap<String, String?>()
        for (userId in posts.map { it.userId }.filter { it.isNotBlank() }.toSet()) {
            avatarByUserId[userId] = when {
                // Always prefer the freshest local value for the signed-in user.
                userId == currentEmail && localPhoto != null -> localPhoto
                else -> fetchUserProfileImage(token, userId)
            }
        }

        return posts.map { post ->
            val live = avatarByUserId[post.userId]?.takeIf { it.isNotBlank() }
            if (live != null) post.copy(userProfileImage = live) else post
        }
    }

    // Reads the current profileImage for [userId] from the users collection (null if none/failed).
    private suspend fun fetchUserProfileImage(token: String, userId: String): String? {
        return try {
            val url = "$BASE_URL/$USERS_COLLECTION/${toFirestoreDocId(userId)}"
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

    // Read-modify-write a numeric counter ("likes" or "comments") on a post document.
    // Low concurrency here makes a simple read → PATCH (single-field updateMask so nothing
    // else is touched) safe. Floors at 0. Best-effort: never throws.
    private suspend fun adjustPostCounter(postId: String, field: String, delta: Int, token: String) {
        try {
            val postUrl = "$BASE_URL/$POSTS_COLLECTION/$postId"
            val (doc, status) = get<FirestoreDocument<CommunityPostFirestoreFields>>(postUrl, token)
            if (!status.isSuccess()) return
            val current = when (field) {
                "likes" -> doc.fields?.likes?.value?.toIntOrNull() ?: 0
                "comments" -> doc.fields?.comments?.value?.toIntOrNull() ?: 0
                else -> return
            }
            val next = (current + delta).coerceAtLeast(0)
            val fields = when (field) {
                "likes" -> CommunityPostFirestoreFields(likes = IntegerValue(next.toString()))
                else -> CommunityPostFirestoreFields(comments = IntegerValue(next.toString()))
            }
            val patchUrl = "$postUrl?updateMask.fieldPaths=$field"
            patch<FirestoreDocument<CommunityPostFirestoreFields>>(
                patchUrl, CommunityPostFirestoreRequest(fields), token
            )
        } catch (e: Exception) {
            println("CommunityRepository.adjustPostCounter error: ${e.message}")
        }
    }

    // Counts followers (docs where followingId == userId) and following (followerId == userId)
    // by fetching the follows collection and counting in memory. Returns followers to following.
    private suspend fun getFollowCounts(userId: String, token: String): Pair<Int, Int> {
        return try {
            val url = "$BASE_URL/$FOLLOWS_COLLECTION?pageSize=200"
            val (response, status) = get<FirestoreListResponse<FollowFields>>(url, token)
            if (!status.isSuccess()) return 0 to 0
            val docs = response.documents?.mapNotNull { it.fields } ?: emptyList()
            val followers = docs.count { it.followingId?.value == userId }
            val following = docs.count { it.followerId?.value == userId }
            followers to following
        } catch (e: Exception) {
            0 to 0
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
