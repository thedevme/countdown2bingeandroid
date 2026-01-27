package io.designtoswiftui.countdown2binge.services.premium

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for premium-related dependencies.
 *
 * PremiumManager is already @Singleton with @Inject constructor,
 * so it's automatically provided by Hilt.
 */
@Module
@InstallIn(SingletonComponent::class)
object PremiumModule {
    // PremiumManager is provided automatically via constructor injection
}
