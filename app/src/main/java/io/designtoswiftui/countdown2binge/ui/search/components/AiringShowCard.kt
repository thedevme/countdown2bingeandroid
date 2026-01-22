package io.designtoswiftui.countdown2binge.ui.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.models.AiringShowDisplay
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService

/**
 * Landscape-style card for airing/ending soon shows.
 * Displays backdrop with title overlay, genres, follow button, and days countdown.
 *
 * Design spec:
 * - Backdrop height: 220dp
 * - Card corner radius: 10dp
 * - Network badge inset: 12dp
 * - Title: 22sp Bold, 16dp padding all sides
 * - Genre row: 12dp horizontal, 12dp top padding
 * - Bottom row: 12dp horizontal, 12dp bottom padding
 * - Vertical divider: 1dp width, 70dp height, #4E4E4F, 12dp each side
 * - Days number: 46sp ExtraBold, zero-padded 2 digits
 * - Days label: 14sp Medium, 0.5 alpha, "DAYS LEFT"
 */
@Composable
fun AiringShowCard(
    show: AiringShowDisplay,
    isFollowed: Boolean,
    isLoading: Boolean,
    onFollowClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardBottomBackground)
            .clickable { onCardClick() }
    ) {
        // Backdrop with overlay (220dp height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            // Backdrop image (fallback to poster if no backdrop)
            val imagePath = show.backdropPath ?: show.posterPath
            AsyncImage(
                model = TMDBService.IMAGE_BASE_URL + TMDBService.BACKDROP_SIZE + imagePath,
                contentDescription = show.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Network badge (top-right, 12dp inset)
            NetworkBadge(
                showId = show.tmdbId,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )

            // Bottom gradient (100dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            )

            // Show title (bottom-left, 16dp padding all sides)
            Text(
                text = show.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }

        // Info section
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Genre tags (12dp horizontal, 12dp top)
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                show.genreNames.take(2).forEach { genre ->
                    GenreTag(text = genre)
                }
            }

            // Follow button + Days counter row (12dp horizontal, 12dp bottom)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Follow button (fills available space)
                FollowButton(
                    isFollowed = isFollowed,
                    isLoading = isLoading,
                    onClick = onFollowClick,
                    modifier = Modifier.weight(1f)
                )

                // Vertical divider (12dp each side, 70dp height, #4E4E4F)
                VerticalDivider(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .height(70.dp),
                    color = VerticalDividerColor,
                    thickness = 1.dp
                )

                // Days left counter (4dp trailing)
                DaysLeftCounter(
                    daysLeft = show.daysLeft,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}

/**
 * Days left countdown display.
 *
 * Design spec:
 * - Number: 46sp ExtraBold, zero-padded 2 digits, "--" for null
 * - Label: 14sp Medium, rgba(255, 255, 255, 0.5), "DAYS LEFT"
 * - Vertical spacing: 0dp (stacked)
 */
@Composable
private fun DaysLeftCounter(
    daysLeft: Int?,
    modifier: Modifier = Modifier
) {
    // Format: zero-padded 2 digits or "--" for null
    val displayText = daysLeft?.let {
        if (it < 10) "0$it" else it.toString()
    } ?: "--"

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = displayText,
            fontSize = 46.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = "DAYS LEFT",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.5f)
        )
    }
}

// Card colors (matching iOS spec)
private val CardBottomBackground = Color(0xFF222224)
private val VerticalDividerColor = Color(0xFF4E4E4F)
