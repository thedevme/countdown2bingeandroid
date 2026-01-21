package io.designtoswiftui.countdown2binge.ui.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.ui.theme.TimelineAccent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Hero card displaying the featured/next airing show with a prominent image.
 */
@Composable
fun HeroCard(
    imageUrl: String?,
    showTitle: String,
    targetDate: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateText = remember(targetDate) {
        targetDate?.let {
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
            when {
                it == today -> "TODAY • ${it.format(formatter).uppercase()}"
                it == today.plusDays(1) -> "TOMORROW • ${it.format(formatter).uppercase()}"
                else -> it.format(formatter).uppercase()
            }
        } ?: ""
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Color.Black.copy(alpha = 0.4f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .clickable(onClick = onClick)
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = showTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
            } else {
                // Placeholder gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    TimelineAccent.copy(alpha = 0.3f),
                                    TimelineAccent.copy(alpha = 0.1f)
                                )
                            )
                        )
                )
            }
        }

        // Date indicator with vertical line
        if (dateText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Vertical line
                Box(
                    modifier = Modifier
                        .size(2.dp, 20.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    TimelineAccent.copy(alpha = 0.6f),
                                    TimelineAccent.copy(alpha = 0.2f)
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date text
                Text(
                    text = dateText,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    ),
                    color = OnBackgroundMuted
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun HeroCardPreview() {
    Countdown2BingeTheme {
        HeroCard(
            imageUrl = null,
            showTitle = "Featured Show",
            targetDate = LocalDate.now(),
            onClick = {},
            modifier = Modifier.background(Background)
        )
    }
}
