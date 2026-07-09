package org.awi.fitness.viewmodel
import org.awi.fitness.data.tr
import org.awi.fitness.data.StringKey

import org.awi.fitness.utils.currentTimeMillis
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.awi.fitness.model.ActivityNotification
import org.awi.fitness.model.CommunityComment
import org.awi.fitness.model.CommunityPost
import org.awi.fitness.model.CommunityUser
import org.awi.fitness.model.WorkoutCategory
import org.awi.fitness.repository.CommunityRepository
import org.awi.fitness.repository.TajlyRepository
import org.awi.fitness.utils.CommunityEvents

data class CommunityFeedState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val posts: List<CommunityPost> = emptyList(),
    val activeFilter: String = "public", // public, friends, my_posts
    val likedPostIds: Set<String> = emptySet(),
    val emptyMessage: String? = null,
    val actionMessage: String? = null // transient toast for report/block confirmations
)

data class PostDetailState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val post: CommunityPost? = null,
    val comments: List<CommunityComment> = emptyList(),
    val newCommentText: String = "",
    val likedByMe: Boolean = false,
    val actionMessage: String? = null // transient toast for comment report/block confirmations
)

data class CreatePostState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val content: String = "",
    val imageUrl: String? = null,
    val workoutCategory: WorkoutCategory? = null,
    val calories: Int? = null,
    val steps: Int? = null,
    val duration: Int? = null,
    val isSuccess: Boolean = false
)

data class FindFriendsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val suggestedUsers: List<CommunityUser> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<CommunityUser> = emptyList(),
    val activeTab: String = "all" // all, friends, suggested
)

data class ActivityState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val notifications: List<ActivityNotification> = emptyList(),
    val unreadCount: Int = 0
)

data class CommunityProfileState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: CommunityUser? = null,
    val userPosts: List<CommunityPost> = emptyList(),
    val isSavingProfile: Boolean = false,
    val saveProfileSuccess: Boolean = false
)

class CommunityViewModel : StateScreenModel<CommunityFeedState>(CommunityFeedState()) {
    val communityRepository = CommunityRepository()
    private val tajlyRepository = TajlyRepository()

    init {
        // Ensure the current user's profile exists in Firestore so they appear in friend suggestions.
        screenModelScope.launch {
            communityRepository.ensureUserProfileExists()
        }
    }
    
    // Post detail state
    private val _postDetailState = MutableStateFlow(PostDetailState())
    val postDetailState: StateFlow<PostDetailState> = _postDetailState.asStateFlow()
    
    // Create post state
    private val _createPostState = MutableStateFlow(CreatePostState())
    val createPostState: StateFlow<CreatePostState> = _createPostState.asStateFlow()
    
    // Find friends state
    private val _findFriendsState = MutableStateFlow(FindFriendsState())
    val findFriendsState: StateFlow<FindFriendsState> = _findFriendsState.asStateFlow()
    
    // Activity state
    private val _activityState = MutableStateFlow(ActivityState())
    val activityState: StateFlow<ActivityState> = _activityState.asStateFlow()
    
    // Community profile state
    private val _profileState = MutableStateFlow(CommunityProfileState())
    val profileState: StateFlow<CommunityProfileState> = _profileState.asStateFlow()
    
