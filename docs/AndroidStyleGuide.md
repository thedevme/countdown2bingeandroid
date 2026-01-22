# Countdown2Binge Android Style Guide

This style guide establishes standards for all Jetpack Compose development in this project, based on our existing patterns and Android best practices.

---

## 1. Project Structure

### Package Organization

```
io.designtoswiftui.countdown2binge/
├── Countdown2BingeApp.kt            # @HiltAndroidApp Application class
├── MainActivity.kt                   # Main activity with NavHost
├── models/                           # Data models (Show, Episode, Season, etc.)
│   ├── Show.kt
│   ├── Season.kt
│   ├── Episode.kt
│   ├── SeasonState.kt
│   ├── ShowStatus.kt
│   └── ReleasePattern.kt
├── viewmodels/                       # ViewModels for UI state
│   ├── TimelineViewModel.kt
│   ├── BingeReadyViewModel.kt
│   ├── SearchViewModel.kt
│   └── ShowDetailViewModel.kt
├── ui/                               # All UI composables
│   ├── components/                   # Reusable across ALL features
│   │   ├── AddButton.kt
│   │   ├── GenreTag.kt
│   │   ├── PosterImage.kt
│   │   └── SynopsisText.kt
│   ├── showdetail/                   # Feature: Show Detail
│   │   ├── ShowDetailScreen.kt       # Main screen at root
│   │   └── components/               # ShowDetail-specific helpers
│   │       ├── CastSection.kt
│   │       ├── EpisodeSection.kt
│   │       └── TrailersSection.kt
│   ├── search/
│   │   ├── SearchScreen.kt
│   │   └── components/
│   ├── timeline/
│   │   ├── TimelineScreen.kt
│   │   └── components/
│   ├── bingeready/
│   │   ├── BingeReadyScreen.kt
│   │   └── components/
│   ├── settings/
│   ├── premium/
│   ├── onboarding/
│   └── navigation/
│       ├── NavGraph.kt
│       └── BottomNavBar.kt
├── services/
│   ├── repository/
│   │   ├── AppDatabase.kt
│   │   ├── ShowDao.kt
│   │   └── ShowRepository.kt
│   ├── tmdb/
│   │   ├── TMDBApi.kt
│   │   ├── TMDBService.kt
│   │   └── TMDBModels.kt
│   └── state/
│       ├── SeasonStateManager.kt
│       └── SeasonDateResolver.kt
├── usecases/
│   └── AddShowUseCase.kt
├── theme/
│   ├── AppColors.kt
│   ├── AppTypography.kt
│   ├── AppSpacing.kt
│   ├── AppRadius.kt
│   ├── ButtonStyles.kt
│   └── Theme.kt
├── di/                               # Hilt modules
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   └── NetworkModule.kt
├── util/
└── demo/
```

### Key Principles

1. **Feature package structure**:
   - Main screen at package root (e.g., `showdetail/ShowDetailScreen.kt`)
   - `components/` subpackage for feature-specific helpers

2. **Where reusable code lives**:
   | Location | Purpose | Examples |
   |----------|---------|----------|
   | `util/` | Small reusable **utilities** | Extensions, helpers, formatters |
   | `ui/components/` | Reusable **composables** (appear in multiple features) | PosterImage, CountdownText |
   | `ui/[feature]/components/` | **Feature-specific** helper composables | Only used in that feature |

3. **Easy to find files** - Main screen is always at root, helpers are always in subpackage

---

## 2. File Organization

### One Main Composable Per File

Each file should have ONE primary public composable. The file is named after this composable.

```kotlin
// ShowDetailScreen.kt
@Composable
fun ShowDetailScreen(...) { ... }  // Primary composable
```

### Helper Composables at Bottom of Same File

Small helper composables that ONLY serve the main composable stay in the same file, placed AFTER the main composable and previews:

