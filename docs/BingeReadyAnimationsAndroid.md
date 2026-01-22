# Binge Ready Animations Guide (Android)

This document explains the animations and interactions on the iOS Binge Ready screen for Android implementation.

---

## Screen Structure

```
┌─────────────────────────────────────┐
│  BINGE READY (header)               │
├─────────────────────────────────────┤
│                                     │
│        SHOW NAME (uppercase)        │
│                                     │
│     ┌─────────────────────────┐     │
│     │                         │     │
│     │    Fanned Card Stack    │     │
│     │   (seasons for show)    │     │
│     │                         │     │
│     └─────────────────────────┘     │
│                                     │
│       [====Progress Bar====]        │
│                                     │
├─────────────────────────────────────┤
│  [img][img][img][img] Thumbnail Nav │
└─────────────────────────────────────┘
```

---

## Interaction 1: Tap on Card Stack (Front Card)

**File:** `BingeReadyView.swift:265-269`

### What Happens
When user taps the **front card** in the stack, it navigates to the show detail screen.

### iOS Implementation
```swift
onTapCard: {
    let seasonIndex = selectedIndices[group.show.id] ?? 0
    let season = seasonIndex < group.seasons.count ? group.seasons[seasonIndex] : group.seasons.first!
    selectedItem = BingeReadyItem(show: group.show, season: season)
}
```

### Navigation
- Uses `navigationDestination(item:)` with `selectedItem` state
- Navigates to `FollowedShowDetailView(show: item.show)`
- Standard iOS push navigation with slide-from-right animation

### Android Equivalent
```kotlin
onClick = {
    if (isFrontCard) {
        val season = seasons.getOrElse(currentIndex) { seasons.first() }
        onNavigateToShowDetail(show.id, season.seasonNumber)
    }
}
```

Use standard `NavController.navigate()` with default transition.

---

## Interaction 2: Tap on Thumbnail Nav

**File:** `BingeReadyView.swift:322-350`

### What Happens
When user taps a thumbnail in the bottom nav, the **entire card stack section** animates to show the selected show.

### iOS Implementation
```swift
Button {
    scrollToShow(index: index)
} label: {
    // Thumbnail image
}
```

### The `scrollToShow` Function
```swift
private func scrollToShow(index targetIndex: Int) {
    // Don't scroll if already at target
    guard targetIndex != currentShowIndex else { return }

    // Set direction based on target position
    navigationDirection = targetIndex > currentShowIndex ? .right : .left

    // Directly animate to target
    withAnimation(.spring(response: 0.5, dampingFraction: 0.8)) {
        currentShowIndex = targetIndex
    }
}
```

### Animation Details

| Property | Value |
|----------|-------|
| **Animation Type** | Spring |
| **Response** | 0.5 seconds |
| **Damping Fraction** | 0.8 |

---

## Card Drop Transition (When Switching Shows)

**File:** `BingeReadyView.swift:460-501`

When `currentShowIndex` changes, the card stack uses a custom **drop transition**.

### Direction Logic
- **Navigating Forward** (higher index): Cards drop from TOP, exit to BOTTOM
- **Navigating Backward** (lower index): Cards rise from BOTTOM, exit to TOP

### Transition: `cardDropFromTop` (Forward Navigation)

```swift
static var cardDropFromTop: AnyTransition {
    .asymmetric(
        insertion: .modifier(
            active: CardDropModifier(offset: -250, opacity: 0, scale: 0.8),
            identity: CardDropModifier(offset: 0, opacity: 1, scale: 1)
        ),
        removal: .modifier(
            active: CardDropModifier(offset: 350, opacity: 0, scale: 0.8),
            identity: CardDropModifier(offset: 0, opacity: 1, scale: 1)
        )
    )
}
```

| State | Y Offset | Opacity | Scale |
|-------|----------|---------|-------|
| **Insertion Start** | -250pt | 0 | 0.8 |
| **Insertion End** | 0pt | 1 | 1.0 |
| **Removal Start** | 0pt | 1 | 1.0 |
| **Removal End** | +350pt | 0 | 0.8 |

### Transition: `cardDropFromBottom` (Backward Navigation)