    // Feed actions
    suspend fun loadCommunityFeed(filter: String = "public") {
        // Fire-and-forget: ensure the TAJLY Day-0 seed exists so the feed is never empty on a
        // fresh install. Idempotent and best-effort — wrapped so a failure never blocks the feed.
        screenModelScope.launch {
            try {
                tajlyRepository.ensureTajlySeed()
            } catch (_: Exception) { }
        }

        mutableState.update {
            it.copy(isLoading = true, error = null)
        }

        try {
            val result = communityRepository.getCommunityFeed(filter)
            result.fold(
                onSuccess = { posts ->
                    val emptyMessage = if (posts.isEmpty() && filter == "friends") {
                        "Follow people to see their posts here"
                    } else null
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            posts = posts,
                            activeFilter = filter,
                            emptyMessage = emptyMessage
                        )
                    }
                    // Load liked post IDs for accurate heart state
                    val postIds = posts.map { it.id }
                    if (postIds.isNotEmpty()) {
                        val likedIds = communityRepository.getLikedPostIds(postIds)
                        mutableState.update { it.copy(likedPostIds = it.likedPostIds + likedIds) }
                    }
                },
                onFailure = { error ->
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            error = "${tr(StringKey.VME_LOAD_FEED_FAILED)}: ${error.message}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            mutableState.update {
                it.copy(
                    isLoading = false,
                    error = "${tr(StringKey.VME_LOAD_FEED_FAILED)}: ${e.message}"
                )
            }
        }
    }

    /** Report a post for review (App Store UGC compliance, Guideline 1.2). */
    suspend fun reportPost(postId: String, authorId: String, reason: String = "Inappropriate content") {
        communityRepository.reportPost(postId, authorId, reason)
        mutableState.update { it.copy(actionMessage = "Thanks — we'll review this within 24 hours.") }
    }

    /** Block a user: hide their posts immediately and persist the block. */
    fun blockUser(userId: String) {
        org.awi.fitness.data.UserSettings.getInstance().blockUser(userId)
        mutableState.update { st ->
            st.copy(
                posts = st.posts.filter { it.userId != userId },
                actionMessage = "Blocked — you won't see their posts anymore."
            )
        }
    }

    fun clearActionMessage() {
        mutableState.update { it.copy(actionMessage = null) }
    }

    /** Report a comment for review (App Store UGC compliance, Guideline 1.2). */
    suspend fun reportComment(commentId: String, commentAuthorId: String, parentPostId: String) {
        communityRepository.reportComment(commentId, commentAuthorId, parentPostId, "Inappropriate content")
        _postDetailState.update { it.copy(actionMessage = "Thanks — we'll review this within 24 hours.") }
    }

    /** Block a user from the post-detail screen: hide their comments now and persist the block. */
    fun blockUserFromDetail(userId: String) {
        org.awi.fitness.data.UserSettings.getInstance().blockUser(userId)
        _postDetailState.update { st ->
            st.copy(
                comments = st.comments.filter { it.userId != userId },
                actionMessage = "Blocked — you won't see their content anymore."
            )
        }
    }

    fun clearDetailActionMessage() {
        _postDetailState.update { it.copy(actionMessage = null) }
    }

    suspend fun likePost(postId: String) {
        try {
            val result = communityRepository.likePost(postId)
            result.fold(
                onSuccess = { nowLiked ->
                    mutableState.update { state ->
                        val newLikedIds = if (nowLiked) {
                            state.likedPostIds + postId
                        } else {
                            state.likedPostIds - postId
                        }
                        val updatedPosts = state.posts.map { post ->
                            if (post.id == postId) {
                                post.copy(likes = (post.likes + if (nowLiked) 1 else -1).coerceAtLeast(0))
                            } else {
                                post
                            }
                        }
                        state.copy(posts = updatedPosts, likedPostIds = newLikedIds)
                    }
                    _postDetailState.update { state ->
                        if (state.post?.id == postId) {
                            state.copy(
                                post = state.post.copy(
                                    likes = (state.post.likes + if (nowLiked) 1 else -1).coerceAtLeast(0)
                                ),
                                likedByMe = nowLiked
                            )
                        } else {
                            state
                        }
                    }
                },
                onFailure = { error ->
                    mutableState.update { it.copy(error = tr(StringKey.VME_UPDATE_LIKE_FAILED)) }
                }
            )
        } catch (_: Exception) { }
    }
    
    // Post detail actions
    suspend fun loadPostDetail(postId: String) {
        _postDetailState.update { it.copy(isLoading = true, error = null) }

        try {
            // Try the in-memory feed first; fall back to a Firestore fetch if not found.
            val post = state.value.posts.find { it.id == postId }
                ?: communityRepository.getPost(postId).getOrNull()

            if (post != null) {
                // Seed the heart state from Firestore so the like button reflects reality.
                val likedByMe = communityRepository.getLikedPostIds(listOf(postId)).contains(postId)
                _postDetailState.update { it.copy(post = post, likedByMe = likedByMe) }

                val commentsResult = communityRepository.getPostComments(postId)
                commentsResult.fold(
                    onSuccess = { comments ->
                        // Hide comments from users this person has blocked.
                        val blocked = org.awi.fitness.data.UserSettings.getInstance().blockedUserIds
                        _postDetailState.update {
                            it.copy(isLoading = false, comments = comments.filter { c -> c.userId !in blocked })
                        }
                    },
                    onFailure = { error ->
                        _postDetailState.update {
                            it.copy(
                                isLoading = false,
                                error = "${tr(StringKey.VME_LOAD_COMMENTS_FAILED)}: ${error.message}"
                            )
                        }
                    }
                )
            } else {
                _postDetailState.update {
                    it.copy(isLoading = false, error = tr(StringKey.VME_POST_NOT_FOUND))
                }
            }
        } catch (e: Exception) {
            _postDetailState.update {
                it.copy(isLoading = false, error = "${tr(StringKey.VME_LOAD_POST_FAILED)}: ${e.message}")
            }
        }
    }
    
    fun updateNewCommentText(text: String) {
        _postDetailState.update { it.copy(newCommentText = text) }
    }
    
    suspend fun addComment() {
        val postId = _postDetailState.value.post?.id ?: return
        val commentText = _postDetailState.value.newCommentText
        
        if (commentText.isBlank()) return
        
        try {
            val result = communityRepository.addComment(postId, commentText)
            result.fold(
                onSuccess = { comment ->
                    // Add the new comment to the list
                    _postDetailState.update { state ->
                        state.copy(
                            comments = state.comments + comment,
                            newCommentText = "" // Clear the input field
                        )
                    }
                    
                    // Update the comment count in the post
                    _postDetailState.update { state ->
                        state.copy(
                            post = state.post?.copy(comments = state.post.comments + 1)
                        )
                    }
                    
                    // Also update in the feed
                    mutableState.update { state ->
                        val updatedPosts = state.posts.map { post ->
                            if (post.id == postId) {
                                post.copy(comments = post.comments + 1)
                            } else {
                                post
                            }
                        }
                        state.copy(posts = updatedPosts)
                    }
                },
                onFailure = { error ->
                    _postDetailState.update { it.copy(error = tr(StringKey.VME_POST_COMMENT_FAILED_RETRY)) }
                }
            )
        } catch (e: Exception) {
            _postDetailState.update { it.copy(error = "${tr(StringKey.VME_POST_COMMENT_FAILED)}: ${e.message}") }
        }
    }
    
    // Holds raw image bytes selected by the native picker; not stored in the immutable state.
    private var pendingImageBytes: ByteArray? = null

    // Create post actions
    fun updateCreatePostContent(content: String) {
        _createPostState.update { it.copy(content = content, error = null) }
    }

    fun resetCreatePostState() {
        pendingImageBytes = null
        _createPostState.value = CreatePostState()
    }
    
    fun updateCreatePostImage(imageUrl: String?) {
        _createPostState.update { it.copy(imageUrl = imageUrl) }
    }

    fun updateCreatePostImageBytes(bytes: ByteArray?) {
        pendingImageBytes = bytes
        // Clear any previously set URL so only one source is active at a time.
        _createPostState.update { it.copy(imageUrl = if (bytes != null) null else it.imageUrl) }
    }
    
    fun updateCreatePostWorkoutCategory(category: WorkoutCategory?) {
        _createPostState.update { it.copy(workoutCategory = category) }
    }
    
    fun updateCreatePostCalories(calories: Int?) {
        _createPostState.update { it.copy(calories = calories) }
    }
    
    fun updateCreatePostSteps(steps: Int?) {
        _createPostState.update { it.copy(steps = steps) }
    }
    
    fun updateCreatePostDuration(duration: Int?) {
        _createPostState.update { it.copy(duration = duration) }
    }
    
    suspend fun createPost() {
        val state = _createPostState.value
        
        if (state.content.isBlank()) return
        
        _createPostState.update { it.copy(isLoading = true, error = null) }

        // If the user selected an image via the native picker, upload it first.
        val resolvedImageUrl: String? = pendingImageBytes?.let { bytes ->
            val fileName = "${currentTimeMillis()}.jpg"
            communityRepository.uploadImageToStorage(bytes, fileName)
        } ?: state.imageUrl

        if (pendingImageBytes != null && resolvedImageUrl == null) {
            _createPostState.update {
                it.copy(
                    isLoading = false,
                    error = tr(StringKey.VME_UPLOAD_PHOTO_FAILED)
                )
            }
            return
        }
        
        try {
            val result = communityRepository.createPost(
                content = state.content,
                imageUrl = resolvedImageUrl,
                workoutCategory = state.workoutCategory,
                calories = state.calories,
                steps = state.steps,
                duration = state.duration
            )
            
            result.fold(
                onSuccess = { post ->
                    pendingImageBytes = null
                    // Auto-progress POSTS-type challenges
                    org.awi.fitness.viewmodel.ViewModelStore.challenges.autoProgressPostChallenges()
                    // Signal success ONLY — do NOT update feed state here.
                    // The UI reads isSuccess, pops the screen, THEN we refresh the feed
                    // via CommunityEvents so no state update fires on a dead screen.
                    _createPostState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            content = "",
                            imageUrl = null,
                            workoutCategory = null,
                            calories = null,
                            steps = null,
                            duration = null
                        )
                    }
                },
                onFailure = { error ->
                    _createPostState.update {
                        it.copy(
                            isLoading = false,
                            error = "${tr(StringKey.VME_CREATE_POST_FAILED)}: ${error.message}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            _createPostState.update { 
                it.copy(
                    isLoading = false,
                    error = "${tr(StringKey.VME_CREATE_POST_FAILED)}: ${e.message}"
                )
            }
        }
    }
    
    // Find friends actions
    suspend fun loadSuggestedUsers() {
        _findFriendsState.update { it.copy(isLoading = true, error = null) }
        
        try {
            val result = communityRepository.getSuggestedUsers()
            result.fold(
                onSuccess = { users ->
                    _findFriendsState.update { 
                        it.copy(
                            isLoading = false,
                            suggestedUsers = users
                        )
                    }
                },
                onFailure = { error ->
                    _findFriendsState.update { 
                        it.copy(
                            isLoading = false,
                            error = "${tr(StringKey.VME_LOAD_SUGGESTED_USERS_FAILED)}: ${error.message}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            _findFriendsState.update { 
                it.copy(
                    isLoading = false,
                    error = "${tr(StringKey.VME_LOAD_SUGGESTED_USERS_FAILED)}: ${e.message}"
                )
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _findFriendsState.update { it.copy(searchQuery = query) }

        // Instant local feedback while typing: filter the already-loaded suggestions.
        // The real, full-collection search runs on the keyboard Search action (performUserSearch).
        if (query.isNotEmpty()) {
            val results = _findFriendsState.value.suggestedUsers.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.username.contains(query, ignoreCase = true)
            }
            _findFriendsState.update { it.copy(searchResults = results) }
        } else {
            _findFriendsState.update { it.copy(searchResults = emptyList()) }
        }
    }

    // Real Firestore-backed search across the whole users collection. Falls back to the
    // in-memory filter over loaded suggestions if the query fails.
    suspend fun performUserSearch() {
        val query = _findFriendsState.value.searchQuery.trim()
        if (query.isEmpty()) {
            _findFriendsState.update { it.copy(searchResults = emptyList()) }
            return
        }
        val result = communityRepository.searchUsers(query)
        result.fold(
            onSuccess = { users ->
                _findFriendsState.update { it.copy(searchResults = users) }
            },
            onFailure = {
                val results = _findFriendsState.value.suggestedUsers.filter {
                    it.name.contains(query, ignoreCase = true) ||
                    it.username.contains(query, ignoreCase = true)
                }
                _findFriendsState.update { it.copy(searchResults = results) }
            }
        )
    }
    
    fun updateFriendsTab(tab: String) {
        _findFriendsState.update { it.copy(activeTab = tab) }
    }
    
    suspend fun followUser(userId: String) {
        try {
            val result = communityRepository.followUser(userId)
            result.fold(
                onSuccess = { success ->
                    if (success) {
                        // Update the user in the suggested users list
                        _findFriendsState.update { state ->
                            val updatedUsers = state.suggestedUsers.map { user ->
                                if (user.id == userId) {
                                    user.copy(isFollowing = true)
                                } else {
                                    user
                                }
                            }
                            state.copy(suggestedUsers = updatedUsers)
                        }
                    }
                },
                onFailure = { error ->
                    // Revert optimistic update — the backend failed, undo the isFollowing change
                    _findFriendsState.update { state ->
                        val revertedUsers = state.suggestedUsers.map { user ->
                            if (user.id == userId) user.copy(isFollowing = false) else user
                        }
                        state.copy(suggestedUsers = revertedUsers, error = tr(StringKey.VME_FOLLOW_USER_FAILED_RETRY))
                    }
                }
            )
        } catch (e: Exception) {
            _findFriendsState.update { state ->
                val revertedUsers = state.suggestedUsers.map { user ->
                    if (user.id == userId) user.copy(isFollowing = false) else user
                }
                state.copy(suggestedUsers = revertedUsers, error = "${tr(StringKey.VME_FOLLOW_USER_FAILED)}: ${e.message}")
            }
        }
    }

    // Activity actions
    suspend fun loadActivityNotifications() {
        _activityState.update { it.copy(isLoading = true, error = null) }

        try {
            val result = communityRepository.getActivityNotifications()
            result.fold(
                onSuccess = { notifications ->
                    _activityState.update {
                        it.copy(
                            isLoading = false,
                            notifications = notifications,
                            unreadCount = notifications.count { !it.isRead }
                        )
                    }
                },
                onFailure = { error ->
                    _activityState.update {
                        it.copy(
                            isLoading = false,
                            error = "${tr(StringKey.VME_LOAD_NOTIFICATIONS_FAILED)}: ${error.message}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            _activityState.update {
                it.copy(
                    isLoading = false,
                    error = "${tr(StringKey.VME_LOAD_NOTIFICATIONS_FAILED)}: ${e.message}"
                )
            }
        }
    }
    
    suspend fun markNotificationAsRead(notificationId: String) {
        try {
            val result = communityRepository.markNotificationAsRead(notificationId)
            result.fold(
                onSuccess = { success ->
                    if (success) {
                        // Update the notification in the list
                        _activityState.update { state ->
                            val updatedNotifications = state.notifications.map { notification ->
                                if (notification.id == notificationId) {
                                    notification.copy(isRead = true)
                                } else {
                                    notification
                                }
                            }
                            state.copy(
                                notifications = updatedNotifications,
                                unreadCount = updatedNotifications.count { !it.isRead }
                            )
                        }
                    }
                },
                onFailure = { error ->
                    // Handle error
                }
            )
        } catch (e: Exception) {
            // Handle exception
        }
    }
    
    // Profile actions
    suspend fun loadUserProfile(userId: String) {
        _profileState.update { it.copy(isLoading = true, error = null, userPosts = emptyList()) }

        try {
            val userResult = communityRepository.getUserProfile(userId)
            userResult.fold(
                onSuccess = { user ->
                    _profileState.update { it.copy(isLoading = false, user = user) }
                    val postsResult = communityRepository.getUserPosts(userId)
                    postsResult.fold(
                        onSuccess = { posts ->
                            _profileState.update { it.copy(userPosts = posts) }
                        },
                        onFailure = { /* posts optional */ }
                    )
                },
                onFailure = { error ->
                    _profileState.update {
                        it.copy(isLoading = false, error = "${tr(StringKey.VME_LOAD_PROFILE_FAILED)}: ${error.message}")
                    }
                }
            )
        } catch (e: Exception) {
            _profileState.update {
                it.copy(isLoading = false, error = "${tr(StringKey.VME_LOAD_PROFILE_FAILED)}: ${e.message}")
            }
        }
    }

    suspend fun updateUserProfile(
        displayName: String,
        bio: String,
        profileImageUrl: String?,
        website: String? = null,
        instagram: String? = null,
        twitter: String? = null
    ) {
        _profileState.update { it.copy(isSavingProfile = true, saveProfileSuccess = false) }
        try {
            val result = communityRepository.updateUserProfile(
                displayName, bio, profileImageUrl, website, instagram, twitter
            )
            result.fold(
                onSuccess = {
                    _profileState.update { state ->
                        state.copy(
                            isSavingProfile = false,
                            saveProfileSuccess = true,
                            user = state.user?.copy(
                                name = displayName.ifBlank { state.user.name },
                                bio = bio.ifBlank { null },
                                profileImage = profileImageUrl ?: state.user.profileImage,
                                website = if (website != null) website.ifBlank { null } else state.user.website,
                                instagram = if (instagram != null) instagram.ifBlank { null } else state.user.instagram,
                                twitter = if (twitter != null) twitter.ifBlank { null } else state.user.twitter
                            )
                        )
                    }
                    val settings = org.awi.fitness.data.UserSettings.getInstance()
                    if (displayName.isNotBlank()) settings.userName = displayName
                    if (bio.isNotBlank()) settings.userBio = bio
                    profileImageUrl?.let { settings.profilePhotoUrl = it }
                },
                onFailure = { e ->
                    _profileState.update { it.copy(isSavingProfile = false, error = e.message) }
                }
            )
        } catch (e: Exception) {
            _profileState.update { it.copy(isSavingProfile = false, error = e.message) }
        }
    }

    suspend fun updateUserProfileWithPhoto(
        displayName: String,
        bio: String,
        existingPhotoUrl: String?,
        imageBytes: ByteArray?,
        website: String? = null,
        instagram: String? = null,
        twitter: String? = null
    ) {
        _profileState.update { it.copy(isSavingProfile = true, saveProfileSuccess = false) }
        try {
            // Upload new photo if bytes provided
            val resolvedPhotoUrl: String? = if (imageBytes != null) {
                val fileName = "${org.awi.fitness.utils.currentTimeMillis()}_profile.jpg"
                // Avatars only need ~512px — compress harder than post images.
                communityRepository.uploadImageToStorage(imageBytes, fileName, maxDim = 512)
            } else {
                existingPhotoUrl
            }
            updateUserProfile(displayName, bio, resolvedPhotoUrl, website, instagram, twitter)
        } catch (e: Exception) {
            _profileState.update { it.copy(isSavingProfile = false, error = e.message) }
        }
    }

    fun resetProfileSaveSuccess() {
        _profileState.update { it.copy(saveProfileSuccess = false) }
    }

    suspend fun toggleFollowUser(userId: String) {
        val currentlyFollowing = _profileState.value.user?.isFollowing == true
        if (currentlyFollowing) {
            // Optimistic UI update first, then call backend
            _profileState.update { state ->
                state.copy(user = state.user?.copy(isFollowing = false))
            }
            _findFriendsState.update { state ->
                state.copy(
                    suggestedUsers = state.suggestedUsers.map { u ->
                        if (u.id == userId) u.copy(isFollowing = false) else u
                    }
                )
            }
            communityRepository.unfollowUser(userId)
        } else {
            followUser(userId)
            _profileState.update { state ->
                state.copy(user = state.user?.copy(isFollowing = true))
            }
        }
    }
}
