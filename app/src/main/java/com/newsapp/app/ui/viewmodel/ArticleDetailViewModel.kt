package com.newsapp.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newsapp.app.data.model.NewsArticle
import com.newsapp.app.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val articleId: String = savedStateHandle.get<String>("articleId") ?: ""

    private val _article = MutableStateFlow<NewsArticle?>(null)
    val article: StateFlow<NewsArticle?> = _article.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadArticle()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            _isLoading.value = true
            _article.value = newsRepository.getArticleById(articleId)
            _isLoading.value = false
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            _article.value?.let { article ->
                val newState = !article.isFavorite
                newsRepository.toggleFavorite(article.id, newState)
                _article.value = article.copy(isFavorite = newState)
            }
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            _article.value?.let { article ->
                val newState = !article.isBookmarked
                newsRepository.toggleBookmark(article.id, newState)
                _article.value = article.copy(isBookmarked = newState)
            }
        }
    }
}
