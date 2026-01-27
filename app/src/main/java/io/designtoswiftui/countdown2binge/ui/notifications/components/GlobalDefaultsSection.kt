package io.designtoswiftui.countdown2binge.ui.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.models.NotificationSettings
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted

/**
 * Section showing global notification default toggles.
 */
@Composable
fun GlobalDefaultsSection(
    settings: NotificationSettings,
    onSeasonPremiereChange: (Boolean) -> Unit,
    onNewEpisodesChange: (Boolean) -> Unit,
    onFinaleReminderChange: (Boolean) -> Unit,
    onBingeReadyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "GLOBAL DEFAULTS",
            color = OnBackgroundMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground, RoundedCornerShape(12.dp))
        ) {
            SettingToggleRow(
                title = "Season Premiere",
                checked = settings.seasonPremiere,
                onCheckedChange = onSeasonPremiereChange
            )

            SettingDivider()

            SettingToggleRow(
                title = "New Episodes",
                checked = settings.newEpisodes,
                onCheckedChange = onNewEpisodesChange
            )

            SettingDivider()

            SettingToggleRow(
                title = "Finale Reminder",
                checked = settings.finaleReminder,
                onCheckedChange = onFinaleReminderChange
            )

            SettingDivider()

            SettingToggleRow(
                title = "Binge Ready",
                checked = settings.bingeReady,
                onCheckedChange = onBingeReadyChange
            )
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = OnBackground,
            fontSize = 16.sp
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = DetailAccent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp),
        color = Color.White.copy(alpha = 0.1f),
        thickness = 0.5.dp
    )
}
