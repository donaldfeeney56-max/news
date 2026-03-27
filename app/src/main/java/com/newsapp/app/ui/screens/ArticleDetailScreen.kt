package com.newsapp.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.newsapp.app.SportNewsApp
import com.newsapp.app.ui.components.LoadingIndicator
import com.newsapp.app.ui.viewmodel.ArticleDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    viewModel: ArticleDetailViewModel = hiltViewModel()
) {
    val article by viewModel.article.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    article?.let { art ->
                        IconButton(onClick = {
                            SportNewsApp.amplitude.track("Article Favorite Toggled", mapOf("article_id" to art.id))
                            viewModel.toggleFavorite()
                        }) {
                            Icon(
                                if (art.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                "Favorite",
                                tint = if (art.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = {
                            SportNewsApp.amplitude.track("Article Bookmark Toggled", mapOf("article_id" to art.id))
                            viewModel.toggleBookmark()
                        }) {
                            Icon(
                                if (art.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                "Bookmark"
                            )
                        }
                        IconButton(onClick = {
                            SportNewsApp.amplitude.track("Article Shared", mapOf("article_id" to art.id))
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "${art.title}\n${art.sourceUrl}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share article"))
                        }) {
                            Icon(Icons.Default.Share, "Share")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            LoadingIndicator(modifier = Modifier.padding(paddingValues))
        } else {
            article?.let { art ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (!art.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = art.imageUrl,
                            contentDescription = art.title,
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SuggestionChip(onClick = {}, label = { Text(art.category.displayName) })
                            Text(text = art.source, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = art.title, style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (art.description.isNotBlank()) {
                            Text(text = art.description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = art.content, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(24.dp))
                        if (art.sourceUrl.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    SportNewsApp.amplitude.track("Read Full Article", mapOf("article_id" to art.id, "source_url" to art.sourceUrl))
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(art.sourceUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Launch, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Read full article")
                            }
                        }
                    }
                }
            }
        }
    }
}
