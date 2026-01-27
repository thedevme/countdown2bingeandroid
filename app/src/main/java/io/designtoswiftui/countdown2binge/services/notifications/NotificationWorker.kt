package io.designtoswiftui.countdown2binge.services.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker for delivering scheduled notifications.
 */
@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationService: NotificationService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notificationId = inputData.getLong("notification_id", -1)
        if (notificationId == -1L) {
            return Result.failure()
        }

        return try {
            notificationService.deliverNotification(notificationId)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
