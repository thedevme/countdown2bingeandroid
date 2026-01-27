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
    val popularity: Double?,
    @Json(name = "genre_ids") val genreIds: List<Int>? = null
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
    @Json(name = "air_date") val airDate: String?,
    @Json(name = "vote_average") val voteAverage: Double?
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

/**
 * Response from TMDB images endpoint.
 */
@JsonClass(generateAdapter = true)
data class TMDBImagesResponse(
    val id: Int,
    val logos: List<TMDBLogo>?,
    val backdrops: List<TMDBImage>?,
    val posters: List<TMDBImage>?
)

/**
 * Logo image from TMDB.
 */
@JsonClass(generateAdapter = true)
data class TMDBLogo(
    @Json(name = "file_path") val filePath: String,
    @Json(name = "iso_639_1") val languageCode: String?,
    val width: Int?,
    val height: Int?,
    @Json(name = "aspect_ratio") val aspectRatio: Double?
)

/**
 * Generic image from TMDB (backdrop or poster).
 */
@JsonClass(generateAdapter = true)
data class TMDBImage(
    @Json(name = "file_path") val filePath: String,
    @Json(name = "iso_639_1") val languageCode: String?,
    val width: Int?,
    val height: Int?,
    @Json(name = "aspect_ratio") val aspectRatio: Double?
)

/**
 * Response from TMDB videos endpoint.
 */
@JsonClass(generateAdapter = true)
data class TMDBVideosResponse(
    val id: Int,
    val results: List<TMDBVideo>
)

/**
 * Video/trailer from TMDB.
 */
@JsonClass(generateAdapter = true)
data class TMDBVideo(
    val id: String,
    val key: String,           // YouTube video ID
    val name: String,
    val site: String,          // "YouTube"
    val type: String,          // "Trailer", "Teaser", "Clip"
    val official: Boolean?
) {
    val youtubeUrl: String get() = "https://www.youtube.com/watch?v=$key"
    val thumbnailUrl: String get() = "https://img.youtube.com/vi/$key/mqdefault.jpg"
}

/**
 * Response from TMDB credits endpoint.
 */
@JsonClass(generateAdapter = true)
data class TMDBCreditsResponse(
    val id: Int,
    val cast: List<TMDBCastMember>,
    val crew: List<TMDBCrewMember>
)

/**
 * Cast member from TMDB.
 */
@JsonClass(generateAdapter = true)
data class TMDBCastMember(
    val id: Int,
    val name: String,
    val character: String?,
    @Json(name = "profile_path") val profilePath: String?,
    val order: Int?
)

/**
 * Crew member from TMDB.
 */
@JsonClass(generateAdapter = true)
data class TMDBCrewMember(
    val id: Int,
    val name: String,
    val job: String?,
    val department: String?,
    @Json(name = "profile_path") val profilePath: String?
)

/**
 * Response from TMDB watch providers endpoint.
 */
@JsonClass(generateAdapter = true)
data class TMDBWatchProvidersResponse(
    val id: Int,
    val results: Map<String, TMDBWatchProviderRegion>?
)

/**
 * Watch provider data for a specific region.
 */
@JsonClass(generateAdapter = true)
data class TMDBWatchProviderRegion(
    val link: String?,
    val flatrate: List<TMDBWatchProvider>?,
    val rent: List<TMDBWatchProvider>?,
    val buy: List<TMDBWatchProvider>?,
    val ads: List<TMDBWatchProvider>?
)

/**
 * Individual watch provider (streaming service).
 */
@JsonClass(generateAdapter = true)
data class TMDBWatchProvider(
    @Json(name = "provider_id") val providerId: Int,
    @Json(name = "provider_name") val providerName: String,
    @Json(name = "logo_path") val logoPath: String?,
    @Json(name = "display_priority") val displayPriority: Int?
)
