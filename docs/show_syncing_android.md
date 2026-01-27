# Countdown2Binge Show Syncing - Android Implementation Guide

## Overview

The cloud sync system enables users to backup and restore their followed shows across devices using Firebase Realtime Database. It requires both:
1. **Authentication** - Sign in with Apple
2. **Premium subscription** - Cloud sync is a premium feature

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                         Android App                          │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │ AuthManager  │    │ SyncService  │    │ PremiumMgr   │  │
│  │              │◄───│              │───►│              │  │
│  │ - Sign In    │    │ - Full Sync  │    │ - isPremium  │  │
│  │ - Sign Out   │    │ - Push/Pull  │    │ - canSync    │  │
│  │ - Delete     │    │ - Offline Q  │    │              │  │
│  └──────┬───────┘    └──────┬───────┘    └──────────────┘  │
│         │                   │                               │
│         ▼                   ▼                               │
│  ┌──────────────┐    ┌──────────────┐                      │
│  │ Firebase     │    │ Room DB      │                      │
│  │ Auth         │    │ (Local)      │                      │
│  └──────┬───────┘    └──────────────┘                      │
│         │                                                   │
└─────────┼───────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────────┐
│                    Firebase Realtime DB                      │
│                                                              │
│  users/                                                      │
│    {userId}/                                                 │
│      shows/                                                  │
│        {tmdbId}/                                            │
│          followedAt: 1705779600000                          │
│          lastModified: 1705779600000                        │
│      metadata/                                               │
│        lastSyncedAt: 1705779600000                          │
└─────────────────────────────────────────────────────────────┘
```

---

## Firebase Database Schema

### Data Structure

```
users/
  {userId}/
    shows/
      {tmdbId}: {
        followedAt: Long,      // Unix timestamp in milliseconds
        lastModified: Long     // Unix timestamp in milliseconds
      }
    metadata/
      lastSyncedAt: Long       // Last full sync timestamp
```

### Example Data

```json
{
  "users": {
    "abc123": {
      "shows": {
        "1399": {
          "followedAt": 1705779600000,
          "lastModified": 1705779600000
        },
        "66732": {
          "followedAt": 1705865600000,
          "lastModified": 1705865600000
        }
      },
      "metadata": {
        "lastSyncedAt": 1705900000000
      }
    }
  }
}
```

**Key Points:**
- Only TMDB ID and timestamps synced (not full show data)
- Full show metadata fetched from TMDB API when restoring
- Timestamps in **milliseconds** since epoch (not seconds)
- Shows indexed by TMDB ID as string key

---

## Authentication

### Sign In with Apple on Android

Use the Firebase Auth + Sign in with Apple credential provider:

```kotlin
data class AuthState {
    sealed class State {
        object Unknown : State()
        object SignedOut : State()
        data class SignedIn(val userId: String, val email: String?) : State()
    }

    val state: State
    val isLoading: Boolean
    val error: AuthError?
}

class AuthManager {
    private val auth = Firebase.auth

    val authState: StateFlow<AuthState>

