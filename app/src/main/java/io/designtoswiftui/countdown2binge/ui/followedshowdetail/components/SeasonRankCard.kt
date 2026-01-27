package io.designtoswiftui.countdown2binge.ui.followedshowdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.followedshowdetail.RankedSeason

// Rank colors
private val GoldColor = Color(0xFFFFD700)
private val SilverColor = Color(0xFFC0C0C0)
private val BronzeColor = Color(0xFFCD853F)

/**
 * Season rank card displaying a season's ranking information.
 *
 * Design specs:
 * - Card height: 140dp
 * - Padding: 16dp (lg)
 * - Corner radius: 12dp (card)
 * - Rank badge: top-right with asymmetric corners
 * - Watermark: 140sp, bold, white @ 3%
 * - Rating bar at bottom
 */
@Composable
fun SeasonRankCard(
    rankedSeason: RankedSeason,
    modifier: Modifier = Modifier
) {
    val rankColor = getRankColor(rankedSeason.rank)
    val cardShape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(cardShape)
            .background(Color(0xFF0D0D0D))
            .border(1.dp, Color(0xFF252525), cardShape)
            .semantics {
                contentDescription = buildAccessibilityDescription(rankedSeason)
            }
    ) {
        // Watermark text (season number)
        Text(
            text = "S${rankedSeason.season.seasonNumber}",
            fontSize = 100.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.03f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 20.dp, y = 20.dp)
        )

        // Main content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Left content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Rank label
                Text(
                    text = rankedSeason.rankLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = rankColor,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Season title and year
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Season ${rankedSeason.season.seasonNumber}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    rankedSeason.year?.let { year ->
                        Text(
                            text = "  ·  $year",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Episode count and descriptor
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${rankedSeason.season.episodeCount ?: 0} Episodes",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "•",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                    Text(
                        text = rankedSeason.descriptor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Rating bar (only for gold)
                if (rankedSeason.rank == 1) {
                    val rating = rankedSeason.season.voteAverage ?: 0.0
                    val progress = (rating / 10f).toFloat()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(3.dp)
                                .background(rankColor)
                        )
                    }
                }
            }

            // Right content - Rating and rank badge
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(108.dp)
            ) {
                // Rank badge
                RankBadge(
                    rank = rankedSeason.rank,
                    color = rankColor
                )

                // Rating score
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = String.format("%.1f", rankedSeason.season.voteAverage ?: 0.0),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (rankedSeason.rank == 1) {
                        Text(
                            text = "GOLD STANDARD",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = rankColor,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RankBadge(
    rank: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Asymmetric shape: rounded on bottom-left, card radius on top-right
    val badgeShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 12.dp,
        bottomEnd = 0.dp,
        bottomStart = 8.dp
    )

    Box(
        modifier = modifier
            .width(36.dp)
            .height(44.dp)
            .offset(x = 16.dp, y = (-16).dp)
            .clip(badgeShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$rank",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

private fun getRankColor(rank: Int): Color {
    return when (rank) {
        1 -> GoldColor
        2 -> SilverColor
        3 -> BronzeColor
        else -> Color.White.copy(alpha = 0.7f)
    }
}

private fun buildAccessibilityDescription(rankedSeason: RankedSeason): String {
    val rating = rankedSeason.season.voteAverage ?: 0.0
    val episodeCount = rankedSeason.season.episodeCount ?: 0

    return "Season ${rankedSeason.season.seasonNumber}, ranked ${rankedSeason.rank}. " +
            "Rating ${String.format("%.1f", rating)} out of 10. $episodeCount episodes."
}
