package io.designtoswiftui.countdown2binge.ui.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.AppTypography
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.FooterButtonBackground
import io.designtoswiftui.countdown2binge.ui.theme.FooterButtonBorder
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle

/**
 * Footer section with "View Full Timeline" button and info text.
 */
@Composable
fun TimelineFooter(
    onViewFullTimelineClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 32.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // View Full Timeline button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(FooterButtonBackground)
                .border(
                    width = 1.dp,
                    color = FooterButtonBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onViewFullTimelineClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VIEW FULL TIMELINE",
                    style = AppTypography.sectionHeader,
                    color = OnBackground
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = OnBackground,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cycle icon
        Text(
            text = "↻",
            fontSize = 24.sp,
            color = OnBackgroundSubtle
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Info text
        Text(
            text = "SHOWS CYCLE BACK\nWHEN SEASONS END",
            style = AppTypography.greeting,
            color = OnBackgroundSubtle,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun TimelineFooterPreview() {
    Countdown2BingeTheme {
        TimelineFooter(
            onViewFullTimelineClick = {},
            modifier = Modifier.background(Background)
        )
    }
}
