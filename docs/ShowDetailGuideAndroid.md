# Show Detail Screen - Android Implementation Guide

A comprehensive guide for implementing the Show Detail screen on Android, including spinoff collections powered by Firebase.

---

## Screen Overview

The Show Detail screen displays comprehensive information about a TV show in a vertically scrolling layout.

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│                     BACKDROP IMAGE                          │
│                      (420dp height)                         │
│                                                             │
│            ▓▓▓▓▓▓ Gradient ▓▓▓▓▓▓                          │
│                  [SHOW LOGO]                                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Synopsis text that can be expanded to show more...         │
│  [more]                                                     │
│                                                             │
│  6 SEASONS • RETURNING SERIES                               │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  [Sci-Fi] [Drama] [Thriller]                                │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────┐│
│  │              🔖  FOLLOW                                 ││
│  └─────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────┤
│  S1   S2   S3   S4   S5   S6       (if multiple seasons)    │
├─────────────────────────────────────────────────────────────┤
│  SEASON 3                                                   │
│  ┌─────────────────────────────────────────────────────────┐│
│  │                                             S3          ││
│  │  01  Episode Title                                      ││
│  │      45M • MAR 15                                       ││
│  │                                                         ││
│  │  02  Episode Title                                      ││
│  │      52M • MAR 22                                       ││
│  │                                                         ││
│  │  03  Episode Title                                      ││
│  │      48M • MAR 29                                       ││
│  │                                                         ││
│  │  04  Episode Title                                      ││
│  │      55M • APR 5                                        ││
│  │                                                         ││
│  │           ─── VIEW ALL EPISODES ───                     ││
│  └─────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────┤
│  TRAILERS & CLIPS                                           │
│  ┌────────┐ ┌────────┐ ┌────────┐                          │
│  │ ▶ Vid1 │ │ ▶ Vid2 │ │ ▶ Vid3 │  ← Horizontal scroll     │
│  └────────┘ └────────┘ └────────┘                          │
├─────────────────────────────────────────────────────────────┤
│  CAST & CREW                                                │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐                       │
│  │  👤  │ │  👤  │ │  👤  │ │  👤  │  ← Horizontal scroll  │
│  │ Name │ │ Name │ │ Name │ │ Name │                       │
│  │ Role │ │ Role │ │ Role │ │ Role │                       │
│  └──────┘ └──────┘ └──────┘ └──────┘                       │
├─────────────────────────────────────────────────────────────┤
│  MORE LIKE THIS                                             │
│  ┌────────────┐  ┌────────────┐                            │
│  │   Poster   │  │   Poster   │                            │
│  │  [FOLLOW]  │  │  [FOLLOW]  │  ← 2-column grid (max 4)   │
│  └────────────┘  └────────────┘                            │
│  ┌────────────┐  ┌────────────┐                            │
│  │   Poster   │  │   Poster   │                            │
│  │  [FOLLOW]  │  │  [FOLLOW]  │                            │
│  └────────────┘  └────────────┘                            │
├─────────────────────────────────────────────────────────────┤
│  SPINOFF COLLECTION ────────────                            │
│  ┌─────────────────────────────────────────────────────────┐│
│  │ [SERIES]        CURRENTLY AIRING               [1]     ││
│  │ [Backdrop]      HOUSE OF THE DRAGON                    ││
│  │                 ⭐ 8.4 Rating • 2 Seasons              ││
│  │                 Synopsis text here...                   ││
│  │                 [    FOLLOW    ]                        ││
│  └─────────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────────┐│
│  │ [SERIES]        CRITICALLY ACCLAIMED           [2]     ││
│  │ [Backdrop]      GAME OF THRONES                        ││
│  │                 ⭐ 9.3 Rating • 8 Seasons              ││
│  │                 Synopsis text here...                   ││
│  │                 [   FOLLOWING  ]                        ││
│  └─────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────┤
│  TECHNICAL SPECS                                            │
│                                                             │
│  [4K ULTRA HD] [HDR10+] [DOLBY ATMOS]                      │
│  [SPATIAL AUDIO] [CC] [AD]                                  │
│  ─────────────────────────────────────────                  │
│  CREATED BY     David Benioff, D.B. Weiss                   │
│  GENRE          Drama, Fantasy, Action                      │
│  NETWORK        HBO                                         │
│  AUDIO          English, Spanish, French, Japanese          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Files to Create

