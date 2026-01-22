package io.designtoswiftui.countdown2binge.ui.bingeready

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.ui.bingeready.components.BingeReadyCardStack
import io.designtoswiftui.countdown2binge.ui.bingeready.components.BingeReadySeasonData
import io.designtoswiftui.countdown2binge.ui.bingeready.components.EpisodeProgressBar
import io.designtoswiftui.countdown2binge.ui.bingeready.components.ShowSelector
import io.designtoswiftui.countdown2binge.ui.bingeready.components.ShowThumbnailData
import io.designtoswiftui.countdown2binge.ui.theme.BingeReadyBackground
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.Destructive
import io.designtoswiftui.countdown2binge.ui.theme.Primary
import io.designtoswiftui.countdown2binge.viewmodels.BingeReadySeason
import io.designtoswiftui.countdown2binge.viewmodels.BingeReadyViewModel

/**
 * Data class to group seasons by show for the card stack.
 */
private data class ShowWithSeasons(
    val showId: Long,
    val showTitle: String,
    val posterUrl: String?,
    val seasons: List<BingeReadySeason>
)

/**
 * Binge Ready screen displaying shows with completed seasons available for binge-watching.
 * Matches iOS design:
 * - Header: "BINGE READY" title
 * - Card Stack: 4-direction swipe for seasons (fanned hand effect)
 * - Episode Progress Bar: Premiere to Finale progress
 * - Show Selector: Horizontal thumbnail strip
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeReadyScreen(
    viewModel: BingeReadyViewModel = hiltViewModel(),
    onSeasonClick: (Long) -> Unit = {}
) {
    val bingeReadySeasons by viewModel.bingeReadySeasons.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isEmpty by viewModel.isEmpty.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Group seasons by show
    val showsWithSeasons by remember(bingeReadySeasons) {
        derivedStateOf {
            bingeReadySeasons
                .groupBy { it.show.id }
                .map { (showId, showSeasons) ->
                    val firstShow = showSeasons.first().show
                    ShowWithSeasons(
                        showId = showId,
                        showTitle = firstShow.title,
                        posterUrl = firstShow.posterPath?.let {
                            "${TMDBService.IMAGE_BASE_URL}${TMDBService.POSTER_SIZE}$it"
                        },
                        seasons = showSeasons.sortedBy { it.season.seasonNumber }
                    )
                }
                .sortedBy { it.showTitle }
        }
    }

    // State for current show and season selection
    var selectedShowIndex by remember { mutableIntStateOf(0) }
    var currentSeasonIndex by remember { mutableIntStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Reset season index when show changes
    val selectedShow = showsWithSeasons.getOrNull(selectedShowIndex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BingeReadyBackground)
    ) {
        when {
            isLoading -> LoadingState()
            isEmpty || showsWithSeasons.isEmpty() -> EmptyState()
            else -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = viewModel::refreshFromNetwork,
                    modifier = Modifier.fillMaxSize()
                ) {
                    BingeReadyContent(
                        showsWithSeasons = showsWithSeasons,
                        selectedShowIndex = selectedShowIndex,
                        currentSeasonIndex = currentSeasonIndex,
                        onShowSelected = { index ->
                            selectedShowIndex = index
                            currentSeasonIndex = 0 // Reset to first season when switching shows
                        },
                        onSeasonChanged = { currentSeasonIndex = it },
                        onMarkWatched = { season ->
                            viewModel.markSeasonWatched(season.seasonId)
                        },
                        onDeleteShow = { showDeleteDialog = true },
                        onCardClick = { season -> onSeasonClick(season.showId) }
                    )
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog && selectedShow != null) {
            DeleteConfirmationDialog(
                showTitle = selectedShow.showTitle,
                onConfirm = {
                    showDeleteDialog = false
                    viewModel.unfollowShow(selectedShow.showId)
                    // Adjust selected index if needed
                    if (selectedShowIndex >= showsWithSeasons.size - 1 && selectedShowIndex > 0) {
                        selectedShowIndex--
                    }
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

@Composable
private fun BingeReadyContent(
    showsWithSeasons: List<ShowWithSeasons>,
    selectedShowIndex: Int,
    currentSeasonIndex: Int,
    onShowSelected: (Int) -> Unit,
    onSeasonChanged: (Int) -> Unit,
    onMarkWatched: (BingeReadySeasonData) -> Unit,
    onDeleteShow: () -> Unit,
    onCardClick: (BingeReadySeasonData) -> Unit
) {
    val selectedShow = showsWithSeasons.getOrNull(selectedShowIndex) ?: return
    val currentSeason = selectedShow.seasons.getOrNull(currentSeasonIndex)

    // State for mark watched animation - tracks seasons that have been marked
    var markingWatchedSeason by remember { mutableStateOf<BingeReadySeasonData?>(null) }
    var markedWatchedSeasons by remember { mutableStateOf(setOf<Long>()) } // Keep filled after animation

    // Track pending watched seasons - will be synced when switching shows
    var pendingWatchedSeasons by remember { mutableStateOf(setOf<Long>()) }
    var previousShowId by remember { mutableStateOf(selectedShow.showId) }

    // When marking watched, play animation but DON'T sync to ViewModel yet
    // This keeps the card in place until user switches shows
    LaunchedEffect(markingWatchedSeason) {
        markingWatchedSeason?.let { season ->
            // Wait for fill animation to complete (10 segments * 80ms + 300ms spring)
            delay(1100)
            // Keep this season marked as filled visually
            markedWatchedSeasons = markedWatchedSeasons + season.seasonId
            // Track as pending - will sync when switching shows
            pendingWatchedSeasons = pendingWatchedSeasons + season.seasonId
            markingWatchedSeason = null
        }
    }

    // Sync pending watched seasons when switching to a different show
    LaunchedEffect(selectedShow.showId) {
        if (selectedShow.showId != previousShowId) {
            // Sync all pending watched seasons from the previous show
            pendingWatchedSeasons.forEach { seasonId ->
                // Find the season data to call onMarkWatched
                showsWithSeasons.find { it.showId == previousShowId }
                    ?.seasons
                    ?.find { it.season.id == seasonId }
                    ?.let { bingeReadySeason ->
                        onMarkWatched(
                            BingeReadySeasonData(
                                showId = bingeReadySeason.show.id,
                                seasonId = bingeReadySeason.season.id,
                                seasonNumber = bingeReadySeason.season.seasonNumber,
                                posterUrl = null,
                                title = bingeReadySeason.show.title,
                                episodesRemaining = bingeReadySeason.season.episodeCount
                            )
                        )
                    }
            }
            // Clear pending and visual state for new show
            pendingWatchedSeasons = emptySet()
            markedWatchedSeasons = emptySet()
            previousShowId = selectedShow.showId
        }
    }

    // Keep updated references for disposal
    val currentPendingSeasons = rememberUpdatedState(pendingWatchedSeasons)
    val currentShowsWithSeasons = rememberUpdatedState(showsWithSeasons)
    val currentSelectedShow = rememberUpdatedState(selectedShow)
    val currentOnMarkWatched = rememberUpdatedState(onMarkWatched)

    // Sync pending watched seasons when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            // Sync all pending watched seasons for the current show
            currentPendingSeasons.value.forEach { seasonId ->
                currentShowsWithSeasons.value
                    .find { it.showId == currentSelectedShow.value.showId }
                    ?.seasons
                    ?.find { it.season.id == seasonId }
                    ?.let { bingeReadySeason ->
                        currentOnMarkWatched.value(
                            BingeReadySeasonData(
                                showId = bingeReadySeason.show.id,
                                seasonId = bingeReadySeason.season.id,
                                seasonNumber = bingeReadySeason.season.seasonNumber,
                                posterUrl = null,
                                title = bingeReadySeason.show.title,
                                episodesRemaining = bingeReadySeason.season.episodeCount
                            )
                        )
                    }
            }
        }
    }

    // Convert to BingeReadySeasonData for the card stack
    val seasonData = remember(selectedShow) {
        selectedShow.seasons.map { bingeReadySeason ->
            BingeReadySeasonData(
                showId = bingeReadySeason.show.id,
                seasonId = bingeReadySeason.season.id,
                seasonNumber = bingeReadySeason.season.seasonNumber,
                posterUrl = (bingeReadySeason.season.posterPath ?: bingeReadySeason.show.posterPath)?.let {
                    "${TMDBService.IMAGE_BASE_URL}${TMDBService.POSTER_SIZE}$it"
                },
                title = bingeReadySeason.show.title,
                episodesRemaining = bingeReadySeason.season.episodeCount
            )
        }
    }

    // Convert to ShowThumbnailData for the selector
    val showThumbnails = remember(showsWithSeasons) {
        showsWithSeasons.map { show ->
            ShowThumbnailData(
                id = show.showId,
                posterUrl = show.posterUrl,
                title = show.showTitle
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        // Calculate card size: 70% of screen width, 1.5:1 aspect ratio
        val cardWidth = maxWidth * 0.7f
        val cardHeight = cardWidth * 1.5f

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            BingeReadyHeader()

            Spacer(modifier = Modifier.height(24.dp))

            // Show Title (above card stack)
            Text(
                text = selectedShow.showTitle.uppercase(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Card Stack
            BingeReadyCardStack(
                seasons = seasonData,
                currentIndex = currentSeasonIndex,
                onIndexChange = onSeasonChanged,
                onMarkWatched = { season ->
                    // Trigger fill animation first
                    markingWatchedSeason = season
                },
                onDelete = onDeleteShow,
                onCardClick = onCardClick,
                cardSize = DpSize(cardWidth, cardHeight)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Episode Progress Bar
            if (currentSeason != null) {
                // Determine watched count:
                // - If marking this season watched, show full (triggers fill animation)
                // - If already marked watched, stay full
                // - Otherwise show 0 (empty state)
                val isMarkingCurrentSeason = markingWatchedSeason?.seasonId == currentSeason.season.id
                val isAlreadyMarked = markedWatchedSeasons.contains(currentSeason.season.id)
                val watchedCount = if (isMarkingCurrentSeason || isAlreadyMarked) {
                    currentSeason.season.episodeCount // Full
                } else {
                    0 // Empty
                }

                EpisodeProgressBar(
                    totalEpisodes = currentSeason.season.episodeCount,
                    watchedCount = watchedCount,
                    showKey = selectedShow.showId,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Show Selector
            ShowSelector(
                shows = showThumbnails,
                selectedIndex = selectedShowIndex,
                onShowSelected = onShowSelected
            )

            // Bottom safe area padding
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun BingeReadyHeader(
    modifier: Modifier = Modifier
) {
    Text(
        text = "BINGE READY",
        fontSize = 28.sp,
        fontWeight = FontWeight.Black,
        color = Color.White,
        letterSpacing = (-0.5).sp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp)
    )
}

/**
 * Delete confirmation dialog.
 */
@Composable
private fun DeleteConfirmationDialog(
    showTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Remove Show",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("Are you sure you want to remove \"$showTitle\" from your binge list?")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Remove",
                    color = Destructive
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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

/**
 * Empty state when no shows are binge ready.
 */
@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Nothing to Binge Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Shows with completed seasons\nwill appear here",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BingeReadyScreenPreview() {
    Countdown2BingeTheme {
        // Preview with mock data would go here
    }
}
