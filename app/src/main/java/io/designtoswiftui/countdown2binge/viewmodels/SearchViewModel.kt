package io.designtoswiftui.countdown2binge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.countdown2binge.models.AiringShowDisplay
import io.designtoswiftui.countdown2binge.models.GenreMapping
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.models.TrendingShowDisplay
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBSearchResult
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.usecases.AddShowUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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

    // Brief loading state for quick add (minimal blocking)
    // With background fetching, this is now very fast
    private val _isAddingShowGlobal = MutableStateFlow(false)
    val isAddingShowGlobal: StateFlow<Boolean> = _isAddingShowGlobal.asStateFlow()

    // Name of show currently being added (for brief loading indicator)
    private val _addingShowName = MutableStateFlow<String?>(null)
    val addingShowName: StateFlow<String?> = _addingShowName.asStateFlow()

    // Set of TMDB IDs that are already followed
    private val _followedShows = MutableStateFlow<Set<Int>>(emptySet())
    val followedShows: StateFlow<Set<Int>> = _followedShows.asStateFlow()

    // Map of TMDB ID to local database ID for followed shows
    private val _showIdMap = MutableStateFlow<Map<Int, Long>>(emptyMap())
    val showIdMap: StateFlow<Map<Int, Long>> = _showIdMap.asStateFlow()

    // Event for showing snackbar messages
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Trending shows with logos
    private val _trendingShows = MutableStateFlow<List<TrendingShowDisplay>>(emptyList())
    val trendingShows: StateFlow<List<TrendingShowDisplay>> = _trendingShows.asStateFlow()

    // Airing/ending soon shows with days left
    private val _airingShows = MutableStateFlow<List<AiringShowDisplay>>(emptyList())
    val airingShows: StateFlow<List<AiringShowDisplay>> = _airingShows.asStateFlow()

    // Loading states for landing sections
    private val _isTrendingLoading = MutableStateFlow(false)
    val isTrendingLoading: StateFlow<Boolean> = _isTrendingLoading.asStateFlow()

    private val _isAiringLoading = MutableStateFlow(false)
    val isAiringLoading: StateFlow<Boolean> = _isAiringLoading.asStateFlow()

    // Pagination state for future "See All" screens
    private var airingShowsPage: Int = 1
    private var airingShowsTotalPages: Int = 1

    init {
        setupSearchDebounce()
        loadLandingContent()
    }

    /**
     * Load trending and airing shows for the landing state.
     */
    private fun loadLandingContent() {
        viewModelScope.launch {
            // Load both concurrently
            launch { loadTrendingShows() }
            launch { loadAiringShows() }
        }
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
     * Shows with many seasons (SVU, Grey's) can take a while - blocks all UI.
     */
    suspend fun addShow(tmdbId: Int, showName: String? = null): Boolean {
        // Block all interactions while adding
        _isAddingShowGlobal.value = true
        _addingShowName.value = showName ?: "show"
        _addingShows.update { it + tmdbId }

        val result = addShowUseCase.execute(tmdbId)

        // Remove from adding and unblock
        _addingShows.update { it - tmdbId }
        _isAddingShowGlobal.value = false
        _addingShowName.value = null

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
    fun addShowAsync(tmdbId: Int, showName: String? = null) {
        // Don't allow adding if already adding another show
        if (_isAddingShowGlobal.value) return

        viewModelScope.launch {
            addShow(tmdbId, showName)
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

    /**
     * Load trending shows with logos.
     * Fetches first 10 shows and their English logos in parallel.
     */
    private suspend fun loadTrendingShows() {
        _isTrendingLoading.value = true

        val result = tmdbService.getTrending()

        result.fold(
            onSuccess = { response ->
                val shows = response.results.take(10)

                // Fetch logos in parallel for all shows
                val showsWithLogos = shows.map { show ->
                    viewModelScope.async {
                        val logoPath = tmdbService.getEnglishLogoPath(show.id)
                        val genreNames = GenreMapping.getGenreNames(show.genreIds ?: emptyList())
                        TrendingShowDisplay(
                            show = show,
                            logoPath = logoPath,
                            genreNames = genreNames,
                            seasonNumber = 1 // Default to S1, could fetch if needed
                        )
                    }
                }.awaitAll()

                _trendingShows.value = showsWithLogos

                // Check followed status for trending shows
                checkFollowedStatus(shows.map { it.id })
            },
            onFailure = {
                // Silently fail, landing will show empty state
            }
        )

        _isTrendingLoading.value = false
    }

    /**
     * Load airing/ending soon shows with days-left calculation.
     * Fetches first ~10 shows, calculates days left for first 3.
     */
    private suspend fun loadAiringShows() {
        _isAiringLoading.value = true

        val result = tmdbService.getOnTheAir()

        result.fold(
            onSuccess = { response ->
                airingShowsTotalPages = response.totalPages
                val shows = response.results.take(10)

                // Calculate days left for first 3 shows only (API intensive)
                val showsWithDays = shows.mapIndexed { index, show ->
                    viewModelScope.async {
                        val daysLeft = if (index < 3) {
                            calculateDaysLeft(show.id)
                        } else {
                            null
                        }
                        val genreNames = GenreMapping.getGenreNames(show.genreIds ?: emptyList())
                        AiringShowDisplay(
                            show = show,
                            daysLeft = daysLeft,
                            genreNames = genreNames
                        )
                    }
                }.awaitAll()

                _airingShows.value = showsWithDays

                // Check followed status for airing shows
                checkFollowedStatus(shows.map { it.id })
            },
            onFailure = {
                // Silently fail
            }
        )

        _isAiringLoading.value = false
    }

    /**
     * Calculate days until the season finale for a show.
     * Returns null if unable to determine.
     */
    private suspend fun calculateDaysLeft(showId: Int): Int? {
        return try {
            val detailsResult = tmdbService.getShowDetails(showId)
            detailsResult.getOrNull()?.let { details ->
                // Find the current airing season
                val currentSeason = details.seasons
                    ?.filter { it.seasonNumber > 0 } // Exclude specials
                    ?.maxByOrNull { it.seasonNumber }

                currentSeason?.let { season ->
                    // Get full season details to find finale date
                    val seasonResult = tmdbService.getSeasonDetails(showId, season.seasonNumber)
                    seasonResult.getOrNull()?.episodes?.lastOrNull()?.airDate?.let { finaleDate ->
                        parseDateAndCalculateDays(finaleDate)
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse a date string and calculate days from today.
     */
    private fun parseDateAndCalculateDays(dateString: String): Int? {
        return try {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val finaleDate = LocalDate.parse(dateString, formatter)
            val today = LocalDate.now()
            val days = ChronoUnit.DAYS.between(today, finaleDate).toInt()
            if (days >= 0) days else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Remove a show from followed list.
     */
    fun removeShow(tmdbId: Int) {
        viewModelScope.launch {
            val show = repository.getShow(tmdbId)
            if (show != null) {
                repository.delete(show)
                _followedShows.update { it - tmdbId }
                _showIdMap.update { it - tmdbId }
                _snackbarMessage.value = "${show.title} removed from your shows"
            }
        }
    }

    /**
     * Toggle follow status for a show.
     */
    fun toggleFollow(tmdbId: Int, showName: String? = null) {
        if (isFollowed(tmdbId)) {
            removeShow(tmdbId)
        } else {
            addShowAsync(tmdbId, showName)
        }
    }

    /**
     * Refresh landing content.
     */
    fun refreshLandingContent() {
        loadLandingContent()
    }
}
