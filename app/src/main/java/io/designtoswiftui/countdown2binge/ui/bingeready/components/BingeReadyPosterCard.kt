package io.designtoswiftui.countdown2binge.ui.bingeready.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.ui.theme.BingeReadyAccent
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme

/**
 * Default dimensions for BingeReadyPosterCard (matching iOS).
 * Aspect ratio: 2:3 (0.667)
 */
object BingeReadyCardDefaults {
    val defaultWidth = 280.dp
    val defaultHeight = 420.dp
}

/**
 * Poster card for Binge Ready showing season info and episode count.
 *
 * Structure (matching iOS):
 * - Background poster image
 * - Episode badge (TOP-CENTER)
 * - Large "S#" season number (BOTTOM-LEFT)
 * - Border glow (top card only)
 */
@Composable
fun BingeReadyPosterCard(
    posterUrl: String?,
    seasonNumber: Int,
    episodeCount: Int,
    isTopCard: Boolean,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp? = null
) {
    val sizeModifier = if (width != null && height != null) {
        Modifier.size(width, height)
    } else {
        Modifier.fillMaxSize()
    }

    Box(
        modifier = modifier
            .then(sizeModifier)
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
    ) {
        // Poster image
        if (posterUrl != null) {
            AsyncImage(
                model = posterUrl,
                contentDescription = "Season $seasonNumber poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Placeholder gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2A2A2E),
                                Color(0xFF1A1A1E)
                            )
                        )
                    )
            )
        }

        // Episode badge (TOP-CENTER) - matching iOS
        EpisodeBadge(
            episodeCount = episodeCount,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        // Large season number (BOTTOM-LEFT) - matching iOS
        Text(
            text = "S$seasonNumber",
            fontSize = 64.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 16.dp)
        )

        // Border glow (top card only)
        if (isTopCard) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            )
        }
    }
}

/**
 * Episode count badge (matching iOS style).
 */
@Composable
private fun EpisodeBadge(
    episodeCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = BingeReadyAccent,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = Color.Black,
            modifier = Modifier.size(10.dp)
        )
        Text(
            text = "$episodeCount EPISODES",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            letterSpacing = 0.3.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun BingeReadyPosterCardPreview() {
    Countdown2BingeTheme {
        BingeReadyPosterCard(
            posterUrl = null,
            seasonNumber = 3,
            episodeCount = 10,
            isTopCard = true,
            modifier = Modifier.padding(24.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun BingeReadyPosterCardNotTopPreview() {
    Countdown2BingeTheme {
        BingeReadyPosterCard(
            posterUrl = null,
            seasonNumber = 2,
            episodeCount = 8,
            isTopCard = false,
            modifier = Modifier.padding(24.dp)
        )
    }
}
