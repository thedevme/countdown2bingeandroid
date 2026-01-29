package io.designtoswiftui.countdown2binge.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Room entity representing a season of a TV show.
 *
 * Each season belongs to a single Show (many-to-one relationship).
 * Cascade delete ensures seasons are automatically removed when their
 * parent show is deleted.
 */
@Entity(
    tableName = "seasons",
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
data class Season(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tmdbId: Int,
    val seasonNumber: Int,
    val name: String = "",
    val overview: String = "",
    val premiereDate: LocalDate?,
    val finaleDate: LocalDate?,
    val isFinaleEstimated: Boolean = false,
    val episodeCount: Int,
    val airedEpisodeCount: Int = 0,
    val releasePattern: ReleasePattern = ReleasePattern.UNKNOWN,
    val state: SeasonState = SeasonState.ANTICIPATED,
    val voteAverage: Double? = null,
    val hasWatched: Boolean = false,
    val watchedDate: LocalDate? = null,
    val posterPath: String?,
    val showId: Long
)
