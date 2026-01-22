package io.designtoswiftui.countdown2binge.ui.bingeready.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.ui.theme.CardBackground
import io.designtoswiftui.countdown2binge.ui.theme.Countdown2BingeTheme

/**
 * Data class for a show thumbnail in the selector.
 */
data class ShowThumbnailData(
    val id: Long,
    val posterUrl: String?,
    val title: String
)

/**
 * Horizontal show selector with thumbnails.
 *
 * Features:
 * - Horizontal scroll with snap behavior
 * - Selected show has teal border ring
 * - Non-selected shows are dimmed (opacity 0.5)
 * - Thumbnail size: 48×72pt (1.5:1 ratio matching poster cards)
 * - Auto-scrolls to selected show
 */
@Composable
fun ShowSelector(
    shows: List<ShowThumbnailData>,
    selectedIndex: Int,
    onShowSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to selected show
    LaunchedEffect(selectedIndex) {
        if (shows.isNotEmpty() && selectedIndex in shows.indices) {
            listState.animateScrollToItem(
                index = selectedIndex,
                scrollOffset = -200 // Center offset approximation
            )
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        itemsIndexed(
            items = shows,
            key = { _, show -> show.id }
        ) { index, show ->
            val isSelected = index == selectedIndex

            ShowThumbnail(
                show = show,
                isSelected = isSelected,
                onClick = { onShowSelected(index) }
            )
        }
    }
}

/**
 * Individual show thumbnail.
 */
@Composable
private fun ShowThumbnail(
    show: ShowThumbnailData,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Thumbnail dimensions: 56×84 (per iOS spec)
    val thumbnailWidth = 56.dp
    val thumbnailHeight = 84.dp

    Box(
        modifier = modifier
            .width(thumbnailWidth)
            .height(thumbnailHeight)
            .alpha(if (isSelected) 1f else 0.6f)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(8.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
    ) {
        if (show.posterUrl != null) {
            AsyncImage(
                model = show.posterUrl,
                contentDescription = show.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(thumbnailWidth)
                    .height(thumbnailHeight)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ShowSelectorPreview() {
    Countdown2BingeTheme {
        ShowSelector(
            shows = listOf(
                ShowThumbnailData(1, null, "Breaking Bad"),
                ShowThumbnailData(2, null, "Better Call Saul"),
                ShowThumbnailData(3, null, "The Wire"),
                ShowThumbnailData(4, null, "The Sopranos"),
                ShowThumbnailData(5, null, "Mad Men")
            ),
            selectedIndex = 1,
            onShowSelected = {},
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ShowSelectorSinglePreview() {
    Countdown2BingeTheme {
        ShowSelector(
            shows = listOf(
                ShowThumbnailData(1, null, "Breaking Bad")
            ),
            selectedIndex = 0,
            onShowSelected = {},
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}
