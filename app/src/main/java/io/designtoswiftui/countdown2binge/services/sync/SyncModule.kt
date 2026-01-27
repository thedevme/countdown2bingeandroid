package io.designtoswiftui.countdown2binge.services.sync

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for sync-related dependencies.
 *
 * CloudSyncService and NetworkMonitor are already @Singleton with @Inject constructor,
 * so they're automatically provided by Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
object SyncModule {
    // All sync services are provided automatically via constructor injection
}
