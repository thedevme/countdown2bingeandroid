package io.designtoswiftui.countdown2binge.ui.followedshowdetail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import io.designtoswiftui.countdown2binge.ui.detail.components.SpinoffSection
import io.designtoswiftui.countdown2binge.viewmodels.SpinoffShowDisplay

/**
 * Spinoffs tab content container.
 *
 * Features:
 * - Premium gate overlay for non-premium users
 * - LandscapeShowCard for each spinoff
 * - Self-referential navigation to spinoff detail
 * - Auto-loads spinoff details when tab is selected
 */
@Composable
fun SpinoffsTab(
    spinoffs: List<SpinoffShowDisplay>,
    followedShows: Set<Int>,
    addingShows: Set<Int>,
    isPremium: Boolean,
    isLoading: Boolean,
    onShowClick: (Int) -> Unit,
    onFollowClick: (Int) -> Unit,
    onUnlock: () -> Unit,
    onLoadSpinoffs: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-load spinoff details when premium and not already loaded
    LaunchedEffect(isPremium) {
        if (isPremium && spinoffs.isEmpty() && !isLoading) {
            onLoadSpinoffs()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        SpinoffSection(
            spinoffs = spinoffs,
            followedShows = followedShows,
            addingShows = addingShows,
            isPremium = isPremium,
            isLoading = isLoading,
            onShowClick = onShowClick,
            onFollowClick = onFollowClick,
            onUnlock = onUnlock
        )
    }
}
