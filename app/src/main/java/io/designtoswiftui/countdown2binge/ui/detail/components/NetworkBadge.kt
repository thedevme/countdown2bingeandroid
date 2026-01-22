package io.designtoswiftui.countdown2binge.ui.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/**
 * Network badge for show cards (HBO, MAX, NETFLIX, etc.).
 *
 * iOS Design:
 * - Small capsule badge
 * - Dark background (#2D2D2F)
 * - Gray border (#383839)
 * - White text at 70% opacity
 * - Used in top-right corner of poster cards
 */
@Composable
fun NetworkBadge(
    network: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(BadgeBackground)
            .border(
                width = 1.dp,
                color = BadgeBorder,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = network.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Get a placeholder network based on show ID.
 * Used when we don't want to make extra API calls for network info.
 */
fun getPlaceholderNetwork(showId: Int): String {
    val networks = listOf("HBO", "NETFLIX", "APPLE TV+", "HULU", "PRIME", "MAX")
    return networks[abs(showId) % networks.size]
}

// Color constants
private val BadgeBackground = Color(0xFF2D2D2F)
private val BadgeBorder = Color(0xFF383839)
