package io.designtoswiftui.countdown2binge.viewmodels

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Data class representing a show with its most relevant season for the timeline.
 */
data class TimelineShow(
    val show: Show,
    val season: Season?,
    val daysUntilPremiere: Int?,
    val daysUntilFinale: Int?,
    val episodesRemaining: Int?
)

/**
 * ViewModel for the Timeline screen.
 * Groups shows by their current state for display.
 */
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: ShowRepository,
    private val stateManager: SeasonStateManager
) : ViewModel() {

    // Shows currently airing
    private val _airingShows = MutableStateFlow<List<TimelineShow>>(emptyList())
    val airingShows: StateFlow<List<TimelineShow>> = _airingShows.asStateFlow()

    // Shows premiering soon
    private val _premieringShows = MutableStateFlow<List<TimelineShow>>(emptyList())
    val premieringShows: StateFlow<List<TimelineShow>> = _premieringShows.asStateFlow()

    // Shows with anticipated/unknown dates
    private val _anticipatedShows = MutableStateFlow<List<TimelineShow>>(emptyList())
    val anticipatedShows: StateFlow<List<TimelineShow>> = _anticipatedShows.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Whether there are any shows at all
    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> = _isEmpty.asStateFlow()

    init {
        loadShows()
    }

    /**
     * Load and group shows from the repository.
     */
    private fun loadShows() {
        viewModelScope.launch {
            _isLoading.value = true

            // Collect timeline shows (returning/in production)
            repository.getTimelineShows().collect { shows ->
                if (shows.isEmpty()) {
                    _isEmpty.value = true
                    _airingShows.value = emptyList()
                    _premieringShows.value = emptyList()
                    _anticipatedShows.value = emptyList()
                    _isLoading.value = false
                    return@collect
                }

                _isEmpty.value = false
                val today = LocalDate.now()

                // Process each show with its seasons
                val timelineShows = mutableListOf<TimelineShow>()

                for (show in shows) {
                    val seasons = repository.getSeasonsForShowSync(show.id)

                    // Find the most relevant season (latest non-watched)
                    val relevantSeason = seasons
                        .filter { it.state != SeasonState.WATCHED }
                        .maxByOrNull { it.seasonNumber }
                        ?: seasons.maxByOrNull { it.seasonNumber }

                    val timelineShow = TimelineShow(
                        show = show,
                        season = relevantSeason,
                        daysUntilPremiere = relevantSeason?.let { stateManager.daysUntilPremiere(it, today) },
                        daysUntilFinale = relevantSeason?.let { stateManager.daysUntilFinale(it, today) },
                        episodesRemaining = relevantSeason?.let { stateManager.episodesRemaining(it) }
                    )
                    timelineShows.add(timelineShow)
                }

                // Group by state
                groupShowsByState(timelineShows)
                _isLoading.value = false
            }
        }
    }

    /**
     * Group timeline shows by their season state.
     */
    private fun groupShowsByState(shows: List<TimelineShow>) {
        val airing = mutableListOf<TimelineShow>()
        val premiering = mutableListOf<TimelineShow>()
        val anticipated = mutableListOf<TimelineShow>()

        for (show in shows) {
            when (show.season?.state) {
                SeasonState.AIRING -> airing.add(show)
                SeasonState.PREMIERING -> premiering.add(show)
                SeasonState.ANTICIPATED -> anticipated.add(show)
                SeasonState.BINGE_READY, SeasonState.WATCHED, null -> {
                    // These don't appear in timeline, but if there's no relevant season,
                    // add to anticipated
                    if (show.season == null) {
                        anticipated.add(show)
                    }
                }
            }
        }

        // Sort by countdown
        _airingShows.value = airing.sortedBy { it.daysUntilFinale ?: Int.MAX_VALUE }
        _premieringShows.value = premiering.sortedBy { it.daysUntilPremiere ?: Int.MAX_VALUE }
        _anticipatedShows.value = anticipated.sortedBy { it.show.title }
    }

    /**
     * Refresh the timeline data.
     */
    fun refresh() {
        loadShows()
    }
}
