package io.designtoswiftui.countdown2binge.ui.timeline.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle

/**
 * Display mode for the slot machine countdown.
 */
enum class CountdownDisplayMode {
    DAYS,
    EPISODES
}

/**
 * Slot machine style countdown showing a horizontal picker with the current value centered.
 * Shows surrounding values faded out to create a slot machine effect.
 *
 * @param value The current countdown value (days or episodes)
 * @param mode Whether to display as "DAYS" or "EPS"
 */
@Composable
fun SlotMachineDaysPicker(
    value: Int?,
    mode: CountdownDisplayMode = CountdownDisplayMode.DAYS,
    modifier: Modifier = Modifier
) {
    val displayValue = value ?: 0
    val label = when (mode) {
        CountdownDisplayMode.DAYS -> "DAYS"
        CountdownDisplayMode.EPISODES -> "EPS"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Show 5 numbers: current+2, current+1, current, current-1, current-2
        val numbers = if (value != null && value > 0) {
            listOf(
                displayValue + 2,
                displayValue + 1,
                displayValue,
                displayValue - 1,
                displayValue - 2
            )
        } else {
            listOf(null, null, null, null, null)
        }

        numbers.forEachIndexed { index, number ->
            val isCenter = index == 2
            val distanceFromCenter = kotlin.math.abs(index - 2)

            val alpha = when (distanceFromCenter) {
                0 -> 1f
                1 -> 0.35f
                else -> 0.15f
            }

            SlotNumber(
                number = number,
                label = label,
                isHighlighted = isCenter,
                alpha = alpha,
                modifier = Modifier.width(if (isCenter) 68.dp else 52.dp)
            )
        }
    }
}

@Composable
private fun SlotNumber(
    number: Int?,
    label: String,
    isHighlighted: Boolean,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (isHighlighted) {
                        Modifier
                            .background(
                                color = Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 10.dp)
                    } else {
                        Modifier.padding(horizontal = 4.dp, vertical = 10.dp)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = number,
                transitionSpec = {
                    slideInVertically { -it } togetherWith slideOutVertically { it }
                },
                label = "slot_number"
            ) { targetNumber ->
                Text(
                    text = targetNumber?.toString()?.padStart(2, '0') ?: "--",
                    fontSize = if (isHighlighted) 38.sp else 28.sp,
                    fontWeight = FontWeight.Black,
                    color = OnBackground,
                    textAlign = TextAlign.Center
                )
            }
        }

        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlighted) OnBackgroundMuted else OnBackgroundSubtle,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/**
 * Simple days display for backward compatibility.
 */
@Composable
fun SlotMachineDaysPicker(
    days: Int?,
    modifier: Modifier = Modifier
) {
    SlotMachineDaysPicker(
        value = days,
        mode = CountdownDisplayMode.DAYS,
        modifier = modifier
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun SlotMachineDaysPickerPreview() {
    Countdown2BingeTheme {
        SlotMachineDaysPicker(
            value = 15,
            mode = CountdownDisplayMode.DAYS,
            modifier = Modifier.background(Background)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun SlotMachineEpisodesPickerPreview() {
    Countdown2BingeTheme {
        SlotMachineDaysPicker(
            value = 8,
            mode = CountdownDisplayMode.EPISODES,
            modifier = Modifier.background(Background)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun SlotMachineEmptyPreview() {
    Countdown2BingeTheme {
        SlotMachineDaysPicker(
            value = null,
            mode = CountdownDisplayMode.DAYS,
            modifier = Modifier.background(Background)
        )
    }
}
