package io.designtoswiftui.countdown2binge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.designtoswiftui.countdown2binge.ui.bingeready.BingeReadyScreen
import io.designtoswiftui.countdown2binge.ui.search.SearchScreen
import io.designtoswiftui.countdown2binge.ui.showdetail.EpisodeListScreen
import io.designtoswiftui.countdown2binge.ui.showdetail.ShowDetailScreen
import io.designtoswiftui.countdown2binge.ui.timeline.TimelineScreen

/**
 * Navigation routes for the app.
 */
sealed class Screen(val route: String) {
    data object Timeline : Screen("timeline")
    data object BingeReady : Screen("binge_ready")
    data object Search : Screen("search")
    data object ShowDetail : Screen("show_detail/{showId}") {
        fun createRoute(showId: Long): String = "show_detail/$showId"
    }
    data object EpisodeList : Screen("episode_list/{seasonId}") {
        fun createRoute(seasonId: Long): String = "episode_list/$seasonId"
    }
}

/**
 * Bottom navigation destinations.
 */
enum class BottomNavDestination(
    val screen: Screen,
    val label: String,
    val icon: String
) {
    Timeline(Screen.Timeline, "Timeline", "timeline"),
    BingeReady(Screen.BingeReady, "Binge Ready", "binge_ready"),
    Search(Screen.Search, "Search", "search")
}

/**
 * Main navigation graph for the app.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Timeline.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Timeline screen
        composable(Screen.Timeline.route) {
            TimelineScreen(
                onShowClick = { showId ->
                    navController.navigate(Screen.ShowDetail.createRoute(showId))
                }
            )
        }

        // Binge Ready screen
        composable(Screen.BingeReady.route) {
            BingeReadyScreen(
                onSeasonClick = { showId ->
                    navController.navigate(Screen.ShowDetail.createRoute(showId))
                }
            )
        }

        // Search screen
        composable(Screen.Search.route) {
            SearchScreen(
                onShowClick = { localShowId ->
                    // SearchScreen now provides the local database ID
                    navController.navigate(Screen.ShowDetail.createRoute(localShowId))
                }
            )
        }

        // Show Detail screen
        composable(
            route = Screen.ShowDetail.route,
            arguments = listOf(
                navArgument("showId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val showId = backStackEntry.arguments?.getLong("showId") ?: 0L
            ShowDetailScreen(
                showId = showId,
                onBackClick = {
                    navController.popBackStack()
                },
                onSeasonClick = { seasonId ->
                    navController.navigate(Screen.EpisodeList.createRoute(seasonId))
                }
            )
        }

        // Episode List screen
        composable(
            route = Screen.EpisodeList.route,
            arguments = listOf(
                navArgument("seasonId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val seasonId = backStackEntry.arguments?.getLong("seasonId") ?: 0L
            EpisodeListScreen(
                seasonId = seasonId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
