package io.designtoswiftui.countdown2binge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBSearchResult
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.usecases.AddShowUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Search screen.
 * Handles search queries, results, and adding shows.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val tmdbService: TMDBService,
    private val addShowUseCase: AddShowUseCase,
    private val repository: ShowRepository
) : ViewModel() {

    // Search query text
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Search results from TMDB
    private val _searchResults = MutableStateFlow<List<TMDBSearchResult>>(emptyList())
    val searchResults: StateFlow<List<TMDBSearchResult>> = _searchResults.asStateFlow()

    // Loading state
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Set of TMDB IDs that are currently being added
    private val _addingShows = MutableStateFlow<Set<Int>>(emptySet())
    val addingShows: StateFlow<Set<Int>> = _addingShows.asStateFlow()

    // Set of TMDB IDs that are already followed
    private val _followedShows = MutableStateFlow<Set<Int>>(emptySet())
    val followedShows: StateFlow<Set<Int>> = _followedShows.asStateFlow()

    // Map of TMDB ID to local database ID for followed shows
    private val _showIdMap = MutableStateFlow<Map<Int, Long>>(emptyMap())
    val showIdMap: StateFlow<Map<Int, Long>> = _showIdMap.asStateFlow()

    // Event for showing snackbar messages
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        setupSearchDebounce()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .filter { it.length >= 2 }
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    /**
     * Update the search query.
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _error.value = null

        // Clear results if query is empty
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
        }
    }

    /**
     * Perform the search manually (e.g., when user presses search button).
     */
    fun search() {
        val query = _searchQuery.value
        if (query.isNotEmpty()) {
            viewModelScope.launch {
                performSearch(query)
            }
        }
    }

    /**
     * Internal search implementation.
     */
    private suspend fun performSearch(query: String) {
        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }

        _isSearching.value = true
        _error.value = null

        val result = tmdbService.search(query)

        result.fold(
            onSuccess = { response ->
                _searchResults.value = response.results
                // Check which shows are already followed
                checkFollowedStatus(response.results.map { it.id })
            },
            onFailure = { exception ->
                _error.value = exception.message ?: "Search failed"
                _searchResults.value = emptyList()
            }
        )

        _isSearching.value = false
    }

    /**
     * Check which shows from the results are already followed.
     * Also builds a map of TMDB ID to local database ID for navigation.
     */
    private suspend fun checkFollowedStatus(tmdbIds: List<Int>) {
        val followed = mutableSetOf<Int>()
        val idMap = mutableMapOf<Int, Long>()
        for (tmdbId in tmdbIds) {
            val show = repository.getShow(tmdbId)
            if (show != null) {
                followed.add(tmdbId)
                idMap[tmdbId] = show.id
            }
        }
        _followedShows.value = followed
        _showIdMap.value = idMap
    }

    /**
     * Add a show to the followed list.
     * Returns true if successful.
     */
    suspend fun addShow(tmdbId: Int): Boolean {
        // Mark as adding
        _addingShows.update { it + tmdbId }

        val result = addShowUseCase.execute(tmdbId)

        // Remove from adding
        _addingShows.update { it - tmdbId }

        return result.fold(
            onSuccess = { show ->
                // Mark as followed and add to ID map
                _followedShows.update { it + tmdbId }
                _showIdMap.update { it + (tmdbId to show.id) }
                _snackbarMessage.value = "${show.title} added to your shows"
                true
            },
            onFailure = { exception ->
                _snackbarMessage.value = exception.message ?: "Failed to add show"
                false
            }
        )
    }

    /**
     * Add a show (fire-and-forget version for UI).
     */
    fun addShowAsync(tmdbId: Int) {
        viewModelScope.launch {
            addShow(tmdbId)
        }
    }

    /**
     * Check if a show is currently being followed.
     */
    fun isFollowed(tmdbId: Int): Boolean {
        return _followedShows.value.contains(tmdbId)
    }

    /**
     * Get the local database ID for a TMDB ID.
     * Returns null if the show is not followed.
     */
    fun getLocalShowId(tmdbId: Int): Long? {
        return _showIdMap.value[tmdbId]
    }

    /**
     * Check if a show is currently being added.
     */
    fun isAdding(tmdbId: Int): Boolean {
        return _addingShows.value.contains(tmdbId)
    }

    /**
     * Clear the snackbar message.
     */
    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    /**
     * Clear the error.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clear search and results.
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _error.value = null
    }
}
