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
        val TIMELINE_SECTIONS_EXPANDED = booleanPreferencesKey("timeline_expanded_v2")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val HAS_SEEN_NOTIFICATION_SETTINGS = booleanPreferencesKey("has_seen_notification_settings")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        // Debug settings
        val DEBUG_SIMULATE_PREMIUM = booleanPreferencesKey("debug_simulate_premium")
        val DEBUG_SHOW_FULL_ONBOARDING = booleanPreferencesKey("debug_show_full_onboarding")
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
     * Whether timeline sections are expanded.
     * Default: true (expanded, matching iOS).
     */
    val timelineSectionsExpanded: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.TIMELINE_SECTIONS_EXPANDED] ?: true
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

    /**
     * Toggle whether timeline sections are expanded or collapsed.
     */
    suspend fun toggleTimelineSectionsExpanded() {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.TIMELINE_SECTIONS_EXPANDED] ?: true
            preferences[PreferencesKeys.TIMELINE_SECTIONS_EXPANDED] = !current
        }
    }

    // region Sound & Haptics

    /**
     * Whether sound effects are enabled.
     */
    val soundEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] ?: true
        }

    /**
     * Whether haptic feedback is enabled.
     */
    val hapticsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true
        }

    /**
     * Update sound enabled setting.
     */
    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    /**
     * Update haptics enabled setting.
     */
    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] = enabled
        }
    }

    // endregion

    // region Onboarding

    /**
     * Whether the user has completed onboarding.
     */
    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false
        }

    /**
     * Whether the user has seen notification settings during onboarding.
     */
    val hasSeenNotificationSettings: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_NOTIFICATION_SETTINGS] ?: false
        }

    /**
     * Mark onboarding as completed.
     */
    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    /**
     * Mark notification settings as seen.
     */
    suspend fun setNotificationSettingsSeen(seen: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_NOTIFICATION_SETTINGS] = seen
        }
    }

    // endregion

    // region Debug Settings

    /**
     * Whether to simulate premium status for testing.
     */
    val debugSimulatePremium: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEBUG_SIMULATE_PREMIUM] ?: false
        }

    /**
     * Whether to show full onboarding flow (vs skipping to certain steps).
     */
    val debugShowFullOnboarding: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEBUG_SHOW_FULL_ONBOARDING] ?: false
        }

    /**
     * Update simulate premium debug setting.
     */
    suspend fun setDebugSimulatePremium(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEBUG_SIMULATE_PREMIUM] = enabled
        }
    }

    /**
     * Update show full onboarding debug setting.
     */
    suspend fun setDebugShowFullOnboarding(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEBUG_SHOW_FULL_ONBOARDING] = enabled
        }
    }

    /**
     * Reset onboarding state for testing.
     */
    suspend fun resetOnboardingState() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = false
            preferences[PreferencesKeys.HAS_SEEN_NOTIFICATION_SETTINGS] = false
        }
    }

    // endregion
}
