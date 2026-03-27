package com.newsapp.app.data.remote

import com.google.gson.JsonObject
import retrofit2.http.*

// ESPN Public API - no key needed
interface EspnApiService {

    // News for a sport
    @GET("apis/site/v2/sports/{sport}/{league}/news")
    suspend fun getNews(
        @Path("sport") sport: String,
        @Path("league") league: String,
        @Query("limit") limit: Int = 30
    ): JsonObject

    // Top headlines
    @GET("apis/site/v2/sports/{sport}/{league}/news")
    suspend fun getTopHeadlines(
        @Path("sport") sport: String = "football",
        @Path("league") league: String = "nfl",
        @Query("limit") limit: Int = 30
    ): JsonObject

    // Live scoreboard
    @GET("apis/site/v2/sports/{sport}/{league}/scoreboard")
    suspend fun getScoreboard(
        @Path("sport") sport: String,
        @Path("league") league: String
    ): JsonObject

    // Standings
    @GET("apis/site/v2/sports/{sport}/{league}/standings")
    suspend fun getStandings(
        @Path("sport") sport: String,
        @Path("league") league: String
    ): JsonObject
}

// YouTube RSS/search - no key needed
interface YouTubeRssService {

    @GET("feeds/videos.xml")
    suspend fun getChannelVideos(
        @Query("channel_id") channelId: String
    ): String
}
