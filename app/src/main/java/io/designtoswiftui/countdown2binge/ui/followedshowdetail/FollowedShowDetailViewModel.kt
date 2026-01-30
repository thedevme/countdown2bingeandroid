package io.designtoswiftui.countdown2binge.ui.followedshowdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.countdown2binge.models.Episode
import io.designtoswiftui.countdown2binge.models.Franchise
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.SeasonState
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.models.ShowStatus
import io.designtoswiftui.countdown2binge.services.firebase.FranchiseService
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import io.designtoswiftui.countdown2binge.services.state.SeasonStateManager
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBCastMember
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBCrewMember
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBSeasonSummary
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBShowDetails
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBVideo
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBWatchProvider
import io.designtoswiftui.countdown2binge.viewmodels.SpinoffShowDisplay
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Tab options for the Followed Show Detail screen.
 */
enum class FollowedShowTab(val displayName: String) {
    BINGE("Binge View"),
    SPINOFFS("Spinoffs"),
    INFO("Info")
}

/**
 * Data class for ranked season display.
 */
data class RankedSeason(
    val season: TMDBSeasonSummary,
    val rank: Int,
    val year: Int?,
    val descriptor: String,
    val rankLabel: String
) {
    companion object {
        fun descriptorForRank(rank: Int): String = when (rank) {
            1 -> "Masterpiece"
            2 -> "High Fidelity"
            3 -> "Origins"
            else -> "Classic"
        }

        fun labelForRank(rank: Int): String = when (rank) {
            1 -> "RATED BEST SEASON"
            2 -> "SILVER RANKING"
            3 -> "BRONZE RANKING"
            else -> "RANKING #$rank"
        }
    }
}

/**
 * Countdown state for finale timer.
 */
data class CountdownState(
    val days: Int? = null,
    val hours: Int? = null,
    val minutes: Int? = null,
    val seconds: Int? = null,
    val finaleEpisode: Episode? = null,
    val currentSeason: Season? = null,
    val eventLabel: String = "SEASON FINALE"
)

/**
 * ViewModel for the Followed Show Detail screen.
 * Manages tab state, countdown timer, and season rankings.
 */
