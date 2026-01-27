package io.designtoswiftui.countdown2binge.services.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.designtoswiftui.countdown2binge.models.Episode
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.Show

@Database(
    entities = [Show::class, Season::class, Episode::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun showDao(): ShowDao
    abstract fun seasonDao(): SeasonDao
    abstract fun episodeDao(): EpisodeDao

    companion object {
        /**
         * Migration from version 1 to 2: Add sync metadata columns to shows table.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add followedAt column with default value of current timestamp
                db.execSQL(
                    "ALTER TABLE shows ADD COLUMN followedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}"
                )
                // Add lastSyncedAt column (nullable)
                db.execSQL(
                    "ALTER TABLE shows ADD COLUMN lastSyncedAt INTEGER"
                )
                // Add isSynced column with default false
                db.execSQL(
                    "ALTER TABLE shows ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }
}
