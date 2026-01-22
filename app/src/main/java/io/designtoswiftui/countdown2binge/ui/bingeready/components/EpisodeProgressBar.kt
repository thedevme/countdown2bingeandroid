package io.designtoswiftui.countdown2binge.ui.bingeready.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import kotlinx.coroutines.delay

// Colors matching iOS
private val EmptyColor = Color.White.copy(alpha = 0.15f)
private val FilledColor = Color(0xFF73E6B3) // Teal accent

// Animation constants
private const val SEGMENT_HEIGHT_DP = 16
private const val SEGMENT_GAP_DP = 1
private const val CORNER_RADIUS_DP = 4
private const val SLIDE_OFFSET_DP = 20f
private const val STAGGER_DELAY_MS = 80L
private const val MAX_SEGMENTS = 10

// Spring animation matching iOS: response 0.3, dampingFraction 0.75
private val segmentSpring = spring<Float>(
    dampingRatio = 0.75f,
    stiffness = 1000f // Approximately 300ms response
)

/**
 * Custom shape for the first segment - rounded corners on right side only.
 */
private class FirstSegmentShape(private val cornerRadius: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            // Start top-left
            moveTo(0f, 0f)
            // Top edge to top-right corner
            lineTo(size.width - cornerRadius, 0f)
            // Top-right rounded corner
            quadraticBezierTo(size.width, 0f, size.width, cornerRadius)
            // Right edge to bottom-right corner
            lineTo(size.width, size.height - cornerRadius)
            // Bottom-right rounded corner
            quadraticBezierTo(size.width, size.height, size.width - cornerRadius, size.height)
            // Bottom edge to bottom-left
            lineTo(0f, size.height)
            // Left edge back to start
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Episode progress bar showing progress from premiere to finale.
 *
 * Features:
 * - Individual episode segments (max 10 visible)
 * - Overflow indicator (+X) when more than 10 episodes
 * - Staggered Y-slide animation:
 *   - Filling: slides UP from below (left to right)
 *   - Emptying: slides UP and out (right to left)
 * - Labels: "PREMIERE" (left) and "FINALE" (right) below bar
 *
 * @param showKey Unique key for the show - when this changes, triggers empty animation
 */
@Composable
fun EpisodeProgressBar(
    totalEpisodes: Int,
    watchedCount: Int,
    modifier: Modifier = Modifier,
    showKey: Long = 0L,
    animateChanges: Boolean = true
) {
    val displaySegments = minOf(totalEpisodes, MAX_SEGMENTS)
    val overflowCount = maxOf(0, totalEpisodes - MAX_SEGMENTS)

    // Track show changes to trigger empty animation
    var previousShowKey by remember { mutableStateOf(showKey) }
    var previousWatchedCount by remember { mutableIntStateOf(watchedCount) }
    var emptyAnimationCount by remember { mutableIntStateOf(0) } // How many segments to animate empty

    LaunchedEffect(showKey) {
        if (showKey != previousShowKey && previousShowKey != 0L) {
            // Show changed - trigger empty animation for previously watched segments
            emptyAnimationCount = previousWatchedCount
            delay(50) // Brief delay to allow state to settle
            emptyAnimationCount = 0
        }
        previousShowKey = showKey
        previousWatchedCount = watchedCount
    }

    // Update previous watched count when it changes
    LaunchedEffect(watchedCount) {
        previousWatchedCount = watchedCount
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Progress segments row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SEGMENT_GAP_DP.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Episode segments
            for (index in 0 until displaySegments) {
                val isWatched = index < watchedCount
                val isFirst = index == 0
                // Only trigger empty animation for segments that were filled
                val shouldAnimateThisSegmentEmpty = index < emptyAnimationCount

                EpisodeSegment(
                    isWatched = isWatched,
                    index = index,
                    totalSegments = displaySegments,
                    isFirst = isFirst,
                    animateChanges = animateChanges,
                    triggerEmptyAnimation = shouldAnimateThisSegmentEmpty,
                    modifier = Modifier.weight(1f)
                )
            }

            // Overflow indicator
            if (overflowCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+$overflowCount",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // Labels row (below bar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "PREMIERE",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 1.2.sp
            )
            Text(
                text = "FINALE",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 1.2.sp
            )
        }
    }
}