@HiltViewModel
class FollowedShowDetailViewModel @Inject constructor(
    private val repository: ShowRepository,
    private val stateManager: SeasonStateManager,
    private val tmdbService: TMDBService,
    private val franchiseService: FranchiseService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val showId: Long = savedStateHandle.get<Long>("showId") ?: 0L

    // Core show data
    private val _show = MutableStateFlow<Show?>(null)
    val show: StateFlow<Show?> = _show.asStateFlow()

    private val _showDetails = MutableStateFlow<TMDBShowDetails?>(null)
    val showDetails: StateFlow<TMDBShowDetails?> = _showDetails.asStateFlow()

    // Tab state
    private val _selectedTab = MutableStateFlow(FollowedShowTab.BINGE)
    val selectedTab: StateFlow<FollowedShowTab> = _selectedTab.asStateFlow()

    private val _availableTabs = MutableStateFlow<List<FollowedShowTab>>(listOf(FollowedShowTab.BINGE, FollowedShowTab.INFO))
    val availableTabs: StateFlow<List<FollowedShowTab>> = _availableTabs.asStateFlow()

    // Countdown state
    private val _countdownState = MutableStateFlow(CountdownState())
    val countdownState: StateFlow<CountdownState> = _countdownState.asStateFlow()

    private val _showCountdown = MutableStateFlow(false)
    val showCountdown: StateFlow<Boolean> = _showCountdown.asStateFlow()

    // Season rankings
    private val _rankedSeasons = MutableStateFlow<List<RankedSeason>>(emptyList())
    val rankedSeasons: StateFlow<List<RankedSeason>> = _rankedSeasons.asStateFlow()

    private val _showRankings = MutableStateFlow(false)
    val showRankings: StateFlow<Boolean> = _showRankings.asStateFlow()

    // Spinoffs
    private val _franchise = MutableStateFlow<Franchise?>(null)
    val franchise: StateFlow<Franchise?> = _franchise.asStateFlow()

    private val _spinoffShows = MutableStateFlow<List<SpinoffShowDisplay>>(emptyList())
    val spinoffShows: StateFlow<List<SpinoffShowDisplay>> = _spinoffShows.asStateFlow()

    private val _isLoadingSpinoffs = MutableStateFlow(false)
    val isLoadingSpinoffs: StateFlow<Boolean> = _isLoadingSpinoffs.asStateFlow()

    private val _hasFranchise = MutableStateFlow(false)
    val hasFranchise: StateFlow<Boolean> = _hasFranchise.asStateFlow()

    // Info tab content
    private val _videos = MutableStateFlow<List<TMDBVideo>>(emptyList())
    val videos: StateFlow<List<TMDBVideo>> = _videos.asStateFlow()

    private val _cast = MutableStateFlow<List<TMDBCastMember>>(emptyList())
    val cast: StateFlow<List<TMDBCastMember>> = _cast.asStateFlow()

    private val _crew = MutableStateFlow<List<TMDBCrewMember>>(emptyList())
    val crew: StateFlow<List<TMDBCrewMember>> = _crew.asStateFlow()

    private val _isSynopsisExpanded = MutableStateFlow(false)
    val isSynopsisExpanded: StateFlow<Boolean> = _isSynopsisExpanded.asStateFlow()

    // Logo
    private val _logoPath = MutableStateFlow<String?>(null)
    val logoPath: StateFlow<String?> = _logoPath.asStateFlow()

    // Streaming providers
    private val _watchProviders = MutableStateFlow<List<TMDBWatchProvider>>(emptyList())
    val watchProviders: StateFlow<List<TMDBWatchProvider>> = _watchProviders.asStateFlow()

    // Loading states
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Local seasons for countdown and Info tab
    private val _localSeasons = MutableStateFlow<List<Season>>(emptyList())
    val localSeasons: StateFlow<List<Season>> = _localSeasons.asStateFlow()

    // Selected season for Info tab episode list
    private val _selectedSeasonNumber = MutableStateFlow(1)
    val selectedSeasonNumber: StateFlow<Int> = _selectedSeasonNumber.asStateFlow()

    // Episodes for selected season
    private val _selectedSeasonEpisodes = MutableStateFlow<List<Episode>>(emptyList())
    val selectedSeasonEpisodes: StateFlow<List<Episode>> = _selectedSeasonEpisodes.asStateFlow()

    // Countdown timer job
    private var countdownJob: Job? = null

    // Computed properties
    val statusText: String
        get() {
            val status = _showDetails.value?.status ?: return ""
            return when (status.lowercase()) {
                "returning series" -> "Returning Series"
                "ended" -> "Ended"
                "canceled" -> "Cancelled"
                "in production" -> "In Production"
                "planned" -> "Planned"
                "pilot" -> "Pilot"
                else -> status
            }
        }

    val seasonCount: Int
        get() = _showDetails.value?.numberOfSeasons ?: 0

    val episodeCount: Int
        get() = _showDetails.value?.numberOfEpisodes ?: 0

    val rating: Double?
        get() = _showDetails.value?.voteAverage

    val networkName: String?
        get() = _showDetails.value?.networks?.firstOrNull()?.name

    init {
        if (showId > 0) {
            loadFollowedShow()
        } else {
            _isLoading.value = false
            _error.value = "Invalid show ID"
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }

    // region Loading

    private fun loadFollowedShow() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val loadedShow = repository.getShowById(showId)
                if (loadedShow != null) {
                    _show.value = loadedShow

                    // Load local seasons for countdown and Info tab
                    val seasons = repository.getSeasonsForShowSync(showId)
                    _localSeasons.value = seasons

                    // Set default selected season to the latest regular season
                    val latestSeason = seasons
                        .filter { it.seasonNumber > 0 }
                        .maxByOrNull { it.seasonNumber }
                    if (latestSeason != null) {
                        _selectedSeasonNumber.value = latestSeason.seasonNumber
                        loadEpisodesForSelectedSeason()
                    }

                    // Fetch TMDB data
                    loadShowFromTmdb(loadedShow.tmdbId)
                } else {
                    _error.value = "Show not found"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load show"
                _isLoading.value = false
            }
        }
    }

    private fun loadShowFromTmdb(showTmdbId: Int) {
        viewModelScope.launch {
            try {
                val detailsResult = tmdbService.getShowDetails(showTmdbId)
                if (detailsResult.isSuccess) {
                    val details = detailsResult.getOrThrow()
                    _showDetails.value = details

                    // Process seasons for rankings
                    processSeasonRankings(details)

                    // Check if countdown should show
                    setupCountdown(details)

                    // Load additional content in parallel
                    loadAdditionalContent(showTmdbId)

                    // Load franchise data
                    loadFranchiseData(showTmdbId)

                    // Load logo
                    loadLogo(showTmdbId)
                } else {
                    _error.value = "Failed to load show details"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load show"
            }

            _isLoading.value = false
        }
    }

    private fun processSeasonRankings(details: TMDBShowDetails) {
        val seasons = details.seasons ?: return

        // Filter: seasonNumber > 0, must have voteAverage
        val regularSeasons = seasons
            .filter { it.seasonNumber > 0 && it.voteAverage != null && it.voteAverage > 0 }
            .sortedByDescending { it.voteAverage }
            .take(3)

        // Only show rankings if more than 1 season
        _showRankings.value = seasons.filter { it.seasonNumber > 0 }.size > 1 && regularSeasons.isNotEmpty()

        _rankedSeasons.value = regularSeasons.mapIndexed { index, season ->
            val rank = index + 1
            val year = season.airDate?.take(4)?.toIntOrNull()
            RankedSeason(
                season = season,
                rank = rank,
                year = year,
                descriptor = RankedSeason.descriptorForRank(rank),
                rankLabel = RankedSeason.labelForRank(rank)
            )
        }
    }

    private fun setupCountdown(details: TMDBShowDetails) {
        val inProduction = details.inProduction == true
        val currentSeason = _localSeasons.value
            .filter { it.seasonNumber > 0 }
            .maxByOrNull { it.seasonNumber }

        _showCountdown.value = inProduction && currentSeason != null

        if (_showCountdown.value && currentSeason != null) {
            // Determine event label
            val eventLabel = when {
                details.status?.lowercase() == "ended" ||
                details.status?.lowercase() == "canceled" -> "SERIES COMPLETE"

                currentSeason.seasonNumber == details.numberOfSeasons &&
                details.status?.lowercase() != "returning series" -> "SERIES CONCLUSION"

                else -> "SEASON FINALE"
            }

            _countdownState.value = CountdownState(
                currentSeason = currentSeason,
                eventLabel = eventLabel
            )

            // Start countdown timer
            startCountdownTimer(currentSeason.finaleDate)
        }
    }

    private fun startCountdownTimer(finaleDate: LocalDate?) {
        countdownJob?.cancel()

        if (finaleDate == null) {
            _countdownState.value = _countdownState.value.copy(
                days = null,
                hours = null,
                minutes = null,
                seconds = null
            )
            return
        }

        countdownJob = viewModelScope.launch {
            while (isActive) {
                val now = LocalDateTime.now()
                val finaleDateTime = finaleDate.atStartOfDay()

                if (now.isAfter(finaleDateTime)) {
                    _countdownState.value = _countdownState.value.copy(
                        days = 0,
                        hours = 0,
                        minutes = 0,
                        seconds = 0
                    )
                    break
                }

                val totalSeconds = ChronoUnit.SECONDS.between(now, finaleDateTime)
                val days = (totalSeconds / 86400).toInt()
                val hours = ((totalSeconds % 86400) / 3600).toInt()
                val minutes = ((totalSeconds % 3600) / 60).toInt()
                val seconds = (totalSeconds % 60).toInt()

                _countdownState.value = _countdownState.value.copy(
                    days = days,
                    hours = hours,
                    minutes = minutes,
                    seconds = seconds
                )

                delay(1000)
            }
        }
    }

    private fun loadAdditionalContent(showTmdbId: Int) {
        viewModelScope.launch {
            val videosDeferred = async {
                tmdbService.getShowVideos(showTmdbId).getOrNull()?.results
                    ?.filter { it.site == "YouTube" }
                    ?.take(6)
                    ?: emptyList()
            }
            val creditsDeferred = async {
                tmdbService.getShowCredits(showTmdbId).getOrNull()
            }
            val providersDeferred = async {
                tmdbService.getWatchProviders(showTmdbId).getOrNull() ?: emptyList()
            }

            _videos.value = videosDeferred.await()
            creditsDeferred.await()?.let { credits ->
                _cast.value = credits.cast.take(10)
                _crew.value = credits.crew.filter { it.job == "Creator" || it.job == "Executive Producer" }
            }
            _watchProviders.value = providersDeferred.await()
        }
    }

    private fun loadFranchiseData(showTmdbId: Int) {
        viewModelScope.launch {
            franchiseService.fetchFranchises()
            val foundFranchise = franchiseService.getFranchise(forShowId = showTmdbId)
            _franchise.value = foundFranchise
            _hasFranchise.value = foundFranchise != null

            // Update available tabs
            updateAvailableTabs()
        }
    }

    private fun updateAvailableTabs() {
        val tabs = mutableListOf(FollowedShowTab.BINGE)
        if (_hasFranchise.value) {
            tabs.add(FollowedShowTab.SPINOFFS)
        }
        tabs.add(FollowedShowTab.INFO)
        _availableTabs.value = tabs
    }

    private fun loadLogo(showTmdbId: Int) {
        viewModelScope.launch {
            _logoPath.value = tmdbService.getEnglishLogoPath(showTmdbId)
        }
    }

    fun loadSpinoffDetails() {
        val currentFranchise = _franchise.value ?: return
        if (_spinoffShows.value.isNotEmpty()) return

        viewModelScope.launch {
            _isLoadingSpinoffs.value = true

            val spinoffs = mutableListOf<SpinoffShowDisplay>()
            var rank = 1

            val currentTmdbId = _showDetails.value?.id ?: 0
            if (currentFranchise.parentShow?.tmdbId != currentTmdbId) {
                currentFranchise.parentShow?.let { parent ->
                    tmdbService.getShowDetails(parent.tmdbId).getOrNull()?.let { details ->
                        spinoffs.add(details.toSpinoffDisplay(rank++))
                    }
                }
            }

            currentFranchise.spinoffs
                .filter { it.tmdbId != currentTmdbId }
                .forEach { spinoff ->
                    tmdbService.getShowDetails(spinoff.tmdbId).getOrNull()?.let { details ->
                        spinoffs.add(details.toSpinoffDisplay(rank++))
                    }
                }

            _spinoffShows.value = spinoffs
            _isLoadingSpinoffs.value = false
        }
    }

    // endregion

    // region Actions

    fun selectTab(tab: FollowedShowTab) {
        if (_availableTabs.value.contains(tab)) {
            _selectedTab.value = tab
        }
    }

    fun toggleSynopsisExpanded() {
        _isSynopsisExpanded.value = !_isSynopsisExpanded.value
    }

    /**
     * Select a season number for the Info tab episode list.
     */
    fun selectSeason(seasonNumber: Int) {
        _selectedSeasonNumber.value = seasonNumber
        loadEpisodesForSelectedSeason()
    }

    /**
     * Load episodes for the currently selected season.
     */
    private fun loadEpisodesForSelectedSeason() {
        val seasonNumber = _selectedSeasonNumber.value
        val season = _localSeasons.value.find { it.seasonNumber == seasonNumber }

        if (season != null) {
            viewModelScope.launch {
                val episodes = repository.getEpisodesForSeasonSync(season.id)
                _selectedSeasonEpisodes.value = episodes.sortedBy { it.episodeNumber }
            }
        } else {
            _selectedSeasonEpisodes.value = emptyList()
        }
    }

    /**
     * Toggle watched status for an episode.
     */
    fun toggleEpisodeWatched(episodeId: Long, currentWatched: Boolean) {
        viewModelScope.launch {
            repository.setEpisodeWatched(episodeId, !currentWatched)
            // Reload episodes to reflect change
            loadEpisodesForSelectedSeason()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadFollowedShow()
            _isRefreshing.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    /**
     * Unfollow the current show from the followed list.
     * Calls onComplete when successful.
     */
    fun unfollowShow(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteShow(showId)
                onComplete()
            } catch (e: Exception) {
                _error.value = "Failed to unfollow show"
            }
        }
    }

    // endregion

    // region Helpers

    private fun parseShowStatus(status: String?): ShowStatus {
        return when (status?.lowercase()) {
            "returning series" -> ShowStatus.RETURNING
            "ended" -> ShowStatus.ENDED
            "canceled" -> ShowStatus.CANCELED
            "in production" -> ShowStatus.IN_PRODUCTION
            "planned" -> ShowStatus.PLANNED
            else -> ShowStatus.UNKNOWN
        }
    }

    private fun TMDBShowDetails.toSpinoffDisplay(rank: Int): SpinoffShowDisplay {
        return SpinoffShowDisplay(
            tmdbId = id,
            title = name,
            overview = overview,
            backdropPath = backdropPath,
            posterPath = posterPath,
            voteAverage = null,
            numberOfSeasons = numberOfSeasons,
            status = parseShowStatus(status),
            rank = rank
        )
    }

    // endregion
}
