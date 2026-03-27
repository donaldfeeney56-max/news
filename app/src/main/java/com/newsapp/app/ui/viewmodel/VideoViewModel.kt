package com.newsapp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newsapp.app.data.model.SportCategory
import com.newsapp.app.data.model.VideoHighlight
import com.newsapp.app.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {

    private val _videos = MutableStateFlow<List<VideoHighlight>>(emptyList())
    val videos: StateFlow<List<VideoHighlight>> = _videos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedCategory = MutableStateFlow(SportCategory.ALL)
    val selectedCategory: StateFlow<SportCategory> = _selectedCategory.asStateFlow()

    init {
        fetchVideos()
    }

    fun fetchVideos(category: SportCategory = SportCategory.ALL) {
        _selectedCategory.value = category
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val query = if (category == SportCategory.ALL) "sports highlights today"
                else "${category.displayName} highlights today"
            newsRepository.fetchVideoHighlights(query)
                .onSuccess { _videos.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }
}
