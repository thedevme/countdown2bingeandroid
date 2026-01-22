package io.designtoswiftui.countdown2binge.ui.timeline.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme

/**
 * Display mode for the slot machine countdown.
 */
enum class CountdownDisplayMode {
    DAYS,
    EPISODES
}

// Design spec colors
private val UnitLabelColor = Color(0xFF808080)       // White 50%
private val CenterBoxFill = Color(0xFF0D0D0D)        // Dark fill
private val CenterBoxBorder = Color(0xFF252525)      // Subtle border
private val NumberColor = Color.White

// Design spec dimensions
private val CellWidth = 90.dp
private val CellHeight = 90.dp
private val CenterBoxCornerRadius = 12.dp

/**
 * Slot machine style number strip - shows 5 numbers with center highlighted in a box.
 * Header ("FINALE" / "EPISODE IN") is handled by parent CountdownLabel component.
 *
 * Design spec:
 * - 5 visible numbers centered around current value
 * - Center number in dark box (90x100dp, 12dp radius, #0D0D0D fill, #252525 border)
 * - Outer numbers fade via alpha (0.35 adjacent, 0.15 far)
 * - Numbers: 61sp heavy, white
 * - TBD: 51sp heavy, white
 * - Unit: 9sp semibold, #808080, tracking 1.5
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

    // Number strip - each cell positioned relative to center
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(CellHeight)
    ) {
        val screenCenter = maxWidth / 2
        val cellHalf = CellWidth / 2

        // 5 positions: -2, -1, center(0), +1, +2
        listOf(-2, -1, 0, 1, 2).forEach { position ->
            val num = if (displayValue >= 100) {
                when (position) {
                    -2 -> 97
                    -1 -> 98
                    0 -> 100
                    else -> 101
                }
            } else {
                displayValue + position
            }

            val isCenter = position == 0
            val alpha = when (kotlin.math.abs(position)) {
                0 -> 1f
                1 -> 0.35f
                else -> 0.15f
            }

            // Calculate X offset: center of screen minus half cell width plus position offset
            val xOffset = screenCenter - cellHalf + (CellWidth * position)

            if (isCenter) {
                Box(
                    modifier = Modifier
                        .offset(x = xOffset)
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
                    NumberCellContent(
                        number = displayValue,
                        unitLabel = unitLabel,
                        isTbd = displayValue >= 100
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .offset(x = xOffset)
                        .size(CellWidth, CellHeight)
                        .alpha(alpha),
                    contentAlignment = Alignment.Center
                ) {
                    NumberCellContent(
                        number = num,
                        unitLabel = unitLabel,
                        isTbd = num >= 100
                    )
                }
            }
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
            fontSize = if (isTbd || number >= 100) 51.sp else 61.sp,
            fontWeight = FontWeight.Black,
            color = NumberColor,
            textAlign = TextAlign.Center,
            letterSpacing = (-1).sp
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
