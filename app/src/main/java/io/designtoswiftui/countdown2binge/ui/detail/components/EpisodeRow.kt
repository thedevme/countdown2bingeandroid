package io.designtoswiftui.countdown2binge.ui.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBEpisode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Single episode row display.
 *
 * Design spec:
 * - Episode number width: 24dp
 * - Episode number format: "01", "02" (zero-padded)
 * - Episode number color: Tertiary white
 * - Title font: Body small
 * - Runtime format: "45M" or "1H 30M"
 * - Date format: "MAR 15" (uppercase)
 * - Vertical padding: 12dp
 */
@Composable
fun EpisodeRow(
    episode: TMDBEpisode,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Episode number (zero-padded, 24dp width)
        Text(
            text = String.format("%02d", episode.episodeNumber),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TertiaryWhite,
            modifier = Modifier.width(24.dp)
        )

        // Episode info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Episode title
            Text(
                text = episode.name ?: "Episode ${episode.episodeNumber}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 2
            )

            // Runtime and date
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Runtime
                episode.runtime?.let { runtime ->
                    Text(
                        text = formatRuntime(runtime),
                        fontSize = 12.sp,
                        color = TertiaryWhite
                    )
                }

                // Separator dot if both present
                if (episode.runtime != null && episode.airDate != null) {
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = TertiaryWhite
                    )
                }

                // Air date
                episode.airDate?.let { dateString ->
                    Text(
                        text = formatAirDate(dateString),
                        fontSize = 12.sp,
                        color = TertiaryWhite
                    )
                }
            }
        }
    }
}

/**
 * Format runtime in minutes to display format.
 * Examples: "45M", "1H 30M"
 */
private fun formatRuntime(minutes: Int): String {
    return if (minutes >= 60) {
        val hours = minutes / 60
        val mins = minutes % 60
        if (mins > 0) "${hours}H ${mins}M" else "${hours}H"
    } else {
        "${minutes}M"
    }
}

/**
 * Format air date string to display format.
 * Input: "2024-03-15"
 * Output: "MAR 15"
 */
private fun formatAirDate(dateString: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
        date.format(formatter).uppercase()
    } catch (e: Exception) {
        dateString
    }
}

// Color constants
private val TertiaryWhite = Color.White.copy(alpha = 0.6f)
