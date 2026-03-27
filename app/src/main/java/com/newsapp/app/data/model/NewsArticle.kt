package com.newsapp.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class NewsArticle(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val content: String,
    val imageUrl: String?,
    val sourceUrl: String,
    val source: String,
    val category: SportCategory,
    val publishedAt: Long,
    val isFavorite: Boolean = false,
    val isBookmarked: Boolean = false
)

enum class SportCategory(val displayName: String, val icon: String) {
    ALL("All", "sports"),
    FOOTBALL("Football", "sports_soccer"),
    BASKETBALL("Basketball", "sports_basketball"),
    HOCKEY("Hockey", "sports_hockey"),
    MMA("MMA", "sports_mma"),
    TENNIS("Tennis", "sports_tennis"),
    F1("Formula 1", "sports_motorsports"),
    BOXING("Boxing", "sports_kabaddi"),
    BASEBALL("Baseball", "sports_baseball"),
    ESPORTS("Esports", "sports_esports")
}
