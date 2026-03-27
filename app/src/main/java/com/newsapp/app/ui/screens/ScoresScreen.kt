package com.newsapp.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.newsapp.app.SportNewsApp
import com.newsapp.app.ui.components.*
import com.newsapp.app.ui.viewmodel.ScoreFilter
import com.newsapp.app.ui.viewmodel.ScoresViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoresScreen(
    viewModel: ScoresViewModel = hiltViewModel()
) {
    val filteredScores by viewModel.filteredScores.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.startAutoRefresh()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Live Scores") },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isLoading),
            onRefresh = {
                SportNewsApp.amplitude.track("Scores Pull to Refresh")
                viewModel.fetchScores()
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
                    // Filter tabs
                    item {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            ScoreFilter.entries.forEachIndexed { index, filter ->
                                SegmentedButton(
                                    selected = filter == selectedFilter,
                                    onClick = {
                                        SportNewsApp.amplitude.track("Score Filter Changed", mapOf("filter" to filter.displayName))
                                        viewModel.setFilter(filter)
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = ScoreFilter.entries.size)
                                ) {
                                    Text(filter.displayName)
                                }
                            }
                        }
                    }

                    if (error != null) {
                        item {
                            ErrorMessage(
                                message = error ?: "Failed to load scores",
                                onRetry = { viewModel.fetchScores() }
                            )
                        }
                    }

                    if (filteredScores.isEmpty() && !isLoading) {
                        item {
                            EmptyState(
                                title = "No scores available",
                                subtitle = "Pull to refresh or check settings"
                            )
                        }
                    }

                    items(filteredScores, key = { it.id }) { score ->
                        ScoreCard(score = score)
                    }
                }
            }
        }
    }

