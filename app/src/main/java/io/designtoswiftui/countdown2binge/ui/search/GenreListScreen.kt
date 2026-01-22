package io.designtoswiftui.countdown2binge.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.countdown2binge.models.ShowCategory
import io.designtoswiftui.countdown2binge.models.TrendingShowDisplay
import io.designtoswiftui.countdown2binge.ui.search.components.TrendingShowCard
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.Primary
import io.designtoswiftui.countdown2binge.ui.theme.SurfaceVariant
import io.designtoswiftui.countdown2binge.viewmodels.GenreListViewModel

/**
 * Screen showing shows filtered by genre category.
 * Displays shows in a 2-column grid with pagination.
 */
@Composable
fun GenreListScreen(
    category: ShowCategory,
    onBackClick: () -> Unit,
    onShowClick: (Int) -> Unit,
    viewModel: GenreListViewModel = hiltViewModel()
) {
    val shows by viewModel.shows.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val followedShows by viewModel.followedShows.collectAsState()
    val addingShows by viewModel.addingShows.collectAsState()
    val showIdMap by viewModel.showIdMap.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    // Load shows on first composition
    LaunchedEffect(category) {
        viewModel.loadShows(category)
    }

    // Show snackbar when message changes
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    // Pagination - load more when reaching end
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isLoadingMore && viewModel.hasMore()) {
            viewModel.loadMore()
        }
    }

    Scaffold(
        containerColor = Background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceVariant,
                    contentColor = OnBackground,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            GenreListHeader(
                categoryName = category.displayName,
                onBackClick = onBackClick
            )

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                    shows.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No shows found",
                                color = OnBackground.copy(alpha = 0.6f),
                                fontSize = 16.sp
                            )
                        }
                    }
                    else -> {
                        GenreShowsGrid(
                            shows = shows,
                            followedShows = followedShows,
                            addingShows = addingShows,
                            showIdMap = showIdMap,
                            isLoadingMore = isLoadingMore,
                            listState = listState,
                            onShowClick = onShowClick,
                            onFollowClick = viewModel::toggleFollow
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GenreListHeader(
    categoryName: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = OnBackground
            )
        }

        Text(
            text = categoryName.uppercase(),
            color = OnBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun GenreShowsGrid(
    shows: List<TrendingShowDisplay>,
    followedShows: Set<Int>,
    addingShows: Set<Int>,
    showIdMap: Map<Int, Long>,
    isLoadingMore: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onShowClick: (Int) -> Unit,
    onFollowClick: (Int) -> Unit
) {
    // Group shows into pairs for 2-column layout
    val rows = shows.chunked(2)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = rows,
            key = { row -> row.first().tmdbId }
        ) { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { show ->
                    val isFollowed = followedShows.contains(show.tmdbId)
                    val localShowId = showIdMap[show.tmdbId]

                    TrendingShowCard(
                        show = show,
                        isFollowed = isFollowed,
                        isLoading = addingShows.contains(show.tmdbId),
                        onFollowClick = { onFollowClick(show.tmdbId) },
                        onCardClick = { onShowClick(show.tmdbId) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty space if odd number of items
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Loading more indicator
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Primary,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}