    suspend fun signInWithApple(): Result<Unit>
    suspend fun signOut(): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
    suspend fun reauthenticateAndDelete(): Result<Unit>
}
```

### Apple Sign In Flow

1. Create Apple OAuth provider
2. Start sign-in activity for result
3. Handle credential in callback
4. Create Firebase credential from Apple token
5. Sign in to Firebase with credential
6. Auth state listener updates UI

```kotlin
suspend fun signInWithApple() {
    val provider = OAuthProvider.newBuilder("apple.com")
        .addScope("email")
        .addScope("name")
        .build()

    auth.startActivityForSignInWithProvider(activity, provider)
        .addOnSuccessListener { result ->
            // User signed in
            val user = result.user
            _authState.value = AuthState.SignedIn(user.uid, user.email)
        }
        .addOnFailureListener { e ->
            _authState.value = AuthState.SignedOut
            _error.value = AuthError.SignInFailed(e)
        }
}
```

### Auth State Listener

```kotlin
init {
    auth.addAuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _authState.value = if (user != null) {
            AuthState.SignedIn(user.uid, user.email)
        } else {
            AuthState.SignedOut
        }
    }
}
```

---

## Sync Service

### Core Properties

```kotlin
@Singleton
class CloudSyncService @Inject constructor(
    private val database: FirebaseDatabase,
    private val authManager: AuthManager,
    private val premiumManager: PremiumManager,
    private val showRepository: ShowRepository,
    private val tmdbService: TMDBService
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastSyncResult = MutableStateFlow<SyncResult?>(null)
    val lastSyncResult: StateFlow<SyncResult?> = _lastSyncResult.asStateFlow()

    // Offline queue
    private val pendingPushes = mutableSetOf<Int>()
    private val pendingRemovals = mutableSetOf<Int>()

    val canSync: Boolean
        get() = authManager.isSignedIn && premiumManager.isPremium
}
```

### Sync States

```kotlin
sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Synced(val lastSyncedAt: Date) : SyncState()
    data class Error(val message: String) : SyncState()

    val isSyncing: Boolean
        get() = this is Syncing
}
```

### Sync Result

```kotlin
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
```

---

## Sync Operations

### 1. Full Sync (Union Merge)

Called on sign-in or manual sync. Merges local and cloud data.

```kotlin
suspend fun syncOnSignIn(): SyncResult {
    if (!canSync) throw SyncError.NotAllowed

    _syncState.value = SyncState.Syncing

    try {
        val userId = authManager.userId ?: throw SyncError.NotSignedIn

        // 1. Get local TMDB IDs
        val localShows = showRepository.getAllFollowed()
        val localTmdbIds = localShows.map { it.tmdbId }.toSet()

        // 2. Get cloud TMDB IDs
        val cloudShows = fetchCloudShows(userId)
        val cloudTmdbIds = cloudShows.keys

        // 3. Shows to add locally (in cloud, not local)
        val toAddLocally = cloudTmdbIds - localTmdbIds

        // 4. Shows to push to cloud (in local, not cloud)
        val toPushToCloud = localTmdbIds - cloudTmdbIds

        var restoredCount = 0
        var backedUpCount = 0

        // 5. Add shows from cloud to local
        for (tmdbId in toAddLocally) {
            try {
                val show = tmdbService.getShowDetails(tmdbId)
                showRepository.save(show, skipCloudPush = true)
                restoredCount++
            } catch (e: Exception) {
                // Log but continue
            }
        }

        // 6. Push local shows to cloud
        for (tmdbId in toPushToCloud) {
            val followedAt = localShows.find { it.tmdbId == tmdbId }?.followedAt ?: Date()
            pushShowToCloud(userId, tmdbId, followedAt)
            backedUpCount++
        }

        // 7. Update last synced timestamp
        updateLastSyncedAt(userId)

        val result = SyncResult(restoredCount, backedUpCount)
        _lastSyncResult.value = result
        _syncState.value = SyncState.Synced(Date())

        return result

    } catch (e: Exception) {
        _syncState.value = SyncState.Error(e.message ?: "Sync failed")
        throw e
    }
}
```

### 2. Incremental Push (Single Show)

Called when user adds a show locally.

```kotlin
suspend fun pushShow(tmdbId: Int, followedAt: Date) {
    if (!canSync) {
        // Queue for later
        pendingPushes.add(tmdbId)
        pendingRemovals.remove(tmdbId)
        return
    }

    val userId = authManager.userId ?: return

    try {
        pushShowToCloud(userId, tmdbId, followedAt)
    } catch (e: Exception) {
        // Queue for retry
        pendingPushes.add(tmdbId)
    }
}

private suspend fun pushShowToCloud(userId: String, tmdbId: Int, followedAt: Date) {
    val data = mapOf(
        "followedAt" to followedAt.time,
        "lastModified" to System.currentTimeMillis()
    )

    database.reference
        .child("users")
        .child(userId)
        .child("shows")
        .child(tmdbId.toString())
        .setValue(data)
        .await()
}
```

### 3. Incremental Remove (Single Show)

Called when user unfollows a show locally.

```kotlin
suspend fun removeShow(tmdbId: Int) {
    if (!canSync) {
        // Queue for later
        pendingRemovals.add(tmdbId)
        pendingPushes.remove(tmdbId)
        return
    }

    val userId = authManager.userId ?: return

    try {
        removeShowFromCloud(userId, tmdbId)
    } catch (e: Exception) {
        // Queue for retry
        pendingRemovals.add(tmdbId)
    }
}

