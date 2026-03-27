package com.newsapp.app.data.remote

import com.newsapp.app.data.model.SportCategory

object SportsConfig {

    data class SportLeague(
        val sport: String,
        val league: String,
        val displayName: String
    )

    val categoryMapping = mapOf(
        SportCategory.ALL to SportLeague("football", "nfl", "NFL"),
        SportCategory.FOOTBALL to SportLeague("soccer", "eng.1", "Premier League"),
        SportCategory.BASKETBALL to SportLeague("basketball", "nba", "NBA"),
        SportCategory.HOCKEY to SportLeague("hockey", "nhl", "NHL"),
        SportCategory.MMA to SportLeague("mma", "ufc", "UFC"),
        SportCategory.TENNIS to SportLeague("tennis", "atp", "ATP"),
        SportCategory.F1 to SportLeague("racing", "f1", "Formula 1"),
        SportCategory.BOXING to SportLeague("boxing", "pbc", "Boxing"),
        SportCategory.BASEBALL to SportLeague("baseball", "mlb", "MLB"),
        SportCategory.ESPORTS to SportLeague("football", "nfl", "NFL") // fallback
    )

    // Sports YouTube channels for highlights
    val highlightChannels = mapOf(
        SportCategory.ALL to "UCYKjxLw3MRH5UkspNOGJMxA",       // ESPN
        SportCategory.FOOTBALL to "UCGHiisIx6MjP_sRbaTDYNBw",  // Premier League
        SportCategory.BASKETBALL to "UCWJ2lWNubArHWmf3FIHbfcQ", // NBA
        SportCategory.MMA to "UCvgfXK4nTYKudb0rFR6noLA",        // UFC
        SportCategory.F1 to "UCB_qr75-ydFVKSk9TiYNIjg"         // F1
    )

    fun getLeague(category: SportCategory): SportLeague {
        return categoryMapping[category] ?: categoryMapping[SportCategory.ALL]!!
    }
}
