package io.designtoswiftui.countdown2binge.ui.bingeready

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
    var displayedShowIndex by remember { mutableIntStateOf(0) }
    var targetShowIndex by remember { mutableIntStateOf(0) }
    var isScrollingToShow by remember { mutableStateOf(false) }
    var isLastStep by remember { mutableStateOf(false) }
    var currentSeasonIndex by remember { mutableIntStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Animate through each intermediate show when target changes
    LaunchedEffect(targetShowIndex) {
        if (targetShowIndex == displayedShowIndex || isScrollingToShow) return@LaunchedEffect

        isScrollingToShow = true
        val direction = if (targetShowIndex > displayedShowIndex) 1 else -1

        // Calculate indices to scroll through
        val indicesToVisit = mutableListOf<Int>()
        var current = displayedShowIndex
        while (current != targetShowIndex) {
            current += direction
            indicesToVisit.add(current)
        }

        // Animate through each show
        for ((i, nextIndex) in indicesToVisit.withIndex()) {
            val isFinalStep = i == indicesToVisit.lastIndex
            isLastStep = isFinalStep

            // Fast through middle (150ms), slow landing (350ms)
            val stepDuration = if (isFinalStep) 350L else 150L

            displayedShowIndex = nextIndex
            delay(stepDuration)
        }

        isScrollingToShow = false
        isLastStep = false
    }

    // Reset season index when displayed show changes
    LaunchedEffect(displayedShowIndex) {
        currentSeasonIndex = 0
    }

    val selectedShow = showsWithSeasons.getOrNull(displayedShowIndex)

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
                        displayedShowIndex = displayedShowIndex,
                        targetShowIndex = targetShowIndex,
                        isLastStep = isLastStep,
                        currentSeasonIndex = currentSeasonIndex,
                        onShowSelected = { newIndex ->
                            if (newIndex != targetShowIndex && !isScrollingToShow) {
                                targetShowIndex = newIndex
                            }
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
                    // Adjust indices if needed
                    if (displayedShowIndex >= showsWithSeasons.size - 1 && displayedShowIndex > 0) {
                        displayedShowIndex--
                        targetShowIndex = displayedShowIndex
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
    displayedShowIndex: Int,
    targetShowIndex: Int,
    isLastStep: Boolean,
    currentSeasonIndex: Int,
    onShowSelected: (Int) -> Unit,
    onSeasonChanged: (Int) -> Unit,
    onMarkWatched: (BingeReadySeasonData) -> Unit,
    onDeleteShow: () -> Unit,
    onCardClick: (BingeReadySeasonData) -> Unit
) {
    val selectedShow = showsWithSeasons.getOrNull(displayedShowIndex) ?: return
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
        // Calculate fixed element heights
        val headerHeight = 44.dp      // Header text + padding
        val spacerAfterHeader = 24.dp
        val titleHeight = 50.dp       // Show title in card area
        val progressBarArea = 60.dp   // Progress bar + padding
        val spacerBeforeThumbs = 24.dp
        val thumbnailHeight = 120.dp  // Updated thumbnail size
        val bottomPadding = 16.dp     // Safe area (reduced since thumbnails are larger)

        val fixedHeight = headerHeight + spacerAfterHeader + titleHeight +
                          progressBarArea + spacerBeforeThumbs + thumbnailHeight + bottomPadding

        // Available height for card (with some padding)
        val availableHeightForCard = (maxHeight - fixedHeight - 32.dp).coerceAtLeast(200.dp)

        // Calculate card size to fill available space while maintaining 1.5:1 aspect ratio
        // Card height should fit in available space, width derived from aspect ratio
        val maxCardWidth = maxWidth * 0.75f
        val cardHeightFromAvailable = availableHeightForCard
        val cardWidthFromHeight = cardHeightFromAvailable / 1.5f

        // Use whichever constraint is tighter
        val cardWidth = minOf(cardWidthFromHeight, maxCardWidth)
        val cardHeight = cardWidth * 1.5f

        // Calculate scale factor based on screen height (similar to iOS)
        val scaleFactor = (maxHeight / 800.dp).coerceIn(0.8f, 1.2f)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            BingeReadyHeader()

            Spacer(modifier = Modifier.height(24.dp))

            // AnimatedContent for view replacement transition
            // Steps through each show with fast intermediate / slow landing
            // iOS uses same spring for ALL steps - only delay differs
            // This creates overlapping animations that feel smooth
            val springStiffness = 500f  // Consistent spring, ~0.4s to settle

            // Fixed offsets in dp (same as iOS)
            val density = androidx.compose.ui.platform.LocalDensity.current
            val enterOffsetPx = with(density) { 250.dp.roundToPx() }
            val exitOffsetPx = with(density) { 350.dp.roundToPx() }

            AnimatedContent(
                targetState = displayedShowIndex,
                transitionSpec = {
                    // Determine direction inside transitionSpec
                    val isForward = targetState > initialState

                    // Fixed pixel offsets based on direction
                    val enterOffset = if (isForward) -enterOffsetPx else enterOffsetPx
                    val exitOffset = if (isForward) exitOffsetPx else -exitOffsetPx

                    (slideInVertically(
                        initialOffsetY = { enterOffset },
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = springStiffness)
                    ) + fadeIn(
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = springStiffness)
                    ) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = springStiffness)
                    )).togetherWith(
                        slideOutVertically(
                            targetOffsetY = { exitOffset },
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = springStiffness)
                        ) + fadeOut(
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = springStiffness)
                        ) + scaleOut(
                            targetScale = 0.8f,
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = springStiffness)
                        )
                    ).using(SizeTransform(clip = false))
                },
                label = "showTransition",
                modifier = Modifier.weight(1f)
            ) { showIndex ->
                val show = showsWithSeasons.getOrNull(showIndex) ?: return@AnimatedContent

                val seasonData = show.seasons.map { bingeReadySeason ->
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

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = show.showTitle.uppercase(),
                        fontSize = (22 * scaleFactor).sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 40.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    BingeReadyCardStack(
                        seasons = seasonData,
                        currentIndex = if (showIndex == displayedShowIndex) currentSeasonIndex else 0,
                        onIndexChange = onSeasonChanged,
                        onMarkWatched = { season -> markingWatchedSeason = season },
                        onDelete = onDeleteShow,
                        onCardClick = onCardClick,
                        cardSize = DpSize(cardWidth, cardHeight)
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }
            }

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
                        .padding(horizontal = 30.dp)
                        .padding(top = (24 * scaleFactor).dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Show Selector (Thumbnail Nav) - follows displayedShowIndex to animate through
            ShowSelector(
                shows = showThumbnails,
                selectedIndex = displayedShowIndex,
                onShowSelected = onShowSelected
            )

            // Bottom safe area padding (reduced since thumbnails are larger)
            Spacer(modifier = Modifier.height(16.dp))
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