```kotlin
// SummarySection.kt

@Composable
fun SummarySection(
    show: Show,
    modifier: Modifier = Modifier
) {
    // Implementation
}

@Preview
@Composable
private fun SummarySectionPreview() {
    Countdown2BingeTheme {
        SummarySection(show = PreviewData.show)
    }
}

// Helper composable - only used by SummarySection
@Composable
private fun SummaryMetricItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    // Implementation
}
```

### When to Extract to Separate File

Extract to a new file when:
- The component is used by multiple screens/features
- The component is substantial (>50 lines)
- The component has its own state management or ViewModel

### File Length Guidelines

- **Target**: 50-150 lines per file
- **Maximum**: ~300 lines before considering extraction
- Main screens should be lean composition, not implementation

---

## 3. Composable Structure

### Standard Composable Template

```kotlin
/*
 * Copyright 2024 Cocoa Academy
 *
 * A composable that [description of what this composable does].
 */

package io.designtoswiftui.countdown2binge.ui.feature

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * A screen that [description].
 *
 * @param viewModel The ViewModel for this screen
 * @param onNavigateToDetail Callback when user taps a show
 * @param modifier Modifier for this composable
 */
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel = hiltViewModel(),
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Local state (if needed)
    var isExpanded by remember { mutableStateOf(false) }

    // Main content
    FeatureContent(
        uiState = uiState,
        onItemClick = onNavigateToDetail,
        modifier = modifier
    )
}

/**
 * Stateless content composable for [FeatureScreen].
 */
@Composable
private fun FeatureContent(
    uiState: FeatureUiState,
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // Keep content clean - compose other composables
}

@Preview(showBackground = true)
@Composable
private fun FeatureScreenPreview() {
    Countdown2BingeTheme {
        FeatureContent(
            uiState = FeatureUiState(),
            onItemClick = {}
        )
    }
}
```

### Content Composition Rules

1. **Keep composables short** - Maximum 20-30 lines of actual layout code
2. **Extract to helper composables** - Use private composables or separate files
3. **Compose, don't implement** - Body should read like an outline
4. **State hoisting** - Separate stateful wrapper from stateless content

**Good:**
```kotlin
@Composable
fun ShowDetailScreen(...) {
    Scaffold(
        topBar = { ShowDetailTopBar(...) }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            item { HeaderSection(...) }
            item { OverviewSection(...) }
            item { SeasonsSection(...) }
        }
    }
}
```

**Bad:**
```kotlin
@Composable
fun ShowDetailScreen(...) {
    Scaffold {
        LazyColumn {
            item {
                // 200 lines of nested composables...
            }
        }
    }
}
```

---

## 4. Adaptive Layouts

### The Golden Rule: Parent Controls Size, Child is Flexible

**This is the most important layout concept in Compose.**

| Component | Responsibility |
|-----------|---------------|
| **Child Composable** | Use `fillMaxWidth()` or `fillMaxSize()` to be flexible |
| **Parent Composable** | Control size via `aspectRatio()`, `weight()`, or explicit size |

```kotlin
// CHILD COMPOSABLE - Always flexible, never fixed
@Composable
fun ShowCard(
    show: Show,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.lg))
    ) {
        AsyncImage(
            model = show.backdropUrl,
            contentDescription = show.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

// PARENT COMPOSABLE - Controls the size
@Composable
fun ShowCardList(shows: List<Show>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        items(shows) { show ->
            ShowCard(
                show = show,
                modifier = Modifier
                    .fillParentMaxWidth(0.8f)  // Parent sets width as 80%
                    .aspectRatio(16f / 9f)      // Parent sets aspect ratio
            )
        }
    }
}
```

### Modifier Best Practices

**NEVER use fixed sizes on reusable components:**

```kotlin
// WRONG - Fixed sizes break on different devices
Modifier.size(200.dp, 150.dp)

// CORRECT - Flexible sizing
Modifier
    .fillMaxWidth()
    .aspectRatio(16f / 9f)
```

**When to use each modifier:**

| Modifier | Use Case |
|----------|----------|
| `.fillMaxWidth()` | Fill available horizontal space |
| `.fillMaxWidth(fraction)` | Fill percentage of available width |
| `.weight(1f)` | Distribute space in Row/Column |
| `.aspectRatio(ratio)` | Maintain proportions |
| `.heightIn(min, max)` | Constrain height range |
| `.widthIn(min, max)` | Constrain width range |

