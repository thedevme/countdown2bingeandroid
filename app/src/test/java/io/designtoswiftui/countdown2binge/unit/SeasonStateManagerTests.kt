package io.designtoswiftui.countdown2binge.unit

import io.designtoswiftui.countdown2binge.helpers.DateHelpers.daysAgo
import io.designtoswiftui.countdown2binge.helpers.DateHelpers.daysFromToday
import io.designtoswiftui.countdown2binge.helpers.DateHelpers.today
import io.designtoswiftui.countdown2binge.models.ReleasePattern
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.SeasonState
import io.designtoswiftui.countdown2binge.services.state.SeasonStateManager
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SeasonStateManagerTests {

    private lateinit var stateManager: SeasonStateManager

    @Before
    fun setUp() {
        stateManager = SeasonStateManager()
    }

    // region determineState tests

    @Test
    fun `no premiere date returns anticipated`() {
        val season = createSeason(
            premiereDate = null,
            finaleDate = null
        )

        val state = stateManager.determineState(season, today())

        assertEquals(SeasonState.ANTICIPATED, state)
    }

    @Test
    fun `future premiere date returns premiering`() {
        val season = createSeason(
            premiereDate = daysFromToday(30),
            finaleDate = daysFromToday(90)
        )

        val state = stateManager.determineState(season, today())

        assertEquals(SeasonState.PREMIERING, state)
    }

    @Test
    fun `mid season returns airing`() {
        val season = createSeason(
            premiereDate = daysAgo(30),
            finaleDate = daysFromToday(30)
        )

        val state = stateManager.determineState(season, today())

        assertEquals(SeasonState.AIRING, state)
    }

    @Test
    fun `finale passed returns binge ready`() {
        val season = createSeason(
            premiereDate = daysAgo(90),
            finaleDate = daysAgo(7)
        )

        val state = stateManager.determineState(season, today())

        assertEquals(SeasonState.BINGE_READY, state)
    }

    @Test
    fun `watched date set returns watched`() {
        val season = createSeason(
            premiereDate = daysAgo(90),
            finaleDate = daysAgo(7),
            watchedDate = daysAgo(1)
        )

        val state = stateManager.determineState(season, today())

        assertEquals(SeasonState.WATCHED, state)
    }

    @Test
    fun `netflix drop returns binge ready on premiere day`() {
        val premiereDate = today()
        val season = createSeason(
            premiereDate = premiereDate,
            finaleDate = premiereDate, // All episodes drop at once
            releasePattern = ReleasePattern.ALL_AT_ONCE
        )

        val state = stateManager.determineState(season, today())

        assertEquals(SeasonState.BINGE_READY, state)
    }

    @Test
    fun `netflix drop returns binge ready after premiere`() {
        val season = createSeason(
            premiereDate = daysAgo(7),
            finaleDate = daysAgo(7),
            releasePattern = ReleasePattern.ALL_AT_ONCE
        )

        val state = stateManager.determineState(season, today())

        assertEquals(SeasonState.BINGE_READY, state)
    }

    @Test
    fun `finale on today returns binge ready`() {
        val season = createSeason(
            premiereDate = daysAgo(60),
            finaleDate = today()
        )

        val state = stateManager.determineState(season, today())

        assertEquals(SeasonState.BINGE_READY, state)
    }

    @Test
    fun `premiere on today returns airing for weekly shows`() {
        val season = createSeason(
            premiereDate = today(),
            finaleDate = daysFromToday(60),
            releasePattern = ReleasePattern.WEEKLY
        )

        val state = stateManager.determineState(season, today())

        assertEquals(SeasonState.AIRING, state)
    }

    // endregion

    // region isBingeReady tests

    @Test
    fun `isBingeReady returns true when finale has passed`() {
        val season = createSeason(
            premiereDate = daysAgo(90),
            finaleDate = daysAgo(7)
        )

        assertTrue(stateManager.isBingeReady(season, today()))
    }

    @Test
    fun `isBingeReady returns false when still airing`() {
        val season = createSeason(
            premiereDate = daysAgo(30),
            finaleDate = daysFromToday(30)
        )

        assertFalse(stateManager.isBingeReady(season, today()))
    }

    // endregion

    // region daysUntilPremiere tests

    @Test
    fun `daysUntilPremiere returns correct count`() {
        val season = createSeason(
            premiereDate = daysFromToday(10)
        )

        assertEquals(10, stateManager.daysUntilPremiere(season, today()))
    }

    @Test
    fun `daysUntilPremiere returns null when no premiere date`() {
        val season = createSeason(premiereDate = null)

        assertNull(stateManager.daysUntilPremiere(season, today()))
    }

    @Test
    fun `daysUntilPremiere returns null when premiere has passed`() {
        val season = createSeason(premiereDate = daysAgo(10))

        assertNull(stateManager.daysUntilPremiere(season, today()))
    }

    // endregion

    // region daysUntilFinale tests

    @Test
    fun `daysUntilFinale returns correct count`() {
        val season = createSeason(
            premiereDate = daysAgo(30),
            finaleDate = daysFromToday(30)
        )

        assertEquals(30, stateManager.daysUntilFinale(season, today()))
    }

    @Test
    fun `daysUntilFinale returns null when no finale date`() {
        val season = createSeason(
            premiereDate = daysAgo(30),
            finaleDate = null
        )

        assertNull(stateManager.daysUntilFinale(season, today()))
    }

    @Test
    fun `daysUntilFinale returns null when finale has passed`() {
        val season = createSeason(
            premiereDate = daysAgo(90),
            finaleDate = daysAgo(10)
        )

        assertNull(stateManager.daysUntilFinale(season, today()))
    }

    // endregion

    // region episodesRemaining tests

    @Test
    fun `episodesRemaining returns correct count`() {
        val season = createSeason(
            episodeCount = 10,
            airedEpisodeCount = 6
        )

        assertEquals(4, stateManager.episodesRemaining(season))
    }

    @Test
    fun `episodesRemaining returns zero when all aired`() {
        val season = createSeason(
            episodeCount = 10,
            airedEpisodeCount = 10
        )

        assertEquals(0, stateManager.episodesRemaining(season))
    }

    @Test
    fun `episodesRemaining returns null when episode count is zero`() {
        val season = createSeason(
            episodeCount = 0,
            airedEpisodeCount = 0
        )

        assertNull(stateManager.episodesRemaining(season))
    }

    // endregion

    // region State Transition Tests

    @Test
    fun `transition premiering to airing when premiere date passes`() {
        val premiereDate = daysFromToday(5)
        val finaleDate = daysFromToday(65)
        val season = createSeason(
            premiereDate = premiereDate,
            finaleDate = finaleDate,
            releasePattern = ReleasePattern.WEEKLY
        )

        // Before premiere - should be PREMIERING
        val beforePremiere = daysAgo(10) // 10 days before today (15 days before premiere)
        assertEquals(
            SeasonState.PREMIERING,
            stateManager.determineState(season, beforePremiere)
        )

        // On premiere day - should be AIRING (weekly show)
        assertEquals(
            SeasonState.AIRING,
            stateManager.determineState(season, premiereDate)
        )

        // After premiere but before finale - should be AIRING
        val midSeason = premiereDate.plusDays(30)
        assertEquals(
            SeasonState.AIRING,
            stateManager.determineState(season, midSeason)
        )
    }

    @Test
    fun `transition airing to binge ready when finale passes`() {
        val premiereDate = daysAgo(60)
        val finaleDate = daysFromToday(5)
        val season = createSeason(
            premiereDate = premiereDate,
            finaleDate = finaleDate,
            releasePattern = ReleasePattern.WEEKLY
        )

        // Before finale - should be AIRING
        val beforeFinale = today()
        assertEquals(
            SeasonState.AIRING,
            stateManager.determineState(season, beforeFinale)
        )

        // On finale day - should be BINGE_READY
        assertEquals(
            SeasonState.BINGE_READY,
            stateManager.determineState(season, finaleDate)
        )

        // After finale - should be BINGE_READY
        val afterFinale = finaleDate.plusDays(7)
        assertEquals(
            SeasonState.BINGE_READY,
            stateManager.determineState(season, afterFinale)
        )
    }

    @Test
    fun `transition anticipated to premiering when premiere date is set`() {
        // Season starts with no premiere date
        val anticipatedSeason = createSeason(
            premiereDate = null,
            finaleDate = null
        )

        assertEquals(
            SeasonState.ANTICIPATED,
            stateManager.determineState(anticipatedSeason, today())
        )

        // After refresh, premiere date is now known (future date)
        val seasonWithPremiere = createSeason(
            premiereDate = daysFromToday(30),
            finaleDate = daysFromToday(90)
        )

        assertEquals(
            SeasonState.PREMIERING,
            stateManager.determineState(seasonWithPremiere, today())
        )
    }

    @Test
    fun `full lifecycle premiering to airing to binge ready`() {
        // A show announced with future premiere
        val premiereDate = LocalDate.of(2025, 3, 1)
        val finaleDate = LocalDate.of(2025, 5, 10)

        val season = createSeason(
            premiereDate = premiereDate,
            finaleDate = finaleDate,
            releasePattern = ReleasePattern.WEEKLY
        )

        // Feb 1 - Before premiere = PREMIERING
        val feb1 = LocalDate.of(2025, 2, 1)
        assertEquals(SeasonState.PREMIERING, stateManager.determineState(season, feb1))
        assertEquals(28, stateManager.daysUntilPremiere(season, feb1))

        // Mar 1 - Premiere day = AIRING
        assertEquals(SeasonState.AIRING, stateManager.determineState(season, premiereDate))
        assertNull(stateManager.daysUntilPremiere(season, premiereDate))
        assertEquals(70, stateManager.daysUntilFinale(season, premiereDate))

        // Apr 1 - Mid season = AIRING
        val apr1 = LocalDate.of(2025, 4, 1)
        assertEquals(SeasonState.AIRING, stateManager.determineState(season, apr1))
        assertEquals(39, stateManager.daysUntilFinale(season, apr1))

        // May 10 - Finale day = BINGE_READY
        assertEquals(SeasonState.BINGE_READY, stateManager.determineState(season, finaleDate))
        assertNull(stateManager.daysUntilFinale(season, finaleDate))

        // June 1 - After finale = BINGE_READY
        val june1 = LocalDate.of(2025, 6, 1)
        assertEquals(SeasonState.BINGE_READY, stateManager.determineState(season, june1))
    }

    @Test
    fun `netflix show transitions directly to binge ready on premiere`() {
        val premiereDate = daysFromToday(10)
        val season = createSeason(
            premiereDate = premiereDate,
            finaleDate = premiereDate, // All episodes drop at once
            releasePattern = ReleasePattern.ALL_AT_ONCE
        )

        // Before premiere = PREMIERING
        assertEquals(
            SeasonState.PREMIERING,
            stateManager.determineState(season, today())
        )

        // On premiere = BINGE_READY (not AIRING)
        assertEquals(
            SeasonState.BINGE_READY,
            stateManager.determineState(season, premiereDate)
        )
    }

    @Test
    fun `countdown updates correctly as time passes`() {
        val premiereDate = daysFromToday(30)
        val finaleDate = daysFromToday(90)
        val season = createSeason(
            premiereDate = premiereDate,
            finaleDate = finaleDate
        )

        // Today: 30 days to premiere
        assertEquals(30, stateManager.daysUntilPremiere(season, today()))

        // 10 days later: 20 days to premiere
        assertEquals(20, stateManager.daysUntilPremiere(season, daysFromToday(10)))

        // 29 days later: 1 day to premiere
        assertEquals(1, stateManager.daysUntilPremiere(season, daysFromToday(29)))

        // On premiere day: null (already premiered)
        assertNull(stateManager.daysUntilPremiere(season, premiereDate))

        // After premiere: countdown to finale
        assertEquals(60, stateManager.daysUntilFinale(season, premiereDate))
        assertEquals(30, stateManager.daysUntilFinale(season, daysFromToday(60)))
        assertEquals(1, stateManager.daysUntilFinale(season, daysFromToday(89)))
    }

    // endregion

    // region Helper functions

    private fun createSeason(
        premiereDate: java.time.LocalDate? = null,
        finaleDate: java.time.LocalDate? = null,
        watchedDate: java.time.LocalDate? = null,
        releasePattern: ReleasePattern = ReleasePattern.WEEKLY,
        episodeCount: Int = 10,
        airedEpisodeCount: Int = 0
    ): Season {
        return Season(
            id = 1L,
            tmdbId = 12345,
            seasonNumber = 1,
            premiereDate = premiereDate,
            finaleDate = finaleDate,
            isFinaleEstimated = false,
            episodeCount = episodeCount,
            airedEpisodeCount = airedEpisodeCount,
            releasePattern = releasePattern,
            state = SeasonState.ANTICIPATED,
            watchedDate = watchedDate,
            posterPath = null,
            showId = 1L
        )
    }

    // endregion
}
