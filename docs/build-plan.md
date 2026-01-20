# Countdown2Binge Android — Build Plan v1 (Vertical Slices)

A sequential execution checklist optimized for **early Play Store builds**.

**Philosophy:** Working features with basic UI first. Polish later.

---

## Quick Reference: All Milestones

| Step | Milestone |
|------|-----------|
| 58 | ✓ State Machine Tests Pass |
| 74 | ✓ TMDB Fetches Data |
| 82 | ✓ Repository Works |
| 84 | ✓ Add Show Flow Complete |
| 148 | ✓ Basic UI Displays |
| 158 | ✓ Runs on Device |
| **172** | **🚀 ALPHA 1 — Internal Testing** |
| **226** | **🚀 ALPHA 2 — Mark Watched, Lifecycle** |
| **290** | **🚀 BETA 1 — Gestures, Animations, Onboarding** |
| **342** | **🚀 BETA 2 — Notifications, Settings** |
| **410** | **🚀 RC — Monetization, Final Polish** |
| **420** | **🎉 LAUNCH** |

---

## Play Store Checkpoints

| Build | Target | Step | What's Working |
|-------|--------|------|----------------|
| **Alpha 1** | End of Week 1 | 172 | Add shows, Timeline, Binge Ready, Show Detail (basic UI) |
| **Alpha 2** | End of Week 2 | 226 | Mark watched, episode checklist, lifecycle complete |
| **Beta 1** | End of Week 3 | 290 | Polished gestures, animations, onboarding |
| **Beta 2** | End of Week 4 | 342 | Notifications, settings complete |
| **RC** | Week 5-6 | 410 | Monetization, final polish |
| **Launch** | Week 6 | 420 | Play Store release |

---

## Project Configuration

**Package ID:** `io.designtoswiftui.countdown2binge`
**Developer:** Cocoa Academy
**TMDB API Key:** `66be443bd46dc8f896607504aa2d72c6`

---

## Week 1: Core + Basic UI → Alpha 1

---

## Phase 1: Project Setup

### 1.1 — Create Android Studio Project
- [ ] 1. Create new project: "Countdown2Binge" (Empty Activity with Jetpack Compose)
- [ ] 2. Set minimum SDK to API 24
- [ ] 3. Set Kotlin DSL for build configuration
- [ ] 4. Verify Compose dependencies in build.gradle.kts
- [ ] 5. Enable unit tests (should be default)
- [ ] 6. Set screen orientation to Portrait only in AndroidManifest.xml
- [ ] 7. Set app theme to dark only

### 1.2 — Configure Project Structure
- [ ] 8. Create package: `models/`
- [ ] 9. Create package: `services/`
- [ ] 10. Create package: `services/tmdb/`
- [ ] 11. Create package: `services/state/`
- [ ] 12. Create package: `services/repository/`
- [ ] 13. Create package: `usecases/`
- [ ] 14. Create package: `viewmodels/`
- [ ] 15. Create package: `ui/`
- [ ] 16. Create package: `ui/timeline/`
- [ ] 17. Create package: `ui/bingeready/`
- [ ] 18. Create package: `ui/search/`
- [ ] 19. Create package: `ui/showdetail/`
- [ ] 20. Create package: `ui/components/`
- [ ] 21. Create package: `util/`

### 1.3 — Configure Test Structure
- [ ] 22. Create test package: `unit/`
- [ ] 23. Create test package: `fixtures/`
- [ ] 24. Create test package: `helpers/`

### 1.4 — Create Configuration Files
- [ ] 25. Update `.gitignore` for Android
- [ ] 26. Create `README.md`
- [ ] 27. Create `CLAUDE.md`
- [ ] 28. Create `Secrets.kt` (gitignored) with TMDB API key
- [ ] 29. Add `Secrets.kt` to `.gitignore`

---

## Phase 2: Data Models

### 2.1 — Create Enums
- [ ] 30. Create `models/SeasonState.kt`
  - Cases: `ANTICIPATED`, `PREMIERING`, `AIRING`, `BINGE_READY`, `WATCHED`

- [ ] 31. Create `models/ShowStatus.kt`
  - Cases: `RETURNING`, `ENDED`, `CANCELED`, `IN_PRODUCTION`, `PLANNED`, `UNKNOWN`
  - Include `fromTmdb(status: String)` companion function

