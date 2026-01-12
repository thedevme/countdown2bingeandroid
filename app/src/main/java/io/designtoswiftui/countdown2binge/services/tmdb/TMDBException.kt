package io.designtoswiftui.countdown2binge.services.tmdb

/**
 * Sealed class representing errors from TMDB API operations.
 */
sealed class TMDBException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /**
     * Network error - no internet connection or server unreachable.
     */
    class NetworkError(cause: Throwable? = null) : TMDBException(
        "Network error: Unable to reach TMDB servers",
        cause
    )

    /**
     * API error with specific HTTP status code.
     */
    class ApiError(val statusCode: Int, message: String) : TMDBException(
        "API error ($statusCode): $message"
    )

    /**
     * Show not found (404).
     */
    class ShowNotFound(val showId: Int) : TMDBException(
        "Show not found: $showId"
    )

    /**
     * Season not found (404).
     */
    class SeasonNotFound(val showId: Int, val seasonNumber: Int) : TMDBException(
        "Season not found: Show $showId, Season $seasonNumber"
    )

    /**
     * Rate limited by TMDB API.
     */
    class RateLimited : TMDBException(
        "Rate limited: Too many requests to TMDB API"
    )

    /**
     * Invalid API key.
     */
    class InvalidApiKey : TMDBException(
        "Invalid API key: Check your TMDB API key configuration"
    )

    /**
     * Parsing error - response couldn't be parsed.
     */
    class ParseError(cause: Throwable? = null) : TMDBException(
        "Parse error: Unable to parse TMDB response",
        cause
    )

    /**
     * Unknown error.
     */
    class Unknown(message: String, cause: Throwable? = null) : TMDBException(
        "Unknown error: $message",
        cause
    )
}