**Only use fixed sizes in:**
- `@Preview` blocks for testing
- Parent containers controlling child size (not on the child itself)
- Icons and specific design requirements

### Image Sizing Pattern

```kotlin
// The CORRECT image pattern - memorize this
AsyncImage(
    model = imageUrl,
    contentDescription = description,
    contentScale = ContentScale.Crop,  // Fill and crop
    modifier = Modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(AppRadius.lg))
)

// ContentScale options:
// ContentScale.Crop - Image fills entire space, may crop edges
// ContentScale.Fit  - Entire image visible, may have empty space
// ContentScale.FillBounds - Stretches to fill (distorts)
// ContentScale.FillWidth - Fills width, crops height if needed
// ContentScale.FillHeight - Fills height, crops width if needed
```

### Aspect Ratios

Use aspect ratios instead of fixed dimensions:

```kotlin
// Video thumbnails (16:9)
Modifier.aspectRatio(16f / 9f)

// Square items
Modifier.aspectRatio(1f)

// Portrait posters (2:3)
Modifier.aspectRatio(2f / 3f)

// From Constants
Modifier.aspectRatio(AppDimensions.cardAspectRatio)
```

### Adaptive Grids

Use `LazyVerticalGrid` with adaptive columns:

```kotlin
// Adaptive - system calculates column count based on available space
LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = AppDimensions.gridItemMinSize),
    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    modifier = Modifier.fillMaxSize()
) {
    items(shows) { show ->
        ShowCard(
            show = show,
            modifier = Modifier.aspectRatio(2f / 3f)  // Grid controls width, aspect ratio controls height
        )
    }
}

// Fixed columns
LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    // ...
)
```

### Horizontal Scrolling Lists

```kotlin
LazyRow(
    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    contentPadding = PaddingValues(horizontal = AppSpacing.horizontalPadding),
    modifier = Modifier.fillMaxWidth()
) {
    items(shows) { show ->
        ShowCard(
            show = show,
            modifier = Modifier
                .fillParentMaxWidth(0.7f)  // 70% of LazyRow width
                .aspectRatio(16f / 9f)
        )
    }
}
```

### BoxWithConstraints for Responsive Layouts

```kotlin
@Composable
fun ResponsiveLayout(content: @Composable () -> Unit) {
    BoxWithConstraints {
        when {
            maxWidth < 600.dp -> CompactLayout(content)
            maxWidth < 840.dp -> MediumLayout(content)
            else -> ExpandedLayout(content)
        }
    }
}
```

### Layout Anti-Patterns

```kotlin
// BAD: Fixed size on reusable component
@Composable
fun TrailerCard(...) {
    Box(
        modifier = Modifier.size(200.dp, 112.dp)  // WRONG
    ) { ... }
}

// GOOD: Flexible component, parent controls size
@Composable
fun TrailerCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    ) { ... }
}

// Parent controls actual size
TrailerCard(
    modifier = Modifier.fillMaxWidth(0.6f)
)
```

### Layout Checklist

- [ ] No `Modifier.size()` on reusable components
- [ ] Images use `AsyncImage` + `ContentScale.Crop` + `fillMaxSize()` + `clip()`
- [ ] Grids use `GridCells.Adaptive()` or `GridCells.Fixed()`
- [ ] Size values come from theme/constants, not inline numbers
- [ ] Parent composables control child sizing via aspect ratio or weight
- [ ] Modifier parameter is always last and has default `Modifier`

---

## 5. Constants and Dimensions

### Dimensions Object

All magic numbers go in the theme package:

