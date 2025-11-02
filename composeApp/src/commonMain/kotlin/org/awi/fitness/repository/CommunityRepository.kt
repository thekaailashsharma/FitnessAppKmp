package org.awi.fitness.repository

import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import org.awi.fitness.model.*
import org.awi.fitness.network.ApiService
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CommunityRepository : ApiService() {
    companion object {
        private const val PROJECT_ID = "fitness-admin-73a72"
    }
    
    // For demo purposes, we'll use mock data for now
    // In a real app, these would be Firestore calls
    private val mockUsers = listOf(
        CommunityUser(
            id = "user1",
            name = "Alex Johnson",
            username = "@alex_j",
            profileImage = "https://i.pravatar.cc/150?img=1",
            streakDays = 7,
            level = 5,
            badges = listOf(
                Badge(id = "badge1", name = "5K Finisher", description = "Completed a 5k run"),
                Badge(id = "badge2", name = "Early Riser", description = "Woke up before 6am"),
                Badge(id = "badge3", name = "New PR", description = "Set a new personal record")
            )
        ),
        CommunityUser(
            id = "user2",
            name = "Samantha Bee",
            username = "@samantha_b",
            profileImage = "https://i.pravatar.cc/150?img=5",
            streakDays = 12,
            level = 7,
            badges = listOf(
                Badge(id = "badge1", name = "5K Runner", description = "Completed a 5k run")
            )
        ),
        CommunityUser(
            id = "user3",
            name = "David Beckham",
            username = "@david_b",
            profileImage = "https://i.pravatar.cc/150?img=3",
            streakDays = 25,
            level = 10,
            badges = listOf(
                Badge(id = "badge4", name = "Cardio King", description = "Completed 30 cardio workouts")
            ),
            isFollowing = true
        ),
        CommunityUser(
            id = "user4",
            name = "Anna Wintour",
            username = "@anna_w",
            profileImage = "https://i.pravatar.cc/150?img=9",
            streakDays = 12,
            level = 6,
            badges = listOf(
                Badge(id = "badge5", name = "5K Runner", description = "Completed a 5k run")
            )
        )
    )
    
    val mockPosts = listOf(
        CommunityPost(
            id = "post1",
            userId = "user1",
            userName = "Alex Johnson",
            userProfileImage = "https://i.pravatar.cc/150?img=1",
            content = "Completed Full Body HIIT",
            imageUrl = "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1470&q=80",
            workoutCategory = WorkoutCategory.HIIT,
            timestamp = Clock.System.now().toEpochMilliseconds() - 3600000,
            likes = 12,
            comments = 3,
            calories = 450,
            steps = 3200,
            duration = 2700, // 45 minutes
            isPersonalBest = true
        ),
        CommunityPost(
            id = "post2",
            userId = "user2",
            userName = "Samantha Bee",
            userProfileImage = "https://i.pravatar.cc/150?img=5",
            content = "Morning Run Along the River",
            imageUrl = "https://images.unsplash.com/photo-1476480862126-209bfaa8edc8?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1470&q=80",
            workoutCategory = WorkoutCategory.CARDIO,
            timestamp = Clock.System.now().toEpochMilliseconds() - 7200000,
            likes = 8,
            comments = 2,
            calories = 350,
            steps = 5280,
            duration = 2100, // 35 minutes
            streakDays = 7,
            badges = listOf("5k Badge")
        ),
        CommunityPost(
            id = "post3",
            userId = "user3",
            userName = "David Beckham",
            userProfileImage = "https://i.pravatar.cc/150?img=3",
            content = "Strength Training Session",
            imageUrl = "https://images.unsplash.com/photo-1581009146145-b5ef050c2e1e?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1470&q=80",
            workoutCategory = WorkoutCategory.STRENGTH,
            timestamp = Clock.System.now().toEpochMilliseconds() - 10800000,
            likes = 24,
            comments = 5,
            calories = 520,
            duration = 3600 // 60 minutes
        )
    )
    
    private val mockComments = listOf(
        CommunityComment(
            id = "comment1",
            postId = "post1",
            userId = "user2",
            userName = "Samantha Bee",
            userProfileImage = "https://i.pravatar.cc/150?img=5",
            content = "Great job! How was the workout?",
            timestamp = Clock.System.now().toEpochMilliseconds() - 1800000
        ),
        CommunityComment(
            id = "comment2",
            postId = "post1",
            userId = "user3",
            userName = "David Beckham",
            userProfileImage = "https://i.pravatar.cc/150?img=3",
            content = "Impressive progress!",
            timestamp = Clock.System.now().toEpochMilliseconds() - 900000
        )
    )
    
    private val mockNotifications = listOf(
        ActivityNotification(
            id = "notif1",
            type = CommunityActivityType.LIKE,
            userId = "user1",
            userName = "John Doe",
            userProfileImage = "https://i.pravatar.cc/150?img=8",
            targetId = "post1",
            targetContent = "Morning Run",
            timestamp = Clock.System.now().toEpochMilliseconds() - 300000
        ),
        ActivityNotification(
            id = "notif2",
            type = CommunityActivityType.COMMENT,
            userId = "user2",
            userName = "Jane Doe",
            userProfileImage = "https://i.pravatar.cc/150?img=9",
            targetId = "post1",
            targetContent = "Wow, that's an impressive workout!",
            timestamp = Clock.System.now().toEpochMilliseconds() - 600000
        ),
        ActivityNotification(
            id = "notif3",
            type = CommunityActivityType.FOLLOW,
            userId = "user3",
            userName = "Mike Smith",
            userProfileImage = "https://i.pravatar.cc/150?img=12",
            targetId = "",
            targetContent = "You have a new follower!",
            timestamp = Clock.System.now().toEpochMilliseconds() - 3600000
        ),
        ActivityNotification(
            id = "notif4",
            type = CommunityActivityType.CHALLENGE_INVITE,
            userId = "system",
            userName = "System",
            userProfileImage = null,
            targetId = "challenge1",
            targetContent = "Weekend Warrior",
            timestamp = Clock.System.now().toEpochMilliseconds() - 7200000
        ),
        ActivityNotification(
            id = "notif5",
            type = CommunityActivityType.BADGE_EARNED,
            userId = "system",
            userName = "System",
            userProfileImage = null,
            targetId = "badge1",
            targetContent = "Morning Mover",
            timestamp = Clock.System.now().toEpochMilliseconds() - 86400000
        ),
        ActivityNotification(
            id = "notif6",
            type = CommunityActivityType.STREAK_MILESTONE,
            userId = "system",
            userName = "System",
            userProfileImage = null,
            targetId = "",
            targetContent = "5-day workout streak",
            timestamp = Clock.System.now().toEpochMilliseconds() - 259200000
        )
    )
    
    // Get community feed posts
    suspend fun getCommunityFeed(filter: String = "public"): Result<List<CommunityPost>> {
        return try {
            // In a real app, we'd make a Firestore call here
            val posts = when (filter) {
                "friends" -> mockPosts.filter { it.userId in mockUsers.filter { user -> user.isFollowing }.map { it.id } }
                "my_posts" -> mockPosts.filter { it.userId == "user1" } // Assuming current user is user1
                else -> mockPosts
            }
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get post comments
    suspend fun getPostComments(postId: String): Result<List<CommunityComment>> {
        return try {
            // In a real app, we'd make a Firestore call here
            val comments = mockComments.filter { it.postId == postId }
            Result.success(comments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Add a comment to a post
    @OptIn(ExperimentalUuidApi::class)
    suspend fun addComment(postId: String, content: String): Result<CommunityComment> {
        return try {
            // In a real app, we'd make a Firestore call here
            val comment = CommunityComment(
                id = Uuid.random().toString(),
                postId = postId,
                userId = "user1", // Assuming current user is user1
                userName = "Current User",
                userProfileImage = "https://i.pravatar.cc/150?img=7",
                content = content,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
            Result.success(comment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Like a post
    suspend fun likePost(postId: String): Result<Boolean> {
        return try {
            // In a real app, we'd make a Firestore call here
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Create a new post
    @OptIn(ExperimentalUuidApi::class)
    suspend fun createPost(
        content: String,
        imageUrl: String?,
        workoutCategory: WorkoutCategory?,
        calories: Int?,
        steps: Int?,
        duration: Int?
    ): Result<CommunityPost> {
        return try {
            // In a real app, we'd make a Firestore call here
            val post = CommunityPost(
                id = Uuid.random().toString(),
                userId = "user1", // Assuming current user is user1
                userName = "Current User",
                userProfileImage = "https://i.pravatar.cc/150?img=7",
                content = content,
                imageUrl = imageUrl,
                workoutCategory = workoutCategory,
                timestamp = Clock.System.now().toEpochMilliseconds(),
                likes = 0,
                comments = 0,
                calories = calories,
                steps = steps,
                duration = duration
            )
            Result.success(post)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get suggested users to follow
    suspend fun getSuggestedUsers(): Result<List<CommunityUser>> {
        return try {
            // In a real app, we'd make a Firestore call here
            Result.success(mockUsers.filter { !it.isFollowing })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Follow a user
    suspend fun followUser(userId: String): Result<Boolean> {
        return try {
            // In a real app, we'd make a Firestore call here
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get activity notifications
    suspend fun getActivityNotifications(): Result<List<ActivityNotification>> {
        return try {
            // In a real app, we'd make a Firestore call here
            Result.success(mockNotifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Mark notification as read
    suspend fun markNotificationAsRead(notificationId: String): Result<Boolean> {
        return try {
            // In a real app, we'd make a Firestore call here
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get user profile
    suspend fun getUserProfile(userId: String): Result<CommunityUser> {
        return try {
            // In a real app, we'd make a Firestore call here
            val user = mockUsers.find { it.id == userId }
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
