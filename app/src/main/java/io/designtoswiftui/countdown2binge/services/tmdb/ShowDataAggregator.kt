package io.designtoswiftui.countdown2binge.services.tmdb

import android.util.Log
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ShowDataAggregator"

/**
 * Aggregates data from multiple TMDB API calls into a complete show dataset.
 */
@Singleton
class ShowDataAggregator @Inject constructor(
    private val tmdbService: TMDBService
) {

    /**
     * Fetches complete show data including the latest season details.
     * Skips season 0 (specials) and fetches the most recent regular season.
     */
    suspend fun fetchFullShowData(showId: Int): Result<FullShowData> {
        // First, get show details
        val showResult = tmdbService.getShowDetails(showId)
        if (showResult.isFailure) {
            return Result.failure(showResult.exceptionOrNull()!!)
        }

        val showDetails = showResult.getOrThrow()

        // Find the latest regular season (skip season 0 which is specials)
        val latestSeasonNumber = showDetails.seasons
            ?.filter { it.seasonNumber > 0 }
            ?.maxByOrNull { it.seasonNumber }
            ?.seasonNumber

        // Fetch latest season details if available
        val latestSeasonDetails = if (latestSeasonNumber != null) {
            val seasonResult = tmdbService.getSeasonDetails(showId, latestSeasonNumber)
            seasonResult.getOrNull()
        } else {
            null
        }

        return Result.success(
            FullShowData(
                showDetails = showDetails,
                latestSeasonDetails = latestSeasonDetails
            )
        )
    }

    /**
     * Fetches details for a specific season.
     */
    suspend fun fetchSeasonDetails(showId: Int, seasonNumber: Int): Result<TMDBSeasonDetails> {
        return tmdbService.getSeasonDetails(showId, seasonNumber)
    }

    /**
     * Fetches details for all seasons of a show (excluding specials).
     * Uses SEQUENTIAL fetching for reliability - large shows like SVU (26 seasons)
     * can cause rate limiting or timeouts with parallel requests.
     * Falls back to summary data if a season fetch fails.
     */
    suspend fun fetchAllSeasons(showId: Int): Result<List<TMDBSeasonDetails>> {
        val showResult = tmdbService.getShowDetails(showId)
        if (showResult.isFailure) {
            Log.e(TAG, "Failed to get show details for showId=$showId")
            return Result.failure(showResult.exceptionOrNull()!!)
        }

        val showDetails = showResult.getOrThrow()
        val regularSeasons = showDetails.seasons
            ?.filter { it.seasonNumber > 0 }
            ?.sortedBy { it.seasonNumber }
            ?: return Result.success(emptyList())

        Log.d(TAG, "Fetching ${regularSeasons.size} seasons SEQUENTIALLY for '${showDetails.name}'")

        // Fetch all seasons SEQUENTIALLY with delay to avoid rate limiting
        // TMDB allows ~40 requests per 10 seconds
        val seasons = mutableListOf<TMDBSeasonDetails>()
        var successCount = 0
        var fallbackCount = 0

        for ((index, seasonSummary) in regularSeasons.withIndex()) {
            // Add delay between requests to avoid rate limiting (skip first)
            if (index > 0) {
                delay(150) // 150ms between requests = ~6.6 requests/second
            }

            // Try up to 3 times per season with increasing delays
            var seasonResult: Result<TMDBSeasonDetails>? = null
            for (attempt in 1..3) {
                seasonResult = tmdbService.getSeasonDetails(showId, seasonSummary.seasonNumber)

                if (seasonResult.isSuccess) {
                    break
                }

                // Check if rate limited (429)
                val error = seasonResult.exceptionOrNull()
                val isRateLimited = error?.message?.contains("rate", ignoreCase = true) == true ||
                                   error?.message?.contains("429") == true

                if (isRateLimited) {
                    Log.w(TAG, "Rate limited on S${seasonSummary.seasonNumber}, waiting ${attempt * 2} seconds...")
                    delay(attempt * 2000L) // 2s, 4s, 6s
                } else if (attempt < 3) {
                    Log.w(TAG, "Retry $attempt for S${seasonSummary.seasonNumber}")
                    delay(300L * attempt)
                }
            }

            if (seasonResult?.isSuccess == true) {
                seasons.add(seasonResult.getOrThrow())
                successCount++
                Log.d(TAG, "Fetched S${seasonSummary.seasonNumber} (${successCount}/${regularSeasons.size})")
            } else {
                // Fallback to summary data (like iOS)
                Log.e(TAG, "Failed S${seasonSummary.seasonNumber} after 3 attempts: ${seasonResult?.exceptionOrNull()?.message}")
                seasons.add(createSeasonFromSummary(seasonSummary))
                fallbackCount++
            }
        }

        Log.d(TAG, "Completed: ${seasons.size} seasons (${successCount} fetched, ${fallbackCount} fallback)")

        return Result.success(seasons)
    }

    /**
     * Creates a TMDBSeasonDetails from a season summary when the full fetch fails.
     * This provides fallback data without episode details.
     */
    private fun createSeasonFromSummary(summary: TMDBSeasonSummary): TMDBSeasonDetails {
        return TMDBSeasonDetails(
            id = summary.id,
            name = summary.name ?: "Season ${summary.seasonNumber}",
            overview = summary.overview ?: "",
            seasonNumber = summary.seasonNumber,
            airDate = summary.airDate,
            posterPath = summary.posterPath,
            episodes = emptyList() // No episodes available from summary
        )
    }
}
