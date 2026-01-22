# Show Detail Screen - Design Specification for Android

Complete design specification for implementing the Show Detail screen on Android, including all typography, colors, spacing, and component specifications.

---

## Design System Tokens

### Spacing Scale

| Token | Value | Usage |
|-------|-------|-------|
| `xxs` | 4dp | Minimal spacing, inline elements |
| `xs` | 6dp | Tight spacing, tag padding |
| `sm` | 8dp | Small gaps, compact layouts |
| `md` | 12dp | Medium gaps, standard spacing |
| `lg` | 16dp | Large gaps, section spacing |
| `xl` | 20dp | Extra large, major sections |
| `xxl` | 24dp | Section dividers |
| `huge` | 32dp | Major layout divisions |
| `massive` | 40dp | Bottom padding, large gaps |

### Semantic Spacing

| Token | Value | Usage |
|-------|-------|-------|
| `horizontalPadding` | 20dp | Screen edge padding |
| `verticalPadding` | 20dp | Vertical screen padding |
| `cardPadding` | 16dp | Card internal padding |
| `cardSpacing` | 16dp | Gap between cards |
| `sectionSpacing` | 24dp | Gap between sections |
| `scrollItemSpacing` | 12dp | Horizontal scroll items |
| `heroHeight` | 420dp | Backdrop header height |

### Corner Radius Scale

| Token | Value | Usage |
|-------|-------|-------|
| `xs` | 4dp | Badges, small tags |
| `sm` | 6dp | Pills, small buttons |
| `md` | 8dp | Cards, inputs, buttons |
| `lg` | 12dp | Large cards, posters |
| `xl` | 16dp | Sheets, modals |
| `card` | 12dp | Standard card radius |
| `button` | 8dp | Button radius |
| `badge` | 4dp | Badge radius |
| `pill` | 999dp | Full rounded (capsule) |

---

## Color Palette

### Brand Colors

| Name | Hex | RGB | Usage |
|------|-----|-----|-------|
| Accent | `#4AC7B8` | `rgb(74, 199, 184)` | Buttons, highlights, badges, links |

### Background Colors

| Name | Hex | RGB | Usage |
|------|-----|-----|-------|
| Background | `#000000` | `rgb(0, 0, 0)` | Primary app background |
| Card Background | `#0D0D0D` | `rgb(13, 13, 13)` | Card surfaces |
| Surface Elevated | `#141414` | `rgb(20, 20, 20)` | Elevated surfaces |
| Surface | `#1A1A1A` | `rgb(26, 26, 26)` | Overlays, modals |

### Border & Divider Colors

| Name | Hex/Value | Usage |
|------|-----------|-------|
| Border | `#252525` | Card borders |
| Divider | `rgba(255, 255, 255, 0.1)` | Subtle dividers |

### Text Colors

| Name | Value | Usage |
|------|-------|-------|
| Text Primary | `#FFFFFF` | Primary text, titles |
| Text Secondary | `rgba(255, 255, 255, 0.7)` | Body text, descriptions |
| Text Tertiary | `rgba(255, 255, 255, 0.5)` | Metadata, captions |
| Text Disabled | `rgba(255, 255, 255, 0.3)` | Disabled states |

---

## Typography Scale

### Display Styles (Hero Titles)

| Style | Size | Weight | Width | Usage |
|-------|------|--------|-------|-------|
| Display XLarge | 44sp | Heavy (800) | Condensed | Extra large titles |
| Display Large | 36sp | Heavy (800) | Condensed | Main titles |
| Display Medium | 32sp | Heavy (800) | Condensed | Section titles |
| Display Small | 26sp | Heavy (800) | Condensed | Card titles |

### Title Styles

| Style | Size | Weight | Usage |
|-------|------|--------|-------|
| Title Large | 22sp | Bold (700) | Show titles |
| Title Medium | 18sp | SemiBold (600) | Subsection titles |
| Title Small | 16sp | Medium (500) | Small titles |

