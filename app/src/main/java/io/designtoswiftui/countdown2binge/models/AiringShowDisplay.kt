package io.designtoswiftui.countdown2binge.models

import io.designtoswiftui.countdown2binge.services.tmdb.TMDBSearchResult

/**
 * Display model for airing/ending soon shows on the search landing page.
 * Combines TMDB show data with calculated days-until-finale.
 */
data class AiringShowDisplay(
    val show: TMDBSearchResult,
    val daysLeft: Int?,
    val genreNames: List<String>
) {
    val tmdbId: Int get() = show.id
    val name: String get() = show.name
    val posterPath: String? get() = show.posterPath
    val backdropPath: String? get() = show.backdropPath

    /**
     * Display text for days left countdown.
     * Returns "--" if days left is unknown.
     */
    val daysLeftDisplay: String
        get() = daysLeft?.toString() ?: "--"
}
