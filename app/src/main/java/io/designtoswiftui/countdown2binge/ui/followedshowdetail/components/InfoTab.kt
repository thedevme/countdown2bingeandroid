package io.designtoswiftui.countdown2binge.ui.followedshowdetail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBCastMember
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBVideo
import io.designtoswiftui.countdown2binge.ui.detail.components.CastSection
import io.designtoswiftui.countdown2binge.ui.detail.components.GenreTagsSection
import io.designtoswiftui.countdown2binge.ui.detail.components.InfoSection
import io.designtoswiftui.countdown2binge.ui.detail.components.TechnicalSpecsSection
import io.designtoswiftui.countdown2binge.ui.detail.components.TrailersSection

/**
 * Info tab content container.
 *
 * Reuses existing detail components:
 * 1. InfoSection (expandable synopsis + metadata)
 * 2. GenreTagsSection (max 3 genre tags)
 * 3. TrailersSection (horizontal video scroll)
 * 4. CastSection (horizontal cast scroll)
 * 5. TechnicalSpecsSection (badges + info rows)
 */
@Composable
fun InfoTab(
    synopsis: String?,
    seasonCount: Int,
    statusText: String,
    isSynopsisExpanded: Boolean,
    onSynopsisExpandClick: () -> Unit,
    genres: List<String>,
    videos: List<TMDBVideo>,
    onVideoClick: (TMDBVideo) -> Unit,
    cast: List<TMDBCastMember>,
    createdBy: List<String>,
    network: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 1. InfoSection (synopsis + metadata)
        InfoSection(
            synopsis = synopsis,
            seasonCount = seasonCount,
            statusText = statusText,
            isExpanded = isSynopsisExpanded,
            onExpandClick = onSynopsisExpandClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. GenreTagsSection
        if (genres.isNotEmpty()) {
            GenreTagsSection(genres = genres)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 3. TrailersSection
        if (videos.isNotEmpty()) {
            TrailersSection(
                videos = videos,
                onVideoClick = onVideoClick
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 4. CastSection
        if (cast.isNotEmpty()) {
            CastSection(cast = cast)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 5. TechnicalSpecsSection
        TechnicalSpecsSection(
            createdBy = createdBy,
            genres = genres,
            network = network
        )
    }
}
