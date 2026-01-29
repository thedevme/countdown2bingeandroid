package io.designtoswiftui.countdown2binge.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a network/streaming platform associated with a show.
 *
 * Each network record is tied to a specific show (one-to-many relationship).
 * The same TMDB network (e.g., "Netflix" with tmdbId 213) can exist multiple
 * times in the database, once per show that airs on that network.
 *
 * Cascade delete ensures networks are automatically removed when their
 * parent show is deleted.
 */
@Entity(
    tableName = "networks",
    foreignKeys = [
        ForeignKey(
            entity = Show::class,
            parentColumns = ["id"],
            childColumns = ["showId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("showId")]
)
data class Network(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tmdbId: Int,
    val name: String,
    val logoPath: String?,
    val showId: Long
)