```kotlin
// theme/AppDimensions.kt

package io.designtoswiftui.countdown2binge.theme

import androidx.compose.ui.unit.dp

/**
 * Dimension constants used throughout the app.
 */
object AppDimensions {
    // Grid Layout
    val gridItemMinSize = 160.dp
    val gridItemMaxSize = 320.dp
    val gridSpacing = 14.dp

    // List Layout
    const val listItemAspectRatio = 1.4f
    const val cardAspectRatio = 16f / 9f
    const val posterAspectRatio = 2f / 3f

    // Detail View
    val headerHeight = 420.dp
    val headerMinHeight = 200.dp

    // Bottom Navigation
    val bottomNavHeight = 80.dp
}
```

### Usage in Composables

```kotlin
// Always reference constants, never inline numbers
Modifier
    .padding(horizontal = AppSpacing.horizontalPadding)
    .clip(RoundedCornerShape(AppRadius.lg))
    .aspectRatio(AppDimensions.cardAspectRatio)
```

---

## 6. Navigation

### Navigation Setup

```kotlin
// navigation/NavGraph.kt

sealed class Screen(val route: String) {
    object Timeline : Screen("timeline")
    object BingeReady : Screen("binge_ready")
    object Search : Screen("search")
    object Settings : Screen("settings")

    data class ShowDetail(val showId: Long) : Screen("show/{showId}") {
        companion object {
            const val route = "show/{showId}"
            fun createRoute(showId: Long) = "show/$showId"
        }
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Timeline.route,
        modifier = modifier
    ) {
        composable(Screen.Timeline.route) {
            TimelineScreen(
                onShowClick = { showId ->
                    navController.navigate(Screen.ShowDetail.createRoute(showId))
                }
            )
        }

        composable(
            route = Screen.ShowDetail.route,
            arguments = listOf(navArgument("showId") { type = NavType.LongType })
        ) { backStackEntry ->
            val showId = backStackEntry.arguments?.getLong("showId") ?: return@composable
            ShowDetailScreen(
                showId = showId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
```

### Bottom Navigation

```kotlin
// navigation/BottomNavBar.kt

@Composable
fun BottomNavBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier,
        containerColor = AppColors.surface
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Timeline") },
            label = { Text("Timeline") },
            selected = currentRoute == Screen.Timeline.route,
            onClick = {
                navController.navigate(Screen.Timeline.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )
        // Additional items...
    }
}
```

---

## 7. Previews

### Standard Preview Template

```kotlin
@Preview(showBackground = true)
@Composable
private fun ShowCardPreview() {
    Countdown2BingeTheme {
        ShowCard(
            show = PreviewData.show,
            onClick = {}
        )
    }
}
```

### Preview with Dark Theme

```kotlin
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShowCardDarkPreview() {
    Countdown2BingeTheme {
        ShowCard(
            show = PreviewData.show,
            onClick = {}
        )
    }
}
```

### Preview with Specific Size

```kotlin
@Preview(showBackground = true, widthDp = 320, heightDp = 180)
@Composable
private fun ShowCardSizedPreview() {
    Countdown2BingeTheme {
        ShowCard(
            show = PreviewData.show,
            onClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
```

### Preview Data Object

```kotlin
// util/PreviewData.kt

object PreviewData {
    val show = Show(
        id = 1,
        tmdbId = 12345,
        title = "Breaking Bad",
        overview = "A high school chemistry teacher...",
        posterPath = "/path.jpg",
        backdropPath = "/backdrop.jpg",
        status = ShowStatus.ENDED
    )

    val season = Season(
        id = 1,
        tmdbId = 123,
        seasonNumber = 1,
        state = SeasonState.BINGE_READY,
        // ...
    )

    val shows = listOf(show, show.copy(id = 2, title = "Better Call Saul"))
}
```

---

## 8. Code Style

### Imports

