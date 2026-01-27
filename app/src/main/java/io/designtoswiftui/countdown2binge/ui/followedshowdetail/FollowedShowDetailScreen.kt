package io.designtoswiftui.countdown2binge.ui.followedshowdetail

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.countdown2binge.models.GenreMapping
import io.designtoswiftui.countdown2binge.ui.detail.components.BackdropHeader
import io.designtoswiftui.countdown2binge.ui.followedshowdetail.components.BingeViewTab
import io.designtoswiftui.countdown2binge.ui.followedshowdetail.components.FollowedShowTabBar
import io.designtoswiftui.countdown2binge.ui.followedshowdetail.components.InfoTab
import io.designtoswiftui.countdown2binge.ui.followedshowdetail.components.SpinoffsTab
import io.designtoswiftui.countdown2binge.ui.followedshowdetail.components.StreamingProvidersSection
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.Primary

/**
 * Followed Show Detail screen displaying comprehensive show information
 * with tabbed interface for Binge View, Spinoffs, and Info.
 *
 * Layout (single scroll - tabs scroll with content):
 * 1. BackdropHeader (420dp hero with logo/title)
 * 2. Streaming Providers (Watch Now)
 * 3. Tab Bar (Binge View | Spinoffs | Info)
 * 4. Tab Content (swappable based on selected tab)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowedShowDetailScreen(
    showId: Long,
    viewModel: FollowedShowDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onShowClick: (Int) -> Unit = {},
    onVideoClick: (String, String) -> Unit = { _, _ -> }
) {
    val show by viewModel.show.collectAsState()
    val showDetails by viewModel.showDetails.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val availableTabs by viewModel.availableTabs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()
    val logoPath by viewModel.logoPath.collectAsState()

    // Binge View content
    val countdownState by viewModel.countdownState.collectAsState()
    val showCountdown by viewModel.showCountdown.collectAsState()
    val rankedSeasons by viewModel.rankedSeasons.collectAsState()
    val showRankings by viewModel.showRankings.collectAsState()

    // Spinoffs content
    val hasFranchise by viewModel.hasFranchise.collectAsState()
    val spinoffShows by viewModel.spinoffShows.collectAsState()
    val isLoadingSpinoffs by viewModel.isLoadingSpinoffs.collectAsState()

    // Streaming providers
    val watchProviders by viewModel.watchProviders.collectAsState()

    // Info content
    val videos by viewModel.videos.collectAsState()
    val cast by viewModel.cast.collectAsState()
    val crew by viewModel.crew.collectAsState()
    val isSynopsisExpanded by viewModel.isSynopsisExpanded.collectAsState()

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Premium state (placeholder - will be connected to settings later)
    val isPremium = true // TODO: Connect to actual premium state

    // Track followed shows for spinoffs (simplified - will be connected to repository later)
    var followedShowsSet by remember { mutableStateOf(setOf<Int>()) }
    var addingShowsSet by remember { mutableStateOf(setOf<Int>()) }

    // Delete animation state
    var isDeleting by remember { mutableStateOf(false) }
    val deleteAnimationProgress by animateFloatAsState(
        targetValue = if (isDeleting) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "delete_animation"
    )

    // Navigate back when animation completes
    LaunchedEffect(deleteAnimationProgress) {
        if (deleteAnimationProgress == 1f && isDeleting) {
            onBackClick()
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
                ErrorState(
                    message = error!!,
                    onBackClick = onBackClick
                )
            }
            showDetails != null -> {
                val details = showDetails!!

                // Extract data for Info tab
                val genres = details.genres?.mapNotNull { it.name } ?: emptyList()
                val genreIds = details.genres?.map { it.id } ?: emptyList()
                val genreNames = GenreMapping.getGenreNames(genreIds, 3)
                val createdBy = crew.filter { it.job == "Creator" }.map { it.name }
                val networkName = details.networks?.firstOrNull()?.name

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = screenWidth * deleteAnimationProgress)
                        .alpha(1f - deleteAnimationProgress)
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
                                    },
                                    onDeleteClick = {
                                        viewModel.unfollowShow {
                                            isDeleting = true
                                        }
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

                        // 2. Streaming Providers ("Watch Now")
                        if (watchProviders.isNotEmpty()) {
                            item {
                                StreamingProvidersSection(
                                    providers = watchProviders,
                                    showName = details.name
                                )
                            }
                        }

                        // Spacing before tabs
                        item { Spacer(modifier = Modifier.height(16.dp)) }

                        // 3. Tab Bar
                        item {
                            FollowedShowTabBar(
                                tabs = availableTabs,
                                selectedTab = selectedTab,
                                onTabSelected = viewModel::selectTab
                            )
                        }

                        item { Spacer(modifier = Modifier.height(16.dp)) }

                        // 4. Tab Content
                        when (selectedTab) {
                            FollowedShowTab.BINGE -> {
                                // Binge View Tab
                                item {
                                    BingeViewTab(
                                        showCountdown = showCountdown,
                                        countdownState = countdownState,
                                        showRankings = showRankings,
                                        rankedSeasons = rankedSeasons
                                    )
                                }
                            }
                            FollowedShowTab.SPINOFFS -> {
                                // Spinoffs Tab
                                item {
                                    SpinoffsTab(
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
                                        },
                                        onLoadSpinoffs = viewModel::loadSpinoffDetails
                                    )
                                }
                            }
                            FollowedShowTab.INFO -> {
                                // Info Tab
                                item {
                                    InfoTab(
                                        synopsis = details.overview,
                                        seasonCount = viewModel.seasonCount,
                                        statusText = viewModel.statusText,
                                        isSynopsisExpanded = isSynopsisExpanded,
                                        onSynopsisExpandClick = viewModel::toggleSynopsisExpanded,
                                        genres = genreNames,
                                        videos = videos,
                                        onVideoClick = { video ->
                                            onVideoClick(video.key, video.name)
                                        },
                                        cast = cast,
                                        createdBy = createdBy,
                                        network = networkName
                                    )
                                }
                            }
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

        Column(
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
