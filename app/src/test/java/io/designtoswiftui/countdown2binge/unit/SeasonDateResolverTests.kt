package io.designtoswiftui.countdown2binge.unit

import io.designtoswiftui.countdown2binge.helpers.DateHelpers.daysAgo
import io.designtoswiftui.countdown2binge.helpers.DateHelpers.daysFromToday
import io.designtoswiftui.countdown2binge.helpers.DateHelpers.today
import io.designtoswiftui.countdown2binge.models.Episode
import io.designtoswiftui.countdown2binge.models.ReleasePattern
import io.designtoswiftui.countdown2binge.services.state.SeasonDateResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class SeasonDateResolverTests {

    private lateinit var resolver: SeasonDateResolver

    @Before
    fun setUp() {
        resolver = SeasonDateResolver()
    }

    // region resolvePremiereDate tests

    @Test
    fun `resolvePremiereDate prefers first episode date over season date`() {
        val seasonDate = LocalDate.of(2024, 1, 1)
        val episodeDate = LocalDate.of(2024, 1, 5)
        val episodes = listOf(
            createEpisode(1, episodeDate)
        )

        val result = resolver.resolvePremiereDate(seasonDate, episodes)

        assertEquals(episodeDate, result)
    }

    @Test
    fun `resolvePremiereDate falls back to season date when no episode date`() {
        val seasonDate = LocalDate.of(2024, 1, 1)
        val episodes = listOf(
            createEpisode(1, null)
        )

        val result = resolver.resolvePremiereDate(seasonDate, episodes)

        assertEquals(seasonDate, result)
    }

    @Test
    fun `resolvePremiereDate returns null when no dates available`() {
        val episodes = listOf(
            createEpisode(1, null)
        )

        val result = resolver.resolvePremiereDate(null, episodes)

        assertNull(result)
    }

    // endregion

    // region resolveFinaleDate tests

    @Test
    fun `resolveFinaleDate returns last episode date when available`() {
        val finaleDate = LocalDate.of(2024, 3, 15)
        val episodes = listOf(
            createEpisode(1, LocalDate.of(2024, 1, 1)),
            createEpisode(10, finaleDate)
        )

        val (date, isEstimated) = resolver.resolveFinaleDate(
            premiereDate = LocalDate.of(2024, 1, 1),
            episodeCount = 10,
            episodes = episodes,
            releasePattern = ReleasePattern.WEEKLY
        )

        assertEquals(finaleDate, date)
        assertFalse(isEstimated)
    }

    @Test
    fun `resolveFinaleDate estimates for weekly releases`() {
        val premiereDate = LocalDate.of(2024, 1, 1)
        val episodes = listOf(
            createEpisode(1, premiereDate)
        )

        val (date, isEstimated) = resolver.resolveFinaleDate(
            premiereDate = premiereDate,
            episodeCount = 10,
            episodes = episodes,
            releasePattern = ReleasePattern.WEEKLY
        )

        // 10 episodes, 9 weeks after premiere
        assertEquals(premiereDate.plusWeeks(9), date)
        assertTrue(isEstimated)
    }

    @Test
    fun `resolveFinaleDate returns premiere for all at once releases`() {
        val premiereDate = LocalDate.of(2024, 1, 1)

        val (date, isEstimated) = resolver.resolveFinaleDate(
            premiereDate = premiereDate,
            episodeCount = 10,
            episodes = emptyList(),
            releasePattern = ReleasePattern.ALL_AT_ONCE
        )

        assertEquals(premiereDate, date)
        assertFalse(isEstimated)
    }

    // endregion

    // region detectReleasePattern tests

    @Test
    fun `detectReleasePattern detects all at once release`() {
        val sameDate = LocalDate.of(2024, 1, 1)
        val episodes = (1..8).map { createEpisode(it, sameDate) }

        val pattern = resolver.detectReleasePattern(episodes)

        assertEquals(ReleasePattern.ALL_AT_ONCE, pattern)
    }

    @Test
    fun `detectReleasePattern detects weekly release`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val episodes = (0..7).map { week ->
            createEpisode(week + 1, startDate.plusWeeks(week.toLong()))
        }

        val pattern = resolver.detectReleasePattern(episodes)

        assertEquals(ReleasePattern.WEEKLY, pattern)
    }

    @Test
    fun `detectReleasePattern detects split season`() {
        val startDate = LocalDate.of(2024, 1, 1)
        val episodes = listOf(
            createEpisode(1, startDate),
            createEpisode(2, startDate.plusWeeks(1)),
            createEpisode(3, startDate.plusWeeks(2)),
            createEpisode(4, startDate.plusWeeks(3)),
            // 2 month gap
            createEpisode(5, startDate.plusWeeks(12)),
            createEpisode(6, startDate.plusWeeks(13))
        )

        val pattern = resolver.detectReleasePattern(episodes)

        assertEquals(ReleasePattern.SPLIT_SEASON, pattern)
    }

    @Test
    fun `detectReleasePattern returns unknown for insufficient data`() {
        val episodes = listOf(
            createEpisode(1, LocalDate.of(2024, 1, 1))
        )

        val pattern = resolver.detectReleasePattern(episodes)

        assertEquals(ReleasePattern.UNKNOWN, pattern)
    }

    // endregion

    // region countAiredEpisodes tests

    @Test
    fun `countAiredEpisodes counts correctly`() {
        val episodes = listOf(
            createEpisode(1, daysAgo(21)),
            createEpisode(2, daysAgo(14)),
            createEpisode(3, daysAgo(7)),
            createEpisode(4, today()),
            createEpisode(5, daysFromToday(7)),
            createEpisode(6, daysFromToday(14))
        )

        val count = resolver.countAiredEpisodes(episodes, today())

        assertEquals(4, count)
    }

    @Test
    fun `countAiredEpisodes handles episodes without dates`() {
        val episodes = listOf(
            createEpisode(1, daysAgo(7)),
            createEpisode(2, null),
            createEpisode(3, daysAgo(1))
        )

        val count = resolver.countAiredEpisodes(episodes, today())

        assertEquals(2, count)
    }

    // endregion

    // region resolve integration tests

    @Test
    fun `resolve returns complete info for weekly show`() {
        val premiereDate = daysAgo(28)
        val episodes = (0..3).map { week ->
            createEpisode(week + 1, premiereDate.plusWeeks(week.toLong()))
        }

        val info = resolver.resolve(
            seasonAirDate = premiereDate,
            episodeCount = 10,
            episodes = episodes,
            asOf = today()
        )

        assertEquals(premiereDate, info.premiereDate)
        assertEquals(ReleasePattern.WEEKLY, info.releasePattern)
        assertEquals(4, info.airedEpisodeCount)
        assertTrue(info.isFinaleEstimated)
    }

    // endregion

    // region Helper functions

    private fun createEpisode(
        episodeNumber: Int,
        airDate: LocalDate?
    ): Episode {
        return Episode(
            id = episodeNumber.toLong(),
            tmdbId = episodeNumber * 1000,
            episodeNumber = episodeNumber,
            name = "Episode $episodeNumber",
            airDate = airDate,
            runtime = 45,
            overview = null,
            isWatched = false,
            seasonId = 1L
        )
    }

    // endregion
}