### Section Headers

| Style | Size | Weight | Letter Spacing | Usage |
|-------|------|--------|----------------|-------|
| Section Header | 14sp | Bold (700) | 1.5sp | Section titles (uppercase) |
| Subsection Header | 13sp | SemiBold (600) | 0 | Subsections, episode numbers |

### Body Styles

| Style | Size | Weight | Line Spacing | Usage |
|-------|------|--------|--------------|-------|
| Body | 15sp | Regular (400) | 6dp | Synopsis, descriptions |
| Body Medium | 15sp | Medium (500) | - | Emphasized body |
| Body Small | 14sp | Regular (400) | - | Secondary text |

### Caption Styles

| Style | Size | Weight | Usage |
|-------|------|--------|-------|
| Caption | 12sp | Regular (400) | General captions |
| Caption Medium | 12sp | Medium (500) | Emphasized captions |
| Caption Small | 11sp | Medium (500) | Badges, tags, metadata |
| Caption XSmall | 10sp | Bold (700) | Rank badges, tiny text |

### Button Styles

| Style | Size | Weight | Width | Usage |
|-------|------|--------|-------|-------|
| Button | 16sp | Heavy (800) | Condensed | Primary buttons |
| Button Small | 14sp | SemiBold (600) | Normal | Secondary buttons |

### Special Styles

| Style | Size | Weight | Design | Usage |
|-------|------|--------|--------|-------|
| Countdown | 48sp | Black (900) | Monospaced | Countdown numbers |
| Season Badge | 13sp | Heavy (800) | Condensed | Season badges |
| Episode Number | 14sp | Bold (700) | Normal | Episode numbers |

---

## Component Specifications

### BackdropHeader

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                     BACKDROP IMAGE                          │
│                      (420dp height)                         │
│                                                             │
│            ▓▓▓▓▓▓ Gradient (200dp) ▓▓▓▓▓▓                  │
│                  [SHOW LOGO]                                │
│                   (max 280x100)                             │
└─────────────────────────────────────────────────────────────┘
```

| Property | Value |
|----------|-------|
| Height | 420dp |
| Stretchy effect | Optional parallax on scroll |
| Gradient height | 200dp |
| Gradient stops | `transparent` → `40% black` → `85% black` → `black` |
| Logo max width | 280dp |
| Logo max height | 100dp |
| Logo bottom padding | 16dp (lg) |
| Logo horizontal padding | 20dp (horizontalPadding) |

**Fallback (no logo):**
- Show title text
- Font: Display Large (36sp, Heavy, Condensed)
- Color: Text Primary
- Shadow: `0dp 4dp 8dp black@50%`
- Text transform: UPPERCASE
- Alignment: Center
- Max lines: 2

---

### InfoSection (Synopsis + Metadata)

```
┌─────────────────────────────────────────────────────────────┐
│ Synopsis text that can be expanded to show more content.    │
│ When collapsed it shows a gradient fade at the bottom...    │
│ [more ▼]                                                    │
│                                                             │
│ 6 SEASONS • RETURNING SERIES                                │
└─────────────────────────────────────────────────────────────┘
```

**Synopsis:**

| Property | Value |
|----------|-------|
| Font | Body (15sp, Regular) |
| Color | Text Secondary |
| Line spacing | 6dp |
| Collapsed height | 96dp |
| Line limit (collapsed) | ~4 lines |
| Fade gradient height | 20dp |

**"more/less" Button:**

| Property | Value |
|----------|-------|
| Text font | Subsection Header (13sp, SemiBold) |
| Icon font | Caption XSmall (10sp) |
| Color | Accent |
| Icon | `chevron.down` / `chevron.up` |
| Gap between text and icon | 4dp (xxs) |
| Animation | Spring (response: 0.35, damping: 0.85) |

**Metadata Row:**

| Property | Value |
|----------|-------|
| Font | Caption Small (11sp, Medium) |
| Color | Text Tertiary |
| Letter spacing | 1sp |
| Format | "{N} SEASONS • {STATUS}" |
| Separator | "•" |
| Separator color | `rgba(255, 255, 255, 0.3)` |

---

### GenreTagsSection

```
[SCI-FI]  [DRAMA]  [THRILLER]
```

| Property | Value |
|----------|-------|
| Max tags displayed | 3 |
| Gap between tags | 8dp (sm) |

**Individual Genre Tag:**

| Property | Value |
|----------|-------|
| Font | Caption XSmall (10sp, Bold) |
| Letter spacing | 1sp |
| Text transform | UPPERCASE |
| Text color | Accent |
| Horizontal padding | 12dp (md) |
| Vertical padding | 6dp (xs) |
| Background | Transparent |
| Border | Accent, 1dp |
| Shape | Capsule (pill) |

---

### FollowActionButton

```
┌─────────────────────────────────────────────────────────────┐
│                    🔖  FOLLOW                               │
└─────────────────────────────────────────────────────────────┘
```

| Property | Value |
|----------|-------|
| Height | 48dp |
| Width | Full width (match parent) |
| Corner radius | 8dp (button) |
| Icon | `bookmark` / `bookmark.fill` |
| Icon size | Section Header (14sp) |
| Text | "FOLLOW" / "FOLLOWING" |
| Text font | Button (16sp, Heavy, Condensed) |
| Text color | Text Primary (white) |
| Gap between icon and text | 6dp (xs) |

**States:**

| State | Background |
|-------|------------|
| Unfollowed | Accent (`#4AC7B8`) |
| Followed | `rgba(255, 255, 255, 0.2)` |
| Loading | Same as current state, show spinner |