- [ ] 32. Create `models/ReleasePattern.kt`
  - Cases: `UNKNOWN`, `WEEKLY`, `ALL_AT_ONCE`, `SPLIT_SEASON`

### 2.2 — Create Core Models (Room Entities)
- [ ] 33. Create `models/Episode.kt`
  - `@Entity` with: id (PK), tmdbId, episodeNumber, name, airDate, runtime, overview, isWatched, seasonId (FK)

- [ ] 34. Create `models/Season.kt`
  - `@Entity` with: id (PK), tmdbId, seasonNumber, premiereDate, finaleDate, isFinaleEstimated, episodeCount, airedEpisodeCount, releasePattern, state, watchedDate, posterPath, showId (FK)

- [ ] 35. Create `models/Show.kt`
  - `@Entity` with: id (PK), tmdbId, title, overview, posterPath, backdropPath, status, addedDate

### 2.3 — Configure Room Database
- [ ] 36. Add Room dependencies to build.gradle.kts
- [ ] 37. Create `services/repository/AppDatabase.kt` with @Database annotation
- [ ] 38. Create DAOs: ShowDao, SeasonDao, EpisodeDao
- [ ] 39. Configure Hilt for dependency injection
- [ ] 40. Create DatabaseModule for Hilt

---

## Phase 3: State Machine

### 3.1 — Create SeasonStateManager
- [ ] 41. Create `services/state/SeasonStateManager.kt`
- [ ] 42. Implement `determineState(season: Season, asOf: LocalDate): SeasonState`
- [ ] 43. Implement `isBingeReady(season: Season, asOf: LocalDate): Boolean`
- [ ] 44. Implement `daysUntilPremiere(season: Season, asOf: LocalDate): Int?`
- [ ] 45. Implement `daysUntilFinale(season: Season, asOf: LocalDate): Int?`
- [ ] 46. Implement `episodesRemaining(season: Season): Int?`

### 3.2 — Create SeasonDateResolver
- [ ] 47. Create `services/state/SeasonDateResolver.kt`
- [ ] 48. Implement `resolve(seasonAirDate, episodeCount, episodes): SeasonDateInfo`
- [ ] 49. Implement `resolvePremiereDate(...)`
- [ ] 50. Implement `resolveFinaleDate(...)` with estimation
- [ ] 51. Implement `detectReleasePattern(episodes: List<Episode>)`
- [ ] 52. Implement `countAiredEpisodes(episodes, asOf)`

### 3.3 — Write Core Tests
- [ ] 53. Create `helpers/DateHelpers.kt` in test package
- [ ] 54. Create `unit/SeasonStateManagerTests.kt`
- [ ] 55. Test: No premiere → anticipated
- [ ] 56. Test: Future premiere → premiering
- [ ] 57. Test: Mid-season → airing
- [ ] 58. Test: Finale passed → bingeReady
- [ ] 59. Test: Watched date set → watched
- [ ] 60. Test: Netflix drop → bingeReady on premiere
- [ ] 61. Run tests — all must pass

**✅ MILESTONE: State Machine Tests Pass (Step 58)**

---

## Phase 4: TMDB Service

### 4.1 — Create TMDB Models
- [ ] 62. Add Retrofit + Moshi dependencies to build.gradle.kts
- [ ] 63. Create `services/tmdb/TMDBModels.kt`
- [ ] 64. Implement TMDBSearchResponse, TMDBSearchResult (data classes)
- [ ] 65. Implement TMDBShowDetails, TMDBSeasonSummary
- [ ] 66. Implement TMDBSeasonDetails, TMDBEpisode

### 4.2 — Create TMDB Service
- [ ] 67. Create `services/tmdb/TMDBApi.kt` (Retrofit interface)
- [ ] 68. Define `suspend fun search(query: String): TMDBSearchResponse`
- [ ] 69. Define `suspend fun getShowDetails(id: Int): TMDBShowDetails`
- [ ] 70. Define `suspend fun getSeasonDetails(showId: Int, seasonNumber: Int): TMDBSeasonDetails`
- [ ] 71. Define `suspend fun getTrending(): TMDBSearchResponse`
- [ ] 72. Create `services/tmdb/TMDBService.kt` (implementation)
- [ ] 73. Create `TMDBException` sealed class
- [ ] 74. Configure Moshi for TMDB date format

