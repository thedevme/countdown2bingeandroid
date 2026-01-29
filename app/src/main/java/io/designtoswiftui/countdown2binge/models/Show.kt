package io.designtoswiftui.countdown2binge.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

/**
 * Room entity representing a TV show followed by the user.
 *
 * This is the root entity in the data hierarchy:
 * Show -> Seasons -> Episodes
 * Show -> Genres (CASCADE)
 * Show -> Networks (CASCADE)
 */
@Entity(tableName = "shows")
data class Show(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tmdbId: Int,
    val title: String,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val logoPath: String? = null,

    // Airing info
    val firstAirDate: LocalDate? = null,
    val status: ShowStatus = ShowStatus.UNKNOWN,
    val statusRaw: String = "",
    val numberOfSeasons: Int = 0,
    val numberOfEpisodes: Int = 0,
    val inProduction: Boolean = false,
    val voteAverage: Double? = null,

    // Current season tracking
    val currentSeasonStartDate: LocalDate? = null,
    val currentSeasonFinaleDate: LocalDate? = null,
    val hasMidseasonBreak: Boolean = false,

    // User tracking
    val addedDate: LocalDate = LocalDate.now(),
    val isShowAdded: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis(),

    // Sync metadata
    val followedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null,
    val isSynced: Boolean = false,

    // Franchise/Spinoff data - JSON array of related TMDB IDs
    val relatedShowIdsJson: String? = null
) {
    /**
     * Get related show IDs (spinoffs and parent show).
     * Parsed from JSON storage.
     */
    val relatedShowIds: List<Int>
        @Ignore
        get() = relatedShowIdsJson?.let {
            try {
                Gson().fromJson(it, object : TypeToken<List<Int>>() {}.type)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()

    /**
     * Whether this show has spinoffs or is part of a franchise.
     */
    val hasSpinoffs: Boolean
        @Ignore
        get() = relatedShowIds.isNotEmpty()

    companion object {
        /**
         * Convert a list of IDs to JSON for storage.
         */
        fun idsToJson(ids: List<Int>): String {
            return Gson().toJson(ids)
        }
    }
}
