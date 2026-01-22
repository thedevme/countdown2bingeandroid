package io.designtoswiftui.countdown2binge.ui.bingeready.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Gesture direction for axis locking.
 */
private enum class GestureDirection {
    None,
    Horizontal,
    Vertical
}

/**
 * Data class for a season card in the binge ready stack.
 */
data class BingeReadySeasonData(
    val showId: Long,
    val seasonId: Long,
    val seasonNumber: Int,
    val posterUrl: String?,
    val title: String,
    val episodesRemaining: Int
)

// Fan configuration matching iOS exactly
private object FanConfig {
    val fanSpread = 28.dp       // Horizontal distance between cards
    val fanYOffset = 8.dp       // Vertical drop per position
    const val fanRotation = 5f  // Rotation degrees per position
    const val scaleStep = 0.06f // Scale reduction per position (6%)
    val swipeThreshold = 100.dp // Threshold to trigger card change
    val verticalSwipeThreshold = 80.dp // Threshold for vertical swipes
}

// Spring matching iOS: interpolatingSpring(stiffness: 300, damping: 40)
// Lower damping ratio = more overshoot/bounce
private val cardSpring = spring<Float>(
    stiffness = 300f,
    dampingRatio = 0.65f // Allows overshoot then settle
)

/**
 * A continuous swiping card stack with looping for Binge Ready seasons.
 * Cards wrap around infinitely - swiping past the last card goes to the first.
 *
 * Gestures:
 * - Swipe RIGHT: Next season (1→2→3)
 * - Swipe LEFT: Previous season (3→2→1)
 * - Swipe UP: Mark season watched
 * - Swipe DOWN: Delete show (with confirmation)
 */