### 4.3 — Create Data Aggregator
- [ ] 75. Create `services/tmdb/ShowDataAggregator.kt`
- [ ] 76. Implement `suspend fun fetchFullShowData(showId: Int): FullShowData`
  - Fetch show details
  - Skip season 0
  - Fetch latest season details

### 4.4 — Create Show Processor
- [ ] 77. Create `services/tmdb/ShowProcessor.kt`
- [ ] 78. Implement `fun process(fullData: FullShowData, asOf: LocalDate): Show`
  - Create Show from TMDB data
  - Use DateResolver for dates
  - Use StateManager for initial state

**✅ MILESTONE: TMDB Fetches Data (Step 74)**

---

## Phase 5: Repository

### 5.1 — Create Show Repository
- [ ] 79. Create `services/repository/ShowRepository.kt`
- [ ] 80. Implement `suspend fun save(show: Show)`
- [ ] 81. Implement `fun getAllShows(): Flow<List<Show>>`
- [ ] 82. Implement `suspend fun getShow(tmdbId: Int): Show?`
- [ ] 83. Implement `fun getTimelineShows(): Flow<List<Show>>` (returning/inProduction only)
- [ ] 84. Implement `fun getBingeReadySeasons(): Flow<List<Season>>`
- [ ] 85. Implement `suspend fun delete(show: Show)`
- [ ] 86. Implement `suspend fun isShowFollowed(tmdbId: Int): Boolean`

**✅ MILESTONE: Repository Works (Step 82)**

---

## Phase 6: Add Show Use Case

- [ ] 87. Create `usecases/AddShowUseCase.kt`
- [ ] 88. Implement `suspend fun execute(tmdbId: Int): Result<Show>`
  - Check if already followed
  - Fetch full show data
  - Process into local model
  - Save to repository
  - Return saved show

**✅ MILESTONE: Add Show Flow Complete (Step 84)**

---

## Phase 7: Basic UI Components

### 7.1 — Create Shared Components
- [ ] 89. Create `ui/components/ShowCard.kt`
  - AsyncImage (Coil) for backdrop
  - Title overlay
  - Basic styling (rounded corners, shadow)

- [ ] 90. Create `ui/components/StateBadge.kt`
  - Colored badge showing state (Airing, Premiering, etc.)
  - Different colors per state

- [ ] 91. Create `ui/components/CountdownText.kt`
  - Shows "X DAYS" or "X EPISODES"
  - Simple Text composable

- [ ] 92. Create `ui/components/AddButton.kt`
  - "+ Add" / "Added ✓" states
  - Clickable

---

## Phase 8: Basic Search UI

### 8.1 — Create Search ViewModel
- [ ] 93. Create `viewmodels/SearchViewModel.kt` (with @HiltViewModel)
- [ ] 94. StateFlow: `searchQuery`, `searchResults`, `isSearching`, `error`
- [ ] 95. Method: `fun search()`
- [ ] 96. Method: `suspend fun addShow(tmdbId: Int): Boolean`
- [ ] 97. Method: `fun isFollowed(tmdbId: Int): Boolean`

### 8.2 — Create Search Screen
- [ ] 98. Create `ui/search/SearchScreen.kt`
- [ ] 99. TextField for search query
- [ ] 100. LazyColumn of search results
- [ ] 101. Each result: poster, title, year, add button
- [ ] 102. CircularProgressIndicator while searching
- [ ] 103. "No results" empty state
- [ ] 104. Snackbar on add success

---

## Phase 9: Basic Timeline UI

### 9.1 — Create Timeline ViewModel
- [ ] 105. Create `viewmodels/TimelineViewModel.kt`
- [ ] 106. StateFlow: `airingShows`, `premieringShows`, `anticipatedShows`
- [ ] 107. Load from repository on init
- [ ] 108. Group shows by state

### 9.2 — Create Timeline Screen
- [ ] 109. Create `ui/timeline/TimelineScreen.kt`
- [ ] 110. LazyColumn with section headers
- [ ] 111. "Airing Now" section
- [ ] 112. "Premiering Soon" section
- [ ] 113. "Anticipated" section
- [ ] 114. Each show: ShowCard with countdown
- [ ] 115. Empty state when no shows

---

## Phase 10: Basic Binge Ready UI

