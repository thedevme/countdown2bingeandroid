package io.designtoswiftui.countdown2binge.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.ui.theme.Background
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent
import io.designtoswiftui.countdown2binge.ui.theme.OnBackground
import io.designtoswiftui.countdown2binge.ui.theme.OnBackgroundMuted
import io.designtoswiftui.countdown2binge.viewmodels.SettingsViewModel

/**
 * Screen showing all saved shows.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllShowsScreen(
    onBackClick: () -> Unit,
    onShowClick: (Long) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val savedShows by viewModel.savedShows.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Saved Shows",
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OnBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        },
        containerColor = Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(savedShows) { show ->
                AllShowsRow(
                    show = show,
                    onClick = { onShowClick(show.id) }
                )
                if (show != savedShows.last()) {
                    HorizontalDivider(
                        color = OnBackgroundMuted.copy(alpha = 0.2f),
                        modifier = Modifier.padding(start = 72.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AllShowsRow(
    show: Show,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Poster thumbnail
        AsyncImage(
            model = show.posterPath?.let { "https://image.tmdb.org/t/p/w92$it" },
            contentDescription = show.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(OnBackgroundMuted.copy(alpha = 0.2f))
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Title
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = show.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = OnBackground
            )
            if (show.isSynced) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Synced",
                    fontSize = 12.sp,
                    color = DetailAccent
                )
            }
        }

        // Sync indicator
        if (show.isSynced) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Synced",
                tint = DetailAccent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
