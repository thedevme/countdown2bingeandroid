package io.designtoswiftui.countdown2binge.ui.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.BingeReadyAccent

/**
 * Section header with title and optional "See All" link.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    showSeeAll: Boolean = true,
    onSeeAllClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )

        if (showSeeAll && onSeeAllClick != null) {
            Text(
                text = "SEE ALL",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = BingeReadyAccent,
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .clickable { onSeeAllClick() }
                    .padding(vertical = 4.dp)
            )
        }
    }
}
