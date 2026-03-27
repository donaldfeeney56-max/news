package com.newsapp.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.newsapp.app.ui.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Scores : Screen("scores")
    object Videos : Screen("videos")
    object Favorites : Screen("favorites")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object Feedback : Screen("feedback")
    object ArticleDetail : Screen("article/{articleId}") {
        fun createRoute(articleId: String) = "article/$articleId"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onArticleClick = { articleId ->
                    navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                },
                onSearchClick = { navController.navigate(Screen.Search.route) }
            )
        }
        composable(Screen.Scores.route) {
            ScoresScreen()
        }
        composable(Screen.Videos.route) {
            VideosScreen()
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onArticleClick = { articleId ->
                    navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onArticleClick = { articleId ->
                    navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onFeedbackClick = { navController.navigate(Screen.Feedback.route) }
            )
        }
        composable(Screen.Feedback.route) {
            FeedbackScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.ArticleDetail.route,
            arguments = listOf(navArgument("articleId") { type = NavType.StringType })
        ) {
            ArticleDetailScreen(
                onBackClick = { navController.popBackStack() },
                onShareClick = { /* handled inside */ }
            )
        }
    }
}
