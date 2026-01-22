package io.designtoswiftui.countdown2binge.ui.detail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Horizontal season selector.
 *
 * Design spec:
 * - Scroll direction: Horizontal
 * - Format: "S1", "S2", "S3"...
 * - Selected style: Larger font, white
 * - Unselected style: Smaller font, gray
 * - Gap between items: 16dp
 */
@Composable
fun SeasonPicker(
    seasons: List<Int>,
    selectedSeason: Int,
    onSeasonSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(seasons) { seasonNumber ->
            val isSelected = seasonNumber == selectedSeason

            Text(
                text = "S$seasonNumber",
                fontSize = if (isSelected) 18.sp else 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else UnselectedGray,
                modifier = Modifier
                    .clickable { onSeasonSelected(seasonNumber) }
                    .padding(vertical = 8.dp)
            )
        }
    }
}

// Color constants
private val UnselectedGray = Color.White.copy(alpha = 0.4f)
