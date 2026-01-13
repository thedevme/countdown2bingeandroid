package io.designtoswiftui.countdown2binge.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.SeasonState
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import io.designtoswiftui.countdown2binge.services.state.SeasonStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Data class representing a season with computed countdown values.
 */
data class SeasonDetail(
    val season: Season,
    val daysUntilPremiere: Int?,
    val daysUntilFinale: Int?,
    val episodesRemaining: Int?
)

/**
 * ViewModel for the Show Detail screen.
 * Loads and displays a single show with its seasons.
 */
@HiltViewModel
class ShowDetailViewModel @Inject constructor(
    private val repository: ShowRepository,
    private val stateManager: SeasonStateManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Show ID from navigation argument
    private val showId: Long = savedStateHandle.get<Long>("showId") ?: 0L

    // The show being displayed
    private val _show = MutableStateFlow<Show?>(null)
    val show: StateFlow<Show?> = _show.asStateFlow()

    // Seasons for this show with countdown info
    private val _seasons = MutableStateFlow<List<SeasonDetail>>(emptyList())
    val seasons: StateFlow<List<SeasonDetail>> = _seasons.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        if (showId > 0) {
            loadShow()
        } else {
            _isLoading.value = false
            _error.value = "Invalid show ID"
        }
    }

    /**
     * Load the show by ID.
     */
    fun loadShowById(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val loadedShow = repository.getShowById(id)
                if (loadedShow != null) {
                    _show.value = loadedShow
                    loadSeasons(id)
                } else {
                    _error.value = "Show not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load show"
            }

            _isLoading.value = false
        }
    }

    /**
     * Load show from the saved state handle ID.
     */
    private fun loadShow() {
        loadShowById(showId)
    }

    /**
     * Load seasons for the show.
     */
    private suspend fun loadSeasons(showId: Long) {
        val today = LocalDate.now()
        val seasonsList = repository.getSeasonsForShowSync(showId)

        val seasonDetails = seasonsList.map { season ->
            SeasonDetail(
                season = season,
                daysUntilPremiere = stateManager.daysUntilPremiere(season, today),
                daysUntilFinale = stateManager.daysUntilFinale(season, today),
                episodesRemaining = stateManager.episodesRemaining(season)
            )
        }.sortedByDescending { it.season.seasonNumber }

        _seasons.value = seasonDetails
    }

    /**
     * Refresh the show data.
     */
    fun refresh() {
        if (showId > 0) {
            loadShow()
        }
    }

    /**
     * Mark a season as watched.
     */
    fun markSeasonWatched(seasonId: Long) {
        viewModelScope.launch {
            try {
                repository.markSeasonWatched(seasonId)
                // Reload seasons to reflect the change
                _show.value?.let { loadSeasons(it.id) }
            } catch (e: Exception) {
                _error.value = "Failed to mark as watched: ${e.message}"
            }
        }
    }

    /**
     * Unmark a season as watched (revert to appropriate state).
     */
    fun unmarkSeasonWatched(seasonId: Long) {
        viewModelScope.launch {
            try {
                // Find the season to determine its proper state
                val seasonDetail = _seasons.value.find { it.season.id == seasonId }
                val season = seasonDetail?.season

                // Determine the new state based on dates
                val newState = if (season != null) {
                    stateManager.determineState(
                        season.copy(watchedDate = null),
                        LocalDate.now()
                    )
                } else {
                    SeasonState.BINGE_READY
                }

                repository.unmarkSeasonWatched(seasonId, newState)
                // Reload seasons to reflect the change
                _show.value?.let { loadSeasons(it.id) }
            } catch (e: Exception) {
                _error.value = "Failed to unmark as watched: ${e.message}"
            }
        }
    }

    /**
     * Toggle the watched state of a season.
     */
    fun toggleSeasonWatched(seasonId: Long) {
        val seasonDetail = _seasons.value.find { it.season.id == seasonId }
        if (seasonDetail?.season?.state == SeasonState.WATCHED) {
            unmarkSeasonWatched(seasonId)
        } else {
            markSeasonWatched(seasonId)
        }
    }
}
