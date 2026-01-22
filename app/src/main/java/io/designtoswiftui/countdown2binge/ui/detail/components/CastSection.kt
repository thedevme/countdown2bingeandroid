package io.designtoswiftui.countdown2binge.ui.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBCastMember

/**
 * Horizontal cast section.
 *
 * Design spec:
 * - Section header: "CAST & CREW"
 * - Scroll direction: Horizontal
 * - Max items: 10
 * - Card gap: 16dp
 */
@Composable
fun CastSection(
    cast: List<TMDBCastMember>,
    modifier: Modifier = Modifier
) {
    if (cast.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section header
        Text(
            text = "CAST & CREW",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Horizontal cast list
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cast.take(10)) { member ->
                PersonCard(castMember = member)
            }
        }
    }
}