**Loading Spinner:**
- Tint: White
- Scale: 0.7

---

### SeasonPicker

```
S1   S2   S3   S4   S5   S6
          ^^^ selected
```

| Property | Value |
|----------|-------|
| Scroll direction | Horizontal |
| Item gap | 16dp (lg) |
| Top padding | 8dp (sm) |

**Season Item:**

| State | Font | Color |
|-------|------|-------|
| Selected | Display Small (26sp, Heavy) | Text Primary |
| Unselected | Title Medium (18sp, SemiBold) | Text Tertiary |

**Format:** "S{number}" (e.g., "S1", "S2", "S3")

---

### EpisodeSection

```
┌─────────────────────────────────────────────────────────────┐
│ SEASON 3                                                    │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │                                             S3          │ │
│ │  01  Episode Title                                      │ │
│ │      45M • MAR 15                                       │ │
│ │                                                         │ │
│ │  02  Episode Title                                      │ │
│ │      52M • MAR 22                                       │ │
│ │                                                         │ │
│ │  03  Episode Title                                      │ │
│ │      48M • MAR 29                                       │ │
│ │                                                         │ │
│ │  04  Episode Title                                      │ │
│ │      55M • APR 5                                        │ │
│ │                                                         │ │
│ │           ─── VIEW ALL EPISODES ───                     │ │
│ └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

**Section Header:**

| Property | Value |
|----------|-------|
| Text | "SEASON {N}" |
| Font | Section Header (14sp, Bold) |
| Letter spacing | 1.5sp |
| Color | Text Primary |

**Container Card:**

| Property | Value |
|----------|-------|
| Corner radius | 12dp (card) |
| Background | Surface Elevated (`#141414`) |
| Border | `rgba(255, 255, 255, 0.08)`, 1dp |
| Horizontal padding | 16dp (lg) |
| Bottom padding | 20dp (xl) |
| Top spacer | 10dp |

**Watermark:**

| Property | Value |
|----------|-------|
| Text | "S{N}" (e.g., "S3") |
| Font size | 180sp |
| Font weight | Black (900) |
| Font width | Condensed |
| Color | `rgba(255, 255, 255, 0.04)` |
| Position | Bottom-right |
| Offset | x: 30dp, y: 50dp |

**Default Episodes Shown:** 4

