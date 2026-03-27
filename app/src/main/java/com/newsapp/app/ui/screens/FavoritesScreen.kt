package com.newsapp.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.newsapp.app.SportNewsApp
import com.newsapp.app.ui.components.*
import com.newsapp.app.ui.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onArticleClick: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Saved") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        SportNewsApp.amplitude.track("Favorites Tab Selected", mapOf("tab" to "favorites"))
                        viewModel.selectTab(0)
                    },
                    text = { Text("Favorites (${favorites.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        SportNewsApp.amplitude.track("Favorites Tab Selected", mapOf("tab" to "bookmarks"))
                        viewModel.selectTab(1)
                    },
                    text = { Text("Bookmarks (${bookmarks.size})") }
                )
            }

            val items = if (selectedTab == 0) favorites else bookmarks

            if (items.isEmpty()) {
                EmptyState(
                    title = if (selectedTab == 0) "No favorites yet" else "No bookmarks yet",
                    subtitle = "Save articles to see them here"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { article ->
                        SwipeToDismissBox(
                            state = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value != SwipeToDismissBoxValue.Settled) {
                                        if (selectedTab == 0) {
                                            SportNewsApp.amplitude.track("Favorite Removed", mapOf("article_id" to article.id))
                                            viewModel.removeFavorite(article.id)
                                        } else {
                                            SportNewsApp.amplitude.track("Bookmark Removed", mapOf("article_id" to article.id))
                                            viewModel.removeBookmark(article.id)
                                        }
                                        true
                                    } else false
                                }
                            ),
                            backgroundContent = {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {}
                            }
                        ) {
                            CompactNewsCard(
                                article = article,
                                onClick = {
                                    SportNewsApp.amplitude.track("Article Opened", mapOf("article_id" to article.id, "source" to "favorites"))
                                    onArticleClick(article.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
