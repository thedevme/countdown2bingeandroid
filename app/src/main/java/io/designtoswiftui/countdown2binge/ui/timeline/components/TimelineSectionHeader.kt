package io.designtoswiftui.countdown2binge.ui.timeline.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.AnticipatedAccent
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.PremieringSoonAccent

/**
 * Section style determines the accent color scheme.
 */
enum class TimelineSectionStyle {
    PREMIERING_SOON,
    ANTICIPATED
}

/**
 * Section header for timeline categories (Premiering Soon, Anticipated).
 * Features:
 * - Count badge (11pt bold, accent color, centered in 80pt zone)
 * - Title (12pt semibold, white @ 50%, letter spacing 1.5)
 * - Rotating chevron (points right when expanded, down when collapsed)
 *
 * @param title Section title (displayed uppercase)
 * @param count Total count to display in badge
 * @param style Section style determines accent color
 * @param isExpanded Whether the section is expanded
 * @param onToggle Called when header is tapped to toggle expand/collapse
 */
@Composable
fun TimelineSectionHeader(
    title: String,
    count: Int,
    style: TimelineSectionStyle = TimelineSectionStyle.PREMIERING_SOON,
    isExpanded: Boolean = true,
    onToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val accentColor = when (style) {
        TimelineSectionStyle.PREMIERING_SOON -> PremieringSoonAccent
        TimelineSectionStyle.ANTICIPATED -> AnticipatedAccent
    }

    // Chevron rotation: 0° when expanded (points right), 90° when collapsed (points down)
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 0f else 90f,
        animationSpec = tween(durationMillis = 250),
        label = "chevronRotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Count badge (centered in 80dp zone for timeline alignment)
        Box(
            modifier = Modifier.width(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
        }

        // Section title
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.5f),
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        // Chevron (rotates on expand/collapse)
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = Color.White.copy(alpha = 0.4f),
            modifier = Modifier
                .size(20.dp)
                .rotate(chevronRotation)
                .padding(end = 24.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun TimelineSectionHeaderExpandedPreview() {
    Countdown2BingeTheme {
        TimelineSectionHeader(
            title = "Premiering Soon",
            count = 3,
            style = TimelineSectionStyle.PREMIERING_SOON,
            isExpanded = true,
            modifier = Modifier.background(Background)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun TimelineSectionHeaderCollapsedPreview() {
    Countdown2BingeTheme {
        TimelineSectionHeader(
            title = "Premiering Soon",
            count = 3,
            style = TimelineSectionStyle.PREMIERING_SOON,
            isExpanded = false,
            modifier = Modifier.background(Background)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun TimelineSectionHeaderAnticipatedPreview() {
    Countdown2BingeTheme {
        TimelineSectionHeader(
            title = "Anticipated",
            count = 5,
            style = TimelineSectionStyle.ANTICIPATED,
            isExpanded = true,
            modifier = Modifier.background(Background)
        )
    }
}