**Expand/Collapse Button:**

| Property | Value |
|----------|-------|
| Text | "VIEW ALL EPISODES" / "SHOW LESS" |
| Font | Caption Small (11sp, Medium) |
| Letter spacing | 1.5sp |
| Color | `rgba(255, 255, 255, 0.4)` |
| Divider lines | `rgba(255, 255, 255, 0.1)`, 1dp |
| Top padding | 8dp (sm) |
| Bottom padding | 20dp (xl) |

---

### EpisodeRow

```
01  Episode Title
    45M • MAR 15
```

| Property | Value |
|----------|-------|
| Vertical padding | 12dp (md) |
| Gap (number to content) | 12dp (md) |

**Episode Number:**

| Property | Value |
|----------|-------|
| Font | Subsection Header (13sp, SemiBold) |
| Color | Text Tertiary |
| Width | 24dp (fixed) |
| Format | Zero-padded ("01", "02", "10") |
| Number style | Monospaced digits |

**Episode Title:**

| Property | Value |
|----------|-------|
| Font | Body Small (14sp, Regular) |
| Color | Text Primary |
| Max lines | 1 |

**Metadata Row:**

| Property | Value |
|----------|-------|
| Gap | 8dp (sm) |
| Font | Caption Small (11sp, Medium) |
| Color | Text Tertiary |
| Separator | "•" |
| Separator color | Text Disabled |

**Runtime Format:**
- Under 60 min: "{M}M" (e.g., "45M")
- 60+ min: "{H}H {M}M" (e.g., "1H 30M")

**Date Format:** "MMM D" uppercase (e.g., "MAR 15", "APR 5")

---

### TrailersSection

```
TRAILERS & CLIPS
┌────────┐ ┌────────┐ ┌────────┐
│ ▶ Vid  │ │ ▶ Vid  │ │ ▶ Vid  │  ← Horizontal scroll
│  name  │ │  name  │ │  name  │
└────────┘ └────────┘ └────────┘
```

**Section Header:**

| Property | Value |
|----------|-------|
| Text | "TRAILERS & CLIPS" |
| Font | Section Header (14sp, Bold) |
| Letter spacing | 1.5sp |
| Color | Text Primary |
| Top padding | 8dp (sm) |

**Horizontal Scroll:**

| Property | Value |
|----------|-------|
| Item gap | 12dp (md) |
| Max items | 6 |
| Show indicators | false |

---

### TrailerCard

```
┌─────────────────────────────────┐
│                                 │
│           [▶]                   │  ← 16:9 thumbnail
│                                 │
└─────────────────────────────────┘
  Trailer Name
```

| Property | Value |
|----------|-------|
| Width | 200dp |
| Aspect ratio | 16:9 |
| Corner radius | 8dp (md) |
| Gap to title | 8dp (sm) |

**Play Button Overlay:**

| Property | Value |
|----------|-------|
| Size | 40dp x 40dp |
| Shape | Circle |
| Background | `rgba(0, 0, 0, 0.5)` |
| Icon | Play (filled) |
| Icon font | Title Small (16sp) |
| Icon color | White |
| Icon offset | 2dp right (visual centering) |

**Title:**

| Property | Value |
|----------|-------|
| Font | Caption Small (11sp, Medium) |
| Color | Text Secondary |
| Max lines | 1 |

**Thumbnail URL:**
```
https://img.youtube.com/vi/{videoKey}/mqdefault.jpg
```

---

### CastSection

```
CAST & CREW
┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐
│  👤  │ │  👤  │ │  👤  │ │  👤  │  ← Horizontal scroll
│ Name │ │ Name │ │ Name │ │ Name │
│ Role │ │ Role │ │ Role │ │ Role │
└──────┘ └──────┘ └──────┘ └──────┘
```

**Section Header:**

| Property | Value |
|----------|-------|
| Text | "CAST & CREW" |
| Font | Section Header (14sp, Bold) |
| Letter spacing | 1.5sp |
| Color | Text Primary |
| Top padding | 8dp (sm) |

