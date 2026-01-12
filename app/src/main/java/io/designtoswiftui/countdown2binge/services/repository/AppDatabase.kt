package io.designtoswiftui.countdown2binge.services.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.designtoswiftui.countdown2binge.models.Episode
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.Show

@Database(
    entities = [Show::class, Season::class, Episode::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun showDao(): ShowDao
    abstract fun seasonDao(): SeasonDao
    abstract fun episodeDao(): EpisodeDao
}