@Composable
fun BingeReadyCardStack(
    seasons: List<BingeReadySeasonData>,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    onMarkWatched: (BingeReadySeasonData) -> Unit,
    onDelete: () -> Unit,
    onCardClick: (BingeReadySeasonData) -> Unit,
    cardSize: DpSize = DpSize(200.dp, 300.dp),
    modifier: Modifier = Modifier
) {
    if (seasons.isEmpty()) return

    val count = seasons.size
    var activeGesture by remember { mutableStateOf(GestureDirection.None) } // Direction locking

    // Animated values for smooth transitions
    val animatedIndex = remember { Animatable(currentIndex.toFloat()) }
    val dragOffsetX = remember { Animatable(0f) }
    val dragOffsetY = remember { Animatable(0f) }

    val scope = rememberCoroutineScope()

    // Track last index we animated to, to detect external changes
    var lastAnimatedIndex by remember { mutableIntStateOf(currentIndex) }

    // Sync animated index with external currentIndex (e.g., when parent resets to 0)
    // Only animate if this is an external change, not from our own gesture
    LaunchedEffect(currentIndex) {
        if (currentIndex != lastAnimatedIndex) {
            lastAnimatedIndex = currentIndex
            animatedIndex.stop()
            animatedIndex.animateTo(currentIndex.toFloat(), cardSpring)
        }
    }

    val hapticFeedback = LocalHapticFeedback.current

    val density = LocalDensity.current
    val thresholdPx = with(density) { FanConfig.swipeThreshold.toPx() }
    val verticalThresholdPx = with(density) { FanConfig.verticalSwipeThreshold.toPx() }
    val directionLockThresholdPx = with(density) { 15.dp.toPx() } // Direction detection threshold
    val fanSpreadPx = with(density) { FanConfig.fanSpread.toPx() }
    val fanYOffsetPx = with(density) { FanConfig.fanYOffset.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .aspectRatio(0.75f)
            .pointerInput(count) {
                detectDragGestures(
                    onDragStart = {
                        // Reset direction lock
                        activeGesture = GestureDirection.None
                    },
                    onDragEnd = {
                        val offsetX = dragOffsetX.value
                        val offsetY = dragOffsetY.value
                        // Get current visual index - use round for accuracy with spring overshoot
                        val visualIndex = animatedIndex.value.roundToInt().let { v ->
                            ((v % count) + count) % count
                        }

                        scope.launch {
                            when (activeGesture) {
                                GestureDirection.Vertical -> {
                                    when {
                                        offsetY < -verticalThresholdPx -> {
                                            // Swipe UP = mark watched
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onMarkWatched(seasons[visualIndex])
                                        }
                                        offsetY > verticalThresholdPx -> {
                                            // Swipe DOWN = delete (show confirmation)
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onDelete()
                                        }
                                    }
                                    // Snap back
                                    launch { dragOffsetX.animateTo(0f, cardSpring) }
                                    launch { dragOffsetY.animateTo(0f, cardSpring) }
                                }
                                GestureDirection.Horizontal -> {
                                    // Swipe RIGHT = next season, Swipe LEFT = prev season
                                    val swipeDirection = when {
                                        offsetX > thresholdPx -> 1    // Swipe right = next
                                        offsetX < -thresholdPx -> -1  // Swipe left = prev
                                        else -> 0
                                    }
                                    val newIndex = (visualIndex + swipeDirection + count) % count

                                    if (newIndex != visualIndex) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        lastAnimatedIndex = newIndex

                                        // Animate in swipe direction (avoids wrap-around cycling through all cards)
                                        val animTarget = animatedIndex.value + swipeDirection
                                        launch {
                                            animatedIndex.animateTo(animTarget, cardSpring)
                                            animatedIndex.snapTo(newIndex.toFloat())
                                        }
                                        onIndexChange(newIndex)
                                    }

                                    // Snap back drag offset
                                    launch { dragOffsetX.animateTo(0f, cardSpring) }
                                    launch { dragOffsetY.animateTo(0f, cardSpring) }
                                }
                                GestureDirection.None -> {
                                    // Snap back
                                    launch { dragOffsetX.animateTo(0f, cardSpring) }
                                    launch { dragOffsetY.animateTo(0f, cardSpring) }
                                }
                            }
                            activeGesture = GestureDirection.None
                        }
                    },
                    onDragCancel = {
                        activeGesture = GestureDirection.None
                        scope.launch {
                            launch { dragOffsetX.animateTo(0f, cardSpring) }
                            launch { dragOffsetY.animateTo(0f, cardSpring) }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            // Detect and lock direction once threshold is exceeded
                            if (activeGesture == GestureDirection.None) {
                                val totalX = abs(dragOffsetX.value + dragAmount.x)
                                val totalY = abs(dragOffsetY.value + dragAmount.y)
                                if (totalX > directionLockThresholdPx || totalY > directionLockThresholdPx) {
                                    // If only one card, disable horizontal swiping (only allow vertical)
                                    activeGesture = if (totalX > totalY && count > 1) {
                                        GestureDirection.Horizontal
                                    } else {
                                        GestureDirection.Vertical
                                    }
                                }
                            }

                            // Only update offset in locked direction
                            // When count == 1, never allow horizontal drag
                            when (activeGesture) {
                                GestureDirection.Horizontal -> {
                                    if (count > 1) {
                                        dragOffsetX.snapTo(dragOffsetX.value + dragAmount.x)
                                    }
                                    // Keep Y at 0
                                }
                                GestureDirection.Vertical -> {
                                    dragOffsetY.snapTo(dragOffsetY.value + dragAmount.y)
                                    // Keep X at 0
                                }
                                GestureDirection.None -> {
                                    // Before locking, track both (but X only if multiple cards)
                                    if (count > 1) {
                                        dragOffsetX.snapTo(dragOffsetX.value + dragAmount.x)
                                    }
                                    dragOffsetY.snapTo(dragOffsetY.value + dragAmount.y)
                                }
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Use animated index for display
        val displayIndex = animatedIndex.value
        val currentDragOffsetX = dragOffsetX.value
        val currentDragOffsetY = dragOffsetY.value
        val isVerticalGesture = activeGesture == GestureDirection.Vertical

        // Sort by z-index and render (back to front)
        seasons.indices
            .sortedBy { index -> calculateZIndex(index, displayIndex, count) }
            .forEach { index ->
                val season = seasons[index]
                val stackPosition = calculateStackPosition(index, displayIndex, count)
                val absPosition = abs(stackPosition)

                // Only render cards within ±2 positions (max 5 visible)
                if (absPosition <= 2.5f) {
                    val isFrontCard = absPosition < 0.5f

                    // Drag progress for front card rotation feedback
                    val dragProgress = if (isFrontCard) (currentDragOffsetX / 200f) else 0f
                    val effectPosition = stackPosition + dragProgress

                    // Scale: 100% → 94% → 88%
                    val scale = maxOf(0.75f, 1f - (FanConfig.scaleStep * absPosition))

                    // X offset: cards fan out, only front card gets drag offset
                    // Clamp position so card edges don't go off screen
                    val maxFanPosition = 1.0f
                    val clampedPosition = stackPosition.coerceIn(-maxFanPosition, maxFanPosition)
                    val xOffset = clampedPosition * fanSpreadPx + (if (isFrontCard) currentDragOffsetX else 0f)

                    // Y offset: 0 → 8 → 16, plus drag offset for front card during vertical swipes
                    val yOffset = absPosition * fanYOffsetPx +
                        if (isFrontCard && isVerticalGesture) currentDragOffsetY else 0f

                    // Rotation: 0° → ±5° → ±10°
                    val rotation = effectPosition * FanConfig.fanRotation

                    // Opacity: 100% → 70% → 40%
                    val alpha = when {
                        absPosition < 0.5f -> 1f
                        absPosition < 1.5f -> 0.7f
                        absPosition < 2.5f -> 0.4f
                        else -> 0.2f
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(calculateZIndex(index, displayIndex, count))
                            .graphicsLayer {
                                translationX = xOffset
                                translationY = yOffset
                                scaleX = scale
                                scaleY = scale
                                rotationZ = rotation
                            }
                            .alpha(alpha)
                            .shadow(
                                elevation = if (isFrontCard) 24.dp else (16 - absPosition.toInt() * 4).coerceAtLeast(4).dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = Color.Black.copy(alpha = 0.3f),
                                spotColor = Color.Black.copy(alpha = 0.3f)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardBackground)
                            .clickable {
                                if (isFrontCard) {
                                    onCardClick(season)
                                }
                            }
                    ) {
                        BingeReadyPosterCard(
                            posterUrl = season.posterUrl,
                            seasonNumber = season.seasonNumber,
                            episodeCount = season.episodesRemaining,
                            isTopCard = isFrontCard
                        )
                    }
                }
            }
    }
}

/**
 * Calculate stack position with wrapping for continuous loop.
 * Uses float for smooth animations.
 */
private fun calculateStackPosition(index: Int, currentIndex: Float, count: Int): Float {
    val rawPosition = index - currentIndex
    val halfCount = count / 2f

    return when {
        rawPosition > halfCount -> rawPosition - count
        rawPosition < -halfCount -> rawPosition + count
        else -> rawPosition
    }
}

/**
 * Calculate z-index for proper stacking order.
 */
private fun calculateZIndex(index: Int, currentIndex: Float, count: Int): Float {
    val position = calculateStackPosition(index, currentIndex, count)
    return if (abs(position) < 0.5f) {
        count.toFloat() * 2
    } else {
        (count - abs(position))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun BingeReadyCardStackPreview() {
    Countdown2BingeTheme {
        var currentIndex by remember { mutableIntStateOf(0) }

        BingeReadyCardStack(
            seasons = listOf(
                BingeReadySeasonData(1, 1, 1, null, "Breaking Bad", 5),
                BingeReadySeasonData(1, 2, 2, null, "Breaking Bad", 8),
                BingeReadySeasonData(1, 3, 3, null, "Breaking Bad", 10),
                BingeReadySeasonData(1, 4, 4, null, "Breaking Bad", 13),
                BingeReadySeasonData(1, 5, 5, null, "Breaking Bad", 16)
            ),
            currentIndex = currentIndex,
            onIndexChange = { currentIndex = it },
            onMarkWatched = {},
            onDelete = {},
            onCardClick = {},
            modifier = Modifier
                .background(Background)
                .padding(vertical = 24.dp)
        )
    }
}
