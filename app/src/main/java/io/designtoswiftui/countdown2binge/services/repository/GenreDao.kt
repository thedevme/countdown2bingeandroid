package io.designtoswiftui.countdown2binge.services.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.designtoswiftui.countdown2binge.models.Genre
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [Genre] entity.
 *
 * Provides CRUD operations for genres associated with shows.
 * Genres are automatically deleted when their parent show is deleted
 * due to CASCADE delete on the foreign key.
 */
@Dao
interface GenreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(genre: Genre): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(genres: List<Genre>)

    @Query("DELETE FROM genres WHERE showId = :showId")
    suspend fun deleteByShowId(showId: Long)

    @Query("SELECT * FROM genres WHERE showId = :showId")
    fun getByShowId(showId: Long): Flow<List<Genre>>

    @Query("SELECT * FROM genres WHERE showId = :showId")
    suspend fun getByShowIdOnce(showId: Long): List<Genre>
}
