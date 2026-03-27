package com.newsapp.app.data.model

data class VideoHighlight(
    val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val videoUrl: String,
    val duration: String?,
    val source: String,
    val category: SportCategory,
    val publishedAt: Long
)
