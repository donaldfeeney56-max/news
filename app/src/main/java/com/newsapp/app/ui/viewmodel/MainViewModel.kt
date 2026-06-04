package com.newsapp.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newsapp.app.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    sealed class AppState {
        object Loading : AppState()
        data class WebView(val url: String) : AppState()
        object NormalApp : AppState()
    }

    private val _appState = MutableStateFlow<AppState>(AppState.Loading)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    init {
        checkUrl()
    }

    private fun checkUrl() {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("webview_prefs", Context.MODE_PRIVATE)
            val webViewPrefs = context.getSharedPreferences("webview_cache", Context.MODE_PRIVATE)
            val cachedUrl = prefs.getString("cached_url", null)
            val cachedFinalUrl = webViewPrefs.getString("cached_final_url", null)
            val fallbackUrl = cachedFinalUrl.takeIf { !it.isNullOrBlank() }
                ?: cachedUrl.takeIf { !it.isNullOrBlank() }

            try {
                val url = withTimeoutOrNull(10_000L) {
                    firestoreRepository.getWebViewUrl()
                }
                when {
                    !url.isNullOrBlank() -> {
                        prefs.edit().putString("cached_url", url).apply()
                        _appState.value = AppState.WebView(url)
                    }
                    else -> {
                        if (!fallbackUrl.isNullOrBlank()) {
                            _appState.value = AppState.WebView(fallbackUrl)
                        } else {
                            _appState.value = AppState.NormalApp
                        }
                    }
                }
            } catch (_: Exception) {
                if (!fallbackUrl.isNullOrBlank()) {
                    _appState.value = AppState.WebView(fallbackUrl)
                } else {
                    _appState.value = AppState.NormalApp
                }
            }
        }
    }
}
