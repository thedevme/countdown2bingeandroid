# Countdown2Binge Onboarding - Android Implementation Guide

## Overview

The onboarding system is a **6-step flow** designed using the **Pain → Agitate → Solution framework**. It guides new users through:
1. Understanding the app's value proposition (Steps 1-3)
2. Adding their shows (Step 4)
3. Reviewing their selection (Step 5)
4. Converting to premium (Step 6)

---

## Flow Diagram

```
New User Entry
    │
    ▼
Step 1: Pain Introduction
    │   "Wait — that show came back?"
    │   [Sign In] [Continue]
    │
    ├─── Sign In Path ──────────────────────┐
    │    → Sign in with Apple               │
    │    → Cloud sync triggered             │
    │    → Shows restored from Firebase     │
    │    → Jump to Step 5                   │
    │                                       │
    ▼                                       │
Step 2: Agitate                             │
    │   "Keeping track is impossible"       │
    │   [Continue]                          │
    ▼                                       │
Step 3: Solution                            │
    │   "Countdown2Binge fixes this"        │
    │   [Let's Set You Up]                  │
    ▼                                       │
Step 4: Add Your Shows ◄────────────────────┘
    │   - Recommended tab (trending)
    │   - Search tab (user query)
    │   - Minimum 3 shows suggested
    │   - First show triggers notification settings
    │   [Continue]
    ▼
Step 5: Completion Review
    │   "You're All Set"
    │   - Shows first 3 posters
    │   - Smart notifications callout
    │   [Continue]
    ▼
Step 6: Paywall
    │   - 6 premium features listed
    │   - 3 pricing options
    │   - [Start 7-Day Free Trial]
    │   - "Continue with limited features"
    │       └─► If >3 shows: Show removal dialog
    ▼
Complete → Main App
```

---

## Step Details

### Step 1: Pain Introduction

**Purpose:** Hook the user with a relatable problem

**Content:**
- Title: "Wait — that show came back?"
- Subtitle: Narrative about missing season renewals
- Hero image: Person looking surprised/confused

**User Actions:**
- **Sign In** - For returning users with existing accounts
  - Triggers Sign in with Apple
  - On success: Cloud sync, restore shows, jump to Step 5
- **Continue** - Proceed to Step 2

**Design:**
```
┌─────────────────────────────────┐
│  STEP 1 OF 6                    │
│                                 │
│  [Hero Image - Person]          │
│                                 │
│  Wait — that show               │
│  came back?                     │
│                                 │
│  We've all been there. You      │
│  loved a show, life got busy,   │
│  and suddenly you find out      │
│  season 4 dropped months ago.   │
│                                 │
│ ─────────────────────────────── │
│ [Sign In]                       │
│ ─────────────────────────────── │
│ Already have an account?        │
│ Sign in to restore your shows   │
│                                 │
│ ═══════════════════════════════ │
│ [      Continue →             ] │
└─────────────────────────────────┘
```

---

### Step 2: Agitate

**Purpose:** Amplify the problem

**Content:**
- Title: "Keeping track is impossible"
- Subtitle: Problems with scattered streaming apps
- Hero image: Person overwhelmed

**User Actions:**
- **Continue** - Proceed to Step 3

---

### Step 3: Solution

**Purpose:** Present the app as the solution

**Content:**
- Title: "Countdown2Binge fixes this"
- Subtitle: Value proposition and benefits
- Hero image: Person happy/relieved

**User Actions:**
- **Let's Set You Up** - Proceed to Step 4

---

### Step 4: Add Your Shows

**Purpose:** Get user to add at least 3 shows

**Layout:**
```
┌─────────────────────────────────┐
│  STEP 4 OF 6           [3 🔖]  │
│                                 │
│  Add your shows                 │
│  Select at least 3 shows to    │
│  personalize your countdowns    │
│                                 │
│  ┌─────────────────────────┐   │
│  │ 🔍 Search for TV series │   │
│  └─────────────────────────┘   │
│                                 │
│  [Recommended] [Search]         │
│  ─────────────────────────────  │
│                                 │
│  ┌──────┐  ┌──────┐            │
│  │Poster│  │Poster│            │
│  │      │  │      │            │
│  │ Name │  │ Name │            │
│  │[FOLLOW]│ │[FOLLOW]│          │
│  └──────┘  └──────┘            │
│                                 │
│ ═══════════════════════════════ │
│  3 shows followed               │
│ [      Continue →             ] │
└─────────────────────────────────┘
```

**Tabs:**
1. **Recommended** - Trending shows from TMDB API
2. **Search** - User query results (debounced 300ms)

**Grid:**
- 2-column layout
- 16dp spacing
- Portrait show cards with:
  - Poster image (2:3 ratio)
  - Network badge (top-right)
  - Logo overlay (bottom, with gradient)
  - Genre tags
  - Follow button

**Special Behavior:**
- First show added triggers notification settings sheet
- Selection badge updates in real-time (top-right corner)
- Continue button always enabled (0 shows skips to Step 6)

---

### Step 5: Completion Review

**Purpose:** Confirm selections before paywall

