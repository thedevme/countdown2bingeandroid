package io.designtoswiftui.countdown2binge.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

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
    val premiereDate: LocalDate?,
    val finaleDate: LocalDate?,
    val isFinaleEstimated: Boolean = false,
    val episodeCount: Int,
    val airedEpisodeCount: Int = 0,
    val releasePattern: ReleasePattern = ReleasePattern.UNKNOWN,
    val state: SeasonState = SeasonState.ANTICIPATED,
    val watchedDate: LocalDate? = null,
    val posterPath: String?,
    val showId: Long
)
