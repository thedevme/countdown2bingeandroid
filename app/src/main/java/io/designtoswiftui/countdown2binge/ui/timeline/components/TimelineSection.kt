package io.designtoswiftui.countdown2binge.ui.timeline.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import io.designtoswiftui.countdown2binge.ui.theme.AnticipatedAccent
import io.designtoswiftui.countdown2binge.ui.theme.PremieringSoonAccent

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
 * - Dashed connector line (80pt zone, centered at x=40)
 * - Expanded mode: landscape backdrop cards (TimelineShowCard)
 * - Collapsed mode: portrait poster cards (CompactPosterCard)
 * - Max 3 cards displayed
 *
 * @param title Section title
 * @param entries List of timeline entries
 * @param style Section style (Premiering Soon or Anticipated)
 * @param isExpanded Whether the section is expanded
 * @param onToggle Called when header is tapped
 * @param onEntryClick Called when a card is tapped
 */
@Composable
fun TimelineSection(
    title: String,
    entries: List<TimelineEntry>,
    style: TimelineSectionStyle,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onEntryClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxCardsPerSection = 3
    val displayEntries = entries.take(maxCardsPerSection)

    val accentColor = when (style) {
        TimelineSectionStyle.PREMIERING_SOON -> PremieringSoonAccent
        TimelineSectionStyle.ANTICIPATED -> AnticipatedAccent
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Section header
        TimelineSectionHeader(
            title = title,
            count = entries.size, // Show total count, not just displayed
            style = style,
            isExpanded = isExpanded,
            onToggle = onToggle
        )

        // Cards with dashed connector line
        Box {
            // Dashed connector line
            if (displayEntries.isNotEmpty()) {
                Canvas(
                    modifier = Modifier
                        .width(80.dp)
                        .matchParentSize()
                ) {
                    val dashLength = 4.dp.toPx()
                    val gapLength = 4.dp.toPx()
                    val pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(dashLength, gapLength),
                        0f
                    )

                    // Line starts below header badge (48dp) and extends to bottom
                    val startY = 0f
                    val endY = size.height
                    val xPosition = 40.dp.toPx() // Centered in 80dp zone

                    drawLine(
                        color = accentColor.copy(alpha = 0.8f),
                        start = Offset(xPosition, startY),
                        end = Offset(xPosition, endY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = pathEffect
                    )
                }
            }

            // Cards content
            Column {
                // Expanded mode: landscape backdrop cards
                AnimatedVisibility(
                    visible = isExpanded,
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
                            if (index < displayEntries.lastIndex) {
                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }
                    }
                }

                // Collapsed mode: portrait poster cards
                AnimatedVisibility(
                    visible = !isExpanded,
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
                            if (index < displayEntries.lastIndex) {
                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
