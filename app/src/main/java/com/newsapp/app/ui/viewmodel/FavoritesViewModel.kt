package com.newsapp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newsapp.app.data.model.NewsArticle
import com.newsapp.app.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0) // 0 = Favorites, 1 = Bookmarks
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    val favorites: StateFlow<List<NewsArticle>> = newsRepository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<NewsArticle>> = newsRepository.getBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun removeFavorite(articleId: String) {
        viewModelScope.launch {
            newsRepository.toggleFavorite(articleId, false)
        }
    }

    fun removeBookmark(articleId: String) {
        viewModelScope.launch {
            newsRepository.toggleBookmark(articleId, false)
        }
    }
}