- Order: Compose first, then AndroidX, then project imports
- Use specific imports, avoid wildcards except for common Compose packages

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import io.designtoswiftui.countdown2binge.models.Show
import io.designtoswiftui.countdown2binge.theme.AppColors
```

### Parameter Order in Composables

1. Required parameters (data)
2. Optional parameters with defaults
3. Event callbacks (onClick, onValueChange, etc.)
4. `modifier: Modifier = Modifier` (always last)

```kotlin
@Composable
fun ShowCard(
    show: Show,                           // Required data
    isSelected: Boolean = false,          // Optional with default
    onClick: () -> Unit,                  // Event callback
    modifier: Modifier = Modifier         // Always last
) { ... }
```

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Screens | PascalCase + Screen suffix | `ShowDetailScreen` |
| Components | PascalCase (no suffix) | `ShowCard`, `CountdownText` |
| ViewModels | PascalCase + ViewModel suffix | `TimelineViewModel` |
| Screen files | Match composable name | `ShowDetailScreen.kt` |
| Feature packages | lowercase | `showdetail/` |
| State classes | PascalCase + UiState/State suffix | `TimelineUiState` |
| Event callbacks | on + Action | `onClick`, `onShowSelected` |

### Documentation

Every public composable gets a KDoc comment:

```kotlin
/**
 * Displays a single show card with poster, title, and countdown.
 *
 * @param show The show data to display
 * @param onClick Called when the card is clicked
 * @param modifier Modifier for this composable
 */
@Composable
fun ShowCard(
    show: Show,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) { ... }
```

---

## 9. State Management

### ViewModel Pattern

```kotlin
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val seasonStateManager: SeasonStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    init {
        loadShows()
    }

    private fun loadShows() {
        viewModelScope.launch {
            showRepository.getTimelineShows()
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { shows ->
                    _uiState.update { state ->
                        state.copy(
                            airingShows = shows.filter { it.currentSeason?.state == SeasonState.AIRING },
                            premieringShows = shows.filter { it.currentSeason?.state == SeasonState.PREMIERING },
                            anticipatedShows = shows.filter { it.currentSeason?.state == SeasonState.ANTICIPATED },
                            isLoading = false
                        )
                    }
                }
        }
    }
}

data class TimelineUiState(
    val airingShows: List<Show> = emptyList(),
    val premieringShows: List<Show> = emptyList(),
    val anticipatedShows: List<Show> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
```

### State Hoisting Pattern

```kotlin
// Stateful wrapper (handles ViewModel)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel(),
    onShowClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TimelineContent(
        uiState = uiState,
        onShowClick = onShowClick
    )
}

// Stateless content (testable, previewable)
@Composable
private fun TimelineContent(
    uiState: TimelineUiState,
    onShowClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // UI implementation
}
```

### When to Use Each State Type

| Type | Use Case |
|------|----------|
| `remember { mutableStateOf() }` | Simple local UI state (expanded, selected) |
| `rememberSaveable` | UI state that survives config changes |
| `ViewModel + StateFlow` | Business logic state, survives config changes |
| `collectAsStateWithLifecycle()` | Collecting Flows in composables |
| `derivedStateOf` | Computed values that depend on other state |

---

## 10. Modifiers

### Custom Modifier Extensions

```kotlin
// util/ModifierExtensions.kt

fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition()
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        )
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.LightGray.copy(alpha = 0.6f),
                Color.LightGray.copy(alpha = 0.2f),
                Color.LightGray.copy(alpha = 0.6f),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    )
    .onGloballyPositioned { size = it.size }
}

fun Modifier.conditional(
    condition: Boolean,
    modifier: Modifier.() -> Modifier
): Modifier = if (condition) then(modifier()) else this
```

### Modifier Order

Apply modifiers in this order:
1. Layout modifiers (size, fillMaxWidth, padding)
2. Drawing modifiers (background, border)
3. Clipping (clip)
4. Click/interaction (clickable, toggleable)
5. Semantics (semantics, contentDescription)

```kotlin
Modifier
    .fillMaxWidth()
    .padding(AppSpacing.md)
    .background(AppColors.surface, RoundedCornerShape(AppRadius.lg))
    .clip(RoundedCornerShape(AppRadius.lg))
    .clickable { onClick() }
