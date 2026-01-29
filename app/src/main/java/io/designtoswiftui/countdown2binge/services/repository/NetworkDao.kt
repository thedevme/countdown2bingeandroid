package io.designtoswiftui.countdown2binge.services.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.designtoswiftui.countdown2binge.models.Network
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [Network] entity.
 *
 * Provides CRUD operations for networks/streaming platforms associated with shows.
 * Networks are automatically deleted when their parent show is deleted
 * due to CASCADE delete on the foreign key.
 */
@Dao
interface NetworkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(network: Network): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(networks: List<Network>)

    @Query("DELETE FROM networks WHERE showId = :showId")
    suspend fun deleteByShowId(showId: Long)

    @Query("SELECT * FROM networks WHERE showId = :showId")
    fun getByShowId(showId: Long): Flow<List<Network>>

    @Query("SELECT * FROM networks WHERE showId = :showId")
    suspend fun getByShowIdOnce(showId: Long): List<Network>
}
