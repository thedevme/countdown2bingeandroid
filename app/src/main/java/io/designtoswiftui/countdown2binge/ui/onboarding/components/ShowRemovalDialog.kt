package io.designtoswiftui.countdown2binge.ui.onboarding.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.ui.onboarding.OnboardingUiState
import io.designtoswiftui.countdown2binge.ui.onboarding.ShowSummary
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.SurfaceVariant

/**
 * Full-screen review selection screen for reviewing and removing shows.
 * Shows when user needs to reduce their selection for free tier.
 */
@Composable
fun ShowRemovalDialog(
    selectedShows: Map<Int, ShowSummary>,
    showsToRemove: Int,
    onRemoveShow: (Int) -> Unit,
    onConfirm: () -> Unit,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canConfirm = selectedShows.size <= OnboardingUiState.FREE_SHOW_LIMIT
    val remainingToRemove = (selectedShows.size - OnboardingUiState.FREE_SHOW_LIMIT).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Review Selection",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )

                // Show count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DetailAccent)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${selectedShows.size} SHOWS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }

        // Limit warning (only show if over limit)
        if (!canConfirm) {
            Text(
                text = "Free accounts are limited to ${OnboardingUiState.FREE_SHOW_LIMIT} shows. Remove $remainingToRemove to continue.",
                fontSize = 14.sp,
                color = OnBackgroundMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
            )
        }

        HorizontalDivider(
            color = SurfaceVariant,
            thickness = 1.dp
        )

        // Show list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            items(selectedShows.values.toList()) { show ->
                ReviewShowRow(
                    show = show,
                    onRemove = { onRemoveShow(show.tmdbId) }
                )
            }
        }

        // Bottom section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Background)
                .padding(20.dp)
        ) {
            // Confirm button
            Button(
                onClick = onConfirm,
                enabled = canConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DetailAccent,
                    contentColor = Color.Black,
                    disabledContainerColor = DetailAccent.copy(alpha = 0.3f),
                    disabledContentColor = Color.Black.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (canConfirm) "CONFIRM SELECTION" else "REMOVE $remainingToRemove TO CONTINUE",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add more / Upgrade option
            Text(
                text = if (canConfirm) "ADD MORE" else "UPGRADE FOR UNLIMITED",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnBackgroundMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (canConfirm) {
                            onDismiss()
                        } else {
                            onUpgrade()
                        }
                    }
                    .padding(vertical = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReviewShowRow(
    show: ShowSummary,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariant)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Poster thumbnail
            AsyncImage(
                model = show.posterPath?.let { "https://image.tmdb.org/t/p/w185$it" },
                contentDescription = show.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(60.dp)
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            // Show info
            Column {
                Text(
                    text = show.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground
                )

                // Network info (if available)
                val subtitle = buildString {
                    if (show.genres.isNotEmpty()) {
                        append(show.genres.first())
                    }
                    if (show.networkName != null) {
                        if (isNotEmpty()) append(" • ")
                        append(show.networkName)
                    }
                }
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = OnBackgroundMuted
                    )
                }
            }
        }

        // Remove button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(OnBackgroundMuted.copy(alpha = 0.3f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Remove",
                tint = OnBackgroundMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
