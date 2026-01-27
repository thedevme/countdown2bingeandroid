package io.designtoswiftui.countdown2binge.ui.followedshowdetail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.followedshowdetail.CountdownState
import io.designtoswiftui.countdown2binge.ui.followedshowdetail.RankedSeason

/**
 * Binge View tab content container.
 *
 * Contains:
 * 1. Countdown to Finale section (if show is in production)
 * 2. Top Ranked Seasons section (if more than 1 season)
 */
@Composable
fun BingeViewTab(
    showCountdown: Boolean,
    countdownState: CountdownState,
    showRankings: Boolean,
    rankedSeasons: List<RankedSeason>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Countdown to Finale section
        if (showCountdown) {
            CountdownToFinaleSection(
                countdownState = countdownState
            )

            if (showRankings) {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Top Ranked Seasons section
        if (showRankings) {
            TopRankedSeasonsSection(
                rankedSeasons = rankedSeasons
            )
        }

        // Empty state if nothing to show
        if (!showCountdown && !showRankings) {
            EmptyBingeState()
        }
    }
}

@Composable
private fun EmptyBingeState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 40.dp)
    ) {
        Text(
            text = "No binge data available",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Season rankings will appear once the show has multiple rated seasons.",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.3f)
        )
    }
}