/**
 * Individual episode segment with Y-slide animation.
 *
 * Animation states:
 * - offset = 20dp: Hidden below (ready to fill)
 * - offset = 0dp: Visible (filled)
 * - offset = -20dp: Hidden above (emptied out)
 */
@Composable
private fun EpisodeSegment(
    isWatched: Boolean,
    index: Int,
    totalSegments: Int,
    isFirst: Boolean,
    animateChanges: Boolean,
    triggerEmptyAnimation: Boolean = false,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { CORNER_RADIUS_DP.dp.toPx() }

    // Track previous watched state to determine animation direction
    var previousWatched by remember { mutableStateOf(isWatched) }
    val fillOffset = remember { Animatable(if (isWatched) 0f else SLIDE_OFFSET_DP) }

    // Handle empty animation trigger (when switching shows)
    // Only animates segments that were previously filled
    LaunchedEffect(triggerEmptyAnimation) {
        if (triggerEmptyAnimation && animateChanges) {
            // Start filled, then animate empty (right to left)
            fillOffset.snapTo(0f) // Start visible
            val reverseIndex = totalSegments - 1 - index
            val delayMs = reverseIndex * STAGGER_DELAY_MS
            delay(delayMs)
            fillOffset.animateTo(-SLIDE_OFFSET_DP, segmentSpring) // Slide up and out
            delay(300)
            fillOffset.snapTo(SLIDE_OFFSET_DP) // Reset for next fill
        }
    }

    LaunchedEffect(isWatched) {
        if (!animateChanges) {
            fillOffset.snapTo(if (isWatched) 0f else SLIDE_OFFSET_DP)
            previousWatched = isWatched
            return@LaunchedEffect
        }

        if (isWatched && !previousWatched) {
            // Filling: left to right (use index for delay)
            val delayMs = index * STAGGER_DELAY_MS
            delay(delayMs)
            fillOffset.snapTo(SLIDE_OFFSET_DP) // Start below
            fillOffset.animateTo(0f, segmentSpring) // Slide up into view
        } else if (!isWatched && previousWatched) {
            // Emptying: right to left (use reverse index for delay)
            val reverseIndex = totalSegments - 1 - index
            val delayMs = reverseIndex * STAGGER_DELAY_MS
            delay(delayMs)
            fillOffset.animateTo(-SLIDE_OFFSET_DP, segmentSpring) // Slide up and out
            // Reset for next fill
            delay(300)
            fillOffset.snapTo(SLIDE_OFFSET_DP)
        }

        previousWatched = isWatched
    }

    // Segment shape: first segment has rounded right corners only, others are rectangles
    val segmentShape = if (isFirst) {
        FirstSegmentShape(cornerRadiusPx)
    } else {
        RectangleShape
    }

    Box(
        modifier = modifier
            .height(SEGMENT_HEIGHT_DP.dp)
            .clip(segmentShape)
            .background(EmptyColor)
    ) {
        // Animated fill overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = with(density) { fillOffset.value.dp.toPx() }
                }
                .background(FilledColor)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun EpisodeProgressBarPreview() {
    Countdown2BingeTheme {
        EpisodeProgressBar(
            totalEpisodes = 8,
            watchedCount = 5,
            animateChanges = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun EpisodeProgressBarOverflowPreview() {
    Countdown2BingeTheme {
        EpisodeProgressBar(
            totalEpisodes = 22,
            watchedCount = 12,
            animateChanges = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun EpisodeProgressBarEmptyPreview() {
    Countdown2BingeTheme {
        EpisodeProgressBar(
            totalEpisodes = 10,
            watchedCount = 0,
            animateChanges = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun EpisodeProgressBarFullPreview() {
    Countdown2BingeTheme {
        EpisodeProgressBar(
            totalEpisodes = 6,
            watchedCount = 6,
            animateChanges = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}
