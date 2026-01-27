package io.designtoswiftui.countdown2binge.services.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.designtoswiftui.countdown2binge.models.NotificationStatus
import io.designtoswiftui.countdown2binge.models.NotificationType
import io.designtoswiftui.countdown2binge.models.ScheduledNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    // region Insert/Update/Delete

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: ScheduledNotification): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<ScheduledNotification>)

    @Update
    suspend fun update(notification: ScheduledNotification)

    @Delete
    suspend fun delete(notification: ScheduledNotification)

    @Query("DELETE FROM scheduled_notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM scheduled_notifications WHERE showId = :showId")
    suspend fun deleteByShowId(showId: Long)

    @Query("DELETE FROM scheduled_notifications WHERE status = :status")
    suspend fun deleteByStatus(status: NotificationStatus)

    // endregion

    // region Queries

    @Query("SELECT * FROM scheduled_notifications WHERE id = :id")
    suspend fun getById(id: Long): ScheduledNotification?

    @Query("SELECT * FROM scheduled_notifications ORDER BY scheduledDate ASC")
    fun getAllFlow(): Flow<List<ScheduledNotification>>

    @Query("SELECT * FROM scheduled_notifications ORDER BY scheduledDate ASC")
    suspend fun getAll(): List<ScheduledNotification>

    @Query("SELECT * FROM scheduled_notifications WHERE showId = :showId ORDER BY scheduledDate ASC")
    fun getByShowIdFlow(showId: Long): Flow<List<ScheduledNotification>>

    @Query("SELECT * FROM scheduled_notifications WHERE showId = :showId ORDER BY scheduledDate ASC")
    suspend fun getByShowId(showId: Long): List<ScheduledNotification>

    @Query("SELECT * FROM scheduled_notifications WHERE status = :status ORDER BY scheduledDate ASC")
    fun getByStatusFlow(status: NotificationStatus): Flow<List<ScheduledNotification>>

    @Query("SELECT * FROM scheduled_notifications WHERE status = :status ORDER BY scheduledDate ASC")
    suspend fun getByStatus(status: NotificationStatus): List<ScheduledNotification>

    @Query("SELECT * FROM scheduled_notifications WHERE status IN ('PENDING', 'SCHEDULED', 'QUEUED') ORDER BY scheduledDate ASC LIMIT 1")
    fun getNextScheduledFlow(): Flow<ScheduledNotification?>

    @Query("SELECT * FROM scheduled_notifications WHERE status IN ('PENDING', 'SCHEDULED', 'QUEUED') ORDER BY scheduledDate ASC LIMIT 1")
    suspend fun getNextScheduled(): ScheduledNotification?

    // endregion

    // region Counts

    @Query("SELECT COUNT(*) FROM scheduled_notifications WHERE status IN ('PENDING', 'SCHEDULED', 'QUEUED')")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM scheduled_notifications WHERE status IN ('PENDING', 'SCHEDULED', 'QUEUED')")
    suspend fun getPendingCount(): Int

    @Query("SELECT COUNT(*) FROM scheduled_notifications WHERE showId = :showId AND status IN ('PENDING', 'SCHEDULED', 'QUEUED')")
    fun getPendingCountForShowFlow(showId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM scheduled_notifications WHERE showId = :showId AND status IN ('PENDING', 'SCHEDULED', 'QUEUED')")
    suspend fun getPendingCountForShow(showId: Long): Int

    @Query("SELECT type, COUNT(*) as count FROM scheduled_notifications WHERE status IN ('PENDING', 'SCHEDULED', 'QUEUED') GROUP BY type")
    suspend fun getPendingCountByType(): List<TypeCount>

    // endregion

    // region Status Updates

    @Query("UPDATE scheduled_notifications SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: NotificationStatus)

    @Query("UPDATE scheduled_notifications SET status = 'CANCELLED' WHERE id = :id")
    suspend fun cancel(id: Long)

    @Query("UPDATE scheduled_notifications SET status = 'CANCELLED' WHERE showId = :showId AND status IN ('PENDING', 'SCHEDULED', 'QUEUED')")
    suspend fun cancelAllForShow(showId: Long)

    @Query("UPDATE scheduled_notifications SET status = 'DELIVERED' WHERE id = :id")
    suspend fun markDelivered(id: Long)

    // endregion
}

/**
 * Helper class for type count query results.
 */
data class TypeCount(
    val type: NotificationType,
    val count: Int
)
