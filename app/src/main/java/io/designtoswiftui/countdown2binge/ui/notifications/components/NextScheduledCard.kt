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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.models.ScheduledNotification
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Card showing the next scheduled notification.
 */
@Composable
fun NextScheduledCard(
    notification: ScheduledNotification?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NEXT SCHEDULED",
                color = OnBackgroundMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )

            if (notification != null) {
                Text(
                    text = notification.scheduledDate.format(
                        DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                    ).uppercase(),
                    color = DetailAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (notification != null) {
            Text(
                text = "${notification.showName} • ${notification.typeDisplayName()}",
                color = OnBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            Text(
                text = "No upcoming notifications",
                color = OnBackgroundMuted,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
