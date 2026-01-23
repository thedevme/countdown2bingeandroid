package io.designtoswiftui.countdown2binge.ui.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.ui.theme.AnticipatedAccent
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.EndingSoonAccent
import io.designtoswiftui.countdown2binge.ui.theme.PremieringSoonAccent

/**
 * Countdown display style for the timeline card.
 */
sealed class TimelineCountdownStyle {
    /** Days until premiere (e.g., "16" with "DAYS" label) */
    data class Days(val count: Int) : TimelineCountdownStyle()

    /** Expected year (e.g., "2026" with "EXP." label) */
    data class Year(val year: Int) : TimelineCountdownStyle()

    /** To be determined (shows "TBD" with "DATE" label) */
    object TBD : TimelineCountdownStyle()
}

/**
 * Timeline show card for EXPANDED mode (landscape backdrop).
 *
 * Layout:
 * - Countdown zone (80dp wide, centered)
 * - Backdrop image (fills remaining width, 175dp tall, 24dp corner radius)
 * - Season badge (bottom-right of backdrop)
 *
 * Dimensions:
 * - Total Height: 190dp
 * - Backdrop Height: 175dp
 * - Countdown Zone Width: 80dp
 * - Right Padding: 24dp
 */
@Composable
fun TimelineShowCard(
    countdownStyle: TimelineCountdownStyle,
    backdropUrl: String?,
    seasonNumber: Int,
    sectionStyle: TimelineSectionStyle = TimelineSectionStyle.PREMIERING_SOON,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when (sectionStyle) {
        TimelineSectionStyle.ENDING_SOON -> EndingSoonAccent
        TimelineSectionStyle.PREMIERING_SOON -> PremieringSoonAccent
        TimelineSectionStyle.ANTICIPATED -> AnticipatedAccent
    }

    val totalHeight = 190.dp
    // Calculate gap for timeline connector
    // Gap should break around countdown text with proper padding
    val countdownHeight = when (sectionStyle) {
        TimelineSectionStyle.ANTICIPATED -> 50.dp  // TBD (24sp) + DATE (9sp) + spacing
        else -> 60.dp  // Number (36sp) + label (9sp) + spacing
    }
    val gapPadding = 12.dp  // Padding from text to line ends
    val gapStart = (totalHeight - countdownHeight) / 2 - gapPadding
    val gapEnd = (totalHeight + countdownHeight) / 2 + gapPadding

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(totalHeight)
    ) {
        // Timeline connector line with gap around countdown
        TimelineConnector(
            color = accentColor.copy(alpha = 0.8f),
            gapStart = gapStart,
            gapEnd = gapEnd
        )

        // Card content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Countdown zone (80dp wide, centered)
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(175.dp),
                contentAlignment = Alignment.Center
            ) {
                CountdownDisplay(
                    style = countdownStyle,
                    accentColor = accentColor
                )
            }

            // Backdrop image zone
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(175.dp)
                    .padding(end = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardBackground)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                // Backdrop image
                if (backdropUrl != null) {
                    AsyncImage(
                        model = backdropUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Season badge (bottom-right)
                Text(
                    text = "S$seasonNumber",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 12.dp)
                )
            }
        }
    }
}

/**
 * Countdown display for the timeline card.
 */
@Composable
private fun CountdownDisplay(
    style: TimelineCountdownStyle,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (style) {
            is TimelineCountdownStyle.Days -> {
                Text(
                    text = String.format("%02d", style.count),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    lineHeight = 36.sp
                )
                Text(
                    text = "DAYS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            is TimelineCountdownStyle.Year -> {
                Text(
                    text = style.year.toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    lineHeight = 36.sp
                )
                Text(
                    text = "EXP.",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor.copy(alpha = 0.7f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            is TimelineCountdownStyle.TBD -> {
                Text(
                    text = "TBD",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    lineHeight = 24.sp
                )
                Text(
                    text = "DATE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor.copy(alpha = 0.7f),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun TimelineShowCardDaysPreview() {
    Countdown2BingeTheme {
        TimelineShowCard(
            countdownStyle = TimelineCountdownStyle.Days(16),
            backdropUrl = null,
            seasonNumber = 3,
            sectionStyle = TimelineSectionStyle.PREMIERING_SOON,
            onClick = {},
            modifier = Modifier.background(Background)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun TimelineShowCardYearPreview() {
    Countdown2BingeTheme {
        TimelineShowCard(
            countdownStyle = TimelineCountdownStyle.Year(2026),
            backdropUrl = null,
            seasonNumber = 2,
            sectionStyle = TimelineSectionStyle.ANTICIPATED,
            onClick = {},
            modifier = Modifier.background(Background)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun TimelineShowCardTBDPreview() {
    Countdown2BingeTheme {
        TimelineShowCard(
            countdownStyle = TimelineCountdownStyle.TBD,
            backdropUrl = null,
            seasonNumber = 1,
            sectionStyle = TimelineSectionStyle.ANTICIPATED,
            onClick = {},
            modifier = Modifier.background(Background)
        )
    }
}
