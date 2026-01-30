package io.designtoswiftui.countdown2binge.ui.detail.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent

/**
 * Info section with expandable synopsis and metadata.
 *
 * iOS Design:
 * - Synopsis line limit (collapsed): 3 lines
 * - "more ∨" / "less ∧" with chevron icons
 * - Metadata format: "{N} SEASONS · {N} EPISODES · {STATUS}"
 * - Rating displayed with star icon
 * - Network name at end
 * - Metadata font size: 11sp
 * - Metadata letter spacing: 1sp
 * - Metadata color: rgba(255, 255, 255, 0.5)
 */
@Composable
fun InfoSection(
    synopsis: String?,
    seasonCount: Int,
    statusText: String,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier,
    episodeCount: Int? = null,
    rating: Double? = null,
    networkName: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Synopsis with expandable text
        if (!synopsis.isNullOrBlank()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = synopsis,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 20.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                )

                // "more ∨" / "less ∧" link with chevron
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onExpandClick() }
                ) {
                    Text(
                        text = if (isExpanded) "less" else "more",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DetailAccent
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = DetailAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Metadata row: "4 SEASONS · 48 EPISODES · RETURNING SERIES · ⭐ 8.4 · NETFLIX"
        MetadataRow(
            seasonCount = seasonCount,
            episodeCount = episodeCount,
            statusText = statusText,
            rating = rating,
            networkName = networkName
        )
    }
}

/**
 * Displays show metadata in a multiline format.
 * Line 1: Seasons · Episodes · Status
 * Line 2: ⭐ Rating · Network
 */
@Composable
private fun MetadataRow(
    seasonCount: Int,
    episodeCount: Int?,
    statusText: String,
    rating: Double?,
    networkName: String?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Line 1: Seasons · Episodes · Status
        val line1Parts = buildList {
            if (seasonCount > 0) {
                add(if (seasonCount == 1) "1 SEASON" else "$seasonCount SEASONS")
            }
            if (episodeCount != null && episodeCount > 0) {
                add(if (episodeCount == 1) "1 EPISODE" else "$episodeCount EPISODES")
            }
            if (statusText.isNotBlank()) {
                add(statusText.uppercase())
            }
        }

        if (line1Parts.isNotEmpty()) {
            Text(
                text = line1Parts.joinToString("  ·  "),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MetadataWhite,
                letterSpacing = 1.sp
            )
        }

        // Line 2: Rating · Network
        val hasRating = rating != null && rating > 0
        val hasNetwork = !networkName.isNullOrBlank()

        if (hasRating || hasNetwork) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rating with star icon
                if (hasRating) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = RatingStarColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = String.format("%.1f", rating),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MetadataWhite,
                        letterSpacing = 1.sp
                    )
                }

                // Separator if both rating and network
                if (hasRating && hasNetwork) {
                    Text(
                        text = "  ·  ",
                        fontSize = 11.sp,
                        color = MetadataWhite
                    )
                }

                // Network name
                if (hasNetwork) {
                    Text(
                        text = networkName!!.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MetadataWhite,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// Color constants
private val MetadataWhite = Color.White.copy(alpha = 0.5f)
private val RatingStarColor = Color(0xFFFFD700) // Gold color for star
