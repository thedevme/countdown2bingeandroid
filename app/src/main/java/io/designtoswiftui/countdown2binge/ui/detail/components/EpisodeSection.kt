package io.designtoswiftui.countdown2binge.ui.detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBEpisode

/**
 * Episode list section with watermark and expand/collapse.
 *
 * iOS Design (from screenshots):
 * - "SEASON X" header is ABOVE the container (not inside)
 * - Container corner radius: 12dp
 * - Container background: #1A1A1C
 * - Container border: rgba(255, 255, 255, 0.08), 1dp
 * - NO divider lines between episodes
 * - Watermark text: "S{number}" in BOTTOM RIGHT corner
 * - Watermark font size: 180sp
 * - Watermark opacity: 0.04
 * - Default episodes shown: 4
 * - "VIEW ALL EPISODES" centered with decorative line above
 */
@Composable
fun EpisodeSection(
    seasonNumber: Int,
    episodes: List<TMDBEpisode>,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasMoreEpisodes = episodes.size > 4

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section header (OUTSIDE the container)
        Text(
            text = "SEASON $seasonNumber",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )

        // Episode container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ContainerBackground)
                .border(
                    width = 1.dp,
                    color = ContainerBorder,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            // Season watermark (BOTTOM RIGHT corner, cut off by container clip)
            Text(
                text = "S$seasonNumber",
                fontSize = 180.sp,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.04f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 40.dp, y = 50.dp)  // Push outside to be clipped
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Always show first 4 episodes
                episodes.take(4).forEach { episode ->
                    EpisodeRow(episode = episode)
                }

                // Animated expansion for remaining episodes
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        expandFrom = Alignment.Top
                    ),
                    exit = shrinkVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        shrinkTowards = Alignment.Top
                    )
                ) {
                    Column {
                        episodes.drop(4).forEach { episode ->
                            EpisodeRow(episode = episode)
                        }
                    }
                }

                // Expand/collapse button with decorative lines (iOS style)
                if (hasMoreEpisodes) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExpandClick() }
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Left decorative line
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = DividerColor,
                            thickness = 0.5.dp
                        )

                        // Text
                        Text(
                            text = if (isExpanded) "SHOW LESS" else "VIEW ALL EPISODES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Right decorative line
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = DividerColor,
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

// Color constants
private val ContainerBackground = Color(0xFF1A1A1C)
private val ContainerBorder = Color.White.copy(alpha = 0.08f)
private val DividerColor = Color.White.copy(alpha = 0.1f)