| File | Purpose |
|------|---------|
| **Models** | |
| `models/Franchise.kt` | Firebase franchise data model |
| `models/Spinoff.kt` | Spinoff show reference |
| `models/LocalizedString.kt` | Multi-language string support |
| `models/WatchOrder.kt` | Release/chronological order |
| `models/TMDBVideo.kt` | Video/trailer model |
| `models/TMDBCastMember.kt` | Cast member model |
| `models/TMDBCrewMember.kt` | Crew member model |
| **Services** | |
| `services/FranchiseService.kt` | Firebase franchise data fetcher |
| **ViewModel** | |
| `ui/detail/ShowDetailViewModel.kt` | Detail screen state management |
| **Screen** | |
| `ui/detail/ShowDetailScreen.kt` | Main composable screen |
| **Components** | |
| `ui/detail/components/BackdropHeader.kt` | Hero image with logo overlay |
| `ui/detail/components/InfoSection.kt` | Synopsis (expandable) + metadata |
| `ui/detail/components/GenreTagsSection.kt` | Horizontal genre tags |
| `ui/detail/components/FollowActionButton.kt` | Full-width follow/unfollow button |
| `ui/detail/components/SeasonPicker.kt` | Horizontal season selector |
| `ui/detail/components/EpisodeSection.kt` | Episode list container with watermark |
| `ui/detail/components/EpisodeRow.kt` | Single episode display |
| `ui/detail/components/TrailersSection.kt` | Horizontal video scroll |
| `ui/detail/components/TrailerCard.kt` | Single trailer thumbnail |
| `ui/detail/components/CastSection.kt` | Horizontal cast scroll |
| `ui/detail/components/PersonCard.kt` | Cast/crew member card |
| `ui/detail/components/MoreLikeThisSection.kt` | 2-column recommendations grid |
| `ui/detail/components/SpinoffSection.kt` | Spinoff collection container |
| `ui/detail/components/TechnicalSpecsSection.kt` | Tech info and badges |
| **Shared Components** | |
| `ui/shared/PortraitShowCard.kt` | Reusable portrait card (for recommendations) |
| `ui/shared/LandscapeShowCard.kt` | Reusable landscape card (for spinoffs) |
| `ui/shared/SynopsisView.kt` | Expandable text component |

---

## Files to Modify

| File | Changes |
|------|---------|
| `build.gradle` | Add Firebase Realtime Database dependency |
| `services/tmdb/TMDBApi.kt` | Add video, credits, recommendations endpoints |
| `services/tmdb/TMDBModels.kt` | Add response models for new endpoints |
| `services/tmdb/TMDBService.kt` | Add wrapper methods |

---

## Firebase Setup

### 1. Add Dependency

```kotlin
// build.gradle (app)
implementation("com.google.firebase:firebase-database-ktx")
```

### 2. Database Path

Franchise data is stored at: `/franchises/{franchiseKey}`

### 3. Data Structure

```json
{
  "franchises": {
    "game_of_thrones": {
      "franchiseName": {
        "en": "Game of Thrones Universe",
        "es": "Universo Juego de Tronos",
        "fr": "Univers Game of Thrones",
        "de": "Game of Thrones Universum",
        "pt": "Universo Game of Thrones",
        "it": "Universo Game of Thrones",
        "ja": "ゲーム・オブ・スローンズ・ユニバース",
        "ko": "왕좌의 게임 유니버스",
        "zh-Hans": "权力的游戏宇宙",
        "ar": "عالم صراع العروش"
      },
      "parentShow": {
        "title": "Game of Thrones",
        "tmdbId": 1399,
        "years": "2011-2019"
      },
      "spinoffs": [
        {
          "title": "House of the Dragon",
          "tmdbId": 94997,
          "years": "2022-",
          "type": "prequel",
          "status": "active"
        }
      ],
      "watchOrder": {
        "release": [
          { "title": "Game of Thrones", "note": { "en": "Original series", ... } }
        ],
        "chronological": [
          { "title": "House of the Dragon", "note": { "en": "200 years before", ... } }
        ]
      }
    }
  }
}
```

### 4. Database Rules

```json
{
  "rules": {
    "franchises": {
      ".read": true,
      ".write": false
    }
  }
}
```

---

## Data Models

### Franchise Models

