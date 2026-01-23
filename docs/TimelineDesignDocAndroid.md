# Timeline Design Document (Android)

This document provides complete design specifications for the Timeline screen on iOS for Android implementation.

---

## Overview & Purpose

The Timeline screen visualizes the lifecycle of a user's followed TV shows. It displays shows that are currently in production (airing, premiering soon, or awaiting renewal) and tracks their progression through different states.

---

## Show Lifecycle

Shows move through the Timeline in a continuous cycle:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                         │
│   ┌─────────┐      ┌──────────────┐      ┌─────────────┐               │
│   │   TBD   │ ──── │  Premiering  │ ──── │   Airing    │               │
│   │         │      │    Soon      │      │  (Hero)     │               │
│   └─────────┘      └──────────────┘      └─────────────┘               │
│        ▲                                        │                       │
│        │                                        ▼                       │
│        │                               ┌─────────────┐                  │
│        └────────────────────────────── │ Binge Ready │                  │
│              (day after finale)        └─────────────┘                  │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### State Transitions

| From | To | Trigger |
|------|----|---------|
| **TBD** | **Premiering Soon** | API returns a premiere date for the next season |
| **Premiering Soon** | **Airing (Hero)** | The premiere date arrives; season begins airing |
| **Airing (Hero)** | **Binge Ready** | The season finale airs; all episodes available |
| **Binge Ready** | **TBD** | Day after finale; show awaits next season info |
| **TBD** | *(Removed)* | Show is canceled or ended (no more seasons) |

### State Definitions

| State | Description | Display Location |
|-------|-------------|------------------|
| **Airing** | Currently broadcasting new episodes | Hero Card Stack (top) |
| **Premiering Soon** | Has a confirmed premiere date in the future | "Premiering Soon" section |
| **TBD (Anticipated)** | Renewed/in production but no premiere date yet | "Anticipated" section |
| **Binge Ready** | Season complete, ready to watch | Binge Ready screen (separate tab) |

---

## Section Visibility Logic

Sections are conditionally displayed based on what data exists. This ensures users only see relevant content.

### Display Rules

| User's Shows | What Displays |
|--------------|---------------|
| **No shows followed** | Hero placeholder + 2 empty sections (Premiering + TBD) with placeholder cards |
| **Only TBD shows** | Hero placeholder + TBD section only |
| **Only Premiering Soon shows** | Hero placeholder + Premiering Soon section only |
| **Only Airing shows** | Hero card stack + slot machine only (no sections) |
| **Airing + Premiering** | Hero + Premiering Soon section |
| **Airing + TBD** | Hero + TBD section |
| **Airing + Premiering + TBD** | Hero + Premiering Soon + TBD sections |
| **Shows exist but all in Binge Ready** | "Nothing on Timeline" empty state |

### Empty State (New Users)

When a user has no followed shows, display the "onboarding" view:
- Hero placeholder card (app icon/branding image)
- Premiering Soon section with 3 empty slot cards
- Anticipated section with 3 empty slot cards

This previews what the timeline will look like once they follow shows.

### Empty Slot Cards

Each section displays up to **3 cards maximum**. If a section has fewer than 3 shows:
- Fill remaining slots with empty placeholder cards
- Empty cards show dashed borders and "EMPTY SLOTS" text
- Only shown in the onboarding/empty state (not when user has some shows)

---

## Section Expand/Collapse Behavior

### Synchronized Toggle

- Both "Premiering Soon" and "Anticipated" sections share **one expand/collapse state**
- Tapping the chevron on either section toggles **both sections together**
- This ensures consistent visual rhythm across the timeline

### Visual States

| State | Card Type | Card Height |
|-------|-----------|-------------|
| **Expanded** | Landscape backdrop cards | 190dp |
| **Collapsed** | Portrait poster cards | 320dp |

### Persistence

- Expand/collapse state is saved to device storage (DataStore on Android)
- State persists across app launches
- Key: `timelineSectionsExpanded` (Boolean)

---

## Slot Machine Countdown

The slot machine displays the countdown for the currently selected hero card.

### Behavior

- Shows number of days until finale (for airing shows) or premiere (for premiering shows)
- Animates when:
  - User swipes to a different card in the hero stack
  - Countdown value changes (new day)
- Can toggle between "Days" and "Episodes" display mode (user setting)

### When No Airing Shows

