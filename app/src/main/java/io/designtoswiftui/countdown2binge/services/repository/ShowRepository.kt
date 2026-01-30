package io.designtoswiftui.countdown2binge.services.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.designtoswiftui.countdown2binge.models.Episode
import io.designtoswiftui.countdown2binge.models.Genre
import io.designtoswiftui.countdown2binge.models.Network
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.SeasonState
import io.designtoswiftui.countdown2binge.models.SeasonWithEpisodes
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.models.ShowStatus
import io.designtoswiftui.countdown2binge.models.ShowWithDetails
import io.designtoswiftui.countdown2binge.models.ShowWithSeasons
import io.designtoswiftui.countdown2binge.models.ShowWithSeasonsAndEpisodes
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing Show, Season, Episode, Genre, and Network data.
 * Provides a clean API for data access and abstracts the underlying DAOs.
 *
 * Key design principle: When updating from API, preserve user-generated data
 * (isWatched, hasWatched, watchedDate) while refreshing metadata.
 */
@Singleton
class ShowRepository @Inject constructor(
    private val showDao: ShowDao,
    private val seasonDao: SeasonDao,
    private val episodeDao: EpisodeDao,
    private val genreDao: GenreDao,
    private val networkDao: NetworkDao
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

    /**
     * Get a show by its ID as a Flow.
     */
    fun getShowByIdFlow(id: Long): Flow<Show?> {
        return showDao.getByIdFlow(id)
    }

    /**
     * Get shows that are in production (returning or actively in production).
     * Used for notification scheduling as these shows have upcoming content.
     */
    fun getInProductionShows(): Flow<List<Show>> {
        return showDao.getShowsByStatus(
            listOf(ShowStatus.RETURNING, ShowStatus.IN_PRODUCTION)
        )
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

    // region Franchise/Spinoff Operations

    /**
     * Update the related show IDs for a show (spinoffs and parent).
     */
    suspend fun updateRelatedShowIds(showId: Long, relatedIds: List<Int>) {
        val json = if (relatedIds.isNotEmpty()) {
            Gson().toJson(relatedIds)
        } else {
            null
        }
        showDao.updateRelatedShowIds(showId, json)
    }

    /**
     * Get the related show IDs for a show.
     */
    suspend fun getRelatedShowIds(showId: Long): List<Int> {
        val json = showDao.getRelatedShowIdsJson(showId) ?: return emptyList()
        return try {
            Gson().fromJson(json, object : TypeToken<List<Int>>() {}.type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get the count of followed shows.
     */
    suspend fun getFollowedShowCount(): Int {
        return showDao.getFollowedShowCount()
    }

    // endregion

    // region Genre Operations

    /**
     * Save genres for a show.
     * Replaces any existing genres for the show.
     */
    suspend fun saveGenres(showId: Long, genres: List<Genre>) {
        genreDao.deleteByShowId(showId)
        if (genres.isNotEmpty()) {
            val genresWithShowId = genres.map { it.copy(showId = showId) }
            genreDao.insertAll(genresWithShowId)
        }
    }

    /**
     * Get genres for a show as a Flow.
     */
    fun getGenresForShow(showId: Long): Flow<List<Genre>> {
        return genreDao.getByShowId(showId)
    }

    /**
     * Get genres for a show (one-shot).
     */
    suspend fun getGenresForShowSync(showId: Long): List<Genre> {
        return genreDao.getByShowIdOnce(showId)
    }

    // endregion

    // region Network Operations

    /**
     * Save networks for a show.
     * Replaces any existing networks for the show.
     */
    suspend fun saveNetworks(showId: Long, networks: List<Network>) {
        networkDao.deleteByShowId(showId)
        if (networks.isNotEmpty()) {
            val networksWithShowId = networks.map { it.copy(showId = showId) }
            networkDao.insertAll(networksWithShowId)
        }
    }

    /**
     * Get networks for a show as a Flow.
     */
    fun getNetworksForShow(showId: Long): Flow<List<Network>> {
        return networkDao.getByShowId(showId)
    }

    /**
     * Get networks for a show (one-shot).
     */
    suspend fun getNetworksForShowSync(showId: Long): List<Network> {
        return networkDao.getByShowIdOnce(showId)
    }

    // endregion

    // region Relationship Queries

    /**
     * Get a show with all its seasons.
     */
    suspend fun getShowWithSeasons(showId: Long): ShowWithSeasons? {
        return showDao.getShowWithSeasons(showId)
    }

    /**
     * Get a show with all its seasons as a Flow.
     */
    fun getShowWithSeasonsFlow(showId: Long): Flow<ShowWithSeasons?> {
        return showDao.getShowWithSeasonsFlow(showId)
    }

    /**
     * Get a show with all related data (seasons, genres, networks).
     */
    suspend fun getShowWithDetails(showId: Long): ShowWithDetails? {
        return showDao.getShowWithDetails(showId)
    }

    /**
     * Get a show with all related data as a Flow.
     */
    fun getShowWithDetailsFlow(showId: Long): Flow<ShowWithDetails?> {
        return showDao.getShowWithDetailsFlow(showId)
    }

    /**
     * Get a show with seasons and their episodes.
     */
    suspend fun getShowWithSeasonsAndEpisodes(showId: Long): ShowWithSeasonsAndEpisodes? {
        return showDao.getShowWithSeasonsAndEpisodes(showId)
    }

    /**
     * Get all shows with their details as a Flow.
     */
    fun getAllShowsWithDetails(): Flow<List<ShowWithDetails>> {
        return showDao.getAllShowsWithDetails()
    }

    /**
     * Get a season with all its episodes.
     */
    suspend fun getSeasonWithEpisodes(seasonId: Long): SeasonWithEpisodes? {
        return seasonDao.getSeasonWithEpisodes(seasonId)
    }

    /**
     * Get a season with all its episodes as a Flow.
     */
    fun getSeasonWithEpisodesFlow(seasonId: Long): Flow<SeasonWithEpisodes?> {
        return seasonDao.getSeasonWithEpisodesFlow(seasonId)
    }

    /**
     * Get all seasons with episodes for a show.
     */
    fun getSeasonsWithEpisodesForShow(showId: Long): Flow<List<SeasonWithEpisodes>> {
        return seasonDao.getSeasonsWithEpisodesForShow(showId)
    }

    /**
     * Get all seasons with episodes for a show (one-shot).
     */
    suspend fun getSeasonsWithEpisodesForShowSync(showId: Long): List<SeasonWithEpisodes> {
        return seasonDao.getSeasonsWithEpisodesForShowSync(showId)
    }

    // endregion

    // region Metadata Update (Preserves User State)

    /**
     * Update show metadata from API while preserving user-generated data.
     *
     * This method:
     * - Updates show fields (name, overview, poster, etc.)
     * - Updates existing seasons (preserves hasWatched, watchedDate)
     * - Adds new seasons if they appear
     * - Updates existing episodes (preserves isWatched)
     * - Adds new episodes if they appear
     * - Replaces genres and networks
     *
     * @param showId The local database ID of the show to update
     * @param updatedShow The show with fresh API data (id field is ignored)
     * @param updatedSeasons Seasons with fresh API data
     * @param episodesBySeasonTmdbId Episodes grouped by season TMDB ID
     * @param genres New genres from API
     * @param networks New networks from API
     */
    suspend fun updateShowMetadata(
        showId: Long,
        updatedShow: Show,
        updatedSeasons: List<Season>,
        episodesBySeasonTmdbId: Map<Int, List<Episode>> = emptyMap(),
        genres: List<Genre> = emptyList(),
        networks: List<Network> = emptyList()
    ) {
        val existingShow = getShowById(showId) ?: return

        // Update show metadata (preserve user tracking fields)
        val refreshedShow = existingShow.copy(
            title = updatedShow.title,
            overview = updatedShow.overview,
            posterPath = updatedShow.posterPath,
            backdropPath = updatedShow.backdropPath,
            logoPath = updatedShow.logoPath,
            firstAirDate = updatedShow.firstAirDate,
            status = updatedShow.status,
            statusRaw = updatedShow.statusRaw,
            numberOfSeasons = updatedShow.numberOfSeasons,
            numberOfEpisodes = updatedShow.numberOfEpisodes,
            inProduction = updatedShow.inProduction,
            voteAverage = updatedShow.voteAverage,
            currentSeasonStartDate = updatedShow.currentSeasonStartDate,
            currentSeasonFinaleDate = updatedShow.currentSeasonFinaleDate,
            hasMidseasonBreak = updatedShow.hasMidseasonBreak,
            lastUpdated = System.currentTimeMillis()
            // Preserved: isShowAdded, addedDate, followedAt, lastSyncedAt, isSynced
        )
        update(refreshedShow)

        // Get existing seasons for comparison
        val existingSeasons = getSeasonsForShowSync(showId)
        val existingSeasonsByNumber = existingSeasons.associateBy { it.seasonNumber }

        for (apiSeason in updatedSeasons.filter { it.seasonNumber > 0 }) {
            val existingSeason = existingSeasonsByNumber[apiSeason.seasonNumber]

            if (existingSeason != null) {
                // Update existing season (preserve watched state)
                val refreshedSeason = existingSeason.copy(
                    name = apiSeason.name,
                    overview = apiSeason.overview,
                    premiereDate = apiSeason.premiereDate,
                    finaleDate = apiSeason.finaleDate,
                    isFinaleEstimated = apiSeason.isFinaleEstimated,
                    episodeCount = apiSeason.episodeCount,
                    airedEpisodeCount = apiSeason.airedEpisodeCount,
                    releasePattern = apiSeason.releasePattern,
                    posterPath = apiSeason.posterPath,
                    voteAverage = apiSeason.voteAverage
                    // Preserved: hasWatched, watchedDate, state
                )
                updateSeason(refreshedSeason)

                // Update episodes for this season
                val apiEpisodes = episodesBySeasonTmdbId[apiSeason.tmdbId] ?: emptyList()
                if (apiEpisodes.isNotEmpty()) {
                    updateSeasonEpisodes(existingSeason.id, apiEpisodes)
                }
            } else {
                // Add new season
                val newSeason = apiSeason.copy(showId = showId)
                val newSeasonId = saveSeason(newSeason)

                // Add episodes for new season
                val apiEpisodes = episodesBySeasonTmdbId[apiSeason.tmdbId] ?: emptyList()
                if (apiEpisodes.isNotEmpty()) {
                    val episodesWithSeasonId = apiEpisodes.map {
                        it.copy(seasonId = newSeasonId)
                    }
                    saveEpisodes(episodesWithSeasonId)
                }
            }
        }

        // Replace genres and networks
        saveGenres(showId, genres)
        saveNetworks(showId, networks)
    }

    /**
     * Update episodes for a season while preserving watched state.
     */
    private suspend fun updateSeasonEpisodes(seasonId: Long, apiEpisodes: List<Episode>) {
        val existingEpisodes = getEpisodesForSeasonSync(seasonId)
        val existingByNumber = existingEpisodes.associateBy { it.episodeNumber }

        for (apiEpisode in apiEpisodes) {
            val existing = existingByNumber[apiEpisode.episodeNumber]

            if (existing != null) {
                // Update existing episode (preserve isWatched)
                val refreshed = existing.copy(
                    name = apiEpisode.name,
                    overview = apiEpisode.overview,
                    airDate = apiEpisode.airDate,
                    stillPath = apiEpisode.stillPath,
                    runtime = apiEpisode.runtime,
                    episodeType = apiEpisode.episodeType,
                    seasonNumber = apiEpisode.seasonNumber,
                    voteAverage = apiEpisode.voteAverage
                    // Preserved: isWatched
                )
                episodeDao.update(refreshed)
            } else {
                // Add new episode
                val newEpisode = apiEpisode.copy(seasonId = seasonId)
                saveEpisode(newEpisode)
            }
        }
    }

    // endregion
}
