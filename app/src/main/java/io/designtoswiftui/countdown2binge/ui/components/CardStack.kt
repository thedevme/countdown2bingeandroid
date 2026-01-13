package io.designtoswiftui.countdown2binge.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Direction of card swipe
 */
enum class SwipeDirection {
    Left,
    Right
}

/**
 * A Tinder-style card stack component.
 * Cards are stacked on top of each other, and the user can swipe
 * left or right to dismiss the top card.
 *
 * @param items The list of items to display as cards
 * @param onSwipeLeft Called when a card is swiped left (skip/dismiss)
 * @param onSwipeRight Called when a card is swiped right (mark watched/accept)
 * @param onEmpty Called when all cards have been swiped
 * @param cardContent The composable content for each card
 */
@Composable
fun <T> CardStack(
    items: List<T>,
    onSwipeLeft: (T) -> Unit,
    onSwipeRight: (T) -> Unit,
    onEmpty: () -> Unit = {},
    modifier: Modifier = Modifier,
    cardContent: @Composable (T) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val swipeThreshold = screenWidthPx * 0.4f

    // Track which cards have been swiped
    var currentIndex by remember { mutableIntStateOf(0) }

    // Show up to 3 cards in the stack
    val visibleCards = items.drop(currentIndex).take(3)

    if (visibleCards.isEmpty()) {
        LaunchedEffect(Unit) {
            onEmpty()
        }
        return
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Render cards in reverse order (bottom to top)
        visibleCards.reversed().forEachIndexed { reversedIndex, item ->
            val stackIndex = visibleCards.size - 1 - reversedIndex
            val isTopCard = stackIndex == 0

            key(item.hashCode()) {
                StackCard(
                    item = item,
                    stackIndex = stackIndex,
                    isTopCard = isTopCard,
                    swipeThreshold = swipeThreshold,
                    screenWidthPx = screenWidthPx,
                    onSwipeLeft = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSwipeLeft(item)
                        currentIndex++
                    },
                    onSwipeRight = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSwipeRight(item)
                        currentIndex++
                    },
                    onDragThresholdReached = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    cardContent = cardContent
                )
            }
        }
    }
}

@Composable
private fun <T> StackCard(
    item: T,
    stackIndex: Int,
    isTopCard: Boolean,
    swipeThreshold: Float,
    screenWidthPx: Float,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onDragThresholdReached: () -> Unit,
    cardContent: @Composable (T) -> Unit
) {
    val scope = rememberCoroutineScope()

    // Animation values for the top card
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    // Calculate visual properties based on stack position
    val scale = 1f - (stackIndex * 0.05f)
    val yOffset = stackIndex * 16

    // Rotation based on drag (only for top card)
    val rotation = if (isTopCard) (offsetX.value / screenWidthPx) * 15f else 0f

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.value.roundToInt(), yOffset + offsetY.value.roundToInt()) }
            .scale(scale)
            .graphicsLayer {
                rotationZ = rotation
            }
            .then(
                if (isTopCard) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    when {
                                        offsetX.value > swipeThreshold -> {
                                            // Swipe right - animate off screen
                                            offsetX.animateTo(
                                                targetValue = screenWidthPx * 1.5f,
                                                animationSpec = tween(300)
                                            )
                                            onSwipeRight()
                                        }
                                        offsetX.value < -swipeThreshold -> {
                                            // Swipe left - animate off screen
                                            offsetX.animateTo(
                                                targetValue = -screenWidthPx * 1.5f,
                                                animationSpec = tween(300)
                                            )
                                            onSwipeLeft()
                                        }
                                        else -> {
                                            // Snap back to center
                                            launch {
                                                offsetX.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessLow
                                                    )
                                                )
                                            }
                                            launch {
                                                offsetY.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessLow
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    hasTriggeredHaptic = false
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                    offsetY.snapTo(offsetY.value + dragAmount.y * 0.3f)
                                }

                                // Trigger haptic when threshold is reached
                                if (!hasTriggeredHaptic && abs(offsetX.value) > swipeThreshold) {
                                    hasTriggeredHaptic = true
                                    onDragThresholdReached()
                                } else if (hasTriggeredHaptic && abs(offsetX.value) < swipeThreshold * 0.8f) {
                                    hasTriggeredHaptic = false
                                }
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp)
    ) {
        cardContent(item)
    }
}
