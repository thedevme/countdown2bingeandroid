package io.designtoswiftui.countdown2binge.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * A scheduled notification stored in the database.
 */
@Entity(
    tableName = "scheduled_notifications",
    foreignKeys = [
        ForeignKey(
            entity = Show::class,
            parentColumns = ["id"],
            childColumns = ["showId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("showId"),
        Index("status"),
        Index("scheduledDate")
    ]
)
data class ScheduledNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val showId: Long,
    val showName: String,
    val posterPath: String?,
    val type: NotificationType,
    val title: String,
    val subtitle: String,
    val scheduledDate: LocalDateTime,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val episodeTitle: String?,
    val status: NotificationStatus = NotificationStatus.PENDING,
    val workRequestId: String? = null // WorkManager request ID for cancellation
) {
    /**
     * Format the notification type for display.
     */
    fun typeDisplayName(): String {
        return when (type) {
            NotificationType.PREMIERE -> "SEASON PREMIERE"
            NotificationType.NEW_EPISODE -> "NEW EPISODE"
            NotificationType.FINALE_REMINDER -> "SEASON FINALE"
            NotificationType.BINGE_READY -> "BINGE READY"
        }
    }

    /**
     * Format the episode info for display.
     */
    fun episodeInfo(): String? {
        if (seasonNumber == null) return null
        val season = "S${seasonNumber.toString().padStart(2, '0')}"
        val episode = episodeNumber?.let { "E${it.toString().padStart(2, '0')}" } ?: ""
        val title = episodeTitle?.let { ": \"$it\"" } ?: ""
        return "$season$episode$title"
    }
}
