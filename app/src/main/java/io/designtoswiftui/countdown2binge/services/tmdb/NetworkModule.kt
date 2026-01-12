package io.designtoswiftui.countdown2binge.services.tmdb

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.designtoswiftui.countdown2binge.services.state.SeasonDateResolver
import io.designtoswiftui.countdown2binge.services.state.SeasonStateManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideTMDBService(): TMDBService {
        return TMDBService()
    }

    @Provides
    @Singleton
    fun provideShowDataAggregator(tmdbService: TMDBService): ShowDataAggregator {
        return ShowDataAggregator(tmdbService)
    }

    @Provides
    @Singleton
    fun provideShowProcessor(
        dateResolver: SeasonDateResolver,
        stateManager: SeasonStateManager
    ): ShowProcessor {
        return ShowProcessor(dateResolver, stateManager)
    }

    @Provides
    @Singleton
    fun provideSeasonDateResolver(): SeasonDateResolver {
        return SeasonDateResolver()
    }

    @Provides
    @Singleton
    fun provideSeasonStateManager(): SeasonStateManager {
        return SeasonStateManager()
    }
}