```

---

## 11. Checklist for New Composables

- [ ] File named after main composable
- [ ] KDoc comment on main composable
- [ ] Parameters in correct order (data, optional, callbacks, modifier)
- [ ] `modifier` parameter is last with default `Modifier`
- [ ] Composable body is clean composition (<30 lines)
- [ ] No magic numbers (use theme constants)
- [ ] Adaptive layout (no fixed sizes on reusable components)
- [ ] Preview included with `@Preview` annotation
- [ ] Helper composables are `private`
- [ ] State hoisting applied (stateful wrapper + stateless content)

---

## 12. Anti-Patterns to Avoid

### DON'T: Fixed Sizes on Reusable Components
```kotlin
// BAD
Modifier.size(200.dp, 150.dp)

// GOOD
Modifier
    .fillMaxWidth()
    .aspectRatio(4f / 3f)
```

### DON'T: Inline Magic Numbers
```kotlin
// BAD
Modifier
    .padding(horizontal = 20.dp)
    .clip(RoundedCornerShape(12.dp))

// GOOD
Modifier
    .padding(horizontal = AppSpacing.horizontalPadding)
    .clip(RoundedCornerShape(AppRadius.lg))
```

### DON'T: Massive Composable Files
```kotlin
// BAD - 1500 line file with 20 private composables

// GOOD - Main screen ~100 lines, extracted components in separate files
```

### DON'T: Deep Nesting
```kotlin
// BAD
Column {
    Row {
        Column {
            Row {
                // ...
            }
        }
    }
}

// GOOD - Extract to private composables
@Composable
fun FeatureScreen() {
    Column {
        HeaderSection()
        ContentSection()
        FooterSection()
    }
}
```

### DON'T: Business Logic in Composables
```kotlin
// BAD
@Composable
fun ShowCard(show: Show) {
    val isReady = show.finaleDate?.let {
        LocalDate.now().isAfter(it)
    } ?: false
    // ...
}

// GOOD - Logic in ViewModel or use case
@Composable
fun ShowCard(
    show: Show,
    isBingeReady: Boolean
) {
    // ...
}
```

---

## 13. Button and Component Styles

### Reusable Button Composables

```kotlin
// theme/ButtonStyles.kt

/**
 * Primary action button with filled accent background.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.accent,
            contentColor = Color.White,
            disabledContainerColor = AppColors.accent.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(AppRadius.md),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = text,
                style = AppTypography.button
            )
        }
    }
}

/**
 * Secondary action button with subtle background.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.2f),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(AppRadius.md),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(
            text = text,
            style = AppTypography.button
        )
    }
}

/**
 * Text-only button for tertiary actions.
 */
@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = AppColors.accent
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = AppTypography.buttonSmall,
            color = color
        )
    }
}
```

### Usage

```kotlin
Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
    PrimaryButton(
        text = "Follow Show",
        onClick = { viewModel.followShow() }
    )

    SecondaryButton(
        text = "View Trailer",
        onClick = onTrailerClick
    )

    TextButton(
        text = "Cancel",
        onClick = onDismiss
    )
}
```

---

## 14. Design System Foundation

### Folder Structure

```
theme/
├── AppColors.kt         # Semantic color tokens
├── AppTypography.kt     # Font tokens
├── AppSpacing.kt        # Spacing tokens
├── AppRadius.kt         # Corner radius tokens
├── AppDimensions.kt     # Size/dimension tokens
├── ButtonStyles.kt      # Reusable button composables
└── Theme.kt             # Material theme setup
```

### AppColors.kt

Use **semantic names** (what it's for) not **descriptive names** (what it looks like):

```kotlin
package io.designtoswiftui.countdown2binge.theme

import androidx.compose.ui.graphics.Color

/**
 * Centralized color tokens for the app.
 */
object AppColors {
    // MARK: - Accent Colors
    /** Primary accent color (teal) - buttons, highlights, links */
    val accent = Color(0xFF4ECDC4)
    /** Secondary accent for less emphasis */
    val accentSecondary = Color(0xFF3BA99C)
    /** Accent with reduced opacity for backgrounds */
    val accentMuted = accent.copy(alpha = 0.12f)

