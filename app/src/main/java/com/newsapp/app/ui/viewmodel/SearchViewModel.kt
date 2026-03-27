package com.newsapp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newsapp.app.data.model.NewsArticle
import com.newsapp.app.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    val searchResults: StateFlow<List<NewsArticle>> = _searchQuery
        .debounce(300)
        .filter { it.length >= 2 }
        .flatMapLatest { query ->
            _isSearching.value = true
            newsRepository.searchArticles(query)
        }
        .onEach { _isSearching.value = false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    fun updateQuery(query: String) {
        _searchQuery.value = query
    }

    fun addToRecentSearches(query: String) {
        if (query.isNotBlank()) {
            _recentSearches.value = (listOf(query) + _recentSearches.value)
                .distinct()
                .take(10)
        }
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    fun onlineSearch(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            newsRepository.fetchNews()
            _isSearching.value = false
        }
    }
}
