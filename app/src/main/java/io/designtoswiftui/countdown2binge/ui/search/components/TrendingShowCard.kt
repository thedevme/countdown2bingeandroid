package io.designtoswiftui.countdown2binge.ui.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import io.designtoswiftui.countdown2binge.models.TrendingShowDisplay
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService

/**
 * Portrait-style card for trending shows.
 * Displays poster with logo overlay, genres, and follow button.
 *
 * Design spec:
 * - Poster height: 220dp
 * - Card corner radius: 10dp
 * - Info section horizontal padding: 12dp
 * - Genre tags: 8dp top, 4dp bottom padding
 * - Divider: #38383A, 1dp, 6dp top margin
 * - Button row: 8dp vertical padding
 * - Vertical divider: 1dp width, 32dp height, #4E4E4F, 8dp leading
 * - Season number: 26sp ExtraBold, 4dp trailing padding
 */
@Composable
fun TrendingShowCard(
    show: TrendingShowDisplay,
    isFollowed: Boolean,
    isLoading: Boolean,
    onFollowClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CardBottomBackground)
            .clickable { onCardClick() }
    ) {
        // Poster with overlay (220dp height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            // Poster image
            AsyncImage(
                model = TMDBService.IMAGE_BASE_URL + TMDBService.POSTER_SIZE_SMALL + show.posterPath,
                contentDescription = show.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Network badge (top-right, 10dp inset)
            NetworkBadge(
                showId = show.tmdbId,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
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
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            // Logo image (bottom-left, max 50dp height)
            if (show.logoPath != null) {
                AsyncImage(
                    model = TMDBService.IMAGE_BASE_URL + TMDBService.LOGO_SIZE + show.logoPath,
                    contentDescription = "${show.name} logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .height(50.dp)
                        .width(120.dp)
                )
            }
        }

        // Info section (12dp horizontal padding)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            // Genre tags (8dp top, 4dp bottom)
            Row(
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                show.genreNames.take(2).forEach { genre ->
                    GenreTag(text = genre)
                }
            }

            // Divider (6dp top margin, color #38383A)
            HorizontalDivider(
                modifier = Modifier.padding(top = 6.dp),
                color = DividerColor,
                thickness = 1.dp
            )

            // Button row (8dp vertical padding)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Follow button (fills available space)
                FollowButton(
                    isFollowed = isFollowed,
                    isLoading = isLoading,
                    onClick = onFollowClick,
                    modifier = Modifier.weight(1f)
                )

                // Season number with vertical divider
                show.seasonNumber?.let { seasonNum ->
                    // Vertical divider (8dp leading, 32dp height, #4E4E4F)
                    VerticalDivider(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .height(32.dp),
                        color = VerticalDividerColor,
                        thickness = 1.dp
                    )

                    // Season number (8dp leading from divider, 4dp trailing)
                    Text(
                        text = "S$seasonNum",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp, end = 4.dp)
                    )
                }
            }
        }
    }
}

// Card colors (matching iOS spec)
private val CardBottomBackground = Color(0xFF222224)
private val DividerColor = Color(0xFF38383A)
private val VerticalDividerColor = Color(0xFF4E4E4F)
