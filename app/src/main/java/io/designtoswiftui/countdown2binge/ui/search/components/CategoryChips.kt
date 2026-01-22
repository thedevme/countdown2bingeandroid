package io.designtoswiftui.countdown2binge.ui.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.models.ShowCategory

/**
 * Horizontal scrolling category chips for browsing by genre.
 */
@Composable
fun CategoryChips(
    categories: List<ShowCategory> = ShowCategory.entries,
    onCategoryClick: (ShowCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { category ->
            CategoryChip(
                category = category,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

/**
 * Individual category chip.
 */
@Composable
private fun CategoryChip(
    category: ShowCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(ChipBackground)
            .border(
                width = 1.dp,
                color = ChipBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.displayName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

// Chip colors (matching iOS spec)
private val ChipBackground = Color(0xFF27272A)
private val ChipBorder = Color(0xFF323234)
