package io.designtoswiftui.countdown2binge.ui.followedshowdetail.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import io.designtoswiftui.countdown2binge.ui.followedshowdetail.FollowedShowTab
import io.designtoswiftui.countdown2binge.ui.theme.DetailAccent

/**
 * Dynamic tab bar for the Followed Show Detail screen.
 *
 * Design specs:
 * - Tab text: 13sp, semibold, uppercase
 * - Tab spacing: 24dp
 * - Selected color: Accent (#4AC7B8)
 * - Unselected color: white @ 50%
 * - Underline: 2dp height, accent color
 * - Haptic feedback on tab change
 */
@Composable
fun FollowedShowTabBar(
    tabs: List<FollowedShowTab>,
    selectedTab: FollowedShowTab,
    onTabSelected: (FollowedShowTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current

    // Track tab widths for underline animation
    var tabWidths by remember { mutableStateOf(mapOf<FollowedShowTab, Float>()) }
    var tabOffsets by remember { mutableStateOf(mapOf<FollowedShowTab, Float>()) }

    // Calculate underline position and width
    val selectedTabWidth = tabWidths[selectedTab] ?: 0f
    val selectedTabOffset = tabOffsets[selectedTab] ?: 0f

    val animatedOffsetPx by animateFloatAsState(
        targetValue = selectedTabOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tab_underline_offset"
    )

    val animatedWidthPx by animateFloatAsState(
        targetValue = selectedTabWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tab_underline_width"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .semantics {
                contentDescription = "Content tabs"
                role = Role.Tab
            }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = tab == selectedTab

                Box(
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            tabWidths = tabWidths + (tab to coordinates.size.width.toFloat())
                            tabOffsets = tabOffsets + (tab to coordinates.positionInParent().x)
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTabSelected(tab)
                            }
                        }
                        .semantics {
                            contentDescription = tab.displayName
                            role = Role.Tab
                            selected = isSelected
                            stateDescription = if (isSelected) "Currently selected" else "Double tap to select"
                        }
                ) {
                    Text(
                        text = tab.displayName.uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        color = if (isSelected) DetailAccent else Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Animated underline indicator
        Box(
            modifier = Modifier
                .offset(x = with(density) { animatedOffsetPx.toDp() })
                .width(with(density) { animatedWidthPx.toDp() })
                .height(2.dp)
                .background(DetailAccent)
        )
    }
}
