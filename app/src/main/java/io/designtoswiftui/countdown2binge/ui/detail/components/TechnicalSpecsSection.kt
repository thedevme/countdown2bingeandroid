package io.designtoswiftui.countdown2binge.ui.detail.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClosedCaption
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent

/**
 * Technical specifications section with badges and info rows.
 *
 * iOS Design:
 * - Section header: "TECHNICAL SPECS"
 * - Badge style: Teal outline (transparent bg, teal border at 50%, teal text)
 * - Badges: 4K ULTRA HD, HDR10+, DOLBY ATMOS, SPATIAL AUDIO, CC, AD
 * - CC and AD badges have icons
 * - Info rows: CREATED BY, GENRE, NETWORK, AUDIO LANGUAGES
 * - Audio languages is static: "English, Spanish, French, Japanese"
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TechnicalSpecsSection(
    createdBy: List<String>,
    genres: List<String>,
    network: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section header
        Text(
            text = "TECHNICAL SPECS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )

        // Tech badges
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TechBadge("4K ULTRA HD")
            TechBadge("HDR10+")
            TechBadge("DOLBY ATMOS")
            TechBadge("SPATIAL AUDIO")
            TechBadgeWithIcon(
                text = "CC",
                icon = Icons.Outlined.ClosedCaption
            )
            TechBadgeWithIcon(
                text = "AD",
                icon = Icons.AutoMirrored.Outlined.VolumeUp
            )
        }

        // Divider before info rows
        HorizontalDivider(
            color = DividerColor,
            thickness = 0.5.dp
        )

        // Info rows with dividers between each (matching iOS design)
        if (createdBy.isNotEmpty()) {
            InfoRow(
                label = "CREATED BY",
                value = createdBy.joinToString(", ")
            )
            HorizontalDivider(
                color = DividerColor,
                thickness = 0.5.dp
            )
        }

        if (genres.isNotEmpty()) {
            InfoRow(
                label = "GENRE",
                value = genres.joinToString(", ")
            )
            HorizontalDivider(
                color = DividerColor,
                thickness = 0.5.dp
            )
        }

        network?.let {
            InfoRow(
                label = "NETWORK",
                value = it
            )
            HorizontalDivider(
                color = DividerColor,
                thickness = 0.5.dp
            )
        }

        // Audio languages (static placeholder)
        InfoRow(
            label = "AUDIO\nLANGUAGES",
            value = "English, Spanish, French, Japanese"
        )

        // Final divider after last row
        HorizontalDivider(
            color = DividerColor,
            thickness = 0.5.dp
        )
    }
}

/**
 * Tech specification badge with teal outline style.
 */
@Composable
private fun TechBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = DetailAccent.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = DetailAccent,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * Tech badge with icon (for CC and AD).
 */
@Composable
private fun TechBadgeWithIcon(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = DetailAccent.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = DetailAccent,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = DetailAccent,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Info row with label and value.
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = LabelColor,
            letterSpacing = 0.5.sp,
            lineHeight = 14.sp,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.weight(0.65f)
        )
    }
}

// Color constants
private val DividerColor = Color.White.copy(alpha = 0.1f)
private val LabelColor = Color.White.copy(alpha = 0.4f)
