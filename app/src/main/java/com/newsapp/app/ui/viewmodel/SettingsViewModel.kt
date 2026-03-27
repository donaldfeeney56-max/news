package com.newsapp.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newsapp.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = settingsRepository.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val apiToken: StateFlow<String> = settingsRepository.apiToken
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val notificationsEnabled: StateFlow<Boolean> = settingsRepository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoRefresh: StateFlow<Boolean> = settingsRepository.autoRefresh
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val refreshInterval: StateFlow<Int> = settingsRepository.refreshInterval
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 15)

    val language: StateFlow<String> = settingsRepository.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDarkTheme(enabled) }
    }

    fun setApiToken(token: String) {
        viewModelScope.launch { settingsRepository.setApiToken(token) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setNotificationsEnabled(enabled) }
    }

    fun setAutoRefresh(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoRefresh(enabled) }
    }

    fun setRefreshInterval(minutes: Int) {
        viewModelScope.launch { settingsRepository.setRefreshInterval(minutes) }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch { settingsRepository.setLanguage(language) }
    }
}