### 10.1 — Create Binge Ready ViewModel
- [ ] 116. Create `viewmodels/BingeReadyViewModel.kt`
- [ ] 117. StateFlow: `bingeReadySeasons`
- [ ] 118. Load from repository

### 10.2 — Create Binge Ready Screen
- [ ] 119. Create `ui/bingeready/BingeReadyScreen.kt`
- [ ] 120. LazyColumn of ready seasons
- [ ] 121. Each item: show poster, title, season number, episode count
- [ ] 122. Empty state when nothing ready

---

## Phase 11: Basic Show Detail UI

### 11.1 — Create Show Detail ViewModel
- [ ] 123. Create `viewmodels/ShowDetailViewModel.kt`
- [ ] 124. StateFlow: `show`, `seasons`, `isLoading`
- [ ] 125. Load show by ID

### 11.2 — Create Show Detail Screen
- [ ] 126. Create `ui/showdetail/ShowDetailScreen.kt`
- [ ] 127. Backdrop image header
- [ ] 128. Title, overview
- [ ] 129. Status badge
- [ ] 130. Season list (basic)
- [ ] 131. Each season: number, state badge, countdown

---

## Phase 12: Navigation

### 12.1 — Set Up Navigation
- [ ] 132. Add Navigation Compose dependency
- [ ] 133. Create `ui/navigation/NavGraph.kt`
- [ ] 134. Define routes: Timeline, BingeReady, Search, ShowDetail
- [ ] 135. Create `ui/navigation/BottomNavBar.kt`

### 12.2 — Wire Up Navigation
- [ ] 136. Update MainActivity with NavHost
- [ ] 137. Bottom nav: Timeline, Binge Ready, Search
- [ ] 138. Timeline tap → Show Detail
- [ ] 139. Search result tap → Show Detail
- [ ] 140. Binge Ready tap → Show Detail

---

## Phase 13: Basic Data Flow

### 13.1 — Connect Everything
- [ ] 141. Search → Add Show → appears in Timeline
- [ ] 142. Timeline shows countdown correctly
- [ ] 143. Binge Ready populates correctly
- [ ] 144. Show Detail loads from database

### 13.2 — Basic Error Handling
- [ ] 145. Network error → show Snackbar
- [ ] 146. Empty states display correctly
- [ ] 147. Loading states display correctly

---

## Phase 14: Device Testing

- [ ] 148. Build and run on emulator
- [ ] 149. Test full flow: search → add → view
- [ ] 150. Fix any crashes
- [ ] 151. Test on physical device
- [ ] 152. Verify dark theme
- [ ] 153. Check portrait lock works

**✅ MILESTONE: Basic UI Displays (Step 148)**
**✅ MILESTONE: Runs on Device (Step 158)**

---

## Phase 15: Alpha 1 Prep

### 15.1 — Prepare Build
- [ ] 154. Set versionCode to 1
- [ ] 155. Set versionName to "0.1.0"
- [ ] 156. Generate signed APK/AAB
- [ ] 157. Create Play Console listing (internal testing track)

### 15.2 — Internal Testing
- [ ] 158. Upload to internal testing
- [ ] 159. Install on test devices
- [ ] 160. Smoke test all features
- [ ] 161. Fix critical bugs
- [ ] 162. Document known issues

**✅ 🚀 ALPHA 1 — Internal Testing (Step 172)**

---

## Week 2: Complete Lifecycle → Alpha 2

---

## Phase 16: Mark Season Watched

### 16.1 — Update Repository
- [ ] 163. Implement `suspend fun markSeasonWatched(seasonId: Long, date: LocalDate)`
- [ ] 164. Implement `suspend fun unmarkSeasonWatched(seasonId: Long)`
- [ ] 165. Update state when marked

### 16.2 — Add UI
- [ ] 166. Add "Mark as Watched" button to Show Detail
- [ ] 167. Confirmation dialog
- [ ] 168. Update UI after marking
- [ ] 169. Move to appropriate section

---

## Phase 17: Episode Checklist

### 17.1 — Create Episode List
- [ ] 170. Create `ui/showdetail/EpisodeListScreen.kt`
- [ ] 171. LazyColumn of episodes
- [ ] 172. Each: number, title, air date, watched checkbox

### 17.2 — Implement Watching
- [ ] 173. Tap checkbox → toggle watched
- [ ] 174. Track progress per season
- [ ] 175. Show progress indicator

