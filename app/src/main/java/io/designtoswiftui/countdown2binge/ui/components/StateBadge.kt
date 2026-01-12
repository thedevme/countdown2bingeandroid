package io.designtoswiftui.countdown2binge.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.models.SeasonState
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.StateAiring
import io.designtoswiftui.countdown2binge.ui.theme.StateAnticipated
import io.designtoswiftui.countdown2binge.ui.theme.StateBingeReady
import io.designtoswiftui.countdown2binge.ui.theme.StatePremieringFrom
import io.designtoswiftui.countdown2binge.ui.theme.StatePremieringTo
import io.designtoswiftui.countdown2binge.ui.theme.StateWatched

/**
 * A pill-shaped badge displaying the current state of a season.
 * Uses muted, elegant colors appropriate for the dark theme.
 */
@Composable
fun StateBadge(
    state: SeasonState,
    modifier: Modifier = Modifier,
    showDot: Boolean = true
) {
    val backgroundColor by animateColorAsState(
        targetValue = state.backgroundColor,
        animationSpec = tween(300),
        label = "badge_bg"
    )

    val textColor by animateColorAsState(
        targetValue = state.textColor,
        animationSpec = tween(300),
        label = "badge_text"
    )

    val isGradient = state == SeasonState.PREMIERING

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .then(
                if (isGradient) {
                    Modifier.background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                StatePremieringFrom.copy(alpha = 0.2f),
                                StatePremieringTo.copy(alpha = 0.2f)
                            )
                        )
                    )
                } else {
                    Modifier.background(backgroundColor.copy(alpha = 0.15f))
                }
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (showDot) {
                // Status indicator dot
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .then(
                            if (isGradient) {
                                Modifier.background(
                                    Brush.horizontalGradient(
                                        colors = listOf(StatePremieringFrom, StatePremieringTo)
                                    )
                                )
                            } else {
                                Modifier.background(backgroundColor)
                            }
                        )
                )
            }

            Text(
                text = state.displayName,
                color = if (isGradient) StatePremieringFrom else textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Extension properties for SeasonState to get visual styling.
 */
val SeasonState.backgroundColor: Color
    get() = when (this) {
        SeasonState.ANTICIPATED -> StateAnticipated
        SeasonState.PREMIERING -> StatePremieringFrom
        SeasonState.AIRING -> StateAiring
        SeasonState.BINGE_READY -> StateBingeReady
        SeasonState.WATCHED -> StateWatched
    }

val SeasonState.textColor: Color
    get() = when (this) {
        SeasonState.ANTICIPATED -> StateAnticipated
        SeasonState.PREMIERING -> StatePremieringFrom
        SeasonState.AIRING -> StateAiring
        SeasonState.BINGE_READY -> StateBingeReady
        SeasonState.WATCHED -> StateWatched
    }

val SeasonState.displayName: String
    get() = when (this) {
        SeasonState.ANTICIPATED -> "ANTICIPATED"
        SeasonState.PREMIERING -> "PREMIERING"
        SeasonState.AIRING -> "AIRING"
        SeasonState.BINGE_READY -> "BINGE READY"
        SeasonState.WATCHED -> "WATCHED"
    }

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun StateBadgePreview() {
    Countdown2BingeTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StateBadge(state = SeasonState.ANTICIPATED)
            StateBadge(state = SeasonState.PREMIERING)
            StateBadge(state = SeasonState.AIRING)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun StateBadgeAllStatesPreview() {
    Countdown2BingeTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StateBadge(state = SeasonState.BINGE_READY)
            StateBadge(state = SeasonState.WATCHED)
        }
    }
}
