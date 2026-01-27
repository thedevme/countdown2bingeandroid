package io.designtoswiftui.countdown2binge.services.tmdb

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.designtoswiftui.countdown2binge.util.Secrets
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service class that provides access to TMDB API with proper error handling.
 */
@Singleton
class TMDBService @Inject constructor() {

    companion object {
        private const val BASE_URL = "https://api.themoviedb.org/3/"
        private const val TIMEOUT_SECONDS = 30L
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
        const val POSTER_SIZE = "w500"
        const val POSTER_SIZE_SMALL = "w342"
        const val BACKDROP_SIZE = "w780"
        const val LOGO_SIZE = "w300"
        const val PROFILE_SIZE = "w185"
    }

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val url = originalRequest.url.newBuilder()
            .addQueryParameter("api_key", Secrets.TMDB_API_KEY)
            .build()
        val newRequest = originalRequest.newBuilder()
            .url(url)
            .build()
        chain.proceed(newRequest)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: TMDBApi = retrofit.create(TMDBApi::class.java)

    /**
     * Search for TV shows by query.
     */
    suspend fun search(query: String, page: Int = 1): Result<TMDBSearchResponse> {
        return executeRequest { api.search(query, page) }
    }

    /**
     * Get detailed show information.
     */
    suspend fun getShowDetails(showId: Int): Result<TMDBShowDetails> {
        return executeRequest(
            notFoundException = { TMDBException.ShowNotFound(showId) }
        ) { api.getShowDetails(showId) }
    }

    /**
     * Get detailed season information.
     */
    suspend fun getSeasonDetails(showId: Int, seasonNumber: Int): Result<TMDBSeasonDetails> {
        return executeRequest(
            notFoundException = { TMDBException.SeasonNotFound(showId, seasonNumber) }
        ) { api.getSeasonDetails(showId, seasonNumber) }
    }

    /**
     * Get trending TV shows.
     */
    suspend fun getTrending(): Result<TMDBSearchResponse> {
        return executeRequest { api.getTrending() }
    }

    /**
     * Get currently airing TV shows.
     */
    suspend fun getOnTheAir(page: Int = 1): Result<TMDBSearchResponse> {
        return executeRequest { api.getOnTheAir(page) }
    }

    /**
     * Get images for a TV show.
     */
    suspend fun getShowImages(showId: Int): Result<TMDBImagesResponse> {
        return executeRequest { api.getShowImages(showId) }
    }

    /**
     * Get TV shows by genre.
     */
    suspend fun getShowsByGenre(genreIds: List<Int>, page: Int = 1): Result<TMDBSearchResponse> {
        val genreString = genreIds.joinToString(",")
        return executeRequest { api.discover(withGenres = genreString, page = page) }
    }

    /**
     * Get English logo path for a show.
     * Returns the first English logo, or null if not found.
     */
    suspend fun getEnglishLogoPath(showId: Int): String? {
        return getShowImages(showId).getOrNull()?.logos
            ?.firstOrNull { it.languageCode == "en" }
            ?.filePath
    }

    /**
     * Get videos (trailers, teasers, clips) for a TV show.
     */
    suspend fun getShowVideos(showId: Int): Result<TMDBVideosResponse> {
        return executeRequest { api.getShowVideos(showId) }
    }

    /**
     * Get credits (cast and crew) for a TV show.
     */
    suspend fun getShowCredits(showId: Int): Result<TMDBCreditsResponse> {
        return executeRequest { api.getShowCredits(showId) }
    }

    /**
     * Get recommended TV shows based on a specific show.
     */
    suspend fun getShowRecommendations(showId: Int, page: Int = 1): Result<TMDBSearchResponse> {
        return executeRequest { api.getShowRecommendations(showId, page) }
    }

    /**
     * Get watch providers (streaming services) for a TV show.
     * Returns providers for the US region by default.
     */
    suspend fun getWatchProviders(showId: Int, region: String = "US"): Result<List<TMDBWatchProvider>> {
        return try {
            val response = executeRequest { api.getWatchProviders(showId) }
            response.map { providersResponse ->
                val regionData = providersResponse.results?.get(region)
                // Combine flatrate (subscription) and ads (free with ads) providers
                val providers = mutableListOf<TMDBWatchProvider>()
                regionData?.flatrate?.let { providers.addAll(it) }
                regionData?.ads?.let { providers.addAll(it) }
                providers.distinctBy { it.providerId }.sortedBy { it.displayPriority ?: 100 }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Build full image URL from path.
     */
    fun buildImageUrl(path: String?, size: String = POSTER_SIZE): String? {
        return path?.let { "$IMAGE_BASE_URL$size$it" }
    }

    /**
     * Build full poster URL.
     */
    fun buildPosterUrl(path: String?): String? {
        return buildImageUrl(path, POSTER_SIZE)
    }

    /**
     * Build full backdrop URL.
     */
    fun buildBackdropUrl(path: String?): String? {
        return buildImageUrl(path, BACKDROP_SIZE)
    }

    /**
     * Build full logo URL.
     */
    fun buildLogoUrl(path: String?): String? {
        return buildImageUrl(path, LOGO_SIZE)
    }

    /**
     * Execute a request with proper error handling.
     */
    private suspend fun <T> executeRequest(
        notFoundException: (() -> TMDBException)? = null,
        request: suspend () -> T
    ): Result<T> {
        return try {
            Result.success(request())
        } catch (e: HttpException) {
            Result.failure(handleHttpException(e, notFoundException))
        } catch (e: IOException) {
            Result.failure(TMDBException.NetworkError(e))
        } catch (e: Exception) {
            Result.failure(TMDBException.Unknown(e.message ?: "Unknown error", e))
        }
    }

    /**
     * Handle HTTP exceptions and convert to TMDBException.
     */
    private fun handleHttpException(
        e: HttpException,
        notFoundException: (() -> TMDBException)?
    ): TMDBException {
        return when (e.code()) {
            401 -> TMDBException.InvalidApiKey()
            404 -> notFoundException?.invoke() ?: TMDBException.ApiError(404, "Not found")
            429 -> TMDBException.RateLimited()
            else -> TMDBException.ApiError(e.code(), e.message())
        }
    }
}
