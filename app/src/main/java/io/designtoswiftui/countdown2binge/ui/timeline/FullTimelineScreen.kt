package io.designtoswiftui.countdown2binge.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.timeline.components.CompactPosterCard
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineCountdownStyle
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineEmptyCard
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineEntry
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineSectionHeader
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineSectionStyle
import io.designtoswiftui.countdown2binge.viewmodels.TimelineViewModel

/**
 * Full Timeline screen showing all shows currently in production.
 * Displays Ending Soon, Premiering Soon, and Anticipated sections with cards.
 * Unlike the main timeline, this screen is not limited to 3 cards per section.
 *
 * Rules:
 * - At launch (no shows): All 3 sections with 3 empty cards each
 * - Once user has shows: Only show sections that have content (no empty filler)
 */
@Composable
fun FullTimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onShowClick: (Long) -> Unit = {}
) {
    val airingShows by viewModel.airingShows.collectAsState()
    val premieringShows by viewModel.premieringShows.collectAsState()
    val anticipatedShows by viewModel.anticipatedShows.collectAsState()

    val defaultEmptySlots = 3

    // Determine if this is onboarding state (no shows at all)
    val isOnboarding = airingShows.isEmpty() && premieringShows.isEmpty() && anticipatedShows.isEmpty()

    // Convert shows to TimelineEntry format
    val endingSoonEntries = remember(airingShows) {
        airingShows.map { show ->
            TimelineEntry(
                showId = show.show.id,
                seasonNumber = show.season?.seasonNumber ?: 1,
                backdropUrl = show.show.backdropPath?.let {
                    "${TMDBService.IMAGE_BASE_URL}${TMDBService.BACKDROP_SIZE}$it"
                },
                posterUrl = show.show.posterPath?.let {
                    "${TMDBService.IMAGE_BASE_URL}${TMDBService.POSTER_SIZE}$it"
                },
                countdownStyle = show.daysUntilFinale?.let {
                    TimelineCountdownStyle.Days(it)
                } ?: TimelineCountdownStyle.TBD
            )
        }
    }

    val premieringEntries = remember(premieringShows) {
        premieringShows.map { show ->
            TimelineEntry(
                showId = show.show.id,
                seasonNumber = show.season?.seasonNumber ?: 1,
                backdropUrl = show.show.backdropPath?.let {
                    "${TMDBService.IMAGE_BASE_URL}${TMDBService.BACKDROP_SIZE}$it"
                },
                posterUrl = show.show.posterPath?.let {
                    "${TMDBService.IMAGE_BASE_URL}${TMDBService.POSTER_SIZE}$it"
                },
                countdownStyle = show.daysUntilPremiere?.let {
                    TimelineCountdownStyle.Days(it)
                } ?: TimelineCountdownStyle.TBD
            )
        }
    }

    val anticipatedEntries = remember(anticipatedShows) {
        anticipatedShows.map { show ->
            TimelineEntry(
                showId = show.show.id,
                seasonNumber = show.anticipatedSeasonNumber,
                backdropUrl = show.show.backdropPath?.let {
                    "${TMDBService.IMAGE_BASE_URL}${TMDBService.BACKDROP_SIZE}$it"
                },
                posterUrl = show.show.posterPath?.let {
                    "${TMDBService.IMAGE_BASE_URL}${TMDBService.POSTER_SIZE}$it"
                },
                countdownStyle = show.season?.premiereDate?.let { date ->
                    TimelineCountdownStyle.Year(date.year)
                } ?: TimelineCountdownStyle.TBD
            )
        }
    }

    // Section visibility: show section if it has content OR if onboarding
    val showEndingSoon = endingSoonEntries.isNotEmpty() || isOnboarding
    val showPremiering = premieringEntries.isNotEmpty() || isOnboarding
    val showAnticipated = anticipatedEntries.isNotEmpty() || isOnboarding

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(top = 48.dp)
    ) {
        // ENDING SOON SECTION
        if (showEndingSoon) {
            item {
                TimelineSectionHeader(
                    title = "Ending Soon",
                    count = airingShows.size,
                    style = TimelineSectionStyle.ENDING_SOON,
                    isExpanded = true,
                    onToggle = {}
                )
            }

            // Ending Soon cards (no limit)
            endingSoonEntries.forEachIndexed { index, entry ->
                item {
                    CompactPosterCard(
                        countdownStyle = entry.countdownStyle,
                        posterUrl = entry.posterUrl,
                        seasonNumber = entry.seasonNumber,
                        sectionStyle = TimelineSectionStyle.ENDING_SOON,
                        onClick = { onShowClick(entry.showId) }
                    )
                    if (index < endingSoonEntries.lastIndex || (isOnboarding && endingSoonEntries.isEmpty())) {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }

            // Empty slots for Ending Soon (only during onboarding)
            if (isOnboarding) {
                repeat(defaultEmptySlots) { index ->
                    item {
                        TimelineEmptyCard(
                            isExpanded = false,
                            sectionStyle = TimelineSectionStyle.ENDING_SOON
                        )
                        if (index < defaultEmptySlots - 1) {
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }

            // Spacing between sections
            if (showPremiering || showAnticipated) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // PREMIERING SOON SECTION
        if (showPremiering) {
            item {
                TimelineSectionHeader(
                    title = "Premiering Soon",
                    count = premieringShows.size,
                    style = TimelineSectionStyle.PREMIERING_SOON,
                    isExpanded = true,
                    onToggle = {}
                )
            }

            // Premiering Soon cards (no limit)
            premieringEntries.forEachIndexed { index, entry ->
                item {
                    CompactPosterCard(
                        countdownStyle = entry.countdownStyle,
                        posterUrl = entry.posterUrl,
                        seasonNumber = entry.seasonNumber,
                        sectionStyle = TimelineSectionStyle.PREMIERING_SOON,
                        onClick = { onShowClick(entry.showId) }
                    )
                    if (index < premieringEntries.lastIndex || (isOnboarding && premieringEntries.isEmpty())) {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }

            // Empty slots for Premiering Soon (only during onboarding)
            if (isOnboarding) {
                repeat(defaultEmptySlots) { index ->
                    item {
                        TimelineEmptyCard(
                            isExpanded = false,
                            sectionStyle = TimelineSectionStyle.PREMIERING_SOON
                        )
                        if (index < defaultEmptySlots - 1) {
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }

            // Spacing between sections
            if (showAnticipated) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // ANTICIPATED SECTION
        if (showAnticipated) {
            item {
                TimelineSectionHeader(
                    title = "Anticipated",
                    count = anticipatedShows.size,
                    style = TimelineSectionStyle.ANTICIPATED,
                    isExpanded = true,
                    onToggle = {}
                )
            }

            // Anticipated cards (no limit)
            anticipatedEntries.forEachIndexed { index, entry ->
                item {
                    CompactPosterCard(
                        countdownStyle = entry.countdownStyle,
                        posterUrl = entry.posterUrl,
                        seasonNumber = entry.seasonNumber,
                        sectionStyle = TimelineSectionStyle.ANTICIPATED,
                        onClick = { onShowClick(entry.showId) }
                    )
                    if (index < anticipatedEntries.lastIndex || (isOnboarding && anticipatedEntries.isEmpty())) {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }

            // Empty slots for Anticipated (only during onboarding)
            if (isOnboarding) {
                repeat(defaultEmptySlots) { index ->
                    item {
                        TimelineEmptyCard(
                            isExpanded = false,
                            sectionStyle = TimelineSectionStyle.ANTICIPATED
                        )
                        if (index < defaultEmptySlots - 1) {
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }
        }

        // Bottom spacing for nav bar
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
