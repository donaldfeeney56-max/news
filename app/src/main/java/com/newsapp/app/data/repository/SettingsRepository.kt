package com.newsapp.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val API_TOKEN = stringPreferencesKey("api_token")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val FAVORITE_CATEGORIES = stringSetPreferencesKey("favorite_categories")
        val AUTO_REFRESH = booleanPreferencesKey("auto_refresh")
        val REFRESH_INTERVAL = intPreferencesKey("refresh_interval_minutes")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { it[Keys.DARK_THEME] ?: false }
    val apiToken: Flow<String> = context.dataStore.data.map { it[Keys.API_TOKEN] ?: "" }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }
    val favoriteCategories: Flow<Set<String>> = context.dataStore.data.map { it[Keys.FAVORITE_CATEGORIES] ?: emptySet() }
    val autoRefresh: Flow<Boolean> = context.dataStore.data.map { it[Keys.AUTO_REFRESH] ?: true }
    val refreshInterval: Flow<Int> = context.dataStore.data.map { it[Keys.REFRESH_INTERVAL] ?: 15 }
    val language: Flow<String> = context.dataStore.data.map { it[Keys.LANGUAGE] ?: "en" }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setApiToken(token: String) {
        context.dataStore.edit { it[Keys.API_TOKEN] = token }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setFavoriteCategories(categories: Set<String>) {
        context.dataStore.edit { it[Keys.FAVORITE_CATEGORIES] = categories }
    }

    suspend fun setAutoRefresh(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_REFRESH] = enabled }
    }

    suspend fun setRefreshInterval(minutes: Int) {
        context.dataStore.edit { it[Keys.REFRESH_INTERVAL] = minutes }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }
}
