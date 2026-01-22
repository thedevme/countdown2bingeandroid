# Design Specification: TrendingShowCard & AiringShowCard

This document provides precise design specifications for implementing the card components on Android. All values are extracted from the iOS implementation.

---

## Color Palette

### Primary Colors

| Name | Hex | RGB | Usage |
|------|-----|-----|-------|
| Accent Teal | `#38D9A9` | `rgb(56, 217, 169)` | Follow button, links |
| Add Button Teal | `#20A3A4` | `rgb(32, 163, 164)` | Follow button background |

### Surface Colors

| Name | Hex | RGB | Usage |
|------|-----|-----|-------|
| Card Background | `#222224` | `rgb(34, 34, 36)` | Bottom info section |
| Tag/Badge Background | `#2D2D2F` | `rgb(45, 45, 47)` | Genre tags, network badge |
| Tag/Badge Border | `#383839` | `rgb(56, 56, 57)` | Genre tag stroke, network badge stroke |
| Divider | `#38383A` | `rgb(56, 56, 58)` | Horizontal divider line |
| Vertical Divider | `#4E4E4F` | `rgb(78, 78, 79)` | Separator between button and season |

### Text Colors

| Name | Value | Usage |
|------|-------|-------|
| Primary White | `#FFFFFF` | Titles, button text, season number |
| Secondary White | `rgba(255, 255, 255, 0.7)` | Genre text, network badge text |
| Tertiary White | `rgba(255, 255, 255, 0.5)` | "DAYS LEFT" label |

### State Colors

| Name | Hex | Usage |
|------|-----|-------|
| Follow Button (default) | `#20A3A4` | Unfollowed state |
| Follow Button (followed) | `rgba(255, 255, 255, 0.2)` | Already following state |

### Placeholder Gradient

| Stop | Color | Position |
|------|-------|----------|
| Start | `rgba(46, 46, 46, 1)` / `#2E2E2E` | Top-left |
| End | `rgba(31, 31, 31, 1)` / `#1F1F1F` | Bottom-right |

---

## Typography

### Font Family
- **System Font**: San Francisco (iOS) / Roboto (Android)
- Use **condensed width** for follow button text

### Font Specifications

| Element | Size (sp) | Weight | Letter Spacing | Notes |
|---------|-----------|--------|----------------|-------|
| Network Badge | 10 | Bold (700) | 0.5 | UPPERCASE |
| Genre Tag | 11 | Medium (500) | 0 | Title case |
| Follow Button Text | 16 | Heavy (800) | 0 | Condensed width, UPPERCASE |
| Follow Button Icon | 14 | Bold (700) | - | SF Symbol / Material Icon |
| Season Number | 26 | Heavy (800) | 0 | Format: "S1", "S2" |
| Show Title (Airing) | 22 | Bold (700) | 0 | Max 2 lines |
| Days Number | 46 | Heavy (800) | 0 | Format: "01", "14" (zero-padded) |
| Days Label | 14 | Medium (500) | 0 | "DAYS LEFT" |

---

## Dimensions

### TrendingShowCard (Portrait)

```
┌─────────────────────────────┐
│                             │
│      POSTER IMAGE           │  Height: 220dp
│                             │
│    [Network]  ← 10dp inset  │
│                             │
│  ▓▓▓ Gradient ▓▓▓           │  Gradient height: 100dp
│  [Logo]                     │  Logo: max 50dp height
├─────────────────────────────┤
│ [Genre] [Genre]             │  Tag row
│─────────────────────────────│  Divider: 1dp
│ [  FOLLOW  ]    │ S1        │  Button row
└─────────────────────────────┘

Total estimated height: ~320dp (220 poster + ~100 info)
Width: Flexible (grid determines)
Corner radius: 10dp
```

| Component | Value |
|-----------|-------|
| Poster Height | 220dp |
| Card Corner Radius | 10dp |
| Info Section Horizontal Padding | 12dp |
| Genre Tags Top Padding | 8dp |
| Genre Tags Bottom Padding | 4dp |
| Divider Top Margin | 6dp |
| Divider Height | 1dp |
| Button Row Vertical Padding | 8dp |

### AiringShowCard (Landscape)

```
┌─────────────────────────────────────────────────────┐
│                                                     │
│              BACKDROP IMAGE                         │  Height: 220dp
│                                          [Network]  │  Badge: 12dp inset
│                                                     │
│  ▓▓▓▓▓▓▓▓▓ Gradient ▓▓▓▓▓▓▓▓▓                      │  Gradient: 100dp
│  Show Title                                         │  Title padding: 16dp
├─────────────────────────────────────────────────────┤
│ [Genre] [Genre]                                     │  12dp horizontal, 12dp top
│                                                     │
│ [    FOLLOW    ]  │   14      │                     │  Divider height: 70dp
│                   │ DAYS LEFT │                     │
└─────────────────────────────────────────────────────┘  12dp bottom padding

Width: Full width (match parent)
Corner radius: 10dp
```