---

## Phase 18: Show Detail - Followed State

- [ ] 176. Expand season cards
- [ ] 177. Tap season → episode list
- [ ] 178. Show countdown per season
- [ ] 179. Unfollow button with confirmation

---

## Phase 19: Refresh Data

### 19.1 — Background Refresh
- [ ] 180. Create `services/RefreshService.kt`
- [ ] 181. Implement `suspend fun refreshShow(showId: Long)`
- [ ] 182. Implement `suspend fun refreshAllShows()`
- [ ] 183. Update dates and states

### 19.2 — Pull to Refresh
- [ ] 184. Add SwipeRefresh to Timeline
- [ ] 185. Refresh all shows on pull
- [ ] 186. Show loading indicator

### 19.3 — WorkManager for Background
- [ ] 187. Add WorkManager dependency
- [ ] 188. Create `RefreshWorker.kt`
- [ ] 189. Schedule daily refresh

---

## Phase 20: State Transitions

- [ ] 190. Test: Premiering → Airing (when premiere passes)
- [ ] 191. Test: Airing → Binge Ready (when finale passes)
- [ ] 192. Verify countdowns update correctly
- [ ] 193. Verify sections update on refresh

---

## Phase 21: Alpha 2 Prep

- [ ] 194. Increment versionCode to 2
- [ ] 195. Set versionName to "0.2.0"
- [ ] 196. Fix bugs from Alpha 1
- [ ] 197. Upload to internal testing
- [ ] 198. Full regression test

**✅ 🚀 ALPHA 2 — Mark Watched, Lifecycle (Step 226)**

---

## Week 3: Polish + Onboarding → Beta 1

---

## Phase 22: Swipe Gestures

- [ ] 199. Swipe right on show → Mark watched
- [ ] 200. Swipe left on show → Remove
- [ ] 201. Swipe actions with icons
- [ ] 202. Haptic feedback

---

## Phase 23: Animations

- [ ] 203. Show card enter animations (staggered)
- [ ] 204. State badge transitions
- [ ] 205. Countdown number animations
- [ ] 206. Navigation transitions
- [ ] 207. List item animations

---

## Phase 24: Empty States

- [ ] 208. Timeline empty → "Add your first show"
- [ ] 209. Binge Ready empty → "Nothing ready yet"
- [ ] 210. Search empty → "No results found"
- [ ] 211. Add illustrations or icons

---

## Phase 25: Enhanced Onboarding (iOS Parity)

### 25.1 — Create Onboarding Container
- [ ] 212. Create `ui/onboarding/OnboardingContainer.kt`
- [ ] 213. Track onboarding state: step, selectedShows, hasShownNotifications
- [ ] 214. Back button navigation between steps
- [ ] 215. Skip functionality at every step

### 25.2 — Add Shows Screen
- [ ] 216. Create `ui/onboarding/AddShowsScreen.kt`
- [ ] 217. Tab bar: Recommended | Search (segmented style)
- [ ] 218. Load recommended/trending shows on appear
- [ ] 219. Search with debounce (auto-switch to Search tab)
- [ ] 220. Show cards with bookmark + count badge for selection
- [ ] 221. Continue button (always enabled, allows skip)
- [ ] 222. Show notification settings sheet on first show added

### 25.3 — Review Shows Screen
- [ ] 223. Create `ui/onboarding/ReviewShowsScreen.kt`
- [ ] 224. Grid of selected shows with posters
- [ ] 225. Swipe to remove shows
- [ ] 226. Skip if no shows selected → go to completion
- [ ] 227. Confirm selection button

### 25.4 — Completion Screen
- [ ] 228. Create `ui/onboarding/CompletionScreen.kt`
- [ ] 229. Success message with animation
- [ ] 230. Google Assistant shortcuts education (Learn button)
- [ ] 231. "Start Watching" button → dismiss immediately, save in background
- [ ] 232. Back button to return to step 1

### 25.5 — First Launch Logic
- [ ] 233. Track completion state in DataStore
- [ ] 234. Track voice assistant education shown
- [ ] 235. Skip onboarding in UI tests
- [ ] 236. Handle existing users (show voice assistant only)

---

## Phase 26: Basic Settings

