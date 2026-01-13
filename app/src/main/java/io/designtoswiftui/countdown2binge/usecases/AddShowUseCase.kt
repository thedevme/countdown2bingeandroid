package io.designtoswiftui.countdown2binge.usecases

import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import io.designtoswiftui.countdown2binge.services.tmdb.ShowDataAggregator
import io.designtoswiftui.countdown2binge.services.tmdb.ShowProcessor
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for adding a new show to the user's followed shows.
 */
@Singleton
class AddShowUseCase @Inject constructor(
    private val repository: ShowRepository,
    private val aggregator: ShowDataAggregator,
    private val processor: ShowProcessor
) {

    /**
     * Result of adding a show.
     */
    sealed class AddShowResult {
        data class Success(val show: Show) : AddShowResult()
        data class AlreadyFollowed(val existingShow: Show) : AddShowResult()
        data class Error(val message: String, val cause: Throwable? = null) : AddShowResult()
    }

    /**
     * Execute the add show flow.
     *
     * @param tmdbId The TMDB ID of the show to add
     * @return Result containing the saved show or error
     */
    suspend fun execute(tmdbId: Int): Result<Show> {
        // Check if already followed
        if (repository.isShowFollowed(tmdbId)) {
            val existingShow = repository.getShow(tmdbId)
            return if (existingShow != null) {
                Result.success(existingShow)
            } else {
                Result.failure(AddShowException.AlreadyFollowed(tmdbId))
            }
        }

        // Fetch full show data from TMDB
        val fullDataResult = aggregator.fetchFullShowData(tmdbId)
        if (fullDataResult.isFailure) {
            return Result.failure(
                AddShowException.FetchFailed(
                    tmdbId,
                    fullDataResult.exceptionOrNull()
                )
            )
        }

        val fullData = fullDataResult.getOrThrow()
        val asOf = LocalDate.now()

        // Process show data into local model
        val show = processor.processShow(fullData, asOf)

        // Process seasons - fetch ALL season details for episode data
        val seasons = mutableListOf<io.designtoswiftui.countdown2binge.models.Season>()
        val episodesBySeasonTmdbId = mutableMapOf<Int, List<io.designtoswiftui.countdown2binge.models.Episode>>()

        // Fetch details for ALL seasons (not just the latest)
        val allSeasonsResult = aggregator.fetchAllSeasons(tmdbId)
        if (allSeasonsResult.isSuccess) {
            val allSeasonDetails = allSeasonsResult.getOrThrow()
            for (seasonDetails in allSeasonDetails) {
                val season = processor.processSeason(seasonDetails, 0, asOf) // showId will be set during save
                seasons.add(season)

                // Process episodes for this season
                val episodes = processor.processEpisodes(seasonDetails, 0) // seasonId will be set during save
                episodesBySeasonTmdbId[seasonDetails.id] = episodes
            }
        } else {
            // Fallback: use the latest season details if available, and summaries for others
            fullData.latestSeasonDetails?.let { seasonDetails ->
                val season = processor.processSeason(seasonDetails, 0, asOf)
                seasons.add(season)
                val episodes = processor.processEpisodes(seasonDetails, 0)
                episodesBySeasonTmdbId[seasonDetails.id] = episodes
            }

            // Process other seasons from summary (without episode details)
            fullData.showDetails.seasons
                ?.filter { summary ->
                    summary.seasonNumber > 0 &&
                            seasons.none { it.tmdbId == summary.id }
                }
                ?.forEach { summary ->
                    val season = processor.processSeasonSummary(summary, 0, asOf)
                    seasons.add(season)
                }
        }

        // Sort seasons by season number
        val sortedSeasons = seasons.sortedBy { it.seasonNumber }

        // Save everything to the repository
        return try {
            val showId = repository.saveShowWithSeasons(
                show = show,
                seasons = sortedSeasons,
                episodesBySeasonTmdbId = episodesBySeasonTmdbId
            )

            // Retrieve the saved show with its ID
            val savedShow = repository.getShowById(showId)
                ?: return Result.failure(AddShowException.SaveFailed(tmdbId, null))

            Result.success(savedShow)
        } catch (e: Exception) {
            Result.failure(AddShowException.SaveFailed(tmdbId, e))
        }
    }

    /**
     * Execute and return a typed result instead of Result<Show>.
     */
    suspend fun executeWithResult(tmdbId: Int): AddShowResult {
        // Check if already followed
        if (repository.isShowFollowed(tmdbId)) {
            val existingShow = repository.getShow(tmdbId)
            return if (existingShow != null) {
                AddShowResult.AlreadyFollowed(existingShow)
            } else {
                AddShowResult.Error("Show is marked as followed but not found in database")
            }
        }

        return when (val result = execute(tmdbId)) {
            is kotlin.Result<Show> -> {
                if (result.isSuccess) {
                    AddShowResult.Success(result.getOrThrow())
                } else {
                    val exception = result.exceptionOrNull()
                    AddShowResult.Error(
                        exception?.message ?: "Unknown error",
                        exception
                    )
                }
            }
        }
    }
}

/**
 * Exceptions specific to the AddShowUseCase.
 */
sealed class AddShowException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class AlreadyFollowed(val tmdbId: Int) : AddShowException(
        "Show with TMDB ID $tmdbId is already being followed"
    )

    class FetchFailed(val tmdbId: Int, cause: Throwable?) : AddShowException(
        "Failed to fetch show data for TMDB ID $tmdbId",
        cause
    )

    class SaveFailed(val tmdbId: Int, cause: Throwable?) : AddShowException(
        "Failed to save show with TMDB ID $tmdbId",
        cause
    )
}
