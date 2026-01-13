package io.designtoswiftui.countdown2binge.services

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker that refreshes all show data from TMDB.
 * Scheduled to run daily to keep episode and season data up to date.
 */
@HiltWorker
class RefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val refreshService: RefreshService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting background refresh of all shows")

        return try {
            val refreshResult = refreshService.refreshAllShows()

            when (refreshResult) {
                is RefreshService.RefreshResult.Success -> {
                    Log.d(TAG, "Successfully refreshed ${refreshResult.refreshedCount} shows")
                    Result.success()
                }
                is RefreshService.RefreshResult.PartialSuccess -> {
                    Log.w(TAG, "Partially refreshed: ${refreshResult.refreshedCount} succeeded, ${refreshResult.failedCount} failed")
                    Result.success() // Still consider partial success as success
                }
                is RefreshService.RefreshResult.Error -> {
                    Log.e(TAG, "Refresh failed: ${refreshResult.message}")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during background refresh", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "RefreshWorker"
        const val WORK_NAME = "refresh_shows_work"
    }
}
