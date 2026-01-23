package io.designtoswiftui.countdown2binge.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.R
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBSearchResult
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.models.GenreMapping
import io.designtoswiftui.countdown2binge.ui.detail.components.NetworkBadge
import io.designtoswiftui.countdown2binge.ui.detail.components.getPlaceholderNetwork
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import kotlin.math.abs

/**
 * Portrait show card for "More Like This" section.
 *
 * iOS Design:
 * - Poster with network badge in top-right corner
 * - NO logo overlay for recommendations
 * - Dark info section below with genre tags + follow button
 * - Genre tags: Dark pill style (#2D2D2F bg, #383839 border)
 * - Follow button: Teal for unfollowed, dark gray for followed
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PortraitShowCard(
    show: TMDBSearchResult,
    isFollowed: Boolean,
    isLoading: Boolean,
    onFollowClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val genreNames = GenreMapping.getGenreNames(show.genreIds ?: emptyList(), 2)
    val placeholderNetwork = getPlaceholderNetwork(show.id)

    Column(
        modifier = modifier
            .height(320.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CardBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCardClick() }
    ) {
        // Poster with network badge - fills remaining space after info section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF1A1A1C)),
            contentAlignment = Alignment.Center
        ) {
            if (show.posterPath != null) {
                AsyncImage(
                    model = TMDBService.IMAGE_BASE_URL + TMDBService.POSTER_SIZE + show.posterPath,
                    contentDescription = show.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Show logo and show name when no poster
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon_wht),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = show.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Network badge (top-right)
            NetworkBadge(
                network = placeholderNetwork,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }

        // Info section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Genre tags (dark pill style for cards)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                genreNames.forEach { genre ->
                    CardGenreTag(text = genre)
                }
            }

            // Follow button
            PortraitFollowButton(
                isFollowed = isFollowed,
                isLoading = isLoading,
                onClick = onFollowClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Genre tag for cards - dark pill style.
 * Different from detail screen genre tags which use teal outline.
 */
@Composable
private fun CardGenreTag(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(TagBackground)
            .border(
                width = 1.dp,
                color = TagBorder,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

/**
 * Follow button with auto-sizing text to prevent wrapping.
 * iOS Design: Teal for FOLLOW, dark gray for FOLLOWING
 */
@Composable
private fun PortraitFollowButton(
    isFollowed: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isFollowed) {
        FollowedBackground
    } else {
        DetailAccent
    }

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !isLoading
            ) { onClick() }
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .scale(0.7f),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isFollowed) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (isFollowed) "Following" else "Follow",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                AutoSizeText(
                    text = if (isFollowed) "FOLLOWING" else "FOLLOW",
                    maxFontSize = 16.sp,
                    minFontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Text that automatically scales down to fit available space.
 */
@Composable
private fun AutoSizeText(
    text: String,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    fontWeight: FontWeight,
    color: Color,
    modifier: Modifier = Modifier
) {
    var fontSize by remember { mutableStateOf(maxFontSize) }

    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        onTextLayout = { result ->
            if (result.hasVisualOverflow && fontSize > minFontSize) {
                fontSize = (fontSize.value - 0.5f).sp
            }
        },
        modifier = modifier
    )
}

// Color constants
private val CardBackground = Color(0xFF222224)
private val TagBackground = Color(0xFF2D2D2F)
private val TagBorder = Color(0xFF383839)
private val FollowedBackground = Color(0xFF2A2A2C)
