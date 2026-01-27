package io.designtoswiftui.countdown2binge.services.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import io.designtoswiftui.countdown2binge.R
import io.designtoswiftui.countdown2binge.models.Episode
import io.designtoswiftui.countdown2binge.models.FinaleReminderTiming
import io.designtoswiftui.countdown2binge.models.NotificationSettings
import io.designtoswiftui.countdown2binge.models.NotificationStatus
import io.designtoswiftui.countdown2binge.models.NotificationType
import io.designtoswiftui.countdown2binge.models.ScheduledNotification
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.services.repository.NotificationDao
import io.designtoswiftui.countdown2binge.services.repository.TypeCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for scheduling and delivering notifications.
 */
@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationDao: NotificationDao,
    private val settingsRepository: NotificationSettingsRepository
) {
    companion object {
        // Notification Channel IDs
        const val CHANNEL_PREMIERE = "premiere_notifications"
        const val CHANNEL_NEW_EPISODE = "new_episode_notifications"
        const val CHANNEL_FINALE = "finale_notifications"
        const val CHANNEL_BINGE_READY = "binge_ready_notifications"

        // WorkManager tags
        const val WORK_TAG_NOTIFICATIONS = "notification_work"
    }

    private val workManager = WorkManager.getInstance(context)

    init {
        createNotificationChannels()
    }

    // region Notification Channels

    private fun createNotificationChannels() {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        val channels = listOf(
            NotificationChannel(
                CHANNEL_PREMIERE,
                "Season Premieres",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for season premieres"
            },
            NotificationChannel(
                CHANNEL_NEW_EPISODE,
                "New Episodes",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for new episode releases"
            },
            NotificationChannel(
                CHANNEL_FINALE,
                "Season Finales",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for upcoming season finales"
            },
            NotificationChannel(
                CHANNEL_BINGE_READY,
                "Binge Ready",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when a season is ready to binge"
            }
        )

        channels.forEach { notificationManager.createNotificationChannel(it) }
    }

    private fun getChannelForType(type: NotificationType): String {
        return when (type) {
            NotificationType.PREMIERE -> CHANNEL_PREMIERE
            NotificationType.NEW_EPISODE -> CHANNEL_NEW_EPISODE
            NotificationType.FINALE_REMINDER -> CHANNEL_FINALE
            NotificationType.BINGE_READY -> CHANNEL_BINGE_READY
        }
    }

    // endregion

    // region Permission Checking

    /**
     * Check if notification permission is granted (Android 13+).
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Check if notifications are enabled in system settings.
     */
    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    // endregion

    // region Scheduling

    /**
     * Schedule notifications for a show based on its seasons and episodes.
     */
    suspend fun scheduleNotificationsForShow(
        show: Show,
        seasons: List<Season>,
        episodes: List<Episode>
    ) {
        val globalSettings = settingsRepository.globalSettings.first()
        val showSettings = settingsRepository.getShowSettings(show.id).first()
        val effectiveSettings = showSettings.resolveWith(globalSettings)

        // Cancel existing notifications for this show
        cancelNotificationsForShow(show.id)

        val now = LocalDateTime.now()
        val notificationsToSchedule = mutableListOf<ScheduledNotification>()

        for (season in seasons) {
            // Skip season 0 (specials)
            if (season.seasonNumber == 0) continue

            val seasonEpisodes = episodes.filter { it.seasonId == season.id }

            // Season Premiere notification
            if (effectiveSettings.seasonPremiere && season.premiereDate != null) {
                val premiereDateTime = season.premiereDate.atTime(9, 0) // 9 AM
                if (premiereDateTime.isAfter(now)) {
                    notificationsToSchedule.add(
                        ScheduledNotification(
                            showId = show.id,
                            showName = show.title,
                            posterPath = show.posterPath,
                            type = NotificationType.PREMIERE,
                            title = "Season ${season.seasonNumber} Premieres Today",
                            subtitle = show.title,
                            scheduledDate = premiereDateTime,
                            seasonNumber = season.seasonNumber,
                            episodeNumber = 1,
                            episodeTitle = seasonEpisodes.firstOrNull()?.name
                        )
                    )
                }
            }

            // Finale Reminder notification
            if (effectiveSettings.finaleReminder && season.finaleDate != null) {
                val reminderDate = when (effectiveSettings.finaleReminderTiming) {
                    FinaleReminderTiming.DAY_OF -> season.finaleDate
                    FinaleReminderTiming.ONE_DAY_BEFORE -> season.finaleDate.minusDays(1)
                    FinaleReminderTiming.TWO_DAYS_BEFORE -> season.finaleDate.minusDays(2)
                    FinaleReminderTiming.ONE_WEEK_BEFORE -> season.finaleDate.minusWeeks(1)
                }
                val reminderDateTime = reminderDate.atTime(9, 0)
                if (reminderDateTime.isAfter(now)) {
                    val lastEpisode = seasonEpisodes.lastOrNull()
                    notificationsToSchedule.add(
                        ScheduledNotification(
                            showId = show.id,
                            showName = show.title,
                            posterPath = show.posterPath,
                            type = NotificationType.FINALE_REMINDER,
                            title = "Season ${season.seasonNumber} Finale",
                            subtitle = formatFinaleReminderSubtitle(effectiveSettings.finaleReminderTiming, show.title),
                            scheduledDate = reminderDateTime,
                            seasonNumber = season.seasonNumber,
                            episodeNumber = lastEpisode?.episodeNumber,
                            episodeTitle = lastEpisode?.name
                        )
                    )
                }
            }

            // New Episode notifications (for airing shows)
            if (effectiveSettings.newEpisodes) {
                for (episode in seasonEpisodes) {
                    if (episode.airDate != null) {
                        val episodeDateTime = episode.airDate.atTime(9, 0)
                        // Skip premiere (already handled) and past episodes
                        if (episode.episodeNumber > 1 && episodeDateTime.isAfter(now)) {
                            notificationsToSchedule.add(
                                ScheduledNotification(
                                    showId = show.id,
                                    showName = show.title,
                                    posterPath = show.posterPath,
                                    type = NotificationType.NEW_EPISODE,
                                    title = "New Episode",
                                    subtitle = "${show.title} S${season.seasonNumber}E${episode.episodeNumber}",
                                    scheduledDate = episodeDateTime,
                                    seasonNumber = season.seasonNumber,
                                    episodeNumber = episode.episodeNumber,
                                    episodeTitle = episode.name
                                )
                            )
                        }
                    }
                }
            }

            // Binge Ready notification
            if (effectiveSettings.bingeReady && season.finaleDate != null) {
                // Schedule for day after finale
                val bingeReadyDate = season.finaleDate.plusDays(1).atTime(9, 0)
                if (bingeReadyDate.isAfter(now)) {
                    notificationsToSchedule.add(
                        ScheduledNotification(
                            showId = show.id,
                            showName = show.title,
                            posterPath = show.posterPath,
                            type = NotificationType.BINGE_READY,
                            title = "Ready to Binge!",
                            subtitle = "${show.title} Season ${season.seasonNumber}",
                            scheduledDate = bingeReadyDate,
                            seasonNumber = season.seasonNumber,
                            episodeNumber = null,
                            episodeTitle = null
                        )
                    )
                }
            }
        }

        // Save and schedule all notifications
        for (notification in notificationsToSchedule) {
            val id = notificationDao.insert(notification)
            scheduleWorkManagerNotification(notification.copy(id = id))
        }
    }

    private fun formatFinaleReminderSubtitle(timing: FinaleReminderTiming, showTitle: String): String {
        return when (timing) {
            FinaleReminderTiming.DAY_OF -> "$showTitle finale airs today"
            FinaleReminderTiming.ONE_DAY_BEFORE -> "$showTitle finale airs tomorrow"
            FinaleReminderTiming.TWO_DAYS_BEFORE -> "$showTitle finale airs in 2 days"
            FinaleReminderTiming.ONE_WEEK_BEFORE -> "$showTitle finale airs in 1 week"
        }
    }

    private fun scheduleWorkManagerNotification(notification: ScheduledNotification) {
        val now = LocalDateTime.now()
        val delay = ChronoUnit.MILLIS.between(now, notification.scheduledDate)
        if (delay <= 0) return

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putLong("notification_id", notification.id)
                    .build()
            )
            .addTag(WORK_TAG_NOTIFICATIONS)
            .addTag("show_${notification.showId}")
            .build()

        workManager.enqueueUniqueWork(
            "notification_${notification.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    // endregion

    // region Cancellation

    /**
     * Cancel all notifications for a show.
     */
    suspend fun cancelNotificationsForShow(showId: Long) {
        notificationDao.cancelAllForShow(showId)
        workManager.cancelAllWorkByTag("show_$showId")
    }

    /**
     * Cancel a specific notification.
     */
    suspend fun cancelNotification(notificationId: Long) {
        notificationDao.cancel(notificationId)
        workManager.cancelUniqueWork("notification_$notificationId")
    }

    // endregion

    // region Delivery

    /**
     * Deliver a notification immediately.
     */
    suspend fun deliverNotification(notificationId: Long) {
        val notification = notificationDao.getById(notificationId) ?: return

        if (!hasNotificationPermission() || !areNotificationsEnabled()) {
            return
        }

        // Check quiet hours
        val globalSettings = settingsRepository.globalSettings.first()
        val showSettings = settingsRepository.getShowSettings(notification.showId).first()
        val effectiveSettings = showSettings.resolveWith(globalSettings)

        if (effectiveSettings.quietHoursEnabled && isInQuietHours(effectiveSettings)) {
            // Reschedule for after quiet hours
            rescheduleAfterQuietHours(notification, effectiveSettings)
            return
        }

        val channelId = getChannelForType(notification.type)
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("show_id", notification.showId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notification.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notification.title)
            .setContentText(notification.subtitle)
            .setPriority(
                when (notification.type) {
                    NotificationType.PREMIERE, NotificationType.FINALE_REMINDER ->
                        NotificationCompat.PRIORITY_HIGH
                    else -> NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notification.episodeInfo()?.let { info ->
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${notification.subtitle}\n$info")
            )
        }

        try {
            NotificationManagerCompat.from(context).notify(
                notification.id.toInt(),
                builder.build()
            )
            notificationDao.markDelivered(notification.id)
        } catch (e: SecurityException) {
            // Permission denied
        }
    }

    private fun isInQuietHours(settings: NotificationSettings): Boolean {
        val now = LocalTime.now()
        val currentMinutes = now.hour * 60 + now.minute
        val start = settings.quietHoursStart
        val end = settings.quietHoursEnd

        return if (start < end) {
            // Same day range (e.g., 8:00 AM to 6:00 PM)
            currentMinutes in start until end
        } else {
            // Overnight range (e.g., 10:00 PM to 8:00 AM)
            currentMinutes >= start || currentMinutes < end
        }
    }

    private suspend fun rescheduleAfterQuietHours(
        notification: ScheduledNotification,
        settings: NotificationSettings
    ) {
        val endTime = LocalTime.of(settings.quietHoursEnd / 60, settings.quietHoursEnd % 60)
        var newDateTime = LocalDate.now().atTime(endTime)

        // If end time has passed today, schedule for tomorrow
        if (newDateTime.isBefore(LocalDateTime.now())) {
            newDateTime = newDateTime.plusDays(1)
        }

        val updatedNotification = notification.copy(
            scheduledDate = newDateTime,
            status = NotificationStatus.QUEUED
        )
        notificationDao.update(updatedNotification)
        scheduleWorkManagerNotification(updatedNotification)
    }

    // endregion

    // region Queries

    fun getAllNotifications(): Flow<List<ScheduledNotification>> {
        return notificationDao.getAllFlow()
    }

    fun getNotificationsForShow(showId: Long): Flow<List<ScheduledNotification>> {
        return notificationDao.getByShowIdFlow(showId)
    }

    fun getNotificationsByStatus(status: NotificationStatus): Flow<List<ScheduledNotification>> {
        return notificationDao.getByStatusFlow(status)
    }

    fun getNextScheduledNotification(): Flow<ScheduledNotification?> {
        return notificationDao.getNextScheduledFlow()
    }

    fun getPendingCount(): Flow<Int> {
        return notificationDao.getPendingCountFlow()
    }

    fun getPendingCountForShow(showId: Long): Flow<Int> {
        return notificationDao.getPendingCountForShowFlow(showId)
    }

    suspend fun getPendingCountByType(): List<TypeCount> {
        return notificationDao.getPendingCountByType()
    }

    // endregion
}
