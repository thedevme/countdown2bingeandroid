package io.designtoswiftui.countdown2binge.ui.showdetail

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.countdown2binge.models.Episode
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle
import io.designtoswiftui.countdown2binge.ui.theme.Primary
import io.designtoswiftui.countdown2binge.ui.theme.StateWatched
import io.designtoswiftui.countdown2binge.ui.theme.Surface
import io.designtoswiftui.countdown2binge.ui.theme.SurfaceVariant
import io.designtoswiftui.countdown2binge.viewmodels.EpisodeListViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Episode List screen showing all episodes for a season.
 */
@Composable
fun EpisodeListScreen(
    seasonId: Long,
    viewModel: EpisodeListViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val show by viewModel.show.collectAsState()
    val season by viewModel.season.collectAsState()
    val episodes by viewModel.episodes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val watchedCount by viewModel.watchedCount.collectAsState()
    val error by viewModel.error.collectAsState()

    // Load episodes if not loaded via SavedStateHandle
    LaunchedEffect(seasonId) {
        if (seasonId > 0 && episodes.isEmpty()) {
            viewModel.loadEpisodesBySeasonId(seasonId)
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
            else -> {
                EpisodeListContent(
                    showTitle = show?.title ?: "Unknown Show",
                    seasonNumber = season?.seasonNumber ?: 0,
                    episodes = episodes,
                    watchedCount = watchedCount,
                    onBackClick = onBackClick,
                    onEpisodeToggle = viewModel::toggleEpisodeWatched,
                    onMarkAllWatched = viewModel::markAllWatched,
                    onUnmarkAllWatched = viewModel::unmarkAllWatched
                )
            }
        }
    }
}

@Composable
private fun EpisodeListContent(
    showTitle: String,
    seasonNumber: Int,
    episodes: List<Episode>,
    watchedCount: Int,
    onBackClick: () -> Unit,
    onEpisodeToggle: (Long) -> Unit,
    onMarkAllWatched: () -> Unit,
    onUnmarkAllWatched: () -> Unit
) {
    val totalEpisodes = episodes.size
    val progress = if (totalEpisodes > 0) watchedCount.toFloat() / totalEpisodes else 0f
    val allWatched = watchedCount == totalEpisodes && totalEpisodes > 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header with back button, title, and progress
        item {
            EpisodeListHeader(
                showTitle = showTitle,
                seasonNumber = seasonNumber,
                watchedCount = watchedCount,
                totalEpisodes = totalEpisodes,
                progress = progress,
                allWatched = allWatched,
                onBackClick = onBackClick,
                onMarkAllWatched = onMarkAllWatched,
                onUnmarkAllWatched = onUnmarkAllWatched
            )
        }

        // Episode items
        items(
            items = episodes,
            key = { it.id }
        ) { episode ->
            EpisodeItem(
                episode = episode,
                onToggle = { onEpisodeToggle(episode.id) }
            )
        }

        // Empty state
        if (episodes.isEmpty() && !allWatched) {
            item {
                EmptyEpisodeState()
            }
        }
    }
}

@Composable
private fun EpisodeListHeader(
    showTitle: String,
    seasonNumber: Int,
    watchedCount: Int,
    totalEpisodes: Int,
    progress: Float,
    allWatched: Boolean,
    onBackClick: () -> Unit,
    onMarkAllWatched: () -> Unit,
    onUnmarkAllWatched: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Back button and title row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = OnBackground
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = showTitle,
                    color = OnBackgroundMuted,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Season $seasonNumber",
                    color = OnBackground,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        color = OnBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$watchedCount / $totalEpisodes",
                        color = if (allWatched) Primary else OnBackgroundMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Primary,
                    trackColor = SurfaceVariant,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mark all button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = if (allWatched) onUnmarkAllWatched else onMarkAllWatched
                    ) {
                        Text(
                            text = if (allWatched) "Unmark All" else "Mark All Watched",
                            color = if (allWatched) StateWatched else Primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun EpisodeItem(
    episode: Episode,
    onToggle: () -> Unit
) {
    val isWatched = episode.isWatched
    val checkboxColor by animateColorAsState(
        targetValue = if (isWatched) Primary else SurfaceVariant,
        label = "checkbox_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(checkboxColor)
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center
            ) {
                if (isWatched) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Watched",
                        tint = OnBackground,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = "${episode.episodeNumber}",
                        color = OnBackgroundMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Episode info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = episode.name,
                    color = if (isWatched) OnBackgroundMuted else OnBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "E${episode.episodeNumber}",
                        color = OnBackgroundSubtle,
                        fontSize = 12.sp
                    )

                    episode.airDate?.let { date ->
                        Text(
                            text = formatAirDate(date),
                            color = OnBackgroundSubtle,
                            fontSize = 12.sp
                        )
                    }

                    episode.runtime?.let { runtime ->
                        if (runtime > 0) {
                            Text(
                                text = "${runtime}m",
                                color = OnBackgroundSubtle,
                                fontSize = 12.sp
                            )
                        }
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

@Composable
private fun EmptyEpisodeState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No episodes available",
            color = OnBackgroundSubtle,
            fontSize = 16.sp
        )
    }
}

/**
 * Format air date for display.
 */
private fun formatAirDate(date: LocalDate): String {
    return try {
        date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    } catch (e: Exception) {
        date.toString()
    }
}

@Preview(showBackground = true)
@Composable
private fun EpisodeListScreenPreview() {
    Countdown2BingeTheme {
        // Preview with mock data would go here
    }
}
