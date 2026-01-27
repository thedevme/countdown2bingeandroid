package io.designtoswiftui.countdown2binge.ui.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.models.AlertFilter
import io.designtoswiftui.countdown2binge.models.ScheduledNotification
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted

/**
 * Section showing scheduled alerts with filter tabs.
 */
@Composable
fun ScheduledAlertsSection(
    alerts: List<ScheduledNotification>,
    selectedFilter: AlertFilter,
    onFilterSelect: (AlertFilter) -> Unit,
    onCancelAlert: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "SCHEDULED ALERTS",
            color = OnBackgroundMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Filter Tabs
        AlertFilterTabs(
            selectedFilter = selectedFilter,
            onFilterSelect = onFilterSelect,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Alert List
        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (selectedFilter) {
                        AlertFilter.PENDING -> "No pending notifications"
                        AlertFilter.DELIVERED -> "No delivered notifications"
                        AlertFilter.CANCELLED -> "No cancelled notifications"
                    },
                    color = OnBackgroundMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                alerts.forEach { alert ->
                    ScheduledAlertRow(
                        notification = alert,
                        onCancelClick = { onCancelAlert(alert.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertFilterTabs(
    selectedFilter: AlertFilter,
    onFilterSelect: (AlertFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AlertFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) DetailAccent else Color.Transparent
                    )
                    .clickable { onFilterSelect(filter) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter.name,
                    color = if (isSelected) Color.White else OnBackgroundMuted,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
