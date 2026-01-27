package io.designtoswiftui.countdown2binge.ui.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.models.ScheduledNotification
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.viewmodels.ShowWithNextNotification
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Row displaying a show with its next notification info.
 */
@Composable
fun ShowManagementRow(
    showWithNotification: ShowWithNextNotification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Show Poster
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w92${showWithNotification.show.posterPath}",
            contentDescription = showWithNotification.show.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        // Show Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = showWithNotification.show.title,
                color = OnBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val nextInfo = formatNextNotification(showWithNotification.nextNotification)
            Text(
                text = nextInfo,
                color = if (showWithNotification.nextNotification != null) DetailAccent else OnBackgroundMuted,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Chevron
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Edit",
            tint = OnBackgroundMuted,
            modifier = Modifier.size(24.dp)
        )
    }
}

private fun formatNextNotification(notification: ScheduledNotification?): String {
    if (notification == null) return "All caught up"

    val date = notification.scheduledDate.format(
        DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    )
    val type = when {
        notification.typeDisplayName().contains("PREMIERE") -> "Premiere"
        notification.typeDisplayName().contains("FINALE") -> "Finale"
        notification.typeDisplayName().contains("EPISODE") -> "New Ep"
        notification.typeDisplayName().contains("BINGE") -> "Binge Ready"
        else -> "Alert"
    }

    return "Next: $date • $type"
}
