package io.designtoswiftui.countdown2binge.services

import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import io.designtoswiftui.countdown2binge.usecases.RefreshShowUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for refreshing show data from TMDB.
 * Handles both individual show refresh and bulk refresh of all shows.
 */
@Singleton
class RefreshService @Inject constructor(
    private val repository: ShowRepository,
    private val refreshShowUseCase: RefreshShowUseCase
) {

    /**
     * Result of a refresh operation.
     */
    sealed class RefreshResult {
        data class Success(val refreshedCount: Int) : RefreshResult()
        data class PartialSuccess(val refreshedCount: Int, val failedCount: Int) : RefreshResult()
        data class Error(val message: String) : RefreshResult()
    }

    /**
     * Refresh a single show's data from TMDB.
     * Updates seasons, episodes, and recalculates states.
     *
     * @param showId The local database ID of the show to refresh
     * @return Result containing the refreshed show or error
     */
    suspend fun refreshShow(showId: Long): Result<Show> {
        return refreshShowUseCase.execute(showId)
    }

    /**
     * Refresh all followed shows from TMDB.
     * Updates seasons, episodes, and recalculates states for each show.
     *
     * @return RefreshResult indicating success, partial success, or error
     */
    suspend fun refreshAllShows(): RefreshResult {
        return try {
            val shows = repository.getAllShows().first()

            if (shows.isEmpty()) {
                return RefreshResult.Success(0)
            }

            var successCount = 0
            var failCount = 0

            for (show in shows) {
                val result = refreshShowUseCase.execute(show.id)
                if (result.isSuccess) {
                    successCount++
                } else {
                    failCount++
                }
            }

            when {
                failCount == 0 -> RefreshResult.Success(successCount)
                successCount == 0 -> RefreshResult.Error("Failed to refresh all shows")
                else -> RefreshResult.PartialSuccess(successCount, failCount)
            }
        } catch (e: Exception) {
            RefreshResult.Error(e.message ?: "Unknown error during refresh")
        }
    }

    /**
     * Refresh shows that haven't been updated recently.
     * Useful for background refresh to avoid unnecessary API calls.
     *
     * @param maxAgeHours Only refresh shows not updated within this many hours
     * @return RefreshResult indicating success, partial success, or error
     */
    suspend fun refreshStaleShows(maxAgeHours: Int = 24): RefreshResult {
        // For now, just refresh all shows
        // Future enhancement: track last refresh time per show
        return refreshAllShows()
    }
}