**Horizontal Scroll:**

| Property | Value |
|----------|-------|
| Item gap | 16dp (lg) |
| Max items | 10 |

---

### PersonCard

```
  ┌───────┐
  │   👤  │  ← Circular image
  └───────┘
    Name
   Character
```

| Property | Value |
|----------|-------|
| Image size | 100dp (default) |
| Image shape | Circle |
| Gap to text | 8dp (sm) |
| Card width | Same as image size |

**Name:**

| Property | Value |
|----------|-------|
| Font | Caption XSmall (10sp, Bold) |
| Color | Text Primary |
| Max lines | 1 |

**Subtitle (Character/Role):**

| Property | Value |
|----------|-------|
| Font | Caption XSmall (10sp, Bold) |
| Color | Text Tertiary |
| Max lines | 1 |
| Gap from name | 2dp |

**Placeholder:**

| Property | Value |
|----------|-------|
| Background | Card Background |
| Icon | Person (filled) |
| Icon font | Display Medium (32sp) |
| Icon color | Text Tertiary |
| Border | Border color, 1dp |

---

### MoreLikeThisSection

```
MORE LIKE THIS
┌────────────┐  ┌────────────┐
│   Poster   │  │   Poster   │
│  [FOLLOW]  │  │  [FOLLOW]  │  ← 2-column grid
└────────────┘  └────────────┘
┌────────────┐  ┌────────────┐
│   Poster   │  │   Poster   │
│  [FOLLOW]  │  │  [FOLLOW]  │
└────────────┘  └────────────┘
```

**Section Header:**

| Property | Value |
|----------|-------|
| Text | "MORE LIKE THIS" |
| Font | Section Header (14sp, Bold) |
| Letter spacing | 1.5sp |
| Color | Text Primary |
| Top padding | 8dp (sm) |

**Grid:**

| Property | Value |
|----------|-------|
| Columns | 2 |
| Column gap | 12dp (md) |
| Row gap | 16dp (lg) |
| Max items | 4 |

Uses **PortraitShowCard** (same as TrendingShowCard from Search).

---

### SpinoffSection (Premium-Only)

```
SPINOFF COLLECTION ────────────
┌─────────────────────────────────────────────────────────────┐
│ [SERIES]        CURRENTLY AIRING               [1]         │
│ [Backdrop]      HOUSE OF THE DRAGON                        │
│                 ⭐ 8.4 Rating • 2 Seasons                   │
│                 Synopsis text here...                       │
│                 [    FOLLOW    ]                            │
└─────────────────────────────────────────────────────────────┘
```

**Section Header:**

| Property | Value |
|----------|-------|
| Text | "SPINOFF COLLECTION" |
| Font | Section Header (14sp, Bold) |
| Letter spacing | 1.5sp |
| Color | Text Primary |
| Divider | White `@15%` opacity, 1dp, fills remaining space |
| Gap between text and divider | 12dp (md) |
| Top padding | 8dp (sm) |

**Card List:**

| Property | Value |
|----------|-------|
| Card gap | 16dp (lg) |
| Uses | LandscapeShowCard |

**Subtitle Text (based on status):**

| Status | Text |
|--------|------|
| Returning | "CURRENTLY AIRING" |
| Ended | "CRITICALLY ACCLAIMED" |
| In Production | "IN PRODUCTION" |
| Default | "SPINOFF SERIES" |

---

### SpinoffSection - Locked State (Free Users)

```
SPINOFF COLLECTION ────────────
┌─────────────────────────────────────────────────────────────┐
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  │
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  │ ← Blurred
│  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  │
│                        🔒                                   │
│             Unlock Spinoff Collections                      │
│     Discover connected shows and franchise universes        │
│                     [UNLOCK]                                │
└─────────────────────────────────────────────────────────────┘
```

**Placeholder Cards:**

| Property | Value |
|----------|-------|
| Count | 2 |
| Height | 100dp |
| Gap | 12dp (md) |
| Blur radius | 6dp |

