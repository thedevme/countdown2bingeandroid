package io.designtoswiftui.countdown2binge.services.tmdb

import io.designtoswiftui.countdown2binge.models.Episode
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.models.ShowStatus
import io.designtoswiftui.countdown2binge.services.state.SeasonDateResolver
import io.designtoswiftui.countdown2binge.services.state.SeasonStateManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Processes TMDB data and converts it to local domain models.
 */
@Singleton
class ShowProcessor @Inject constructor(
    private val dateResolver: SeasonDateResolver,
    private val stateManager: SeasonStateManager
) {

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
    }

    /**
     * Process full show data into local Show model.
     * Note: This creates a Show object but doesn't set the ID (that's done by Room on insert).
     */
    fun processShow(fullData: FullShowData, asOf: LocalDate = LocalDate.now()): Show {
        val showDetails = fullData.showDetails

        return Show(
            id = 0, // Will be set by Room on insert
            tmdbId = showDetails.id,
            title = showDetails.name,
            overview = showDetails.overview,
            posterPath = showDetails.posterPath,
            backdropPath = showDetails.backdropPath,
            status = showDetails.status?.let { ShowStatus.fromTmdb(it) } ?: ShowStatus.UNKNOWN,
            addedDate = asOf
        )
    }

    /**
     * Process season data into local Season model.
     * Resolves dates and determines initial state.
     */
    fun processSeason(
        seasonDetails: TMDBSeasonDetails,
        showId: Long,
        asOf: LocalDate = LocalDate.now()
    ): Season {
        val episodes = seasonDetails.episodes?.map { processEpisodeForDateResolution(it) } ?: emptyList()

        val dateInfo = dateResolver.resolve(
            seasonAirDate = parseDate(seasonDetails.airDate),
            episodeCount = seasonDetails.episodes?.size ?: 0,
            episodes = episodes,
            asOf = asOf
        )

        // Create a temporary season to determine state
        val tempSeason = Season(
            id = 0,
            tmdbId = seasonDetails.id,
            seasonNumber = seasonDetails.seasonNumber,
            premiereDate = dateInfo.premiereDate,
            finaleDate = dateInfo.finaleDate,
            isFinaleEstimated = dateInfo.isFinaleEstimated,
            episodeCount = seasonDetails.episodes?.size ?: 0,
            airedEpisodeCount = dateInfo.airedEpisodeCount,
            releasePattern = dateInfo.releasePattern,
            state = io.designtoswiftui.countdown2binge.models.SeasonState.ANTICIPATED,
            watchedDate = null,
            posterPath = seasonDetails.posterPath,
            showId = showId
        )

        // Determine the actual state
        val state = stateManager.determineState(tempSeason, asOf)

        return tempSeason.copy(state = state)
    }

    /**
     * Process episode data into local Episode model.
     */
    fun processEpisode(
        tmdbEpisode: TMDBEpisode,
        seasonId: Long
    ): Episode {
        return Episode(
            id = 0, // Will be set by Room on insert
            tmdbId = tmdbEpisode.id,
            episodeNumber = tmdbEpisode.episodeNumber,
            name = tmdbEpisode.name ?: "Episode ${tmdbEpisode.episodeNumber}",
            airDate = parseDate(tmdbEpisode.airDate),
            runtime = tmdbEpisode.runtime,
            overview = tmdbEpisode.overview,
            isWatched = false,
            seasonId = seasonId
        )
    }

    /**
     * Process all episodes from season details.
     */
    fun processEpisodes(
        seasonDetails: TMDBSeasonDetails,
        seasonId: Long
    ): List<Episode> {
        return seasonDetails.episodes?.map { processEpisode(it, seasonId) } ?: emptyList()
    }

    /**
     * Process season summary (from show details) into Season model.
     * Used when we don't have full season details.
     */
    fun processSeasonSummary(
        summary: TMDBSeasonSummary,
        showId: Long,
        asOf: LocalDate = LocalDate.now()
    ): Season {
        val premiereDate = parseDate(summary.airDate)

        val tempSeason = Season(
            id = 0,
            tmdbId = summary.id,
            seasonNumber = summary.seasonNumber,
            premiereDate = premiereDate,
            finaleDate = null, // Unknown without full season data
            isFinaleEstimated = false,
            episodeCount = summary.episodeCount ?: 0,
            airedEpisodeCount = 0,
            releasePattern = io.designtoswiftui.countdown2binge.models.ReleasePattern.UNKNOWN,
            state = io.designtoswiftui.countdown2binge.models.SeasonState.ANTICIPATED,
            watchedDate = null,
            posterPath = summary.posterPath,
            showId = showId
        )

        val state = stateManager.determineState(tempSeason, asOf)
        return tempSeason.copy(state = state)
    }

    /**
     * Helper to create Episode for date resolution (before we have seasonId).
     */
    private fun processEpisodeForDateResolution(tmdbEpisode: TMDBEpisode): Episode {
        return Episode(
            id = 0,
            tmdbId = tmdbEpisode.id,
            episodeNumber = tmdbEpisode.episodeNumber,
            name = tmdbEpisode.name ?: "Episode ${tmdbEpisode.episodeNumber}",
            airDate = parseDate(tmdbEpisode.airDate),
            runtime = tmdbEpisode.runtime,
            overview = tmdbEpisode.overview,
            isWatched = false,
            seasonId = 0 // Temporary, not used for date resolution
        )
    }

    /**
     * Parse a date string from TMDB format (YYYY-MM-DD).
     */
    private fun parseDate(dateString: String?): LocalDate? {
        if (dateString.isNullOrBlank()) return null

        return try {
            LocalDate.parse(dateString, DATE_FORMATTER)
        } catch (e: DateTimeParseException) {
            null
        }
    }
}
