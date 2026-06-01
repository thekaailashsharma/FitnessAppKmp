package org.awi.fitness.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CommunityEvents {
    private val _feedRefreshTick = MutableStateFlow(0)
    val feedRefreshTick: StateFlow<Int> = _feedRefreshTick.asStateFlow()

    fun requestFeedRefresh() {
        _feedRefreshTick.value++
    }
}
