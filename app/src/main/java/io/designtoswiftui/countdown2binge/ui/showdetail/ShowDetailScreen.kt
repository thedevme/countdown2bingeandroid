package io.designtoswiftui.countdown2binge.ui.showdetail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.countdown2binge.models.GenreMapping
import io.designtoswiftui.countdown2binge.ui.detail.components.BackdropHeader
import io.designtoswiftui.countdown2binge.ui.detail.components.CastSection
import io.designtoswiftui.countdown2binge.ui.detail.components.EpisodeSection
import io.designtoswiftui.countdown2binge.ui.detail.components.FollowActionButton
import io.designtoswiftui.countdown2binge.ui.detail.components.GenreTagsSection
import io.designtoswiftui.countdown2binge.ui.detail.components.InfoSection
import io.designtoswiftui.countdown2binge.ui.detail.components.MoreLikeThisSection
import io.designtoswiftui.countdown2binge.ui.detail.components.SeasonPicker
import io.designtoswiftui.countdown2binge.ui.detail.components.SpinoffSection
import io.designtoswiftui.countdown2binge.ui.detail.components.TechnicalSpecsSection
import io.designtoswiftui.countdown2binge.ui.detail.components.TrailersSection
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.Primary
import io.designtoswiftui.countdown2binge.viewmodels.ShowDetailViewModel

