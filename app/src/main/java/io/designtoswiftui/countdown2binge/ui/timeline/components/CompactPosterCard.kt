package io.designtoswiftui.countdown2binge.ui.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.ui.theme.AnticipatedAccent
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.PremieringSoonAccent

/**
 * Compact poster card for COLLAPSED mode (portrait poster).
 *
 * Layout:
 * - Countdown zone (80dp wide, centered)
 * - Spacer
 * - Portrait poster (230x310dp, 15dp corner radius)
 * - Large season badge (56sp, bottom-right of poster)
 *
 * Dimensions:
 * - Card Height: 320dp
 * - Poster Width: 230dp
 * - Poster Height: 310dp
 * - Countdown Zone Width: 80dp
 * - Right Padding: 24dp
 */
@Composable
fun CompactPosterCard(
    countdownStyle: TimelineCountdownStyle,
    posterUrl: String?,
    seasonNumber: Int,
    sectionStyle: TimelineSectionStyle = TimelineSectionStyle.PREMIERING_SOON,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when (sectionStyle) {
        TimelineSectionStyle.PREMIERING_SOON -> PremieringSoonAccent
        TimelineSectionStyle.ANTICIPATED -> AnticipatedAccent
    }

    Row(
        modifier = modifier
            .height(320.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Countdown zone (80dp wide, centered)
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(310.dp),
            contentAlignment = Alignment.Center
        ) {
            CompactCountdownDisplay(
                style = countdownStyle,
                accentColor = accentColor
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Portrait poster
        Box(
            modifier = Modifier
                .width(230.dp)
                .height(310.dp)
                .padding(end = 24.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(CardBackground)
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(15.dp)
                )
        ) {
            // Poster image
            if (posterUrl != null) {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Large season badge (bottom-right)
            Text(
                text = "S$seasonNumber",
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 12.dp)
            )
        }
    }
}

/**
 * Countdown display for compact poster card.
 */
@Composable
private fun CompactCountdownDisplay(
    style: TimelineCountdownStyle,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (style) {
            is TimelineCountdownStyle.Days -> {
                Text(
                    text = style.count.toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "DAYS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
            }
            is TimelineCountdownStyle.Year -> {
                Text(
                    text = style.year.toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
                Text(
                    text = "EXP.",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
            }
            is TimelineCountdownStyle.TBD -> {
                Text(
                    text = "TBD",
                    fontSize = 24.sp, // 36 * 0.67 ≈ 24
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
                Text(
                    text = "DATE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun CompactPosterCardDaysPreview() {
    Countdown2BingeTheme {
        CompactPosterCard(
            countdownStyle = TimelineCountdownStyle.Days(16),
            posterUrl = null,
            seasonNumber = 3,
            sectionStyle = TimelineSectionStyle.PREMIERING_SOON,
            onClick = {},
            modifier = Modifier.background(Background)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun CompactPosterCardYearPreview() {
    Countdown2BingeTheme {
        CompactPosterCard(
            countdownStyle = TimelineCountdownStyle.Year(2026),
            posterUrl = null,
            seasonNumber = 2,
            sectionStyle = TimelineSectionStyle.ANTICIPATED,
            onClick = {},
            modifier = Modifier.background(Background)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun CompactPosterCardTBDPreview() {
    Countdown2BingeTheme {
        CompactPosterCard(
            countdownStyle = TimelineCountdownStyle.TBD,
            posterUrl = null,
            seasonNumber = 1,
            sectionStyle = TimelineSectionStyle.ANTICIPATED,
            onClick = {},
            modifier = Modifier.background(Background)
        )
    }
}
