package io.designtoswiftui.countdown2binge.ui.timeline.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.ui.theme.AppTypography
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle
import io.designtoswiftui.countdown2binge.ui.theme.TimelineAccent

/**
 * Represents the type of countdown to display on the timeline card.
 */
sealed class TimelineCountdownType {
    data class Days(val count: Int) : TimelineCountdownType()
    data class Year(val year: Int) : TimelineCountdownType()
    object TBD : TimelineCountdownType()
}

/**
 * Timeline card showing countdown on left, dashed vertical line, and show image on right.
 */
@Composable
fun TimelineCard(
    countdownType: TimelineCountdownType,
    backdropUrl: String?,
    seasonNumber: Int,
    showTimeline: Boolean = true,
    isFirstInSection: Boolean = false,
    isLastInSection: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Countdown + Dashed Timeline line
        Box(
            modifier = Modifier
                .width(72.dp)
                .fillMaxHeight()
        ) {
            // Dashed timeline vertical line
            if (showTimeline) {
                Canvas(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                ) {
                    val dashLength = 8.dp.toPx()
                    val gapLength = 6.dp.toPx()
                    val pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(dashLength, gapLength),
                        0f
                    )

                    val startY = if (isFirstInSection) size.height / 2 else 0f
                    val endY = if (isLastInSection) size.height / 2 else size.height

                    drawLine(
                        color = TimelineAccent.copy(alpha = 0.6f),
                        start = Offset(size.width / 2, startY),
                        end = Offset(size.width / 2, endY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = pathEffect
                    )
                }

                // Timeline dot
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(8.dp)
                        .align(Alignment.CenterStart)
                        .background(TimelineAccent, RoundedCornerShape(4.dp))
                )
            }

            // Countdown text
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(start = 12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                when (countdownType) {
                    is TimelineCountdownType.Days -> {
                        Text(
                            text = String.format("%02d", countdownType.count),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = OnBackground
                        )
                        Text(
                            text = "DAYS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TimelineAccent,
                            letterSpacing = 1.sp
                        )
                    }
                    is TimelineCountdownType.Year -> {
                        Text(
                            text = countdownType.year.toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = OnBackgroundMuted
                        )
                        Text(
                            text = "EXP.",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackgroundSubtle,
                            letterSpacing = 1.sp
                        )
                    }
                    is TimelineCountdownType.TBD -> {
                        Text(
                            text = "TBD",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = OnBackgroundSubtle
                        )
                        Text(
                            text = "DATE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackgroundSubtle.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Right side: Show image with season badge
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .clickable(onClick = onClick)
        ) {
            // Backdrop image
            if (backdropUrl != null) {
                AsyncImage(
                    model = backdropUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay for text visibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f)
                                )
                            )
                        )
                )
            }

            // Season badge (bottom right)
            Text(
                text = "S$seasonNumber",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun TimelineCardDaysPreview() {
    Countdown2BingeTheme {
        Column(modifier = Modifier.background(Background)) {
            TimelineCard(
                countdownType = TimelineCountdownType.Days(9),
                backdropUrl = null,
                seasonNumber = 4,
                isFirstInSection = true,
                onClick = {}
            )
            TimelineCard(
                countdownType = TimelineCountdownType.Days(15),
                backdropUrl = null,
                seasonNumber = 2,
                onClick = {}
            )
            TimelineCard(
                countdownType = TimelineCountdownType.Days(42),
                backdropUrl = null,
                seasonNumber = 1,
                isLastInSection = true,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun TimelineCardTBDPreview() {
    Countdown2BingeTheme {
        Column(modifier = Modifier.background(Background)) {
            TimelineCard(
                countdownType = TimelineCountdownType.TBD,
                backdropUrl = null,
                seasonNumber = 2,
                isFirstInSection = true,
                onClick = {}
            )
            TimelineCard(
                countdownType = TimelineCountdownType.TBD,
                backdropUrl = null,
                seasonNumber = 1,
                onClick = {}
            )
            TimelineCard(
                countdownType = TimelineCountdownType.TBD,
                backdropUrl = null,
                seasonNumber = 4,
                isLastInSection = true,
                onClick = {}
            )
        }
    }
}