- [ ] 221. Create `ui/settings/SettingsScreen.kt`
- [ ] 222. Add Settings to navigation
- [ ] 223. App version display
- [ ] 224. TMDB attribution
- [ ] 225. About section

---

## Phase 27: Beta 1 Prep

- [ ] 226. Increment versionCode to 3
- [ ] 227. Set versionName to "0.5.0"
- [ ] 228. Fix bugs from Alpha 2
- [ ] 229. Upload to closed testing track
- [ ] 230. Invite testers (5-10)

**✅ 🚀 BETA 1 — Gestures, Animations, Onboarding (Step 290)**

---

## Week 4: Notifications + Settings → Beta 2

---

## Phase 28: Notifications Service

### 28.1 — Notification Setup
- [ ] 237. Add notification permission handling (Android 13+)
- [ ] 238. Create `services/NotificationService.kt`
- [ ] 239. Create notification channels (premiere, finale, bingeReady)

### 28.2 — Notification Types
- [ ] 240. Binge Ready notification (day after finale)
- [ ] 241. Premiere reminder (9 AM on premiere day)
- [ ] 242. Finale reminder with timing options (day of, 1 day, 2 days, 1 week)
- [ ] 243. New episode notifications

### 28.3 — Schedule Notifications
- [ ] 244. Schedule with AlarmManager or WorkManager
- [ ] 245. Cancel notifications when show removed
- [ ] 246. Reschedule on data refresh
- [ ] 247. Skip scheduling for non-production shows

### 28.4 — Notification Settings Model
- [ ] 248. Create `models/NotificationSettings.kt`
- [ ] 249. Global defaults: seasonPremiere, newEpisodes, finaleReminder, bingeReady
- [ ] 250. Finale reminder timing enum
- [ ] 251. Quiet hours (start/end times)
- [ ] 252. Per-show settings override

---

## Phase 28B: Notifications Hub (iOS Parity)

### 28B.1 — Create ScheduledNotification Model
- [ ] 253. Create `models/ScheduledNotification.kt`
- [ ] 254. Properties: id, showId, showName, posterPath, type, title, subtitle
- [ ] 255. Properties: scheduledDate, seasonNumber, episodeNumber, status
- [ ] 256. Status enum: pending, delivered, cancelled, scheduled, queued
- [ ] 257. NotificationType enum: premiere, newEpisode, finaleReminder, bingeReady

### 28B.2 — Extend NotificationService
- [ ] 258. `getScheduledNotifications(): List<ScheduledNotification>`
- [ ] 259. `getScheduledNotifications(showId): List<ScheduledNotification>`
- [ ] 260. `getPendingCount(): Int`
- [ ] 261. `getPendingCountByType(): Map<NotificationType, Int>`
- [ ] 262. `getNextScheduledNotification(): ScheduledNotification?`
- [ ] 263. `cancelNotification(identifier: String)`
- [ ] 264. `getDeliveredNotifications(): List<ScheduledNotification>`

### 28B.3 — Create NotificationsViewModel
- [ ] 265. Create `viewmodels/NotificationsViewModel.kt`
- [ ] 266. StateFlow: permissionStatus, isPremium, pendingCount, typeBreakdown
- [ ] 267. StateFlow: nextScheduled, scheduledNotifications, filteredNotifications
- [ ] 268. StateFlow: globalDefaults, followedShows (in-production only)
- [ ] 269. StateFlow: selectedFilter (pending/delivered/cancelled)
- [ ] 270. Per-show state: showNotificationSettings, showScheduledNotifications
- [ ] 271. Methods: loadData(), requestPermission(), openSettings()
- [ ] 272. Methods: loadShowData(show), cancelNotification(), saveShowSettings()

### 28B.4 — Hub Components
- [ ] 273. Create `ui/notifications/SystemPermissionsCard.kt`
- [ ] 274. Create `ui/notifications/PremiumAccessCard.kt`
- [ ] 275. Create `ui/notifications/NotificationSummaryCard.kt` (pending + type breakdown)
- [ ] 276. Create `ui/notifications/NextScheduledCard.kt`
- [ ] 277. Create `ui/notifications/GlobalDefaultsSection.kt` (toggles)
- [ ] 278. Create `ui/notifications/ShowManagementRow.kt` (poster, name, next notification)
- [ ] 279. Create `ui/notifications/ScheduledAlertRow.kt` (type badge, details, cancel)

