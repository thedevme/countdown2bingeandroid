package io.designtoswiftui.countdown2binge.ui.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.designtoswiftui.countdown2binge.ui.theme.AppTypography
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle
import io.designtoswiftui.countdown2binge.ui.theme.TimelineLine

/**
 * Empty placeholder card for timeline sections with no shows.
 */
@Composable
fun TimelineEmptyCard(
    showTimeline: Boolean = true,
    isFirstInSection: Boolean = false,
    isLastInSection: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(start = 20.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Countdown placeholder + Timeline line
        Box(
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight()
        ) {
            // Timeline vertical line (dashed effect with segments)
            if (showTimeline) {
                Column(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .padding(start = 4.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(6) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(12.dp)
                                .background(
                                    color = if (isFirstInSection && it == 0) {
                                        Color.Transparent
                                    } else if (isLastInSection && it == 5) {
                                        Color.Transparent
                                    } else {
                                        TimelineLine.copy(alpha = 0.3f)
                                    }
                                )
                        )
                    }
                }
            }

            // Countdown placeholder (dashed)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(start = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "—",
                    style = AppTypography.countdownLarge,
                    color = OnBackgroundSubtle.copy(alpha = 0.5f)
                )
                Text(
                    text = "DAYS",
                    style = AppTypography.countdownLabel,
                    color = OnBackgroundSubtle.copy(alpha = 0.5f)
                )
            }
        }

        // Right side: Empty slot placeholder
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = 8.dp, horizontal = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 1.dp,
                    color = OnBackgroundSubtle.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(CardBackground.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "EMPTY SLOT",
                style = AppTypography.sectionHeader,
                color = OnBackgroundSubtle.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun TimelineEmptyCardPreview() {
    Countdown2BingeTheme {
        Column(modifier = Modifier.background(Background)) {
            TimelineEmptyCard(isFirstInSection = true)
            TimelineEmptyCard()
            TimelineEmptyCard(isLastInSection = true)
        }
    }
}
