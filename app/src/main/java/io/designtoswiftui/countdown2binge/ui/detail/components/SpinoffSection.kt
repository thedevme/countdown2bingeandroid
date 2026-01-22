package io.designtoswiftui.countdown2binge.ui.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.shared.LandscapeShowCard
import io.designtoswiftui.countdown2binge.ui.theme.Primary
import io.designtoswiftui.countdown2binge.viewmodels.SpinoffShowDisplay

/**
 * Spinoff collection section with premium gating.
 *
 * Design spec:
 * - Section header: "SPINOFF COLLECTION"
 * - Header divider: White line, 15% opacity
 * - Layout: Vertical list
 * - Card gap: 16dp
 *
 * Locked State (non-premium):
 * - Placeholder card height: 100dp
 * - Blur radius: 6dp
 * - Lock icon size: 28dp
 * - Lock icon color: Accent teal
 * - Title: "Unlock Spinoff Collections"
 * - Subtitle: "Discover connected shows and franchise universes"
 * - Button: Teal capsule, "UNLOCK" text
 */
@Composable
fun SpinoffSection(
    spinoffs: List<SpinoffShowDisplay>,
    followedShows: Set<Int>,
    addingShows: Set<Int>,
    isPremium: Boolean,
    isLoading: Boolean,
    onShowClick: (Int) -> Unit,
    onFollowClick: (Int) -> Unit,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section header with divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "SPINOFF COLLECTION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color.White.copy(alpha = 0.15f),
                thickness = 1.dp
            )
        }

        // Content based on premium status
        if (isPremium) {
            // Premium: Show actual spinoff cards
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                spinoffs.forEach { spinoff ->
                    LandscapeShowCard(
                        show = spinoff,
                        isFollowed = followedShows.contains(spinoff.tmdbId),
                        isLoading = addingShows.contains(spinoff.tmdbId),
                        onFollowClick = { onFollowClick(spinoff.tmdbId) },
                        onCardClick = { onShowClick(spinoff.tmdbId) }
                    )
                }
            }
        } else {
            // Non-premium: Show locked state
            SpinoffLockedState(onUnlock = onUnlock)
        }
    }
}

/**
 * Locked state UI for non-premium users.
 */
@Composable
private fun SpinoffLockedState(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Blurred placeholder cards
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Placeholder card 1
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .blur(6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PlaceholderBackground)
                        .border(
                            width = 1.dp,
                            color = PlaceholderBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                )

                // Placeholder card 2
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .blur(6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PlaceholderBackground)
                        .border(
                            width = 1.dp,
                            color = PlaceholderBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }

            // Overlay content
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Lock icon
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Primary,
                    modifier = Modifier.size(28.dp)
                )

                // Title
                Text(
                    text = "Unlock Spinoff Collections",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                // Subtitle
                Text(
                    text = "Discover connected shows and franchise universes",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Unlock button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Primary)
                        .clickable { onUnlock() }
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "UNLOCK",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Color constants
private val PlaceholderBackground = Color(0xFF2A2A2C)
private val PlaceholderBorder = Color(0xFF383839)
