package io.designtoswiftui.countdown2binge.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBSearchResult
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBShowDetails
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBSeasonDetails
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBVideo
import io.designtoswiftui.countdown2binge.usecases.RefreshShowUseCase
import kotlinx.coroutines.async
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
    val episodesRemaining: Int?,
    val watchedCount: Int = 0,
    val totalEpisodes: Int = 0
) {
    val isFullyWatched: Boolean
        get() = watchedCount >= totalEpisodes && totalEpisodes > 0
}

/**
 * Display model for spinoff shows in the detail screen.
 */
data class SpinoffShowDisplay(
    val tmdbId: Int,
    val title: String,
    val overview: String?,
    val backdropPath: String?,
    val posterPath: String?,
    val voteAverage: Double?,
    val numberOfSeasons: Int?,
    val status: ShowStatus,
    val rank: Int
) {
    val statusLabel: String
        get() = when (status) {
            ShowStatus.RETURNING -> "CURRENTLY AIRING"
            ShowStatus.ENDED -> "CRITICALLY ACCLAIMED"
            ShowStatus.IN_PRODUCTION -> "IN PRODUCTION"
            else -> "SPINOFF SERIES"
        }
}

/**
 * ViewModel for the Show Detail screen.
 * Supports both followed shows (from Room) and non-followed shows (from TMDB).
 */
