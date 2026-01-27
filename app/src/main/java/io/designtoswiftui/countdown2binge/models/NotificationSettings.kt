package io.designtoswiftui.countdown2binge.models

/**
 * Global notification settings.
 */
data class NotificationSettings(
    val seasonPremiere: Boolean = true,
    val newEpisodes: Boolean = true,
    val finaleReminder: Boolean = true,
    val finaleReminderTiming: FinaleReminderTiming = FinaleReminderTiming.ONE_DAY_BEFORE,
    val bingeReady: Boolean = false,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: Int = 22 * 60, // 10:00 PM in minutes from midnight
    val quietHoursEnd: Int = 8 * 60     // 8:00 AM in minutes from midnight
)

/**
 * Per-show notification settings override.
 * Null values mean "use global default".
 */
data class ShowNotificationSettings(
    val showId: Long,
    val seasonPremiere: Boolean? = null,
    val newEpisodes: Boolean? = null,
    val finaleReminder: Boolean? = null,
    val finaleReminderTiming: FinaleReminderTiming? = null,
    val bingeReady: Boolean? = null,
    val quietHoursEnabled: Boolean? = null,
    val quietHoursStart: Int? = null,
    val quietHoursEnd: Int? = null
) {
    /**
     * Resolve effective settings by merging with global defaults.
     */
    fun resolveWith(global: NotificationSettings): NotificationSettings {
        return NotificationSettings(
            seasonPremiere = seasonPremiere ?: global.seasonPremiere,
            newEpisodes = newEpisodes ?: global.newEpisodes,
            finaleReminder = finaleReminder ?: global.finaleReminder,
            finaleReminderTiming = finaleReminderTiming ?: global.finaleReminderTiming,
            bingeReady = bingeReady ?: global.bingeReady,
            quietHoursEnabled = quietHoursEnabled ?: global.quietHoursEnabled,
            quietHoursStart = quietHoursStart ?: global.quietHoursStart,
            quietHoursEnd = quietHoursEnd ?: global.quietHoursEnd
        )
    }

    /**
     * Check if any setting has been customized.
     */
    fun hasCustomSettings(): Boolean {
        return seasonPremiere != null ||
                newEpisodes != null ||
                finaleReminder != null ||
                finaleReminderTiming != null ||
                bingeReady != null ||
                quietHoursEnabled != null ||
                quietHoursStart != null ||
                quietHoursEnd != null
    }
}
