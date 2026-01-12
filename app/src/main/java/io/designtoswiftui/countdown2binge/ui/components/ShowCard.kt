package io.designtoswiftui.countdown2binge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.GradientOverlayEnd
import io.designtoswiftui.countdown2binge.ui.theme.GradientOverlayStart
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.Surface

/**
 * A card component displaying a TV show with backdrop image and title overlay.
 * Designed with a cinematic, premium feel for the dark theme.
 */
@Composable
fun ShowCard(
    title: String,
    backdropUrl: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    badge: @Composable (() -> Unit)? = null
) {
    val cardShape = RoundedCornerShape(16.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Backdrop image
            AsyncImage(
                model = backdropUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cardShape),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                GradientOverlayStart,
                                GradientOverlayStart,
                                GradientOverlayEnd
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            // Badge (top-left)
            badge?.let {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    it()
                }
            }

            // Title overlay (bottom)
            Text(
                text = title,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                color = OnBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp
            )
        }
    }
}

/**
 * Compact variant of ShowCard for grid layouts.
 */
@Composable
fun ShowCardCompact(
    title: String,
    posterUrl: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardShape = RoundedCornerShape(12.dp)

    Card(
        modifier = modifier
            .aspectRatio(2f / 3f)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = Surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = posterUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cardShape),
                contentScale = ContentScale.Crop
            )

            // Subtle gradient at bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 200f
                        )
                    )
            )

            Text(
                text = title,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
                color = OnBackground,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun ShowCardPreview() {
    Countdown2BingeTheme {
        ShowCard(
            title = "Severance",
            backdropUrl = null,
            modifier = Modifier.padding(16.dp),
            badge = { StateBadge(state = io.designtoswiftui.countdown2binge.models.SeasonState.AIRING) }
        )
    }
}