| State | Y Offset | Opacity | Scale |
|-------|----------|---------|-------|
| **Insertion Start** | +250pt | 0 | 0.8 |
| **Insertion End** | 0pt | 1 | 1.0 |
| **Removal Start** | 0pt | 1 | 1.0 |
| **Removal End** | -350pt | 0 | 0.8 |

### Android Implementation

```kotlin
// Track direction
val direction = if (targetIndex > currentIndex) Direction.FORWARD else Direction.BACKWARD

AnimatedContent(
    targetState = currentShowIndex,
    transitionSpec = {
        if (direction == Direction.FORWARD) {
            // Drop from top
            slideInVertically { -it } + fadeIn() + scaleIn(initialScale = 0.8f) togetherWith
            slideOutVertically { it } + fadeOut() + scaleOut(targetScale = 0.8f)
        } else {
            // Rise from bottom
            slideInVertically { it } + fadeIn() + scaleIn(initialScale = 0.8f) togetherWith
            slideOutVertically { -it } + fadeOut() + scaleOut(targetScale = 0.8f)
        }
    }
) { showIndex ->
    CardStackContent(groups[showIndex])
}
```

---

## Thumbnail Nav Visual States

**File:** `BingeReadyView.swift:322-350`

### Thumbnail Dimensions
| Property | Value |
|----------|-------|
| **Width** | 56pt |
| **Height** | 84pt |
| **Corner Radius** | 8pt |
| **Spacing** | 16pt between items |
| **Horizontal Padding** | 20pt on edges |

### Selected State
| Property | Selected | Unselected |
|----------|----------|------------|
| **Opacity** | 1.0 | 0.6 |
| **Border** | 2pt white stroke | None |

### Auto-Scroll Behavior
When `currentShowIndex` changes, the ScrollView auto-scrolls to center the selected thumbnail:

```swift
.onChange(of: currentShowIndex) { _, newIndex in
    withAnimation {
        proxy.scrollTo(newIndex, anchor: .center)
    }
}
```

---

## Fanned Card Stack Animations

**File:** `BingeReadyCardStack.swift`

The card stack within each show uses a **fanned layout** with swipe gestures.

### Fan Configuration
| Property | Value |
|----------|-------|
| **Fan Spread** | 28pt horizontal |
| **Fan Y Offset** | 8pt vertical drop per card |
| **Fan Rotation** | 5° per position |
| **Scale Step** | 6% reduction per position |
| **Swipe Threshold** | 80pt |

### Card Swipe Gestures (Within Same Show)
- **Swipe RIGHT**: Next season (card goes to back)
- **Swipe LEFT**: Previous season
- **Swipe DOWN**: Mark watched
- **Swipe UP**: Delete show (with confirmation)

### Spring Animation
```swift
.interpolatingSpring(stiffness: 300, damping: 40)
```

Android equivalent:
```kotlin
spring(stiffness = 300f, dampingRatio = 0.65f)
```

---

## Summary: Two Tap Actions

| Tap Target | Action | Animation |
|------------|--------|-----------|
| **Front Card (Stack)** | Navigate to Show Detail | Standard push navigation |
| **Thumbnail (Bottom Nav)** | Switch to that show | Card drop + spring (0.5s, 0.8 damping) |

---

## Complete Animation Timeline (Thumbnail Tap)

1. **User taps thumbnail** at index 3 (current is 0)
2. **Direction calculated**: forward (3 > 0)
3. **Navigation direction set**: `.right`
4. **Spring animation starts** (response: 0.5s, damping: 0.8)
5. **Current card stack**:
   - Fades out (opacity: 1 → 0)
   - Scales down (scale: 1 → 0.8)
   - Moves down (y: 0 → +350pt)
6. **New card stack**:
   - Fades in (opacity: 0 → 1)
   - Scales up (scale: 0.8 → 1)
   - Drops from top (y: -250pt → 0)
7. **Thumbnail nav**:
   - Previous thumbnail: opacity 1 → 0.6, border removed
   - New thumbnail: opacity 0.6 → 1, white border appears
   - ScrollView auto-centers on new thumbnail
8. **Show name** updates with same spring animation
9. **Progress bar** updates (if show is in production)
