package io.designtoswiftui.countdown2binge.ui.timeline.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Data class for a show card in the stack.
 */
data class StackedShowData(
    val id: Long,
    val posterUrl: String?,
    val title: String
)

// Fan configuration matching iOS exactly
private object FanConfig {
    val fanSpread = 28.dp       // Horizontal distance between cards
    val fanYOffset = 8.dp       // Vertical drop per position
    const val fanRotation = 5f  // Rotation degrees per position
    const val scaleStep = 0.06f // Scale reduction per position (6%)
    val swipeThreshold = 100.dp // Threshold to trigger card change
}

// Spring matching iOS: interpolatingSpring(stiffness: 300, damping: 40)
// Lower damping ratio = more overshoot/bounce
private val cardSpring = spring<Float>(
    stiffness = 300f,
    dampingRatio = 0.65f // Allows overshoot then settle
)

/**
 * A continuous swiping card stack with looping.
 * Cards wrap around infinitely - swiping past the last card goes to the first.
 */
@Composable
fun StackedShowCards(
    shows: List<StackedShowData>,
    onShowClick: (Long) -> Unit,
    onPageChanged: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (shows.isEmpty()) return

    val count = shows.size
    var currentIndex by remember { mutableIntStateOf(0) }
    var draggedCardIndex by remember { mutableIntStateOf(0) } // Track which card is being dragged

    // Animated values for smooth transitions
    val animatedIndex = remember { Animatable(0f) }
    val dragOffset = remember { Animatable(0f) }

    val scope = rememberCoroutineScope()

    val density = LocalDensity.current
    val thresholdPx = with(density) { FanConfig.swipeThreshold.toPx() }
    val fanSpreadPx = with(density) { FanConfig.fanSpread.toPx() }
    val fanYOffsetPx = with(density) { FanConfig.fanYOffset.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .aspectRatio(0.75f)
            .pointerInput(count) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        // Remember which card we're dragging
                        draggedCardIndex = currentIndex
                    },
                    onDragEnd = {
                        val offset = dragOffset.value
                        scope.launch {
                            val swipeDirection = when {
                                offset < -thresholdPx -> 1   // Swipe left = next
                                offset > thresholdPx -> -1   // Swipe right = prev
                                else -> 0                    // No change
                            }

                            val newIndex = (currentIndex + swipeDirection + count) % count

                            if (newIndex != currentIndex) {
                                currentIndex = newIndex
                                onPageChanged?.invoke(currentIndex)
                            }

                            // Animate in swipe direction (avoids 3→0 going through 2,1)
                            // Instead of animating to newIndex directly, animate by direction
                            // e.g., from 3 swipe left: animate to 4.0, then snap to 0.0
                            val animTarget = animatedIndex.value + swipeDirection

                            launch {
                                animatedIndex.animateTo(animTarget, cardSpring)
                                // Snap to actual index after animation (keeps value in 0..count-1 range)
                                animatedIndex.snapTo(newIndex.toFloat())
                            }
                            launch { dragOffset.animateTo(0f, cardSpring) }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            dragOffset.animateTo(0f, cardSpring)
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            dragOffset.snapTo(dragOffset.value + dragAmount)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Use animated index for display
        val displayIndex = animatedIndex.value
        val currentDragOffset = dragOffset.value

        // Sort by z-index and render (back to front)
        shows.indices
            .sortedBy { index -> calculateZIndex(index, displayIndex, count) }
            .forEach { index ->
                val show = shows[index]
                val stackPosition = calculateStackPosition(index, displayIndex, count)
                val absPosition = abs(stackPosition)

                // Only render cards within ±2 positions
                if (absPosition <= 2.5f) {
                    val isFrontCard = absPosition < 0.5f
                    // Apply drag offset only to the card that was being dragged
                    val isDraggedCard = index == draggedCardIndex

                    // Drag progress for dragged card rotation feedback
                    val dragProgress = if (isDraggedCard) (currentDragOffset / 200f) else 0f
                    val effectPosition = stackPosition + dragProgress

                    // Scale: 100% → 94% → 88%
                    val scale = maxOf(0.75f, 1f - (FanConfig.scaleStep * absPosition))

                    // X offset: cards fan out, only dragged card gets drag offset
                    // Clamp position so card edges don't go off screen
                    val maxFanPosition = 1.0f
                    val clampedPosition = stackPosition.coerceIn(-maxFanPosition, maxFanPosition)
                    val xOffset = clampedPosition * fanSpreadPx + (if (isDraggedCard) currentDragOffset else 0f)

                    // Y offset: 0 → 8 → 16
                    val yOffset = absPosition * fanYOffsetPx

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
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (isFrontCard) {
                                    onShowClick(show.id)
                                }
                            }
                    ) {
                        if (show.posterUrl != null) {
                            AsyncImage(
                                model = show.posterUrl,
                                contentDescription = show.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
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
private fun StackedShowCardsPreview() {
    Countdown2BingeTheme {
        StackedShowCards(
            shows = listOf(
                StackedShowData(1, null, "Show 1"),
                StackedShowData(2, null, "Show 2"),
                StackedShowData(3, null, "Show 3"),
                StackedShowData(4, null, "Show 4"),
                StackedShowData(5, null, "Show 5")
            ),
            onShowClick = {},
            modifier = Modifier
                .background(Background)
                .padding(vertical = 24.dp)
        )
    }
}
