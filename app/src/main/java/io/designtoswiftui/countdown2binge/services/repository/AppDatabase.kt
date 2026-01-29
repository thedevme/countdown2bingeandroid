package io.designtoswiftui.countdown2binge.services.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.designtoswiftui.countdown2binge.models.Episode
import io.designtoswiftui.countdown2binge.models.Genre
import io.designtoswiftui.countdown2binge.models.Network
import io.designtoswiftui.countdown2binge.models.ScheduledNotification
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.Show

@Database(
    entities = [
        Show::class,
        Season::class,
        Episode::class,
        Genre::class,
        Network::class,
        ScheduledNotification::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun showDao(): ShowDao
    abstract fun seasonDao(): SeasonDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun genreDao(): GenreDao
    abstract fun networkDao(): NetworkDao
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

        /**
         * Migration from version 3 to 4: Add relatedShowIdsJson column for franchise/spinoff data.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add relatedShowIdsJson column (nullable) to store related TMDB IDs as JSON
                db.execSQL(
                    "ALTER TABLE shows ADD COLUMN relatedShowIdsJson TEXT"
                )
            }
        }

        /**
         * Migration from version 4 to 5: Add new fields to Show/Season/Episode,
         * create Genre and Network tables.
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // ===== SHOW TABLE: Add 12 new columns =====
                db.execSQL("ALTER TABLE shows ADD COLUMN logoPath TEXT")
                db.execSQL("ALTER TABLE shows ADD COLUMN firstAirDate TEXT")
                db.execSQL("ALTER TABLE shows ADD COLUMN statusRaw TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE shows ADD COLUMN numberOfSeasons INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE shows ADD COLUMN numberOfEpisodes INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE shows ADD COLUMN inProduction INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE shows ADD COLUMN voteAverage REAL")
                db.execSQL("ALTER TABLE shows ADD COLUMN isShowAdded INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE shows ADD COLUMN lastUpdated INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE shows ADD COLUMN currentSeasonStartDate TEXT")
                db.execSQL("ALTER TABLE shows ADD COLUMN currentSeasonFinaleDate TEXT")
                db.execSQL("ALTER TABLE shows ADD COLUMN hasMidseasonBreak INTEGER NOT NULL DEFAULT 0")

                // ===== SEASON TABLE: Add 4 new columns =====
                db.execSQL("ALTER TABLE seasons ADD COLUMN name TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE seasons ADD COLUMN overview TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE seasons ADD COLUMN voteAverage REAL")
                db.execSQL("ALTER TABLE seasons ADD COLUMN hasWatched INTEGER NOT NULL DEFAULT 0")

                // ===== EPISODE TABLE: Add 4 new columns =====
                db.execSQL("ALTER TABLE episodes ADD COLUMN stillPath TEXT")
                db.execSQL("ALTER TABLE episodes ADD COLUMN episodeType TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE episodes ADD COLUMN seasonNumber INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE episodes ADD COLUMN voteAverage REAL")

                // ===== CREATE GENRE TABLE =====
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS genres (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tmdbId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        showId INTEGER NOT NULL,
                        FOREIGN KEY (showId) REFERENCES shows(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_genres_showId ON genres(showId)")

                // ===== CREATE NETWORK TABLE =====
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS networks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tmdbId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        logoPath TEXT,
                        showId INTEGER NOT NULL,
                        FOREIGN KEY (showId) REFERENCES shows(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_networks_showId ON networks(showId)")
            }
        }
    }
}
