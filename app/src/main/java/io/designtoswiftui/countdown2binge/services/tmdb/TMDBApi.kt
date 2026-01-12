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
     * Get trending TV shows.
     */
    @GET("trending/tv/week")
    suspend fun getTrending(): TMDBSearchResponse

    /**
     * Discover TV shows with various filters.
     */
    @GET("discover/tv")
    suspend fun discover(
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int = 1
    ): TMDBSearchResponse
}
