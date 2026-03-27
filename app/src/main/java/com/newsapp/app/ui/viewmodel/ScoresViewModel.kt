package com.newsapp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newsapp.app.data.model.LiveScore
import com.newsapp.app.data.model.MatchStatus
import com.newsapp.app.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScoresViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _scores = MutableStateFlow<List<LiveScore>>(emptyList())
    val scores: StateFlow<List<LiveScore>> = _scores.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedFilter = MutableStateFlow(ScoreFilter.ALL)
    val selectedFilter: StateFlow<ScoreFilter> = _selectedFilter.asStateFlow()

    val filteredScores: StateFlow<List<LiveScore>> = combine(_scores, _selectedFilter) { scores, filter ->
        when (filter) {
            ScoreFilter.ALL -> scores
            ScoreFilter.LIVE -> scores.filter { it.status == MatchStatus.LIVE }
            ScoreFilter.FINISHED -> scores.filter { it.status == MatchStatus.FINISHED }
            ScoreFilter.UPCOMING -> scores.filter { it.status == MatchStatus.UPCOMING }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchScores()
    }

    fun fetchScores() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            newsRepository.fetchLiveScores()
                .onSuccess { _scores.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun setFilter(filter: ScoreFilter) {
        _selectedFilter.value = filter
    }

    fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(60_000) // Refresh every minute
                fetchScores()
            }
        }
    }
}

enum class ScoreFilter(val displayName: String) {
    ALL("All"),
    LIVE("Live"),
    FINISHED("Finished"),
    UPCOMING("Upcoming")
}
