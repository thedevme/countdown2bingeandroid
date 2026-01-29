package io.designtoswiftui.countdown2binge.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a genre associated with a show.
 *
 * Each genre record is tied to a specific show (one-to-many relationship).
 * The same TMDB genre (e.g., "Drama" with tmdbId 18) can exist multiple times
 * in the database, once per show that has that genre.
 *
 * Cascade delete ensures genres are automatically removed when their
 * parent show is deleted.
 */
@Entity(
    tableName = "genres",
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
data class Genre(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tmdbId: Int,
    val name: String,
    val showId: Long
)