@HiltViewModel
class ShowDetailViewModel @Inject constructor(
    private val repository: ShowRepository,
    private val stateManager: SeasonStateManager,
    private val refreshShowUseCase: RefreshShowUseCase,
    private val tmdbService: TMDBService,
    private val franchiseService: FranchiseService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Navigation parameters - can use either showId (local) or tmdbId (remote)
    private val showId: Long = savedStateHandle.get<Long>("showId") ?: 0L
    private val tmdbId: Int = savedStateHandle.get<Int>("tmdbId") ?: 0

    // Core show data
    private val _show = MutableStateFlow<Show?>(null)
    val show: StateFlow<Show?> = _show.asStateFlow()

    // TMDB show details (for non-followed shows or fresh data)
    private val _showDetails = MutableStateFlow<TMDBShowDetails?>(null)
    val showDetails: StateFlow<TMDBShowDetails?> = _showDetails.asStateFlow()

    // Season details from TMDB (includes episodes)
    private val _seasonDetails = MutableStateFlow<Map<Int, TMDBSeasonDetails>>(emptyMap())
    val seasonDetails: StateFlow<Map<Int, TMDBSeasonDetails>> = _seasonDetails.asStateFlow()

    // Seasons for this show with countdown info (for followed shows)
    private val _seasons = MutableStateFlow<List<SeasonDetail>>(emptyList())
    val seasons: StateFlow<List<SeasonDetail>> = _seasons.asStateFlow()

    // Selected season number
    private val _selectedSeasonNumber = MutableStateFlow(1)
    val selectedSeasonNumber: StateFlow<Int> = _selectedSeasonNumber.asStateFlow()

    // Follow state
    private val _isFollowed = MutableStateFlow(false)
    val isFollowed: StateFlow<Boolean> = _isFollowed.asStateFlow()

    // Loading states
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isAdding = MutableStateFlow(false)
    val isAdding: StateFlow<Boolean> = _isAdding.asStateFlow()

    private val _isRemoving = MutableStateFlow(false)
    val isRemoving: StateFlow<Boolean> = _isRemoving.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Additional content
    private val _videos = MutableStateFlow<List<TMDBVideo>>(emptyList())
    val videos: StateFlow<List<TMDBVideo>> = _videos.asStateFlow()

    private val _cast = MutableStateFlow<List<TMDBCastMember>>(emptyList())
    val cast: StateFlow<List<TMDBCastMember>> = _cast.asStateFlow()

    private val _crew = MutableStateFlow<List<TMDBCrewMember>>(emptyList())
    val crew: StateFlow<List<TMDBCrewMember>> = _crew.asStateFlow()

    private val _recommendations = MutableStateFlow<List<TMDBSearchResult>>(emptyList())
    val recommendations: StateFlow<List<TMDBSearchResult>> = _recommendations.asStateFlow()

    // Spinoffs
    private val _franchise = MutableStateFlow<Franchise?>(null)
    val franchise: StateFlow<Franchise?> = _franchise.asStateFlow()

    private val _spinoffShows = MutableStateFlow<List<SpinoffShowDisplay>>(emptyList())
    val spinoffShows: StateFlow<List<SpinoffShowDisplay>> = _spinoffShows.asStateFlow()

    private val _isLoadingSpinoffs = MutableStateFlow(false)
    val isLoadingSpinoffs: StateFlow<Boolean> = _isLoadingSpinoffs.asStateFlow()

    private val _hasFranchise = MutableStateFlow(false)
    val hasFranchise: StateFlow<Boolean> = _hasFranchise.asStateFlow()

    // UI State
    private val _isSynopsisExpanded = MutableStateFlow(false)
    val isSynopsisExpanded: StateFlow<Boolean> = _isSynopsisExpanded.asStateFlow()

    private val _isEpisodeListExpanded = MutableStateFlow(false)
    val isEpisodeListExpanded: StateFlow<Boolean> = _isEpisodeListExpanded.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Logo path for header
    private val _logoPath = MutableStateFlow<String?>(null)
    val logoPath: StateFlow<String?> = _logoPath.asStateFlow()

    // Computed properties
    val regularSeasons: List<Int>
        get() = _showDetails.value?.seasons
            ?.filter { it.seasonNumber > 0 }
            ?.map { it.seasonNumber }
            ?.sortedBy { it }
            ?: emptyList()

    val hasMultipleSeasons: Boolean
        get() = regularSeasons.size > 1

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

    init {
        when {
            showId > 0 -> loadFollowedShow()
            tmdbId > 0 -> loadShowFromTmdb(tmdbId)
            else -> {
                _isLoading.value = false
                _error.value = "Invalid show ID"
            }
        }
    }

    // region Loading

    /**
     * Load show by its local database ID.
     * Used by the existing ShowDetailScreen.
     */
    fun loadShowById(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val loadedShow = repository.getShowById(id)
                if (loadedShow != null) {
                    _show.value = loadedShow
                    _isFollowed.value = true
                    loadSeasons(id)

                    // Also fetch fresh TMDB data for additional content
                    loadShowFromTmdb(loadedShow.tmdbId)
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
     * Refresh local data from database.
     */
    fun refresh() {
        if (showId > 0) {
            loadFollowedShow()
        }
    }

    /**
     * Load a followed show from local database.
     */
    private fun loadFollowedShow() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val loadedShow = repository.getShowById(showId)
                if (loadedShow != null) {
                    _show.value = loadedShow
                    _isFollowed.value = true
                    loadSeasons(showId)

                    // Also fetch fresh TMDB data for additional content
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

    /**
     * Load show data from TMDB API.
     */
    fun loadShowFromTmdb(showTmdbId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Check if this show is followed
                _isFollowed.value = repository.isShowFollowed(showTmdbId)

                // Fetch show details
                val detailsResult = tmdbService.getShowDetails(showTmdbId)
                if (detailsResult.isSuccess) {
                    val details = detailsResult.getOrThrow()
                    _showDetails.value = details

                    // Set default selected season to latest regular season
                    val latestSeason = details.seasons
                        ?.filter { it.seasonNumber > 0 }
                        ?.maxByOrNull { it.seasonNumber }
                        ?.seasonNumber ?: 1
                    _selectedSeasonNumber.value = latestSeason

                    // Load season details for selected season
                    loadSeasonDetails(showTmdbId, latestSeason)

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

    /**
     * Load season details including episodes.
     */
    private suspend fun loadSeasonDetails(showTmdbId: Int, seasonNumber: Int) {
        val result = tmdbService.getSeasonDetails(showTmdbId, seasonNumber)
        if (result.isSuccess) {
            val details = result.getOrThrow()
            _seasonDetails.value = _seasonDetails.value + (seasonNumber to details)
        }
    }

    /**
     * Load seasons for a followed show.
     */
    private suspend fun loadSeasons(showId: Long) {
        val today = LocalDate.now()
        val seasonsList = repository.getSeasonsForShowSync(showId)

        val seasonDetails = seasonsList.map { season ->
            val watchedCount = repository.getWatchedEpisodeCount(season.id)
            val episodes = repository.getEpisodesForSeasonSync(season.id)
            SeasonDetail(
                season = season,
                daysUntilPremiere = stateManager.daysUntilPremiere(season, today),
                daysUntilFinale = stateManager.daysUntilFinale(season, today),
                episodesRemaining = stateManager.episodesRemaining(season),
                watchedCount = watchedCount,
                totalEpisodes = episodes.size
            )
        }.sortedByDescending { it.season.seasonNumber }

        _seasons.value = seasonDetails
    }

    /**
     * Load additional content (videos, cast, recommendations).
     */
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
            val recommendationsDeferred = async {
                tmdbService.getShowRecommendations(showTmdbId).getOrNull()?.results
                    ?.take(4)
                    ?: emptyList()
            }

            _videos.value = videosDeferred.await()
            creditsDeferred.await()?.let { credits ->
                _cast.value = credits.cast.take(10)
                _crew.value = credits.crew.filter { it.job == "Creator" || it.job == "Executive Producer" }
            }
            _recommendations.value = recommendationsDeferred.await()
        }
    }

    /**
     * Load franchise and spinoff data.
     */
    private fun loadFranchiseData(showTmdbId: Int) {
        viewModelScope.launch {
            // First fetch franchises from Firebase
            franchiseService.fetchFranchises()

            // Check if this show has a franchise
            val foundFranchise = franchiseService.franchiseForShowId(showTmdbId)
            _franchise.value = foundFranchise
            _hasFranchise.value = foundFranchise != null
        }
    }

    /**
     * Load spinoff show details (only call if user is premium).
     */
    fun loadSpinoffDetails() {
        val currentFranchise = _franchise.value ?: return
        if (_spinoffShows.value.isNotEmpty()) return // Already loaded

        viewModelScope.launch {
            _isLoadingSpinoffs.value = true

            val spinoffs = mutableListOf<SpinoffShowDisplay>()
            var rank = 1

            // Include parent show if current show is a spinoff
            val currentTmdbId = _showDetails.value?.id ?: tmdbId
            if (currentFranchise.parentShow?.tmdbId != currentTmdbId) {
                currentFranchise.parentShow?.let { parent ->
                    tmdbService.getShowDetails(parent.tmdbId).getOrNull()?.let { details ->
                        spinoffs.add(details.toSpinoffDisplay(rank++))
                    }
                }
            }

            // Load spinoff details
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

    /**
     * Load the English logo for the show.
     */
    private fun loadLogo(showTmdbId: Int) {
        viewModelScope.launch {
            _logoPath.value = tmdbService.getEnglishLogoPath(showTmdbId)
        }
    }

    // endregion

    // region Actions

    /**
     * Select a season to display.
     */
    fun selectSeason(seasonNumber: Int) {
        if (regularSeasons.contains(seasonNumber)) {
            _selectedSeasonNumber.value = seasonNumber

            // Load season details if not already loaded
            val showTmdbId = _showDetails.value?.id ?: tmdbId
            if (!_seasonDetails.value.containsKey(seasonNumber) && showTmdbId > 0) {
                viewModelScope.launch {
                    loadSeasonDetails(showTmdbId, seasonNumber)
                }
            }
        }
    }

    /**
     * Toggle synopsis expanded state.
     */
    fun toggleSynopsisExpanded() {
        _isSynopsisExpanded.value = !_isSynopsisExpanded.value
    }

    /**
     * Toggle episode list expanded state.
     */
    fun toggleEpisodeListExpanded() {
        _isEpisodeListExpanded.value = !_isEpisodeListExpanded.value
    }

    /**
     * Follow the show (add to database).
     */
    fun followShow() {
        val details = _showDetails.value ?: return
        if (_isFollowed.value || _isAdding.value) return

        viewModelScope.launch {
            _isAdding.value = true

            try {
                // Create Show model from TMDB details
                val show = Show(
                    tmdbId = details.id,
                    title = details.name,
                    overview = details.overview,
                    posterPath = details.posterPath,
                    backdropPath = details.backdropPath,
                    status = parseShowStatus(details.status)
                )

                // Save to repository (would need full implementation with seasons)
                // For now, just save the show
                repository.save(show)
                _isFollowed.value = true
            } catch (e: Exception) {
                _error.value = "Failed to follow show: ${e.message}"
            }

            _isAdding.value = false
        }
    }

    /**
     * Unfollow the show (remove from database).
     */
    fun unfollowShow(onComplete: () -> Unit = {}) {
        if (!_isFollowed.value || _isRemoving.value) return

        viewModelScope.launch {
            _isRemoving.value = true

            try {
                val currentShow = _show.value
                if (currentShow != null) {
                    repository.delete(currentShow)
                } else {
                    // Find by tmdbId
                    val showTmdbId = _showDetails.value?.id ?: tmdbId
                    repository.getShow(showTmdbId)?.let { show ->
                        repository.delete(show)
                    }
                }
                _isFollowed.value = false
                onComplete()
            } catch (e: Exception) {
                _error.value = "Failed to unfollow: ${e.message}"
            }

            _isRemoving.value = false
        }
    }

    /**
     * Toggle follow state.
     */
    fun toggleFollow() {
        if (_isFollowed.value) {
            unfollowShow()
        } else {
            followShow()
        }
    }

    /**
     * Refresh the show data from TMDB.
     */
    fun refreshFromNetwork() {
        val showTmdbId = _showDetails.value?.id ?: tmdbId
        if (showTmdbId <= 0) return

        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null

            try {
                if (showId > 0) {
                    // Refresh followed show
                    val result = refreshShowUseCase.execute(showId)
                    if (result.isSuccess) {
                        loadFollowedShow()
                    } else {
                        _error.value = result.exceptionOrNull()?.message ?: "Failed to refresh"
                    }
                } else {
                    // Refresh non-followed show from TMDB
                    loadShowFromTmdb(showTmdbId)
                }
            } catch (e: Exception) {
                _error.value = "Failed to refresh: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * Mark a season as watched.
     */
    fun markSeasonWatched(seasonId: Long) {
        viewModelScope.launch {
            try {
                repository.markSeasonWatched(seasonId)
                _show.value?.let { loadSeasons(it.id) }
            } catch (e: Exception) {
                _error.value = "Failed to mark as watched: ${e.message}"
            }
        }
    }

    /**
     * Unmark a season as watched.
     */
    fun unmarkSeasonWatched(seasonId: Long) {
        viewModelScope.launch {
            try {
                val seasonDetail = _seasons.value.find { it.season.id == seasonId }
                val season = seasonDetail?.season

                val newState = if (season != null) {
                    stateManager.determineState(
                        season.copy(watchedDate = null),
                        LocalDate.now()
                    )
                } else {
                    SeasonState.BINGE_READY
                }

                repository.unmarkSeasonWatched(seasonId, newState)
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

    /**
     * Clear error message.
     */
    fun clearError() {
        _error.value = null
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
            voteAverage = null, // Would need to add this to TMDBShowDetails
            numberOfSeasons = numberOfSeasons,
            status = parseShowStatus(status),
            rank = rank
        )
    }

    // endregion
}