/**
 * Show Detail screen displaying comprehensive show information.
 * Works for both followed and non-followed shows - always fetches from TMDB.
 *
 * Layout (vertical scroll):
 * 1. BackdropHeader (420dp hero with logo/title)
 * 2. InfoSection (expandable synopsis + metadata)
 * 3. GenreTagsSection (max 3 tags)
 * 4. FollowActionButton (48dp full-width)
 * 5. SeasonPicker (if multiple seasons)
 * 6. EpisodeSection (episode list with watermark)
 * 7. TrailersSection (horizontal video scroll)
 * 8. CastSection (horizontal cast scroll)
 * 9. MoreLikeThisSection (2-column recommendations)
 * 10. SpinoffSection (premium-gated franchise shows)
 * 11. TechnicalSpecsSection (badges + info rows)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDetailScreen(
    showId: Long,
    viewModel: ShowDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSeasonClick: (Long) -> Unit = {},
    onShowClick: (Int) -> Unit = {},
    onVideoClick: (String, String) -> Unit = { _, _ -> }
) {
    val showDetails by viewModel.showDetails.collectAsState()
    val seasonDetails by viewModel.seasonDetails.collectAsState()
    val selectedSeasonNumber by viewModel.selectedSeasonNumber.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isFollowed by viewModel.isFollowed.collectAsState()
    val isAdding by viewModel.isAdding.collectAsState()
    val error by viewModel.error.collectAsState()
    val logoPath by viewModel.logoPath.collectAsState()
    val isSynopsisExpanded by viewModel.isSynopsisExpanded.collectAsState()
    val isEpisodeListExpanded by viewModel.isEpisodeListExpanded.collectAsState()

    // Additional content
    val videos by viewModel.videos.collectAsState()
    val cast by viewModel.cast.collectAsState()
    val crew by viewModel.crew.collectAsState()
    val recommendations by viewModel.recommendations.collectAsState()

    // Spinoffs
    val hasFranchise by viewModel.hasFranchise.collectAsState()
    val spinoffShows by viewModel.spinoffShows.collectAsState()
    val isLoadingSpinoffs by viewModel.isLoadingSpinoffs.collectAsState()

    // For opening YouTube links
    val context = LocalContext.current

    // Track followed shows for recommendations (simplified - just use isFollowed for current show)
    var followedShowsSet by remember { mutableStateOf(setOf<Int>()) }
    var addingShowsSet by remember { mutableStateOf(setOf<Int>()) }

    // Premium state (placeholder - will be connected to settings later)
    val isPremium = true // TODO: Connect to actual premium state

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
                ErrorState(
                    message = error!!,
                    onBackClick = onBackClick
                )
            }
            showDetails != null -> {
                val details = showDetails!!
                val genres = details.genres?.mapNotNull { it.name } ?: emptyList()
                val genreIds = details.genres?.map { it.id } ?: emptyList()
                val genreNames = GenreMapping.getGenreNames(genreIds, 3)
                val currentSeasonEpisodes = seasonDetails[selectedSeasonNumber]?.episodes ?: emptyList()
                val createdBy = crew.filter { it.job == "Creator" }.map { it.name }
                val networkName = details.networks?.firstOrNull()?.name

                // PullToRefreshBox with LazyColumn
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 1. BackdropHeader (420dp hero with logo/title)
                        item {
                            Box {
                                BackdropHeader(
                                    backdropPath = details.backdropPath,
                                    logoPath = logoPath,
                                    title = details.name,
                                    onShareClick = {
                                        val shareText = "Check out ${details.name} on Countdown2Binge!\nhttps://www.themoviedb.org/tv/${details.id}"
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share show"))
                                    }
                                )

                                // Back button overlay (with status bar padding)
                                IconButton(
                                    onClick = onBackClick,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .statusBarsPadding()
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = OnBackground
                                    )
                                }
                            }
                        }

                        // Spacing after header
                        item { Spacer(modifier = Modifier.height(20.dp)) }

                        // 2. InfoSection (synopsis + metadata)
                        item {
                            InfoSection(
                                synopsis = details.overview,
                                seasonCount = viewModel.seasonCount,
                                statusText = viewModel.statusText,
                                isExpanded = isSynopsisExpanded,
                                onExpandClick = viewModel::toggleSynopsisExpanded
                            )
                        }

                        // Spacing
                        item { Spacer(modifier = Modifier.height(16.dp)) }

                        // 3. GenreTagsSection
                        if (genreNames.isNotEmpty()) {
                            item {
                                GenreTagsSection(genres = genreNames)
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }

                        // 4. FollowActionButton
                        item {
                            FollowActionButton(
                                isFollowed = isFollowed,
                                isLoading = isAdding,
                                onClick = viewModel::toggleFollow
                            )
                        }

                        // Spacing
                        item { Spacer(modifier = Modifier.height(20.dp)) }

                        // 5. SeasonPicker (if multiple seasons)
                        if (viewModel.hasMultipleSeasons) {
                            item {
                                SeasonPicker(
                                    seasons = viewModel.regularSeasons,
                                    selectedSeason = selectedSeasonNumber,
                                    onSeasonSelected = viewModel::selectSeason
                                )
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }

                        // 6. EpisodeSection
                        if (currentSeasonEpisodes.isNotEmpty()) {
                            item {
                                EpisodeSection(
                                    seasonNumber = selectedSeasonNumber,
                                    episodes = currentSeasonEpisodes,
                                    isExpanded = isEpisodeListExpanded,
                                    onExpandClick = viewModel::toggleEpisodeListExpanded
                                )
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }

                        // 7. TrailersSection
                        if (videos.isNotEmpty()) {
                            item {
                                TrailersSection(
                                    videos = videos,
                                    onVideoClick = { video ->
                                        // Navigate to in-app YouTube player
                                        onVideoClick(video.key, video.name)
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }

                        // 8. CastSection
                        if (cast.isNotEmpty()) {
                            item {
                                CastSection(cast = cast)
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }

                        // 9. MoreLikeThisSection
                        if (recommendations.isNotEmpty()) {
                            item {
                                MoreLikeThisSection(
                                    recommendations = recommendations,
                                    followedShows = followedShowsSet,
                                    addingShows = addingShowsSet,
                                    onShowClick = onShowClick,
                                    onFollowClick = { tmdbId ->
                                        // TODO: Implement follow for recommendations
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }

                        // 10. SpinoffSection (premium-gated)
                        if (hasFranchise) {
                            item {
                                SpinoffSection(
                                    spinoffs = spinoffShows,
                                    followedShows = followedShowsSet,
                                    addingShows = addingShowsSet,
                                    isPremium = isPremium,
                                    isLoading = isLoadingSpinoffs,
                                    onShowClick = onShowClick,
                                    onFollowClick = { tmdbId ->
                                        // TODO: Implement follow for spinoffs
                                    },
                                    onUnlock = {
                                        // TODO: Open paywall
                                    }
                                )

                                // Load spinoff details when section is visible
                                if (isPremium && spinoffShows.isEmpty() && !isLoadingSpinoffs) {
                                    viewModel.loadSpinoffDetails()
                                }
                            }
                            item { Spacer(modifier = Modifier.height(24.dp)) }
                        }

                        // 11. TechnicalSpecsSection
                        item {
                            TechnicalSpecsSection(
                                createdBy = createdBy,
                                genres = genres,
                                network = networkName
                            )
                        }

                        // Bottom padding
                        item { Spacer(modifier = Modifier.height(40.dp)) }
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = OnBackground
            )
        }

        // Error message
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Something went wrong",
                color = OnBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = message,
                color = OnBackgroundMuted,
                fontSize = 14.sp
            )
        }
    }
}