```kotlin
data class Franchise(
    val franchiseName: LocalizedString,
    val parentShow: ParentShow,
    val spinoffs: List<Spinoff> = emptyList(),
    val watchOrder: WatchOrder
) {
    val id: String get() = parentShow.tmdbId.toString()
}

data class LocalizedString(
    val en: String,
    val es: String,
    val fr: String,
    val de: String,
    val pt: String,
    val it: String,
    val ja: String,
    val ko: String,
    @SerializedName("zh-Hans") val zhHans: String,
    val ar: String
) {
    val localized: String
        get() {
            val languageCode = Locale.getDefault().language
            return when (languageCode) {
                "es" -> es
                "fr" -> fr
                "de" -> de
                "pt" -> pt
                "it" -> it
                "ja" -> ja
                "ko" -> ko
                "zh" -> zhHans
                "ar" -> ar
                else -> en
            }
        }
}

data class ParentShow(
    val title: String,
    val tmdbId: Int,
    val years: String
)

data class Spinoff(
    val title: String,
    val tmdbId: Int,
    val years: String,
    val type: String,   // "prequel", "sequel", "companion"
    val status: String  // "active", "ended"
) {
    val id: Int get() = tmdbId
}

data class WatchOrder(
    val release: List<WatchOrderItem> = emptyList(),
    val chronological: List<WatchOrderItem> = emptyList()
)

data class WatchOrderItem(
    val title: String,
    val note: LocalizedString
)
```

### TMDB Models

```kotlin
data class TMDBVideosResponse(
    val results: List<TMDBVideo>
)

data class TMDBVideo(
    val id: String,
    val key: String,           // YouTube video ID
    val name: String,
    val site: String,          // "YouTube"
    val type: String,          // "Trailer", "Teaser", "Clip"
    val official: Boolean
)

data class TMDBCreditsResponse(
    val cast: List<TMDBCastMember>,
    val crew: List<TMDBCrewMember>
)

data class TMDBCastMember(
    val id: Int,
    val name: String,
    val character: String,
    @SerializedName("profile_path") val profilePath: String?
)

data class TMDBCrewMember(
    val id: Int,
    val name: String,
    val job: String,
    @SerializedName("profile_path") val profilePath: String?
)
```

---

## FranchiseService

```kotlin
@Singleton
class FranchiseService @Inject constructor() {
    private val database = Firebase.database.reference

    private val _franchises = MutableStateFlow<List<Franchise>>(emptyList())
    val franchises: StateFlow<List<Franchise>> = _franchises.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var hasLoaded = false

    suspend fun fetchFranchises() {
        // Skip if already loaded with data
        if (hasLoaded && _franchises.value.isNotEmpty()) return

        _isLoading.value = true

        try {
            val snapshot = database.child("franchises").get().await()
            val loadedFranchises = mutableListOf<Franchise>()

            snapshot.children.forEach { child ->
                try {
                    val json = Gson().toJson(child.value)
                    val franchise = Gson().fromJson(json, Franchise::class.java)
                    loadedFranchises.add(franchise)
                } catch (e: Exception) {
                    Log.e("FranchiseService", "Failed to parse: ${child.key}", e)
                }
            }

            _franchises.value = loadedFranchises.sortedBy { it.parentShow.title }
            hasLoaded = _franchises.value.isNotEmpty()

        } catch (e: Exception) {
            Log.e("FranchiseService", "Failed to fetch franchises", e)
        }

        _isLoading.value = false
    }

    fun franchiseForShowId(showId: Int): Franchise? {
        // Check if it's a parent show
        _franchises.value.find { it.parentShow.tmdbId == showId }?.let { return it }

        // Check if it's a spinoff
        _franchises.value.forEach { franchise ->
            if (franchise.spinoffs.any { it.tmdbId == showId }) {
                return franchise
            }
        }
        return null
    }
}
```

---

## TMDB API Additions

```kotlin
// TMDBApi.kt

@GET("tv/{show_id}/videos")
suspend fun getShowVideos(
    @Path("show_id") showId: Int
): TMDBVideosResponse

@GET("tv/{show_id}/credits")
suspend fun getShowCredits(
    @Path("show_id") showId: Int
): TMDBCreditsResponse

@GET("tv/{show_id}/recommendations")
suspend fun getShowRecommendations(
    @Path("show_id") showId: Int
): TMDBPagedResponse<TMDBSearchResult>
```

---

## ViewModel