- Display placeholder state with "—" instead of numbers
- Slot machine still visible but inactive

---

## Color Palette

| Name | Hex | RGB | Usage |
|------|-----|-----|-------|
| **Teal Accent** | `#73E6B3` | `rgb(115, 230, 179)` / `(0.45, 0.90, 0.70)` | Premiering Soon, Ending Soon accents |
| **Teal Accent 40%** | `#73E6B366` | Above at 40% opacity | Empty slot borders (premiering) |
| **Muted Gray** | `#666666` | `rgb(102, 102, 102)` / `(0.4, 0.4, 0.4)` | Anticipated section accents |
| **Muted Gray 30%** | `#4D4D4D` | `(0.3, 0.3, 0.3)` | Empty slot borders (anticipated) |
| **Background** | `#000000` | Pure black | Screen background |
| **Card Background** | `#0D0D0D` | `rgb(13, 13, 13)` | Footer button background |
| **Card Border** | `#252525` | `rgb(37, 37, 37)` | Footer button border |
| **Last Updated Text** | `#555555` | `rgb(85, 85, 85)` | Header timestamp |
| **Section Label** | `#808080` | `(0.5, 0.5, 0.5)` | Section header text |
| **Disclosure Arrow** | `#666666` | `(0.4, 0.4, 0.4)` | Chevron icon |
| **Info Text** | `#595959` | `(0.35, 0.35, 0.35)` | Footer info text |

---

## Typography

### Screen Title
- **Font**: System Condensed Heavy
- **Size**: 36sp
- **Color**: White
- **Alignment**: Center

### Last Updated
- **Font**: System Regular
- **Size**: 11sp
- **Color**: `#555555`
- **Format**: "Last updated 2:30 pm"

### Section Header Title
- **Font**: System Semibold
- **Size**: 12sp
- **Letter Spacing**: 1.5sp
- **Color**: `#808080` (50% white)
- **Text Transform**: UPPERCASE

### Section Count Badge
- **Font**: System Bold
- **Size**: 11sp
- **Color**: Accent color (teal or gray based on section)

### Countdown Number (Expanded Cards)
- **Font**: System Heavy Rounded
- **Size**: 36sp (normal), 24sp (for "TBD")
- **Style**: Monospaced digits
- **Color**: White (premiering) or Muted Gray (anticipated)

### Countdown Label
- **Font**: System Heavy
- **Size**: 9sp
- **Letter Spacing**: 1sp
- **Color**: White 70% (premiering) or Muted Gray 70% (anticipated)

### Season Badge (Cards)
- **Font**: System Bold
- **Size**: 20sp (expanded landscape), 56sp (collapsed portrait)
- **Color**: White

### Footer Button Text
- **Font**: System Semibold
- **Size**: 12sp
- **Letter Spacing**: 1.5sp
- **Color**: White 90%

### Footer Info Text
- **Font**: System Medium
- **Size**: 10sp
- **Letter Spacing**: 1.5sp
- **Color**: `#595959` (35% white)

### Empty Slot Text
- **Font**: System Medium
- **Size**: 11sp
- **Letter Spacing**: 1.5sp
- **Color**: `#595959` (35% white)

---

## Layout & Spacing

### Screen Structure
```
┌────────────────────────────────────┐
│         TIMELINE (title)           │
│      Last updated 2:30 pm          │
├────────────────────────────────────┤
│                                    │
│     ┌─────────────────────────┐    │
│     │   Hero Card Stack       │    │
│     │   (fanned posters)      │    │
│     └─────────────────────────┘    │
│              │ (connector)         │
│         [SLOT MACHINE]             │
│              │ (connector)         │
├────────────────────────────────────┤
│  (3) PREMIERING SOON          ▼    │
│  ┌─────────────────────────────┐   │
│  │  Timeline Cards / Posters   │   │
│  └─────────────────────────────┘   │
├────────────────────────────────────┤
│  (2) ANTICIPATED                   │
│  ┌─────────────────────────────┐   │
│  │  Timeline Cards / Posters   │   │
│  └─────────────────────────────┘   │
├────────────────────────────────────┤
│      [VIEW FULL TIMELINE →]        │
│                                    │
│           ↻ Info text              │
└────────────────────────────────────┘
```

