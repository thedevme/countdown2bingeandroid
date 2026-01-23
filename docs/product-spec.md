# Countdown2Binge — Product Spec (Android)

## What this app is
Countdown2Binge is a TV show tracking app focused on **when a season is ready to binge**, not just what episode airs next. It helps users track favorite shows, know exactly when a season finishes, and plan binge time around their real schedule.

The core value is removing mental overhead:
users should never need to remember dates, finales, or season status.

---

## Target user
- Watches TV in seasons, not weekly
- Prefers to binge once a season is complete
- Wants calm, intentional planning (not notification spam)
- Likely already uses TV apps but finds them noisy or cluttered

---

## Core features (non-negotiable)
- Track followed TV shows
- Know when a season is:
  - Airing
  - Finished
  - Ready to binge
- Clear countdowns to:
  - Finale
  - Next episode (secondary)
- Timeline-style home screen showing:
  - Currently Airing (Ending Soon on full timeline)
  - Premiering Soon
  - Anticipated / TBD

### Timeline Categorization Logic

Shows are categorized based on their **production status** and **season state**:

**Currently Airing / Ending Soon**
- Season has premiered (premiere date in past)
- Season finale hasn't aired yet OR no finale date (TBD)
- Show displays days until finale (or TBD if unknown)

**Premiering Soon**
- Season has a confirmed premiere date in the future
- Show displays days until premiere

**Anticipated (TBD)**
- Show is still in production (Returning/In Production status)
- Current season is complete (Binge Ready or Watched)
- No premiere date announced for next season
- Always shows "TBD" - never shows past years

**Not in Timeline (Binge Ready only)**
- Show status is Ended or Canceled
- These shows skip the timeline entirely
- Only appear in Binge Ready section

**State Transitions:**
1. Anticipated (TBD) → Premiering Soon: When premiere date is announced
2. Premiering Soon → Currently Airing: When premiere date arrives
3. Currently Airing → Binge Ready: Day after finale airs
4. Binge Ready → Anticipated: If show is still in production (waiting for next season)
5. Binge Ready → Removed from timeline: If show is ended/canceled

### Automatic Data Refresh

The app automatically keeps show data fresh - users never have to worry about outdated information:

- **On Launch:** Silently refreshes all shows from TMDB in the background
- **Daily Background:** WorkManager runs refresh every 24 hours (requires network)

No manual pull-to-refresh needed on the main timeline - this is a core differentiator. The app handles data freshness automatically so users can trust the countdowns are always accurate.
- Ability to plan binges around dates (calendar-aware later)
- My Show Reactions:
  - Quick reactions per episode or season
  - Lightweight, personal (not social media)

### Onboarding Experience
- Multi-step onboarding flow:
  - Add your first shows (search + recommended)
  - Review selected shows
  - Notification settings prompt (first show added)
  - Completion screen with voice assistant education
- Skip option available at every step
- Back navigation allowed

### Notifications Hub
- Central notifications management (Settings → Reminders):
  - System permissions card (enable/open settings)
  - Premium access card (free users see upsell)
  - Summary: pending count + type breakdown
  - Next scheduled notification display
  - Global defaults toggles (premiere, new episodes, finale, binge ready)
  - Show management list (only in-production shows)
  - Scheduled alerts with filter (pending/delivered/cancelled)

### Per-Show Notification Settings
- Tap show from hub → Edit screen with:
  - Stretchy poster header
  - Individual toggles (override global defaults)
  - Finale reminder timing (day of, 1 day, 2 days, 1 week)
  - Quiet hours configuration
  - Scheduled alerts list for that show
  - Reset to global defaults button
  - Cancel all notifications button
- Only shows still in production get notification options

---

## What this app is NOT
- Not a social network
- Not a recommendation engine
- Not a "what should I watch" app
- Not focused on episode-by-episode reminders

---

## Design principles
- Calm, minimal, intentional
- Timeline-driven, not list-driven
- Design-forward, premium feel
- No clutter, no gamification
- Visual hierarchy over density
- Motion is subtle and purposeful

---

## Technical constraints
- Android API 24+ (Kotlin / Jetpack Compose)
- iOS version exists (shared concepts, not shared code)
- Uses TMDB for show data
- Offline-friendly for followed shows
- Testable architecture (repository-based, ViewModel for UI state)

---

## Success criteria
The app feels:
- Quiet
- Trustworthy
- Predictable
- Helpful without demanding attention

Users should feel relief, not urgency.
