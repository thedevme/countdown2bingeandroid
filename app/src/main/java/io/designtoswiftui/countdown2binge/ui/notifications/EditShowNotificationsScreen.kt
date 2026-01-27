package io.designtoswiftui.countdown2binge.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.models.FinaleReminderTiming
import io.designtoswiftui.countdown2binge.models.NotificationStatus
import io.designtoswiftui.countdown2binge.models.ScheduledNotification
import io.designtoswiftui.countdown2binge.ui.notifications.components.ScheduledAlertRow
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.viewmodels.EditShowNotificationsViewModel

/**
 * Screen for editing per-show notification settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShowNotificationsScreen(
    onBackClick: () -> Unit,
    viewModel: EditShowNotificationsViewModel = hiltViewModel()
) {
    val show by viewModel.show.collectAsState()
    val effectiveSettings by viewModel.effectiveSettings.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val nextNotification by viewModel.nextNotification.collectAsState()
    val scheduledAlerts by viewModel.scheduledAlerts.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showCancelAllDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "EDIT SHOW NOTIFICATIONS",
                    color = OnBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = OnBackground
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Background
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Show Poster
            item {
                show?.let { currentShow ->
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w342${currentShow.posterPath}",
                        contentDescription = currentShow.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 180.dp, height = 270.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            // Show Info
            item {
                show?.let { currentShow ->
                    Text(
                        text = currentShow.title,
                        color = OnBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    nextNotification?.let { notification ->
                        Text(
                            text = "NEXT: ${notification.scheduledDate.month.name.take(3)} ${notification.scheduledDate.dayOfMonth} • ${notification.typeDisplayName()}",
                            color = DetailAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Text(
                        text = "$pendingCount PENDING NOTIFICATIONS",
                        color = OnBackgroundMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Notification Settings Section
            item {
                SectionHeader(title = "NOTIFICATION SETTINGS")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground, RoundedCornerShape(12.dp))
                ) {
                    SettingToggleRow(
                        title = "Season Premiere",
                        checked = effectiveSettings.seasonPremiere,
                        onCheckedChange = viewModel::setSeasonPremiere
                    )

                    SettingDivider()

                    SettingToggleRow(
                        title = "New Episodes",
                        checked = effectiveSettings.newEpisodes,
                        onCheckedChange = viewModel::setNewEpisodes
                    )

                    SettingDivider()

                    SettingToggleRow(
                        title = "Finale Reminder",
                        checked = effectiveSettings.finaleReminder,
                        onCheckedChange = viewModel::setFinaleReminder
                    )

                    // Finale Timing Pills
                    if (effectiveSettings.finaleReminder) {
                        FinaleTimingPills(
                            selectedTiming = effectiveSettings.finaleReminderTiming,
                            onTimingSelect = viewModel::setFinaleReminderTiming
                        )
                    }

                    SettingDivider()

                    SettingToggleRow(
                        title = "Binge Ready",
                        checked = effectiveSettings.bingeReady,
                        onCheckedChange = viewModel::setBingeReady
                    )
                }
            }

            // Quiet Hours Section
            item {
                SectionHeader(title = "QUIET HOURS")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "START",
                                color = OnBackgroundMuted,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = formatTime(effectiveSettings.quietHoursStart),
                                color = OnBackground,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "END",
                                color = OnBackgroundMuted,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = formatTime(effectiveSettings.quietHoursEnd),
                                color = OnBackground,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Alerts will be silenced and queued during this window.",
                        color = OnBackgroundMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Scheduled Alerts Section
            if (scheduledAlerts.isNotEmpty()) {
                item {
                    SectionHeader(title = "SCHEDULED ALERTS")
                }

                items(
                    items = scheduledAlerts,
                    key = { it.id }
                ) { alert ->
                    ScheduledAlertRow(
                        notification = alert,
                        onCancelClick = { viewModel.cancelNotification(alert.id) }
                    )
                }
            }

            // Action Buttons
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "RESET TO GLOBAL DEFAULTS",
                        color = OnBackground,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showCancelAllDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE57373))
                    )
                ) {
                    Text(
                        text = "CANCEL ALL NOTIFICATIONS",
                        color = Color(0xFFE57373),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset to Global Defaults?") },
            text = { Text("This will remove all custom notification settings for this show.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToGlobalDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = DetailAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = OnBackgroundMuted)
                }
            },
            containerColor = CardBackground
        )
    }

    // Cancel All Confirmation Dialog
    if (showCancelAllDialog) {
        AlertDialog(
            onDismissRequest = { showCancelAllDialog = false },
            title = { Text("Cancel All Notifications?") },
            text = { Text("This will cancel all scheduled notifications for this show. You can reschedule them later.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelAllNotifications()
                        showCancelAllDialog = false
                    }
                ) {
                    Text("Cancel All", color = Color(0xFFE57373))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelAllDialog = false }) {
                    Text("Keep", color = OnBackgroundMuted)
                }
            },
            containerColor = CardBackground
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = OnBackgroundMuted,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
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

@Composable
private fun FinaleTimingPills(
    selectedTiming: FinaleReminderTiming,
    onTimingSelect: (FinaleReminderTiming) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FinaleReminderTiming.entries.forEach { timing ->
            val isSelected = timing == selectedTiming
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) DetailAccent else Color.Transparent
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) DetailAccent else OnBackgroundMuted.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onTimingSelect(timing) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (timing) {
                        FinaleReminderTiming.DAY_OF -> "DAY OF"
                        FinaleReminderTiming.ONE_DAY_BEFORE -> "1 DAY BEFORE"
                        FinaleReminderTiming.TWO_DAYS_BEFORE -> "2 DAYS"
                        FinaleReminderTiming.ONE_WEEK_BEFORE -> "1 WEEK"
                    },
                    color = if (isSelected) Color.White else OnBackgroundMuted,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

private fun formatTime(minutesFromMidnight: Int): String {
    val hours = minutesFromMidnight / 60
    val minutes = minutesFromMidnight % 60
    val period = if (hours >= 12) "PM" else "AM"
    val displayHour = when {
        hours == 0 -> 12
        hours > 12 -> hours - 12
        else -> hours
    }
    return "${displayHour}:${minutes.toString().padStart(2, '0')} $period"
}
