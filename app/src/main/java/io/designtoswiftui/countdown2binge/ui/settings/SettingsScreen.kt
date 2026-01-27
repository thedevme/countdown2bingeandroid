package io.designtoswiftui.countdown2binge.ui.settings

import android.content.Intent
import io.designtoswiftui.countdown2binge.BuildConfig
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.models.CountdownDisplayMode
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.services.auth.AuthState
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.SurfaceVariant
import io.designtoswiftui.countdown2binge.viewmodels.SettingsViewModel

/**
 * Settings screen with app preferences matching iOS design.
 */
@Composable
fun SettingsScreen(
    onNotificationsClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val isPremium by viewModel.isPremium.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val savedShows by viewModel.savedShows.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val countdownDisplayMode by viewModel.countdownDisplayMode.collectAsState()
    val includeAiring by viewModel.includeAiring.collectAsState()
    val debugSimulatePremium by viewModel.debugSimulatePremium.collectAsState()
    val debugShowFullOnboarding by viewModel.debugShowFullOnboarding.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Header
        item {
            Text(
                text = "SETTINGS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = OnBackground,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        // Premium Status
        item {
            PremiumStatusRow(isPremium = isPremium)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Account Section
        item {
            SectionHeader(title = "Account")
            AccountRow(authState = authState)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Saved Shows Section (only if signed in and has shows)
        if (authState is AuthState.SignedIn && savedShows.isNotEmpty()) {
            item {
                SavedShowsSection(
                    shows = savedShows,
                    syncedCount = savedShows.count { it.isSynced }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Sound & Haptics
        item {
            SettingsRowWithToggle(
                icon = Icons.Default.MusicNote,
                iconTint = DetailAccent,
                title = "Sound",
                checked = soundEnabled,
                onCheckedChange = viewModel::setSoundEnabled
            )
            SettingsDivider()
            SettingsRowWithToggle(
                icon = Icons.Default.Vibration,
                iconTint = DetailAccent,
                title = "Haptics",
                checked = hapticsEnabled,
                onCheckedChange = viewModel::setHapticsEnabled
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Countdown Display Mode
        item {
            SettingsRowWithSegment(
                icon = Icons.Default.Schedule,
                iconTint = DetailAccent,
                title = "Countdown",
                selectedMode = countdownDisplayMode,
                onModeSelected = viewModel::setCountdownDisplayMode
            )
            SettingsDivider()
        }

        // Show Airing Seasons Toggle
        item {
            SettingsRowWithToggle(
                icon = Icons.Outlined.PlayCircle,
                iconTint = DetailAccent,
                title = "Show airing seasons in Binge Ready",
                subtitle = "Include the current season even if it's still airing",
                checked = includeAiring,
                onCheckedChange = viewModel::setIncludeAiring
            )
            SettingsDivider()
        }

        // Reminders
        item {
            SettingsRowWithChevron(
                icon = Icons.Default.Notifications,
                iconTint = DetailAccent,
                title = "Reminders",
                onClick = onNotificationsClick
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Get in touch Section
        item {
            SectionHeader(title = "Get in touch")
            SettingsRowWithExternalLink(
                icon = Icons.Default.Email,
                iconTint = DetailAccent,
                title = "Email",
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@countdown2binge.com")
                    }
                    context.startActivity(intent)
                }
            )
            SettingsDivider()
            SettingsRowWithExternalLink(
                icon = Icons.Default.Email, // Using email as placeholder for Twitter/X
                iconTint = DetailAccent,
                title = "Twitter",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/countdown2binge"))
                    context.startActivity(intent)
                }
            )
            SettingsDivider()
            SettingsRowWithExternalLink(
                icon = Icons.Default.Star,
                iconTint = DetailAccent,
                title = "Rate on Play Store",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=io.designtoswiftui.countdown2binge"))
                    context.startActivity(intent)
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Other stuff Section
        item {
            SectionHeader(title = "Other stuff")
            SettingsRowWithExternalLink(
                icon = Icons.Outlined.Description,
                iconTint = DetailAccent,
                title = "Privacy policy",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://countdown2binge.com/privacy"))
                    context.startActivity(intent)
                }
            )
            SettingsDivider()
            SettingsRowWithExternalLink(
                icon = Icons.Outlined.Description,
                iconTint = DetailAccent,
                title = "Terms of use",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://countdown2binge.com/terms"))
                    context.startActivity(intent)
                }
            )
            SettingsDivider()
            SettingsRowWithExternalLink(
                icon = Icons.Outlined.Movie,
                iconTint = DetailAccent,
                title = "API provided by themoviedb.org",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.themoviedb.org"))
                    context.startActivity(intent)
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Debug Section - only visible in debug builds
        if (BuildConfig.DEBUG) {
            item {
                SectionHeader(title = "Debug (Dev Only)", color = Color(0xFFE8A838))
                SettingsRowWithToggle(
                    icon = Icons.Default.Star,
                    iconTint = Color(0xFFE8A838),
                    title = "Simulate Premium",
                    subtitle = "Enable to test cloud sync",
                    checked = debugSimulatePremium,
                    onCheckedChange = viewModel::setDebugSimulatePremium
                )
                SettingsDivider()
                SettingsRowWithToggle(
                    icon = Icons.Default.Refresh,
                    iconTint = Color(0xFFE8A838),
                    title = "Show Full Onboarding",
                    checked = debugShowFullOnboarding,
                    onCheckedChange = viewModel::setDebugShowFullOnboarding
                )
                SettingsDivider()
                SettingsRowClickable(
                    icon = Icons.Default.Refresh,
                    iconTint = Color(0xFFE8A838),
                    title = "Reset Onboarding State",
                    onClick = viewModel::resetOnboardingState
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    color: Color = OnBackgroundMuted
) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun PremiumStatusRow(isPremium: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Crown icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DetailAccent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = DetailAccent,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isPremium) "Premium" else "Free",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnBackground
            )
            Text(
                text = if (isPremium) "All features unlocked" else "Limited features",
                fontSize = 14.sp,
                color = OnBackgroundMuted
            )
        }

        if (isPremium) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Premium active",
                tint = DetailAccent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun AccountRow(authState: AuthState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariant)
            .clickable { /* TODO: Manage account */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Person icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DetailAccent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = DetailAccent,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (authState) {
                    is AuthState.SignedIn -> authState.email ?: "Signed In"
                    else -> "Not signed in"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = OnBackground
            )
            Text(
                text = "Manage account",
                fontSize = 14.sp,
                color = OnBackgroundMuted
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = OnBackgroundMuted,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SavedShowsSection(
    shows: List<Show>,
    syncedCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariant)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SAVED SHOWS",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnBackgroundMuted,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Synced: $syncedCount / ${shows.size}",
                fontSize = 12.sp,
                color = OnBackgroundMuted
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Show list (first 3)
        shows.take(3).forEach { show ->
            SavedShowRow(show = show)
            if (show != shows.take(3).last()) {
                HorizontalDivider(
                    color = OnBackgroundMuted.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // View all link
        if (shows.size > 3) {
            HorizontalDivider(
                color = OnBackgroundMuted.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: View all shows */ },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View All Shows",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DetailAccent
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${shows.size}",
                        fontSize = 14.sp,
                        color = OnBackgroundMuted
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = OnBackgroundMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedShowRow(show: Show) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = show.posterPath?.let { "https://image.tmdb.org/t/p/w92$it" },
            contentDescription = show.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(50.dp, 75.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = show.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = OnBackground,
            modifier = Modifier.weight(1f)
        )

        if (show.isSynced) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Synced",
                tint = DetailAccent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsRowWithToggle(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = OnBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = OnBackgroundMuted
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = DetailAccent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = OnBackgroundMuted.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun SettingsRowWithSegment(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    selectedMode: CountdownDisplayMode,
    onModeSelected: (CountdownDisplayMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = OnBackground,
            modifier = Modifier.weight(1f)
        )

        SegmentedControl(
            selectedMode = selectedMode,
            onModeSelected = onModeSelected
        )
    }
}

@Composable
private fun SegmentedControl(
    selectedMode: CountdownDisplayMode,
    onModeSelected: (CountdownDisplayMode) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(2.dp)
    ) {
        SegmentOption(
            text = "Days",
            isSelected = selectedMode == CountdownDisplayMode.DAYS,
            onClick = { onModeSelected(CountdownDisplayMode.DAYS) }
        )
        SegmentOption(
            text = "Episodes",
            isSelected = selectedMode == CountdownDisplayMode.EPISODES,
            onClick = { onModeSelected(CountdownDisplayMode.EPISODES) }
        )
    }
}

@Composable
private fun SegmentOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun SettingsRowWithChevron(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = OnBackground,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = OnBackgroundMuted,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun SettingsRowWithExternalLink(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = OnBackground,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "External link",
            tint = OnBackgroundMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsRowClickable(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = OnBackground
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = OnBackgroundMuted.copy(alpha = 0.2f),
        modifier = Modifier.padding(start = 48.dp)
    )
}