| Component | Value |
|-----------|-------|
| Backdrop Height | 220dp |
| Card Corner Radius | 10dp |
| Network Badge Inset | 12dp |
| Title Padding | 16dp all sides |
| Genre Row Horizontal Padding | 12dp |
| Genre Row Top Padding | 12dp |
| Bottom Row Horizontal Padding | 12dp |
| Bottom Row Bottom Padding | 12dp |
| Vertical Divider Width | 1dp |
| Vertical Divider Height | 70dp |
| Vertical Divider Horizontal Margin | 12dp |

---

## Component Specifications

### Network Badge

```
┌──────────────┐
│    HBO       │
└──────────────┘
```

| Property | Value |
|----------|-------|
| Font Size | 10sp |
| Font Weight | Bold (700) |
| Letter Spacing | 0.5sp |
| Text Color | `rgba(255, 255, 255, 0.7)` |
| Text Transform | UPPERCASE |
| Horizontal Padding | 8dp |
| Vertical Padding | 5dp |
| Background Color | `#2D2D2F` |
| Border Color | `#383839` |
| Border Width | 1dp |
| Corner Radius | 6dp |
| Position | Top-right, 10dp inset (Trending) / 12dp inset (Airing) |

### Genre Tag

```
┌──────────────┐
│   Sci-Fi     │
└──────────────┘
```

| Property | Value |
|----------|-------|
| Font Size | 11sp |
| Font Weight | Medium (500) |
| Text Color | `rgba(255, 255, 255, 0.7)` |
| Height | 20dp (fixed) |
| Horizontal Padding | 10dp |
| Background Color | `#2D2D2F` |
| Border Color | `#383839` |
| Border Width | 1dp |
| Corner Radius | 6dp |
| Max Tags Displayed | 2 |
| Gap Between Tags | 6dp |

### Follow Button

```
┌─────────────────────────┐
│  🔖  FOLLOW             │
└─────────────────────────┘
```

| Property | Value |
|----------|-------|
| Height | 40dp |
| Width | Fill available space |
| Horizontal Padding | 4dp |
| Corner Radius | 6dp |
| Background (unfollowed) | `#20A3A4` |
| Background (followed) | `rgba(255, 255, 255, 0.2)` |
| Text Color | `#FFFFFF` |
| Icon Size | 14dp |
| Icon-to-Text Gap | 4dp |
| Font Size | 16sp |
| Font Weight | Heavy (800) |
| Font Width | Condensed |
| Text Transform | UPPERCASE |
| Min Scale Factor | 0.7 (for long text) |

**States:**
- Default: Teal background, "FOLLOW" text, outline bookmark icon
- Followed: Gray translucent background, "FOLLOWING" text, filled bookmark icon
- Loading: Show circular progress indicator (white, 0.7 scale)

### Season Indicator (TrendingShowCard only)

```
│ S1
```

| Property | Value |
|----------|-------|
| Font Size | 26sp |
| Font Weight | Heavy (800) |
| Text Color | `#FFFFFF` |
| Format | "S" + number (e.g., "S1", "S2") |
| Leading Padding (from divider) | 8dp |
| Trailing Padding | 4dp |

### Vertical Divider

| Property | TrendingShowCard | AiringShowCard |
|----------|------------------|----------------|
| Width | 1dp | 1dp |
| Height | 32dp | 70dp |
| Color | `#4E4E4F` | `#4E4E4F` |
| Horizontal Margin | 8dp leading | 12dp each side |

### Days Left Indicator (AiringShowCard only)

```
  14
DAYS LEFT
```

| Property | Value |
|----------|-------|
| Number Font Size | 46sp |
| Number Font Weight | Heavy (800) |
| Number Color | `#FFFFFF` |
| Number Format | Zero-padded 2 digits (e.g., "01", "14") |
| Fallback | "--" when null |
| Label Font Size | 14sp |
| Label Font Weight | Medium (500) |
| Label Color | `rgba(255, 255, 255, 0.5)` |
| Label Text | "DAYS LEFT" |
| Vertical Spacing | 0dp (stacked) |
| Trailing Padding | 4dp |

### Show Title (AiringShowCard only)

