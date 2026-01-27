package io.designtoswiftui.countdown2binge

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import io.designtoswiftui.countdown2binge.services.FranchiseMigrationService
import io.designtoswiftui.countdown2binge.services.RefreshWorker
import io.designtoswiftui.countdown2binge.services.premium.PremiumManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class Countdown2BingeApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var premiumManager: PremiumManager

    @Inject
    lateinit var franchiseMigrationService: FranchiseMigrationService

    // Application-scoped coroutine scope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        initializeRevenueCat()
        scheduleBackgroundRefresh()
        runFranchiseMigration()
    }

    /**
     * Initialize RevenueCat SDK for premium subscription management.
     */
    private fun initializeRevenueCat() {
        premiumManager.configure()
    }

    /**
     * Schedule daily background refresh of show data.
     * Runs once per day when the device has network connectivity.
     */
    private fun scheduleBackgroundRefresh() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val refreshWorkRequest = PeriodicWorkRequestBuilder<RefreshWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.HOURS) // Delay first run by 1 hour
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            RefreshWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Don't replace if already scheduled
            refreshWorkRequest
        )
    }

    /**
     * Run franchise migration to backfill relatedShowIds for existing shows.
     * This ensures existing users get spinoff data linked to their shows.
     */
    private fun runFranchiseMigration() {
        applicationScope.launch {
            franchiseMigrationService.migrateIfNeeded()
        }
    }
}
