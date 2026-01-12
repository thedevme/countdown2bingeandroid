package io.designtoswiftui.countdown2binge.services.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.models.ShowStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ShowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(show: Show): Long

    @Update
    suspend fun update(show: Show)

    @Delete
    suspend fun delete(show: Show)

    @Query("SELECT * FROM shows WHERE id = :id")
    suspend fun getById(id: Long): Show?

    @Query("SELECT * FROM shows WHERE tmdbId = :tmdbId")
    suspend fun getByTmdbId(tmdbId: Int): Show?

    @Query("SELECT * FROM shows ORDER BY addedDate DESC")
    fun getAllShows(): Flow<List<Show>>

    @Query("SELECT * FROM shows WHERE status IN (:statuses) ORDER BY addedDate DESC")
    fun getShowsByStatus(statuses: List<ShowStatus>): Flow<List<Show>>

    @Query("SELECT EXISTS(SELECT 1 FROM shows WHERE tmdbId = :tmdbId)")
    suspend fun isShowFollowed(tmdbId: Int): Boolean
}