**Layout:**
```
┌─────────────────────────────────┐
│  STEP 5 OF 6                    │
│                                 │
│         ┌─────┐                 │
│         │  ✓  │                 │
│         └─────┘                 │
│                                 │
│     You're All Set              │
│                                 │
│  ┌─────────────────────────┐   │
│  │ FOLLOWED                 │   │
│  │ ┌────┐┌────┐┌────┐      │   │
│  │ │    ││    ││    │      │   │
│  │ │ P1 ││ P2 ││ P3 │      │   │
│  │ │    ││    ││    │      │   │
│  │ └────┘└────┘└────┘      │   │
│  │ +2 MORE IN YOUR LIST     │   │
│  └─────────────────────────┘   │
│                                 │
│  ┌─────────────────────────┐   │
│  │ 🔔 SMART NOTIFICATIONS   │   │
│  │ Get alerts for new       │   │
│  │ episodes and seasons     │   │
│  └─────────────────────────┘   │
│                                 │
│ ═══════════════════════════════ │
│ [      Continue →             ] │
└─────────────────────────────────┘
```

**Poster Grid:**
- Shows first 3 selected shows
- Size: 80x120dp per poster
- Corner radius: 8dp
- "+X MORE" text if more than 3

---

### Step 6: Paywall

**Purpose:** Convert users to premium

**Layout:**
```
┌─────────────────────────────────┐
│                                 │
│  You're tracking 5 shows        │
│                                 │
│  Unlock the full                │
│  experience                     │
│                                 │
│  ┌─────────────────────────┐   │
│  │ ∞  Unlimited Shows    ✓ │   │
│  │ ⟳  Spinoff Collections ✓ │   │
│  │ 🔔 Smart Notifications ✓ │   │
│  │ 🎤 Siri Integration   ✓ │   │
│  │ ↔  Cloud Sync         ✓ │   │
│  │ ✨ All Future Features ✓ │   │
│  └─────────────────────────┘   │
│                                 │
│  ┌────────────────────────┐    │
│  │ Monthly         $2.99  │    │
│  │ /mo                    │    │
│  └────────────────────────┘    │
│  ┌════════════════════════┐    │
│  ║ Yearly   SAVE 44%      ║    │
│  ║ $19.99/yr              ║    │
│  └════════════════════════┘    │
│  ┌────────────────────────┐    │
│  │ Lifetime        $29.99 │    │
│  │ once                   │    │
│  └────────────────────────┘    │
│                                 │
│ ═══════════════════════════════ │
│ [  Start 7-Day Free Trial     ] │
│                                 │
│  Continue with limited features │
└─────────────────────────────────┘
```

**Features List (6 items):**
| Icon | Feature |
|------|---------|
| ∞ | Unlimited Shows |
| ⟳ | Spinoff Collections |
| 🔔 | Smart Notifications |
| 🎤 | Siri / Voice Integration |
| ↔ | Cloud Sync |
| ✨ | All Future Features |

**Pricing Cards:**
- Monthly: Default monthly price
- Yearly: Default + "SAVE 44%" badge (pre-selected)
- Lifetime: One-time purchase

**Free Account Flow:**
- Free limit: 3 shows
- If user selected >3 shows and taps "Continue with limited features":
  - Show removal dialog appears
  - User must remove shows until ≤3 remain
  - Shows not deleted, just unavailable until upgrade

---

## Design Specifications

### Color Palette

| Element | Color | Hex/RGB |
|---------|-------|---------|
| Background | Black | #000000 |
| Card Background | Dark Gray | #0D0D0D |
| Accent (Primary) | Teal | #4AC7B8 / rgb(74, 199, 184) |
| Text Primary | White | #FFFFFF |
| Text Secondary | White 70% | rgba(255, 255, 255, 0.7) |
| Text Tertiary | White 50% | rgba(255, 255, 255, 0.5) |
| Surface Elevated | Dark Gray | #141414 |
| Divider | White 10% | rgba(255, 255, 255, 0.1) |
| Border | White 15% | rgba(255, 255, 255, 0.15) |

### Typography

| Element | Size | Weight | Notes |
|---------|------|--------|-------|
| Step Indicator | 12sp | Bold | Letter-spacing 1.5sp, uppercase |
| Screen Title | 34sp | Bold | Standard weight |
| Subtitle | 17sp | Regular | Line spacing 4sp |
| Section Header | 12sp | Semibold | Uppercase, letter-spacing 1.5sp |
| Body | 17sp | Regular | Line spacing 4sp |
| Caption | 12sp | Regular | |
| Button Text | 16sp | Bold | |
| Badge Count | 11sp | Bold | |

### Spacing

| Element | Value |
|---------|-------|
| Horizontal Padding | 20dp |
| Vertical Padding | 20dp |
| Card Spacing | 16dp |
| Section Spacing | 24dp |
| Grid Item Spacing | 16dp |
| Step Indicator Spacing | 8dp |

### Corner Radius

| Element | Value |
|---------|-------|
| Cards | 12dp |
| Buttons | 16dp or 8dp |
| Posters/Images | 8dp |
| Search Field | 16dp |
| Badges | 4dp (pill shape) |

