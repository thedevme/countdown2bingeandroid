package io.designtoswiftui.countdown2binge.ui.notifications

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.countdown2binge.ui.notifications.components.GlobalDefaultsSection
import io.designtoswiftui.countdown2binge.ui.notifications.components.NextScheduledCard
import io.designtoswiftui.countdown2binge.ui.notifications.components.NotificationSummaryCards
import io.designtoswiftui.countdown2binge.ui.notifications.components.PremiumAccessCard
import io.designtoswiftui.countdown2binge.ui.notifications.components.ScheduledAlertsSection
import io.designtoswiftui.countdown2binge.ui.notifications.components.ShowManagementRow
import io.designtoswiftui.countdown2binge.ui.notifications.components.SystemPermissionsCard
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.viewmodels.NotificationsViewModel

/**
 * Main Notifications Hub screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsHubScreen(
    onBackClick: () -> Unit,
    onShowClick: (Long) -> Unit,
    onUnlockPremiumClick: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val hasPermission by viewModel.hasPermission.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val typeBreakdown by viewModel.typeBreakdown.collectAsState()
    val globalSettings by viewModel.globalSettings.collectAsState()
    val nextScheduled by viewModel.nextScheduled.collectAsState()
    val followedShows by viewModel.followedShows.collectAsState()
    val scheduledAlerts by viewModel.scheduledAlerts.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.refreshPermissionStatus()
    }

    // Refresh permission status on resume
    LaunchedEffect(Unit) {
        viewModel.refreshPermissionStatus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Notifications",
                    color = OnBackground,
                    fontWeight = FontWeight.SemiBold
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
            actions = {
                IconButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Notification Settings",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // System Permissions Card
            item {
                SystemPermissionsCard(
                    hasPermission = hasPermission,
                    onEnableClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        }
                    }
                )
            }

            // Premium Access Card
            item {
                PremiumAccessCard(
                    isPremium = isPremium,
                    onUnlockClick = onUnlockPremiumClick
                )
            }

            // Summary Cards
            item {
                NotificationSummaryCards(
                    pendingCount = pendingCount,
                    typeBreakdown = typeBreakdown
                )
            }

            // Next Scheduled Card
            item {
                NextScheduledCard(notification = nextScheduled)
            }

            // Global Defaults Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                GlobalDefaultsSection(
                    settings = globalSettings,
                    onSeasonPremiereChange = viewModel::setSeasonPremiere,
                    onNewEpisodesChange = viewModel::setNewEpisodes,
                    onFinaleReminderChange = viewModel::setFinaleReminder,
                    onBingeReadyChange = viewModel::setBingeReady
                )
            }

            // Show Management Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SHOW MANAGEMENT",
                        color = OnBackgroundMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "VIEW ALL",
                        color = DetailAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { /* TODO: View all shows */ }
                    )
                }
            }

            // Show List (limited to 3)
            items(
                items = followedShows.take(3),
                key = { it.show.id }
            ) { showWithNotification ->
                ShowManagementRow(
                    showWithNotification = showWithNotification,
                    onClick = { onShowClick(showWithNotification.show.id) }
                )
            }

            // Scheduled Alerts Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                ScheduledAlertsSection(
                    alerts = scheduledAlerts,
                    selectedFilter = selectedFilter,
                    onFilterSelect = viewModel::setSelectedFilter,
                    onCancelAlert = viewModel::cancelNotification
                )
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
