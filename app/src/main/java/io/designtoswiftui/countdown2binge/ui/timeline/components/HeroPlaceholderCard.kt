package io.designtoswiftui.countdown2binge.ui.timeline.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.R
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundSubtle

/**
 * Hero placeholder card shown when no shows are currently airing.
 * Matches iOS design: dark rounded card with TV icon and "No Shows Airing" text.
 */
@Composable
fun HeroPlaceholderCard(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dark rounded card (matches iOS)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f) // Portrait aspect ratio like iOS
                .clip(RoundedCornerShape(28.dp))
                .background(CardBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // White logo
                Image(
                    painter = painterResource(id = R.drawable.app_icon_wht),
                    contentDescription = "No Shows",
                    modifier = Modifier.size(80.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // "No Shows Airing" text
                Text(
                    text = "No Shows Airing",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = OnBackgroundSubtle
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F12)
@Composable
private fun HeroPlaceholderCardPreview() {
    Countdown2BingeTheme {
        HeroPlaceholderCard(
            modifier = Modifier.background(Background)
        )
    }
}