### General Spacing
| Element | Value |
|---------|-------|
| Horizontal padding | 20dp |
| Section vertical spacing | 30dp |
| Card spacing (expanded) | 30dp |
| Card spacing (collapsed) | 30dp |

### Header
| Element | Value |
|---------|-------|
| Top padding | 8dp |
| Horizontal padding | 16dp (12dp on small screens < 380dp) |

### Vertical Connectors
| Element | Value |
|---------|-------|
| Hero to Slot Machine | 24dp (8dp on small screens) |
| Slot Machine to Sections | 30dp |
| Line width | 2dp |
| Dash pattern | 4dp dash, 4dp gap |
| Color | Teal accent 80% opacity |

---

## Section Header Component

### Layout
```
┌──────────────────────────────────────────────┐
│  [●3]  PREMIERING SOON              ▼        │
│   ↑      ↑                          ↑        │
│  badge  title                    chevron     │
└──────────────────────────────────────────────┘
```

### Specifications
| Element | Value |
|---------|-------|
| Badge size | 24×24dp circle |
| Badge background | Accent color 15% opacity |
| Badge text color | Accent color |
| Badge container width | 80dp (centered) |
| Title font | System Semibold 12sp |
| Title tracking | 1.5sp |
| Chevron size | 12sp |
| Vertical padding | 12dp |
| Trailing padding | 24dp |

### Chevron Rotation
| State | Rotation |
|-------|----------|
| Expanded | 0° (pointing right) |
| Collapsed | 90° (pointing down) |

### Animation
- **Duration**: 0.25s
- **Easing**: ease-in-out

---

## Expanded State: TimelineShowCard

### Layout
```
┌────────────────────────────────────────────────┐
│  ┌──────┐  ┌────────────────────────────────┐  │
│  │  16  │  │                                │  │
│  │ DAYS │  │     Backdrop Image             │  │
│  │      │  │                           S3   │  │
│  └──────┘  └────────────────────────────────┘  │
│     ↑              ↑                    ↑      │
│  80dp wide    fills remaining       season     │
│               height: 175dp         badge      │
└────────────────────────────────────────────────┘
```

### Specifications
| Element | Value |
|---------|-------|
| Total card height | 190dp |
| Countdown area width | 80dp |
| Countdown vertical padding | 8dp |
| Countdown horizontal padding | 12dp |
| Backdrop height | 175dp |
| Backdrop corner radius | 24dp |
| Backdrop border | 1dp, white 40% 50% opacity |
| Season badge trailing | 12dp |
| Season badge bottom | 10dp |
| Card trailing padding | 24dp |

### Countdown Display
| Style | Value Format | Label |
|-------|--------------|-------|
| Premiering | `16` (2-digit padded) | "DAYS" |
| Anticipated (with date) | `2025` | "EXP." |
| Anticipated (no date) | `TBD` | "DATE" |

---

## Collapsed State: CompactPosterCard

### Layout
```
┌────────────────────────────────────────────────┐
│  ┌──────┐                    ┌──────────────┐  │
│  │  16  │                    │              │  │
│  │ DAYS │                    │   Poster     │  │
│  │      │                    │   Image      │  │
│  └──────┘                    │         S3   │  │
│                              │              │  │
│                              └──────────────┘  │
└────────────────────────────────────────────────┘
```

### Specifications
| Element | Value |
|---------|-------|
| Total card height | 320dp |
| Countdown area width | 80dp |
| Poster width | 230dp |
| Poster height | 310dp |
| Poster corner radius | 15dp |
| Poster border | 1dp, white 40% 50% opacity |
| Season badge size | 56sp Heavy |
| Season badge trailing | 16dp |
| Season badge bottom | 12dp |
| Card trailing padding | 24dp |

---

## Empty Slot Cards

### Expanded Empty (Landscape)
| Element | Value |
|---------|-------|
| Height | 190dp |
| Placeholder backdrop height | 175dp |
| Border style | Dashed (6dp dash, 4dp gap) |
| Border color | Accent 50% opacity |
| "EMPTY" text size | 11sp Medium |
| "EMPTY" tracking | 1.5sp |

### Collapsed Empty (Portrait)
| Element | Value |
|---------|-------|
| Height | 320dp |
| Placeholder poster size | 230×310dp |
| Border style | Dashed (6dp dash, 4dp gap) |
| Border color | Accent 50% opacity |

---

## Timeline Connector Line

