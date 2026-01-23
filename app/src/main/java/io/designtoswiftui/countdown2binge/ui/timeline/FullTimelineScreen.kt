package io.designtoswiftui.countdown2binge.ui.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.timeline.components.CompactPosterCard
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineCountdownStyle
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineEmptyCard
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineEntry
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineSectionHeader
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineSectionStyle
import io.designtoswiftui.countdown2binge.ui.timeline.components.TimelineShowCard
import io.designtoswiftui.countdown2binge.viewmodels.TimelineViewModel

/**
 * Full Timeline screen showing all shows currently in production.
 * Displays Ending Soon, Premiering Soon, and Anticipated sections with cards.
 * Unlike the main timeline, this screen is not limited to 3 cards per section.
 *
 * Features:
 * - Toggle between expanded (portrait poster) and compact (landscape backdrop) views
 * - Nav bar with back button and layout toggle icon
 *
 * Rules:
 * - At launch (no shows): All 3 sections with 3 empty cards each
 * - Once user has shows: Only show sections that have content (no empty filler)
 */
@Composable
fun FullTimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onShowClick: (Long) -> Unit = {}
) {
    val airingShows by viewModel.airingShows.collectAsState()
    val premieringShows by viewModel.premieringShows.collectAsState()
    val anticipatedShows by viewModel.anticipatedShows.collectAsState()

    // Expanded = portrait poster cards, Compact = landscape backdrop cards
    var isExpanded by remember { mutableStateOf(true) }

    val defaultEmptySlots = 3

    // Determine if this is onboarding state (no shows at all)
    val isOnboarding = airingShows.isEmpty() && premieringShows.isEmpty() && anticipatedShows.isEmpty()

    // Convert shows to TimelineEntry format
    val endingSoonEntries = remember(airingShows) {
        airingShows.map { show ->
            TimelineEntry(
                showId = show.show.id,
                seasonNumber = show.season?.seasonNumber ?: 1,
                backdropUrl = show.show.backdropPath?.let {
                    "${TMDBService.IMAGE_BASE_URL}${TMDBService.BACKDROP_SIZE}$it"
                },
                posterUrl = show.show.posterPath?.let {
                    "${TMDBService.IMAGE_BASE_URL}${TMDBService.POSTER_SIZE}$it"
                },
                countdownStyle = show.daysUntilFinale?.let {
                    TimelineCountdownStyle.Days(it)
                } ?: TimelineCountdownStyle.TBD
            )
        }
    }

    val premieringEntries = remember(premieringShows) {
        premieringShows.map { show ->
            TimelineEntry(
                showId = show.show.id,
                seasonNumber = show.season?.seasonNumber ?: 1,
                backdropUrl = show.show.backdropPath?.let {
                    "${TMDBService.IMAGE_BASE_URL}${TMDBService.BACKDROP_SIZE}$it"
                },
                posterUrl = show.show.posterPath?.let {
                    "${TMDBService.IMAGE_BASE_URL}${TMDBService.POSTER_SIZE}$it"
                },
                countdownStyle = show.daysUntilPremiere?.let {
                    TimelineCountdownStyle.Days(it)
                } ?: TimelineCountdownStyle.TBD
            )
        }
    }

    val anticipatedEntries = remember(anticipatedShows) {
        val currentYear = java.time.LocalDate.now().year
        anticipatedShows.map { show ->
            TimelineEntry(
                showId = show.show.id,
                seasonNumber = show.anticipatedSeasonNumber,
                backdropUrl = show.show.backdropPath?.let {
                    "${TMDBService.IMAGE_BASE_URL}${TMDBService.BACKDROP_SIZE}$it"
                },
                posterUrl = show.show.posterPath?.let {
                    "${TMDBService.IMAGE_BASE_URL}${TMDBService.POSTER_SIZE}$it"
                },
                // Only show year if it's current year or later (can't anticipate the past)
                countdownStyle = show.season?.premiereDate?.let { date ->
                    if (date.year >= currentYear) {
                        TimelineCountdownStyle.Year(date.year)
                    } else {
                        TimelineCountdownStyle.TBD
                    }
                } ?: TimelineCountdownStyle.TBD
            )
        }
    }

    // Section visibility: show section if it has content OR if onboarding
    val showEndingSoon = endingSoonEntries.isNotEmpty() || isOnboarding
    val showPremiering = premieringEntries.isNotEmpty() || isOnboarding
    val showAnticipated = anticipatedEntries.isNotEmpty() || isOnboarding

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp) // Space for nav bar
        ) {
        // ENDING SOON SECTION
        if (showEndingSoon) {
            item {
                TimelineSectionHeader(
                    title = "Ending Soon",
                    count = airingShows.size,
                    style = TimelineSectionStyle.ENDING_SOON,
                    isExpanded = true,
                    onToggle = {},
                    isFirstSection = true,
                    hideChevron = true
                )
            }

            // Ending Soon cards (no limit)
            endingSoonEntries.forEachIndexed { index, entry ->
                item {
                    if (isExpanded) {
                        CompactPosterCard(
                            countdownStyle = entry.countdownStyle,
                            posterUrl = entry.posterUrl,
                            seasonNumber = entry.seasonNumber,
                            sectionStyle = TimelineSectionStyle.ENDING_SOON,
                            onClick = { onShowClick(entry.showId) }
                        )
                    } else {
                        TimelineShowCard(
                            countdownStyle = entry.countdownStyle,
                            backdropUrl = entry.backdropUrl,
                            seasonNumber = entry.seasonNumber,
                            sectionStyle = TimelineSectionStyle.ENDING_SOON,
                            onClick = { onShowClick(entry.showId) }
                        )
                    }
                    if (index < endingSoonEntries.lastIndex || (isOnboarding && endingSoonEntries.isEmpty())) {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }

            // Empty slots for Ending Soon (only during onboarding)
            if (isOnboarding) {
                repeat(defaultEmptySlots) { index ->
                    item {
                        TimelineEmptyCard(
                            isExpanded = !isExpanded, // Inverted: TimelineEmptyCard.isExpanded means landscape mode
                            sectionStyle = TimelineSectionStyle.ENDING_SOON
                        )
                        if (index < defaultEmptySlots - 1) {
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }

            // Spacing between sections
            if (showPremiering || showAnticipated) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // PREMIERING SOON SECTION
        if (showPremiering) {
            item {
                TimelineSectionHeader(
                    title = "Premiering Soon",
                    count = premieringShows.size,
                    style = TimelineSectionStyle.PREMIERING_SOON,
                    isExpanded = true,
                    onToggle = {},
                    hideChevron = true
                )
            }

            // Premiering Soon cards (no limit)
            premieringEntries.forEachIndexed { index, entry ->
                item {
                    if (isExpanded) {
                        CompactPosterCard(
                            countdownStyle = entry.countdownStyle,
                            posterUrl = entry.posterUrl,
                            seasonNumber = entry.seasonNumber,
                            sectionStyle = TimelineSectionStyle.PREMIERING_SOON,
                            onClick = { onShowClick(entry.showId) }
                        )
                    } else {
                        TimelineShowCard(
                            countdownStyle = entry.countdownStyle,
                            backdropUrl = entry.backdropUrl,
                            seasonNumber = entry.seasonNumber,
                            sectionStyle = TimelineSectionStyle.PREMIERING_SOON,
                            onClick = { onShowClick(entry.showId) }
                        )
                    }
                    if (index < premieringEntries.lastIndex || (isOnboarding && premieringEntries.isEmpty())) {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }

            // Empty slots for Premiering Soon (only during onboarding)
            if (isOnboarding) {
                repeat(defaultEmptySlots) { index ->
                    item {
                        TimelineEmptyCard(
                            isExpanded = !isExpanded, // Inverted: TimelineEmptyCard.isExpanded means landscape mode
                            sectionStyle = TimelineSectionStyle.PREMIERING_SOON
                        )
                        if (index < defaultEmptySlots - 1) {
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }

            // Spacing between sections
            if (showAnticipated) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // ANTICIPATED SECTION
        if (showAnticipated) {
            item {
                TimelineSectionHeader(
                    title = "Anticipated",
                    count = anticipatedShows.size,
                    style = TimelineSectionStyle.ANTICIPATED,
                    isExpanded = true,
                    onToggle = {},
                    hideChevron = true
                )
            }

            // Anticipated cards (no limit)
            anticipatedEntries.forEachIndexed { index, entry ->
                item {
                    if (isExpanded) {
                        CompactPosterCard(
                            countdownStyle = entry.countdownStyle,
                            posterUrl = entry.posterUrl,
                            seasonNumber = entry.seasonNumber,
                            sectionStyle = TimelineSectionStyle.ANTICIPATED,
                            onClick = { onShowClick(entry.showId) }
                        )
                    } else {
                        TimelineShowCard(
                            countdownStyle = entry.countdownStyle,
                            backdropUrl = entry.backdropUrl,
                            seasonNumber = entry.seasonNumber,
                            sectionStyle = TimelineSectionStyle.ANTICIPATED,
                            onClick = { onShowClick(entry.showId) }
                        )
                    }
                    if (index < anticipatedEntries.lastIndex || (isOnboarding && anticipatedEntries.isEmpty())) {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }

            // Empty slots for Anticipated (only during onboarding)
            if (isOnboarding) {
                repeat(defaultEmptySlots) { index ->
                    item {
                        TimelineEmptyCard(
                            isExpanded = !isExpanded, // Inverted: TimelineEmptyCard.isExpanded means landscape mode
                            sectionStyle = TimelineSectionStyle.ANTICIPATED
                        )
                        if (index < defaultEmptySlots - 1) {
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }
        }

            // Bottom spacing for nav bar
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Navigation bar
        FullTimelineNavBar(
            isExpanded = isExpanded,
            onBackClick = onBackClick,
            onToggleLayout = { isExpanded = !isExpanded },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

/**
 * Navigation bar for the full timeline screen with back button and layout toggle.
 */
@Composable
private fun FullTimelineNavBar(
    isExpanded: Boolean,
    onBackClick: () -> Unit,
    onToggleLayout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Background)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = OnBackground,
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackClick
                )
        )

        // Title
        Text(
            text = "FULL TIMELINE",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.5f),
            letterSpacing = 1.5.sp
        )

        // Layout toggle icon
        LayoutToggleIcon(
            isExpanded = isExpanded,
            onClick = onToggleLayout,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Layout toggle icon showing expanded (single rectangle) or compact (two rectangles) state.
 * iOS icons: rectangle.ratio.3.to.4 (expanded) / rectangle.grid.1x2 (compact)
 */
@Composable
private fun LayoutToggleIcon(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        val iconColor = Color.White.copy(alpha = 0.7f)
        val cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())

        if (isExpanded) {
            // Single tall rectangle (expanded state - click to compact)
            val rectWidth = size.width * 0.6f
            val rectHeight = size.height * 0.85f
            val offsetX = (size.width - rectWidth) / 2
            val offsetY = (size.height - rectHeight) / 2
            drawRoundRect(
                color = iconColor,
                topLeft = Offset(offsetX, offsetY),
                size = Size(rectWidth, rectHeight),
                cornerRadius = cornerRadius
            )
        } else {
            // Two stacked rectangles (compact state - click to expand)
            val rectWidth = size.width * 0.7f
            val rectHeight = size.height * 0.38f
            val gap = size.height * 0.1f
            val offsetX = (size.width - rectWidth) / 2

            // Top rectangle
            drawRoundRect(
                color = iconColor,
                topLeft = Offset(offsetX, size.height * 0.08f),
                size = Size(rectWidth, rectHeight),
                cornerRadius = cornerRadius
            )
            // Bottom rectangle
            drawRoundRect(
                color = iconColor,
                topLeft = Offset(offsetX, size.height * 0.08f + rectHeight + gap),
                size = Size(rectWidth, rectHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}
