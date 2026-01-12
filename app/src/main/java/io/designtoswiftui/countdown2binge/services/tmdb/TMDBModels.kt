package io.designtoswiftui.countdown2binge.services.tmdb

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response from TMDB search endpoint.
 */
@JsonClass(generateAdapter = true)
data class TMDBSearchResponse(
    val page: Int,
    val results: List<TMDBSearchResult>,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "total_results") val totalResults: Int
)

/**
 * Individual search result from TMDB.
 */
@JsonClass(generateAdapter = true)
data class TMDBSearchResult(
    val id: Int,
    val name: String,
    val overview: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "first_air_date") val firstAirDate: String?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "vote_count") val voteCount: Int?,
    val popularity: Double?
)

/**
 * Detailed show information from TMDB.
 */
@JsonClass(generateAdapter = true)
data class TMDBShowDetails(
    val id: Int,
    val name: String,
    val overview: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "first_air_date") val firstAirDate: String?,
    @Json(name = "last_air_date") val lastAirDate: String?,
    val status: String?,
    @Json(name = "number_of_seasons") val numberOfSeasons: Int?,
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int?,
    val seasons: List<TMDBSeasonSummary>?,
    @Json(name = "next_episode_to_air") val nextEpisodeToAir: TMDBEpisodeSummary?,
    @Json(name = "last_episode_to_air") val lastEpisodeToAir: TMDBEpisodeSummary?,
    @Json(name = "in_production") val inProduction: Boolean?,
    val networks: List<TMDBNetwork>?,
    val genres: List<TMDBGenre>?
)

/**
 * Summary of a season (as returned in show details).
 */
@JsonClass(generateAdapter = true)
data class TMDBSeasonSummary(
    val id: Int,
    val name: String?,
    val overview: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "season_number") val seasonNumber: Int,
    @Json(name = "episode_count") val episodeCount: Int?,
    @Json(name = "air_date") val airDate: String?
)

/**
 * Detailed season information from TMDB.
 */
@JsonClass(generateAdapter = true)
data class TMDBSeasonDetails(
    val id: Int,
    val name: String?,
    val overview: String?,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "season_number") val seasonNumber: Int,
    @Json(name = "air_date") val airDate: String?,
    val episodes: List<TMDBEpisode>?
)

/**
 * Episode information from TMDB.
 */
@JsonClass(generateAdapter = true)
data class TMDBEpisode(
    val id: Int,
    val name: String?,
    val overview: String?,
    @Json(name = "episode_number") val episodeNumber: Int,
    @Json(name = "season_number") val seasonNumber: Int,
    @Json(name = "air_date") val airDate: String?,
    val runtime: Int?,
    @Json(name = "still_path") val stillPath: String?,
    @Json(name = "vote_average") val voteAverage: Double?,
    @Json(name = "vote_count") val voteCount: Int?
)

/**
 * Episode summary (for next/last episode to air).
 */
@JsonClass(generateAdapter = true)
data class TMDBEpisodeSummary(
    val id: Int,
    val name: String?,
    @Json(name = "episode_number") val episodeNumber: Int,
    @Json(name = "season_number") val seasonNumber: Int,
    @Json(name = "air_date") val airDate: String?,
    val runtime: Int?
)

/**
 * Network information.
 */
@JsonClass(generateAdapter = true)
data class TMDBNetwork(
    val id: Int,
    val name: String?,
    @Json(name = "logo_path") val logoPath: String?
)

/**
 * Genre information.
 */
@JsonClass(generateAdapter = true)
data class TMDBGenre(
    val id: Int,
    val name: String?
)

/**
 * Aggregated data for a complete show fetch.
 */
data class FullShowData(
    val showDetails: TMDBShowDetails,
    val latestSeasonDetails: TMDBSeasonDetails?
)