### 28B.5 — Notifications Hub Screen
- [ ] 280. Create `ui/notifications/NotificationsHubScreen.kt`
- [ ] 281. ScrollView with all hub components
- [ ] 282. Show Management section (tap → Edit screen)
- [ ] 283. Scheduled Alerts with filter picker
- [ ] 284. Navigation from Settings → Reminders

### 28B.6 — Edit Show Notifications Screen
- [ ] 285. Create `ui/notifications/EditShowNotificationsScreen.kt`
- [ ] 286. Collapsing/stretchy poster header (CollapsingToolbarScaffold)
- [ ] 287. Show info section (name, next notification, pending count)
- [ ] 288. Notification settings toggles (override global)
- [ ] 289. Finale timing selector (pill buttons)
- [ ] 290. Quiet hours section with time pickers
- [ ] 291. Scheduled alerts list for this show
- [ ] 292. "Reset to Global Defaults" button with confirmation
- [ ] 293. "Cancel All Notifications" button with confirmation

---

## Phase 29: Enhanced Search

### 29.1 — Add Trending Section
- [ ] 241. Update SearchViewModel with `trendingShows`
- [ ] 242. Load trending on appear
- [ ] 243. Create `ui/search/TrendingSection.kt`
- [ ] 244. Horizontal LazyRow of show cards

### 29.2 — Search Landing
- [ ] 245. Show landing when query empty
- [ ] 246. Show results when query entered
- [ ] 247. Trending shows section on landing

---

## Phase 30: Enhanced Show Detail

### 30.1 — Add Trailers
- [ ] 248. Fetch videos from TMDB
- [ ] 249. Create `ui/showdetail/TrailerRow.kt`
- [ ] 250. Tap → open YouTube

### 30.2 — Add Cast
- [ ] 251. Fetch credits from TMDB
- [ ] 252. Create `ui/showdetail/CastRow.kt`
- [ ] 253. Horizontal scroll of cast members

### 30.3 — Add Recommendations
- [ ] 254. Fetch recommendations from TMDB
- [ ] 255. Create `ui/showdetail/RecommendationsSection.kt`
- [ ] 256. Tap recommendation → new detail view

---

## Phase 31: Settings (Complete)

### 31.1 — Notification Settings
- [ ] 257. Toggle: Binge Ready alerts
- [ ] 258. Toggle: Premiere reminders
- [ ] 259. Toggle: Finale reminders
- [ ] 260. Reminder timing picker

### 31.2 — Data Management
- [ ] 261. Export data option
- [ ] 262. Clear all data option (with confirmation)

### 31.3 — Legal
- [ ] 263. Privacy policy link
- [ ] 264. Terms of service link

---

## Phase 32: Beta 2 Prep

- [ ] 265. Increment versionCode to 4
- [ ] 266. Set versionName to "0.9.0"
- [ ] 267. Fix bugs from Beta 1
- [ ] 268. Upload to closed testing
- [ ] 269. Expand testers (10-20)
- [ ] 270. Create Play Store screenshots

**✅ 🚀 BETA 2 — Notifications, Settings Complete (Step 342)**

---

## Week 5-6: Monetization + Final Polish → RC

---

## Phase 33: RevenueCat Integration

### 33.1 — Configure RevenueCat
- [ ] 271. Add RevenueCat SDK via Gradle
- [ ] 272. Create RevenueCat account + app
- [ ] 273. Configure entitlement: "premium"
- [ ] 274. Configure offering with subscription
- [ ] 275. Configure 7-day free trial
- [ ] 276. Initialize in Application class

### 33.2 — Create Premium Manager
- [ ] 277. Create `services/PremiumManager.kt`
- [ ] 278. StateFlow: `isPremium`, `isInTrial`
- [ ] 279. Method: `suspend fun checkEntitlements()`
- [ ] 280. Method: `suspend fun purchase(activity: Activity)`
- [ ] 281. Method: `suspend fun restorePurchases()`

### 33.3 — Create Paywall
- [ ] 282. Create `ui/settings/PaywallScreen.kt`
- [ ] 283. Show subscription options
- [ ] 284. Highlight free trial
- [ ] 285. Purchase button
- [ ] 286. Restore purchases button

