package io.designtoswiftui.countdown2binge.services.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.designtoswiftui.countdown2binge.models.CountdownDisplayMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Repository for managing app settings using DataStore.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val INCLUDE_AIRING = booleanPreferencesKey("include_airing")
        val COUNTDOWN_DISPLAY_MODE = stringPreferencesKey("countdown_display_mode")
    }

    /**
     * Whether to include airing seasons in Binge Ready.
     *
     * When OFF (default): Binge Ready only shows complete seasons - seasons where all episodes
     * have finished airing. This is the traditional "binge-ready" concept.
     *
     * When ON: Binge Ready also includes currently airing seasons - seasons that are still
     * releasing new episodes week-to-week.
     */
    val includeAiring: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.INCLUDE_AIRING] ?: false
        }

    /**
     * Countdown display mode for the Timeline hero card.
     *
     * Days mode: Shows how many calendar days until the season finale airs
     * Episodes mode: Shows how many episodes remain until the finale
     */
    val countdownDisplayMode: Flow<CountdownDisplayMode> = context.dataStore.data
        .map { preferences ->
            val modeString = preferences[PreferencesKeys.COUNTDOWN_DISPLAY_MODE]
            when (modeString) {
                "EPISODES" -> CountdownDisplayMode.EPISODES
                else -> CountdownDisplayMode.DAYS
            }
        }

    /**
     * Update the include airing setting.
     */
    suspend fun setIncludeAiring(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INCLUDE_AIRING] = enabled
        }
    }

    /**
     * Update the countdown display mode.
     */
    suspend fun setCountdownDisplayMode(mode: CountdownDisplayMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COUNTDOWN_DISPLAY_MODE] = mode.name
        }
    }
}