```kotlin
@HiltViewModel
class ShowDetailViewModel @Inject constructor(
    private val tmdbService: TMDBService,
    private val showRepository: ShowRepository,
    private val franchiseService: FranchiseService
) : ViewModel() {

    // Core show data
    private val _show = MutableStateFlow<Show?>(null)
    val show: StateFlow<Show?> = _show.asStateFlow()

    // Season selection
    private val _selectedSeasonNumber = MutableStateFlow(1)
    val selectedSeasonNumber: StateFlow<Int> = _selectedSeasonNumber.asStateFlow()

    // Follow state
    private val _isFollowed = MutableStateFlow(false)
    val isFollowed: StateFlow<Boolean> = _isFollowed.asStateFlow()

    // Loading states
    private val _isAdding = MutableStateFlow(false)
    val isAdding: StateFlow<Boolean> = _isAdding.asStateFlow()

    private val _isRemoving = MutableStateFlow(false)
    val isRemoving: StateFlow<Boolean> = _isRemoving.asStateFlow()

    // Additional content
    private val _videos = MutableStateFlow<List<TMDBVideo>>(emptyList())
    val videos: StateFlow<List<TMDBVideo>> = _videos.asStateFlow()

    private val _cast = MutableStateFlow<List<TMDBCastMember>>(emptyList())
    val cast: StateFlow<List<TMDBCastMember>> = _cast.asStateFlow()

    private val _recommendations = MutableStateFlow<List<TMDBSearchResult>>(emptyList())
    val recommendations: StateFlow<List<TMDBSearchResult>> = _recommendations.asStateFlow()

    // Spinoffs
    private val _spinoffShows = MutableStateFlow<List<Show>>(emptyList())
    val spinoffShows: StateFlow<List<Show>> = _spinoffShows.asStateFlow()

    private val _isLoadingSpinoffs = MutableStateFlow(false)
    val isLoadingSpinoffs: StateFlow<Boolean> = _isLoadingSpinoffs.asStateFlow()

    // UI State
    private val _isSynopsisExpanded = MutableStateFlow(false)
    val isSynopsisExpanded: StateFlow<Boolean> = _isSynopsisExpanded.asStateFlow()

    private val _isEpisodeListExpanded = MutableStateFlow(false)
    val isEpisodeListExpanded: StateFlow<Boolean> = _isEpisodeListExpanded.asStateFlow()

    // Computed properties
    val selectedSeason: Season?
        get() = _show.value?.seasons?.find {
            it.seasonNumber == _selectedSeasonNumber.value
        }

    val regularSeasons: List<Season>
        get() = _show.value?.seasons
            ?.filter { it.seasonNumber > 0 }
            ?.sortedBy { it.seasonNumber }
            ?: emptyList()

    val hasMultipleSeasons: Boolean
        get() = regularSeasons.size > 1

    val statusText: String
        get() = when (_show.value?.status) {
            ShowStatus.RETURNING -> "Returning Series"
            ShowStatus.ENDED -> "Ended"
            ShowStatus.CANCELLED -> "Cancelled"
            ShowStatus.IN_PRODUCTION -> "In Production"
            ShowStatus.PLANNED -> "Planned"
            ShowStatus.PILOT -> "Pilot"
            else -> ""
        }

    // Actions
    fun selectSeason(seasonNumber: Int) {
        if (regularSeasons.any { it.seasonNumber == seasonNumber }) {
            _selectedSeasonNumber.value = seasonNumber
        }
    }

    fun toggleSynopsisExpanded() {
        _isSynopsisExpanded.value = !_isSynopsisExpanded.value
    }

    fun toggleEpisodeListExpanded() {
        _isEpisodeListExpanded.value = !_isEpisodeListExpanded.value
    }

    fun loadAdditionalContent(showId: Int) {
        viewModelScope.launch {
            // Parallel fetches
            val videosDeferred = async {
                runCatching { tmdbService.getShowVideos(showId) }
                    .getOrNull()?.results ?: emptyList()
            }
            val creditsDeferred = async {
                runCatching { tmdbService.getShowCredits(showId) }.getOrNull()
            }
            val recommendationsDeferred = async {
                runCatching { tmdbService.getShowRecommendations(showId) }
                    .getOrNull()?.results ?: emptyList()
            }

            _videos.value = videosDeferred.await()
            creditsDeferred.await()?.let { credits ->
                _cast.value = credits.cast.take(10)
            }
            _recommendations.value = recommendationsDeferred.await()
        }
    }

    fun loadSpinoffs(showId: Int) {
        viewModelScope.launch {
            franchiseService.fetchFranchises()

            val franchise = franchiseService.franchiseForShowId(showId) ?: return@launch

            _isLoadingSpinoffs.value = true

            val spinoffs = franchise.spinoffs.mapNotNull { spinoff ->
                runCatching {
                    tmdbService.getShowDetails(spinoff.tmdbId)
                }.getOrNull()
            }

            _spinoffShows.value = spinoffs
            _isLoadingSpinoffs.value = false
        }
    }

    suspend fun addShow() {
        if (_isFollowed.value || _isAdding.value) return
        _isAdding.value = true

        _show.value?.let { show ->
            runCatching {
                showRepository.save(show)
                _isFollowed.value = true
            }
        }

        _isAdding.value = false
    }

    suspend fun removeShow() {
        if (!_isFollowed.value || _isRemoving.value) return
        _isRemoving.value = true

        _show.value?.let { show ->
            runCatching {
                showRepository.delete(show)
                _isFollowed.value = false
            }
        }

        _isRemoving.value = false
    }
}
```

