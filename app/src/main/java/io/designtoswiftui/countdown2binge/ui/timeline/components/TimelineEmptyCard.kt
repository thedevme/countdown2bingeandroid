package io.designtoswiftui.countdown2binge.ui.timeline.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.AnticipatedAccent
import io.designtoswiftui.countdown2binge.ui.theme.EndingSoonAccent
import io.designtoswiftui.countdown2binge.ui.theme.PremieringSoonAccent

/**
 * Draws a dashed timeline connector line with a gap around the countdown area.
 * The line runs vertically at x=40dp (centered in the 80dp countdown zone).
 *
 * @param color Line color
 * @param gapStart Y position where the gap starts (top of countdown area)
 * @param gapEnd Y position where the gap ends (bottom of countdown area)
 * @param extendBottom Extra distance to extend the bottom line (to cover spacers between cards)
 * @param showTopLine Whether to show the line segment above the gap (false for first section)
 */
@Composable
fun TimelineConnector(
    color: Color,
    gapStart: Dp,
    gapEnd: Dp,
    extendBottom: Dp = 30.dp,
    showTopLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .width(80.dp)
            .fillMaxHeight()
    ) {
        val dashLength = 4.dp.toPx()
        val gapLength = 4.dp.toPx()
        val pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashLength, gapLength),
            0f
        )
        val xPosition = 40.dp.toPx()
        val strokeWidth = 2.dp.toPx()
        val gapStartPx = gapStart.toPx()
        val gapEndPx = gapEnd.toPx()
        val extendBottomPx = extendBottom.toPx()

        // Line from top to gap start (optional - hidden for first section)
        if (showTopLine && gapStartPx > 0) {
            drawLine(
                color = color,
                start = Offset(xPosition, 0f),
                end = Offset(xPosition, gapStartPx),
                strokeWidth = strokeWidth,
                pathEffect = pathEffect
            )
        }

        // Line from gap end to bottom (extended to cover spacer to next card)
        if (gapEndPx < size.height) {
            drawLine(
                color = color,
                start = Offset(xPosition, gapEndPx),
                end = Offset(xPosition, size.height + extendBottomPx),
                strokeWidth = strokeWidth,
                pathEffect = pathEffect
            )
        }
    }
}

/**
 * Empty placeholder card for timeline sections.
 * Shows countdown placeholder and "EMPTY" in a dashed border card.
 *
 * @param isExpanded If true, shows landscape card (fills width, 175dp height).
 *                   If false, shows portrait card (230dp x 310dp, right-aligned).
 * @param sectionStyle Section style for accent color
 */
@Composable
fun TimelineEmptyCard(
    isExpanded: Boolean = false,
    sectionStyle: TimelineSectionStyle = TimelineSectionStyle.PREMIERING_SOON,
    modifier: Modifier = Modifier
) {
    val accentColor = when (sectionStyle) {
        TimelineSectionStyle.ENDING_SOON -> EndingSoonAccent.copy(alpha = 0.4f)
        TimelineSectionStyle.PREMIERING_SOON -> PremieringSoonAccent.copy(alpha = 0.4f)
        TimelineSectionStyle.ANTICIPATED -> AnticipatedAccent
    }

    val borderColor = accentColor.copy(alpha = 0.5f)

    val (countdownText, countdownLabel) = when (sectionStyle) {
        TimelineSectionStyle.ENDING_SOON -> "--" to "DAYS"
        TimelineSectionStyle.PREMIERING_SOON -> "--" to "DAYS"
        TimelineSectionStyle.ANTICIPATED -> "TBD" to "DATE"
    }

    val countdownTextColor = when (sectionStyle) {
        TimelineSectionStyle.ENDING_SOON -> Color.White
        TimelineSectionStyle.PREMIERING_SOON -> Color.White
        TimelineSectionStyle.ANTICIPATED -> accentColor
    }

    val countdownLabelColor = when (sectionStyle) {
        TimelineSectionStyle.ENDING_SOON -> Color.White.copy(alpha = 0.7f)
        TimelineSectionStyle.PREMIERING_SOON -> Color.White.copy(alpha = 0.7f)
        TimelineSectionStyle.ANTICIPATED -> accentColor.copy(alpha = 0.7f)
    }

    // Landscape (expanded): fills width, 175dp height, 24dp corner radius
    // Portrait (collapsed): 230dp x 310dp fixed, 15dp corner radius
    val totalHeight = if (isExpanded) 190.dp else 320.dp
    val cardHeight = if (isExpanded) 175.dp else 310.dp
    val cardWidth = if (isExpanded) null else 230.dp // null means fill
    val cornerRadius = if (isExpanded) 24.dp else 15.dp
    val emptyText = if (isExpanded) "EMPTY SLOTS" else "EMPTY"

    // Calculate gap for timeline connector
    // Adjust gap to have consistent visual padding from text to line
    val countdownHeight = when (sectionStyle) {
        TimelineSectionStyle.ANTICIPATED -> 40.dp  // Smaller for TBD text
        else -> 55.dp  // Larger for "--" text (36sp + 9sp label)
    }
    val gapPadding = 8.dp  // Increased padding for better visual balance
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
                .padding(end = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.SpaceBetween
        ) {
            // Countdown zone (80dp)
            Box(
                modifier = Modifier
                    .width(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    // Countdown text ("--" or "TBD")
                    Text(
                        text = countdownText,
                        fontSize = if (countdownText == "TBD") 24.sp else 36.sp,
                        fontWeight = FontWeight.Black,
                        color = countdownTextColor
                    )
                    // Label
                    Text(
                        text = countdownLabel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = countdownLabelColor,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Spacer for portrait layout to push card to the right
            if (!isExpanded) {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Border card with "EMPTY" text (dashed for Premiering, solid for Anticipated)
            // Only Premiering Soon uses dashed border; Ending Soon and Anticipated use solid
            val useDashedBorder = sectionStyle == TimelineSectionStyle.PREMIERING_SOON

            Box(
                modifier = Modifier
                    .then(
                        if (cardWidth != null) {
                            Modifier.width(cardWidth)
                        } else {
                            Modifier.weight(1f)
                        }
                    )
                    .height(cardHeight)
                    .clip(RoundedCornerShape(cornerRadius)),
                contentAlignment = Alignment.Center
            ) {
                // Border (dashed for Premiering Soon, solid for Anticipated)
                Canvas(modifier = Modifier.matchParentSize()) {
                    val borderWidth = 1.dp.toPx()
                    val cornerRadiusPx = cornerRadius.toPx()

                    val pathEffect = if (useDashedBorder) {
                        val dashLength = 6.dp.toPx()
                        val gapLength = 4.dp.toPx()
                        PathEffect.dashPathEffect(
                            floatArrayOf(dashLength, gapLength),
                            0f
                        )
                    } else {
                        null // Solid line
                    }

                    drawRoundRect(
                        color = borderColor,
                        topLeft = Offset(borderWidth / 2, borderWidth / 2),
                        size = Size(size.width - borderWidth, size.height - borderWidth),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                        style = Stroke(width = borderWidth, pathEffect = pathEffect)
                    )
                }

                // "EMPTY" or "EMPTY SLOTS" text
                Text(
                    text = emptyText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF595959),
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}
