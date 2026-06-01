package org.awi.fitness.viewmodel

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.Article
import org.awi.fitness.repository.ArticleRepository

data class ArticleUiState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    val error: String? = null,
    val hasLoadedOnce: Boolean = false
)

class ArticleViewModel {
    private val repository = ArticleRepository()
    private val userSettings = UserSettings.getInstance()

    private val _state = MutableStateFlow(ArticleUiState())
    val state: StateFlow<ArticleUiState> = _state.asStateFlow()

    fun loadIfNeeded() {
        if (_state.value.isLoading) return
        if (_state.value.hasLoadedOnce) {
            val now = org.awi.fitness.utils.currentTimeMillis()
            val lastRefresh = userSettings.lastArticleRefresh
            val dayMs = 24 * 60 * 60 * 1000L
            if (now - lastRefresh < dayMs) return
        }
        loadArticles()
    }

    fun loadArticles() {
        _state.update { it.copy(isLoading = true, error = null) }
        MainScope().launch {
            val lang = if (userSettings.language.value == "nl") "nl" else "en"
            repository.fetchArticles(lang).fold(
                onSuccess = { articles ->
                    userSettings.lastArticleRefresh = org.awi.fitness.utils.currentTimeMillis()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            articles = articles,
                            hasLoadedOnce = true
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message,
                            hasLoadedOnce = true
                        )
                    }
                }
            )
        }
    }

    fun refresh() {
        loadArticles()
    }
}
