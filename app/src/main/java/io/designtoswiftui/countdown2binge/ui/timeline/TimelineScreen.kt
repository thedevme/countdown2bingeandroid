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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import io.designtoswiftui.countdown2binge.models.SeasonState
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle
import io.designtoswiftui.countdown2binge.ui.theme.Primary
import io.designtoswiftui.countdown2binge.ui.timeline.components.CountdownLabel
import io.designtoswiftui.countdown2binge.ui.timeline.components.DottedDivider
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
import io.designtoswiftui.countdown2binge.viewmodels.TimelineShow
import io.designtoswiftui.countdown2binge.viewmodels.TimelineViewModel
import java.time.LocalDateTime
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Timeline screen showing shows grouped by their current state.
 * Displays: Currently Airing (stacked cards) → Premiering Soon → Anticipated
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel(),
    onShowClick: (Long) -> Unit = {},
    onViewFullTimelineClick: () -> Unit = {}
) {
    val airingShows by viewModel.airingShows.collectAsState()
    val premieringShows by viewModel.premieringShows.collectAsState()
    val anticipatedShows by viewModel.anticipatedShows.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isEmpty by viewModel.isEmpty.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

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
            isEmpty -> EmptyTimelineState()
            else -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = viewModel::refreshFromNetwork,
                    modifier = Modifier.fillMaxSize()
                ) {
                    TimelineContent(
                        airingShows = airingShows,
                        premieringShows = premieringShows,
                        anticipatedShows = anticipatedShows,
                        heroDays = heroDays,
                        isHeroFinale = isHeroFinale,
                        onShowClick = onShowClick,
                        onViewFullTimelineClick = onViewFullTimelineClick
                    )
                }
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
    onShowClick: (Long) -> Unit,
    onViewFullTimelineClick: () -> Unit
) {
    // Track which card is currently selected in the pager
    var selectedCardIndex by remember { mutableIntStateOf(0) }

    // Single state controls BOTH sections (they sync together)
    var isSectionsExpanded by remember { mutableStateOf(false) }

    // Get the currently selected show's countdown data
    val selectedShow = remember(airingShows, selectedCardIndex) {
        airingShows.getOrNull(selectedCardIndex)
    }

    val selectedDays = remember(selectedShow) {
        selectedShow?.daysUntilFinale ?: selectedShow?.daysUntilPremiere
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
                countdownStyle = show.season?.premiereDate?.let { date ->
                    TimelineCountdownStyle.Year(date.year)
                } ?: TimelineCountdownStyle.TBD
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // CURRENTLY AIRING SECTION (if there are airing shows)
        if (airingShows.isNotEmpty()) {
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

            // Slot Machine Days Picker - updates based on selected card
            item {
                SlotMachineDaysPicker(
                    days = selectedDays ?: heroDays
                )
            }

            // Dotted divider below slot machine
            item {
                DottedDivider(
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        // PREMIERING SOON SECTION
        if (premieringShows.isNotEmpty()) {
            // If no airing shows, show Timeline header first
            if (airingShows.isEmpty()) {
                item {
                    TimelineHeader(
                        headerType = TimelineHeaderType.TIMELINE,
                        lastUpdated = LocalDateTime.now()
                    )
                }
            }

            item {
                TimelineSection(
                    title = "Premiering Soon",
                    entries = premieringEntries,
                    style = TimelineSectionStyle.PREMIERING_SOON,
                    isExpanded = isSectionsExpanded,
                    onToggle = { isSectionsExpanded = !isSectionsExpanded },
                    onEntryClick = onShowClick
                )
            }
        }

        // ANTICIPATED SECTION
        if (anticipatedShows.isNotEmpty()) {
            // If no airing or premiering shows, show Timeline header first
            if (airingShows.isEmpty() && premieringShows.isEmpty()) {
                item {
                    TimelineHeader(
                        headerType = TimelineHeaderType.TIMELINE,
                        lastUpdated = LocalDateTime.now()
                    )
                }
            }

            item {
                TimelineSection(
                    title = "Anticipated",
                    entries = anticipatedEntries,
                    style = TimelineSectionStyle.ANTICIPATED,
                    isExpanded = isSectionsExpanded,
                    onToggle = { isSectionsExpanded = !isSectionsExpanded },
                    onEntryClick = onShowClick
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

@Composable
private fun EmptyTimelineState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Your Timeline is Empty",
                color = OnBackgroundMuted,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Search for TV shows to start\ntracking your binge timeline",
                color = OnBackgroundSubtle,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimelineScreenPreview() {
    Countdown2BingeTheme {
        // Preview with mock data would go here
    }
}
