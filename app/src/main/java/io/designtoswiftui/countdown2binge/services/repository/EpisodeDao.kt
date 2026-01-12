package io.designtoswiftui.countdown2binge.services.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.designtoswiftui.countdown2binge.models.Episode
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: Episode): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(episodes: List<Episode>)

    @Update
    suspend fun update(episode: Episode)

    @Delete
    suspend fun delete(episode: Episode)

    @Query("SELECT * FROM episodes WHERE id = :id")
    suspend fun getById(id: Long): Episode?

    @Query("SELECT * FROM episodes WHERE seasonId = :seasonId ORDER BY episodeNumber ASC")
    fun getEpisodesForSeason(seasonId: Long): Flow<List<Episode>>

    @Query("SELECT * FROM episodes WHERE seasonId = :seasonId ORDER BY episodeNumber ASC")
    suspend fun getEpisodesForSeasonSync(seasonId: Long): List<Episode>

    @Query("UPDATE episodes SET isWatched = :watched WHERE id = :episodeId")
    suspend fun setWatched(episodeId: Long, watched: Boolean)

    @Query("SELECT COUNT(*) FROM episodes WHERE seasonId = :seasonId AND isWatched = 1")
    suspend fun getWatchedCountForSeason(seasonId: Long): Int
}
