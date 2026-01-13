package io.designtoswiftui.countdown2binge.usecases

import io.designtoswiftui.countdown2binge.models.Episode
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import io.designtoswiftui.countdown2binge.services.state.SeasonStateManager
import io.designtoswiftui.countdown2binge.services.tmdb.ShowDataAggregator
import io.designtoswiftui.countdown2binge.services.tmdb.ShowProcessor
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for refreshing a show's data from TMDB.
 * Re-fetches all season details and episodes, updating the local database.
 */
@Singleton
class RefreshShowUseCase @Inject constructor(
    private val repository: ShowRepository,
    private val aggregator: ShowDataAggregator,
    private val processor: ShowProcessor,
    private val stateManager: SeasonStateManager
) {

    /**
     * Refresh a show's data from TMDB.
     * Updates seasons and fetches missing episodes.
     *
     * @param showId The local database ID of the show to refresh
     * @return Result containing the refreshed show or error
     */
    suspend fun execute(showId: Long): Result<Show> {
        // Get the existing show
        val existingShow = repository.getShowById(showId)
            ?: return Result.failure(RefreshException.ShowNotFound(showId))

        val tmdbId = existingShow.tmdbId

        // Fetch fresh show data from TMDB
        val fullDataResult = aggregator.fetchFullShowData(tmdbId)
        if (fullDataResult.isFailure) {
            return Result.failure(
                RefreshException.FetchFailed(tmdbId, fullDataResult.exceptionOrNull())
            )
        }

        val fullData = fullDataResult.getOrThrow()
        val asOf = LocalDate.now()

        // Update show details
        val updatedShow = existingShow.copy(
            title = fullData.showDetails.name,
            overview = fullData.showDetails.overview,
            posterPath = fullData.showDetails.posterPath,
            backdropPath = fullData.showDetails.backdropPath,
            status = processor.processShow(fullData, asOf).status
        )
        repository.update(updatedShow)

        // Fetch ALL season details
        val allSeasonsResult = aggregator.fetchAllSeasons(tmdbId)
        if (allSeasonsResult.isFailure) {
            return Result.failure(
                RefreshException.FetchFailed(tmdbId, allSeasonsResult.exceptionOrNull())
            )
        }

        val allSeasonDetails = allSeasonsResult.getOrThrow()
        val existingSeasons = repository.getSeasonsForShowSync(showId)

        // Process each season
        for (seasonDetails in allSeasonDetails) {
            val existingSeason = existingSeasons.find { it.tmdbId == seasonDetails.id }

            if (existingSeason != null) {
                // Update existing season
                val processedSeason = processor.processSeason(seasonDetails, showId, asOf)
                val updatedSeason = existingSeason.copy(
                    premiereDate = processedSeason.premiereDate,
                    finaleDate = processedSeason.finaleDate,
                    isFinaleEstimated = processedSeason.isFinaleEstimated,
                    episodeCount = processedSeason.episodeCount,
                    airedEpisodeCount = processedSeason.airedEpisodeCount,
                    releasePattern = processedSeason.releasePattern,
                    state = if (existingSeason.watchedDate != null) {
                        existingSeason.state // Keep watched state
                    } else {
                        stateManager.determineState(processedSeason, asOf)
                    },
                    posterPath = processedSeason.posterPath
                )
                repository.updateSeason(updatedSeason)

                // Check for missing episodes and add them
                val existingEpisodes = repository.getEpisodesForSeasonSync(existingSeason.id)
                val processedEpisodes = processor.processEpisodes(seasonDetails, existingSeason.id)

                for (episode in processedEpisodes) {
                    val existingEpisode = existingEpisodes.find { it.tmdbId == episode.tmdbId }
                    if (existingEpisode == null) {
                        // Add missing episode
                        repository.saveEpisode(episode)
                    }
                }
            } else {
                // Add new season
                val newSeason = processor.processSeason(seasonDetails, showId, asOf)
                val seasonId = repository.saveSeason(newSeason)

                // Add episodes for new season
                val episodes = processor.processEpisodes(seasonDetails, seasonId)
                repository.saveEpisodes(episodes)
            }
        }

        // Return the updated show
        return Result.success(repository.getShowById(showId) ?: updatedShow)
    }
}

/**
 * Exceptions specific to the RefreshShowUseCase.
 */
sealed class RefreshException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class ShowNotFound(val showId: Long) : RefreshException(
        "Show with ID $showId not found in database"
    )

    class FetchFailed(val tmdbId: Int, cause: Throwable?) : RefreshException(
        "Failed to fetch show data for TMDB ID $tmdbId",
        cause
    )
}