**Lock Overlay:**

| Property | Value |
|----------|-------|
| Icon | Lock (filled) |
| Icon size | 28dp |
| Icon color | Accent |
| Title | "Unlock Spinoff Collections" |
| Title font | Title Small (16sp, Medium) |
| Title color | Text Primary |
| Subtitle | "Discover connected shows and franchise universes" |
| Subtitle font | Caption (12sp, Regular) |
| Subtitle color | `rgba(255, 255, 255, 0.6)` |
| Subtitle alignment | Center |
| Button text | "UNLOCK" |
| Button font | Button (16sp, Heavy, Condensed) |
| Button text color | Black |
| Button background | Accent |
| Button shape | Capsule |
| Button horizontal padding | 20dp (xl) |
| Button vertical padding | 12dp (md) |
| Gap from subtitle to button | 8dp (sm) |
| Overall padding | 20dp (xl) |

---

### TechnicalSpecsSection

```
TECHNICAL SPECS

[4K ULTRA HD] [HDR10+] [DOLBY ATMOS]
[SPATIAL AUDIO] [🔊 CC] [👁 AD]
─────────────────────────────────────────
CREATED BY     David Benioff, D.B. Weiss
GENRE          Drama, Fantasy, Action
NETWORK        HBO
AUDIO          English, Spanish, French, Japanese
```

**Section Header:**

| Property | Value |
|----------|-------|
| Text | "TECHNICAL SPECS" |
| Font | Section Header (14sp, Bold) |
| Letter spacing | 1.5sp |
| Color | Text Primary |
| Top padding | 24dp (xxl) |

**Tech Badges:**

| Property | Value |
|----------|-------|
| Layout | Flow/Wrap |
| Gap | 12dp (md) |
| Margin below | 24dp (xxl) |

**Individual Badge:**

| Property | Value |
|----------|-------|
| Font | Caption Small (11sp, Medium) |
| Letter spacing | 0.5sp |
| Text color | Accent |
| Horizontal padding | 12dp (md) |
| Vertical padding | 8dp (sm) |
| Background | Transparent |
| Border | Accent `@50%` opacity, 1dp |
| Shape | Capsule |

**Badge List:**
- 4K ULTRA HD
- HDR10+
- DOLBY ATMOS
- SPATIAL AUDIO
- CC (with `captions.bubble` icon)
- AD (with `speaker.wave.2` icon)

**Divider:**

| Property | Value |
|----------|-------|
| Color | Divider |
| Height | 1dp |

**Info Rows:**

| Property | Value |
|----------|-------|
| Gap between rows | 20dp (xl) |
| Label font | Caption Small (11sp, Medium) |
| Label color | Text Tertiary |
| Value font | Body Small (14sp, Regular) |
| Value color | Text Secondary |

**Info Row Labels:**
- CREATED BY
- GENRE
- NETWORK
- AUDIO LANGUAGES

---

## LandscapeShowCard (Spinoff Card)

```
┌─────────────────────────────────────────────────────────────┐
│ [SERIES]                                              [1]   │
│                     16:9 BACKDROP IMAGE                     │
├─────────────────────────────────────────────────────────────┤
│ CURRENTLY AIRING                                            │
│ SHOW TITLE                                                  │
│ ⭐ 8.4 Rating • 2 Seasons                                   │
│ Synopsis text that can span up to 3 lines...                │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │              🔖  FOLLOW                                 │ │
│ └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

| Property | Value |
|----------|-------|
| Corner radius | 12dp (card) |
| Background | Card Background (`#0D0D0D`) |
| Border | Border (`#252525`), 1dp |

**Backdrop:**

| Property | Value |
|----------|-------|
| Aspect ratio | 16:9 |
| Corner radius | Top corners: 12dp, Bottom: 0dp |

**Series Badge:**

