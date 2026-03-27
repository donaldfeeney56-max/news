package com.newsapp.app.data.local

import androidx.room.*
import com.newsapp.app.data.model.NewsArticle
import com.newsapp.app.data.model.SportCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY publishedAt DESC")
    fun getAllArticles(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM articles WHERE category = :category ORDER BY publishedAt DESC")
    fun getArticlesByCategory(category: SportCategory): Flow<List<NewsArticle>>

    @Query("SELECT * FROM articles WHERE isFavorite = 1 ORDER BY publishedAt DESC")
    fun getFavoriteArticles(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY publishedAt DESC")
    fun getBookmarkedArticles(): Flow<List<NewsArticle>>

    @Query("SELECT * FROM articles WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY publishedAt DESC")
    fun searchArticles(query: String): Flow<List<NewsArticle>>

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getArticleById(id: String): NewsArticle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticle>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: NewsArticle)

    @Update
    suspend fun updateArticle(article: NewsArticle)

    @Query("UPDATE articles SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE articles SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun setBookmarked(id: String, isBookmarked: Boolean)

    @Query("DELETE FROM articles WHERE isFavorite = 0 AND isBookmarked = 0 AND publishedAt < :timestamp")
    suspend fun deleteOldArticles(timestamp: Long)
}