---

## Component Specifications

### BackdropHeader

| Property | Value |
|----------|-------|
| Height | 420dp |
| Stretchy effect | Optional parallax on scroll |
| Logo max width | 280dp |
| Logo max height | 100dp |
| Logo bottom padding | 16dp |
| Gradient height | 200dp |
| Gradient stops | `transparent` → `40% black` → `85% black` → `black` |

**Fallback**: If no logo, show title text in uppercase with heavy weight.

### InfoSection

| Property | Value |
|----------|-------|
| Synopsis line limit (collapsed) | 3 lines |
| "more" link color | Accent teal |
| Metadata format | "{N} SEASONS • {STATUS}" |
| Metadata font size | 11sp |
| Metadata letter spacing | 1sp |
| Metadata color | `rgba(255, 255, 255, 0.5)` |

### GenreTagsSection

| Property | Value |
|----------|-------|
| Max tags displayed | 3 |
| Tag horizontal padding | 12dp |
| Tag vertical padding | 6dp |
| Tag corner radius | 6dp |
| Tag background | `#2D2D2F` |
| Tag border | `#383839`, 1dp |
| Gap between tags | 8dp |

### FollowActionButton

| Property | Value |
|----------|-------|
| Height | 48dp |
| Width | Full width |
| Corner radius | 8dp |
| Background (unfollowed) | Accent teal |
| Background (followed) | `rgba(255, 255, 255, 0.2)` |
| Icon | `bookmark` / `bookmark.fill` |
| Text | "FOLLOW" / "FOLLOWING" |

### SeasonPicker

| Property | Value |
|----------|-------|
| Scroll direction | Horizontal |
| Format | "S1", "S2", "S3"... |
| Selected style | Larger font, white |
| Unselected style | Smaller font, gray |
| Gap between items | 16dp |

### EpisodeSection

| Property | Value |
|----------|-------|
| Container corner radius | 12dp |
| Container background | `#1A1A1C` |
| Container border | `rgba(255, 255, 255, 0.08)`, 1dp |
| Watermark text | "S{number}" |
| Watermark font size | 180sp |
| Watermark opacity | 0.04 |
| Default episodes shown | 4 |
| Expand button text | "VIEW ALL EPISODES" / "SHOW LESS" |

### EpisodeRow

| Property | Value |
|----------|-------|
| Episode number width | 24dp |
| Episode number format | "01", "02" (zero-padded) |
| Episode number color | Tertiary white |
| Title font | Body small |
| Runtime format | "45M" or "1H 30M" |
| Date format | "MAR 15" (uppercase) |
| Vertical padding | 12dp |

### TrailersSection

| Property | Value |
|----------|-------|
| Section header | "TRAILERS & CLIPS" |
| Scroll direction | Horizontal |
| Max items | 6 |
| Card gap | 12dp |

### TrailerCard

| Property | Value |
|----------|-------|
| Thumbnail URL | `https://img.youtube.com/vi/{key}/mqdefault.jpg` |
| Aspect ratio | 16:9 |
| Corner radius | 8dp |
| Play icon overlay | Centered, semi-transparent background |

### CastSection

| Property | Value |
|----------|-------|
| Section header | "CAST & CREW" |
| Scroll direction | Horizontal |
| Max items | 10 |
| Card gap | 16dp |

### PersonCard

| Property | Value |
|----------|-------|
| Image shape | Circle |
| Image size | 80dp |
| Name font | Caption |
| Role font | Caption (tertiary color) |
| Placeholder | Gray circle with person icon |

### MoreLikeThisSection

| Property | Value |
|----------|-------|
| Section header | "MORE LIKE THIS" |
| Layout | 2-column grid |
| Max items | 4 |
| Card gap | 12dp |
| Uses | PortraitShowCard component |

### SpinoffSection

