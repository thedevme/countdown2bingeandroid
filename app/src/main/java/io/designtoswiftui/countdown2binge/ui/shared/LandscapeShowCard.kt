package io.designtoswiftui.countdown2binge.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
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
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.ui.theme.Primary
import io.designtoswiftui.countdown2binge.viewmodels.SpinoffShowDisplay

/**
 * Landscape show card for spinoff collections.
 *
 * Design spec:
 * - Image aspect ratio: 16:9
 * - Card corner radius: 12dp
 * - Card background: #222224
 * - Card border: #383839, 1dp
 * - Content padding: 12dp
 * - Rank badge size: 40dp x 40dp
 * - Rank badge background: Accent teal
 * - Rank badge position: Top-right corner
 * - Series badge background: Accent teal
 * - Series badge corner radius: 3dp
 */
@Composable
fun LandscapeShowCard(
    show: SpinoffShowDisplay,
    isFollowed: Boolean,
    isLoading: Boolean,
    onFollowClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .border(
                width = 1.dp,
                color = CardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onCardClick() }
    ) {
        // Backdrop image with badges (16:9)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            AsyncImage(
                model = TMDBService.IMAGE_BASE_URL + TMDBService.BACKDROP_SIZE + show.backdropPath,
                contentDescription = show.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Series badge (top-left)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .background(
                        color = Primary,
                        shape = RoundedCornerShape(3.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "SERIES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Rank badge (top-right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(40.dp)
                    .background(
                        color = Primary,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = show.rank.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Info section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status label
            Text(
                text = show.statusLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                letterSpacing = 0.5.sp
            )

            // Title
            Text(
                text = show.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Rating and seasons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                show.voteAverage?.let { rating ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = String.format("%.1f Rating", rating),
                            fontSize = 12.sp,
                            color = MetadataColor
                        )
                    }

                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = MetadataColor
                    )
                }

                show.numberOfSeasons?.let { seasons ->
                    val seasonText = if (seasons == 1) "1 Season" else "$seasons Seasons"
                    Text(
                        text = seasonText,
                        fontSize = 12.sp,
                        color = MetadataColor
                    )
                }
            }

            // Synopsis
            show.overview?.let { overview ->
                Text(
                    text = overview,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            // Follow button
            LandscapeFollowButton(
                isFollowed = isFollowed,
                isLoading = isLoading,
                onClick = onFollowClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Follow button for landscape cards.
 */
@Composable
private fun LandscapeFollowButton(
    isFollowed: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isFollowed) {
        Color.White.copy(alpha = 0.2f)
    } else {
        Primary
    }

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(enabled = !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isFollowed) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (isFollowed) "Following" else "Follow",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isFollowed) "FOLLOWING" else "FOLLOW",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// Color constants
private val CardBackground = Color(0xFF222224)
private val CardBorder = Color(0xFF383839)
private val MetadataColor = Color.White.copy(alpha = 0.6f)
