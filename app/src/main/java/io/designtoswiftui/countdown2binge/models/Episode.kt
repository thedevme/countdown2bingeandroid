package io.designtoswiftui.countdown2binge.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Room entity representing an episode of a TV show season.
 *
 * Each episode belongs to a single Season (many-to-one relationship).
 * Cascade delete ensures episodes are automatically removed when their
 * parent season is deleted.
 *
 * The [seasonNumber] field is denormalized from [Season] for efficient
 * filtering without requiring a join.
 */
@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = Season::class,
            parentColumns = ["id"],
            childColumns = ["seasonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("seasonId")]
)
data class Episode(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tmdbId: Int,
    val episodeNumber: Int,
    val seasonNumber: Int = 0,
    val name: String,
    val overview: String?,
    val airDate: LocalDate?,
    val stillPath: String? = null,
    val runtime: Int?,
    val episodeType: String = "",
    val voteAverage: Double? = null,
    val isWatched: Boolean = false,
    val seasonId: Long
)
