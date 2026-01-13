package io.designtoswiftui.countdown2binge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.SeasonState
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.models.ShowStatus
import io.designtoswiftui.countdown2binge.services.RefreshService
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
    private val stateManager: SeasonStateManager,
    private val refreshService: RefreshService
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

    // Refreshing state for pull-to-refresh
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

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
     *
     * Logic:
     * - inProduction AND last episode aired → Anticipated (TBD - more seasons coming)
     * - Currently airing (some episodes future) → Airing Now
     * - Has premiere date in future → Premiering Soon
     * - NOT inProduction AND season complete → NOT in timeline (Binge Ready only)
     * - Cancelled/Ended → NOT in timeline (Binge Ready only)
     */
    private fun groupShowsByState(shows: List<TimelineShow>) {
        val airing = mutableListOf<TimelineShow>()
        val premiering = mutableListOf<TimelineShow>()
        val anticipated = mutableListOf<TimelineShow>()

        for (timelineShow in shows) {
            val show = timelineShow.show
            val season = timelineShow.season
            val isInProduction = show.status == ShowStatus.RETURNING || show.status == ShowStatus.IN_PRODUCTION

            // Skip shows that are ended/canceled - they only go to Binge Ready
            if (show.status == ShowStatus.ENDED || show.status == ShowStatus.CANCELED) {
                continue
            }

            when (season?.state) {
                SeasonState.AIRING -> {
                    // Currently airing - show in Airing Now
                    airing.add(timelineShow)
                }
                SeasonState.PREMIERING -> {
                    // Has future premiere date - show in Premiering Soon
                    premiering.add(timelineShow)
                }
                SeasonState.ANTICIPATED -> {
                    // No premiere date yet - show in Anticipated
                    anticipated.add(timelineShow)
                }
                SeasonState.BINGE_READY -> {
                    // Season is complete - if show is in production, it goes to Anticipated
                    // (waiting for next season), otherwise skip (Binge Ready only)
                    if (isInProduction) {
                        anticipated.add(timelineShow)
                    }
                }
                SeasonState.WATCHED -> {
                    // Already watched - if show is in production, it goes to Anticipated
                    // (waiting for next season)
                    if (isInProduction) {
                        anticipated.add(timelineShow)
                    }
                }
                null -> {
                    // No season data - if in production, show in Anticipated
                    if (isInProduction) {
                        anticipated.add(timelineShow)
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
     * Refresh the timeline data from local database.
     */
    fun refresh() {
        loadShows()
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
            loadShows()

            _isRefreshing.value = false
        }
    }
}
