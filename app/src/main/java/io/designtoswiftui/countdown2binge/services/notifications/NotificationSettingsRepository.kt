package io.designtoswiftui.countdown2binge.services.notifications

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.designtoswiftui.countdown2binge.models.FinaleReminderTiming
import io.designtoswiftui.countdown2binge.models.NotificationSettings
import io.designtoswiftui.countdown2binge.models.ShowNotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "notification_settings"
)

/**
 * Repository for managing notification settings using DataStore.
 */
@Singleton
class NotificationSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object GlobalKeys {
        val SEASON_PREMIERE = booleanPreferencesKey("global_season_premiere")
        val NEW_EPISODES = booleanPreferencesKey("global_new_episodes")
        val FINALE_REMINDER = booleanPreferencesKey("global_finale_reminder")
        val FINALE_REMINDER_TIMING = stringPreferencesKey("global_finale_reminder_timing")
        val BINGE_READY = booleanPreferencesKey("global_binge_ready")
        val QUIET_HOURS_ENABLED = booleanPreferencesKey("global_quiet_hours_enabled")
        val QUIET_HOURS_START = intPreferencesKey("global_quiet_hours_start")
        val QUIET_HOURS_END = intPreferencesKey("global_quiet_hours_end")
    }

    // region Global Settings

    /**
     * Flow of global notification settings.
     */
    val globalSettings: Flow<NotificationSettings> = context.notificationSettingsDataStore.data
        .map { preferences ->
            NotificationSettings(
                seasonPremiere = preferences[GlobalKeys.SEASON_PREMIERE] ?: true,
                newEpisodes = preferences[GlobalKeys.NEW_EPISODES] ?: true,
                finaleReminder = preferences[GlobalKeys.FINALE_REMINDER] ?: true,
                finaleReminderTiming = preferences[GlobalKeys.FINALE_REMINDER_TIMING]
                    ?.let { FinaleReminderTiming.fromValue(it) }
                    ?: FinaleReminderTiming.ONE_DAY_BEFORE,
                bingeReady = preferences[GlobalKeys.BINGE_READY] ?: false,
                quietHoursEnabled = preferences[GlobalKeys.QUIET_HOURS_ENABLED] ?: false,
                quietHoursStart = preferences[GlobalKeys.QUIET_HOURS_START] ?: (22 * 60),
                quietHoursEnd = preferences[GlobalKeys.QUIET_HOURS_END] ?: (8 * 60)
            )
        }

    suspend fun setSeasonPremiere(enabled: Boolean) {
        context.notificationSettingsDataStore.edit { preferences ->
            preferences[GlobalKeys.SEASON_PREMIERE] = enabled
        }
    }

    suspend fun setNewEpisodes(enabled: Boolean) {
        context.notificationSettingsDataStore.edit { preferences ->
            preferences[GlobalKeys.NEW_EPISODES] = enabled
        }
    }

    suspend fun setFinaleReminder(enabled: Boolean) {
        context.notificationSettingsDataStore.edit { preferences ->
            preferences[GlobalKeys.FINALE_REMINDER] = enabled
        }
    }

    suspend fun setFinaleReminderTiming(timing: FinaleReminderTiming) {
        context.notificationSettingsDataStore.edit { preferences ->
            preferences[GlobalKeys.FINALE_REMINDER_TIMING] = timing.value
        }
    }

    suspend fun setBingeReady(enabled: Boolean) {
        context.notificationSettingsDataStore.edit { preferences ->
            preferences[GlobalKeys.BINGE_READY] = enabled
        }
    }

    suspend fun setQuietHoursEnabled(enabled: Boolean) {
        context.notificationSettingsDataStore.edit { preferences ->
            preferences[GlobalKeys.QUIET_HOURS_ENABLED] = enabled
        }
    }

    suspend fun setQuietHoursStart(minutesFromMidnight: Int) {
        context.notificationSettingsDataStore.edit { preferences ->
            preferences[GlobalKeys.QUIET_HOURS_START] = minutesFromMidnight
        }
    }

    suspend fun setQuietHoursEnd(minutesFromMidnight: Int) {
        context.notificationSettingsDataStore.edit { preferences ->
            preferences[GlobalKeys.QUIET_HOURS_END] = minutesFromMidnight
        }
    }

    // endregion

    // region Per-Show Settings

    private fun showKey(showId: Long, setting: String) = "${showId}_$setting"

    /**
     * Get per-show notification settings.
     */
    fun getShowSettings(showId: Long): Flow<ShowNotificationSettings> {
        return context.notificationSettingsDataStore.data.map { preferences ->
            ShowNotificationSettings(
                showId = showId,
                seasonPremiere = preferences[booleanPreferencesKey(showKey(showId, "season_premiere"))],
                newEpisodes = preferences[booleanPreferencesKey(showKey(showId, "new_episodes"))],
                finaleReminder = preferences[booleanPreferencesKey(showKey(showId, "finale_reminder"))],
                finaleReminderTiming = preferences[stringPreferencesKey(showKey(showId, "finale_reminder_timing"))]
                    ?.let { FinaleReminderTiming.fromValue(it) },
                bingeReady = preferences[booleanPreferencesKey(showKey(showId, "binge_ready"))],
                quietHoursEnabled = preferences[booleanPreferencesKey(showKey(showId, "quiet_hours_enabled"))],
                quietHoursStart = preferences[intPreferencesKey(showKey(showId, "quiet_hours_start"))],
                quietHoursEnd = preferences[intPreferencesKey(showKey(showId, "quiet_hours_end"))]
            )
        }
    }

    suspend fun setShowSeasonPremiere(showId: Long, enabled: Boolean?) {
        context.notificationSettingsDataStore.edit { preferences ->
            val key = booleanPreferencesKey(showKey(showId, "season_premiere"))
            if (enabled != null) {
                preferences[key] = enabled
            } else {
                preferences.remove(key)
            }
        }
    }

    suspend fun setShowNewEpisodes(showId: Long, enabled: Boolean?) {
        context.notificationSettingsDataStore.edit { preferences ->
            val key = booleanPreferencesKey(showKey(showId, "new_episodes"))
            if (enabled != null) {
                preferences[key] = enabled
            } else {
                preferences.remove(key)
            }
        }
    }

    suspend fun setShowFinaleReminder(showId: Long, enabled: Boolean?) {
        context.notificationSettingsDataStore.edit { preferences ->
            val key = booleanPreferencesKey(showKey(showId, "finale_reminder"))
            if (enabled != null) {
                preferences[key] = enabled
            } else {
                preferences.remove(key)
            }
        }
    }

    suspend fun setShowFinaleReminderTiming(showId: Long, timing: FinaleReminderTiming?) {
        context.notificationSettingsDataStore.edit { preferences ->
            val key = stringPreferencesKey(showKey(showId, "finale_reminder_timing"))
            if (timing != null) {
                preferences[key] = timing.value
            } else {
                preferences.remove(key)
            }
        }
    }

    suspend fun setShowBingeReady(showId: Long, enabled: Boolean?) {
        context.notificationSettingsDataStore.edit { preferences ->
            val key = booleanPreferencesKey(showKey(showId, "binge_ready"))
            if (enabled != null) {
                preferences[key] = enabled
            } else {
                preferences.remove(key)
            }
        }
    }

    suspend fun setShowQuietHoursEnabled(showId: Long, enabled: Boolean?) {
        context.notificationSettingsDataStore.edit { preferences ->
            val key = booleanPreferencesKey(showKey(showId, "quiet_hours_enabled"))
            if (enabled != null) {
                preferences[key] = enabled
            } else {
                preferences.remove(key)
            }
        }
    }

    suspend fun setShowQuietHoursStart(showId: Long, minutesFromMidnight: Int?) {
        context.notificationSettingsDataStore.edit { preferences ->
            val key = intPreferencesKey(showKey(showId, "quiet_hours_start"))
            if (minutesFromMidnight != null) {
                preferences[key] = minutesFromMidnight
            } else {
                preferences.remove(key)
            }
        }
    }

    suspend fun setShowQuietHoursEnd(showId: Long, minutesFromMidnight: Int?) {
        context.notificationSettingsDataStore.edit { preferences ->
            val key = intPreferencesKey(showKey(showId, "quiet_hours_end"))
            if (minutesFromMidnight != null) {
                preferences[key] = minutesFromMidnight
            } else {
                preferences.remove(key)
            }
        }
    }

    /**
     * Reset all per-show settings to global defaults.
     */
    suspend fun resetShowToGlobalDefaults(showId: Long) {
        context.notificationSettingsDataStore.edit { preferences ->
            listOf(
                "season_premiere", "new_episodes", "finale_reminder",
                "finale_reminder_timing", "binge_ready", "quiet_hours_enabled",
                "quiet_hours_start", "quiet_hours_end"
            ).forEach { setting ->
                preferences.remove(booleanPreferencesKey(showKey(showId, setting)))
                preferences.remove(stringPreferencesKey(showKey(showId, setting)))
                preferences.remove(intPreferencesKey(showKey(showId, setting)))
            }
        }
    }

    // endregion
}
