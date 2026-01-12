package io.designtoswiftui.countdown2binge.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.Primary

/**
 * Displays a countdown value with a label (e.g., "14 DAYS" or "8 EPISODES").
 * Uses a bold number with a lighter label for visual hierarchy.
 */
@Composable
fun CountdownText(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
    size: CountdownSize = CountdownSize.Medium,
    accentColor: androidx.compose.ui.graphics.Color = Primary
) {
    val text = buildAnnotatedString {
        withStyle(
            SpanStyle(
                color = accentColor,
                fontSize = size.numberSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default
            )
        ) {
            append(value.toString())
        }
        withStyle(
            SpanStyle(
                color = OnBackgroundMuted,
                fontSize = size.labelSize,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        ) {
            append(" $label")
        }
    }

    Text(
        text = text,
        modifier = modifier
    )
}

/**
 * Stacked countdown display with the number above the label.
 */
@Composable
fun CountdownTextStacked(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
    size: CountdownSize = CountdownSize.Medium,
    accentColor: androidx.compose.ui.graphics.Color = Primary
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            color = accentColor,
            fontSize = size.numberSize,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default
        )
        Text(
            text = label,
            color = OnBackgroundMuted,
            fontSize = size.labelSize,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
    }
}

/**
 * Countdown display for days until premiere/finale.
 */
@Composable
fun DaysCountdown(
    days: Int,
    modifier: Modifier = Modifier,
    size: CountdownSize = CountdownSize.Medium,
    accentColor: androidx.compose.ui.graphics.Color = Primary
) {
    CountdownText(
        value = days,
        label = if (days == 1) "DAY" else "DAYS",
        modifier = modifier,
        size = size,
        accentColor = accentColor
    )
}

/**
 * Countdown display for episodes remaining.
 */
@Composable
fun EpisodesCountdown(
    episodes: Int,
    modifier: Modifier = Modifier,
    size: CountdownSize = CountdownSize.Medium,
    accentColor: androidx.compose.ui.graphics.Color = Primary
) {
    CountdownText(
        value = episodes,
        label = if (episodes == 1) "EPISODE" else "EPISODES",
        modifier = modifier,
        size = size,
        accentColor = accentColor
    )
}

/**
 * Size variants for countdown text.
 */
enum class CountdownSize(
    val numberSize: TextUnit,
    val labelSize: TextUnit
) {
    Small(numberSize = 16.sp, labelSize = 10.sp),
    Medium(numberSize = 24.sp, labelSize = 12.sp),
    Large(numberSize = 36.sp, labelSize = 14.sp),
    Hero(numberSize = 56.sp, labelSize = 16.sp)
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun CountdownTextPreview() {
    Countdown2BingeTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DaysCountdown(days = 14, size = CountdownSize.Small)
            DaysCountdown(days = 7, size = CountdownSize.Medium)
            DaysCountdown(days = 3, size = CountdownSize.Large)
            DaysCountdown(days = 1, size = CountdownSize.Hero)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun CountdownTextStackedPreview() {
    Countdown2BingeTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            CountdownTextStacked(value = 14, label = "DAYS")
            CountdownTextStacked(value = 8, label = "EPISODES")
        }
    }
}
