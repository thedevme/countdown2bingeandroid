package io.designtoswiftui.countdown2binge.models

import io.designtoswiftui.countdown2binge.services.tmdb.TMDBSearchResult

/**
 * Display model for trending shows on the search landing page.
 * Combines TMDB show data with additional fetched information.
 */
data class TrendingShowDisplay(
    val show: TMDBSearchResult,
    val logoPath: String?,
    val genreNames: List<String>,
    val seasonNumber: Int? = null
) {
    val tmdbId: Int get() = show.id
    val name: String get() = show.name
    val posterPath: String? get() = show.posterPath
    val backdropPath: String? get() = show.backdropPath
}
