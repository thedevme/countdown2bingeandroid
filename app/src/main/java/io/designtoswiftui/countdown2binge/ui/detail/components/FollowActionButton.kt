package io.designtoswiftui.countdown2binge.ui.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent

/**
 * Full-width follow/following button for the detail screen.
 *
 * iOS Design (from screenshots detail1.png and detail5.png):
 * - Height: 48dp
 * - Width: Full width
 * - Corner radius: 8dp
 * - FOLLOW: Teal background (#4AC7B8)
 * - FOLLOWING: Dark gray background (#2A2A2C)
 * - Icon: bookmark.fill when followed, bookmark outline when not
 * - Text: "FOLLOW" / "FOLLOWING"
 */
@Composable
fun FollowActionButton(
    isFollowed: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Teal for FOLLOW, dark gray for FOLLOWING (matching iOS design)
    val backgroundColor = if (isFollowed) FollowedBackground else DetailAccent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(enabled = !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isFollowed) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (isFollowed) "Following" else "Follow",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isFollowed) "FOLLOWING" else "FOLLOW",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// Color constants
private val FollowedBackground = Color(0xFF2A2A2C)
