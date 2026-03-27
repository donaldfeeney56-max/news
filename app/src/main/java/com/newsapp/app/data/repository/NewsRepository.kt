package com.newsapp.app.data.repository

import com.google.gson.JsonObject
import com.newsapp.app.data.local.ArticleDao
import com.newsapp.app.data.model.*
import com.newsapp.app.data.remote.EspnApiService
import com.newsapp.app.data.remote.SportsConfig
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val espnApi: EspnApiService,
    private val articleDao: ArticleDao
) {
    fun getAllArticles(): Flow<List<NewsArticle>> = articleDao.getAllArticles()

    fun getArticlesByCategory(category: SportCategory): Flow<List<NewsArticle>> =
        articleDao.getArticlesByCategory(category)

    fun getFavorites(): Flow<List<NewsArticle>> = articleDao.getFavoriteArticles()

    fun getBookmarks(): Flow<List<NewsArticle>> = articleDao.getBookmarkedArticles()

    fun searchArticles(query: String): Flow<List<NewsArticle>> = articleDao.searchArticles(query)

    suspend fun getArticleById(id: String): NewsArticle? = articleDao.getArticleById(id)

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) = articleDao.setFavorite(id, isFavorite)

    suspend fun toggleBookmark(id: String, isBookmarked: Boolean) = articleDao.setBookmarked(id, isBookmarked)

    suspend fun fetchNews(category: SportCategory = SportCategory.ALL): Result<List<NewsArticle>> {
        return try {
            val articles = mutableListOf<NewsArticle>()

            if (category == SportCategory.ALL) {
                // Fetch from multiple sports
                val sports = listOf(
                    SportCategory.FOOTBALL,
                    SportCategory.BASKETBALL,
                    SportCategory.HOCKEY,
                    SportCategory.MMA,
                    SportCategory.BASEBALL
                )
                for (sport in sports) {
                    try {
                        val league = SportsConfig.getLeague(sport)
                        val response = espnApi.getNews(league.sport, league.league, 10)
                        articles.addAll(parseEspnNews(response, sport))
                    } catch (_: Exception) { }
                }
            } else {
                val league = SportsConfig.getLeague(category)
                val response = espnApi.getNews(league.sport, league.league, 30)
                articles.addAll(parseEspnNews(response, category))
            }

            articles.sortByDescending { it.publishedAt }
            if (articles.isNotEmpty()) {
                articleDao.insertArticles(articles)
            }
            Result.success(articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchLiveScores(category: SportCategory = SportCategory.FOOTBALL): Result<List<LiveScore>> {
        return try {
            val scores = mutableListOf<LiveScore>()
            val categories = if (category == SportCategory.ALL) {
                listOf(SportCategory.FOOTBALL, SportCategory.BASKETBALL, SportCategory.HOCKEY, SportCategory.BASEBALL)
            } else {
                listOf(category)
            }

            for (cat in categories) {
                try {
                    val league = SportsConfig.getLeague(cat)
                    val response = espnApi.getScoreboard(league.sport, league.league)
                    scores.addAll(parseEspnScoreboard(response, cat))
                } catch (_: Exception) { }
            }
            Result.success(scores)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchVideoHighlights(query: String = "sports highlights"): Result<List<VideoHighlight>> {
        return try {
            // Use ESPN news images as "highlights" cards with links
            val league = SportsConfig.getLeague(SportCategory.ALL)
            val response = espnApi.getNews(league.sport, league.league, 20)
            val videos = parseEspnAsHighlights(response)
            Result.success(videos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cleanupOldArticles() {
        val oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        articleDao.deleteOldArticles(oneWeekAgo)
    }

    private fun parseEspnNews(json: JsonObject, category: SportCategory): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()
        val articlesArray = json.getAsJsonArray("articles") ?: return articles

        for (element in articlesArray) {
            try {
                val article = element.asJsonObject
                val headline = article.get("headline")?.asString ?: continue
                val description = article.get("description")?.asString ?: ""

                // Get image
                var imageUrl: String? = null
                val images = article.getAsJsonArray("images")
                if (images != null && images.size() > 0) {
                    imageUrl = images[0].asJsonObject.get("url")?.asString
                }

                // Get link
                var sourceUrl = ""
                val links = article.getAsJsonObject("links")
                if (links != null) {
                    val web = links.getAsJsonObject("web")
                    if (web != null) {
                        sourceUrl = web.get("href")?.asString ?: ""
                    }
                }

                // Get published date
                val published = article.get("published")?.asString ?: ""
                val publishedAt = try {
                    java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                        .parse(published)?.time ?: System.currentTimeMillis()
                } catch (_: Exception) {
                    System.currentTimeMillis()
                }

                articles.add(
                    NewsArticle(
                        id = sourceUrl.hashCode().toString().ifBlank { UUID.randomUUID().toString() },
                        title = headline,
                        description = description,
                        content = description,
                        imageUrl = imageUrl,
                        sourceUrl = sourceUrl,
                        source = "ESPN",
                        category = category,
                        publishedAt = publishedAt
                    )
                )
            } catch (_: Exception) { }
        }
        return articles
    }

    private fun parseEspnScoreboard(json: JsonObject, category: SportCategory): List<LiveScore> {
        val scores = mutableListOf<LiveScore>()
        val events = json.getAsJsonArray("events") ?: return scores

        for (element in events) {
            try {
                val event = element.asJsonObject
                val competitions = event.getAsJsonArray("competitions") ?: continue
                if (competitions.size() == 0) continue

                val competition = competitions[0].asJsonObject
                val competitors = competition.getAsJsonArray("competitors") ?: continue
                if (competitors.size() < 2) continue

                val home = competitors.firstOrNull {
                    it.asJsonObject.get("homeAway")?.asString == "home"
                }?.asJsonObject ?: competitors[0].asJsonObject

                val away = competitors.firstOrNull {
                    it.asJsonObject.get("homeAway")?.asString == "away"
                }?.asJsonObject ?: competitors[1].asJsonObject

                val homeTeamObj = home.getAsJsonObject("team")
                val awayTeamObj = away.getAsJsonObject("team")

                val statusObj = competition.getAsJsonObject("status")
                    ?: event.getAsJsonObject("status")
                val statusType = statusObj?.getAsJsonObject("type")
                val statusState = statusType?.get("state")?.asString ?: "pre"
                val statusDetail = statusType?.get("shortDetail")?.asString
                    ?: statusType?.get("detail")?.asString ?: ""

                val matchStatus = when (statusState) {
                    "in" -> MatchStatus.LIVE
                    "post" -> MatchStatus.FINISHED
                    "pre" -> MatchStatus.UPCOMING
                    else -> MatchStatus.UPCOMING
                }

                val league = SportsConfig.getLeague(category)

                scores.add(
                    LiveScore(
                        id = event.get("id")?.asString ?: UUID.randomUUID().toString(),
                        homeTeam = homeTeamObj?.get("displayName")?.asString
                            ?: homeTeamObj?.get("name")?.asString ?: "Home",
                        awayTeam = awayTeamObj?.get("displayName")?.asString
                            ?: awayTeamObj?.get("name")?.asString ?: "Away",
                        homeScore = home.get("score")?.asString?.toIntOrNull() ?: 0,
                        awayScore = away.get("score")?.asString?.toIntOrNull() ?: 0,
                        status = matchStatus,
                        minute = if (matchStatus == MatchStatus.LIVE) statusDetail else null,
                        league = league.displayName,
                        category = category,
                        startTime = System.currentTimeMillis(),
                        homeLogoUrl = homeTeamObj?.get("logo")?.asString,
                        awayLogoUrl = awayTeamObj?.get("logo")?.asString
                    )
                )
            } catch (_: Exception) { }
        }
        return scores
    }

    private fun parseEspnAsHighlights(json: JsonObject): List<VideoHighlight> {
        val highlights = mutableListOf<VideoHighlight>()
        val articles = json.getAsJsonArray("articles") ?: return highlights

        for (element in articles) {
            try {
                val article = element.asJsonObject
                val headline = article.get("headline")?.asString ?: continue

                var imageUrl: String? = null
                val images = article.getAsJsonArray("images")
                if (images != null && images.size() > 0) {
                    imageUrl = images[0].asJsonObject.get("url")?.asString
                }

                var videoUrl = ""
                val links = article.getAsJsonObject("links")
                if (links != null) {
                    val web = links.getAsJsonObject("web")
                    if (web != null) {
                        videoUrl = web.get("href")?.asString ?: ""
                    }
                }

                if (imageUrl != null) {
                    highlights.add(
                        VideoHighlight(
                            id = videoUrl.hashCode().toString().ifBlank { UUID.randomUUID().toString() },
                            title = headline,
                            thumbnailUrl = imageUrl,
                            videoUrl = videoUrl,
                            duration = null,
                            source = "ESPN",
                            category = SportCategory.ALL,
                            publishedAt = System.currentTimeMillis()
                        )
                    )
                }
            } catch (_: Exception) { }
        }
        return highlights
    }
}