---

## Animations

### View Transitions
- Content opacity: 0 → 1 over 250ms ease-in-out
- No horizontal slide animations

### Text Entrance
- Title: Y offset 20dp fade, 500ms ease-out
- Subtitle: Same with 100ms delay
- Images: Scale 0.95 → 1.0 with 200ms delay

### Card Entrance (Review/Paywall)
- Y offset 20dp → 0 with opacity 0 → 1
- Staggered timing: 200ms, 300ms, 400ms delays

### Component Animations
- Step indicator: 300ms with staggered dot fills (50ms per dot)
- Selection badge: Standard color transition
- Button press: Scale 0.98 with opacity change

---

## State Management

### Persisted State (SharedPreferences/DataStore)

```kotlin
data class OnboardingState(
    val hasCompletedOnboarding: Boolean = false,
    val hasSeenNotificationSettings: Boolean = false
)
```

### View Model State

```kotlin
data class OnboardingUiState(
    val currentStep: Int = 1,  // 1-6
    val selectedShows: Map<Int, ShowSummary> = emptyMap(),
    val recommendedShows: List<ShowSummary> = emptyList(),
    val searchResults: List<ShowSummary> = emptyList(),
    val searchQuery: String = "",
    val isLoadingRecommended: Boolean = false,
    val isSearching: Boolean = false,
    val showNotificationSettings: Boolean = false,
    val isSigningIn: Boolean = false,
    val signInError: String? = null
)
```

### Navigation Logic

```kotlin
fun nextStep() {
    when (currentStep) {
        4 -> if (selectedShows.isEmpty()) goToStep(6) else goToStep(5)
        else -> goToStep(currentStep + 1)
    }
}

fun canProceed(): Boolean = true  // Always enabled

fun completeOnboarding() {
    // Save all selected shows to local database
    // Trigger cloud sync if signed in
    // Set hasCompletedOnboarding = true
}
```

---

## Sign In with Apple Integration

### Flow
1. User taps "Sign In" on Step 1
2. Show loading state on button
3. Trigger Sign in with Apple native flow
4. On success:
   - Authenticate with Firebase
   - Trigger cloud sync
   - Load restored shows into selection
   - Jump to Step 5
5. On failure:
   - Show error message
   - Stay on Step 1

### Firebase Authentication
- Use Firebase Auth with Apple credential
- Store user ID for cloud sync
- See `show_syncing_android.md` for full details

---

## Notification Settings Sheet

**Triggered:** When first show is added in Step 4

**Content:**
- Toggle: Season Premiere alerts
- Toggle: New Episode alerts
- Toggle: Finale Reminder
- Picker: Finale Reminder Timing (Day of, 1 day before, 2 days, 1 week)
- Toggle: Season Binge Ready
- Toggle: Quiet Hours (with time pickers)

**Sheet Presentation:**
- Large/expanded detent (85% height)
- Drag indicator visible
- No skip option during onboarding
- Must save to proceed

---

## Free Account Show Removal Dialog

**Triggered:** When user with >3 shows taps "Continue with limited features"

**Layout:**
```
┌─────────────────────────────────┐
│  Free accounts are limited      │
│  to 3 shows                     │
│                                 │
│  Remove 2 shows to continue     │
│                                 │
│  ┌─────────────────────────┐   │
│  │ Show 1            [−]   │   │
│  │ Show 2            [−]   │   │
│  │ Show 3            [−]   │   │
│  │ Show 4            [−]   │   │
│  │ Show 5            [−]   │   │
│  └─────────────────────────┘   │
│                                 │
│ [   Remove 2 more to continue ] │
│         (disabled)              │
│                                 │
│ Or upgrade to keep all shows    │
│ [      Upgrade to Premium     ] │
└─────────────────────────────────┘
```

**Logic:**
- Show list of selected shows
- [-] button removes show from selection
- Primary button disabled until ≤3 shows remain
- Secondary button returns to paywall

---

## Implementation Checklist

### Screens to Build
- [ ] OnboardingContainerActivity/Fragment (orchestrator)
- [ ] IntroScreen (Steps 1-3, parameterized)
- [ ] AddShowsScreen (Step 4)
- [ ] CompletionScreen (Step 5)
- [ ] PaywallScreen (Step 6)
- [ ] NotificationSettingsSheet
- [ ] ShowRemovalDialog

### Components to Build
- [ ] StepIndicator (dots + text)
- [ ] PortraitShowCard (grid item)
- [ ] SegmentedTabBar (Recommended/Search)
- [ ] SearchField
- [ ] PricingCard
- [ ] FeatureRow
- [ ] PosterGrid (3-up)

### API Integration
- [ ] TMDB trending shows endpoint
- [ ] TMDB search endpoint
- [ ] RevenueCat/Play Billing for purchases
- [ ] Firebase Auth for Sign in with Apple
- [ ] Firebase Realtime Database for sync

### State Persistence
- [ ] SharedPreferences for completion flag
- [ ] Room/DataStore for selected shows
- [ ] Notification settings storage
