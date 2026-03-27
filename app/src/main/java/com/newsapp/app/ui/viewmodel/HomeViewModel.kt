package com.newsapp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newsapp.app.data.model.NewsArticle
import com.newsapp.app.data.model.SportCategory
import com.newsapp.app.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(SportCategory.ALL)
    val selectedCategory: StateFlow<SportCategory> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val articles: StateFlow<List<NewsArticle>> = _selectedCategory
        .flatMapLatest { category ->
            if (category == SportCategory.ALL) {
                newsRepository.getAllArticles()
            } else {
                newsRepository.getArticlesByCategory(category)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshNews()
    }

    fun selectCategory(category: SportCategory) {
        _selectedCategory.value = category
        refreshNews()
    }

    fun refreshNews() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            try {
                val category = _selectedCategory.value
                newsRepository.fetchNews(category)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load news"
            } finally {
                _isRefreshing.value = false
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(articleId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            newsRepository.toggleFavorite(articleId, isFavorite)
        }
    }

    fun toggleBookmark(articleId: String, isBookmarked: Boolean) {
        viewModelScope.launch {
            newsRepository.toggleBookmark(articleId, isBookmarked)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
