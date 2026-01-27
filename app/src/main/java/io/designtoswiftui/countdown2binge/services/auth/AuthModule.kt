package io.designtoswiftui.countdown2binge.services.auth

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for authentication dependencies.
 *
 * AuthManager is already a @Singleton with @Inject constructor,
 * so it's automatically provided by Hilt. This module exists
 * for future auth-related dependencies that may need manual binding.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    // AuthManager is provided automatically via constructor injection
    // Add additional auth-related bindings here as needed
}
