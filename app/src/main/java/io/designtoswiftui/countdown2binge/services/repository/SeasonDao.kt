package io.designtoswiftui.countdown2binge.services.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.SeasonState
import io.designtoswiftui.countdown2binge.models.SeasonWithEpisodes
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SeasonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(season: Season): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(seasons: List<Season>)

    @Update
    suspend fun update(season: Season)

    @Delete
    suspend fun delete(season: Season)

    @Query("SELECT * FROM seasons WHERE id = :id")
    suspend fun getById(id: Long): Season?

    @Query("SELECT * FROM seasons WHERE showId = :showId ORDER BY seasonNumber ASC")
    fun getSeasonsForShow(showId: Long): Flow<List<Season>>

    @Query("SELECT * FROM seasons WHERE showId = :showId ORDER BY seasonNumber ASC")
    suspend fun getSeasonsForShowSync(showId: Long): List<Season>

    @Query("SELECT * FROM seasons WHERE state = :state")
    fun getSeasonsByState(state: SeasonState): Flow<List<Season>>

    @Query("SELECT * FROM seasons WHERE state IN (:states)")
    fun getSeasonsByStates(states: List<SeasonState>): Flow<List<Season>>

    @Query("UPDATE seasons SET watchedDate = :date, state = :state WHERE id = :seasonId")
    suspend fun markWatched(seasonId: Long, date: LocalDate, state: SeasonState = SeasonState.WATCHED)

    @Query("UPDATE seasons SET watchedDate = NULL, state = :state WHERE id = :seasonId")
    suspend fun unmarkWatched(seasonId: Long, state: SeasonState)

    // Relationship queries

    @Transaction
    @Query("SELECT * FROM seasons WHERE id = :id")
    suspend fun getSeasonWithEpisodes(id: Long): SeasonWithEpisodes?

    @Transaction
    @Query("SELECT * FROM seasons WHERE id = :id")
    fun getSeasonWithEpisodesFlow(id: Long): Flow<SeasonWithEpisodes?>

    @Transaction
    @Query("SELECT * FROM seasons WHERE showId = :showId ORDER BY seasonNumber ASC")
    fun getSeasonsWithEpisodesForShow(showId: Long): Flow<List<SeasonWithEpisodes>>

    @Transaction
    @Query("SELECT * FROM seasons WHERE showId = :showId ORDER BY seasonNumber ASC")
    suspend fun getSeasonsWithEpisodesForShowSync(showId: Long): List<SeasonWithEpisodes>
}
