package io.designtoswiftui.countdown2binge.ui.onboarding

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import io.designtoswiftui.countdown2binge.ui.onboarding.components.ShowRemovalDialog
import io.designtoswiftui.countdown2binge.ui.onboarding.steps.AddShowsScreen
import io.designtoswiftui.countdown2binge.ui.onboarding.steps.CompletionScreen
import io.designtoswiftui.countdown2binge.ui.onboarding.steps.IntroStepScreen
import io.designtoswiftui.countdown2binge.ui.onboarding.steps.PaywallScreen
import io.designtoswiftui.countdown2binge.ui.theme.Background

/**
 * Main onboarding screen that orchestrates the 6-step flow.
 */
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    onShowClick: (Int) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    // Load recommended shows when reaching step 4
    LaunchedEffect(uiState.currentStep) {
        if (uiState.currentStep == 4 && uiState.recommendedShows.isEmpty()) {
            viewModel.loadRecommendedShows()
        }
    }

    // Navigate to main app when onboarding is complete
    // This is handled by the parent checking onboarding state

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        AnimatedContent(
            targetState = uiState.currentStep,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            modifier = Modifier.fillMaxSize(),
            label = "onboardingStep"
        ) { step ->
            when (step) {
                1 -> IntroStepScreen(
                    content = IntroSteps.step1,
                    isSigningIn = uiState.isSigningIn,
                    isSyncing = uiState.isSyncing,
                    signInError = uiState.signInError,
                    onContinue = { viewModel.nextStep() },
                    onSignIn = {
                        activity?.let { viewModel.signInWithGoogle(it) }
                    }
                )

                2 -> IntroStepScreen(
                    content = IntroSteps.step2,
                    isSigningIn = false,
                    isSyncing = false,
                    signInError = null,
                    onContinue = { viewModel.nextStep() },
                    onSignIn = { }
                )

                3 -> IntroStepScreen(
                    content = IntroSteps.step3,
                    isSigningIn = false,
                    isSyncing = false,
                    signInError = null,
                    onContinue = { viewModel.nextStep() },
                    onSignIn = { }
                )

                4 -> AddShowsScreen(
                    selectedShows = uiState.selectedShows,
                    recommendedShows = uiState.recommendedShows,
                    searchResults = uiState.searchResults,
                    searchQuery = uiState.searchQuery,
                    selectedTab = uiState.selectedTab,
                    isLoadingRecommended = uiState.isLoadingRecommended,
                    isSearching = uiState.isSearching,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onTabSelect = viewModel::selectTab,
                    onShowToggle = viewModel::toggleShowSelection,
                    onShowClick = onShowClick,
                    onContinue = { viewModel.nextStep() }
                )

                5 -> CompletionScreen(
                    selectedShows = uiState.selectedShows,
                    onContinue = { viewModel.nextStep() }
                )

                6 -> PaywallScreen(
                    selectedShows = uiState.selectedShows,
                    selectedPricingOption = uiState.selectedPricingOption,
                    isPurchasing = uiState.isPurchasing,
                    purchaseError = uiState.purchaseError,
                    onPricingOptionSelect = viewModel::selectPricingOption,
                    onStartTrial = {
                        activity?.let { viewModel.startFreeTrial(it) }
                    },
                    onContinueFree = {
                        viewModel.continueWithLimitedFeatures()
                    }
                )
            }
        }

        // Full-screen review selection overlay
        AnimatedVisibility(
            visible = uiState.showRemovalDialog,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            ShowRemovalDialog(
                selectedShows = uiState.selectedShows,
                showsToRemove = uiState.showsToRemove,
                onRemoveShow = viewModel::removeShowFromSelection,
                onConfirm = {
                    // Just confirm - navigation will happen when isOnboardingComplete becomes true
                    viewModel.confirmLimitedFeatures()
                },
                onUpgrade = {
                    viewModel.dismissRemovalDialog()
                    // Stay on paywall
                },
                onDismiss = viewModel::dismissRemovalDialog
            )
        }
    }

    // Trigger completion when appropriate - wait for shows to be saved
    LaunchedEffect(isPremium, uiState.currentStep, uiState.isOnboardingComplete) {
        if (uiState.isOnboardingComplete) {
            // Shows have been saved, safe to navigate
            onOnboardingComplete()
        } else if (isPremium && uiState.currentStep == 6) {
            // User is premium (either just purchased or already was)
            // Trigger completion to save shows, then navigate
            viewModel.finishOnboarding()
        }
    }
}
