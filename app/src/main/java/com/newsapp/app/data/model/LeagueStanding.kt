package com.newsapp.app.data.model

data class LeagueStanding(
    val league: String,
    val category: SportCategory,
    val teams: List<TeamStanding>
)

data class TeamStanding(
    val position: Int,
    val teamName: String,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int,
    val points: Int,
    val logoUrl: String?
)
