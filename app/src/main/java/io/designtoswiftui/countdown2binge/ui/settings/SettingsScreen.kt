package io.designtoswiftui.countdown2binge.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.countdown2binge.models.CountdownDisplayMode
import io.designtoswiftui.countdown2binge.ui.theme.Primary
import io.designtoswiftui.countdown2binge.viewmodels.SettingsViewModel

/**
 * Settings screen with app preferences.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val includeAiring by viewModel.includeAiring.collectAsState()
    val countdownDisplayMode by viewModel.countdownDisplayMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Text(
            text = "SETTINGS",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = (-0.5).sp,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 24.dp)
        )

        // Include Airing Toggle
        SettingsSection(title = "Binge Ready") {
            SettingsToggleRow(
                title = "Show airing seasons in Binge Ready",
                description = "Include the current season even if it's still airing",
                checked = includeAiring,
                onCheckedChange = { viewModel.setIncludeAiring(it) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Countdown Display Mode
        SettingsSection(title = "Timeline") {
            SettingsSegmentedRow(
                title = "Countdown",
                selectedMode = countdownDisplayMode,
                onModeSelected = { viewModel.setCountdownDisplayMode(it) }
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.05f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Primary,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
        }
        Text(
            text = description,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.6f),
            lineHeight = 18.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun SettingsSegmentedRow(
    title: String,
    selectedMode: CountdownDisplayMode,
    onModeSelected: (CountdownDisplayMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.05f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Clock icon
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )

        // Title
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        )

        // Segmented control
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
    val segmentShape = RoundedCornerShape(8.dp)

    Row(
        modifier = Modifier
            .clip(segmentShape)
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(2.dp)
    ) {
        // Days option
        SegmentOption(
            text = "Days",
            isSelected = selectedMode == CountdownDisplayMode.DAYS,
            onClick = { onModeSelected(CountdownDisplayMode.DAYS) }
        )

        // Episodes option
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
            .background(
                if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent
            )
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

