# Followed Show Detail View - Android Implementation Guide

This document provides a comprehensive reference for implementing the **FollowedShowDetailView** on Android. This screen displays detailed information for shows the user is already following, with a tabbed interface for different content sections.

---

## Table of Contents

1. [Overview](#overview)
2. [Screen Architecture](#screen-architecture)
3. [Design System Reference](#design-system-reference)
4. [Component Breakdown](#component-breakdown)
5. [Tab Content Sections](#tab-content-sections)
6. [Data Loading & State](#data-loading--state)
7. [Navigation & Actions](#navigation--actions)
8. [Accessibility](#accessibility)

---

## Overview

The **FollowedShowDetailView** is the detail screen for shows the user has already added to their collection. Unlike the standard ShowDetailView (used for discovery), this view focuses on **binge tracking** and **countdown information**.

### Key Differences from ShowDetailView

| Feature | ShowDetailView | FollowedShowDetailView |
|---------|---------------|------------------------|
| Primary Purpose | Discovery & Add | Track & Countdown |
| First Tab | Overview | Binge View |
| CTA Button | Follow/Add | N/A (already following) |
| Countdown | Not shown | Prominent feature |
| Season Rankings | Not shown | Shown in Binge tab |

### Visual Structure

```
┌─────────────────────────────────────┐
│        Stretchy Header              │
│      (Backdrop + Logo)              │
│           420dp                     │
├─────────────────────────────────────┤
│  Watch Now (Streaming Providers)    │  ← Optional section
├─────────────────────────────────────┤
│  [Binge View]  [Spinoffs]  [Info]   │  ← Tab Bar
├─────────────────────────────────────┤
│                                     │
│         Tab Content Area            │
│                                     │
│                                     │
└─────────────────────────────────────┘
```

---

## Screen Architecture

### Tab System

```kotlin
enum class FollowedShowTab(val displayName: String) {
    BINGE("Binge View"),
    SPINOFFS("Spinoffs"),
    INFO("Info")
}
```

**Dynamic Tab Visibility:**
- `BINGE` and `INFO` are always shown
- `SPINOFFS` is only shown when the show has related shows (spinoffs/prequels/sequels)

```kotlin
val availableTabs: List<FollowedShowTab> = buildList {
    add(FollowedShowTab.BINGE)
    if (relatedShowIds.isNotEmpty()) {
        add(FollowedShowTab.SPINOFFS)
    }
    add(FollowedShowTab.INFO)
}
```

### State Management

```kotlin
data class FollowedShowDetailState(
    val show: Show,
    val selectedTab: FollowedShowTab = FollowedShowTab.BINGE,

    // Spinoff support
    val relatedShowIds: List<Int> = emptyList(),
    val relatedShows: List<Show> = emptyList(),
    val isLoadingSpinoffs: Boolean = false,

    // Info tab content
    val videos: List<TMDBVideo> = emptyList(),
    val cast: List<TMDBCastMember> = emptyList(),
    val isSynopsisExpanded: Boolean = false,

    // Streaming providers
    val streamingServices: List<StreamingService> = emptyList()
)
```

---

## Design System Reference

### Colors

| Token | Value | Usage |
|-------|-------|-------|
| `background` | `#000000` | Screen background |
| `cardBackground` | `#0D0D0D` | Card backgrounds |
| `accent` | `rgb(74, 199, 184)` / `#4AC7B8` | Primary accent (teal) |
| `textPrimary` | `#FFFFFF` | Primary text |
| `textSecondary` | `#FFFFFF` @ 70% | Secondary text |
| `textTertiary` | `#FFFFFF` @ 50% | Tertiary text |
| `border` | `#252525` | Card borders |

### Spacing

| Token | Value | Usage |
|-------|-------|-------|
| `xs` | 6dp | Extra small gaps |
| `sm` | 8dp | Small gaps |
| `md` | 12dp | Medium gaps |
| `lg` | 16dp | Large gaps |
| `xl` | 20dp | Section gaps |
| `xxl` | 24dp | Tab spacing |
| `horizontalPadding` | 20dp | Screen edge padding |
| `massive` | 40dp | Bottom padding |

### Typography

| Style | Size | Weight |
|-------|------|--------|
| `titleLarge` | 22sp | Bold |
| `titleSmall` | 16sp | Medium |
| `sectionHeader` | 14sp | Bold |
| `subsectionHeader` | 13sp | Semibold |
| `caption` | 12sp | Regular |
| `captionSmall` | 11sp | Medium |
| `captionXSmall` | 10sp | Bold |
| `button` | 16sp | Heavy, Condensed |

### Corner Radius

| Token | Value |
|-------|-------|
| `card` | 12dp |
| `sm` | 6dp |
| `xs` | 4dp |
| `md` | 8dp |

---

## Component Breakdown

### 1. Stretchy Show Header

A parallax header that stretches when pulled down, displaying the show's backdrop and logo.

**Dimensions:**
- Height: `420dp`
- Logo max size: `280dp x 100dp`

**Behavior:**
- On scroll down (overscroll): Header scales up proportionally
- Uses scale transform anchored at bottom center

**Gradient Overlay:**
```kotlin
// Bottom gradient for text readability
LinearGradient(
    colors = listOf(
        Color.Transparent,
        Color.Black.copy(alpha = 0.4f),
        Color.Black.copy(alpha = 0.85f),
        Color.Black
    ),
    startY = 0f,      // top
    endY = 200dp      // 200dp gradient height
)
```

**Fallback Behavior:**
- If no backdrop: Show gradient placeholder with app icon (10% opacity)
- If no logo: Show title text (36sp, black weight, uppercase)

```kotlin
@Composable
fun StretchyShowHeader(
    show: Show,
    height: Dp = 420.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        // Backdrop image with stretch effect
        AsyncImage(
            model = show.backdropUrl,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .stretchyModifier(height)
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter)
                .background(bottomGradient)
        )

        // Logo or title
        if (show.logoPath != null) {
            AsyncImage(
                model = show.logoUrl,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .heightIn(max = 100.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        } else {
            Text(
                text = show.name.uppercase(),
                style = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
}
```

---

### 2. Streaming Providers Section

Horizontal row of streaming service buttons that deep link to the respective apps.

**Layout:**
- Section header: "Watch Now"
- Horizontal scroll of provider logos
- Logo size: `48dp x 48dp`
- Spacing between logos: `12dp (md)`
- Corner radius: `6dp (sm)`

```kotlin
@Composable
fun StreamingProvidersSection(
    services: List<StreamingService>,
    onServiceClick: (StreamingService) -> Unit
) {
    if (services.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp)
    ) {
        Text(
            text = "Watch Now",
            style = AppTypography.sectionHeader,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(services) { service ->
                AsyncImage(
                    model = service.logoUrl,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onServiceClick(service) }
                )
            }
        }
    }
}
```

---

### 3. Dynamic Tab Bar

A horizontally scrolling tab bar with animated underline indicator.

**Design Specs:**
- Tab text: `13sp`, semibold, uppercase
- Tab spacing: `24dp (xxl)`
- Selected color: Accent (`#4AC7B8`)
- Unselected color: `textTertiary` (white @ 50%)
- Underline: `2dp` height, accent color
- Haptic feedback on tab change

```kotlin
@Composable
fun DynamicTabBar(
    tabs: List<FollowedShowTab>,
    selectedTab: FollowedShowTab,
    onTabSelected: (FollowedShowTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab

            Column(
                modifier = Modifier.clickable {
                    if (!isSelected) {
                        // Haptic feedback
                        HapticFeedback.performHapticFeedback(HapticFeedbackType.LightImpact)
                        onTabSelected(tab)
                    }
                }
            ) {
                Text(
                    text = tab.displayName.uppercase(),
                    style = AppTypography.subsectionHeader,
                    color = if (isSelected) AppColors.accent else AppColors.textTertiary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(AppColors.accent)
                    )
                }
            }
        }
    }
}
```

---

## Tab Content Sections

### Tab 1: Binge View

The Binge View tab shows countdown and season ranking information.

#### Countdown to Finale Section

**Visibility:** Only shown when `show.inProduction == true` AND a current season exists.

**Layout Structure:**
```
┌─────────────────────────────────────┐
│  Countdown to Finale                │  ← Section header
├─────────────────────────────────────┤
│  ┌────┐:┌────┐:┌────┐:┌────┐       │
│  │ DD ││ HH ││ MM ││ SS │       │  ← Countdown boxes
│  │DAYS││ HRS││ MIN││ SEC│       │
│  └────┘ └────┘ └────┘ └────┘       │
│                                     │
│  ┌──────────────────┐              │
│  │ SEASON FINALE    │              │  ← Event badge (capsule)
│  └──────────────────┘              │
│                                     │
│  ┌─────┐                           │
│  │S01  │  EPISODE TITLE            │  ← Episode badge + title
│  │E05  │                           │
│  └─────┘                           │
│                                     │
│  ⓘ BINGE STATUS: READY...          │  ← Status text
└─────────────────────────────────────┘
```

**Countdown Box Specs:**
- Box background: `white @ 8%`
- Box border: `white @ 15%`, 1dp
- Corner radius: `6dp (sm)`
- Number font: Heavy weight, dynamic size based on width
- Colon color: Accent (`#4AC7B8`), 32sp

**Dynamic Size Calculation:**
```kotlin
val boxWidth = (containerWidth - totalSpacing) / 4
val colonWidth = 12.dp
val spacing = 4.dp

// Font size scales with box width
val fontSize = when {
    value >= 100 -> min(boxWidth * 0.45f, 32.sp)
    else -> min(boxWidth * 0.65f, 44.sp)
}
```

**Countdown Value Display:**
```kotlin
fun formatCountdownValue(value: Int?): String {
    return when {
        value == null -> "--"      // Unknown/TBD
        value >= 100 -> "$value"   // No leading zero for 3+ digits
        else -> "%02d".format(value)
    }
}
```

**Event Badge Logic:**
```kotlin
val eventLabel = when {
    show.status == ShowStatus.ENDED ||
    show.status == ShowStatus.CANCELLED -> "SERIES COMPLETE"

    season.seasonNumber == show.numberOfSeasons &&
    show.status != ShowStatus.RETURNING -> "SERIES CONCLUSION"

    else -> "SEASON FINALE"
}
```

**Episode Badge:**
- Background: Accent color
- Text: Black, caption size
- Format: `S01 • E05`
- Corner radius: `4dp (xs)`

**Status Text Examples:**
```kotlin
val statusText = when {
    season.isComplete && watchedCount == 0 ->
        "BINGE STATUS: READY. ALL $totalCount EPISODES AVAILABLE."
    season.isComplete && watchedCount == totalCount ->
        "BINGE STATUS: COMPLETE. SEASON FINISHED."
    season.isComplete ->
        "BINGE STATUS: IN PROGRESS. ${totalCount - watchedCount} EPISODES REMAINING."
    season.isAiring ->
        "BINGE STATUS: IMMINENT. SEASON CURRENTLY AIRING."
    else ->
        "BINGE STATUS: ANTICIPATED. PREMIERE APPROACHING."
}
```

---

#### Top Ranked Seasons Section

**Visibility:** Only shown when `show.seasons.count > 1`

Shows top 3 seasons ranked by TMDB vote average.

**Season Rank Card Layout:**
```
┌─────────────────────────────────────────────────┬───┐
│  RATED BEST SEASON          (watermark: S1)     │ 1 │ ← Rank badge
│                                                 ├───┤
│  Season 1 · 2019                               │    │
│  10 Episodes • Masterpiece                     │8.9 │ ← Rating score
│                                                │GOLD│
│  ████████████████░░░░░░                        │    │ ← Rating bar
└─────────────────────────────────────────────────────┘
```

**Card Dimensions:**
- Height: `140dp`
- Padding: `16dp (lg)`
- Corner radius: `12dp (card)`

**Rank Colors:**
```kotlin
val rankColor = when (rank) {
    1 -> Color(0xFFFFD700)  // Gold
    2 -> Color(0xFFC0C0C0)  // Silver
    3 -> Color(0xFFCD853F)  // Bronze
    else -> AppColors.textSecondary
}
```

**Rank Labels:**
```kotlin
val rankLabel = when (rank) {
    1 -> "RATED BEST SEASON"
    2 -> "SILVER RANKING"
    3 -> "BRONZE RANKING"
    else -> "RANKING #$rank"
}
```

**Season Descriptor (based on rank):**
```kotlin
val episodeDescriptor = when (rank) {
    1 -> "Masterpiece"
    2 -> "High Fidelity"
    3 -> "Origins"
    else -> "Classic"
}
```

**Watermark Text:**
- Font: 140sp, bold
- Color: `white @ 3%`
- Position: Bottom-right, offset (30dp, 40dp) to clip off edge

**Rank Badge:**
- Position: Top-right corner
- Shape: Asymmetric rounded rectangle (0 top-left, md bottom-left, 0 bottom-right, card top-right)
- Width: 36dp, Height: 44dp
- Text: Black, titleLarge weight

---

### Tab 2: Spinoffs

Shows related shows (spinoffs, prequels, sequels).

#### Premium Gate

If user is not premium, show locked state:

```
┌─────────────────────────────────────┐
│  SPINOFF COLLECTION ────────────    │  ← Section header with line
├─────────────────────────────────────┤
│  ┌─────────────────────────────┐    │
│  │ (Blurred placeholder cards) │    │  ← blur(6dp)
│  │                             │    │
│  │       🔒                    │    │
│  │  Unlock Spinoff Collections │    │
│  │  Discover connected shows   │    │
│  │       [UNLOCK]              │    │
│  └─────────────────────────────┘    │
└─────────────────────────────────────┘
```

**Lock Overlay:**
- Lock icon: 28sp, accent color
- Title: `titleSmall`, white
- Subtitle: `caption`, white @ 60%
- Button: Capsule, accent background, black text

#### Unlocked Content

Uses `LandscapeShowCard` for each spinoff.

**LandscapeShowCard Layout:**
```
┌─────────────────────────────────────────────────┬───┐
│                                                 │ 1 │ ← Rank number
│            Backdrop Image (16:9)                │   │
│  ┌────────┐                                     │   │
│  │ SERIES │                                     │   │
│  └────────┘                                     │   │
├─────────────────────────────────────────────────────┤
│  CURRENTLY AIRING                               │   │
│  BETTER CALL SAUL                               │   │
│  ★ 9.5 Rating • 6 Seasons                       │   │
│                                                 │   │
│  A criminal lawyer becomes a morally dubious... │   │
│                                                 │   │
│  ┌─────────────────────────────────────────┐    │   │
│  │  🔖 FOLLOW                               │    │   │
│  └─────────────────────────────────────────┘    │   │
└─────────────────────────────────────────────────────┘
```

**Card Specs:**
- Image aspect ratio: 16:9
- Badge position: Top-left, `12dp` padding
- Rank badge position: Top-right corner
- Content padding: `12dp`
- Overview: 3 line limit

**Subtitle Text Logic:**
```kotlin
val subtitleText = when (show.status) {
    ShowStatus.ENDED -> "CRITICALLY ACCLAIMED"
    ShowStatus.RETURNING -> "CURRENTLY AIRING"
    ShowStatus.IN_PRODUCTION -> "IN PRODUCTION"
    else -> "SPINOFF SERIES"
}
```

---

### Tab 3: Info

Contains show metadata, trailers, cast, and technical specs.

#### Info Section

**Synopsis:**
- Expandable text with "MORE" / "LESS" toggle
- When collapsed: 3-4 line limit

**Meta Row:**
```
5 SEASONS • Returning Series
```
- Font: `captionSmall`, tracking 1
- Color: `white @ 50%`

#### Genre Tags Section

Horizontal row of genre tags (max 3).

**Genre Tag Style:**
- Background: Filled capsule
- Text: `captionSmall`
- Color: Accent

#### Trailers & Clips Section

Horizontal scroll of trailer cards (max 6).

**Section Header:** "TRAILERS & CLIPS"
- Font: `sectionHeader`
- Tracking: 1.5
- Color: White

#### Cast & Crew Section

Horizontal scroll of person cards (max 10).

**Section Header:** "CAST & CREW"

**Person Card:**
- Circular profile image
- Name below
- Character/role as subtitle

#### Technical Specs Section

**Section Header:** "TECHNICAL SPECS"

**Spec Badges (horizontal flow layout):**
```
┌──────────────┐ ┌─────────┐ ┌─────────────┐
│ 4K ULTRA HD  │ │ HDR10+  │ │ DOLBY ATMOS │
└──────────────┘ └─────────┘ └─────────────┘
┌───────────────┐ ┌────────┐ ┌────────┐
│ SPATIAL AUDIO │ │ 💬 CC  │ │ 🔊 AD  │
└───────────────┘ └────────┘ └────────┘
```

**Badge Style:**
- Border: Capsule stroke, accent @ 50%, 1dp
- Text: `captionSmall`, accent color
- Padding: horizontal 12dp, vertical 8dp

**Info Rows:**
```
CREATED BY      Vince Gilligan
GENRE           Drama, Crime, Thriller
NETWORK         AMC, Netflix
AUDIO LANGUAGES English, Spanish, French, Japanese
```

---

## Data Loading & State

### Initial Load Sequence

```kotlin
// On view appear, load data in parallel
LaunchedEffect(show.id) {
    // 1. Load related show IDs from local cache
    val relatedIds = showRepository.getRelatedShowIds(show.id)

    // 2. Parallel data fetching
    coroutineScope {
        // Fetch full show details for related shows
        launch {
            if (relatedIds.isNotEmpty()) {
                val shows = relatedIds.mapNotNull { id ->
                    try { tmdbService.getShowDetails(id) }
                    catch (e: Exception) { null }
                }
                updateState { copy(relatedShows = shows) }
            }
        }

        // Fetch videos
        launch {
            val videos = tmdbService.getShowVideos(show.id)
            updateState { copy(videos = videos) }
        }

        // Fetch cast
        launch {
            val credits = tmdbService.getShowCredits(show.id)
            updateState { copy(cast = credits.cast) }
        }

        // Fetch streaming providers
        launch {
            val providers = tmdbService.getWatchProviders(show.id)
            val services = StreamingDeepLinkService.getAvailableServices(providers)
            updateState { copy(streamingServices = services) }
        }
    }
}
```

---

## Navigation & Actions

### Toolbar

**Share Button:**
- Position: Top-right
- Icon: `share` (square.and.arrow.up equivalent)
- Color: `white @ 80%`

### Spinoff Navigation

Tapping a spinoff card navigates to another `FollowedShowDetailView` for that spinoff.

```kotlin
// Self-referential navigation
navController.navigate("followed_show_detail/${spinoff.id}")
```

### Streaming Deep Links

Opening streaming service opens the relevant app or falls back to web.

```kotlin
fun openStreamingService(service: StreamingService) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(service.deepLinkUrl ?: service.webUrl)
    }
    context.startActivity(intent)
}
```

### Paywall

When non-premium users tap "Unlock" on spinoffs:
```kotlin
showPaywall = true
// Present PaywallView as full-screen modal
```

---

## Accessibility

### Tab Bar

```kotlin
Modifier
    .semantics {
        contentDescription = "Content tabs"
        role = Role.TabList
    }
```

Each tab:
```kotlin
Modifier.semantics {
    contentDescription = tab.displayName
    role = Role.Tab
    selected = isSelected
    stateDescription = if (isSelected) "Currently selected" else "Double tap to select"
}
```

### Countdown Timer

```kotlin
Modifier.semantics {
    contentDescription = "Countdown: $days days, $hours hours, $minutes minutes, $seconds seconds until finale"
    liveRegion = LiveRegionMode.Polite
}
```

### Season Rank Cards

```kotlin
Modifier.semantics {
    contentDescription = "Season $seasonNumber, ranked $rank. Rating $rating out of 10. $episodeCount episodes."
}
```

---

## Summary Checklist

- [ ] Stretchy parallax header with backdrop and logo
- [ ] Dynamic tab bar (2-3 tabs based on spinoff availability)
- [ ] Streaming providers horizontal row with deep links
- [ ] Countdown to Finale section with live timer
- [ ] Top Ranked Seasons with medal colors
- [ ] Spinoffs tab with premium gate
- [ ] Landscape show cards with follow action
- [ ] Info tab with synopsis, genres, trailers, cast, specs
- [ ] Share toolbar button
- [ ] Full-screen paywall modal
- [ ] Self-referential navigation for spinoffs
