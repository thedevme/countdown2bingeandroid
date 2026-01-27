package io.designtoswiftui.countdown2binge.ui.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.onboarding.PremiumFeature
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground

/**
 * Row displaying a premium feature with icon and checkmark.
 */
@Composable
fun FeatureRow(
    feature: PremiumFeature,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = feature.icon.toImageVector(),
                contentDescription = null,
                tint = OnBackground,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = feature.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = OnBackground
            )
        }

        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Included",
            tint = DetailAccent,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Map icon string identifiers to Material Icons.
 */
private fun String.toImageVector(): ImageVector {
    return when (this) {
        "infinity" -> Icons.Default.AllInclusive
        "arrow.triangle.2.circlepath" -> Icons.Default.Loop
        "bell.fill" -> Icons.Default.Notifications
        "mic.fill" -> Icons.Default.Mic
        "arrow.left.arrow.right" -> Icons.Default.SwapHoriz
        "sparkles" -> Icons.Default.AutoAwesome
        else -> Icons.Default.Check
    }
}
