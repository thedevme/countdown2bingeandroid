package io.designtoswiftui.countdown2binge.unit

import io.designtoswiftui.countdown2binge.helpers.DateHelpers.daysAgo
import io.designtoswiftui.countdown2binge.helpers.DateHelpers.daysFromToday
import io.designtoswiftui.countdown2binge.helpers.DateHelpers.today
import io.designtoswiftui.countdown2binge.models.ReleasePattern
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.models.SeasonState
import io.designtoswiftui.countdown2binge.services.state.SeasonStateManager
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
