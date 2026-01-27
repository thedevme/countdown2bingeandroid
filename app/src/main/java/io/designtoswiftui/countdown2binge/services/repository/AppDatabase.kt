package io.designtoswiftui.countdown2binge.services.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.designtoswiftui.countdown2binge.models.Episode
import io.designtoswiftui.countdown2binge.models.ScheduledNotification
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.Show

@Database(
    entities = [Show::class, Season::class, Episode::class, ScheduledNotification::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun showDao(): ShowDao
    abstract fun seasonDao(): SeasonDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun notificationDao(): NotificationDao

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

        /**
         * Migration from version 2 to 3: Add scheduled_notifications table.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS scheduled_notifications (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        showId INTEGER NOT NULL,
                        showName TEXT NOT NULL,
                        posterPath TEXT,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        subtitle TEXT NOT NULL,
                        scheduledDate TEXT NOT NULL,
                        seasonNumber INTEGER,
                        episodeNumber INTEGER,
                        episodeTitle TEXT,
                        status TEXT NOT NULL DEFAULT 'PENDING',
                        workRequestId TEXT,
                        FOREIGN KEY (showId) REFERENCES shows(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_scheduled_notifications_showId ON scheduled_notifications(showId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_scheduled_notifications_status ON scheduled_notifications(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_scheduled_notifications_scheduledDate ON scheduled_notifications(scheduledDate)")
            }
        }
    }
}
