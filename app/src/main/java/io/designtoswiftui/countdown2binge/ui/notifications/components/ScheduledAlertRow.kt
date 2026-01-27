package io.designtoswiftui.countdown2binge.ui.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.models.NotificationStatus
import io.designtoswiftui.countdown2binge.models.NotificationType
import io.designtoswiftui.countdown2binge.models.ScheduledNotification
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Row displaying a scheduled notification alert.
 */
@Composable
fun ScheduledAlertRow(
    notification: ScheduledNotification,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Show Poster
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w92${notification.posterPath}",
            contentDescription = notification.showName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        // Alert Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Type Badge
            NotificationTypeBadge(type = notification.type)

            // Show Name
            Text(
                text = notification.showName,
                color = OnBackground,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Episode Info
            notification.episodeInfo()?.let { info ->
                Text(
                    text = info,
                    color = OnBackgroundMuted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Date and Cancel
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = notification.scheduledDate.format(
                    DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                ).uppercase(),
                color = OnBackgroundMuted,
                fontSize = 12.sp
            )
            Text(
                text = notification.scheduledDate.format(
                    DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
                ),
                color = OnBackgroundMuted,
                fontSize = 12.sp
            )

            // Cancel button (only for pending notifications)
            if (notification.status in listOf(
                    NotificationStatus.PENDING,
                    NotificationStatus.SCHEDULED,
                    NotificationStatus.QUEUED
                )
            ) {
                IconButton(
                    onClick = onCancelClick,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = OnBackgroundMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationTypeBadge(
    type: NotificationType,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (type) {
        NotificationType.PREMIERE -> "SEASON PREMIERE" to DetailAccent
        NotificationType.NEW_EPISODE -> "NEW EPISODE" to Color(0xFF4CAF50)
        NotificationType.FINALE_REMINDER -> "SEASON FINALE" to Color(0xFFFF9800)
        NotificationType.BINGE_READY -> "BINGE READY" to Color(0xFF9C27B0)
    }

    Text(
        text = text,
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = modifier
    )
}