    // MARK: - Backgrounds
    /** Main app background (pure black) */
    val background = Color.Black
    /** Elevated surface (cards, sheets) */
    val surface = Color(0xFF0F0F0F)
    /** Card background */
    val cardBackground = Color(0xFF141414)

    // MARK: - Text
    /** Primary text (white) */
    val textPrimary = Color.White
    /** Secondary text (75% opacity) */
    val textSecondary = Color.White.copy(alpha = 0.75f)
    /** Tertiary/disabled text (50% opacity) */
    val textTertiary = Color.White.copy(alpha = 0.5f)
    /** Quaternary text (40% opacity) */
    val textQuaternary = Color.White.copy(alpha = 0.4f)

    // MARK: - Borders
    /** Standard border */
    val border = Color.White.copy(alpha = 0.15f)
    /** Subtle border for cards */
    val borderSubtle = Color.White.copy(alpha = 0.08f)

    // MARK: - Semantic Colors
    /** Destructive actions (delete, remove) */
    val destructive = Color(0xFFFF6B6B)
    /** Success states */
    val success = Color(0xFF51CF66)
    /** Warning states */
    val warning = Color(0xFFFFD43B)

    // MARK: - State Badge Colors
    val airingBadge = Color(0xFF4ECDC4)
    val premieringBadge = Color(0xFFFFE66D)
    val anticipatedBadge = Color(0xFFFF6B6B)
    val bingeReadyBadge = Color(0xFF51CF66)
    val watchedBadge = Color(0xFF868E96)
}
```

### AppTypography.kt

Define fonts by **semantic purpose**, not by size:

```kotlin
package io.designtoswiftui.countdown2binge.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Centralized typography tokens for the app.
 */
object AppTypography {
    // MARK: - Display (Hero text, large titles)
    /** Large hero title - 36sp heavy */
    val displayLarge = TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = (-0.5).sp
    )
    /** Medium display - 28sp bold */
    val displayMedium = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold
    )

    // MARK: - Titles
    /** Section titles - 22sp bold */
    val titleLarge = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold
    )
    /** Card titles - 20sp semibold */
    val titleMedium = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    )
    /** Small titles - 16sp semibold */
    val titleSmall = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )

    // MARK: - Section Headers
    /** Section header - 14sp bold, uppercase */
    val sectionHeader = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp
    )

    // MARK: - Body
    /** Standard body text - 15sp regular */
    val body = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal
    )
    /** Emphasized body - 14sp medium */
    val bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium
    )
    /** Small body - 13sp regular */
    val bodySmall = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal
    )

    // MARK: - Captions
    /** Primary caption - 12sp regular */
    val caption = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    )
    /** Secondary caption - 11sp medium */
    val captionMedium = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
    )

    // MARK: - Special
    /** Button text - 16sp bold */
    val button = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
    /** Small button - 14sp bold */
    val buttonSmall = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    /** Tag/badge text - 10sp semibold */
    val tag = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp
    )
}
```

### AppSpacing.kt

Consistent spacing scale:

```kotlin
package io.designtoswiftui.countdown2binge.theme

import androidx.compose.ui.unit.dp

/**
 * Centralized spacing tokens for the app.
 */
object AppSpacing {
    /** 4dp - Minimal spacing (between icon and label) */
    val xs = 4.dp
    /** 8dp - Tight spacing (between related items) */
    val sm = 8.dp
    /** 12dp - Compact spacing */
    val md = 12.dp
    /** 16dp - Standard spacing (most common) */
    val lg = 16.dp
    /** 20dp - Comfortable spacing */
    val xl = 20.dp
    /** 24dp - Generous spacing (between sections) */
    val xxl = 24.dp
    /** 32dp - Large spacing */
    val xxxl = 32.dp
    /** 40dp - Extra large (bottom padding, hero spacing) */
    val huge = 40.dp

    // MARK: - Semantic Spacing
    /** Horizontal padding for screen content */
    val horizontalPadding = 20.dp
    /** Standard section spacing */
    val sectionSpacing = 24.dp
    /** Card internal padding */
    val cardPadding = 16.dp
}
```

### AppRadius.kt

Consistent corner radii:

```kotlin
package io.designtoswiftui.countdown2binge.theme