private suspend fun removeShowFromCloud(userId: String, tmdbId: Int) {
    database.reference
        .child("users")
        .child(userId)
        .child("shows")
        .child(tmdbId.toString())
        .removeValue()
        .await()
}
```

### 4. Fetch Cloud Shows

```kotlin
private suspend fun fetchCloudShows(userId: String): Map<Int, CloudShowData> {
    val snapshot = database.reference
        .child("users")
        .child(userId)
        .child("shows")
        .get()
        .await()

    if (!snapshot.exists()) return emptyMap()

    return snapshot.children.associate { child ->
        val tmdbId = child.key?.toIntOrNull() ?: return@associate null to null
        val followedAt = child.child("followedAt").getValue(Long::class.java) ?: 0L
        val lastModified = child.child("lastModified").getValue(Long::class.java) ?: 0L

        tmdbId to CloudShowData(
            followedAt = Date(followedAt),
            lastModified = Date(lastModified)
        )
    }.filterKeys { it != null }.mapKeys { it.key!! }
}
```

---

## Sync Triggers

### 1. App Launch

```kotlin
// In Application or MainActivity
lifecycleScope.launch {
    if (cloudSyncService.canSync) {
        cloudSyncService.syncOnLaunch()
    }
}
```

### 2. Return from Background

```kotlin
// Using ProcessLifecycleOwner
ProcessLifecycleOwner.get().lifecycle.addObserver(
    object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            if (cloudSyncService.canSync) {
                lifecycleScope.launch {
                    cloudSyncService.syncOnLaunch()
                }
            }
        }
    }
)
```

### 3. After Sign In

```kotlin
// In sign-in flow
suspend fun completeSignIn() {
    authManager.signInWithApple()

    if (premiumManager.isPremium) {
        showLoadingMessage("Syncing shows...")
        cloudSyncService.syncOnSignIn()
    }
}
```

### 4. Manual Sync Button

```kotlin
// In Settings > Account
Button(
    onClick = {
        scope.launch {
            cloudSyncService.syncOnSignIn()
        }
    },
    enabled = !syncState.isSyncing
) {
    Text("Sync Now")
}
```

### 5. Per-Show Operations

```kotlin
// In ShowRepository
suspend fun followShow(show: Show) {
    // Save locally
    localDb.followedShowDao().insert(show.toFollowedShow())

    // Push to cloud (non-blocking)
    scope.launch {
        cloudSyncService.pushShow(show.tmdbId, Date())
    }
}

suspend fun unfollowShow(tmdbId: Int) {
    // Remove locally
    localDb.followedShowDao().delete(tmdbId)

    // Remove from cloud (non-blocking)
    scope.launch {
        cloudSyncService.removeShow(tmdbId)
    }
}
```

---

## Offline Support

### Network Monitor

```kotlin
class NetworkMonitor @Inject constructor(
    private val context: Context
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isOnline: StateFlow<Boolean>

    init {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isOnline.value = true
                onConnectionRestored()
            }

            override fun onLost(network: Network) {
                _isOnline.value = false
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)
    }

    private fun onConnectionRestored() {
        scope.launch {
            cloudSyncService.processPendingOperations()
        }
    }
}
```

### Process Pending Queue

```kotlin
suspend fun processPendingOperations() {
    if (!canSync) return

    val userId = authManager.userId ?: return

    // Process pushes
    val pushes = pendingPushes.toList()
    for (tmdbId in pushes) {
        try {
            pushShowToCloud(userId, tmdbId, Date())
            pendingPushes.remove(tmdbId)
        } catch (e: Exception) {
            // Keep in queue
        }
    }

    // Process removals
    val removals = pendingRemovals.toList()
    for (tmdbId in removals) {
        try {
            removeShowFromCloud(userId, tmdbId)
            pendingRemovals.remove(tmdbId)
        } catch (e: Exception) {
            // Keep in queue
        }
    }
}
```

---

## Premium Gating

### Premium Manager

```kotlin
@Singleton
class PremiumManager @Inject constructor(
    private val billing: BillingClient  // Or RevenueCat
) {
    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    val canUseCloudSync: Boolean
        get() = isPremium.value

    val showLimit: Int
        get() = if (isPremium.value) Int.MAX_VALUE else 3
}
```

### Gate Check

```kotlin
// Before any sync operation
fun validateSyncPrerequisites() {
    if (!authManager.isSignedIn) {
        throw SyncError.NotSignedIn
    }
    if (!premiumManager.isPremium) {
        throw SyncError.NotPremium
    }
}
```

### Special Case: Returning User Restore

Users who previously had premium can restore shows even if not currently premium:

```kotlin
suspend fun syncForReturningUser(): SyncResult {
    // Only requires sign-in, NOT premium
    if (!authManager.isSignedIn) throw SyncError.NotSignedIn

    // Perform restore-only sync (no push)
    // This allows restoring shows from previous premium period
}
```

---

## Account Management

### Sign Out

```kotlin
suspend fun signOut() {
    // Clear local sync state
    cloudSyncService.clearSyncedState()

    // Sign out from Firebase
    authManager.signOut()

    // Navigate to signed-out state
    // Local shows remain on device
}
```

### Delete Account

**Important:** Apple requires all user data be deleted when account is deleted.

```kotlin
suspend fun deleteAccount() {
    try {
        // 1. Delete cloud data first (atomic)
        cloudSyncService.deleteAllCloudData()

        // 2. Delete Firebase account
        authManager.deleteAccount()

    } catch (e: FirebaseAuthRecentLoginRequiredException) {
        // Need to re-authenticate
        authManager.reauthenticateAndDelete()
    }
}

