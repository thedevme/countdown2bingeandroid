package io.designtoswiftui.countdown2binge.ui.timeline.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.models.CountdownDisplayMode
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import kotlin.math.roundToInt

// Design spec colors
private val UnitLabelColor = Color(0xFF808080)       // White 50%
private val CenterBoxFill = Color(0xFF0D0D0D)        // Dark fill
private val CenterBoxBorder = Color(0xFF252525)      // Subtle border
private val NumberColor = Color.White

// Design spec dimensions
private val CellWidth = 90.dp
private val CellHeight = 90.dp
private val CenterBoxCornerRadius = 12.dp

// Animation spec
private const val ANIMATION_DURATION_MS = 400

/**
 * Slot machine style countdown with horizontal scroll animation.
 *
 * When the value changes, the number strip scrolls horizontally:
 * - Value increases → strip moves LEFT (higher numbers scroll in from right)
 * - Value decreases → strip moves RIGHT (lower numbers scroll in from left)
 *
 * The center box stays fixed while numbers scroll behind it.
 *
 * @param value The current countdown value (days or episodes), null for TBD
 * @param mode Whether to display as "DAYS" or "EPS"
 */
@Composable
fun SlotMachineCountdown(
    value: Int?,
    mode: CountdownDisplayMode = CountdownDisplayMode.DAYS,
    modifier: Modifier = Modifier
) {
    val unitLabel = when (mode) {
        CountdownDisplayMode.DAYS -> "DAYS"
        CountdownDisplayMode.EPISODES -> "EPS"
    }

    // Calculate display value (0-99 or 100 for TBD)
    val displayValue = when {
        value == null -> 100
        value < 0 -> 100
        value > 99 -> 100
        else -> value
    }

    // Animate the position (0-100 range)
    val animatedPosition by animateFloatAsState(
        targetValue = displayValue.toFloat(),
        animationSpec = tween(
            durationMillis = ANIMATION_DURATION_MS,
            easing = androidx.compose.animation.core.EaseOut
        ),
        label = "slotMachineScroll"
    )

    val density = LocalDensity.current
    val cellWidthPx = with(density) { CellWidth.toPx() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(CellHeight)
    ) {
        val screenCenterPx = with(density) { (maxWidth / 2).toPx() }
        val cellHalfPx = cellWidthPx / 2

        // Calculate which numbers are visible (render extra for smooth animation)
        val centerNumber = animatedPosition.roundToInt()
        val visibleRange = (centerNumber - 3)..(centerNumber + 3)

        // Render each visible number cell
        visibleRange.forEach { number ->
            // Skip invalid numbers
            if (number < 0 || number > 100) return@forEach

            // Calculate position based on animated value
            // When animatedPosition = number, that number should be in center
            val offsetFromCenter = number - animatedPosition
            val xOffsetPx = screenCenterPx - cellHalfPx + (offsetFromCenter * cellWidthPx)
            val xOffset = with(density) { xOffsetPx.toDp() }

            // Calculate alpha based on distance from center
            val distanceFromCenter = kotlin.math.abs(offsetFromCenter)
            val alpha = when {
                distanceFromCenter < 0.5f -> 1f
                distanceFromCenter < 1.5f -> 0.35f
                else -> 0.15f
            }

            // Render cell (non-center cells only - center box is overlaid)
            if (distanceFromCenter > 0.1f) {
                Box(
                    modifier = Modifier
                        .offset(x = xOffset)
                        .size(CellWidth, CellHeight)
                        .alpha(alpha),
                    contentAlignment = Alignment.Center
                ) {
                    NumberCellContent(
                        number = number,
                        unitLabel = unitLabel,
                        isTbd = number >= 100
                    )
                }
            }
        }

        // Static center box overlay (always shows current animated value)
        val centerXOffset = with(density) { (screenCenterPx - cellHalfPx).toDp() }
        Box(
            modifier = Modifier
                .offset(x = centerXOffset)
                .size(CellWidth, CellHeight)
                .background(
                    color = CenterBoxFill,
                    shape = RoundedCornerShape(CenterBoxCornerRadius)
                )
                .border(
                    width = 1.dp,
                    color = CenterBoxBorder,
                    shape = RoundedCornerShape(CenterBoxCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Show the number closest to center during animation
            val centerDisplayNumber = animatedPosition.roundToInt().coerceIn(0, 100)
            NumberCellContent(
                number = centerDisplayNumber,
                unitLabel = unitLabel,
                isTbd = centerDisplayNumber >= 100
            )
        }
    }
}

/**
 * Content of a number cell (number + unit label).
 */
@Composable
private fun NumberCellContent(
    number: Int,
    unitLabel: String,
    isTbd: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.offset(y = 4.dp)
    ) {
        Text(
            text = when {
                isTbd || number >= 100 -> "TBD"
                number < 0 -> ""
                else -> String.format("%02d", number)
            },
            fontSize = if (isTbd || number >= 100) 36.sp else 61.sp,
            fontWeight = FontWeight.Black,
            color = NumberColor,
            textAlign = TextAlign.Center,
            letterSpacing = if (isTbd || number >= 100) 0.sp else (-1).sp
        )

        if (number >= 0) {
            Text(
                text = unitLabel,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = UnitLabelColor,
                letterSpacing = 1.5.sp,
                modifier = Modifier.offset(y = (-12).dp)
            )
        }
    }
}

/**
 * Backward compatible function name.
 */
@Composable
fun SlotMachineDaysPicker(
    value: Int?,
    mode: CountdownDisplayMode = CountdownDisplayMode.DAYS,
    modifier: Modifier = Modifier
) {
    SlotMachineCountdown(
        value = value,
        mode = mode,
        modifier = modifier
    )
}

/**
 * Simple days display for backward compatibility.
 */
@Composable
fun SlotMachineDaysPicker(
    days: Int?,
    modifier: Modifier = Modifier
) {
    SlotMachineCountdown(
        value = days,
        mode = CountdownDisplayMode.DAYS,
        modifier = modifier
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SlotMachineCountdownPreview() {
    Countdown2BingeTheme {
        SlotMachineCountdown(
            value = 50,
            mode = CountdownDisplayMode.DAYS,
            modifier = Modifier.background(Background)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SlotMachineCountdownEpisodesPreview() {
    Countdown2BingeTheme {
        SlotMachineCountdown(
            value = 8,
            mode = CountdownDisplayMode.EPISODES,
            modifier = Modifier.background(Background)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SlotMachineCountdownTbdPreview() {
    Countdown2BingeTheme {
        SlotMachineCountdown(
            value = null,
            mode = CountdownDisplayMode.DAYS,
            modifier = Modifier.background(Background)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SlotMachineCountdownLowValuePreview() {
    Countdown2BingeTheme {
        SlotMachineCountdown(
            value = 1,
            mode = CountdownDisplayMode.DAYS,
            modifier = Modifier.background(Background)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SlotMachineCountdownHighValuePreview() {
    Countdown2BingeTheme {
        SlotMachineCountdown(
            value = 99,
            mode = CountdownDisplayMode.DAYS,
            modifier = Modifier.background(Background)
        )
    }
}
