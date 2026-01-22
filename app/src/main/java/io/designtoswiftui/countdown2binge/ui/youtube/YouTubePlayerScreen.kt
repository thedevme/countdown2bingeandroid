package io.designtoswiftui.countdown2binge.ui.youtube

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

/**
 * Fullscreen YouTube player screen.
 *
 * iOS Design (from YouTubePlayerFullscreen):
 * - Forces landscape orientation on appear
 * - Returns to portrait on dismiss
 * - Auto-plays the video
 * - Shows YouTube's native controls
 * - Close button: 32x32pt circle, black 60% opacity, top-left corner
 * - Close button position: 50pt from top, 24pt from leading
 *
 * @param videoKey The YouTube video ID (e.g., "dQw4w9WgXcQ")
 * @param videoTitle The title of the video for accessibility
 * @param onDismiss Callback when the player should be dismissed
 */
@Composable
fun YouTubePlayerScreen(
    videoKey: String,
    videoTitle: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Force landscape orientation on appear
    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        onDispose {
            // Return to portrait on dismiss
            activity?.requestedOrientation = originalOrientation
                ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Remember the YouTube player view
    val youTubePlayerView = remember {
        YouTubePlayerView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    // Lifecycle management for the player
    DisposableEffect(youTubePlayerView) {
        onDispose {
            youTubePlayerView.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // YouTube Player (WebView-based)
        AndroidView(
            factory = { ctx ->
                youTubePlayerView.apply {
                    // Configure player options
                    val options = IFramePlayerOptions.Builder()
                        .controls(1) // Show controls
                        .fullscreen(0) // We handle fullscreen ourselves
                        .autoplay(1) // Auto-play
                        .rel(0) // Don't show related videos
                        .build()

                    // Initialize with listener
                    initialize(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            // Load and play the video
                            youTubePlayer.loadVideo(videoKey, 0f)
                        }
                    }, options)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Close button (top-left corner)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 50.dp, start = 24.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
