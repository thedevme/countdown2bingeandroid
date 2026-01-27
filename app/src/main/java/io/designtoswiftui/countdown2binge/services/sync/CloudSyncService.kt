package io.designtoswiftui.countdown2binge.services.sync

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import io.designtoswiftui.countdown2binge.services.auth.AuthManager
import io.designtoswiftui.countdown2binge.services.premium.PremiumManager
import io.designtoswiftui.countdown2binge.services.repository.ShowDao
import io.designtoswiftui.countdown2binge.services.settings.SettingsRepository
import io.designtoswiftui.countdown2binge.usecases.AddShowUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for syncing shows with Firebase Realtime Database.
 */
@Singleton
class CloudSyncService @Inject constructor(
    private val authManager: AuthManager,
    private val premiumManager: PremiumManager,
    private val showDao: ShowDao,
    private val addShowUseCase: AddShowUseCase,
    private val networkMonitor: NetworkMonitor,
    private val settingsRepository: SettingsRepository
) {
    companion object {
        private const val TAG = "CloudSyncService"
        private const val USERS_PATH = "users"
        private const val SHOWS_PATH = "shows"
        private const val METADATA_PATH = "metadata"
        private const val LAST_SYNCED_AT = "lastSyncedAt"
    }

    private val database = FirebaseDatabase.getInstance()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncResult = MutableStateFlow<SyncResult?>(null)
    val lastSyncResult: StateFlow<SyncResult?> = _lastSyncResult.asStateFlow()

    // Offline queue for pending operations
    private val pendingPushes = mutableSetOf<Int>()
    private val pendingRemovals = mutableSetOf<Int>()

    /**
     * Check if user has premium (real or simulated for debug).
     */
    private suspend fun isPremiumOrSimulated(): Boolean {
        val realPremium = premiumManager.isPremium.value
        val simulatedPremium = settingsRepository.debugSimulatePremium.first()
        return realPremium || simulatedPremium
    }

    /**
     * Whether sync is allowed (signed in and premium).
     */
    val canSync: Boolean
        get() = authManager.isSignedIn && premiumManager.isPremium.value

    /**
     * Whether sync is allowed for restore-only (signed in, but doesn't require premium).
     * Used for returning users who had premium before.
     */
    val canRestoreOnly: Boolean
        get() = authManager.isSignedIn

    init {
        // Set up network monitor callback to process pending operations when back online
        networkMonitor.onConnectionRestored = {
            scope.launch {
                processPendingOperations()
            }
        }
    }

    /**
     * Perform a full sync on sign-in.
     * Merges local and cloud data (union merge).
     */
    suspend fun syncOnSignIn(): SyncResult {
        if (!canRestoreOnly) {
            throw SyncError.NotSignedIn
        }

        if (!networkMonitor.isOnline.value) {
            throw SyncError.Offline
        }

        _syncState.value = SyncState.Syncing

        return try {
            val userId = authManager.userId ?: throw SyncError.NotSignedIn

            // 1. Get local TMDB IDs
            val localTmdbIds = showDao.getAllTmdbIds().toSet()
            Log.d(TAG, "Local shows: ${localTmdbIds.size}")

            // 2. Get cloud TMDB IDs
            val cloudShows = fetchCloudShows(userId)
            val cloudTmdbIds = cloudShows.keys
            Log.d(TAG, "Cloud shows: ${cloudTmdbIds.size}")

            // 3. Shows to add locally (in cloud, not local)
            val toAddLocally = cloudTmdbIds - localTmdbIds

            // 4. Shows to push to cloud (in local, not cloud) - only if premium
            val isPremium = isPremiumOrSimulated()
            val toPushToCloud = if (isPremium) {
                localTmdbIds - cloudTmdbIds
            } else {
                emptySet()
            }

            var restoredCount = 0
            var backedUpCount = 0

            // 5. Add shows from cloud to local
            for (tmdbId in toAddLocally) {
                try {
                    Log.d(TAG, "Restoring show $tmdbId from cloud")
                    val result = addShowUseCase.execute(tmdbId)
                    if (result.isSuccess) {
                        // Mark as synced
                        showDao.markAsSynced(tmdbId)
                        restoredCount++
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to restore show $tmdbId: ${e.message}")
                    // Continue with other shows
                }
            }

            // 6. Push local shows to cloud (only if premium)
            for (tmdbId in toPushToCloud) {
                try {
                    val show = showDao.getByTmdbId(tmdbId)
                    val followedAt = show?.followedAt ?: System.currentTimeMillis()
                    pushShowToCloud(userId, tmdbId, followedAt)
                    showDao.markAsSynced(tmdbId)
                    backedUpCount++
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to push show $tmdbId: ${e.message}")
                    // Queue for later
                    pendingPushes.add(tmdbId)
                }
            }

            // 7. Update last synced timestamp
            if (isPremium) {
                updateLastSyncedAt(userId)
            }

            val result = SyncResult(restoredCount, backedUpCount)
            _lastSyncResult.value = result
            _syncState.value = SyncState.Synced(Date())

            Log.d(TAG, "Sync complete: restored=$restoredCount, backed up=$backedUpCount")
            result

        } catch (e: SyncError) {
            _syncState.value = SyncState.Error(e.message ?: "Sync failed")
            throw e
        } catch (e: Exception) {
            val error = SyncError.Unknown(e)
            _syncState.value = SyncState.Error(error.message ?: "Sync failed")
            throw error
        }
    }

    /**
     * Push a single show to cloud (incremental).
     * Call this when a user follows a new show.
     */
    suspend fun pushShow(tmdbId: Int, followedAt: Long = System.currentTimeMillis()) {
        if (!canSync) {
            // Queue for later
            pendingPushes.add(tmdbId)
            pendingRemovals.remove(tmdbId)
            Log.d(TAG, "Queued push for show $tmdbId (not syncing)")
            return
        }

        if (!networkMonitor.isOnline.value) {
            pendingPushes.add(tmdbId)
            Log.d(TAG, "Queued push for show $tmdbId (offline)")
            return
        }

        val userId = authManager.userId ?: return

        try {
            pushShowToCloud(userId, tmdbId, followedAt)
            showDao.markAsSynced(tmdbId)
            Log.d(TAG, "Pushed show $tmdbId to cloud")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push show $tmdbId: ${e.message}")
            pendingPushes.add(tmdbId)
        }
    }

    /**
     * Remove a show from cloud (incremental).
     * Call this when a user unfollows a show.
     */
    suspend fun removeShow(tmdbId: Int) {
        if (!canSync) {
            // Queue for later
            pendingRemovals.add(tmdbId)
            pendingPushes.remove(tmdbId)
            Log.d(TAG, "Queued removal for show $tmdbId (not syncing)")
            return
        }

        if (!networkMonitor.isOnline.value) {
            pendingRemovals.add(tmdbId)
            Log.d(TAG, "Queued removal for show $tmdbId (offline)")
            return
        }

        val userId = authManager.userId ?: return

        try {
            removeShowFromCloud(userId, tmdbId)
            Log.d(TAG, "Removed show $tmdbId from cloud")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove show $tmdbId: ${e.message}")
            pendingRemovals.add(tmdbId)
        }
    }

    /**
     * Process any pending sync operations.
     * Called when network is restored or user becomes premium.
     */
    suspend fun processPendingOperations() {
        if (!canSync) return

        val userId = authManager.userId ?: return

        Log.d(TAG, "Processing pending operations: ${pendingPushes.size} pushes, ${pendingRemovals.size} removals")

        // Process pushes
        val pushesToProcess = pendingPushes.toList()
        for (tmdbId in pushesToProcess) {
            try {
                val show = showDao.getByTmdbId(tmdbId)
                val followedAt = show?.followedAt ?: System.currentTimeMillis()
                pushShowToCloud(userId, tmdbId, followedAt)
                showDao.markAsSynced(tmdbId)
                pendingPushes.remove(tmdbId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process pending push for $tmdbId: ${e.message}")
            }
        }

        // Process removals
        val removalsToProcess = pendingRemovals.toList()
        for (tmdbId in removalsToProcess) {
            try {
                removeShowFromCloud(userId, tmdbId)
                pendingRemovals.remove(tmdbId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process pending removal for $tmdbId: ${e.message}")
            }
        }
    }

    /**
     * Delete all cloud data for the current user.
     * Call this before deleting the user's account.
     */
    suspend fun deleteAllCloudData() {
        val userId = authManager.userId ?: throw SyncError.NotSignedIn

        try {
            database.reference
                .child(USERS_PATH)
                .child(userId)
                .removeValue()
                .await()

            _syncState.value = SyncState.Idle
            _lastSyncResult.value = null

            Log.d(TAG, "Deleted all cloud data for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete cloud data: ${e.message}")
            throw SyncError.DatabaseError(e)
        }
    }

    /**
     * Clear local sync state (call on sign out).
     */
    suspend fun clearSyncedState() {
        showDao.clearSyncedStatus()
        pendingPushes.clear()
        pendingRemovals.clear()
        _syncState.value = SyncState.Idle
        _lastSyncResult.value = null
        Log.d(TAG, "Cleared local sync state")
    }

    // region Private helpers

    private suspend fun fetchCloudShows(userId: String): Map<Int, CloudShowData> {
        val snapshot = database.reference
            .child(USERS_PATH)
            .child(userId)
            .child(SHOWS_PATH)
            .get()
            .await()

        if (!snapshot.exists()) return emptyMap()

        val result = mutableMapOf<Int, CloudShowData>()
        for (child in snapshot.children) {
            val tmdbId = child.key?.toIntOrNull() ?: continue
            val followedAt = child.child("followedAt").getValue(Long::class.java) ?: 0L
            val lastModified = child.child("lastModified").getValue(Long::class.java) ?: 0L
            result[tmdbId] = CloudShowData(followedAt, lastModified)
        }
        return result
    }

    private suspend fun pushShowToCloud(userId: String, tmdbId: Int, followedAt: Long) {
        val data = mapOf(
            "followedAt" to followedAt,
            "lastModified" to System.currentTimeMillis()
        )

        database.reference
            .child(USERS_PATH)
            .child(userId)
            .child(SHOWS_PATH)
            .child(tmdbId.toString())
            .setValue(data)
            .await()
    }

    private suspend fun removeShowFromCloud(userId: String, tmdbId: Int) {
        database.reference
            .child(USERS_PATH)
            .child(userId)
            .child(SHOWS_PATH)
            .child(tmdbId.toString())
            .removeValue()
            .await()
    }

    private suspend fun updateLastSyncedAt(userId: String) {
        database.reference
            .child(USERS_PATH)
            .child(userId)
            .child(METADATA_PATH)
            .child(LAST_SYNCED_AT)
            .setValue(System.currentTimeMillis())
            .await()
    }

    // endregion
}
