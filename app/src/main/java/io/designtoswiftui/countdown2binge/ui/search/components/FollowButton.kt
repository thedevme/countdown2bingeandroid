package io.designtoswiftui.countdown2binge.ui.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Follow/Following button used in show cards.
 * Supports loading state with spinner.
 *
 * Design spec:
 * - Height: 40dp
 * - Width: Fill available space
 * - Horizontal padding: 4dp
 * - Corner radius: 6dp
 * - Background (unfollowed): #20A3A4
 * - Background (followed): rgba(255, 255, 255, 0.2)
 * - Text color: #FFFFFF
 * - Icon size: 14dp
 * - Icon-to-text gap: 4dp
 * - Font size: 16sp (min 11.2sp at 70% scale)
 * - Font weight: Heavy (800) / ExtraBold
 * - Text transform: UPPERCASE
 * - Loading spinner: 0.7 scale
 */
@Composable
fun FollowButton(
    isFollowed: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isFollowed) {
        Color.White.copy(alpha = 0.2f)
    } else {
        FollowButtonTeal
    }

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable(enabled = !isLoading) { onClick() }
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .scale(0.7f),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isFollowed) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (isFollowed) "Following" else "Follow",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                AutoSizeText(
                    text = if (isFollowed) "FOLLOWING" else "FOLLOW",
                    maxFontSize = 16.sp,
                    minFontSize = 11.sp, // 70% of 16sp ≈ 11.2sp
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Text that automatically scales down to fit available space.
 * Mimics iOS minimumScaleFactor behavior.
 */
@Composable
private fun AutoSizeText(
    text: String,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    fontWeight: FontWeight,
    color: Color,
    modifier: Modifier = Modifier
) {
    var fontSize by remember { mutableStateOf(maxFontSize) }

    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        onTextLayout = { result ->
            if (result.hasVisualOverflow && fontSize > minFontSize) {
                fontSize = (fontSize.value - 0.5f).sp
            }
        },
        modifier = modifier
    )
}

// Follow button teal color (matching iOS spec)
val FollowButtonTeal = Color(0xFF20A3A4)
