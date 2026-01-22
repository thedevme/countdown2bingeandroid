package io.designtoswiftui.countdown2binge.services.repository

import io.designtoswiftui.countdown2binge.models.Episode
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.SeasonState
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.models.ShowStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing Show, Season, and Episode data.
 * Provides a clean API for data access and abstracts the underlying DAOs.
 */
@Singleton
class ShowRepository @Inject constructor(
    private val showDao: ShowDao,
    private val seasonDao: SeasonDao,
    private val episodeDao: EpisodeDao
) {

    // region Show Operations

    /**
     * Save a show to the database.
     * Returns the ID of the inserted show.
     */
    suspend fun save(show: Show): Long {
        return showDao.insert(show)
    }

    /**
     * Update an existing show.
     */
    suspend fun update(show: Show) {
        showDao.update(show)
    }

    /**
     * Delete a show and all its associated seasons and episodes (cascade).
     */
    suspend fun delete(show: Show) {
        showDao.delete(show)
    }

    /**
     * Delete a show by its ID.
     */
    suspend fun deleteShow(showId: Long) {
        val show = getShowById(showId)
        if (show != null) {
            delete(show)
        }
    }

    /**
     * Get all shows ordered by added date.
     */
    fun getAllShows(): Flow<List<Show>> {
        return showDao.getAllShows()
    }

    /**
     * Get a show by its local database ID.
     */
    suspend fun getShowById(id: Long): Show? {
        return showDao.getById(id)
    }

    /**
     * Get a show by its TMDB ID.
     */
    suspend fun getShow(tmdbId: Int): Show? {
        return showDao.getByTmdbId(tmdbId)
    }

    /**
     * Get shows for the timeline (returning series and in production).
     * These are shows that are still active and have upcoming content.
     */
    fun getTimelineShows(): Flow<List<Show>> {
        return showDao.getShowsByStatus(
            listOf(ShowStatus.RETURNING, ShowStatus.IN_PRODUCTION)
        )
    }

    /**
     * Check if a show is already being followed.
     */
    suspend fun isShowFollowed(tmdbId: Int): Boolean {
        return showDao.isShowFollowed(tmdbId)
    }

    // endregion

    // region Season Operations

    /**
     * Save a season to the database.
     * Returns the ID of the inserted season.
     */
    suspend fun saveSeason(season: Season): Long {
        return seasonDao.insert(season)
    }

    /**
     * Save multiple seasons at once.
     */
    suspend fun saveSeasons(seasons: List<Season>) {
        seasonDao.insertAll(seasons)
    }

    /**
     * Update a season.
     */
    suspend fun updateSeason(season: Season) {
        seasonDao.update(season)
    }

    /**
     * Get all seasons for a show.
     */
    fun getSeasonsForShow(showId: Long): Flow<List<Season>> {
        return seasonDao.getSeasonsForShow(showId)
    }

    /**
     * Get all seasons for a show (synchronous).
     */
    suspend fun getSeasonsForShowSync(showId: Long): List<Season> {
        return seasonDao.getSeasonsForShowSync(showId)
    }

    /**
     * Get all binge-ready seasons across all shows.
     */
    fun getBingeReadySeasons(): Flow<List<Season>> {
        return seasonDao.getSeasonsByState(SeasonState.BINGE_READY)
    }

    /**
     * Get binge-ready seasons, optionally including airing seasons.
     */
    fun getBingeReadySeasons(includeAiring: Boolean): Flow<List<Season>> {
        return if (includeAiring) {
            seasonDao.getSeasonsByStates(listOf(SeasonState.BINGE_READY, SeasonState.AIRING))
        } else {
            seasonDao.getSeasonsByState(SeasonState.BINGE_READY)
        }
    }

    /**
     * Get seasons by state.
     */
    fun getSeasonsByState(state: SeasonState): Flow<List<Season>> {
        return seasonDao.getSeasonsByState(state)
    }

    /**
     * Mark a season as watched.
     */
    suspend fun markSeasonWatched(seasonId: Long, date: LocalDate = LocalDate.now()) {
        seasonDao.markWatched(seasonId, date, SeasonState.WATCHED)
    }

    /**
     * Unmark a season as watched and recalculate its state.
     */
    suspend fun unmarkSeasonWatched(seasonId: Long, newState: SeasonState) {
        seasonDao.unmarkWatched(seasonId, newState)
    }

    // endregion

    // region Episode Operations

    /**
     * Save an episode to the database.
     * Returns the ID of the inserted episode.
     */
    suspend fun saveEpisode(episode: Episode): Long {
        return episodeDao.insert(episode)
    }

    /**
     * Save multiple episodes at once.
     */
    suspend fun saveEpisodes(episodes: List<Episode>) {
        episodeDao.insertAll(episodes)
    }

    /**
     * Get all episodes for a season.
     */
    fun getEpisodesForSeason(seasonId: Long): Flow<List<Episode>> {
        return episodeDao.getEpisodesForSeason(seasonId)
    }

    /**
     * Get all episodes for a season (synchronous).
     */
    suspend fun getEpisodesForSeasonSync(seasonId: Long): List<Episode> {
        return episodeDao.getEpisodesForSeasonSync(seasonId)
    }

    /**
     * Mark an episode as watched or unwatched.
     */
    suspend fun setEpisodeWatched(episodeId: Long, watched: Boolean) {
        episodeDao.setWatched(episodeId, watched)
    }

    /**
     * Get the count of watched episodes for a season.
     */
    suspend fun getWatchedEpisodeCount(seasonId: Long): Int {
        return episodeDao.getWatchedCountForSeason(seasonId)
    }

    // endregion

    // region Compound Operations

    /**
     * Save a complete show with its seasons and episodes.
     * Returns the ID of the saved show.
     */
    suspend fun saveShowWithSeasons(
        show: Show,
        seasons: List<Season>,
        episodesBySeasonTmdbId: Map<Int, List<Episode>> = emptyMap()
    ): Long {
        // Save the show first
        val showId = save(show)

        // Save each season with the correct showId
        for (season in seasons) {
            val seasonWithShowId = season.copy(showId = showId)
            val seasonId = saveSeason(seasonWithShowId)

            // Save episodes for this season if available
            val episodes = episodesBySeasonTmdbId[season.tmdbId]
            if (!episodes.isNullOrEmpty()) {
                val episodesWithSeasonId = episodes.map { it.copy(seasonId = seasonId) }
                saveEpisodes(episodesWithSeasonId)
            }
        }

        return showId
    }

    // endregion
}
