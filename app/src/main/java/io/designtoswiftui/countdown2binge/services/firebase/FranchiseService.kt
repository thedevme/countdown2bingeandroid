package io.designtoswiftui.countdown2binge.services.firebase

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import io.designtoswiftui.countdown2binge.models.Franchise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for fetching franchise data from Firebase Realtime Database.
 * Franchises contain parent shows and their spinoffs for connected universe features.
 */
@Singleton
class FranchiseService @Inject constructor() {

    companion object {
        private const val TAG = "FranchiseService"
        private const val FRANCHISES_PATH = "franchises"
    }

    private val database = Firebase.database.reference
    private val gson = Gson()

    private val _franchises = MutableStateFlow<List<Franchise>>(emptyList())
    val franchises: StateFlow<List<Franchise>> = _franchises.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var hasLoaded = false

    // O(1) lookup map: TMDB ID -> Franchise
    private val showToFranchise: MutableMap<Int, Franchise> = mutableMapOf()

    /**
     * Fetch all franchises from Firebase.
     * Caches results and skips re-fetch if already loaded.
     */
    suspend fun fetchFranchises() {
        // Skip if already loaded with data (memory cache)
        if (hasLoaded && _franchises.value.isNotEmpty()) return

        _isLoading.value = true

        try {
            val snapshot = database.child(FRANCHISES_PATH).get().await()
            val loadedFranchises = mutableListOf<Franchise>()

            snapshot.children.forEach { child ->
                try {
                    val json = gson.toJson(child.value)
                    val franchise = gson.fromJson(json, Franchise::class.java)
                    if (franchise.parentShow != null) {
                        loadedFranchises.add(franchise)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse franchise: ${child.key}", e)
                }
            }

            _franchises.value = loadedFranchises.sortedBy { it.parentShow?.title }
            hasLoaded = _franchises.value.isNotEmpty()

            // Build O(1) lookup map after fetching
            buildLookupMap()

            Log.d(TAG, "Loaded ${_franchises.value.size} franchises, built lookup map with ${showToFranchise.size} entries")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch franchises", e)
        }

        _isLoading.value = false
    }

    /**
     * Build O(1) lookup map from TMDB ID to Franchise.
     * Maps both parent shows and all spinoffs to their franchise.
     */
    private fun buildLookupMap() {
        showToFranchise.clear()
        _franchises.value.forEach { franchise ->
            // Map parent show
            franchise.parentShow?.tmdbId?.let { parentId ->
                showToFranchise[parentId] = franchise
            }
            // Map all spinoffs
            franchise.spinoffs.forEach { spinoff ->
                showToFranchise[spinoff.tmdbId] = franchise
            }
        }
    }

    /**
     * Find the franchise for a given show ID.
     * O(1) lookup using the pre-built map.
     *
     * @param showId The TMDB ID of the show
     * @return The franchise if found, null otherwise
     */
    fun getFranchise(forShowId: Int): Franchise? {
        return showToFranchise[forShowId]
    }

    /**
     * Legacy method - use getFranchise instead.
     */
    @Deprecated("Use getFranchise instead", ReplaceWith("getFranchise(showId)"))
    fun franchiseForShowId(showId: Int): Franchise? {
        return getFranchise(showId)
    }

    /**
     * Check if a show has a franchise (either as parent or spinoff).
     * O(1) lookup.
     */
    fun hasFranchise(showId: Int): Boolean {
        return showToFranchise.containsKey(showId)
    }

    /**
     * Get all related show IDs for a given show (parent + spinoffs, excluding self).
     *
     * @param forShowId The TMDB ID of the show
     * @return List of related TMDB IDs, empty if not part of a franchise
     */
    fun getSpinoffIds(forShowId: Int): List<Int> {
        val franchise = showToFranchise[forShowId] ?: return emptyList()

        val allIds = mutableListOf<Int>()

        // Add parent show ID
        franchise.parentShow?.tmdbId?.let { allIds.add(it) }

        // Add all spinoff IDs
        allIds.addAll(franchise.spinoffs.map { it.tmdbId })

        // IMPORTANT: Exclude self
        return allIds.filter { it != forShowId }
    }

    /**
     * Force refresh franchises from Firebase.
     */
    suspend fun refresh() {
        hasLoaded = false
        showToFranchise.clear()
        fetchFranchises()
    }
}
