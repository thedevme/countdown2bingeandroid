package io.designtoswiftui.countdown2binge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.countdown2binge.models.CountdownDisplayMode
import io.designtoswiftui.countdown2binge.services.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Settings screen.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    /**
     * Whether to include airing seasons in Binge Ready.
     */
    val includeAiring: StateFlow<Boolean> = settingsRepository.includeAiring
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    /**
     * Countdown display mode for the Timeline hero card.
     */
    val countdownDisplayMode: StateFlow<CountdownDisplayMode> = settingsRepository.countdownDisplayMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CountdownDisplayMode.DAYS
        )

    /**
     * Whether timeline sections are expanded.
     * Default: true (expanded).
     */
    val timelineSectionsExpanded: StateFlow<Boolean> = settingsRepository.timelineSectionsExpanded
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    /**
     * Toggle the include airing setting.
     */
    fun setIncludeAiring(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setIncludeAiring(enabled)
        }
    }

    /**
     * Set the countdown display mode.
     */
    fun setCountdownDisplayMode(mode: CountdownDisplayMode) {
        viewModelScope.launch {
            settingsRepository.setCountdownDisplayMode(mode)
        }
    }

    /**
     * Toggle whether timeline sections are expanded or collapsed.
     */
    fun toggleTimelineSectionsExpanded() {
        viewModelScope.launch {
            settingsRepository.toggleTimelineSectionsExpanded()
        }
    }
}
