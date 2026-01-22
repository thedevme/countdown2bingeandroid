package io.designtoswiftui.countdown2binge.ui.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Genre tag chip for displaying show genres.
 * Used in TrendingShowCard and AiringShowCard.
 *
 * Design spec:
 * - Font: 11sp Medium (500)
 * - Text color: rgba(255, 255, 255, 0.7)
 * - Height: 20dp fixed
 * - Horizontal padding: 10dp
 * - Background: #2D2D2F
 * - Border: #383839, 1dp
 * - Corner radius: 6dp
 * - Max 2 tags, 6dp gap
 */
@Composable
fun GenreTag(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(20.dp)
            .background(
                color = GenreTagBackground,
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 1.dp,
                color = GenreTagBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

// Genre tag colors (matching iOS spec)
private val GenreTagBackground = Color(0xFF2D2D2F)
private val GenreTagBorder = Color(0xFF383839)
