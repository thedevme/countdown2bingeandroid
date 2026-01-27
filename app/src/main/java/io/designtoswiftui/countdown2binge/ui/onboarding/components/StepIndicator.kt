package io.designtoswiftui.countdown2binge.ui.onboarding.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle

/**
 * Step indicator showing progress through onboarding.
 * Displays "STEP X OF Y" text and dots for each step.
 */
@Composable
fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Step text
        Text(
            text = "STEP $currentStep OF $totalSteps",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = OnBackgroundSubtle,
            letterSpacing = 1.5.sp
        )

        // Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { index ->
                val stepNumber = index + 1
                StepDot(
                    isActive = stepNumber <= currentStep,
                    isCurrent = stepNumber == currentStep
                )
            }
        }
    }
}

@Composable
private fun StepDot(
    isActive: Boolean,
    isCurrent: Boolean,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = when {
            isCurrent -> DetailAccent
            isActive -> DetailAccent.copy(alpha = 0.5f)
            else -> OnBackgroundSubtle.copy(alpha = 0.3f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "dotColor"
    )

    val size = if (isCurrent) 8.dp else 6.dp

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}
