package io.designtoswiftui.countdown2binge.ui.bingeready

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Data class to group seasons by show for the card stack
 */
private data class ShowWithSeasons(
    val showId: Long,
    val showTitle: String,
    val seasons: List<BingeReadySeason>
)

/**
 * Binge Ready screen showing seasons grouped by show with stacked cards.
 * Matches iOS design: vertical list of shows, stacked swipeable cards per show.
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
            BingeReadyHeader()
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
private fun BingeReadyHeader() {
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
    var currentIndex by remember { mutableIntStateOf(0) }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val swipeThreshold = screenWidthPx * 0.3f

    var dragOffset by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
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

        // Stacked cards
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(horizontal = 16.dp)
                .pointerInput(showWithSeasons.seasons.size, currentIndex) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                dragOffset > swipeThreshold && currentIndex < showWithSeasons.seasons.size - 1 -> {
                                    // Swipe right - go to next (older) season
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    currentIndex++
                                }
                                dragOffset < -swipeThreshold && currentIndex > 0 -> {
                                    // Swipe left - go to previous (newer) season
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    currentIndex--
                                }
                            }
                            dragOffset = 0f
                        },
                        onHorizontalDrag = { _, amount ->
                            dragOffset += amount
                        }
                    )
                }
        ) {
            // Render stacked cards (show up to 3 behind current)
            val visibleRange = (currentIndex until minOf(currentIndex + 3, showWithSeasons.seasons.size))

            visibleRange.reversed().forEach { index ->
                val stackPosition = index - currentIndex
                val season = showWithSeasons.seasons[index]

                // Calculate offset and scale for stacking effect
                val offsetX = if (stackPosition == 0) dragOffset else 0f
                val scale = 1f - (stackPosition * 0.05f)
                val yOffset = stackPosition * 8
                val alpha = 1f - (stackPosition * 0.2f)

                SeasonCard(
                    bingeReadySeason = season,
                    onClick = { onSeasonClick(season.show.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex((10 - stackPosition).toFloat())
                        .offset { IntOffset(offsetX.roundToInt(), yOffset) }
                        .scale(scale)
                        .graphicsLayer { this.alpha = alpha }
                )
            }
        }

        // Page dots and next season indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Empty spacer for alignment
            Spacer(modifier = Modifier.width(48.dp))

            // Page dots
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                repeat(showWithSeasons.seasons.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentIndex)
                                    OnBackground
                                else
                                    OnBackgroundSubtle
                            )
                    )
                }
            }

            // Next season indicator
            if (currentIndex < showWithSeasons.seasons.size - 1) {
                val nextSeason = showWithSeasons.seasons[currentIndex + 1]
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "S${nextSeason.season.seasonNumber}",
                        color = OnBackgroundMuted,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Next season",
                        tint = OnBackgroundMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        // Action buttons
        val currentSeason = showWithSeasons.seasons.getOrNull(currentIndex)
        if (currentSeason != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // Complete button (swipe down)
                Row(
                    modifier = Modifier
                        .clickable {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onMarkWatched(currentSeason.season.id)
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = StateBingeReady,
                        modifier = Modifier.size(18.dp)
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

                // Remove button (swipe up)
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
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = Destructive,
                        modifier = Modifier.size(18.dp)
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
}

@Composable
private fun SeasonCard(
    bingeReadySeason: BingeReadySeason,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val show = bingeReadySeason.show
    val season = bingeReadySeason.season

    Card(
        modifier = modifier
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