The dashed vertical line runs through the section:

| Element | Value |
|---------|-------|
| X position | 40dp from left |
| Start Y | 48dp (below header badge) |
| End Y | Bottom of section |
| Line width | 2dp |
| Dash pattern | 4dp dash, 4dp gap |
| Color (Premiering) | Teal 80% opacity |
| Color (Anticipated) | White 40% 80% opacity |

---

## Expand/Collapse Animation

### Transition
| Property | Value |
|----------|-------|
| Duration | 0.3s |
| Easing | ease-in-out |

### What Animates
1. **Chevron rotation**: 0° ↔ 90°
2. **Content crossfade**: Landscape cards ↔ Portrait posters
3. **Height change**: Cards resize from 190dp to 320dp (or vice versa)

### State Persistence
- Expand/collapse state is saved to `UserDefaults`
- Key: `timelineSectionsExpanded`
- Both sections share the same state

---

## Footer Component

### View Full Timeline Button
| Element | Value |
|---------|-------|
| Height | 70dp |
| Corner radius | 16dp |
| Background | `#0D0D0D` |
| Border | 1dp `#252525` |
| Horizontal margin | 20dp |
| Text color | White 90% |
| Arrow icon | `arrow.right` 11sp Semibold |
| Icon spacing | 8dp |

### Info Section
| Element | Value |
|---------|-------|
| Top spacing | 24dp |
| Icon size | 16sp |
| Icon color | White 35% |
| Text spacing | 6dp |
| Bottom padding | 52dp (32 + 20) |

---

## Hero Card Stack

### Card Dimensions
| Element | Default | Scaled |
|---------|---------|--------|
| Width | 280dp | Responsive |
| Height | 420dp | Responsive |
| Aspect ratio | 1:1.5 | Maintained |
| Corner radius | ~8.6% of width | ~24dp at 280 |

### Fan Layout
| Property | Value |
|----------|-------|
| Horizontal spread | 28dp × scaleFactor |
| Vertical drop per card | 8dp × scaleFactor |
| Rotation per position | 5° |
| Scale reduction per position | 6% |
| Max visible cards | 5 (2 on each side + center) |

### Opacity by Position
| Position | Opacity |
|----------|---------|
| Center (0) | 100% |
| ±1 | 70% |
| ±2 | 40% |
| Beyond | 20% |

### Z-Index
- Center card: `shows.count` (highest)
- Others: `shows.count - abs(position)`

### Swipe Gesture
| Property | Value |
|----------|-------|
| Threshold | 100dp |
| Animation | Spring (stiffness: 300, damping: 40) |

---

## Empty States

### No Shows Airing (Hero)
```
┌─────────────────────────────┐
│                             │
│        [App Icon]           │
│         (48×48)             │
│     "No Shows Airing"       │
│                             │
└─────────────────────────────┘
```
- Icon opacity: 40%
- Text: 14sp Medium, white 40%
- Background: Gradient white 15% → 10%

### No Timeline Shows
```
┌─────────────────────────────────┐
│                                 │
│    📅 (calendar.badge.clock)    │
│                                 │
│     "Nothing on the Timeline"   │
│                                 │
│  "Your followed shows are not   │
│   currently airing..."          │
│                                 │
└─────────────────────────────────┘
```
- Icon: 44sp, teal 50% opacity
- Title: 18sp Semibold, white 90%
- Subtitle: 14sp, white 50%, center aligned
- Vertical padding: 60dp

---

## Placeholder Gradients

### Card Placeholder (Backdrop/Poster missing)
```kotlin
LinearGradient(
    colors = listOf(
        Color(0xFF333333),  // white 20%
        Color(0xFF262626)   // white 15%
    ),
    start = Offset.TopLeft,
    end = Offset.BottomRight
)
```

### Empty Slot Placeholder
```kotlin
// Dashed border only, no fill
// Center text: "EMPTY"
```

---

## Accessibility

### Section Headers
- Role: Header + Button (when expandable)
- Label: "{title}, {count} shows"
- Hint: "Double tap to expand/collapse section"

### Timeline Cards
- Role: Button
- Label: "{show name}, Season {n}, {status} in {days} days"
- Hint: "Double tap to view show details"

### Hero Cards
- Front card: Selected trait
- Label: "{show name}, currently airing"
- Actions: "Next show", "Previous show"
