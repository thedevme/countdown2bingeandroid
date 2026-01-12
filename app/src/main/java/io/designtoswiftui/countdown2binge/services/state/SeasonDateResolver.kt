package io.designtoswiftui.countdown2binge.services.state

import io.designtoswiftui.countdown2binge.models.Episode
import io.designtoswiftui.countdown2binge.models.ReleasePattern
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Contains resolved date information for a season.
 */
data class SeasonDateInfo(
    val premiereDate: LocalDate?,
    val finaleDate: LocalDate?,
    val isFinaleEstimated: Boolean,
    val releasePattern: ReleasePattern,
    val airedEpisodeCount: Int
)

@Singleton
class SeasonDateResolver @Inject constructor() {

    companion object {
        private const val WEEKLY_INTERVAL_DAYS = 7L
        private const val WEEKLY_TOLERANCE_DAYS = 2L
    }

    /**
     * Resolves all date information for a season based on available data.
     */
    fun resolve(
        seasonAirDate: LocalDate?,
        episodeCount: Int,
        episodes: List<Episode>,
        asOf: LocalDate = LocalDate.now()
    ): SeasonDateInfo {
        val premiereDate = resolvePremiereDate(seasonAirDate, episodes)
        val releasePattern = detectReleasePattern(episodes)
        val airedEpisodeCount = countAiredEpisodes(episodes, asOf)

        val (finaleDate, isEstimated) = resolveFinaleDate(
            premiereDate = premiereDate,
            episodeCount = episodeCount,
            episodes = episodes,
            releasePattern = releasePattern
        )

        return SeasonDateInfo(
            premiereDate = premiereDate,
            finaleDate = finaleDate,
            isFinaleEstimated = isEstimated,
            releasePattern = releasePattern,
            airedEpisodeCount = airedEpisodeCount
        )
    }

    /**
     * Resolves the premiere date from season air date or first episode.
     */
    fun resolvePremiereDate(
        seasonAirDate: LocalDate?,
        episodes: List<Episode>
    ): LocalDate? {
        // Try to get from first episode
        val firstEpisodeDate = episodes
            .filter { it.episodeNumber == 1 }
            .mapNotNull { it.airDate }
            .firstOrNull()

        // Prefer first episode date, fall back to season air date
        return firstEpisodeDate ?: seasonAirDate
    }

    /**
     * Resolves the finale date, estimating if necessary.
     * Returns a pair of (finaleDate, isEstimated).
     */
    fun resolveFinaleDate(
        premiereDate: LocalDate?,
        episodeCount: Int,
        episodes: List<Episode>,
        releasePattern: ReleasePattern
    ): Pair<LocalDate?, Boolean> {
        // Try to get from last episode
        val lastEpisodeDate = episodes
            .filter { it.episodeNumber == episodeCount && episodeCount > 0 }
            .mapNotNull { it.airDate }
            .firstOrNull()

        if (lastEpisodeDate != null) {
            return lastEpisodeDate to false
        }

        // For all-at-once releases, finale is same as premiere
        if (releasePattern == ReleasePattern.ALL_AT_ONCE && premiereDate != null) {
            return premiereDate to false
        }

        // Estimate based on weekly release pattern
        if (premiereDate != null && episodeCount > 0 && releasePattern == ReleasePattern.WEEKLY) {
            val estimatedFinale = premiereDate.plusWeeks((episodeCount - 1).toLong())
            return estimatedFinale to true
        }

        return null to false
    }

    /**
     * Detects the release pattern based on episode air dates.
     */
    fun detectReleasePattern(episodes: List<Episode>): ReleasePattern {
        val datedEpisodes = episodes
            .filter { it.airDate != null }
            .sortedBy { it.episodeNumber }

        if (datedEpisodes.size < 2) {
            return ReleasePattern.UNKNOWN
        }

        // Check if all episodes air on the same day (Netflix style)
        val firstDate = datedEpisodes.first().airDate
        val allSameDay = datedEpisodes.all { it.airDate == firstDate }

        if (allSameDay) {
            return ReleasePattern.ALL_AT_ONCE
        }

        // Check for weekly pattern
        val intervals = datedEpisodes.zipWithNext { a, b ->
            ChronoUnit.DAYS.between(a.airDate, b.airDate)
        }

        if (intervals.isEmpty()) {
            return ReleasePattern.UNKNOWN
        }

        val averageInterval = intervals.average()
        val isWeekly = intervals.all { interval ->
            interval in (WEEKLY_INTERVAL_DAYS - WEEKLY_TOLERANCE_DAYS)..(WEEKLY_INTERVAL_DAYS + WEEKLY_TOLERANCE_DAYS)
        }

        if (isWeekly) {
            return ReleasePattern.WEEKLY
        }

        // Check for split season (significant gap in the middle)
        val hasLargeGap = intervals.any { it > WEEKLY_INTERVAL_DAYS * 4 }
        if (hasLargeGap) {
            return ReleasePattern.SPLIT_SEASON
        }

        return ReleasePattern.UNKNOWN
    }

    /**
     * Counts the number of episodes that have aired as of a given date.
     */
    fun countAiredEpisodes(episodes: List<Episode>, asOf: LocalDate = LocalDate.now()): Int {
        return episodes.count { episode ->
            episode.airDate?.let { !it.isAfter(asOf) } ?: false
        }
    }
}
