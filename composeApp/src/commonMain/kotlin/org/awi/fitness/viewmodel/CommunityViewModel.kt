package org.awi.fitness.viewmodel

import cafe.adriel.voyager.core.model.StateScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.awi.fitness.model.ActivityNotification
import org.awi.fitness.model.CommunityComment
import org.awi.fitness.model.CommunityPost
import org.awi.fitness.model.CommunityUser
import org.awi.fitness.model.WorkoutCategory
import org.awi.fitness.repository.CommunityRepository

data class CommunityFeedState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val posts: List<CommunityPost> = emptyList(),
    val activeFilter: String = "public" // public, friends, my_posts
)

data class PostDetailState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val post: CommunityPost? = null,
    val comments: List<CommunityComment> = emptyList(),
    val newCommentText: String = ""
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
    val userPosts: List<CommunityPost> = emptyList()
)

class CommunityViewModel : StateScreenModel<CommunityFeedState>(CommunityFeedState()) {
    // Repository reference
    val communityRepository = CommunityRepository()
    
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
                onSuccess = { success ->
                    if (success) {
                        // Update the post in the feed
                        mutableState.update { state ->
                            val updatedPosts = state.posts.map { post ->
                                if (post.id == postId) {
                                    post.copy(likes = post.likes + 1)
                                } else {
                                    post
                                }
                            }
                            state.copy(posts = updatedPosts)
                        }
                        
                        // Also update in post detail if loaded
                        _postDetailState.update { state ->
                            if (state.post?.id == postId) {
                                state.copy(post = state.post.copy(likes = state.post.likes + 1))
                            } else {
                                state
                            }
                        }
                    }
                },
                onFailure = { error ->
                    // Handle error (could show a toast or snackbar)
                }
            )
        } catch (e: Exception) {
            // Handle exception
        }
    }
    
    // Post detail actions
    suspend fun loadPostDetail(postId: String) {
        _postDetailState.update { it.copy(isLoading = true, error = null) }
        
        try {
            // First find the post in the feed
            val post = state.value.posts.find { it.id == postId }
            
            if (post != null) {
                _postDetailState.update { it.copy(post = post) }
                
                // Then load comments
                val commentsResult = communityRepository.getPostComments(postId)
                commentsResult.fold(
                    onSuccess = { comments ->
                        _postDetailState.update { 
                            it.copy(
                                isLoading = false,
                                comments = comments
                            )
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
                    it.copy(
                        isLoading = false,
                        error = "Post not found"
                    )
                }
            }
        } catch (e: Exception) {
            _postDetailState.update { 
                it.copy(
                    isLoading = false,
                    error = "Failed to load post: ${e.message}"
                )
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
    
    // Create post actions
    fun updateCreatePostContent(content: String) {
        _createPostState.update { it.copy(content = content) }
    }
    
    fun updateCreatePostImage(imageUrl: String?) {
        _createPostState.update { it.copy(imageUrl = imageUrl) }
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
        
        try {
            val result = communityRepository.createPost(
                content = state.content,
                imageUrl = state.imageUrl,
                workoutCategory = state.workoutCategory,
                calories = state.calories,
                steps = state.steps,
                duration = state.duration
            )
            
            result.fold(
                onSuccess = { post ->
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
                    
                    // Add the new post to the feed
                    mutableState.update { feedState ->
                        feedState.copy(posts = listOf(post) + feedState.posts)
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
        _profileState.update { it.copy(isLoading = true, error = null) }
        
        try {
            val userResult = communityRepository.getUserProfile(userId)
            userResult.fold(
                onSuccess = { user ->
                    _profileState.update { 
                        it.copy(
                            isLoading = false,
                            user = user
                        )
                    }
                    
                    // Load user posts
                    val postsResult = communityRepository.getCommunityFeed("public")
                    postsResult.fold(
                        onSuccess = { allPosts ->
                            val userPosts = allPosts.filter { it.userId == userId }
                            _profileState.update { 
                                it.copy(userPosts = userPosts)
                            }
                        },
                        onFailure = { error ->
                            // Just log the error, we already have the user profile
                        }
                    )
                },
                onFailure = { error ->
                    _profileState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to load profile: ${error.message}"
                        )
                    }
                }
            )
        } catch (e: Exception) {
            _profileState.update { 
                it.copy(
                    isLoading = false,
                    error = "Failed to load profile: ${e.message}"
                )
            }
        }
    }
}
