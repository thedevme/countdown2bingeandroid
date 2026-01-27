package io.designtoswiftui.countdown2binge.services

import android.util.Log
import io.designtoswiftui.countdown2binge.services.firebase.FranchiseService
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to migrate existing shows to include franchise/spinoff data.
 * Runs on every app launch to backfill relatedShowIds for shows that
 * don't have spinoff data linked yet.
 */
@Singleton
class FranchiseMigrationService @Inject constructor(
    private val showRepository: ShowRepository,
    private val franchiseService: FranchiseService
) {
    companion object {
        private const val TAG = "FranchiseMigration"
    }

    /**
     * Run migration on app launch.
     * Always checks for shows missing spinoff data, so users get updates
     * when Firebase franchise data changes.
     */
    suspend fun migrateIfNeeded() {
        Log.d(TAG, "Checking for shows missing franchise data")
        runMigration()
    }

    /**
     * Force run migration regardless of previous state.
     * Useful for refreshing franchise data after Firebase updates.
     */
    suspend fun forceRunMigration() {
        Log.d(TAG, "Force running franchise migration")
        runMigration()
    }

    private suspend fun runMigration() {
        try {
            // 1. Fetch franchise data from Firebase
            franchiseService.fetchFranchises()

            // 2. Get all followed shows
            val shows = showRepository.getAllShows().first()
            Log.d(TAG, "Found ${shows.size} shows to check for franchise data")

            var updatedCount = 0

            // 3. For each show, check if it's part of a franchise and update
            for (show in shows) {
                // Skip if already has related show IDs
                if (show.relatedShowIds.isNotEmpty()) {
                    continue
                }

                // Check if this show is part of a franchise
                val relatedIds = franchiseService.getSpinoffIds(forShowId = show.tmdbId)

                if (relatedIds.isNotEmpty()) {
                    Log.d(TAG, "Updating '${show.title}' with ${relatedIds.size} related shows")
                    showRepository.updateRelatedShowIds(show.id, relatedIds)
                    updatedCount++
                }
            }

            Log.d(TAG, "Migration complete: updated $updatedCount shows with franchise data")

        } catch (e: Exception) {
            Log.e(TAG, "Migration failed: ${e.message}", e)
        }
    }
}