| Property | Value |
|----------|-------|
| Text | "SERIES" |
| Font | Caption XSmall (10sp, Bold) |
| Letter spacing | 0.5sp |
| Text color | Background (black) |
| Background | Accent |
| Corner radius | 3dp |
| Horizontal padding | 8dp (sm) |
| Vertical padding | 5dp |
| Position | Top-left, 12dp margin |

**Rank Badge:**

| Property | Value |
|----------|-------|
| Size | 40dp x 40dp |
| Font | Title Large (22sp, Bold) + Black weight |
| Text color | White |
| Background | Accent |
| Shape | Custom (top-right corner matches card, bottom-left rounded 8dp) |
| Position | Top-right corner (flush) |

**Content Section:**

| Property | Value |
|----------|-------|
| Padding | 12dp (md) all sides |
| Gap (subtitle to title) | 6dp (xs) |

**Subtitle:**

| Property | Value |
|----------|-------|
| Font | Caption XSmall (10sp, Bold) |
| Letter spacing | 2sp |
| Color | Accent |
| Top padding | 4dp (xxs) |

**Title:**

| Property | Value |
|----------|-------|
| Font | Title Large (22sp, Bold) |
| Color | Text Primary |
| Text transform | UPPERCASE |
| Max lines | 2 |
| Bottom padding | 6dp (xs) |

**Meta Row:**

| Property | Value |
|----------|-------|
| Gap | 4dp (xxs) |
| Star icon | Yellow, Caption XSmall |
| Rating font | Caption Small (11sp) |
| Rating color | Text Primary |
| "Rating" text | Caption Small, Text Tertiary |
| Separator | "•", Text Disabled |
| Seasons text | Caption Small, Text Tertiary |

**Overview:**

| Property | Value |
|----------|-------|
| Font | Caption (12sp, Regular) |
| Color | Text Secondary |
| Line spacing | 2dp |
| Max lines | 3 |
| Top padding | 4dp (xxs) |

**Follow Button:**

| Property | Value |
|----------|-------|
| Height | 36dp (buttonHeightSmall) |
| Top margin | 12dp (md) |
| Full width | Yes |
| Corner radius | 6dp (sm) |
| Same styling as main FollowActionButton |

---

## PortraitShowCard (Recommendation Card)

Same as **TrendingShowCard** from Search screen. See `DesignSpecForCardsAndroid.md` for full specs.

Key specs:
- Poster height: 220dp
- Card corner radius: 10dp
- Info section background: `#222224`
- Genre tags: max 2
- Follow button: 40dp height

---

## Image URLs

```kotlin
// Backdrop (detail header, landscape cards)
"https://image.tmdb.org/t/p/w780${backdropPath}"

// Poster (portrait cards)
"https://image.tmdb.org/t/p/w342${posterPath}"

// Logo
"https://image.tmdb.org/t/p/w300${logoPath}"

// Profile (cast members)
"https://image.tmdb.org/t/p/w185${profilePath}"

// YouTube Thumbnail
"https://img.youtube.com/vi/${videoKey}/mqdefault.jpg"
```

---

## Screen Layout Summary

Vertical scroll, sections in order:

1. **BackdropHeader** - 420dp hero with logo
2. **InfoSection** - Synopsis + metadata (20dp top padding)
3. **GenreTagsSection** - Max 3 genre pills
4. **FollowActionButton** - Full-width 48dp button
5. **SeasonPicker** - If multiple seasons
6. **EpisodeSection** - Episode list with watermark
7. **TrailersSection** - Horizontal video scroll
8. **CastSection** - Horizontal person scroll
9. **MoreLikeThisSection** - 2x2 grid recommendations
10. **SpinoffSection** - Premium-only franchise shows
11. **TechnicalSpecsSection** - Badges + info rows

**Screen Padding:**
- Horizontal: 20dp (except BackdropHeader which is edge-to-edge)
- Top (after header): 20dp (xl)
- Bottom: 40dp (massive)
- Section gap: 20dp (xl)

**Background:** Pure black (`#000000`)
