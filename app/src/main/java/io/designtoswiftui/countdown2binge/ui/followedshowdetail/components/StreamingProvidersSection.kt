package io.designtoswiftui.countdown2binge.ui.followedshowdetail.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBWatchProvider

/**
 * Streaming providers section ("Watch Now") with horizontal scroll of provider logos.
 *
 * Design specs:
 * - Section header: "Watch Now"
 * - Horizontal scroll of provider logos
 * - Logo size: 48dp x 48dp
 * - Spacing between logos: 12dp (md)
 * - Corner radius: 6dp (sm)
 */
@Composable
fun StreamingProvidersSection(
    providers: List<TMDBWatchProvider>,
    showName: String,
    modifier: Modifier = Modifier
) {
    if (providers.isEmpty()) return

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Section header
        Text(
            text = "Watch Now",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal provider logos
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 20.dp)
        ) {
            items(providers) { provider ->
                ProviderLogo(
                    provider = provider,
                    onClick = {
                        // Try to open provider's app or website
                        val deepLinkUrl = getProviderDeepLink(provider.providerId, showName)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUrl))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback to web search if deep link fails
                            val searchUrl = "https://www.google.com/search?q=${showName}+${provider.providerName}"
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl))
                            context.startActivity(webIntent)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ProviderLogo(
    provider: TMDBWatchProvider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = TMDBService.IMAGE_BASE_URL + "w92" + provider.logoPath,
        contentDescription = provider.providerName,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
    )
}

/**
 * Get deep link URL for a streaming provider.
 * Falls back to web search URL if provider not mapped.
 */
private fun getProviderDeepLink(providerId: Int, showName: String): String {
    val encodedName = Uri.encode(showName)

    return when (providerId) {
        // Netflix
        8 -> "https://www.netflix.com/search?q=$encodedName"
        // Amazon Prime Video
        9, 119 -> "https://www.amazon.com/s?k=$encodedName&i=instant-video"
        // Disney+
        337 -> "https://www.disneyplus.com/search?q=$encodedName"
        // Hulu
        15 -> "https://www.hulu.com/search?q=$encodedName"
        // HBO Max / Max
        384, 1899 -> "https://play.max.com/search?q=$encodedName"
        // Apple TV+
        350 -> "https://tv.apple.com/search?term=$encodedName"
        // Peacock
        386, 387 -> "https://www.peacocktv.com/watch/search?q=$encodedName"
        // Paramount+
        531 -> "https://www.paramountplus.com/search/?q=$encodedName"
        // Showtime
        37 -> "https://www.sho.com/search?q=$encodedName"
        // AMC+
        526 -> "https://www.amcplus.com/search?q=$encodedName"
        // Starz
        43 -> "https://www.starz.com/search?q=$encodedName"
        // Crunchyroll
        283 -> "https://www.crunchyroll.com/search?q=$encodedName"
        // fuboTV
        257 -> "https://www.fubo.tv/search?q=$encodedName"
        // Philo
        675 -> "https://www.philo.com/search/$encodedName"
        // Tubi
        73 -> "https://tubitv.com/search/$encodedName"
        // Pluto TV
        300 -> "https://pluto.tv/search/details/$encodedName"
        // Vudu
        7 -> "https://www.vudu.com/content/movies/search?searchString=$encodedName"
        // YouTube TV
        363 -> "https://tv.youtube.com/search?q=$encodedName"
        // Discovery+
        520 -> "https://www.discoveryplus.com/search?q=$encodedName"
        // BritBox
        151 -> "https://www.britbox.com/us/search?q=$encodedName"
        // Default: Google search
        else -> "https://www.google.com/search?q=$encodedName+streaming"
    }
}
