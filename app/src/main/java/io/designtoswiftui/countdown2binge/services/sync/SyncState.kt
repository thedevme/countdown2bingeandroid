package io.designtoswiftui.countdown2binge.services.sync

import java.util.Date

/**
 * Represents the current state of cloud synchronization.
 */
sealed class SyncState {
    /**
     * No sync operation in progress.
     */
    data object Idle : SyncState()

    /**
     * Sync is currently in progress.
     */
    data object Syncing : SyncState()

    /**
     * Last sync completed successfully.
     */
    data class Synced(val lastSyncedAt: Date) : SyncState()

    /**
     * Sync failed with an error.
     */
    data class Error(val message: String) : SyncState()

    val isSyncing: Boolean
        get() = this is Syncing
}

/**
 * Result of a sync operation.
 */
data class SyncResult(
    val showsRestoredFromCloud: Int,
    val showsBackedUpToCloud: Int
) {
    val isFirstTimeBackup: Boolean
        get() = showsBackedUpToCloud > 0 && showsRestoredFromCloud == 0

    val isReturningUser: Boolean
        get() = showsRestoredFromCloud > 0

    val hasNoShows: Boolean
        get() = showsRestoredFromCloud == 0 && showsBackedUpToCloud == 0

    val title: String
        get() = when {
            isReturningUser -> "Welcome Back!"
            isFirstTimeBackup -> "Backup Complete"
            else -> "You're All Set!"
        }

    val message: String
        get() = when {
            isReturningUser && showsBackedUpToCloud > 0 ->
                "Restored $showsRestoredFromCloud shows and backed up $showsBackedUpToCloud"
            isReturningUser ->
                "Restored $showsRestoredFromCloud shows from your account"
            isFirstTimeBackup ->
                "Backed up $showsBackedUpToCloud shows to your account"
            else ->
                "Your shows are ready to sync"
        }
}

/**
 * Data structure for a show stored in Firebase.
 */
data class CloudShowData(
    val followedAt: Long = 0L,
    val lastModified: Long = 0L
) {
    constructor() : this(0L, 0L)  // Required for Firebase
}

/**
 * Errors that can occur during sync operations.
 */
sealed class SyncError : Exception() {
    data object NotSignedIn : SyncError() {
        override val message: String = "Please sign in to sync"
    }

    data object NotPremium : SyncError() {
        override val message: String = "Cloud sync requires premium"
    }

    data object Offline : SyncError() {
        override val message: String = "No internet connection"
    }

    data class NetworkError(override val cause: Throwable?) : SyncError() {
        override val message: String = "Network error: ${cause?.message}"
    }

    data class DatabaseError(override val cause: Throwable?) : SyncError() {
        override val message: String = "Database error: ${cause?.message}"
    }

    data class Unknown(override val cause: Throwable?) : SyncError() {
        override val message: String = cause?.message ?: "An unknown error occurred"
    }
}
