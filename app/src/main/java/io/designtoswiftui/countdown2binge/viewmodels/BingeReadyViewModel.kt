package io.designtoswiftui.countdown2binge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.SeasonState
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.services.RefreshService
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import io.designtoswiftui.countdown2binge.services.settings.SettingsRepository
import io.designtoswiftui.countdown2binge.services.state.SeasonStateManager
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class representing a binge-ready season with its associated show.
 */
data class BingeReadySeason(
    val show: Show,
    val season: Season,
    val watchedCount: Int = 0,
    val totalEpisodes: Int = 0
) {
    val isFullyWatched: Boolean
        get() = watchedCount >= totalEpisodes && totalEpisodes > 0
}

/**
 * ViewModel for the Binge Ready screen.
 * Displays seasons that are complete and ready to binge.
 */
@HiltViewModel
class BingeReadyViewModel @Inject constructor(
    private val repository: ShowRepository,
    private val stateManager: SeasonStateManager,
    private val refreshService: RefreshService,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // Binge-ready seasons with their shows
    private val _bingeReadySeasons = MutableStateFlow<List<BingeReadySeason>>(emptyList())
    val bingeReadySeasons: StateFlow<List<BingeReadySeason>> = _bingeReadySeasons.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Whether the list is empty
    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> = _isEmpty.asStateFlow()

    // Refreshing state for pull-to-refresh
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        // Observe settings changes and reload when includeAiring changes
        viewModelScope.launch {
            settingsRepository.includeAiring.collectLatest {
                loadBingeReadySeasons()
            }
        }
    }

    /**
     * Load binge-ready seasons from the repository.
     */
    private fun loadBingeReadySeasons() {
        viewModelScope.launch {
            _isLoading.value = true

            val includeAiring = settingsRepository.includeAiring.first()
            repository.getBingeReadySeasons(includeAiring).collect { seasons ->
                if (seasons.isEmpty()) {
                    _isEmpty.value = true
                    _bingeReadySeasons.value = emptyList()
                    _isLoading.value = false
                    return@collect
                }

                _isEmpty.value = false

                // Fetch show data and watched count for each season
                val bingeReadyList = mutableListOf<BingeReadySeason>()
                for (season in seasons) {
                    val show = repository.getShowById(season.showId)
                    if (show != null) {
                        val watchedCount = repository.getWatchedEpisodeCount(season.id)
                        val episodes = repository.getEpisodesForSeasonSync(season.id)
                        bingeReadyList.add(
                            BingeReadySeason(
                                show = show,
                                season = season,
                                watchedCount = watchedCount,
                                totalEpisodes = episodes.size
                            )
                        )
                    }
                }

                // Sort by show title
                _bingeReadySeasons.value = bingeReadyList.sortedBy { it.show.title }
                _isLoading.value = false
            }
        }
    }

    /**
     * Refresh the binge-ready list from local database.
     */
    fun refresh() {
        loadBingeReadySeasons()
    }

    /**
     * Refresh all shows from TMDB network.
     * Used for pull-to-refresh.
     */
    fun refreshFromNetwork() {
        viewModelScope.launch {
            _isRefreshing.value = true

            // Refresh all shows from TMDB
            refreshService.refreshAllShows()

            // Reload local data
            loadBingeReadySeasons()

            _isRefreshing.value = false
        }
    }

    /**
     * Mark a season as watched (marks all episodes as watched).
     */
    fun markSeasonWatched(seasonId: Long) {
        viewModelScope.launch {
            try {
                // Mark all episodes as watched
                val episodes = repository.getEpisodesForSeasonSync(seasonId)
                for (episode in episodes) {
                    if (!episode.isWatched) {
                        repository.setEpisodeWatched(episode.id, true)
                    }
                }
                // Mark the season as watched
                repository.markSeasonWatched(seasonId)
                // List will auto-refresh via Flow collection
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }
    }

    /**
     * Unmark a season as watched (marks all episodes as unwatched).
     */
    fun unmarkSeasonWatched(seasonId: Long) {
        viewModelScope.launch {
            try {
                // Unmark all episodes
                val episodes = repository.getEpisodesForSeasonSync(seasonId)
                for (episode in episodes) {
                    if (episode.isWatched) {
                        repository.setEpisodeWatched(episode.id, false)
                    }
                }
                // Find the season to determine its proper state
                val season = _bingeReadySeasons.value.find { it.season.id == seasonId }?.season
                val newState = if (season != null) {
                    stateManager.determineState(
                        season.copy(watchedDate = null),
                        LocalDate.now()
                    )
                } else {
                    SeasonState.BINGE_READY
                }
                repository.unmarkSeasonWatched(seasonId, newState)
                // List will auto-refresh via Flow collection
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }
    }

    /**
     * Unfollow/remove a show entirely.
     */
    fun unfollowShow(showId: Long) {
        viewModelScope.launch {
            repository.deleteShow(showId)
            // List will auto-refresh via Flow collection
        }
    }
}
