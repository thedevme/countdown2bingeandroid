package io.designtoswiftui.countdown2binge.services.state

import io.designtoswiftui.countdown2binge.models.ReleasePattern
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.SeasonState
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeasonStateManager @Inject constructor() {

    /**
     * Determines the current state of a season based on its dates and watched status.
     */
    fun determineState(season: Season, asOf: LocalDate = LocalDate.now()): SeasonState {
        // If already watched, return watched state
        if (season.watchedDate != null) {
            return SeasonState.WATCHED
        }

        val premiereDate = season.premiereDate
        val finaleDate = season.finaleDate

        // No premiere date means we're still anticipating
        if (premiereDate == null) {
            return SeasonState.ANTICIPATED
        }

        // Premiere hasn't happened yet
        if (asOf.isBefore(premiereDate)) {
            return SeasonState.PREMIERING
        }

        // For all-at-once releases (Netflix style), binge ready on premiere
        if (season.releasePattern == ReleasePattern.ALL_AT_ONCE) {
            return SeasonState.BINGE_READY
        }

        // No finale date but premiere has passed - assume airing
        if (finaleDate == null) {
            return SeasonState.AIRING
        }

        // Finale has passed - binge ready
        if (!asOf.isBefore(finaleDate)) {
            return SeasonState.BINGE_READY
        }

        // Between premiere and finale - currently airing
        return SeasonState.AIRING
    }

    /**
     * Checks if a season is ready to binge.
     */
    fun isBingeReady(season: Season, asOf: LocalDate = LocalDate.now()): Boolean {
        return determineState(season, asOf) == SeasonState.BINGE_READY
    }

    /**
     * Calculates the number of days until the season premiere.
     * Returns null if premiere date is not set or has already passed.
     */
    fun daysUntilPremiere(season: Season, asOf: LocalDate = LocalDate.now()): Int? {
        val premiereDate = season.premiereDate ?: return null

        if (!asOf.isBefore(premiereDate)) {
            return null // Premiere has already happened
        }

        return ChronoUnit.DAYS.between(asOf, premiereDate).toInt()
    }

    /**
     * Calculates the number of days until the season finale.
     * Returns null if finale date is not set or has already passed.
     */
    fun daysUntilFinale(season: Season, asOf: LocalDate = LocalDate.now()): Int? {
        val finaleDate = season.finaleDate ?: return null

        if (!asOf.isBefore(finaleDate)) {
            return null // Finale has already happened
        }

        return ChronoUnit.DAYS.between(asOf, finaleDate).toInt()
    }

    /**
     * Calculates the number of episodes remaining to air.
     * Returns null if episode counts are not available.
     */
    fun episodesRemaining(season: Season): Int? {
        if (season.episodeCount <= 0) {
            return null
        }

        val remaining = season.episodeCount - season.airedEpisodeCount
        return if (remaining > 0) remaining else 0
    }
}
