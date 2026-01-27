package io.designtoswiftui.countdown2binge.ui.onboarding.steps

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.onboarding.AddShowsTab
import io.designtoswiftui.countdown2binge.ui.onboarding.OnboardingUiState
import io.designtoswiftui.countdown2binge.ui.onboarding.ShowSummary
import io.designtoswiftui.countdown2binge.ui.onboarding.components.StepIndicator
import io.designtoswiftui.countdown2binge.ui.search.components.TrendingShowCard
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle
import io.designtoswiftui.countdown2binge.ui.theme.SurfaceVariant

/**
 * Add Shows screen (Step 4).
 * Allows users to select shows from trending or search.
 */
@Composable
fun AddShowsScreen(
    selectedShows: Map<Int, ShowSummary>,
    recommendedShows: List<ShowSummary>,
    searchResults: List<ShowSummary>,
    searchQuery: String,
    selectedTab: AddShowsTab,
    isLoadingRecommended: Boolean,
    isSearching: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onTabSelect: (AddShowsTab) -> Unit,
    onShowToggle: (ShowSummary) -> Unit,
    onShowClick: (Int) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Load recommended shows on first display
    LaunchedEffect(Unit) {
        // This is handled by the ViewModel when step changes
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Header
        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            // Step indicator with bookmark badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepIndicator(
                    currentStep = 4,
                    totalSteps = OnboardingUiState.TOTAL_STEPS
                )

                // Bookmark badge with count
                BookmarkBadge(count = selectedShows.size)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Add your shows",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Select at least 3 shows to personalize your binge countdowns.",
                fontSize = 15.sp,
                color = OnBackgroundMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search field
            SearchField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pill-shaped tab buttons
            PillTabRow(
                selectedTab = selectedTab,
                onTabSelect = onTabSelect,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                AddShowsTab.RECOMMENDED -> {
                    if (isLoadingRecommended) {
                        LoadingIndicator()
                    } else {
                        ShowGrid(
                            shows = recommendedShows,
                            selectedShows = selectedShows,
                            onShowToggle = onShowToggle,
                            onShowClick = onShowClick
                        )
                    }
                }
                AddShowsTab.SEARCH -> {
                    if (isSearching) {
                        LoadingIndicator()
                    } else if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                        EmptySearchState()
                    } else if (searchResults.isEmpty()) {
                        SearchPromptState()
                    } else {
                        ShowGrid(
                            shows = searchResults,
                            selectedShows = selectedShows,
                            onShowToggle = onShowToggle,
                            onShowClick = onShowClick
                        )
                    }
                }
            }
        }

        // Bottom bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Background)
                .padding(20.dp)
        ) {
            // Selection count
            Text(
                text = "${selectedShows.size} show${if (selectedShows.size != 1) "s" else ""} followed",
                fontSize = 14.sp,
                color = OnBackgroundMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Continue button
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DetailAccent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Bookmark icon in a circle with count badge overlay.
 */
@Composable
private fun BookmarkBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Circle background with bookmark icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = "Selected shows",
                tint = OnBackground,
                modifier = Modifier.size(20.dp)
            )
        }

        // Count badge (top-right)
        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(DetailAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

/**
 * Pill-shaped tab buttons for Recommended / Search Results.
 */
@Composable
private fun PillTabRow(
    selectedTab: AddShowsTab,
    onTabSelect: (AddShowsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PillTab(
            text = "Recommended",
            isSelected = selectedTab == AddShowsTab.RECOMMENDED,
            onClick = { onTabSelect(AddShowsTab.RECOMMENDED) }
        )
        PillTab(
            text = "Search Results",
            isSelected = selectedTab == AddShowsTab.SEARCH,
            onClick = { onTabSelect(AddShowsTab.SEARCH) }
        )
    }
}

@Composable
private fun PillTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) DetailAccent.copy(alpha = 0.15f) else Color.Transparent
    val textColor = if (isSelected) DetailAccent else OnBackgroundMuted

    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = OnBackground
        ),
        cursorBrush = SolidColor(DetailAccent),
        singleLine = true,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = OnBackgroundSubtle,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search for TV series",
                            fontSize = 16.sp,
                            color = OnBackgroundSubtle
                        )
                    }
                    innerTextField()
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun ShowGrid(
    shows: List<ShowSummary>,
    selectedShows: Map<Int, ShowSummary>,
    onShowToggle: (ShowSummary) -> Unit,
    onShowClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(shows, key = { it.tmdbId }) { show ->
            val isSelected = selectedShows.containsKey(show.tmdbId)

            // Wrap TrendingShowCard with selection checkmark overlay
            Box {
                TrendingShowCard(
                    show = show.toTrendingShowDisplay(),
                    isFollowed = isSelected,
                    isLoading = false,
                    onFollowClick = { onShowToggle(show) },
                    onCardClick = { onShowClick(show.tmdbId) }
                )

                // Selection checkmark overlay (top-right of poster area)
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DetailAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = DetailAccent,
            strokeWidth = 2.dp
        )
    }
}

@Composable
private fun EmptySearchState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No shows found",
            fontSize = 16.sp,
            color = OnBackgroundSubtle
        )
    }
}

@Composable
private fun SearchPromptState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = OnBackgroundSubtle,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Search for your favorite shows",
                fontSize = 16.sp,
                color = OnBackgroundSubtle
            )
        }
    }
}
