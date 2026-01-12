package io.designtoswiftui.countdown2binge.services.tmdb

import javax.inject.Inject
import javax.inject.Singleton

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
     */
    suspend fun fetchAllSeasons(showId: Int): Result<List<TMDBSeasonDetails>> {
        val showResult = tmdbService.getShowDetails(showId)
        if (showResult.isFailure) {
            return Result.failure(showResult.exceptionOrNull()!!)
        }

        val showDetails = showResult.getOrThrow()
        val seasonNumbers = showDetails.seasons
            ?.filter { it.seasonNumber > 0 }
            ?.map { it.seasonNumber }
            ?: return Result.success(emptyList())

        val seasonDetails = mutableListOf<TMDBSeasonDetails>()
        for (seasonNumber in seasonNumbers) {
            val result = tmdbService.getSeasonDetails(showId, seasonNumber)
            if (result.isSuccess) {
                seasonDetails.add(result.getOrThrow())
            }
            // Continue even if individual season fetch fails
        }

        return Result.success(seasonDetails)
    }
}
