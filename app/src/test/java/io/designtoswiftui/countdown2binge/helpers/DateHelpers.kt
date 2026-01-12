package io.designtoswiftui.countdown2binge.helpers

import java.time.LocalDate

/**
 * Test helper functions for working with dates.
 */
object DateHelpers {

    /**
     * Creates a date relative to today.
     * Positive values are future dates, negative values are past dates.
     */
    fun daysFromNow(days: Int): LocalDate {
        return LocalDate.now().plusDays(days.toLong())
    }

    /**
     * Creates a date relative to today.
     * Positive values are future dates, negative values are past dates.
     */
    fun daysFromNow(days: Long): LocalDate {
        return LocalDate.now().plusDays(days)
    }

    /**
     * Creates a date in the past (convenience for negative days).
     */
    fun daysAgo(days: Int): LocalDate {
        return LocalDate.now().minusDays(days.toLong())
    }

    /**
     * Creates a date in the future (convenience for positive days).
     */
    fun daysFromToday(days: Int): LocalDate {
        return LocalDate.now().plusDays(days.toLong())
    }

    /**
     * Creates a date relative to a reference date.
     */
    fun daysFrom(referenceDate: LocalDate, days: Int): LocalDate {
        return referenceDate.plusDays(days.toLong())
    }

    /**
     * Returns today's date.
     */
    fun today(): LocalDate {
        return LocalDate.now()
    }

    /**
     * Returns yesterday's date.
     */
    fun yesterday(): LocalDate {
        return LocalDate.now().minusDays(1)
    }

    /**
     * Returns tomorrow's date.
     */
    fun tomorrow(): LocalDate {
        return LocalDate.now().plusDays(1)
    }
}
