@file:SuppressLint("NewApi")

package io.designtoswiftui.countdown2binge.ui.timeline.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Design spec colors for countdown label
private val CountdownHeaderColor = Color(0xFF737373)  // White 45%
private val DividerDotColor = Color(0xFF2BAFA9)       // Teal accent

/**
 * Timeline header types based on what's showing.
 */
enum class TimelineHeaderType {
    CURRENTLY_AIRING,
    PREMIERING_SOON,
    TIMELINE
}

/**
 * Timeline header showing the current section title and last updated time.
 */
@Composable
fun TimelineHeader(
    headerType: TimelineHeaderType,
    lastUpdated: LocalDateTime?,
    modifier: Modifier = Modifier
) {
    val title = when (headerType) {
        TimelineHeaderType.CURRENTLY_AIRING -> "CURRENTLY AIRING"
        TimelineHeaderType.PREMIERING_SOON -> "PREMIERING SOON"
        TimelineHeaderType.TIMELINE -> "Timeline"
    }

    val lastUpdatedText = remember(lastUpdated) {
        lastUpdated?.let {
            val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            "Last updated: ${it.format(formatter).lowercase()}"
        } ?: ""
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = if (headerType == TimelineHeaderType.TIMELINE) 24.sp else 16.sp,
            fontWeight = FontWeight.Bold,
            color = OnBackground,
            textAlign = TextAlign.Center,
            letterSpacing = if (headerType != TimelineHeaderType.TIMELINE) 2.sp else 0.sp
        )

        if (lastUpdatedText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lastUpdatedText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = OnBackgroundSubtle,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Countdown label showing what the countdown is for (e.g., "FINALE EPISODE IN").
 *
 * Design spec:
 * - "FINALE"/"PREMIERE": 28sp Heavy, #737373
 * - "EPISODE IN": 12sp Regular, #737373
 * - Dotted divider: Teal (#2BAFA9), 4dp dots
 */
@Composable
fun CountdownLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Vertical dotted divider above
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = DividerDotColor,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        // Main label (FINALE or PREMIERE)
        Text(
            text = text,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = CountdownHeaderColor,
            textAlign = TextAlign.Center,
            letterSpacing = 2.sp
        )

        // Subtitle (tight to FINALE)
        Text(
            text = "EPISODE IN",
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = CountdownHeaderColor,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp,
            modifier = Modifier
                .offset(y = (-4).dp)
                .padding(bottom = 16.dp)
        )
    }
}

/**
 * Vertical dotted divider line (teal dots).
 * Used above and below the slot machine countdown.
 */
@Composable
fun DottedDivider(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        color = DividerDotColor,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun TimelineHeaderCurrentlyAiringPreview() {
    Countdown2BingeTheme {
        TimelineHeader(
            headerType = TimelineHeaderType.CURRENTLY_AIRING,
            lastUpdated = LocalDateTime.now().minusMinutes(3),
            modifier = Modifier.background(Background)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun CountdownLabelPreview() {
    Countdown2BingeTheme {
        CountdownLabel(
            text = "FINALE",
            modifier = Modifier.background(Background)
        )
    }
}
