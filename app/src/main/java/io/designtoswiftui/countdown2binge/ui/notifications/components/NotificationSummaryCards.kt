package io.designtoswiftui.countdown2binge.ui.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.models.NotificationType
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted

/**
 * Summary cards showing pending count and type breakdown.
 */
@Composable
fun NotificationSummaryCards(
    pendingCount: Int,
    typeBreakdown: Map<NotificationType, Int>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pending Count Card
        Column(
            modifier = Modifier
                .weight(1f)
                .background(CardBackground, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "PENDING",
                color = OnBackgroundMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Text(
                text = pendingCount.toString(),
                color = OnBackground,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Type Breakdown Card
        Column(
            modifier = Modifier
                .weight(1f)
                .background(CardBackground, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "TYPE BREAKDOWN",
                color = OnBackgroundMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Text(
                text = formatTypeBreakdown(typeBreakdown),
                color = OnBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatTypeBreakdown(breakdown: Map<NotificationType, Int>): String {
    val parts = mutableListOf<String>()

    breakdown[NotificationType.PREMIERE]?.let { count ->
        if (count > 0) parts.add("${count}P")
    }
    breakdown[NotificationType.FINALE_REMINDER]?.let { count ->
        if (count > 0) parts.add("${count}F")
    }
    breakdown[NotificationType.NEW_EPISODE]?.let { count ->
        if (count > 0) parts.add("${count}E")
    }
    breakdown[NotificationType.BINGE_READY]?.let { count ->
        if (count > 0) parts.add("${count}B")
    }

    return if (parts.isEmpty()) "—" else parts.joinToString(" • ")
}
