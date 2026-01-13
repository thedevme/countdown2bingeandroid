package io.designtoswiftui.countdown2binge.ui.bingeready

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.Destructive
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle
import io.designtoswiftui.countdown2binge.ui.theme.Primary
import io.designtoswiftui.countdown2binge.ui.theme.StateBingeReady
import io.designtoswiftui.countdown2binge.ui.theme.Surface
import io.designtoswiftui.countdown2binge.ui.theme.SurfaceVariant
import io.designtoswiftui.countdown2binge.viewmodels.BingeReadySeason
import io.designtoswiftui.countdown2binge.viewmodels.BingeReadyViewModel

/**
 * Data class to group seasons by show for the card stack
 */
private data class ShowWithSeasons(
    val showId: Long,
    val showTitle: String,
    val seasons: List<BingeReadySeason>
)

/**
 * Binge Ready screen showing seasons grouped by show with horizontal paging.
 * Matches iOS design: vertical list of shows, horizontal pager of seasons per show.
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
                    BingeReadyContent(
                        seasons = bingeReadySeasons,
                        onSeasonClick = onSeasonClick,
                        onMarkWatched = viewModel::markSeasonWatched,
                        onRemoveShow = viewModel::unfollowShow
                    )
                }
            }
        }
    }
}

@Composable
private fun BingeReadyContent(
    seasons: List<BingeReadySeason>,
    onSeasonClick: (Long) -> Unit,
    onMarkWatched: (Long) -> Unit,
    onRemoveShow: (Long) -> Unit
) {
    // Group seasons by show, sorted by season number (most recent first)
    val showsWithSeasons = seasons
        .groupBy { it.show.id }
        .map { (showId, showSeasons) ->
            ShowWithSeasons(
                showId = showId,
                showTitle = showSeasons.first().show.title,
                seasons = showSeasons.sortedByDescending { it.season.seasonNumber }
            )
        }
        .sortedBy { it.showTitle }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header
        item {
            BingeReadyHeader(count = seasons.size)
        }

        // Show stacks
        items(
            items = showsWithSeasons,
            key = { it.showId }
        ) { showWithSeasons ->
            ShowSeasonStack(
                showWithSeasons = showWithSeasons,
                onSeasonClick = onSeasonClick,
                onMarkWatched = onMarkWatched,
                onRemoveShow = { onRemoveShow(showWithSeasons.showId) }
            )
        }
    }
}

@Composable
private fun BingeReadyHeader(count: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text = "Binge Ready",
            color = OnBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ShowSeasonStack(
    showWithSeasons: ShowWithSeasons,
    onSeasonClick: (Long) -> Unit,
    onMarkWatched: (Long) -> Unit,
    onRemoveShow: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val pagerState = rememberPagerState(pageCount = { showWithSeasons.seasons.size })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section header
        Text(
            text = "SEASONS",
            color = OnBackgroundMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Horizontal pager for seasons
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 12.dp
            ) { page ->
                val season = showWithSeasons.seasons[page]
                SeasonCard(
                    bingeReadySeason = season,
                    onClick = { onSeasonClick(season.show.id) },
                    onSwipeDown = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMarkWatched(season.season.id)
                    },
                    onSwipeUp = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRemoveShow()
                    }
                )
            }

            // Next season indicator (if more seasons exist)
            if (showWithSeasons.seasons.size > 1 && pagerState.currentPage < showWithSeasons.seasons.size - 1) {
                val nextSeason = showWithSeasons.seasons.getOrNull(pagerState.currentPage + 1)
                if (nextSeason != null) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "S${nextSeason.season.seasonNumber}",
                            color = OnBackgroundMuted,
                            fontSize = 12.sp
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next season",
                            tint = OnBackgroundMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Page indicators
        if (showWithSeasons.seasons.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(showWithSeasons.seasons.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage)
                                    OnBackground
                                else
                                    OnBackgroundSubtle
                            )
                    )
                }
            }
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Complete button
            Row(
                modifier = Modifier
                    .clickable {
                        val currentSeason = showWithSeasons.seasons[pagerState.currentPage]
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onMarkWatched(currentSeason.season.id)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = StateBingeReady,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Complete",
                    color = StateBingeReady,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(32.dp))

            // Remove button
            Row(
                modifier = Modifier
                    .clickable {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRemoveShow()
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Destructive,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Remove",
                    color = Destructive,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SeasonCard(
    bingeReadySeason: BingeReadySeason,
    onClick: () -> Unit,
    onSwipeDown: () -> Unit,
    onSwipeUp: () -> Unit
) {
    val show = bingeReadySeason.show
    val season = bingeReadySeason.season
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 100f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        when {
                            dragOffset > swipeThreshold -> onSwipeDown()
                            dragOffset < -swipeThreshold -> onSwipeUp()
                        }
                        dragOffset = 0f
                    },
                    onDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                    }
                )
            }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster image
            AsyncImage(
                model = show.posterPath?.let {
                    "${TMDBService.IMAGE_BASE_URL}${TMDBService.POSTER_SIZE}$it"
                },
                contentDescription = show.title,
                modifier = Modifier
                    .width(100.dp)
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Show and season info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Season number
                Text(
                    text = "SEASON ${season.seasonNumber}",
                    color = OnBackgroundMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Show title
                Text(
                    text = show.title,
                    color = OnBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Binge Ready badge
                Box(
                    modifier = Modifier
                        .background(
                            color = StateBingeReady.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(StateBingeReady)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Binge Ready",
                            color = StateBingeReady,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Episodes and Status row
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "EPISODES",
                            color = OnBackgroundSubtle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${season.episodeCount}/${season.episodeCount}",
                            color = OnBackground,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    Column {
                        Text(
                            text = "STATUS",
                            color = OnBackgroundSubtle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Ready!",
                            color = StateBingeReady,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = StateBingeReady.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📺",
                    fontSize = 36.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Nothing ready yet",
                color = OnBackgroundMuted,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "When a season finishes airing, it will appear here ready to binge",
                color = OnBackgroundSubtle,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 32.dp),
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BingeReadyScreenPreview() {
    Countdown2BingeTheme {
        // Preview with mock data would go here
    }
}
