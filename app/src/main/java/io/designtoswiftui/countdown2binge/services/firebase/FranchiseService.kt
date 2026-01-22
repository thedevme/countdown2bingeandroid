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

    /**
     * Fetch all franchises from Firebase.
     * Caches results and skips re-fetch if already loaded.
     */
    suspend fun fetchFranchises() {
        // Skip if already loaded with data
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

            Log.d(TAG, "Loaded ${_franchises.value.size} franchises")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch franchises", e)
        }

        _isLoading.value = false
    }

    /**
     * Find the franchise for a given show ID.
     * Checks both parent shows and spinoffs.
     *
     * @param showId The TMDB ID of the show
     * @return The franchise if found, null otherwise
     */
    fun franchiseForShowId(showId: Int): Franchise? {
        // Check if it's a parent show
        _franchises.value.find { it.parentShow?.tmdbId == showId }?.let { return it }

        // Check if it's a spinoff
        _franchises.value.forEach { franchise ->
            if (franchise.spinoffs.any { it.tmdbId == showId }) {
                return franchise
            }
        }
        return null
    }

    /**
     * Check if a show has a franchise (either as parent or spinoff).
     */
    fun hasFranchise(showId: Int): Boolean {
        return franchiseForShowId(showId) != null
    }

    /**
     * Force refresh franchises from Firebase.
     */
    suspend fun refresh() {
        hasLoaded = false
        fetchFranchises()
    }
}
