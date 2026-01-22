package io.designtoswiftui.countdown2binge.ui.search

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.models.AiringShowDisplay
import io.designtoswiftui.countdown2binge.models.ShowCategory
import io.designtoswiftui.countdown2binge.models.TrendingShowDisplay
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBSearchResult
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.ui.components.AddButton
import io.designtoswiftui.countdown2binge.ui.shared.PortraitShowCard
import io.designtoswiftui.countdown2binge.ui.search.components.AiringShowCard
import io.designtoswiftui.countdown2binge.ui.search.components.CategoryChips
import io.designtoswiftui.countdown2binge.ui.search.components.SectionHeader
import io.designtoswiftui.countdown2binge.ui.search.components.TrendingShowCard
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle
import io.designtoswiftui.countdown2binge.ui.theme.Primary
import io.designtoswiftui.countdown2binge.ui.theme.Surface
import io.designtoswiftui.countdown2binge.ui.theme.SurfaceVariant
import io.designtoswiftui.countdown2binge.viewmodels.SearchViewModel

/**
 * Search screen for finding and adding TV shows.
 * Shows landing content (trending, airing) when search is empty.
 */
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onShowClick: (Int) -> Unit = {},  // Now uses tmdbId for navigation
    onCategoryClick: (ShowCategory) -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val error by viewModel.error.collectAsState()
    val followedShows by viewModel.followedShows.collectAsState()
    val addingShows by viewModel.addingShows.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val showIdMap by viewModel.showIdMap.collectAsState()

    // Landing content states
    val trendingShows by viewModel.trendingShows.collectAsState()
    val airingShows by viewModel.airingShows.collectAsState()
    val isTrendingLoading by viewModel.isTrendingLoading.collectAsState()
    val isAiringLoading by viewModel.isAiringLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Show snackbar when message changes
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
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
            // Search header
            SearchHeader(
                query = searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                onSearch = {
                    focusManager.clearFocus()
                    viewModel.search()
                },
                onClear = viewModel::clearSearch,
                isSearching = isSearching
            )

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    isSearching && searchResults.isEmpty() -> {
                        // Loading state
                        LoadingState()
                    }
                    error != null -> {
                        // Error state
                        ErrorState(message = error!!)
                    }
                    searchQuery.isEmpty() -> {
                        // Landing state with trending and airing shows
                        LandingContent(
                            trendingShows = trendingShows,
                            airingShows = airingShows,
                            isTrendingLoading = isTrendingLoading,
                            isAiringLoading = isAiringLoading,
                            followedShows = followedShows,
                            addingShows = addingShows,
                            showIdMap = showIdMap,
                            onCategoryClick = onCategoryClick,
                            onShowClick = onShowClick,
                            onFollowClick = viewModel::toggleFollow
                        )
                    }
                    searchResults.isEmpty() && searchQuery.length >= 2 -> {
                        // No results state
                        NoResultsState(query = searchQuery)
                    }
                    else -> {
                        // Results list
                        SearchResultsList(
                            results = searchResults,
                            followedShows = followedShows,
                            addingShows = addingShows,
                            showIdMap = showIdMap,
                            onShowClick = onShowClick,
                            onAddClick = viewModel::addShowAsync
                        )
                    }
                }

                // Loading overlay when searching with existing results
                if (isSearching && searchResults.isNotEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        color = Primary,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    isSearching: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "SEARCH",
            color = OnBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search shows to binge",
                    color = OnBackgroundSubtle
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = OnBackgroundMuted
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = onClear) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = OnBackgroundMuted
                            )
                        }
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch() }
            ),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceVariant,
                unfocusedContainerColor = Surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Primary,
                focusedTextColor = OnBackground,
                unfocusedTextColor = OnBackground
            )
        )
    }
}

/**
 * Landing content shown when search query is empty.
 */
