package io.designtoswiftui.countdown2binge.ui.showdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.models.SeasonState
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.models.ShowStatus
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.ui.components.CountdownSize
import io.designtoswiftui.countdown2binge.ui.components.DaysCountdown
import io.designtoswiftui.countdown2binge.ui.components.EpisodesCountdown
import io.designtoswiftui.countdown2binge.ui.components.StateBadge
import io.designtoswiftui.countdown2binge.ui.components.backgroundColor
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.GradientOverlayEnd
import io.designtoswiftui.countdown2binge.ui.theme.GradientOverlayStart
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle
import io.designtoswiftui.countdown2binge.ui.theme.Primary
import io.designtoswiftui.countdown2binge.ui.theme.Surface
import io.designtoswiftui.countdown2binge.ui.theme.SurfaceVariant
import io.designtoswiftui.countdown2binge.viewmodels.SeasonDetail
import io.designtoswiftui.countdown2binge.viewmodels.ShowDetailViewModel

/**
 * Show Detail screen displaying a show with its seasons.
 */
@Composable
fun ShowDetailScreen(
    showId: Long,
    viewModel: ShowDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSeasonClick: (Long) -> Unit = {}
) {
    val show by viewModel.show.collectAsState()
    val seasons by viewModel.seasons.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Load show if not already loaded via SavedStateHandle
    LaunchedEffect(showId) {
        if (showId > 0 && show == null) {
            viewModel.loadShowById(showId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        when {
            isLoading -> {
                LoadingState()
            }
            error != null -> {
                ErrorState(message = error!!, onBackClick = onBackClick)
            }
            show != null -> {
                ShowDetailContent(
                    show = show!!,
                    seasons = seasons,
                    onBackClick = onBackClick,
                    onSeasonClick = onSeasonClick
                )
            }
        }
    }
}

@Composable
private fun ShowDetailContent(
    show: Show,
    seasons: List<SeasonDetail>,
    onBackClick: () -> Unit,
    onSeasonClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Hero backdrop header
        item {
            HeroHeader(
                show = show,
                onBackClick = onBackClick
            )
        }

        // Show info section
        item {
            ShowInfoSection(show = show)
        }

        // Seasons header
        if (seasons.isNotEmpty()) {
            item {
                SeasonsHeader(count = seasons.size)
            }
        }

        // Season cards
        items(
            items = seasons,
            key = { it.season.id }
        ) { seasonDetail ->
            SeasonCard(
                seasonDetail = seasonDetail,
                onClick = { onSeasonClick(seasonDetail.season.id) }
            )
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroHeader(
    show: Show,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    ) {
        // Backdrop image
        AsyncImage(
            model = show.backdropPath?.let {
                "${TMDBService.IMAGE_BASE_URL}${TMDBService.BACKDROP_SIZE}$it"
            },
            contentDescription = show.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GradientOverlayStart,
                            GradientOverlayEnd
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = OnBackground
            )
        }

        // Title overlay at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = show.title,
                color = OnBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status badge
            ShowStatusBadge(status = show.status)
        }
    }
}

@Composable
private fun ShowStatusBadge(status: ShowStatus) {
    val (text, color) = when (status) {
        ShowStatus.RETURNING -> "Returning Series" to Primary
        ShowStatus.ENDED -> "Ended" to OnBackgroundMuted
        ShowStatus.CANCELED -> "Canceled" to OnBackgroundMuted
        ShowStatus.IN_PRODUCTION -> "In Production" to Primary
        ShowStatus.PLANNED -> "Planned" to OnBackgroundSubtle
        ShowStatus.UNKNOWN -> "Unknown" to OnBackgroundSubtle
    }

    Box(
        modifier = Modifier
            .background(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ShowInfoSection(show: Show) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Overview
        show.overview?.let { overview ->
            if (overview.isNotEmpty()) {
                Text(
                    text = overview,
                    color = OnBackgroundMuted,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun SeasonsHeader(count: Int) {
    Text(
        text = "Seasons ($count)",
        color = OnBackground,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SeasonCard(
    seasonDetail: SeasonDetail,
    onClick: () -> Unit
) {
    val season = seasonDetail.season

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Season info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Season ${season.seasonNumber}",
                        color = OnBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    StateBadge(
                        state = season.state,
                        showDot = true
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${season.episodeCount} episodes",
                    color = OnBackgroundSubtle,
                    fontSize = 13.sp
                )
            }

            // Right side: Countdown
            SeasonCountdown(seasonDetail = seasonDetail)
        }
    }
}

@Composable
private fun SeasonCountdown(seasonDetail: SeasonDetail) {
    val season = seasonDetail.season
    val accentColor = season.state.backgroundColor

    when (season.state) {
        SeasonState.PREMIERING -> {
            seasonDetail.daysUntilPremiere?.let { days ->
                DaysCountdown(
                    days = days,
                    size = CountdownSize.Small,
                    accentColor = accentColor
                )
            }
        }
        SeasonState.AIRING -> {
            seasonDetail.daysUntilFinale?.let { days ->
                DaysCountdown(
                    days = days,
                    size = CountdownSize.Small,
                    accentColor = accentColor
                )
            } ?: seasonDetail.episodesRemaining?.let { episodes ->
                EpisodesCountdown(
                    episodes = episodes,
                    size = CountdownSize.Small,
                    accentColor = accentColor
                )
            }
        }
        else -> {
            // No countdown for other states
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
private fun ErrorState(
    message: String,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Something went wrong",
            color = OnBackgroundMuted,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            color = OnBackgroundSubtle,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go back",
                tint = OnBackground
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShowDetailScreenPreview() {
    Countdown2BingeTheme {
        // Preview with mock data would go here
    }
}
