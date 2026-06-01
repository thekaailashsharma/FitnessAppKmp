package org.awi.fitness.viewmodel

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
import org.awi.fitness.utils.CommunityEvents

data class CommunityFeedState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val posts: List<CommunityPost> = emptyList(),
    val activeFilter: String = "public", // public, friends, my_posts
    val likedPostIds: Set<String> = emptySet()
)

data class PostDetailState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val post: CommunityPost? = null,
    val comments: List<CommunityComment> = emptyList(),
    val newCommentText: String = "",
    val likedByMe: Boolean = false
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
        mutableState.update { 
            it.copy(isLoading = true, error = null) 
        }
        
        try {
            val result = communityRepository.getCommunityFeed(filter)
            result.fold(
                onSuccess = { posts ->
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            posts = posts,
                            activeFilter = filter
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
                            error = "Failed to load feed: ${error.message}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            mutableState.update { 
                it.copy(
                    isLoading = false,
                    error = "Failed to load feed: ${e.message}"
                )
            }
        }
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
                            val nowLikedInDetail = state.likedByMe != nowLiked
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
                onFailure = { /* silently ignore */ }
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
                _postDetailState.update { it.copy(post = post) }

                val commentsResult = communityRepository.getPostComments(postId)
                commentsResult.fold(
                    onSuccess = { comments ->
                        _postDetailState.update {
                            it.copy(isLoading = false, comments = comments)
                        }
                    },
                    onFailure = { error ->
                        _postDetailState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load comments: ${error.message}"
                            )
                        }
                    }
                )
            } else {
                _postDetailState.update {
                    it.copy(isLoading = false, error = "Post not found")
                }
            }
        } catch (e: Exception) {
            _postDetailState.update {
                it.copy(isLoading = false, error = "Failed to load post: ${e.message}")
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
                    // Handle error
                }
            )
        } catch (e: Exception) {
            // Handle exception
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
                    error = "Failed to upload photo. Try again or post without a photo."
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
                            error = "Failed to create post: ${error.message}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            _createPostState.update { 
                it.copy(
                    isLoading = false,
                    error = "Failed to create post: ${e.message}"
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
                            error = "Failed to load suggested users: ${error.message}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            _findFriendsState.update { 
                it.copy(
                    isLoading = false,
                    error = "Failed to load suggested users: ${e.message}"
                )
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _findFriendsState.update { it.copy(searchQuery = query) }
        
        // In a real app, we'd do a search here
        // For now, we'll just filter the suggested users
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
                    // Handle error
                }
            )
        } catch (e: Exception) {
            // Handle exception
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
                            error = "Failed to load notifications: ${error.message}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            _activityState.update {
                it.copy(
                    isLoading = false,
                    error = "Failed to load notifications: ${e.message}"
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
                        it.copy(isLoading = false, error = "Failed to load profile: ${error.message}")
                    }
                }
            )
        } catch (e: Exception) {
            _profileState.update {
                it.copy(isLoading = false, error = "Failed to load profile: ${e.message}")
            }
        }
    }

    suspend fun updateUserProfile(displayName: String, bio: String, profileImageUrl: String?) {
        _profileState.update { it.copy(isSavingProfile = true, saveProfileSuccess = false) }
        try {
            val result = communityRepository.updateUserProfile(displayName, bio, profileImageUrl)
            result.fold(
                onSuccess = {
                    _profileState.update { state ->
                        state.copy(
                            isSavingProfile = false,
                            saveProfileSuccess = true,
                            user = state.user?.copy(
                                name = displayName.ifBlank { state.user.name },
                                bio = bio.ifBlank { null },
                                profileImage = profileImageUrl ?: state.user.profileImage
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
        imageBytes: ByteArray?
    ) {
        _profileState.update { it.copy(isSavingProfile = true, saveProfileSuccess = false) }
        try {
            // Upload new photo if bytes provided
            val resolvedPhotoUrl: String? = if (imageBytes != null) {
                val fileName = "${org.awi.fitness.utils.currentTimeMillis()}_profile.jpg"
                communityRepository.uploadImageToStorage(imageBytes, fileName)
            } else {
                existingPhotoUrl
            }
            updateUserProfile(displayName, bio, resolvedPhotoUrl)
        } catch (e: Exception) {
            _profileState.update { it.copy(isSavingProfile = false, error = e.message) }
        }
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
