package com.newsapp.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.newsapp.app.data.model.SportCategory
import com.newsapp.app.SportNewsApp
import com.newsapp.app.ui.components.*
import com.newsapp.app.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onArticleClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val articles by viewModel.articles.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("FonSport News") },
                actions = {
                    IconButton(onClick = {
                        SportNewsApp.amplitude.track("Search Opened")
                        onSearchClick()
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                SportNewsApp.amplitude.track("Home Pull to Refresh")
                viewModel.refreshNews()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category chips
                    item {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SportCategory.entries.forEach { category ->
                                FilterChip(
                                    selected = category == selectedCategory,
                                    onClick = {
                                        SportNewsApp.amplitude.track("Category Selected", mapOf("category" to category.displayName))
                                        viewModel.selectCategory(category)
                                    },
                                    label = { Text(category.displayName) }
                                )
                            }
                        }
                    }

                    // Error
                    if (error != null) {
                        item {
                            ErrorMessage(
                                message = error ?: "Unknown error",
                                onRetry = { viewModel.refreshNews() }
                            )
                        }
                    }

                    // Articles
                    if (articles.isEmpty() && !isRefreshing) {
                        item {
                            EmptyState(
                                title = "No news yet",
                                subtitle = "Configure your API token in Settings to load news"
                            )
                        }
                    }

                    // Featured article (first)
                    if (articles.isNotEmpty()) {
                        item {
                            NewsCard(
                                article = articles.first(),
                                onClick = {
                                    SportNewsApp.amplitude.track("Article Opened", mapOf("article_id" to articles.first().id, "source" to "featured"))
                                    onArticleClick(articles.first().id)
                                },
                                onFavoriteClick = {
                                    SportNewsApp.amplitude.track("Favorite Toggled", mapOf("article_id" to articles.first().id))
                                    viewModel.toggleFavorite(articles.first().id, !articles.first().isFavorite)
                                },
                                onBookmarkClick = {
                                    SportNewsApp.amplitude.track("Bookmark Toggled", mapOf("article_id" to articles.first().id))
                                    viewModel.toggleBookmark(articles.first().id, !articles.first().isBookmarked)
                                }
                            )
                        }
                    }

                    // Rest of articles
                    items(articles.drop(1), key = { it.id }) { article ->
                        CompactNewsCard(
                            article = article,
                            onClick = {
                                SportNewsApp.amplitude.track("Article Opened", mapOf("article_id" to article.id, "source" to "feed"))
                                onArticleClick(article.id)
                            }
                        )
                    }
                }
            }
        }
    }

