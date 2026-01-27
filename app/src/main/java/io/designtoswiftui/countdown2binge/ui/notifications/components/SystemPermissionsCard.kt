package io.designtoswiftui.countdown2binge.ui.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted

/**
 * Card showing system notification permission status with enable button.
 */
@Composable
fun SystemPermissionsCard(
    hasPermission: Boolean,
    onEnableClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "System Permissions",
                color = OnBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (hasPermission) "Status: Enabled" else "Status: Not Determined",
                color = OnBackgroundMuted,
                fontSize = 13.sp
            )
        }

        if (!hasPermission) {
            Button(
                onClick = onEnableClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DetailAccent
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "ENABLE",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
