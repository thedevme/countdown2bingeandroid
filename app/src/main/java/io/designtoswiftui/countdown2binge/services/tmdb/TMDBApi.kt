package io.designtoswiftui.countdown2binge.services.tmdb

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for TMDB API endpoints.
 */
interface TMDBApi {

    /**
     * Search for TV shows by query string.
     */
    @GET("search/tv")
    suspend fun search(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TMDBSearchResponse

    /**
     * Get detailed information about a specific TV show.
     */
    @GET("tv/{tv_id}")
    suspend fun getShowDetails(
        @Path("tv_id") showId: Int
    ): TMDBShowDetails

    /**
     * Get detailed information about a specific season.
     */
    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getSeasonDetails(
        @Path("tv_id") showId: Int,
        @Path("season_number") seasonNumber: Int
    ): TMDBSeasonDetails

    /**
     * Get trending TV shows for the day.
     */
    @GET("trending/tv/day")
    suspend fun getTrending(): TMDBSearchResponse

    /**
     * Get currently airing TV shows.
     */
    @GET("tv/on_the_air")
    suspend fun getOnTheAir(
        @Query("page") page: Int = 1
    ): TMDBSearchResponse

    /**
     * Get images for a TV show (logos, backdrops, posters).
     */
    @GET("tv/{tv_id}/images")
    suspend fun getShowImages(
        @Path("tv_id") showId: Int
    ): TMDBImagesResponse

    /**
     * Discover TV shows with various filters.
     */
    @GET("discover/tv")
    suspend fun discover(
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int = 1,
        @Query("with_genres") withGenres: String? = null
    ): TMDBSearchResponse

    /**
     * Get videos (trailers, teasers, clips) for a TV show.
     */
    @GET("tv/{tv_id}/videos")
    suspend fun getShowVideos(
        @Path("tv_id") showId: Int
    ): TMDBVideosResponse

    /**
     * Get credits (cast and crew) for a TV show.
     */
    @GET("tv/{tv_id}/credits")
    suspend fun getShowCredits(
        @Path("tv_id") showId: Int
    ): TMDBCreditsResponse

    /**
     * Get recommended TV shows based on a specific show.
     */
    @GET("tv/{tv_id}/recommendations")
    suspend fun getShowRecommendations(
        @Path("tv_id") showId: Int,
        @Query("page") page: Int = 1
    ): TMDBSearchResponse
}