| Property | Value |
|----------|-------|
| Font Size | 22sp |
| Font Weight | Bold (700) |
| Text Color | `#FFFFFF` |
| Max Lines | 2 |
| Position | Bottom-left of backdrop |
| All-side Padding | 16dp |

### Logo Overlay (TrendingShowCard only)

| Property | Value |
|----------|-------|
| Max Height | 50dp |
| Alignment | Bottom-left |
| Horizontal Padding | 12dp |
| Bottom Padding | 12dp |

---

## Gradient Specifications

### Poster Gradient (TrendingShowCard)

| Property | Value |
|----------|-------|
| Type | Linear (vertical) |
| Start Point | Top |
| End Point | Bottom |
| Height | 100dp |
| Position | Bottom of poster |
| Colors | `transparent` → `rgba(0, 0, 0, 0.8)` |

### Backdrop Gradient (AiringShowCard)

| Property | Value |
|----------|-------|
| Type | Linear (vertical) |
| Start Point | Top |
| End Point | Bottom |
| Height | 100dp |
| Position | Bottom of backdrop |
| Colors | `transparent` → `rgba(0, 0, 0, 0.9)` |

---

## Image Specifications

### Poster Image (TrendingShowCard)

| Property | Value |
|----------|-------|
| Content Mode | Fill (crop to fit) |
| Aspect Ratio | ~2:3 (portrait) |
| TMDB Size | `w342` |
| Corner Radius | Top corners: 10dp, Bottom: 0dp (squared off) |

### Backdrop Image (AiringShowCard)

| Property | Value |
|----------|-------|
| Content Mode | Fill (crop to fit) |
| Aspect Ratio | ~16:9 (landscape) |
| TMDB Size | `w780` |
| Corner Radius | Top corners: 10dp, Bottom: 0dp (squared off) |
| Fallback | Use poster image if backdrop unavailable |

### Placeholder Image

| Property | Value |
|----------|-------|
| Background | Linear gradient (diagonal) |
| Gradient Start | `#2E2E2E` (top-left) |
| Gradient End | `#1F1F1F` (bottom-right) |
| Icon | App icon (white variant) |
| Icon Size | 32dp (Trending) / 40dp (Airing) |
| Icon Opacity | 0.3 |

---

## Accessibility

### Content Descriptions

**TrendingShowCard:**
```
"{Show Name}, on {Network}, {Genre1}, {Genre2}, Already added" (if followed)
"{Show Name}, on {Network}, {Genre1}, {Genre2}" (if not followed)
```

**AiringShowCard:**
```
"{Show Name}, on {Network}, {X} days left, Already added" (if followed)
"{Show Name}, on {Network}, {X} days left" (if not followed)
```

**Follow Button:**
- Unfollowed: "Follow {Show Name}"
- Followed: "Following {Show Name}"

### Interaction Hints
- Card tap: "Double tap to view details"
- Follow button: "Double tap to follow" (when not followed)

---

## Animation Notes

- **Button state change**: Instant (no animation specified)
- **Loading spinner**: Use standard circular indeterminate progress (0.7 scale)
- **Card press**: Consider ripple effect on Android (not specified in iOS)

---

## Grid Layout

When used in a 2-column grid (SearchView):

| Property | Value |
|----------|-------|
| Column Count | 2 |
| Column Spacing | 10dp |
| Row Spacing | 10dp |
| Grid Padding | 16dp horizontal |

---

## TMDB Image URLs

```kotlin
// Poster (TrendingShowCard)
"https://image.tmdb.org/t/p/w342${posterPath}"

// Backdrop (AiringShowCard)
"https://image.tmdb.org/t/p/w780${backdropPath}"

// Logo
"https://image.tmdb.org/t/p/w300${logoPath}"

// YouTube Thumbnail (for trailers)
"https://img.youtube.com/vi/${videoKey}/mqdefault.jpg"
```

---

## Genre ID Mapping

```kotlin
val genreMap = mapOf(
    10759 to "Action",
    16 to "Animation",
    35 to "Comedy",
    80 to "Crime",
    99 to "Documentary",
    18 to "Drama",
    10751 to "Family",
    10762 to "Kids",
    9648 to "Mystery",
    10763 to "News",
    10764 to "Reality",
    10765 to "Sci-Fi",
    10766 to "Soap",
    10767 to "Talk",
    10768 to "Politics",
    37 to "Western",
    27 to "Horror"
)
```

---

## Network Badge Placeholder Logic

```kotlin
val networks = listOf("HBO", "NETFLIX", "APPLE TV+", "HULU", "PRIME", "MAX")
val network = networks[abs(showId) % networks.size]
```

*Note: This is placeholder logic. In production, use actual network data from TMDB.*
