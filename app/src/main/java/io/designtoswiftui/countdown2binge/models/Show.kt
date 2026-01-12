package io.designtoswiftui.countdown2binge.models

import androidx.room.Entity
import androidx.room.PrimaryKey
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
    val addedDate: LocalDate = LocalDate.now()
)
