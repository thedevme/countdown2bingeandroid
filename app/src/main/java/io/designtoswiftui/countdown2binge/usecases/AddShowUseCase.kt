package io.designtoswiftui.countdown2binge.usecases

import android.util.Log
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.services.firebase.FranchiseService
import io.designtoswiftui.countdown2binge.services.premium.PremiumManager
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import io.designtoswiftui.countdown2binge.services.tmdb.ShowDataAggregator
import io.designtoswiftui.countdown2binge.services.tmdb.ShowProcessor
import io.designtoswiftui.countdown2binge.services.tmdb.FullShowData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AddShowUseCase"

/**
 * Use case for adding a new show to the user's followed shows.
 *
 * Uses a two-phase approach for better UX:
 * 1. Quick add: Fetch show details + latest season, save, return immediately
 * 2. Background: Fetch all remaining seasons and update the database
 *
 * This provides immediate feedback while large shows (SVU, Grey's Anatomy)
 * download their 20+ seasons in the background.
 *
 * Also handles:
 * - Premium limit check (3 shows for free users)
 * - Franchise/spinoff linking from Firebase
 */
@Singleton
class AddShowUseCase @Inject constructor(
    private val repository: ShowRepository,
    private val aggregator: ShowDataAggregator,
    private val processor: ShowProcessor,
    private val franchiseService: FranchiseService,
    private val premiumManager: PremiumManager
) {
    // Background scope for fetching remaining seasons after quick add
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Result of adding a show.
     */
    sealed class AddShowResult {
        data class Success(val show: Show) : AddShowResult()
        data class AlreadyFollowed(val existingShow: Show) : AddShowResult()
        data class Error(val message: String, val cause: Throwable? = null) : AddShowResult()
    }

    /**
     * Execute the add show flow using two-phase approach:
     * 1. Quick: Save show with latest season (for correct timeline placement)
     * 2. Background: Fetch all remaining seasons (no blocking)
     * 3. Link franchise data (spinoffs)
     *
     * @param tmdbId The TMDB ID of the show to add
     * @return Result containing the saved show or error
     */
    suspend fun execute(tmdbId: Int): Result<Show> {
        // Check show limit for free users
        val currentCount = repository.getFollowedShowCount()
        val showLimit = premiumManager.showLimit
        if (currentCount >= showLimit) {
            Log.w(TAG, "Show limit reached: $currentCount >= $showLimit")
            return Result.failure(AddShowException.ShowLimitReached(showLimit))
        }

        // Check if already followed
        if (repository.isShowFollowed(tmdbId)) {
            val existingShow = repository.getShow(tmdbId)
            return if (existingShow != null) {
                Result.success(existingShow)
            } else {
                Result.failure(AddShowException.AlreadyFollowed(tmdbId))
            }
        }

        // PHASE 1: Quick add with latest season only
        // This gives us enough data for correct timeline categorization
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
        val show = processor.processShow(fullData, asOf)
        val totalSeasonCount = fullData.showDetails.seasons
            ?.filter { it.seasonNumber > 0 }?.size ?: 0

        Log.d(TAG, "Quick add: '${show.title}' has $totalSeasonCount seasons total")

        // Process ONLY the latest season for quick save
        val quickSeasons = mutableListOf<io.designtoswiftui.countdown2binge.models.Season>()
        val quickEpisodes = mutableMapOf<Int, List<io.designtoswiftui.countdown2binge.models.Episode>>()

        fullData.latestSeasonDetails?.let { latestSeason ->
            Log.d(TAG, "Quick add: Using latest season S${latestSeason.seasonNumber}")
            val season = processor.processSeason(latestSeason, 0, asOf)
            Log.d(TAG, "Quick add: S${season.seasonNumber} state=${season.state}")
            quickSeasons.add(season)
            val episodes = processor.processEpisodes(latestSeason, 0)
            quickEpisodes[latestSeason.id] = episodes
        }

        // Also add season summaries for other seasons (basic info, no episodes)
        // This ensures the show appears complete even before background fetch
        fullData.showDetails.seasons
            ?.filter { summary ->
                summary.seasonNumber > 0 &&
                        quickSeasons.none { it.seasonNumber == summary.seasonNumber }
            }
            ?.forEach { summary ->
                val season = processor.processSeasonSummary(summary, 0, asOf)
                quickSeasons.add(season)
            }

        val sortedQuickSeasons = quickSeasons.sortedBy { it.seasonNumber }

        // Quick save to database
        val showId = try {
            repository.saveShowWithSeasons(
                show = show,
                seasons = sortedQuickSeasons,
                episodesBySeasonTmdbId = quickEpisodes
            )
        } catch (e: Exception) {
            return Result.failure(AddShowException.SaveFailed(tmdbId, e))
        }

        val savedShow = repository.getShowById(showId)
            ?: return Result.failure(AddShowException.SaveFailed(tmdbId, null))

        Log.d(TAG, "Quick add complete: '${savedShow.title}' saved with ${sortedQuickSeasons.size} seasons")

        // Link franchise/spinoff data
        saveFranchiseData(tmdbId, showId)

        // PHASE 2: Background fetch for full season details
        // Only needed if there are more seasons than just the latest
        if (totalSeasonCount > 1) {
            Log.d(TAG, "Launching background fetch for remaining season details")
            backgroundScope.launch {
                fetchRemainingSeasonsInBackground(
                    showId = showId,
                    tmdbId = tmdbId,
                    fullData = fullData,
                    asOf = asOf
                )
            }
        }

        return Result.success(savedShow)
    }

    /**
     * Background fetch for remaining season details.
     * Updates existing seasons with full episode data.
     */
    private suspend fun fetchRemainingSeasonsInBackground(
        showId: Long,
        tmdbId: Int,
        fullData: FullShowData,
        asOf: LocalDate
    ) {
        try {
            Log.d(TAG, "Background: Starting fetch for all seasons of tmdbId=$tmdbId")

            val allSeasonsResult = aggregator.fetchAllSeasons(tmdbId)
            if (allSeasonsResult.isFailure) {
                Log.e(TAG, "Background: Failed to fetch seasons - ${allSeasonsResult.exceptionOrNull()?.message}")
                return
            }

            val allSeasonDetails = allSeasonsResult.getOrThrow()
            Log.d(TAG, "Background: Fetched ${allSeasonDetails.size} season details")

            // Get existing seasons from database
            val existingSeasons = repository.getSeasonsForShowSync(showId)

            var updatedCount = 0
            for (seasonDetails in allSeasonDetails) {
                // Skip the latest season (already has full data)
                if (seasonDetails.id == fullData.latestSeasonDetails?.id) {
                    continue
                }

                // Find existing season by season number
                val existingSeason = existingSeasons.find { it.seasonNumber == seasonDetails.seasonNumber }

                if (existingSeason != null && seasonDetails.episodes?.isNotEmpty() == true) {
                    // Update existing season with full episode data
                    val processedSeason = processor.processSeason(seasonDetails, showId, asOf)
                    val updatedSeason = existingSeason.copy(
                        premiereDate = processedSeason.premiereDate,
                        finaleDate = processedSeason.finaleDate,
                        isFinaleEstimated = processedSeason.isFinaleEstimated,
                        episodeCount = processedSeason.episodeCount,
                        airedEpisodeCount = processedSeason.airedEpisodeCount,
                        releasePattern = processedSeason.releasePattern,
                        state = processedSeason.state
                    )
                    repository.updateSeason(updatedSeason)

                    // Add episodes if we don't have them
                    val existingEpisodes = repository.getEpisodesForSeasonSync(existingSeason.id)
                    if (existingEpisodes.isEmpty()) {
                        val episodes = processor.processEpisodes(seasonDetails, existingSeason.id)
                        repository.saveEpisodes(episodes)
                    }

                    updatedCount++
                }
            }

            Log.d(TAG, "Background: Updated $updatedCount seasons with full details")
        } catch (e: Exception) {
            Log.e(TAG, "Background: Error fetching seasons - ${e.message}")
        }
    }

    /**
     * Save franchise data for a show.
     * Links the show to its related shows (parent + spinoffs) from Firebase.
     */
    private suspend fun saveFranchiseData(tmdbId: Int, showId: Long) {
        try {
            // 1. Ensure franchise data is loaded from Firebase (uses memory cache)
            franchiseService.fetchFranchises()

            // 2. Check if this show is part of a franchise
            val franchise = franchiseService.getFranchise(forShowId = tmdbId)
            if (franchise == null) {
                Log.d(TAG, "Show $tmdbId is not part of any franchise")
                return
            }

            Log.d(TAG, "Found franchise: ${franchise.franchiseName?.en} for show $tmdbId")

            // 3. Get all related show IDs (excludes self)
            val relatedIds = franchiseService.getSpinoffIds(forShowId = tmdbId)

            Log.d(TAG, "Related show IDs: $relatedIds")

            // 4. Save related IDs to local database
            repository.updateRelatedShowIds(showId, relatedIds)
            Log.d(TAG, "Successfully saved ${relatedIds.size} related show IDs")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save franchise data: ${e.message}")
            // Non-fatal error - show is still added, just without spinoff links
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

    class ShowLimitReached(val limit: Int) : AddShowException(
        "Free tier limit of $limit shows reached. Upgrade to premium for unlimited shows."
    )

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
