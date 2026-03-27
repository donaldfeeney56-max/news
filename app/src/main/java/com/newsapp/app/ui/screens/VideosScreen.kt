package com.newsapp.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.newsapp.app.data.model.SportCategory
import com.newsapp.app.SportNewsApp
import com.newsapp.app.ui.components.*
import com.newsapp.app.ui.viewmodel.VideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(
    viewModel: VideoViewModel = hiltViewModel()
) {
    val videos by viewModel.videos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Highlights") },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isLoading),
            onRefresh = {
                SportNewsApp.amplitude.track("Videos Pull to Refresh")
                viewModel.fetchVideos(selectedCategory)
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
                    item {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val categories = listOf(
                                SportCategory.ALL, SportCategory.FOOTBALL,
                                SportCategory.BASKETBALL, SportCategory.MMA,
                                SportCategory.F1, SportCategory.TENNIS
                            )
                            categories.forEach { category ->
                                FilterChip(
                                    selected = category == selectedCategory,
                                    onClick = {
                                        SportNewsApp.amplitude.track("Video Category Selected", mapOf("category" to category.displayName))
                                        viewModel.fetchVideos(category)
                                    },
                                    label = { Text(category.displayName) }
                                )
                            }
                        }
                    }

                    if (error != null) {
                        item {
                            ErrorMessage(
                                message = error ?: "Failed to load videos",
                                onRetry = { viewModel.fetchVideos(selectedCategory) }
                            )
                        }
                    }

                    if (videos.isEmpty() && !isLoading) {
                        item {
                            EmptyState(
                                title = "No highlights yet",
                                subtitle = "Select a category and pull to refresh"
                            )
                        }
                    }

                    items(videos, key = { it.id }) { video ->
                        VideoCard(
                            video = video,
                            onClick = {
                                SportNewsApp.amplitude.track("Video Played", mapOf("video_id" to video.id))
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }

