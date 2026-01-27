package io.designtoswiftui.countdown2binge.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

@Entity(tableName = "shows")
data class Show(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tmdbId: Int,
    val title: String,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val status: ShowStatus = ShowStatus.UNKNOWN,
    val addedDate: LocalDate = LocalDate.now(),

    // Sync metadata
    val followedAt: Long = System.currentTimeMillis(),  // Unix timestamp in milliseconds
    val lastSyncedAt: Long? = null,                      // Last sync timestamp, null if never synced
    val isSynced: Boolean = false,                       // Whether this show has been synced to cloud

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
