package io.designtoswiftui.countdown2binge.ui.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/**
 * Network badge overlay for show cards.
 * Displays streaming platform (HBO, Netflix, etc.)
 *
 * Design spec:
 * - Font: 10sp Bold (700)
 * - Letter spacing: 0.5sp
 * - Text color: rgba(255, 255, 255, 0.7)
 * - Text transform: UPPERCASE
 * - Horizontal padding: 8dp
 * - Vertical padding: 5dp
 * - Background: #2D2D2F
 * - Border: #383839, 1dp
 * - Corner radius: 6dp
 * - Position: Top-right, 10dp inset (Trending) / 12dp inset (Airing)
 */
@Composable
fun NetworkBadge(
    showId: Int,
    modifier: Modifier = Modifier
) {
    val networkName = getNetworkForShow(showId)

    Box(
        modifier = modifier
            .background(
                color = NetworkBadgeBackground,
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 1.dp,
                color = NetworkBadgeBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text(
            text = networkName,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f),
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Get network name for a show.
 * Currently a placeholder based on show ID.
 * TODO: Use actual network data from TMDB when available.
 */
private fun getNetworkForShow(showId: Int): String {
    val networks = listOf("HBO", "NETFLIX", "APPLE TV+", "HULU", "PRIME", "MAX")
    return networks[abs(showId) % networks.size]
}

// Network badge colors (matching iOS spec)
private val NetworkBadgeBackground = Color(0xFF2D2D2F)
private val NetworkBadgeBorder = Color(0xFF383839)
