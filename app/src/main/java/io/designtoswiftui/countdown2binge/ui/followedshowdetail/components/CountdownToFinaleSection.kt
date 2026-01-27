package io.designtoswiftui.countdown2binge.ui.followedshowdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.models.Season
import io.designtoswiftui.countdown2binge.ui.followedshowdetail.CountdownState
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent

/**
 * Countdown to Finale section displaying a live countdown timer.
 *
 * Design specs:
 * - Box background: white @ 8%
 * - Box border: white @ 15%, 1dp
 * - Corner radius: 6dp
 * - Colon color: Accent (#4AC7B8), 32sp
 * - Shows "--" when date is unknown
 */
@Composable
fun CountdownToFinaleSection(
    countdownState: CountdownState,
    modifier: Modifier = Modifier
) {
    val season = countdownState.currentSeason ?: return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .semantics {
                contentDescription = buildAccessibilityDescription(countdownState)
                liveRegion = LiveRegionMode.Polite
            }
    ) {
        // Section header
        Text(
            text = "Countdown to Finale",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Countdown boxes row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CountdownBox(
                value = countdownState.days,
                label = "DAYS"
            )
            CountdownColon()
            CountdownBox(
                value = countdownState.hours,
                label = "HRS"
            )
            CountdownColon()
            CountdownBox(
                value = countdownState.minutes,
                label = "MIN"
            )
            CountdownColon()
            CountdownBox(
                value = countdownState.seconds,
                label = "SEC"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Event badge
        EventBadge(label = countdownState.eventLabel)

        Spacer(modifier = Modifier.height(12.dp))

        // Episode badge and title
        EpisodeInfo(season = season)

        Spacer(modifier = Modifier.height(12.dp))

        // Binge status text
        BingeStatusText(season = season)
    }
}

@Composable
private fun CountdownBox(
    value: Int?,
    label: String,
    modifier: Modifier = Modifier
) {
    val displayValue = formatCountdownValue(value)
    val boxShape = RoundedCornerShape(6.dp)

    Column(
        modifier = modifier
            .width(72.dp)
            .clip(boxShape)
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), boxShape)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = displayValue,
            fontSize = if (value != null && value >= 100) 28.sp else 36.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun CountdownColon() {
    Text(
        text = ":",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = DetailAccent,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun EventBadge(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun EpisodeInfo(
    season: Season,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Episode badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(DetailAccent)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "S${String.format("%02d", season.seasonNumber)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "E${String.format("%02d", season.episodeCount)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        // Episode title placeholder
        Text(
            text = "Season ${season.seasonNumber} Finale",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun BingeStatusText(
    season: Season,
    modifier: Modifier = Modifier
) {
    val statusText = getBingeStatus(season)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "ⓘ",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
        Text(
            text = statusText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.5f),
            letterSpacing = 0.5.sp
        )
    }
}

private fun formatCountdownValue(value: Int?): String {
    return when {
        value == null -> "--"
        value >= 100 -> "$value"
        else -> String.format("%02d", value)
    }
}

private fun getBingeStatus(season: Season): String {
    val totalCount = season.episodeCount
    val airedCount = season.airedEpisodeCount

    return when {
        airedCount >= totalCount && totalCount > 0 ->
            "BINGE STATUS: READY. ALL $totalCount EPISODES AVAILABLE."
        airedCount > 0 && airedCount < totalCount ->
            "BINGE STATUS: IMMINENT. SEASON CURRENTLY AIRING."
        else ->
            "BINGE STATUS: ANTICIPATED. PREMIERE APPROACHING."
    }
}

private fun buildAccessibilityDescription(state: CountdownState): String {
    val days = state.days?.toString() ?: "unknown"
    val hours = state.hours?.toString() ?: "unknown"
    val minutes = state.minutes?.toString() ?: "unknown"
    val seconds = state.seconds?.toString() ?: "unknown"

    return "Countdown: $days days, $hours hours, $minutes minutes, $seconds seconds until finale"
}