import androidx.compose.ui.unit.dp

/**
 * Centralized corner radius tokens for the app.
 */
object AppRadius {
    /** 4dp - Subtle rounding (badges) */
    val xs = 4.dp
    /** 6dp - Small rounding (small buttons) */
    val sm = 6.dp
    /** 8dp - Standard rounding (buttons) */
    val md = 8.dp
    /** 12dp - Card rounding */
    val lg = 12.dp
    /** 16dp - Large cards */
    val xl = 16.dp
    /** 50% for circles/pills */
    val full = 999.dp
}
```

### Theme.kt

Material theme configuration:

```kotlin
package io.designtoswiftui.countdown2binge.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.accent,
    secondary = AppColors.accentSecondary,
    background = AppColors.background,
    surface = AppColors.surface,
    onPrimary = AppColors.textPrimary,
    onSecondary = AppColors.textPrimary,
    onBackground = AppColors.textPrimary,
    onSurface = AppColors.textPrimary,
    error = AppColors.destructive
)

@Composable
fun Countdown2BingeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
```

### Usage Examples

```kotlin
// Colors
Text(
    text = "Hello",
    color = AppColors.textSecondary
)
Box(
    modifier = Modifier.background(AppColors.surface)
)

// Typography
Text(
    text = "SECTION TITLE",
    style = AppTypography.sectionHeader
)

// Spacing
Column(
    verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    modifier = Modifier.padding(horizontal = AppSpacing.horizontalPadding)
) {
    // content
}

// Radius
Box(
    modifier = Modifier.clip(RoundedCornerShape(AppRadius.lg))
)

// Buttons
PrimaryButton(
    text = "Follow",
    onClick = { }
)

SecondaryButton(
    text = "Following",
    onClick = { }
)
```

### Development Workflow (IMPORTANT)

**This is the standard workflow for all UI development:**

#### Setting Up Design System (First Time Only)

Before creating the theme folder and design tokens, **review all designs first**:
1. Look through `screenshots/` folder to see the visual language
2. Identify common colors, fonts, and spacing used across designs
3. Note the accent colors, card styles, button styles
4. Create theme files (`AppColors`, `AppTypography`, etc.) based on what you observed
5. This ensures your tokens match what's actually needed, not guesses

#### Daily Workflow

1. **Look at the designs first**
   - Check `screenshots/` folder for reference designs
   - Understand exactly what needs to be built
   - Note colors, fonts, spacing, layout from the design

2. **Check the design system**
   - Before writing any styling code, check `theme/` for existing tokens
   - Does `AppColors` have the color you need?
   - Does `AppTypography` have the font style?
   - Does `AppSpacing` have the spacing value?

3. **Use existing tokens or add new ones**
   - If token exists → use it
   - If token doesn't exist → add it to the appropriate theme file, then use it
   - **NEVER** inline colors, fonts, or spacing values

4. **Build to match the design exactly**
   - Use the design system tokens to achieve the design
   - The design is the source of truth for appearance
   - The design system is the source of truth for implementation

### Migration Strategy

When updating existing code:

1. **Don't refactor everything at once** - Fix violations file by file
2. **New code uses design system** - All new features use tokens
3. **Fix when touching** - When modifying a file, update to tokens
4. **Extract components BEFORE applying tokens** - When refactoring a large file:
   - First extract components to separate files
   - Then apply design system tokens to each extracted file
   - This avoids fixing code that will be moved/changed

---

## Summary

1. **Structure**: Feature-based packages, one main composable per file
2. **Layout**: Always adaptive, use theme constants, no fixed sizes
3. **Composition**: Small composables, clean body, helpers at bottom or in separate files
4. **Navigation**: Navigation Compose with sealed class routes
5. **State**: ViewModel + StateFlow, state hoisting pattern
6. **Style**: KDoc comments, ordered parameters, meaningful names
7. **Component Styles**: Reusable button/component composables
8. **Design System**: Centralized tokens for colors, typography, spacing, radius