// In CloudSyncService
suspend fun deleteAllCloudData() {
    val userId = authManager.userId ?: throw SyncError.NotSignedIn

    // Delete entire user node atomically
    database.reference
        .child("users")
        .child(userId)
        .removeValue()
        .await()

    _syncState.value = SyncState.Idle
}
```

---

## Settings UI

### Account Section (Not Signed In)

```
┌─────────────────────────────────┐
│ ACCOUNT                         │
├─────────────────────────────────┤
│ ┌───┐                          │
│ │ ☁ │  Sign in to Sync         │
│ └───┘  Backup and restore your │
│        shows across devices     │
│                                 │
│ ┌─────────────────────────────┐│
│ │   Sign in with Apple      ││
│ └─────────────────────────────┘│
└─────────────────────────────────┘
```

### Account Section (Signed In)

```
┌─────────────────────────────────┐
│ ACCOUNT                         │
├─────────────────────────────────┤
│ ┌───┐                          │
│ │ 👤│  user@email.com          │
│ └───┘   Signed in with Apple  │
│                                 │
│ [Manage Account →]              │
└─────────────────────────────────┘
```

### Account Detail View

```
┌─────────────────────────────────┐
│ ← Account                       │
├─────────────────────────────────┤
│ ACCOUNT INFO                    │
│ ┌───────────────────────────┐  │
│ │ ┌───┐  user@email.com     │  │
│ │ │ 👤│                     │  │
│ │ └───┘   Signed in with   │  │
│ │         Apple             │  │
│ └───────────────────────────┘  │
│                                 │
│ CLOUD SYNC                      │
│ ┌───────────────────────────┐  │
│ │ ┌───┐  Sync Status        │  │
│ │ │ ☁✓│  Last synced        │  │
│ │ └───┘  2 hours ago        │  │
│ └───────────────────────────┘  │
│                                 │
│ ┌───────────────────────────┐  │
│ │ ┌───┐  Sync Now           │  │
│ │ │ ↻ │                     │  │
│ │ └───┘                     │  │
│ └───────────────────────────┘  │
│                                 │
│ ACCOUNT ACTIONS                 │
│ ┌───────────────────────────┐  │
│ │ ┌───┐  Sign Out           │  │
│ │ │ → │                     │  │
│ │ └───┘                     │  │
│ └───────────────────────────┘  │
│                                 │
│ ┌───────────────────────────┐  │
│ │ ┌───┐  Delete Account     │  │
│ │ │ 🗑 │  This cannot be     │  │
│ │ └───┘  undone             │  │
│ └───────────────────────────┘  │
└─────────────────────────────────┘
```

### Sync Status States

| State | Icon | Text |
|-------|------|------|
| Ready | ☁ | Ready to sync |
| Syncing | ↻ (animated) | Syncing... |
| Synced | ☁✓ | Last synced X ago |
| Error | ☁! | Error message |
| Premium Required | ☁ | Premium required (with PRO badge) |

---

## Error Handling

### Sync Errors

```kotlin
sealed class SyncError : Exception() {
    object NotSignedIn : SyncError() {
        override val message = "Please sign in to sync"
    }
    object NotPremium : SyncError() {
        override val message = "Cloud sync requires premium"
    }
    object Offline : SyncError() {
        override val message = "No internet connection"
    }
    data class NetworkError(val cause: Throwable) : SyncError() {
        override val message = "Network error: ${cause.message}"
    }
    data class DatabaseError(val cause: Throwable) : SyncError() {
        override val message = "Database error: ${cause.message}"
    }
}
```

### Auth Errors

```kotlin
sealed class AuthError : Exception() {
    data class SignInFailed(val cause: Throwable) : AuthError()
    data class SignOutFailed(val cause: Throwable) : AuthError()
    object NotSignedIn : AuthError()
    data class DeletionFailed(val cause: Throwable) : AuthError()
    object ReauthenticationRequired : AuthError()
}
```

---

## Local Data Model

### FollowedShow Entity

```kotlin
@Entity(tableName = "followed_shows")
data class FollowedShow(
    @PrimaryKey val tmdbId: Int,
    val followedAt: Date,
    val lastRefreshedAt: Date?,
    val isSynced: Boolean = false,

    // Cached show data (optional, for offline)
    val name: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String?,
    val status: ShowStatus?,
    val numberOfSeasons: Int?,
    val voteAverage: Double?
)
```

### DAO

```kotlin
@Dao
interface FollowedShowDao {
    @Query("SELECT * FROM followed_shows ORDER BY followedAt DESC")
    fun getAllFollowed(): Flow<List<FollowedShow>>

