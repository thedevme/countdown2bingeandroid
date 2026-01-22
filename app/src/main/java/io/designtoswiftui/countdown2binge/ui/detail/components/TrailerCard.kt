package io.designtoswiftui.countdown2binge.ui.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBVideo

/**
 * Trailer/video card with YouTube thumbnail and play overlay.
 *
 * iOS Design:
 * - Thumbnail URL: https://img.youtube.com/vi/{key}/mqdefault.jpg
 * - Larger card width: ~200dp
 * - Title displayed BELOW the thumbnail (not overlaid)
 * - Play button overlay centered
 * - Corner radius: 8dp
 */
@Composable
fun TrailerCard(
    video: TMDBVideo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(200.dp)
            .clickable { onClick() },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Thumbnail with play overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            // Thumbnail image
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Play button overlay
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Video title (below thumbnail)
        Text(
            text = video.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
