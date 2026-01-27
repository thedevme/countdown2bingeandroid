package io.designtoswiftui.countdown2binge.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.designtoswiftui.countdown2binge.models.ShowCategory
import io.designtoswiftui.countdown2binge.ui.bingeready.BingeReadyScreen
import io.designtoswiftui.countdown2binge.ui.followedshowdetail.FollowedShowDetailScreen
import io.designtoswiftui.countdown2binge.ui.notifications.EditShowNotificationsScreen
import io.designtoswiftui.countdown2binge.ui.notifications.NotificationsHubScreen
import io.designtoswiftui.countdown2binge.ui.onboarding.OnboardingScreen
import io.designtoswiftui.countdown2binge.ui.premium.PaywallScreen
import io.designtoswiftui.countdown2binge.ui.search.GenreListScreen
import io.designtoswiftui.countdown2binge.ui.search.SearchScreen
import io.designtoswiftui.countdown2binge.ui.settings.AllShowsScreen
import io.designtoswiftui.countdown2binge.ui.settings.SettingsScreen
import io.designtoswiftui.countdown2binge.ui.showdetail.EpisodeListScreen
import io.designtoswiftui.countdown2binge.ui.showdetail.ShowDetailScreen
import io.designtoswiftui.countdown2binge.ui.timeline.FullTimelineScreen
import io.designtoswiftui.countdown2binge.ui.timeline.TimelineScreen
import io.designtoswiftui.countdown2binge.ui.youtube.YouTubePlayerScreen
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Navigation routes for the app.
 */
sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Timeline : Screen("timeline")
    data object FullTimeline : Screen("full_timeline")
    data object BingeReady : Screen("binge_ready")
    data object Search : Screen("search")
    data object Settings : Screen("settings")
    data object ShowDetail : Screen("show_detail/{showId}") {
        fun createRoute(showId: Long): String = "show_detail/$showId"
    }
    data object ShowDetailByTmdb : Screen("show_detail_tmdb/{tmdbId}") {
        fun createRoute(tmdbId: Int): String = "show_detail_tmdb/$tmdbId"
    }
    data object FollowedShowDetail : Screen("followed_show_detail/{showId}") {
        fun createRoute(showId: Long): String = "followed_show_detail/$showId"
    }
    data object EpisodeList : Screen("episode_list/{seasonId}") {
        fun createRoute(seasonId: Long): String = "episode_list/$seasonId"
    }
    data object GenreList : Screen("genre_list/{categoryName}") {
        fun createRoute(category: ShowCategory): String = "genre_list/${category.name}"
    }
    data object YouTubePlayer : Screen("youtube_player/{videoKey}/{videoTitle}") {
        fun createRoute(videoKey: String, videoTitle: String): String {
            val encodedTitle = URLEncoder.encode(videoTitle, "UTF-8")
            return "youtube_player/$videoKey/$encodedTitle"
        }
    }
    data object NotificationsHub : Screen("notifications_hub")
    data object EditShowNotifications : Screen("edit_show_notifications/{showId}") {
        fun createRoute(showId: Long): String = "edit_show_notifications/$showId"
    }
    data object Paywall : Screen("paywall")
    data object AllShows : Screen("all_shows")
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
        // Onboarding screen
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Screen.Timeline.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onShowClick = { tmdbId ->
                    navController.navigate(Screen.ShowDetailByTmdb.createRoute(tmdbId))
                }
            )
        }

        // Timeline screen
        composable(Screen.Timeline.route) {
            TimelineScreen(
                onShowClick = { showId ->
                    navController.navigate(Screen.FollowedShowDetail.createRoute(showId))
                },
                onViewFullTimelineClick = {
                    navController.navigate(Screen.FullTimeline.route)
                }
            )
        }

        // Full Timeline screen
        composable(Screen.FullTimeline.route) {
            FullTimelineScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onShowClick = { showId ->
                    navController.navigate(Screen.FollowedShowDetail.createRoute(showId))
                }
            )
        }

        // Binge Ready screen
        composable(Screen.BingeReady.route) {
            BingeReadyScreen(
                onSeasonClick = { showId ->
                    navController.navigate(Screen.FollowedShowDetail.createRoute(showId))
                }
            )
        }

        // Search screen
        composable(Screen.Search.route) {
            SearchScreen(
                onShowClick = { tmdbId ->
                    // Navigate to show detail using TMDB ID
                    navController.navigate(Screen.ShowDetailByTmdb.createRoute(tmdbId))
                },
                onCategoryClick = { category ->
                    navController.navigate(Screen.GenreList.createRoute(category))
                }
            )
        }

        // Settings screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNotificationsClick = {
                    navController.navigate(Screen.NotificationsHub.route)
                },
                onUpgradeClick = {
                    navController.navigate(Screen.Paywall.route)
                },
                onAllShowsClick = {
                    navController.navigate(Screen.AllShows.route)
                }
            )
        }

        // Notifications Hub screen
        composable(Screen.NotificationsHub.route) {
            NotificationsHubScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onShowClick = { showId ->
                    navController.navigate(Screen.EditShowNotifications.createRoute(showId))
                },
                onUnlockPremiumClick = {
                    navController.navigate(Screen.Paywall.route)
                }
            )
        }

        // Edit Show Notifications screen
        composable(
            route = Screen.EditShowNotifications.route,
            arguments = listOf(
                navArgument("showId") {
                    type = NavType.LongType
                }
            )
        ) {
            EditShowNotificationsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Show Detail screen (by local showId)
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
                },
                onShowClick = { tmdbId ->
                    navController.navigate(Screen.ShowDetailByTmdb.createRoute(tmdbId))
                },
                onVideoClick = { videoKey, videoTitle ->
                    navController.navigate(Screen.YouTubePlayer.createRoute(videoKey, videoTitle))
                }
            )
        }

        // Show Detail screen (by TMDB ID - for non-followed shows)
        composable(
            route = Screen.ShowDetailByTmdb.route,
            arguments = listOf(
                navArgument("tmdbId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val tmdbId = backStackEntry.arguments?.getInt("tmdbId") ?: 0
            ShowDetailScreen(
                showId = 0L, // Will use tmdbId from SavedStateHandle
                onBackClick = {
                    navController.popBackStack()
                },
                onSeasonClick = { seasonId ->
                    navController.navigate(Screen.EpisodeList.createRoute(seasonId))
                },
                onShowClick = { showTmdbId ->
                    navController.navigate(Screen.ShowDetailByTmdb.createRoute(showTmdbId))
                },
                onVideoClick = { videoKey, videoTitle ->
                    navController.navigate(Screen.YouTubePlayer.createRoute(videoKey, videoTitle))
                }
            )
        }

        // Followed Show Detail screen (tabbed interface for followed shows)
        composable(
            route = Screen.FollowedShowDetail.route,
            arguments = listOf(
                navArgument("showId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val showId = backStackEntry.arguments?.getLong("showId") ?: 0L
            FollowedShowDetailScreen(
                showId = showId,
                onBackClick = {
                    navController.popBackStack()
                },
                onShowClick = { tmdbId ->
                    // Navigate to another followed show detail (self-referential for spinoffs)
                    navController.navigate(Screen.ShowDetailByTmdb.createRoute(tmdbId))
                },
                onVideoClick = { videoKey, videoTitle ->
                    navController.navigate(Screen.YouTubePlayer.createRoute(videoKey, videoTitle))
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

        // Genre List screen
        composable(
            route = Screen.GenreList.route,
            arguments = listOf(
                navArgument("categoryName") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            val category = try {
                ShowCategory.valueOf(categoryName)
            } catch (e: IllegalArgumentException) {
                ShowCategory.ACTION // Fallback
            }
            GenreListScreen(
                category = category,
                onBackClick = {
                    navController.popBackStack()
                },
                onShowClick = { tmdbId ->
                    navController.navigate(Screen.ShowDetailByTmdb.createRoute(tmdbId))
                }
            )
        }

        // YouTube Player screen (fullscreen landscape)
        composable(
            route = Screen.YouTubePlayer.route,
            arguments = listOf(
                navArgument("videoKey") {
                    type = NavType.StringType
                },
                navArgument("videoTitle") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val videoKey = backStackEntry.arguments?.getString("videoKey") ?: ""
            val videoTitle = backStackEntry.arguments?.getString("videoTitle")?.let {
                URLDecoder.decode(it, "UTF-8")
            } ?: ""
            YouTubePlayerScreen(
                videoKey = videoKey,
                videoTitle = videoTitle,
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }

        // Paywall screen
        composable(Screen.Paywall.route) {
            PaywallScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPurchaseComplete = {
                    // Purchase completed, screen will auto-dismiss
                }
            )
        }

        // All Shows screen
        composable(Screen.AllShows.route) {
            AllShowsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onShowClick = { showId ->
                    navController.navigate(Screen.FollowedShowDetail.createRoute(showId))
                }
            )
        }
    }
}
