package io.designtoswiftui.countdown2binge.ui.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBCastMember
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService

/**
 * Cast/crew member card with circular photo.
 *
 * Design spec:
 * - Image shape: Circle
 * - Image size: 80dp
 * - Name font: Caption
 * - Role font: Caption (tertiary color)
 * - Placeholder: Gray circle with person icon
 */
@Composable
fun PersonCard(
    castMember: TMDBCastMember,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Profile image or placeholder
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(PlaceholderGray),
            contentAlignment = Alignment.Center
        ) {
            if (castMember.profilePath != null) {
                AsyncImage(
                    model = TMDBService.IMAGE_BASE_URL + TMDBService.PROFILE_SIZE + castMember.profilePath,
                    contentDescription = castMember.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(80.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Person placeholder",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Name
        Text(
            text = castMember.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Character/Role
        castMember.character?.let { character ->
            Text(
                text = character,
                fontSize = 10.sp,
                color = TertiaryWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Color constants
private val PlaceholderGray = Color(0xFF3A3A3C)
private val TertiaryWhite = Color.White.copy(alpha = 0.6f)
