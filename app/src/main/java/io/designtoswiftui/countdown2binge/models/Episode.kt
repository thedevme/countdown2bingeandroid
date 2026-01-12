package io.designtoswiftui.countdown2binge.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

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
    val name: String,
    val airDate: LocalDate?,
    val runtime: Int?,
    val overview: String?,
    val isWatched: Boolean = false,
    val seasonId: Long
)
