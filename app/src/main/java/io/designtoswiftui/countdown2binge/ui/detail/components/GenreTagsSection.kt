package io.designtoswiftui.countdown2binge.ui.detail.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent

/**
 * Horizontal genre tags section for the detail screen.
 *
 * iOS Design:
 * - Max tags displayed: 3
 * - Tag style: Teal outline (transparent bg, teal border, teal text)
 * - Tag horizontal padding: 12dp
 * - Tag vertical padding: 6dp
 * - Tag corner radius: 16dp (pill shape)
 * - Gap between tags: 8dp
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenreTagsSection(
    genres: List<String>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        genres.take(3).forEach { genre ->
            DetailGenreTag(text = genre.uppercase())
        }
    }
}

/**
 * Individual genre tag for the detail screen.
 * Teal outline style (transparent bg, teal border, teal text).
 */
@Composable
private fun DetailGenreTag(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = DetailAccent,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = DetailAccent,
            letterSpacing = 0.5.sp
        )
    }
}
