package io.designtoswiftui.countdown2binge.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.countdown2binge.models.CountdownDisplayMode
import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.services.auth.AuthManager
import io.designtoswiftui.countdown2binge.services.auth.AuthState
import io.designtoswiftui.countdown2binge.services.premium.PremiumManager
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import io.designtoswiftui.countdown2binge.services.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Settings screen.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val premiumManager: PremiumManager,
    private val authManager: AuthManager,
    private val showRepository: ShowRepository
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
     * Whether sound effects are enabled.
     */
    val soundEnabled: StateFlow<Boolean> = settingsRepository.soundEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    /**
     * Whether haptic feedback is enabled.
     */
    val hapticsEnabled: StateFlow<Boolean> = settingsRepository.hapticsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    /**
     * Premium status - combines real premium with debug simulation.
     */
    val isPremium: StateFlow<Boolean> = combine(
        premiumManager.isPremium,
        settingsRepository.debugSimulatePremium
    ) { realPremium, simulatedPremium ->
        realPremium || simulatedPremium
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    /**
     * Auth state for account section.
     */
    val authState: StateFlow<AuthState> = authManager.uiState
        .map { it.authState }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.SignedOut
        )

    /**
     * All saved shows.
     */
    val savedShows: StateFlow<List<Show>> = showRepository.getAllShows()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // region Debug settings

    /**
     * Debug: Simulate premium status.
     */
    val debugSimulatePremium: StateFlow<Boolean> = settingsRepository.debugSimulatePremium
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    /**
     * Debug: Show full onboarding.
     */
    val debugShowFullOnboarding: StateFlow<Boolean> = settingsRepository.debugShowFullOnboarding
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // endregion

    // region Setters

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

    /**
     * Set sound enabled.
     */
    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundEnabled(enabled)
        }
    }

    /**
     * Set haptics enabled.
     */
    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHapticsEnabled(enabled)
        }
    }

    /**
     * Set debug simulate premium.
     */
    fun setDebugSimulatePremium(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDebugSimulatePremium(enabled)
        }
    }

    /**
     * Set debug show full onboarding.
     */
    fun setDebugShowFullOnboarding(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDebugShowFullOnboarding(enabled)
        }
    }

    /**
     * Reset onboarding state for testing.
     */
    fun resetOnboardingState() {
        viewModelScope.launch {
            settingsRepository.resetOnboardingState()
        }
    }

    // endregion
}
