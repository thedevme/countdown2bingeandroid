package io.designtoswiftui.countdown2binge.ui.timeline.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Data class for a timeline entry (show with upcoming season).
 */
data class TimelineEntry(
    val showId: Long,
    val seasonNumber: Int,
    val backdropUrl: String?,
    val posterUrl: String?,
    val countdownStyle: TimelineCountdownStyle
)

/**
 * Timeline section with expandable/collapsible cards.
 *
 * Features:
 * - Section header with rotating chevron
 * - Dashed connector line with gaps around countdown areas
 * - Expanded mode (default): portrait poster cards (CompactPosterCard)
 * - Collapsed mode: landscape backdrop cards (TimelineShowCard)
 * - Max 3 cards displayed
 * - Empty cards shown when showEmptyCards is true
 *
 * @param title Section title
 * @param entries List of timeline entries
 * @param style Section style (Premiering Soon or Anticipated)
 * @param isExpanded Whether the section is expanded
 * @param onToggle Called when header is tapped
 * @param onEntryClick Called when a card is tapped
 * @param showEmptyCards Whether to show empty placeholder cards
 */
@Composable
fun TimelineSection(
    title: String,
    entries: List<TimelineEntry>,
    style: TimelineSectionStyle,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onEntryClick: (Long) -> Unit,
    showEmptyCards: Boolean = false,
    isFirstSection: Boolean = false,
    modifier: Modifier = Modifier
) {
    val maxCardsPerSection = 3
    val displayEntries = entries.take(maxCardsPerSection)
    val emptyCardsCount = if (showEmptyCards) maxCardsPerSection - displayEntries.size else 0

    Column(modifier = modifier.fillMaxWidth()) {
        // Section header
        TimelineSectionHeader(
            title = title,
            count = entries.size, // Show total count, not just displayed
            style = style,
            isExpanded = isExpanded,
            onToggle = onToggle,
            isFirstSection = isFirstSection
        )

        // Cards content (each card draws its own connector line with gaps)
        // Expanded mode (default): portrait poster cards
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(300)) + expandVertically(tween(300)),
            exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
        ) {
            Column {
                displayEntries.forEachIndexed { index, entry ->
                    CompactPosterCard(
                        countdownStyle = entry.countdownStyle,
                        posterUrl = entry.posterUrl,
                        seasonNumber = entry.seasonNumber,
                        sectionStyle = style,
                        onClick = { onEntryClick(entry.showId) }
                    )
                    if (index < displayEntries.lastIndex || emptyCardsCount > 0) {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
                // Empty cards for expanded mode (portrait/taller)
                repeat(emptyCardsCount) { index ->
                    TimelineEmptyCard(
                        isExpanded = false,
                        sectionStyle = style
                    )
                    if (index < emptyCardsCount - 1) {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }

        // Collapsed mode: landscape backdrop cards
        AnimatedVisibility(
            visible = !isExpanded,
            enter = fadeIn(tween(300)) + expandVertically(tween(300)),
            exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
        ) {
            Column {
                displayEntries.forEachIndexed { index, entry ->
                    TimelineShowCard(
                        countdownStyle = entry.countdownStyle,
                        backdropUrl = entry.backdropUrl,
                        seasonNumber = entry.seasonNumber,
                        sectionStyle = style,
                        onClick = { onEntryClick(entry.showId) }
                    )
                    if (index < displayEntries.lastIndex || emptyCardsCount > 0) {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
                // Empty cards for collapsed mode (landscape/shorter)
                repeat(emptyCardsCount) { index ->
                    TimelineEmptyCard(
                        isExpanded = true,
                        sectionStyle = style
                    )
                    if (index < emptyCardsCount - 1) {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}