@Composable
private fun LandingContent(
    trendingShows: List<TrendingShowDisplay>,
    airingShows: List<AiringShowDisplay>,
    isTrendingLoading: Boolean,
    isAiringLoading: Boolean,
    followedShows: Set<Int>,
    addingShows: Set<Int>,
    showIdMap: Map<Int, Long>,
    onCategoryClick: (ShowCategory) -> Unit,
    onShowClick: (Int) -> Unit,  // Now uses tmdbId
    onFollowClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Browse by Category
        item {
            SectionHeader(
                title = "Browse by Category",
                showSeeAll = false,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            CategoryChips(
                onCategoryClick = onCategoryClick
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Trending Shows
        item {
            SectionHeader(
                title = "Trending Shows",
                showSeeAll = false, // Not implemented yet per spec
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            if (isTrendingLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                TrendingShowsGrid(
                    shows = trendingShows.take(4),
                    followedShows = followedShows,
                    addingShows = addingShows,
                    showIdMap = showIdMap,
                    onShowClick = onShowClick,
                    onFollowClick = onFollowClick
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Ending Soon
        item {
            SectionHeader(
                title = "Ending Soon",
                showSeeAll = false, // Not implemented yet per spec
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (isAiringLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
        } else {
            items(
                items = airingShows.take(3),
                key = { it.tmdbId }
            ) { show ->
                val isFollowed = followedShows.contains(show.tmdbId)

                AiringShowCard(
                    show = show,
                    isFollowed = isFollowed,
                    isLoading = addingShows.contains(show.tmdbId),
                    onFollowClick = { onFollowClick(show.tmdbId) },
                    onCardClick = { onShowClick(show.tmdbId) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * 2-column grid for trending shows.
 */
@Composable
private fun TrendingShowsGrid(
    shows: List<TrendingShowDisplay>,
    followedShows: Set<Int>,
    addingShows: Set<Int>,
    showIdMap: Map<Int, Long>,
    onShowClick: (Int) -> Unit,  // Now uses tmdbId
    onFollowClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // First row
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            shows.take(2).forEach { show ->
                val isFollowed = followedShows.contains(show.tmdbId)

                TrendingShowCard(
                    show = show,
                    isFollowed = isFollowed,
                    isLoading = addingShows.contains(show.tmdbId),
                    onFollowClick = { onFollowClick(show.tmdbId) },
                    onCardClick = { onShowClick(show.tmdbId) },
                    modifier = Modifier.weight(1f)
                )
            }
            // Fill empty space if odd number
            if (shows.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Second row
        if (shows.size > 2) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                shows.drop(2).take(2).forEach { show ->
                    val isFollowed = followedShows.contains(show.tmdbId)

                    TrendingShowCard(
                        show = show,
                        isFollowed = isFollowed,
                        isLoading = addingShows.contains(show.tmdbId),
                        onFollowClick = { onFollowClick(show.tmdbId) },
                        onCardClick = { onShowClick(show.tmdbId) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty space if odd number
                if (shows.size == 3) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SearchResultsList(
    results: List<TMDBSearchResult>,
    followedShows: Set<Int>,
    addingShows: Set<Int>,
    showIdMap: Map<Int, Long>,
    onShowClick: (Int) -> Unit,
    onAddClick: (Int) -> Unit
) {
    // Group results into pairs for 2-column grid
    val rows = results.chunked(2)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = rows,
            key = { row -> row.first().id }
        ) { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { result ->
                    val isFollowed = followedShows.contains(result.id)

                    PortraitShowCard(
                        show = result,
                        isFollowed = isFollowed,
                        isLoading = addingShows.contains(result.id),
                        onFollowClick = { onAddClick(result.id) },
                        onCardClick = { onShowClick(result.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty space if odd number of items
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
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
private fun NoResultsState(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No results found",
                color = OnBackgroundMuted,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Try searching for \"$query\" with different spelling",
                color = OnBackgroundSubtle,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
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
        }
    }
}