### 33.4 — Implement Free Tier Limits
- [ ] 287. Free tier: 1 show only
- [ ] 288. Free tier: No notifications
- [ ] 289. Free tier: No countdowns (just status text)
- [ ] 290. Add checks in AddShowUseCase
- [ ] 291. Add upgrade prompts in UI

---

## Phase 34: Ads Integration

### 34.1 — Configure AdMob
- [ ] 292. Add AdMob SDK via Gradle
- [ ] 293. Configure app ID in AndroidManifest.xml
- [ ] 294. Create `services/AdManager.kt`

### 34.2 — Add Banner Ads
- [ ] 295. Create `ui/components/BannerAdView.kt`
- [ ] 296. Place in non-intrusive location
- [ ] 297. Hide for premium users

### 34.3 — Add Interstitial Ads
- [ ] 298. Load interstitials
- [ ] 299. Show at natural breakpoints
- [ ] 300. Frequency cap
- [ ] 301. Hide for premium users

---

## Phase 35: Firebase Setup

### 35.1 — Configure Firebase
- [ ] 302. Add Firebase SDK via Gradle
- [ ] 303. Add google-services.json
- [ ] 304. Initialize Firebase
- [ ] 305. Configure Crashlytics

### 35.2 — Add Analytics Events
- [ ] 306. Track: app_open
- [ ] 307. Track: onboarding_complete
- [ ] 308. Track: show_added
- [ ] 309. Track: season_completed
- [ ] 310. Track: subscription_started

---

## Phase 36: Accessibility

- [ ] 311. Add contentDescription to all interactive elements
- [ ] 312. Test with TalkBack
- [ ] 313. Test with font scaling (all sizes)
- [ ] 314. Check contrast ratios
- [ ] 315. Add reduce motion support

---

## Phase 37: Final Polish

### 37.1 — Visual Polish
- [ ] 316. Review all screens match designs
- [ ] 317. Smooth all animations
- [ ] 318. Add loading states everywhere
- [ ] 319. Add error states everywhere

### 37.2 — Bug Fixes
- [ ] 320. Fix all reported bugs
- [ ] 321. Fix any crashes in Crashlytics
- [ ] 322. Edge case testing

### 37.3 — Performance
- [ ] 323. Profile with Android Profiler
- [ ] 324. Fix any memory leaks
- [ ] 325. Optimize image loading with Coil

---

## Phase 38: Release Candidate

### 38.1 — Prepare RC Build
- [ ] 326. Set versionCode to 5
- [ ] 327. Set versionName to "1.0.0"
- [ ] 328. Final QA pass
- [ ] 329. Generate signed AAB
- [ ] 330. Upload to open testing track

### 38.2 — Open Beta
- [ ] 331. Enable open testing
- [ ] 332. Share beta link
- [ ] 333. Monitor Crashlytics
- [ ] 334. Collect final feedback

**✅ 🚀 RC — Monetization, Final Polish (Step 410)**

---

## Phase 39: Play Store Submission

### 39.1 — Play Store Assets
- [ ] 335. Create screenshots (phone, tablet)
- [ ] 336. Create feature graphic
- [ ] 337. Write app description
- [ ] 338. Write short description
- [ ] 339. Add keywords/tags
- [ ] 340. Create privacy policy URL

### 39.2 — Submit
- [ ] 341. Complete Play Store listing
- [ ] 342. Configure pricing
- [ ] 343. Submit for review
- [ ] 344. Wait for approval
- [ ] 345. Release to production!

**✅ 🎉 VERSION 1.0 SHIPPED (Step 420)**

---

## Summary

| Week | Goal | Steps | Release |
|------|------|-------|---------|
| 1 | Core + Basic UI | 1-162 | Alpha 1 (Internal) |
| 2 | Complete Lifecycle | 163-198 | Alpha 2 |
| 3 | Polish + Onboarding | 199-230 | Beta 1 (Closed) |
| 4 | Settings + Notifications | 231-270 | Beta 2 |
| 5-6 | Monetization + Final | 271-345 | RC → Launch |

**Total: ~345 steps** (consolidated from iOS 420 due to platform differences)

---

## Key Android Dependencies

```kotlin
// build.gradle.kts (app)
dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose")
    implementation("androidx.navigation:navigation-compose")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    
    // Images
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
```

---

## Design Dependencies

Same designs from iOS apply—just adapt for Material 3 / Android conventions.
