package io.designtoswiftui.countdown2binge.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.OnPrimary
import io.designtoswiftui.countdown2binge.ui.theme.Primary
import io.designtoswiftui.countdown2binge.ui.theme.StateBingeReady

/**
 * A toggle button for adding/removing shows from the followed list.
 * Shows "+ Add" when not added, "Added ✓" when added, with loading state support.
 */
@Composable
fun AddButton(
    isAdded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val buttonShape = RoundedCornerShape(24.dp)

    AnimatedContent(
        targetState = isAdded,
        transitionSpec = {
            (fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.95f))
                .togetherWith(fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.95f))
        },
        label = "add_button_transition"
    ) { added ->
        if (added) {
            // Added state - filled button
            Button(
                onClick = onClick,
                modifier = modifier.height(40.dp),
                enabled = enabled && !isLoading,
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StateBingeReady.copy(alpha = 0.15f),
                    contentColor = StateBingeReady,
                    disabledContainerColor = StateBingeReady.copy(alpha = 0.1f),
                    disabledContentColor = StateBingeReady.copy(alpha = 0.5f)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = StateBingeReady,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "✓",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Added",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        } else {
            // Not added state - outline button
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.height(40.dp),
                enabled = enabled && !isLoading,
                shape = buttonShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Primary,
                    disabledContentColor = Primary.copy(alpha = 0.5f)
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (enabled && !isLoading) Primary.copy(alpha = 0.6f) else Primary.copy(alpha = 0.3f)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "+",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

/**
 * Compact icon-only version of add button.
 */
@Composable
fun AddButtonCompact(
    isAdded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isAdded) StateBingeReady.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(200),
        label = "compact_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isAdded) StateBingeReady else Primary,
        animationSpec = tween(200),
        label = "compact_content"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isAdded) Color.Transparent else Primary.copy(alpha = 0.6f),
        animationSpec = tween(200),
        label = "compact_border"
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.size(36.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.5.dp, borderColor),
        contentPadding = PaddingValues(0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = if (isAdded) "✓" else "+",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun AddButtonPreview() {
    Countdown2BingeTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AddButton(isAdded = false, onClick = {})
            AddButton(isAdded = true, onClick = {})
            AddButton(isAdded = false, onClick = {}, isLoading = true)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun AddButtonCompactPreview() {
    Countdown2BingeTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AddButtonCompact(isAdded = false, onClick = {})
            AddButtonCompact(isAdded = true, onClick = {})
            AddButtonCompact(isAdded = false, onClick = {}, isLoading = true)
        }
    }
}
