package io.designtoswiftui.countdown2binge.models

/**
 * Timing options for finale reminder notifications.
 */
enum class FinaleReminderTiming(val value: String) {
    DAY_OF("day_of"),
    ONE_DAY_BEFORE("1_day_before"),
    TWO_DAYS_BEFORE("2_days_before"),
    ONE_WEEK_BEFORE("1_week_before");

    companion object {
        fun fromValue(value: String): FinaleReminderTiming {
            return entries.find { it.value == value } ?: DAY_OF
        }
    }
}
