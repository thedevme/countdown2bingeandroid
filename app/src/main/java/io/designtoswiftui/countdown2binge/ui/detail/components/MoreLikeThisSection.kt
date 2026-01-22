package io.designtoswiftui.countdown2binge.ui.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBSearchResult
import io.designtoswiftui.countdown2binge.ui.shared.PortraitShowCard

/**
 * 2-column recommendations grid section.
 *
 * Design spec:
 * - Section header: "MORE LIKE THIS"
 * - Layout: 2-column grid
 * - Max items: 4
 * - Card gap: 12dp
 */
@Composable
fun MoreLikeThisSection(
    recommendations: List<TMDBSearchResult>,
    followedShows: Set<Int>,
    addingShows: Set<Int>,
    onShowClick: (Int) -> Unit,
    onFollowClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (recommendations.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section header
        Text(
            text = "MORE LIKE THIS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )

        // 2-column grid
        val rows = recommendations.take(4).chunked(2)
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { show ->
                    val isFollowed = followedShows.contains(show.id)
                    PortraitShowCard(
                        show = show,
                        isFollowed = isFollowed,
                        isLoading = addingShows.contains(show.id),
                        onFollowClick = { onFollowClick(show.id) },
                        onCardClick = { onShowClick(show.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty space if odd number
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
