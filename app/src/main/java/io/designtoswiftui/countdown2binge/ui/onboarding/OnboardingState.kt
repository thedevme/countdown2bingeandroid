package io.designtoswiftui.countdown2binge.ui.onboarding

import io.designtoswiftui.countdown2binge.models.TrendingShowDisplay
import io.designtoswiftui.countdown2binge.services.tmdb.TMDBSearchResult

/**
 * UI state for the onboarding flow.
 */
data class OnboardingUiState(
    val currentStep: Int = 1,
    val selectedShows: Map<Int, ShowSummary> = emptyMap(),
    val recommendedShows: List<ShowSummary> = emptyList(),
    val searchResults: List<ShowSummary> = emptyList(),
    val searchQuery: String = "",
    val isLoadingRecommended: Boolean = false,
    val isSearching: Boolean = false,
    val isSigningIn: Boolean = false,
    val signInError: String? = null,
    val isSyncing: Boolean = false,
    val syncError: String? = null,
    val selectedTab: AddShowsTab = AddShowsTab.RECOMMENDED,
    val showNotificationSettings: Boolean = false,
    val isAddingShow: Boolean = false,
    val addingShowId: Int? = null,
    val isPurchasing: Boolean = false,
    val purchaseError: String? = null,
    val selectedPricingOption: PricingOption = PricingOption.YEARLY,
    val showRemovalDialog: Boolean = false,
    val isOnboardingComplete: Boolean = false
) {
    val selectedShowCount: Int
        get() = selectedShows.size

    val canProceedFromAddShows: Boolean
        get() = true // Always can proceed (0 shows skips to paywall)

    val needsShowRemoval: Boolean
        get() = selectedShows.size > FREE_SHOW_LIMIT

    val showsToRemove: Int
        get() = (selectedShows.size - FREE_SHOW_LIMIT).coerceAtLeast(0)

    companion object {
        const val FREE_SHOW_LIMIT = 3
        const val TOTAL_STEPS = 6
    }
}

/**
 * Summary of a show for onboarding display.
 */
data class ShowSummary(
    val tmdbId: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val overview: String?,
    val networkName: String?,
    val networkLogoPath: String?,
    val logoPath: String?,
    val genres: List<String> = emptyList(),
    val voteAverage: Double? = null
) {
    /**
     * Convert to TrendingShowDisplay for use with TrendingShowCard.
     */
    fun toTrendingShowDisplay(): TrendingShowDisplay {
        val searchResult = TMDBSearchResult(
            id = tmdbId,
            name = title,
            overview = overview,
            posterPath = posterPath,
            backdropPath = backdropPath,
            firstAirDate = null,
            voteAverage = voteAverage,
            voteCount = null,
            popularity = null,
            genreIds = null
        )
        return TrendingShowDisplay(
            show = searchResult,
            logoPath = logoPath,
            genreNames = genres,
            seasonNumber = null
        )
    }

    companion object {
        fun fromSearchResult(result: TMDBSearchResult, genres: List<String> = emptyList()): ShowSummary {
            return ShowSummary(
                tmdbId = result.id,
                title = result.name,
                posterPath = result.posterPath,
                backdropPath = result.backdropPath,
                overview = result.overview,
                networkName = null,
                networkLogoPath = null,
                logoPath = null,
                genres = genres,
                voteAverage = result.voteAverage
            )
        }
    }
}

/**
 * Tabs on the Add Shows screen.
 */
enum class AddShowsTab {
    RECOMMENDED,
    SEARCH
}

/**
 * Pricing options for the paywall.
 */
enum class PricingOption {
    MONTHLY,
    YEARLY,
    LIFETIME
}

/**
 * Intro step content configuration.
 */
data class IntroStepContent(
    val step: Int,
    val title: String,
    val subtitle: String,
    val buttonText: String,
    val showSignIn: Boolean = false
)

/**
 * Premium feature for paywall display.
 */
data class PremiumFeature(
    val icon: String,
    val title: String
)

/**
 * Predefined intro step contents.
 */
object IntroSteps {
    val step1 = IntroStepContent(
        step = 1,
        title = "Wait — that show\ncame back?",
        subtitle = "We've all been there. You loved a show, life got busy, and suddenly you find out season 4 dropped months ago.",
        buttonText = "Continue",
        showSignIn = true
    )

    val step2 = IntroStepContent(
        step = 2,
        title = "Keeping track is\nimpossible",
        subtitle = "Between streaming services, cable, and everything in between, knowing when your shows return feels like a full-time job.",
        buttonText = "Continue"
    )

    val step3 = IntroStepContent(
        step = 3,
        title = "Countdown2Binge\nfixes this",
        subtitle = "Track all your shows in one place. Get notified when seasons premiere, and know exactly when they're ready to binge.",
        buttonText = "Let's Set You Up"
    )
}

/**
 * Premium features list for paywall.
 */
object PremiumFeatures {
    val features = listOf(
        PremiumFeature("infinity", "Unlimited Shows"),
        PremiumFeature("arrow.triangle.2.circlepath", "Spinoff Collections"),
        PremiumFeature("bell.fill", "Smart Notifications"),
        PremiumFeature("mic.fill", "Voice Integration"),
        PremiumFeature("arrow.left.arrow.right", "Cloud Sync"),
        PremiumFeature("sparkles", "All Future Features")
    )
}
