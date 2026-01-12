package io.designtoswiftui.countdown2binge.services.repository

import androidx.room.TypeConverter
import io.designtoswiftui.countdown2binge.models.ReleasePattern
import io.designtoswiftui.countdown2binge.models.SeasonState
import io.designtoswiftui.countdown2binge.models.ShowStatus
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromSeasonState(state: SeasonState): String {
        return state.name
    }

    @TypeConverter
    fun toSeasonState(stateName: String): SeasonState {
        return SeasonState.valueOf(stateName)
    }

    @TypeConverter
    fun fromShowStatus(status: ShowStatus): String {
        return status.name
    }

    @TypeConverter
    fun toShowStatus(statusName: String): ShowStatus {
        return ShowStatus.valueOf(statusName)
    }

    @TypeConverter
    fun fromReleasePattern(pattern: ReleasePattern): String {
        return pattern.name
    }

    @TypeConverter
    fun toReleasePattern(patternName: String): ReleasePattern {
        return ReleasePattern.valueOf(patternName)
    }
}
