package io.designtoswiftui.countdown2binge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.countdown2binge.models.GenreMapping
import io.designtoswiftui.countdown2binge.models.ShowCategory
import io.designtoswiftui.countdown2binge.models.TrendingShowDisplay
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.usecases.AddShowUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Genre List screen.
 * Handles loading shows by genre with pagination.
 */
@HiltViewModel
class GenreListViewModel @Inject constructor(
    private val tmdbService: TMDBService,
    private val addShowUseCase: AddShowUseCase,
    private val repository: ShowRepository
) : ViewModel() {

    // Shows list
    private val _shows = MutableStateFlow<List<TrendingShowDisplay>>(emptyList())
    val shows: StateFlow<List<TrendingShowDisplay>> = _shows.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Loading more (pagination)
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    // Set of TMDB IDs that are currently being added
    private val _addingShows = MutableStateFlow<Set<Int>>(emptySet())
    val addingShows: StateFlow<Set<Int>> = _addingShows.asStateFlow()

    // Set of TMDB IDs that are already followed
    private val _followedShows = MutableStateFlow<Set<Int>>(emptySet())
    val followedShows: StateFlow<Set<Int>> = _followedShows.asStateFlow()

    // Map of TMDB ID to local database ID for followed shows
    private val _showIdMap = MutableStateFlow<Map<Int, Long>>(emptyMap())
    val showIdMap: StateFlow<Map<Int, Long>> = _showIdMap.asStateFlow()

    // Snackbar message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Pagination state
    private var currentPage = 1
    private var totalPages = 1
    private var currentCategory: ShowCategory? = null

    /**
     * Load shows for a category.
     */
    fun loadShows(category: ShowCategory) {
        if (currentCategory == category && _shows.value.isNotEmpty()) {
            // Already loaded this category
            return
        }

        currentCategory = category
        currentPage = 1
        _shows.value = emptyList()

        viewModelScope.launch {
            _isLoading.value = true
            loadPage(category, 1)
            _isLoading.value = false
        }
    }

    /**
     * Load more shows (pagination).
     */
    fun loadMore() {
        val category = currentCategory ?: return

        if (_isLoadingMore.value || currentPage >= totalPages) {
            return
        }

        viewModelScope.launch {
            _isLoadingMore.value = true
            loadPage(category, currentPage + 1)
            _isLoadingMore.value = false
        }
    }

    /**
     * Load a specific page.
     */
    private suspend fun loadPage(category: ShowCategory, page: Int) {
        val result = tmdbService.getShowsByGenre(category.genreIds, page)

        result.fold(
            onSuccess = { response ->
                totalPages = response.totalPages
                currentPage = page

                val showDisplays = response.results.map { show ->
                    viewModelScope.async {
                        val logoPath = tmdbService.getEnglishLogoPath(show.id)
                        val genreNames = GenreMapping.getGenreNames(show.genreIds ?: emptyList())
                        TrendingShowDisplay(
                            show = show,
                            logoPath = logoPath,
                            genreNames = genreNames,
                            seasonNumber = 1
                        )
                    }
                }.awaitAll()

                if (page == 1) {
                    _shows.value = showDisplays
                } else {
                    _shows.value = _shows.value + showDisplays
                }

                // Check followed status
                checkFollowedStatus(response.results.map { it.id })
            },
            onFailure = {
                // Handle error silently
            }
        )
    }

    /**
     * Check which shows are already followed.
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
        _followedShows.update { it + followed }
        _showIdMap.update { it + idMap }
    }

    /**
     * Add a show to followed list.
     */
    fun addShowAsync(tmdbId: Int) {
        viewModelScope.launch {
            _addingShows.update { it + tmdbId }

            val result = addShowUseCase.execute(tmdbId)

            _addingShows.update { it - tmdbId }

            result.fold(
                onSuccess = { show ->
                    _followedShows.update { it + tmdbId }
                    _showIdMap.update { it + (tmdbId to show.id) }
                    _snackbarMessage.value = "${show.title} added to your shows"
                },
                onFailure = { exception ->
                    _snackbarMessage.value = exception.message ?: "Failed to add show"
                }
            )
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
     * Toggle follow status.
     */
    fun toggleFollow(tmdbId: Int) {
        if (_followedShows.value.contains(tmdbId)) {
            removeShow(tmdbId)
        } else {
            addShowAsync(tmdbId)
        }
    }

    /**
     * Clear snackbar message.
     */
    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    /**
     * Check if there are more pages to load.
     */
    fun hasMore(): Boolean = currentPage < totalPages
}
