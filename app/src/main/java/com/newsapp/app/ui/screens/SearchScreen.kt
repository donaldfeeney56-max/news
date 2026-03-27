package com.newsapp.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.newsapp.app.SportNewsApp
import com.newsapp.app.ui.components.*
import com.newsapp.app.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onArticleClick: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateQuery(it) },
                        placeholder = { Text("Search sports news...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateQuery("") }) {
                                    Icon(Icons.Default.Clear, "Clear")
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            SportNewsApp.amplitude.track("Search Performed", mapOf("query" to searchQuery))
                            viewModel.addToRecentSearches(searchQuery)
                            viewModel.onlineSearch(searchQuery)
                        }) {
                            Icon(Icons.Default.Search, "Search online")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSearching) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }

            if (searchQuery.length < 2 && recentSearches.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Recent Searches", style = MaterialTheme.typography.titleSmall)
                        TextButton(onClick = { viewModel.clearRecentSearches() }) {
                            Text("Clear")
                        }
                    }
                }
                items(recentSearches) { query ->
                    ListItem(
                        headlineContent = { Text(query) },
                        leadingContent = { Icon(Icons.Default.History, null) },
                        modifier = Modifier.clickable {
                            viewModel.updateQuery(query)
                            viewModel.onlineSearch(query)
                        }
                    )
                }
            }

            if (searchResults.isEmpty() && searchQuery.length >= 2 && !isSearching) {
                item {
                    EmptyState(
                        title = "No results found",
                        subtitle = "Try a different search term or search online"
                    )
                }
            }

            items(searchResults, key = { it.id }) { article ->
                CompactNewsCard(
                    article = article,
                    onClick = {
                        SportNewsApp.amplitude.track("Article Opened", mapOf("article_id" to article.id, "source" to "search"))
                        onArticleClick(article.id)
                    }
                )
            }
        }
    }
}