| Property | Value |
|----------|-------|
| Section header | "SPINOFF COLLECTION" |
| Header divider | White line, 15% opacity |
| Layout | Vertical list |
| Card gap | 16dp |
| Uses | LandscapeShowCard component |
| Loading state | Centered progress indicator |

### TechnicalSpecsSection

| Property | Value |
|----------|-------|
| Section header | "TECHNICAL SPECS" |
| Badge style | Capsule with accent border |
| Badge layout | Flow/wrap layout |
| Info row labels | "CREATED BY", "GENRE", "NETWORK", "AUDIO LANGUAGES" |

**Tech Badges:**
- 4K ULTRA HD
- HDR10+
- DOLBY ATMOS
- SPATIAL AUDIO
- CC (with icon)
- AD (with icon)

---

## LandscapeShowCard (Spinoff Card)

Used in the Spinoff Collection section.

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
| Image aspect ratio | 16:9 |
| Card corner radius | 12dp |
| Card background | `#222224` |
| Card border | `#383839`, 1dp |
| Content padding | 12dp |
| Rank badge size | 40dp x 40dp |
| Rank badge background | Accent teal |
| Rank badge position | Top-right corner |
| Series badge background | Accent teal |
| Series badge corner radius | 3dp |

**Subtitle Text (based on show status):**
- `RETURNING` → "CURRENTLY AIRING"
- `ENDED` → "CRITICALLY ACCLAIMED"
- `IN_PRODUCTION` → "IN PRODUCTION"
- Default → "SPINOFF SERIES"

---

## PortraitShowCard (Recommendation Card)

Used in the More Like This section. Same as TrendingShowCard structure.

| Property | Value |
|----------|-------|
| Poster height | 220dp |
| Card corner radius | 10dp |
| Info section background | `#222224` |
| Genre tags | Max 2 |
| Follow button height | 40dp |

---

## Data Flow

```
Screen Load
    │
    ├─► loadShow(tmdbId) ───────────────► TMDB API ──► _show
    │
    ├─► loadAdditionalContent(tmdbId)
    │       ├─► getShowVideos() ────────► _videos (max 6)
    │       ├─► getShowCredits() ───────► _cast (max 10)
    │       └─► getShowRecommendations() ► _recommendations (max 4)
    │
    └─► loadSpinoffs(tmdbId)
            ├─► franchiseService.fetchFranchises() ──► Firebase
            ├─► franchiseForShowId(tmdbId) ──────────► Franchise?
            └─► For each spinoff:
                    getShowDetails(spinoff.tmdbId) ──► _spinoffShows
```

---

## Navigation

| Action | Destination |
|--------|-------------|
| Back button | Pop to previous screen |
| Recommendation card tap | ShowDetailScreen (with recommendation's tmdbId) |
| Spinoff card tap | ShowDetailScreen (with spinoff's tmdbId) |
| Trailer card tap | YouTube app or WebView |
| Share button | Share sheet with show link |

---

## Implementation Order

1. **Models** - Create all data models (Franchise, TMDB types)
2. **Firebase** - Set up FranchiseService
3. **API** - Add TMDB endpoints for videos, credits, recommendations
4. **ViewModel** - Implement ShowDetailViewModel
5. **Simple Components** - GenreTag, PersonCard, TrailerCard, EpisodeRow
6. **Section Headers** - Reusable section header with optional divider
7. **BackdropHeader** - Hero image with gradient and logo
8. **InfoSection** - Expandable synopsis + metadata
9. **FollowActionButton** - Full-width button with states
10. **SeasonPicker** - Horizontal scroll selector
11. **EpisodeSection** - List with watermark and expand/collapse
12. **TrailersSection** - Horizontal video scroll
13. **CastSection** - Horizontal person scroll
14. **Card Components** - PortraitShowCard, LandscapeShowCard
15. **MoreLikeThisSection** - 2-column grid with cards
16. **SpinoffSection** - Vertical list with landscape cards
17. **TechnicalSpecsSection** - Badges and info rows
18. **ShowDetailScreen** - Compose all sections

---

## Image URLs

```kotlin
// Backdrop
"https://image.tmdb.org/t/p/w780${backdropPath}"

// Poster
"https://image.tmdb.org/t/p/w342${posterPath}"

// Logo
"https://image.tmdb.org/t/p/w300${logoPath}"

// Profile (Cast)
"https://image.tmdb.org/t/p/w185${profilePath}"

// YouTube Thumbnail
"https://img.youtube.com/vi/${videoKey}/mqdefault.jpg"
```
