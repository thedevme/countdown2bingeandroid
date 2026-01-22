package io.designtoswiftui.countdown2binge.ui.detail.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent

/**
 * Info section with expandable synopsis and metadata.
 *
 * iOS Design:
 * - Synopsis line limit (collapsed): 3 lines
 * - "more ∨" / "less ∧" with chevron icons
 * - Metadata format: "{N} SEASON · {STATUS}"
 * - Metadata font size: 11sp
 * - Metadata letter spacing: 1sp
 * - Metadata color: rgba(255, 255, 255, 0.5)
 */
@Composable
fun InfoSection(
    synopsis: String?,
    seasonCount: Int,
    statusText: String,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Synopsis with expandable text
        if (!synopsis.isNullOrBlank()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = synopsis,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 20.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                )

                // "more ∨" / "less ∧" link with chevron
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onExpandClick() }
                ) {
                    Text(
                        text = if (isExpanded) "less" else "more",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DetailAccent
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = DetailAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Metadata row: "1 SEASON · RETURNING SERIES"
        val seasonText = if (seasonCount == 1) "1 SEASON" else "$seasonCount SEASONS"
        val metadataText = if (statusText.isNotBlank()) {
            "$seasonText  ·  ${statusText.uppercase()}"
        } else {
            seasonText
        }

        Text(
            text = metadataText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MetadataWhite,
            letterSpacing = 1.sp
        )
    }
}

// Color constants
private val MetadataWhite = Color.White.copy(alpha = 0.5f)