    @Query("SELECT tmdbId FROM followed_shows")
    suspend fun getAllTmdbIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(show: FollowedShow)

    @Query("DELETE FROM followed_shows WHERE tmdbId = :tmdbId")
    suspend fun delete(tmdbId: Int)

    @Query("UPDATE followed_shows SET isSynced = 0")
    suspend fun clearSyncedStatus()

    @Query("SELECT COUNT(*) FROM followed_shows WHERE isSynced = 1")
    fun getSyncedCount(): Flow<Int>
}
```

---

## Localization Keys

```xml
<!-- Account -->
<string name="settings_account">Account</string>
<string name="settings_sign_in_to_sync">Sign in to Sync</string>
<string name="settings_sync_description">Backup and restore your shows across devices</string>
<string name="settings_cloud_sync">Cloud Sync</string>
<string name="settings_manage_account">Manage account</string>

<!-- Sync Status -->
<string name="settings_sync_ready">Ready to sync</string>
<string name="settings_sync_syncing">Syncing…</string>
<string name="settings_sync_last_synced">Last synced %s</string>
<string name="settings_sync_premium_required">Premium required</string>

<!-- Account Detail -->
<string name="account_title">Account</string>
<string name="account_info">Account Info</string>
<string name="account_signed_in_with_apple">Signed in with Apple</string>
<string name="account_sync_status">Sync Status</string>
<string name="account_sync_now">Sync Now</string>
<string name="account_actions">Account Actions</string>
<string name="account_sign_out">Sign Out</string>
<string name="account_sign_out_title">Sign Out?</string>
<string name="account_sign_out_message">Your shows will remain on this device.</string>
<string name="account_delete">Delete Account</string>
<string name="account_delete_title">Delete Account?</string>
<string name="account_delete_message">This will permanently delete your account and cloud data. Your shows will remain on this device.</string>
<string name="account_delete_warning">This cannot be undone</string>

<!-- Sync Results -->
<string name="sync_title_welcome_back">Welcome Back!</string>
<string name="sync_title_backed_up">Backup Complete</string>
<string name="sync_title_ready">You\'re All Set!</string>
<string name="sync_result_restored">Restored %d shows from your account</string>
<string name="sync_result_restored_one">Restored 1 show from your account</string>
<string name="sync_result_backed_up">Backed up %d shows to your account</string>
<string name="sync_result_backed_up_one">Backed up 1 show to your account</string>
<string name="sync_result_merged">Restored %1$d shows and backed up %2$d new shows</string>
<string name="sync_result_ready">Your shows are ready to sync</string>
```

---

## Implementation Checklist

### Services
- [ ] AuthManager with Firebase Auth
- [ ] CloudSyncService with Firebase Realtime Database
- [ ] PremiumManager with Play Billing / RevenueCat
- [ ] NetworkMonitor for offline support

### Database
- [ ] FollowedShow entity with sync metadata
- [ ] FollowedShowDao with sync queries
- [ ] Migration if adding sync columns

### UI Screens
- [ ] AccountSectionView (in Settings)
- [ ] AccountDetailView (full screen)
- [ ] AllSavedShowsView (list view)
- [ ] SignInWithAppleButton (reusable)
- [ ] Sync status indicators

### Flows
- [ ] Sign in with Apple flow
- [ ] Full sync on sign-in
- [ ] Incremental push on follow
- [ ] Incremental remove on unfollow
- [ ] Offline queue processing
- [ ] Sign out flow
- [ ] Delete account flow

### Testing
- [ ] Unit tests for sync logic
- [ ] Integration tests with Firebase emulator
- [ ] Offline/online transition tests
- [ ] Account deletion verification
