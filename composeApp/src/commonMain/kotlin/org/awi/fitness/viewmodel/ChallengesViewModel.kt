package org.awi.fitness.viewmodel

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.awi.fitness.model.Challenge
import org.awi.fitness.model.ChallengeProgress
import org.awi.fitness.model.ChallengeTargetType
import org.awi.fitness.model.LeaderboardEntry
import org.awi.fitness.repository.ChallengesRepository

data class ChallengesState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val activeChallenges: List<Challenge> = emptyList(),
    val availableChallenges: List<Challenge> = emptyList(),
    val selectedChallenge: Challenge? = null,
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val userProgress: Map<String, ChallengeProgress> = emptyMap(), // challengeId → progress
    val joiningChallengeIds: Set<String> = emptySet(),  // challenges currently being joined
    val joinedChallengeIds: Set<String> = emptySet()    // successfully joined this session
)

class ChallengesViewModel {

    private val repository = ChallengesRepository()
    private val scope = MainScope()

    private val _state = MutableStateFlow(ChallengesState())
    val state: StateFlow<ChallengesState> = _state.asStateFlow()

    // ---------------------------------------------------------------------------
    // Public actions
    // ---------------------------------------------------------------------------

    fun loadChallenges() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            // Seed system templates on first load (no-op if already seeded)
            runCatching { repository.seedSystemTemplatesIfNeeded() }

            val activeResult = repository.getActiveChallenges()
            val availableResult = repository.getAvailableChallenges()

            _state.value = _state.value.copy(
                isLoading = false,
                activeChallenges = activeResult.getOrElse { _state.value.activeChallenges },
                availableChallenges = availableResult.getOrElse { _state.value.availableChallenges },
                error = activeResult.exceptionOrNull()?.message
                    ?: availableResult.exceptionOrNull()?.message
            )

            // Load leaderboard for the first active challenge (overview)
            val firstActive = _state.value.activeChallenges.firstOrNull()
            if (firstActive != null) {
                loadLeaderboard(firstActive.id)
            }
        }
    }

    fun joinChallenge(challengeId: String) {
        if (challengeId in _state.value.joiningChallengeIds) return // dedupe rapid taps
        scope.launch {
            _state.value = _state.value.copy(
                joiningChallengeIds = _state.value.joiningChallengeIds + challengeId,
                error = null
            )
            val result = repository.joinChallenge(challengeId)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        joiningChallengeIds = _state.value.joiningChallengeIds - challengeId,
                        joinedChallengeIds = _state.value.joinedChallengeIds + challengeId,
                        // Optimistically remove from available so it doesn't linger
                        availableChallenges = _state.value.availableChallenges
                            .filter { it.id != challengeId }
                    )
                    loadChallenges()
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        joiningChallengeIds = _state.value.joiningChallengeIds - challengeId,
                        error = "Failed to join challenge: ${e.message}"
                    )
                }
            )
        }
    }

    fun updateProgress(challengeId: String, newValue: Int) {
        scope.launch {
            val result = repository.updateProgress(challengeId, newValue)
            result.fold(
                onSuccess = {
                    // Optimistically update the progress map
                    val currentProgress = _state.value.userProgress[challengeId]
                    if (currentProgress != null) {
                        _state.value = _state.value.copy(
                            userProgress = _state.value.userProgress + (challengeId to currentProgress.copy(currentValue = newValue)),
                            activeChallenges = _state.value.activeChallenges.map { c ->
                                if (c.id == challengeId) c.copy(progress = newValue) else c
                            },
                            selectedChallenge = _state.value.selectedChallenge?.let { sel ->
                                if (sel.id == challengeId) sel.copy(progress = newValue) else sel
                            }
                        )
                    }
                    loadLeaderboard(challengeId)
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(error = e.message)
                }
            )
        }
    }

    fun loadLeaderboard(challengeId: String) {
        scope.launch {
            val result = repository.getLeaderboard(challengeId)
            result.onSuccess { entries ->
                _state.value = _state.value.copy(leaderboard = entries)
                // Populate userProgress from leaderboard so detail screen shows correct value
                val email = repository.currentUserEmail()
                val emailPrefix = email.substringBefore("@")
                val myEntry = entries.firstOrNull { it.userId == email || it.username == emailPrefix }
                if (myEntry != null) {
                    val currentProgress = myEntry.xp // xp field stores currentValue in leaderboard entries
                    _state.value = _state.value.copy(
                        userProgress = _state.value.userProgress + (challengeId to ChallengeProgress(
                            challengeId = challengeId,
                            userId = email,
                            currentValue = currentProgress
                        ))
                    )
                }
            }
        }
    }

    fun selectChallenge(challenge: Challenge) {
        _state.value = _state.value.copy(selectedChallenge = challenge)
    }

    // Called by WorkoutScreen when a workout day is completed (count-based, per event)
    fun autoProgressWorkoutChallenges() {
        autoProgressByType(ChallengeTargetType.WORKOUTS, perDay = false)
    }

    // Called by MealScreen when a meal is marked as eaten (count-based, per event)
    fun autoProgressMealChallenges() {
        autoProgressByType(ChallengeTargetType.MEALS, perDay = false)
    }

    // Called by CommunityViewModel when a post is created (count-based, per event)
    fun autoProgressPostChallenges() {
        autoProgressByType(ChallengeTargetType.POSTS, perDay = false)
    }

    // Called on daily check-in / workout for STREAK challenges (day-based → once per calendar day)
    fun autoProgressStreakChallenges() {
        autoProgressByType(ChallengeTargetType.STREAK, perDay = true)
    }

    /**
     * Advance active challenges of [type]. Count-based challenges advance once per event.
     * Day-based challenges ([perDay]) advance at most once per calendar day (deduped via
     * UserSettings) so a "7-day" target can't be finished by 7 events in one day.
     */
    private fun autoProgressByType(type: ChallengeTargetType, perDay: Boolean) {
        scope.launch {
            val userSettings = org.awi.fitness.data.UserSettings.getInstance()
            val matching = _state.value.activeChallenges.filter { it.targetType == type }
            var advancedAny = false
            for (challenge in matching) {
                if (perDay && !userSettings.canAdvanceChallengeToday(challenge.id)) continue
                val currentProgress = _state.value.userProgress[challenge.id]?.currentValue
                    ?: challenge.progress
                if (currentProgress < challenge.target) {
                    repository.updateProgress(challenge.id, currentProgress + 1)
                    if (perDay) userSettings.markChallengeAdvancedToday(challenge.id)
                    advancedAny = true
                }
            }
            if (advancedAny) loadChallenges()
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
