package io.designtoswiftui.countdown2binge.models

/**
 * Categories for browsing TV shows by genre.
 * Maps display names to TMDB genre IDs.
 */
enum class ShowCategory(val displayName: String, val genreIds: List<Int>) {
    ACTION("Action & Adventure", listOf(10759)),
    ANIMATION("Animation", listOf(16)),
    COMEDY("Comedy", listOf(35)),
    CRIME("Crime", listOf(80)),
    DOCUMENTARY("Documentary", listOf(99)),
    DRAMA("Drama", listOf(18)),
    FAMILY("Family", listOf(10751)),
    MYSTERY("Mystery", listOf(9648)),
    SCI_FI("Sci-Fi & Fantasy", listOf(10765)),
    WESTERN("Western", listOf(37))
}

/**
 * Hardcoded mapping of TMDB genre IDs to display names.
 * Used to convert genre IDs from API responses to readable text.
 */
object GenreMapping {
    private val genreIdToName = mapOf(
        10759 to "Action",
        16 to "Animation",
        35 to "Comedy",
        80 to "Crime",
        99 to "Documentary",
        18 to "Drama",
        10751 to "Family",
        27 to "Horror",
        10762 to "Kids",
        9648 to "Mystery",
        10763 to "News",
        10764 to "Reality",
        10765 to "Sci-Fi",
        10766 to "Soap",
        10767 to "Talk",
        10768 to "Politics",
        37 to "Western"
    )

    /**
     * Convert a list of genre IDs to display names.
     * Returns up to [limit] genre names.
     */
    fun getGenreNames(genreIds: List<Int>, limit: Int = 2): List<String> {
        return genreIds
            .mapNotNull { genreIdToName[it] }
            .take(limit)
    }

    /**
     * Get a single genre name by ID.
     */
    fun getGenreName(genreId: Int): String? = genreIdToName[genreId]
}
