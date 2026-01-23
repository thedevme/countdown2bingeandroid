package io.designtoswiftui.countdown2binge.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.countdown2binge.models.CountdownDisplayMode
import io.designtoswiftui.countdown2binge.models.SeasonState
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle
import io.designtoswiftui.countdown2binge.ui.theme.Primary
import io.designtoswiftui.countdown2binge.ui.timeline.components.CountdownLabel
import io.designtoswiftui.countdown2binge.ui.timeline.components.DottedDivider
import io.designtoswiftui.countdown2binge.ui.timeline.components.HeroPlaceholderCard
import io.designtoswiftui.countdown2binge.ui.timeline.components.SlotMachineDaysPicker
import io.designtoswiftui.countdown2binge.ui.timeline.components.StackedShowCards
import io.designtoswiftui.countdown2binge.ui.timeline.components.StackedShowData
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineCountdownStyle
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineEntry
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineFooter
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineHeader
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineHeaderType
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineSection
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineSectionStyle
import io.designtoswiftui.countdown2binge.viewmodels.SettingsViewModel
import io.designtoswiftui.countdown2binge.viewmodels.TimelineShow
import io.designtoswiftui.countdown2binge.viewmodels.TimelineViewModel
import java.time.LocalDateTime
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Timeline screen showing shows grouped by their current state.
 * Displays: Currently Airing (stacked cards) → Premiering Soon → Anticipated
 */
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onShowClick: (Long) -> Unit = {},
    onViewFullTimelineClick: () -> Unit = {}
) {
    val airingShows by viewModel.airingShows.collectAsState()
    val premieringShows by viewModel.premieringShows.collectAsState()
    val anticipatedShows by viewModel.anticipatedShows.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val countdownDisplayMode by settingsViewModel.countdownDisplayMode.collectAsState()
    // Default to expanded (true)
    var isSectionsExpanded by remember { mutableStateOf(true) }

    // Determine hero show (show with soonest date)
    val heroShow by remember(airingShows, premieringShows) {
        derivedStateOf {
            // Priority: Airing show closest to finale → Premiering show closest to premiere
            val airingWithFinale = airingShows
                .filter { it.daysUntilFinale != null }
                .minByOrNull { it.daysUntilFinale!! }

            val premieringWithDate = premieringShows
                .filter { it.daysUntilPremiere != null }
                .minByOrNull { it.daysUntilPremiere!! }

            when {
                airingWithFinale != null && premieringWithDate != null -> {
                    if ((airingWithFinale.daysUntilFinale ?: Int.MAX_VALUE) <= (premieringWithDate.daysUntilPremiere ?: Int.MAX_VALUE)) {
                        airingWithFinale
                    } else {
                        premieringWithDate
                    }
                }
                airingWithFinale != null -> airingWithFinale
                premieringWithDate != null -> premieringWithDate
                else -> airingShows.firstOrNull() ?: premieringShows.firstOrNull()
            }
        }
    }

    // Compute days for slot machine (from hero show)
    val heroDays by remember(heroShow) {
        derivedStateOf {
            heroShow?.let { show ->
                when (show.season?.state) {
                    SeasonState.AIRING -> show.daysUntilFinale
                    SeasonState.PREMIERING -> show.daysUntilPremiere
                    else -> show.daysUntilPremiere ?: show.daysUntilFinale
                }
            }
        }
    }

    // Determine if hero is finale or premiere
    val isHeroFinale by remember(heroShow) {
        derivedStateOf {
            heroShow?.season?.state == SeasonState.AIRING
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        when {
            isLoading -> LoadingState()
            else -> {
                // TimelineContent handles all states including empty (shows hero placeholder + empty cards)
                // No pull-to-refresh needed - app auto-refreshes on launch and daily in background
                TimelineContent(
                    airingShows = airingShows,
                    premieringShows = premieringShows,
                    anticipatedShows = anticipatedShows,
                    heroDays = heroDays,
                    isHeroFinale = isHeroFinale,
                    countdownDisplayMode = countdownDisplayMode,
                    isSectionsExpanded = isSectionsExpanded,
                    onToggleSections = { isSectionsExpanded = !isSectionsExpanded },
                    onShowClick = onShowClick,
                    onViewFullTimelineClick = onViewFullTimelineClick
                )
            }
        }
    }
}

@Composable
private fun TimelineContent(
    airingShows: List<TimelineShow>,
    premieringShows: List<TimelineShow>,
    anticipatedShows: List<TimelineShow>,
    heroDays: Int?,
    isHeroFinale: Boolean,
    countdownDisplayMode: CountdownDisplayMode,
    isSectionsExpanded: Boolean,
    onToggleSections: () -> Unit,
    onShowClick: (Long) -> Unit,
    onViewFullTimelineClick: () -> Unit
) {
    // Track which card is currently selected in the pager
    var selectedCardIndex by remember { mutableIntStateOf(0) }

    // Section visibility rules based on show counts
    val hasAiring = airingShows.isNotEmpty()
    val hasPremiering = premieringShows.isNotEmpty()
    val hasTbd = anticipatedShows.isNotEmpty()

    // Determine if we should show hero placeholder (no airing shows)
    val showHeroPlaceholder = !hasAiring

    // Determine if we should show empty cards in sections (onboarding state)
    val showEmptyCardsInSections = !hasAiring && !hasPremiering && !hasTbd

    // Get the currently selected show's countdown data
    val selectedShow = remember(airingShows, selectedCardIndex) {
        airingShows.getOrNull(selectedCardIndex)
    }

    val selectedDays = remember(selectedShow) {
        selectedShow?.daysUntilFinale ?: selectedShow?.daysUntilPremiere
    }

    val selectedEpisodes = remember(selectedShow) {
        selectedShow?.episodesRemaining
    }

    val selectedIsFinale = remember(selectedShow) {
        selectedShow?.season?.state == SeasonState.AIRING
    }

    // Convert shows to TimelineEntry format
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
        val currentYear = java.time.LocalDate.now().year
        anticipatedShows.map { show ->
            TimelineEntry(
                showId = show.show.id,
                // Use anticipatedSeasonNumber for TBD shows (shows next season: S3 finished → S4 TBD)
                seasonNumber = show.anticipatedSeasonNumber,
                backdropUrl = show.show.backdropPath?.let {
                    "${TMDBService.IMAGE_BASE_URL}${TMDBService.BACKDROP_SIZE}$it"
                },
                posterUrl = show.show.posterPath?.let {
                    "${TMDBService.IMAGE_BASE_URL}${TMDBService.POSTER_SIZE}$it"
                },
                // Only show year if it's current year or later (can't anticipate the past)
                countdownStyle = show.season?.premiereDate?.let { date ->
                    if (date.year >= currentYear) {
                        TimelineCountdownStyle.Year(date.year)
                    } else {
                        TimelineCountdownStyle.TBD
                    }
                } ?: TimelineCountdownStyle.TBD
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // HERO SECTION
        if (hasAiring) {
            // CURRENTLY AIRING: Show stacked cards and countdown
            // Header: "CURRENTLY AIRING"
            item {
                TimelineHeader(
                    headerType = TimelineHeaderType.CURRENTLY_AIRING,
                    lastUpdated = LocalDateTime.now()
                )
            }

            // Stacked Cards (airing shows) - swipeable
            item {
                val stackedData = airingShows.map { show ->
                    StackedShowData(
                        id = show.show.id,
                        posterUrl = show.show.posterPath?.let {
                            "${TMDBService.IMAGE_BASE_URL}${TMDBService.POSTER_SIZE}$it"
                        },
                        title = show.show.title
                    )
                }

                StackedShowCards(
                    shows = stackedData,
                    onShowClick = onShowClick,
                    onPageChanged = { page ->
                        selectedCardIndex = page
                    },
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            // Countdown Label: "FINALE EPISODE IN" - updates based on selected card
            item {
                CountdownLabel(
                    text = if (selectedIsFinale) "FINALE" else "PREMIERE",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            // Slot Machine Countdown - updates based on selected card and display mode
            item {
                val countdownValue = when (countdownDisplayMode) {
                    CountdownDisplayMode.DAYS -> selectedDays ?: heroDays
                    CountdownDisplayMode.EPISODES -> selectedEpisodes
                }
                SlotMachineDaysPicker(
                    value = countdownValue,
                    mode = countdownDisplayMode
                )
            }

            // Dotted divider below slot machine
            item {
                DottedDivider(
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        } else {
            // NO AIRING SHOWS: Show "Timeline" header and hero placeholder (matching iOS)
            item {
                TimelineHeader(
                    headerType = TimelineHeaderType.TIMELINE,
                    lastUpdated = LocalDateTime.now()
                )
            }

            item {
                HeroPlaceholderCard()
            }

            // Dotted divider below placeholder
            item {
                DottedDivider(
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }

        // SECTION VISIBILITY RULES:
        // - 0 airing, 0 premiering, 0 TBD → Both sections with empty cards
        // - 0 airing, 0 premiering, N TBD → TBD section only
        // - 0 airing, N premiering, 0 TBD → Premiering section only
        // - N airing, 0 premiering, 0 TBD → No sections (hero stack only)
        // - N airing, N premiering, N TBD → Both sections

        // PREMIERING SOON SECTION (has chevron that controls both sections)
        val showPremieringSection = hasPremiering || showEmptyCardsInSections
        if (showPremieringSection) {
            item {
                TimelineSection(
                    title = "Premiering Soon",
                    entries = premieringEntries,
                    style = TimelineSectionStyle.PREMIERING_SOON,
                    isExpanded = isSectionsExpanded,
                    onToggle = onToggleSections,
                    onEntryClick = onShowClick,
                    showEmptyCards = showEmptyCardsInSections,
                    isFirstSection = true
                )
            }
        }

        // ANTICIPATED SECTION (no chevron - follows Premiering Soon's state)
        val showTbdSection = hasTbd || showEmptyCardsInSections
        if (showTbdSection) {
            item {
                TimelineSection(
                    title = "Anticipated",
                    entries = anticipatedEntries,
                    style = TimelineSectionStyle.ANTICIPATED,
                    isExpanded = isSectionsExpanded,
                    onToggle = onToggleSections,
                    onEntryClick = onShowClick,
                    showEmptyCards = showEmptyCardsInSections
                )
            }
        }

        // Footer
        item {
            TimelineFooter(
                onViewFullTimelineClick = onViewFullTimelineClick
            )
        }

        // Bottom spacing for nav bar
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Primary,
            strokeWidth = 3.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimelineScreenPreview() {
    Countdown2BingeTheme {
        // Preview with mock data would go here
    }
}
