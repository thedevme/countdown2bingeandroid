package io.designtoswiftui.countdown2binge.ui.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.countdown2binge.models.SeasonState
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.ui.components.CountdownSize
import io.designtoswiftui.countdown2binge.ui.components.DaysCountdown
import io.designtoswiftui.countdown2binge.ui.components.EpisodesCountdown
import io.designtoswiftui.countdown2binge.ui.components.ShowCard
import io.designtoswiftui.countdown2binge.ui.components.StateBadge
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle
import io.designtoswiftui.countdown2binge.ui.theme.Primary
import io.designtoswiftui.countdown2binge.ui.theme.StateAiring
import io.designtoswiftui.countdown2binge.ui.theme.StatePremieringFrom
import io.designtoswiftui.countdown2binge.viewmodels.TimelineShow
import io.designtoswiftui.countdown2binge.viewmodels.TimelineViewModel

/**
 * Timeline screen showing shows grouped by their current state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel(),
    onShowClick: (Long) -> Unit = {}
) {
    val airingShows by viewModel.airingShows.collectAsState()
    val premieringShows by viewModel.premieringShows.collectAsState()
    val anticipatedShows by viewModel.anticipatedShows.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isEmpty by viewModel.isEmpty.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        when {
            isLoading -> {
                LoadingState()
            }
            isEmpty -> {
                EmptyState()
            }
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
                        onShowClick = onShowClick
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
    onShowClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header
        item {
            TimelineHeader()
        }

        // Airing Now Section
        if (airingShows.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Airing Now",
                    subtitle = "${airingShows.size} ${if (airingShows.size == 1) "show" else "shows"}"
                )
            }
            items(
                items = airingShows,
                key = { "airing_${it.show.id}" }
            ) { timelineShow ->
                TimelineShowItem(
                    timelineShow = timelineShow,
                    onClick = { onShowClick(timelineShow.show.id) }
                )
            }
        }

        // Premiering Soon Section
        if (premieringShows.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Premiering Soon",
                    subtitle = "${premieringShows.size} ${if (premieringShows.size == 1) "show" else "shows"}"
                )
            }
            items(
                items = premieringShows,
                key = { "premiering_${it.show.id}" }
            ) { timelineShow ->
                TimelineShowItem(
                    timelineShow = timelineShow,
                    onClick = { onShowClick(timelineShow.show.id) }
                )
            }
        }

        // Anticipated Section
        if (anticipatedShows.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Anticipated",
                    subtitle = "${anticipatedShows.size} ${if (anticipatedShows.size == 1) "show" else "shows"}"
                )
            }
            items(
                items = anticipatedShows,
                key = { "anticipated_${it.show.id}" }
            ) { timelineShow ->
                TimelineShowItem(
                    timelineShow = timelineShow,
                    onClick = { onShowClick(timelineShow.show.id) }
                )
            }
        }
    }
}

@Composable
private fun TimelineHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text = "Timeline",
            color = OnBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Your shows at a glance",
            color = OnBackgroundMuted,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp, bottom = 12.dp)
    ) {
        Text(
            text = title,
            color = OnBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subtitle,
            color = OnBackgroundSubtle,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun TimelineShowItem(
    timelineShow: TimelineShow,
    onClick: () -> Unit
) {
    val backdropUrl = timelineShow.show.backdropPath?.let {
        "${TMDBService.IMAGE_BASE_URL}${TMDBService.BACKDROP_SIZE}$it"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        ShowCard(
            title = timelineShow.show.title,
            backdropUrl = backdropUrl,
            onClick = onClick,
            badge = {
                timelineShow.season?.state?.let { state ->
                    StateBadge(state = state)
                }
            }
        )

        // Countdown overlay (bottom right)
        timelineShow.season?.let { season ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                when (season.state) {
                    SeasonState.AIRING -> {
                        // Show days until finale or episodes remaining
                        timelineShow.daysUntilFinale?.let { days ->
                            DaysCountdown(
                                days = days,
                                size = CountdownSize.Small,
                                accentColor = StateAiring
                            )
                        } ?: timelineShow.episodesRemaining?.let { episodes ->
                            EpisodesCountdown(
                                episodes = episodes,
                                size = CountdownSize.Small,
                                accentColor = StateAiring
                            )
                        }
                    }
                    SeasonState.PREMIERING -> {
                        timelineShow.daysUntilPremiere?.let { days ->
                            DaysCountdown(
                                days = days,
                                size = CountdownSize.Small,
                                accentColor = StatePremieringFrom
                            )
                        }
                    }
                    else -> { /* No countdown for other states */ }
                }
            }
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
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "No shows yet",
                color = OnBackgroundMuted,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Search for TV shows to start tracking your binge timeline",
                color = OnBackgroundSubtle,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 32.dp),
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
