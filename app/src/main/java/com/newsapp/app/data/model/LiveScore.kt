package com.newsapp.app.data.model

data class LiveScore(
    val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val status: MatchStatus,
    val minute: String?,
    val league: String,
    val category: SportCategory,
    val startTime: Long,
    val homeLogoUrl: String?,
    val awayLogoUrl: String?
)

enum class MatchStatus(val displayName: String) {
    LIVE("Live"),
    FINISHED("Finished"),
    UPCOMING("Upcoming"),
    HALFTIME("Half Time"),
    POSTPONED("Postponed")
}
