package io.designtoswiftui.countdown2binge.ui.followedshowdetail.components

import androidx.compose.foundation.layout.Arrangement
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
import io.designtoswiftui.countdown2binge.ui.followedshowdetail.RankedSeason

/**
 * Top Ranked Seasons section displaying the top 3 seasons by rating.
 *
 * Visibility rules:
 * - Only shown when seasons.count > 1
 * - Filters: seasonNumber > 0 (exclude specials)
 * - Sorted by voteAverage descending
 * - Limited to top 3
 */
@Composable
fun TopRankedSeasonsSection(
    rankedSeasons: List<RankedSeason>,
    modifier: Modifier = Modifier
) {
    if (rankedSeasons.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Section header
        Text(
            text = "Top Ranked Seasons",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Season rank cards
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            rankedSeasons.forEach { rankedSeason ->
                SeasonRankCard(rankedSeason = rankedSeason)
            }
        }
    }
}
