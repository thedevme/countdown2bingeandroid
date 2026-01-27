package io.designtoswiftui.countdown2binge.ui.onboarding

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.purchases.Package
import dagger.hilt.android.lifecycle.HiltViewModel
import io.designtoswiftui.countdown2binge.models.GenreMapping
import io.designtoswiftui.countdown2binge.services.auth.AuthManager
import io.designtoswiftui.countdown2binge.services.premium.PremiumManager
import io.designtoswiftui.countdown2binge.services.repository.ShowRepository
import io.designtoswiftui.countdown2binge.services.settings.SettingsRepository
import io.designtoswiftui.countdown2binge.services.sync.CloudSyncService
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBService
import io.designtoswiftui.countdown2binge.usecases.AddShowUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val tmdbService: TMDBService,
    private val addShowUseCase: AddShowUseCase,
    private val showRepository: ShowRepository,
    private val authManager: AuthManager,
    private val cloudSyncService: CloudSyncService,
    private val premiumManager: PremiumManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    // Search query for debouncing
    private val _searchQueryFlow = MutableStateFlow("")

    init {
        setupSearchDebounce()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _searchQueryFlow
                .debounce(300)
                .distinctUntilChanged()
                .filter { it.length >= 2 }
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    // region Navigation

    fun goToStep(step: Int) {
        _uiState.update { it.copy(currentStep = step.coerceIn(1, OnboardingUiState.TOTAL_STEPS)) }
    }

    fun nextStep() {
        val current = _uiState.value.currentStep
        when (current) {
            4 -> {
                // From Add Shows: skip to paywall if no shows selected
                if (_uiState.value.selectedShows.isEmpty()) {
                    goToStep(6)
                } else {
                    goToStep(5)
                }
            }
            else -> goToStep(current + 1)
        }
    }

    fun previousStep() {
        val current = _uiState.value.currentStep
        if (current > 1) {
            goToStep(current - 1)
        }
    }

    // endregion

    // region Sign In

    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningIn = true, signInError = null) }

            val result = authManager.signInWithGoogle(activityContext)

            result.fold(
                onSuccess = { signedInState ->
                    // Identify user in RevenueCat
                    premiumManager.identifyUser(signedInState.uid)

                    // Attempt to sync shows from cloud
                    _uiState.update { it.copy(isSigningIn = false, isSyncing = true) }

                    try {
                        val syncResult = cloudSyncService.syncOnSignIn()

                        // If shows were restored, load them into selected shows
                        if (syncResult.showsRestoredFromCloud > 0) {
                            loadRestoredShows()
                        }

                        _uiState.update { it.copy(isSyncing = false) }

                        // Jump to step 5 (completion)
                        goToStep(5)
                    } catch (e: Exception) {
                        _uiState.update {
                            it.copy(
                                isSyncing = false,
                                syncError = e.message
                            )
                        }
                        // Still proceed to step 5 even if sync fails
                        goToStep(5)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSigningIn = false,
                            signInError = error.message
                        )
                    }
                }
            )
        }
    }

    private suspend fun loadRestoredShows() {
        // Load all followed shows from repository and add to selected
        showRepository.getAllShows().collect { shows ->
            val summaries = shows.associate { show ->
                show.tmdbId to ShowSummary(
                    tmdbId = show.tmdbId,
                    title = show.title,
                    posterPath = show.posterPath,
                    backdropPath = show.backdropPath,
                    overview = show.overview,
                    networkName = null,
                    networkLogoPath = null,
                    logoPath = null
                )
            }
            _uiState.update { it.copy(selectedShows = summaries) }
        }
    }

    fun clearSignInError() {
        _uiState.update { it.copy(signInError = null) }
    }

    // endregion

    // region Add Shows (Step 4)

    fun loadRecommendedShows() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRecommended = true) }

            val result = tmdbService.getTrending()

            result.fold(
                onSuccess = { response ->
                    val shows = response.results.map { searchResult ->
                        val genres = GenreMapping.getGenreNames(searchResult.genreIds ?: emptyList())
                        ShowSummary.fromSearchResult(searchResult, genres)
                    }
                    _uiState.update {
                        it.copy(
                            recommendedShows = shows,
                            isLoadingRecommended = false
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoadingRecommended = false) }
                }
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        _searchQueryFlow.value = query

        if (query.isEmpty()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }

    private suspend fun performSearch(query: String) {
        if (query.length < 2) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }

        _uiState.update { it.copy(isSearching = true) }

        val result = tmdbService.search(query)

        result.fold(
            onSuccess = { response ->
                val shows = response.results.map { searchResult ->
                    val genres = GenreMapping.getGenreNames(searchResult.genreIds ?: emptyList())
                    ShowSummary.fromSearchResult(searchResult, genres)
                }
                _uiState.update {
                    it.copy(
                        searchResults = shows,
                        isSearching = false
                    )
                }
            },
            onFailure = {
                _uiState.update { it.copy(isSearching = false) }
            }
        )
    }

    fun selectTab(tab: AddShowsTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleShowSelection(show: ShowSummary) {
        _uiState.update { state ->
            val currentSelection = state.selectedShows.toMutableMap()
            if (currentSelection.containsKey(show.tmdbId)) {
                currentSelection.remove(show.tmdbId)
            } else {
                currentSelection[show.tmdbId] = show
            }
            state.copy(selectedShows = currentSelection)
        }
    }

    fun isShowSelected(tmdbId: Int): Boolean {
        return _uiState.value.selectedShows.containsKey(tmdbId)
    }

    // endregion

    // region Paywall (Step 6)

    fun selectPricingOption(option: PricingOption) {
        _uiState.update { it.copy(selectedPricingOption = option) }
    }

    fun startFreeTrial(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true, purchaseError = null) }

            try {
                val offeringsResult = premiumManager.getOfferings()

                offeringsResult.fold(
                    onSuccess = { offerings ->
                        // Find the package matching selected option
                        val packageId = when (_uiState.value.selectedPricingOption) {
                            PricingOption.MONTHLY -> "\$rc_monthly"
                            PricingOption.YEARLY -> "\$rc_annual"
                            PricingOption.LIFETIME -> "\$rc_lifetime"
                        }

                        val pkg = offerings.packages.find { it.identifier == packageId }
                            ?: offerings.packages.firstOrNull()

                        if (pkg != null) {
                            val purchaseResult = premiumManager.purchase(activity, pkg.rcPackage)

                            purchaseResult.fold(
                                onSuccess = {
                                    _uiState.update { it.copy(isPurchasing = false) }
                                    completeOnboarding()
                                },
                                onFailure = { error ->
                                    _uiState.update {
                                        it.copy(
                                            isPurchasing = false,
                                            purchaseError = error.message
                                        )
                                    }
                                }
                            )
                        } else {
                            _uiState.update {
                                it.copy(
                                    isPurchasing = false,
                                    purchaseError = "No packages available"
                                )
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isPurchasing = false,
                                purchaseError = error.message
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPurchasing = false,
                        purchaseError = e.message
                    )
                }
            }
        }
    }

    fun continueWithLimitedFeatures() {
        val state = _uiState.value
        if (state.needsShowRemoval) {
            _uiState.update { it.copy(showRemovalDialog = true) }
        } else {
            completeOnboarding()
        }
    }

    fun dismissRemovalDialog() {
        _uiState.update { it.copy(showRemovalDialog = false) }
    }

    fun removeShowFromSelection(tmdbId: Int) {
        _uiState.update { state ->
            val updated = state.selectedShows.toMutableMap()
            updated.remove(tmdbId)
            state.copy(selectedShows = updated)
        }
    }

    fun confirmLimitedFeatures() {
        // User has removed enough shows, proceed
        _uiState.update { it.copy(showRemovalDialog = false) }
        completeOnboarding()
    }

    // endregion

    // region Completion

    /**
     * Public function to trigger onboarding completion.
     * Called when premium user reaches step 6 or when completing normally.
     */
    fun finishOnboarding() {
        completeOnboarding()
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            // Save all selected shows to database
            val selectedShows = _uiState.value.selectedShows.values.toList()

            for (show in selectedShows) {
                // Check if already saved (from sync)
                val existing = showRepository.getShow(show.tmdbId)
                if (existing == null) {
                    try {
                        addShowUseCase.execute(show.tmdbId)

                        // Push to cloud if premium
                        if (premiumManager.isPremium.value) {
                            cloudSyncService.pushShow(show.tmdbId)
                        }
                    } catch (e: Exception) {
                        // Continue with other shows
                    }
                }
            }

            // Mark onboarding as completed
            settingsRepository.setOnboardingCompleted(true)

            // Signal that onboarding is truly complete (triggers navigation)
            _uiState.update { it.copy(isOnboardingComplete = true) }
        }
    }

    // endregion

    // region Premium Status

    val isPremium: StateFlow<Boolean> = premiumManager.isPremium

    // endregion
}
