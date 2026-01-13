package io.designtoswiftui.countdown2binge.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.countdown2binge.models.Episode
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Episode List screen.
 * Displays episodes for a season with watched status.
 */
@HiltViewModel
class EpisodeListViewModel @Inject constructor(
    private val repository: ShowRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Season ID from navigation argument
    private val seasonId: Long = savedStateHandle.get<Long>("seasonId") ?: 0L

    // The show this season belongs to
    private val _show = MutableStateFlow<Show?>(null)
    val show: StateFlow<Show?> = _show.asStateFlow()

    // The season being displayed
    private val _season = MutableStateFlow<Season?>(null)
    val season: StateFlow<Season?> = _season.asStateFlow()

    // Episodes for this season
    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    val episodes: StateFlow<List<Episode>> = _episodes.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Progress: watched count / total count
    private val _watchedCount = MutableStateFlow(0)
    val watchedCount: StateFlow<Int> = _watchedCount.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        if (seasonId > 0) {
            loadEpisodes()
        } else {
            _isLoading.value = false
            _error.value = "Invalid season ID"
        }
    }

    /**
     * Load episodes by season ID from SavedStateHandle.
     */
    fun loadEpisodesBySeasonId(seasonId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Load episodes with flow collection for real-time updates
                repository.getEpisodesForSeason(seasonId).collect { episodeList ->
                    _episodes.value = episodeList
                    _watchedCount.value = episodeList.count { it.isWatched }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load episodes"
                _isLoading.value = false
            }
        }
    }

    /**
     * Load season and show info, then episodes.
     */
    private fun loadEpisodes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // First get all seasons to find our season
                val allShows = repository.getAllShows()
                allShows.collect { shows ->
                    for (show in shows) {
                        val seasons = repository.getSeasonsForShowSync(show.id)
                        val matchingSeason = seasons.find { it.id == seasonId }
                        if (matchingSeason != null) {
                            _show.value = show
                            _season.value = matchingSeason
                            break
                        }
                    }

                    // Now load episodes
                    loadEpisodesBySeasonId(seasonId)
                    return@collect
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load data"
                _isLoading.value = false
            }
        }
    }

    /**
     * Toggle the watched state of an episode.
     */
    fun toggleEpisodeWatched(episodeId: Long) {
        viewModelScope.launch {
            try {
                val episode = _episodes.value.find { it.id == episodeId }
                if (episode != null) {
                    val newWatchedState = !episode.isWatched
                    repository.setEpisodeWatched(episodeId, newWatchedState)

                    // Update local state immediately for responsive UI
                    _episodes.value = _episodes.value.map {
                        if (it.id == episodeId) it.copy(isWatched = newWatchedState) else it
                    }
                    _watchedCount.value = _episodes.value.count { it.isWatched }
                }
            } catch (e: Exception) {
                _error.value = "Failed to update episode: ${e.message}"
            }
        }
    }

    /**
     * Mark all episodes as watched.
     */
    fun markAllWatched() {
        viewModelScope.launch {
            try {
                for (episode in _episodes.value) {
                    if (!episode.isWatched) {
                        repository.setEpisodeWatched(episode.id, true)
                    }
                }
                // Update local state
                _episodes.value = _episodes.value.map { it.copy(isWatched = true) }
                _watchedCount.value = _episodes.value.size
            } catch (e: Exception) {
                _error.value = "Failed to mark all as watched: ${e.message}"
            }
        }
    }

    /**
     * Unmark all episodes as watched.
     */
    fun unmarkAllWatched() {
        viewModelScope.launch {
            try {
                for (episode in _episodes.value) {
                    if (episode.isWatched) {
                        repository.setEpisodeWatched(episode.id, false)
                    }
                }
                // Update local state
                _episodes.value = _episodes.value.map { it.copy(isWatched = false) }
                _watchedCount.value = 0
            } catch (e: Exception) {
                _error.value = "Failed to unmark all: ${e.message}"
            }
        }
    }

    /**
     * Refresh the episode list.
     */
    fun refresh() {
        if (seasonId > 0) {
            loadEpisodes()
        }
    }
}
