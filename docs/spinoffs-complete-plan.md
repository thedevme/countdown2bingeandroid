# Complete Spinoffs JSON Plan

## Overview

Create a comprehensive `spinoffs_multilang.json` file that includes:
- All existing American franchises (with updates)
- New international franchises from Spain, Korea, Turkey, Japan

## Structure Per Franchise

```json
{
  "franchise-id": {
    "franchiseName": { /* 15 languages */ },
    "origin": "US|ES|KR|TR|JP",
    "parentShow": {
      "title": "Show Name",
      "localTitle": { /* optional - for non-English shows */ },
      "tmdbId": 12345,
      "years": "2017-2021"
    },
    "spinoffs": [
      {
        "title": "Spinoff Name",
        "tmdbId": 12345,
        "years": "2023-present",
        "type": "prequel|sequel|companion|remake",
        "status": "active|ended|in_development|on_hold"
      }
    ],
    "watchOrder": {
      "release": [ /* array of titles with multilang notes */ ],
      "chronological": [ /* array of titles with multilang notes */ ]
    }
  }
}
```

## Languages (15 total)

| Code | Language |
|------|----------|
| en | English |
| es | Spanish |
| fr | French |
| de | German |
| pt-BR | Portuguese (Brazil) |
| it | Italian |
| ja | Japanese |
| ko | Korean |
| zh-Hans | Chinese Simplified |
| ar | Arabic |
| ru | Russian |
| tr | Turkish |
| pl | Polish |
| nl | Dutch |
| th | Thai |

---

## FRANCHISES TO INCLUDE

### 1. AMERICAN FRANCHISES (6)

#### 1.1 Breaking Bad Universe (KEEP - no changes needed)
- **Parent:** Breaking Bad (tmdbId: 1396, 2008-2013)
- **Spinoffs:**
  - Better Call Saul (tmdbId: 60059, 2015-2022, prequel, ended)
  - El Camino (tmdbId: 559969, 2019, sequel, ended)

#### 1.2 Game of Thrones Universe (KEEP - no changes needed)
- **Parent:** Game of Thrones (tmdbId: 1399, 2011-2019)
- **Spinoffs:**
  - House of the Dragon (tmdbId: 94997, 2022-present, prequel, active)
  - A Knight of the Seven Kingdoms (tmdbId: 209948, 2025, prequel, active)

#### 1.3 The Walking Dead Universe (KEEP - no changes needed)
- **Parent:** The Walking Dead (tmdbId: 1402, 2010-2022)
- **Spinoffs:**
  - Fear the Walking Dead (tmdbId: 62286, 2015-2023, companion, ended)
  - World Beyond (tmdbId: 94305, 2020-2021, companion, ended)
  - Dead City (tmdbId: 194583, 2023-present, sequel, active)
  - Daryl Dixon (tmdbId: 131237, 2023-present, sequel, active)
  - The Ones Who Live (tmdbId: 206559, 2024, sequel, ended)

#### 1.4 Yellowstone Universe (UPDATE - add missing shows)
- **Parent:** Yellowstone (tmdbId: 73586, 2018-2024) ← UPDATE years
- **Spinoffs:**
  - 1883 (tmdbId: 117098, 2021-2022, prequel, ended)
  - 1923 (tmdbId: 153312, 2022-2025, prequel, ended) ← UPDATE years
  - **1944 (tmdbId: TBD, TBA, prequel, in_development)** ← ADD
  - The Madison (tmdbId: 251418, 2025, sequel, active)
  - **Marshals (tmdbId: TBD, 2026, sequel, active)** ← ADD
  - **The Dutton Ranch (tmdbId: TBD, 2025, sequel, active)** ← ADD
  - 6666 (tmdbId: 195378, TBA, companion, on_hold) ← UPDATE status

#### 1.5 Star Wars TV Universe (KEEP - already complete)
- **Parent:** The Mandalorian (tmdbId: 82856, 2019-present)
- **Spinoffs:**
  - The Book of Boba Fett (tmdbId: 115036, 2021-2022, companion, ended)
  - Obi-Wan Kenobi (tmdbId: 92830, 2022, companion, ended)
  - Andor (tmdbId: 83867, 2022-present, prequel, active)
  - Ahsoka (tmdbId: 114461, 2023-present, companion, active)
  - The Acolyte (tmdbId: 114479, 2024, prequel, ended)
  - Skeleton Crew (tmdbId: 202879, 2024-present, companion, active)

---

### 2. SPANISH FRANCHISES (1)

#### 2.1 Money Heist Universe ← ADD NEW
- **Origin:** ES
- **Parent:** Money Heist / La Casa de Papel (tmdbId: 71446, 2017-2021)
- **Spinoffs:**
  - Money Heist: Korea (tmdbId: 135868, 2022, remake, active)
  - Berlin (tmdbId: 157741, 2023-present, prequel, active)

**Watch Order (Release):**
1. Money Heist - "Original Spanish series - watch first"
2. Money Heist: Korea - "Korean remake - alternate version"
3. Berlin - "Prequel - Berlin's earlier heists"

**Watch Order (Chronological):**
1. Berlin - "Years before the Royal Mint heist"
2. Money Heist Parts 1-2 - "Royal Mint of Spain heist"
3. Money Heist Parts 3-5 - "Bank of Spain heist"

---

### 3. KOREAN FRANCHISES (1)

#### 3.1 Kingdom Universe ← ADD NEW
- **Origin:** KR
- **Parent:** Kingdom / 킹덤 (tmdbId: 80988, 2019-2020)
- **Spinoffs:**
  - Kingdom: Ashin of the North (tmdbId: 858806, 2021, prequel, ended)

**Watch Order (Release):**
1. Kingdom S1-S2 - "Main series - Joseon zombie outbreak"
2. Kingdom: Ashin of the North - "Prequel special - origin of the plague"

**Watch Order (Chronological):**
1. Kingdom: Ashin of the North - "Shows origin of the resurrection plant"
2. Kingdom S1-S2 - "Crown Prince Lee Chang investigates"

---

### 4. TURKISH FRANCHISES (2)

#### 4.1 Ertuğrul/Ottoman Saga ← ADD NEW
- **Origin:** TR
- **Parent:** Resurrection: Ertuğrul / Diriliş: Ertuğrul (tmdbId: 65684, 2014-2019)
- **Spinoffs:**
  - Establishment: Osman / Kuruluş: Osman (tmdbId: 90521, 2019-present, sequel, active)

**Watch Order (Release):**
1. Resurrection: Ertuğrul - "Father's story - 13th century Anatolia"
2. Establishment: Osman - "Son's story - founding the Ottoman Empire"

**Watch Order (Chronological):**
1. Resurrection: Ertuğrul (5 seasons) - "Ertuğrul leads Kayı tribe against Mongols & Crusaders"
2. Establishment: Osman - "Osman I establishes Ottoman beylik"

#### 4.2 Magnificent Century Universe ← ADD NEW
- **Origin:** TR
- **Parent:** Magnificent Century / Muhteşem Yüzyıl (tmdbId: 51331, 2011-2014)
- **Spinoffs:**
  - Magnificent Century: Kösem / Muhteşem Yüzyıl: Kösem (tmdbId: 64069, 2015-2017, sequel, ended)

**Watch Order (Release):**
1. Magnificent Century - "Sultan Suleiman the Magnificent's reign"
2. Magnificent Century: Kösem - "37 years later - Kösem Sultan's story"

**Watch Order (Chronological):**
1. Magnificent Century - "16th century - Suleiman's reign"
2. Magnificent Century: Kösem - "17th century - Kösem's rise to power"

---

### 5. JAPANESE FRANCHISES (1)

#### 5.1 Alice in Borderland ← ADD NEW
- **Origin:** JP
- **Parent:** Alice in Borderland / 今際の国のアリス (tmdbId: 110316, 2020-present)
- **Spinoffs:** None yet (but has complex timeline worth tracking)
- **Note:** Include for timeline guide, S3 coming September 2025

**Watch Order (Release):**
1. Alice in Borderland S1 - "Arisu trapped in deadly game world"
2. Alice in Borderland S2 - "Face card games revealed"
3. Alice in Borderland S3 - "Coming September 2025"

---

## SUMMARY

| # | Franchise | Origin | Parent Show | Spinoffs |
|---|-----------|--------|-------------|----------|
| 1 | Breaking Bad | US | Breaking Bad | 2 |
| 2 | Game of Thrones | US | Game of Thrones | 2 |
| 3 | Walking Dead | US | The Walking Dead | 5 |
| 4 | Yellowstone | US | Yellowstone | 7 |
| 5 | Star Wars TV | US | The Mandalorian | 6 |
| 6 | Money Heist | ES | La Casa de Papel | 2 |
| 7 | Kingdom | KR | Kingdom | 1 |
| 8 | Ertuğrul | TR | Diriliş: Ertuğrul | 1 |
| 9 | Magnificent Century | TR | Muhteşem Yüzyıl | 1 |
| 10 | Alice in Borderland | JP | Alice in Borderland | 0 |

**Total: 10 franchises, 27 spinoffs**

---

## TRANSLATIONS NEEDED

Each franchise needs `franchiseName` and all `note` fields translated into 15 languages.

### Common Phrases to Translate

| English | es | fr | de | pt-BR | it | ja | ko | zh-Hans | ar | ru | tr | pl | nl | th |
|---------|----|----|----|----|----|----|----|----|----|----|----|----|----|----|
| Original series | Serie original | Série originale | Originalserie | Série original | Serie originale | オリジナルシリーズ | 오리지널 시리즈 | 原创剧集 | المسلسل الأصلي | Оригинальный сериал | Orijinal dizi | Oryginalna seria | Originele serie | ซีรีส์ต้นฉบับ |
| Prequel series | Serie precuela | Série préquelle | Prequel-Serie | Série prequela | Serie prequel | 前日譚シリーズ | 프리퀄 시리즈 | 前传剧集 | مسلسل تمهيدي | Сериал-приквел | Öncül dizi | Seria prequel | Prequel serie | ซีรีส์ภาคก่อน |
| Sequel series | Serie secuela | Série suite | Fortsetzungsserie | Série sequência | Serie sequel | 続編シリーズ | 후속 시리즈 | 续集系列 | مسلسل تكملة | Сериал-продолжение | Devam dizisi | Seria kontynuacyjna | Vervolgserie | ซีรีส์ภาคต่อ |
| Spinoff | Spin-off | Spin-off | Spin-off | Spin-off | Spin-off | スピンオフ | 스핀오프 | 衍生剧 | مسلسل فرعي | Спин-офф | Spin-off | Spin-off | Spin-off | สปินออฟ |
| Watch first | Ver primero | Regarder en premier | Zuerst ansehen | Assistir primeiro | Guardare per prima | 最初に視聴 | 먼저 시청 | 首先观看 | شاهده أولاً | Смотреть первым | Önce izle | Oglądaj najpierw | Eerst kijken | ดูก่อน |
| Main timeline | Línea temporal principal | Chronologie principale | Hauptzeitlinie | Linha do tempo principal | Timeline principale | メインタイムライン | 메인 타임라인 | 主时间线 | الجدول الزمني الرئيسي | Основная временная линия | Ana zaman çizelgesi | Główna oś czasu | Hoofdtijdlijn | ไทม์ไลน์หลัก |

---

## CLI INSTRUCTIONS

Tell Claude Code to:

1. **Create the JSON file in parts:**
   - Part 1: American franchises (Breaking Bad, GOT, Walking Dead)
   - Part 2: American franchises (Yellowstone, Star Wars)
   - Part 3: International franchises (Money Heist, Kingdom, Ertuğrul, Magnificent Century, Alice in Borderland)

2. **Merge all parts** into single `spinoffs_multilang.json`

3. **Validate JSON** syntax before finalizing

4. **Key changes from current file:**
   - Add `"origin"` field to each franchise
   - Add `"localTitle"` for non-English shows
   - Update Yellowstone with 3 new spinoffs (1944, Marshals, The Dutton Ranch)
   - Update 1923 status to ended, years to 2022-2025
   - Update 6666 status to on_hold
   - Add 5 new international franchises

---

## TMDB IDs TO LOOK UP

| Show | TMDB ID |
|------|---------|
| 1944 (Yellowstone) | TBD - not announced yet, use 0 |
| Marshals (Yellowstone) | TBD - not announced yet, use 0 |
| The Dutton Ranch | TBD - not announced yet, use 0 |
| Money Heist: Korea | 135868 |
| Berlin | 157741 |
| Kingdom | 80988 |
| Kingdom: Ashin of the North | 858806 (movie) |
| Diriliş: Ertuğrul | 65684 |
| Kuruluş: Osman | 90521 |
| Muhteşem Yüzyıl | 51331 |
| Muhteşem Yüzyıl: Kösem | 64069 |
| Alice in Borderland | 110316 |

---

## ANDROID IMPLEMENTATION GUIDE

This section documents the iOS implementation for adding shows, linking spinoffs, and searching - for Android parity.

---

### 1. ADDING A SHOW (AddShowUseCase)

When a user adds a show, iOS follows this flow:

```
User taps "Follow" → AddShowUseCase.execute(tmdbId) → Show saved + Spinoffs linked
```

#### Step-by-Step Flow

```kotlin
// Android equivalent of AddShowUseCase
class AddShowUseCase(
    private val tmdbService: TMDBService,
    private val repository: ShowRepository,
    private val franchiseService: FranchiseService,
    private val premiumManager: PremiumManager
) {
    suspend fun execute(tmdbId: Int): Result<Show> {
        // 1. Check show limit for free users
        val currentCount = repository.getFollowedShowCount()
        if (!premiumManager.canAddShow(currentCount)) {
            return Result.failure(PremiumError.ShowLimitReached)
        }

        // 2. Check if already followed
        if (repository.isShowFollowed(tmdbId)) {
            return Result.failure(AddShowError.AlreadyFollowed)
        }

        // 3. Fetch full show data from TMDB
        val show = try {
            tmdbService.getShowDetails(tmdbId)
        } catch (e: Exception) {
            return Result.failure(AddShowError.FetchFailed(e))
        }

        // 4. Save to repository (follow + cache)
        try {
            repository.save(show)
        } catch (e: Exception) {
            return Result.failure(AddShowError.SaveFailed(e))
        }

        // 5. Fetch and save franchise/spinoff data
        saveFranchiseData(tmdbId)

        // 6. Return the saved show
        return Result.success(show)
    }
}
```

#### Linking Spinoffs (saveFranchiseData)

After saving a show, iOS checks if it belongs to a franchise and stores related show IDs:

```kotlin
private suspend fun saveFranchiseData(showId: Int) {
    // 1. Ensure franchise data is loaded from Firebase
    franchiseService.fetchFranchises()

    // 2. Check if this show is part of a franchise
    val franchise = franchiseService.getFranchise(forShowId = showId)
    if (franchise == null) {
        Log.d("AddShowUseCase", "Show $showId is not part of any franchise")
        return
    }

    Log.d("AddShowUseCase", "Found franchise: ${franchise.franchiseName.en} for show $showId")

    // 3. Collect all related show IDs (parent + spinoffs, excluding self)
    val relatedIds = mutableListOf<Int>()
    relatedIds.add(franchise.parentShow.tmdbId)
    relatedIds.addAll(franchise.spinoffs.map { it.tmdbId })
    relatedIds.remove(showId)  // Don't include self

    Log.d("AddShowUseCase", "Related show IDs: $relatedIds")

    // 4. Save related IDs to local database
    try {
        repository.updateRelatedShowIds(showId, relatedIds)
        Log.d("AddShowUseCase", "Successfully saved ${relatedIds.size} related show IDs")
    } catch (e: Exception) {
        Log.e("AddShowUseCase", "Failed to save related show IDs: $e")
    }
}
```

#### Data Model for Related Shows

```kotlin
// In FollowedShow entity (Room)
@Entity(tableName = "followed_shows")
data class FollowedShow(
    @PrimaryKey val tmdbId: Int,
    val followedAt: Date,
    // ... other fields ...

    // Related show IDs stored as JSON string
    val relatedShowIdsJson: String? = null
) {
    // Computed property to get/set related IDs
    var relatedShowIds: List<Int>
        get() {
            return relatedShowIdsJson?.let {
                Gson().fromJson(it, object : TypeToken<List<Int>>() {}.type)
            } ?: emptyList()
        }
        set(value) {
            relatedShowIdsJson = Gson().toJson(value)
        }

    val hasSpinoffs: Boolean
        get() = relatedShowIds.isNotEmpty()
}
```

---

### 2. FRANCHISE SERVICE

The FranchiseService fetches franchise data from Firebase and provides O(1) lookups:

```kotlin
@Singleton
class FranchiseService @Inject constructor(
    private val database: FirebaseDatabase
) {
    private var franchises: List<Franchise> = emptyList()
    private var hasLoaded = false

    // O(1) lookup map: TMDB ID → Franchise
    private val showToFranchise: MutableMap<Int, Franchise> = mutableMapOf()

    suspend fun fetchFranchises() {
        if (hasLoaded && franchises.isNotEmpty()) return

        try {
            val snapshot = database.reference.child("franchises").get().await()
            val value = snapshot.value as? Map<String, Any> ?: return

            franchises = parseFranchises(value)
            buildLookupMap()
            hasLoaded = true
        } catch (e: Exception) {
            Log.e("FranchiseService", "Firebase error: ${e.message}")
        }
    }

    private fun buildLookupMap() {
        showToFranchise.clear()
        for (franchise in franchises) {
            // Map parent show
            showToFranchise[franchise.parentShow.tmdbId] = franchise
            // Map all spinoffs
            for (spinoff in franchise.spinoffs) {
                showToFranchise[spinoff.tmdbId] = franchise
            }
        }
    }

    // O(1) lookup
    fun getFranchise(forShowId: Int): Franchise? {
        return showToFranchise[forShowId]
    }

    fun hasFranchise(showId: Int): Boolean {
        return showToFranchise.containsKey(showId)
    }

    fun getSpinoffIds(forShowId: Int): List<Int> {
        val franchise = getFranchise(forShowId) ?: return emptyList()
        return franchise.spinoffs.map { it.tmdbId }
    }
}
```

---

### 3. DISPLAYING SPINOFFS IN SHOW DETAIL

When displaying a show's detail view, iOS loads and displays spinoffs:

```kotlin
// In ShowDetailViewModel
class ShowDetailViewModel(
    private val repository: ShowRepository,
    private val tmdbService: TMDBService
) {
    private val _spinoffShows = MutableStateFlow<Map<Int, Show>>(emptyMap())
    val spinoffShows: StateFlow<Map<Int, Show>> = _spinoffShows

    val spinoffShowsList: List<Show>
        get() = relatedShowIds.mapNotNull { spinoffShows.value[it] }

    private var relatedShowIds: List<Int> = emptyList()

    fun loadRelatedShows(showId: Int) {
        // 1. Get related IDs from local database
        relatedShowIds = repository.getRelatedShowIds(showId)
        if (relatedShowIds.isEmpty()) return

        // 2. Fetch each related show from TMDB
        viewModelScope.launch {
            val shows = mutableMapOf<Int, Show>()
            for (id in relatedShowIds) {
                try {
                    val show = tmdbService.getShowDetails(id)
                    shows[id] = show
                } catch (e: Exception) {
                    Log.e("ShowDetail", "Failed to load spinoff $id: $e")
                }
            }
            _spinoffShows.value = shows
        }
    }
}
```

---

### 4. SEARCH IMPLEMENTATION

Search uses TMDB API and returns `TMDBShowSummary` objects with TMDB IDs:

#### Search Flow

```kotlin
class SearchViewModel(
    private val tmdbService: TMDBService,
    private val addShowUseCase: AddShowUseCase,
    private val repository: ShowRepository
) {
    private val _searchResults = MutableStateFlow<List<TMDBShowSummary>>(emptyList())
    val searchResults: StateFlow<List<TMDBShowSummary>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private var searchJob: Job? = null

    // Debounced search
    fun search(query: String) {
        searchJob?.cancel()

        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            // Debounce: wait 300ms before searching
            delay(300)
            performSearch(trimmedQuery)
        }
    }

    private suspend fun performSearch(query: String) {
        _isSearching.value = true

        try {
            val response = tmdbService.searchShows(query, page = 1)
            _searchResults.value = response.results
        } catch (e: Exception) {
            _searchResults.value = emptyList()
        }

        _isSearching.value = false
    }

    // Add show from search result
    suspend fun addShow(tmdbId: Int): Boolean {
        return when (val result = addShowUseCase.execute(tmdbId)) {
            is Result.Success -> true
            is Result.Failure -> {
                if (result.error is PremiumError.ShowLimitReached) {
                    // Show paywall
                }
                false
            }
        }
    }

    fun isFollowed(tmdbId: Int): Boolean {
        return repository.isShowFollowed(tmdbId)
    }
}
```

#### TMDB Search API

```kotlin
interface TMDBService {
    // Search shows by query
    suspend fun searchShows(query: String, page: Int = 1): TMDBSearchResponse

    // Get full show details (includes seasons, episodes)
    suspend fun getShowDetails(id: Int): Show

    // Get trending shows
    suspend fun getTrendingShows(): List<TMDBShowSummary>

    // Get currently airing shows
    suspend fun getAiringShows(page: Int = 1): TMDBSearchResponse

    // Get shows by genre
    suspend fun getShowsByGenre(genreIds: List<Int>, page: Int = 1): TMDBSearchResponse
}

// Search response model
data class TMDBSearchResponse(
    val page: Int,
    val results: List<TMDBShowSummary>,
    val totalPages: Int,
    val totalResults: Int
)

// Summary model (from search/list endpoints)
data class TMDBShowSummary(
    val id: Int,                    // ← This is the TMDB ID used everywhere
    val name: String,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val firstAirDate: String?,
    val voteAverage: Double?,
    val genreIds: List<Int>?
)
```

---

### 5. COMPLETE FLOW DIAGRAM

```
┌─────────────────────────────────────────────────────────────────────┐
│                         USER SEARCHES                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  SearchView                                                          │
│     │                                                                │
│     ▼                                                                │
│  SearchViewModel.search(query)                                       │
│     │                                                                │
│     ▼ (debounce 300ms)                                              │
│  TMDBService.searchShows(query)                                      │
│     │                                                                │
│     ▼                                                                │
│  Returns List<TMDBShowSummary>                                       │
│     │                                                                │
│     ▼ (each item has tmdbId)                                        │
│  Display search results                                              │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼ User taps "Follow"
┌─────────────────────────────────────────────────────────────────────┐
│                         USER ADDS SHOW                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  AddShowUseCase.execute(tmdbId)                                      │
│     │                                                                │
│     ├─► Check premium limit (3 shows for free)                      │
│     │                                                                │
│     ├─► Check if already followed                                   │
│     │                                                                │
│     ├─► TMDBService.getShowDetails(tmdbId)                          │
│     │      └─► Returns full Show with seasons/episodes              │
│     │                                                                │
│     ├─► Repository.save(show)                                       │
│     │      └─► Save to Room database                                │
│     │                                                                │
│     └─► saveFranchiseData(tmdbId)                                   │
│            │                                                         │
│            ├─► FranchiseService.fetchFranchises()                   │
│            │      └─► Load from Firebase (if not cached)            │
│            │                                                         │
│            ├─► FranchiseService.getFranchise(tmdbId)                │
│            │      └─► O(1) lookup in showToFranchise map            │
│            │                                                         │
│            ├─► Collect related IDs:                                 │
│            │      • franchise.parentShow.tmdbId                     │
│            │      • franchise.spinoffs.map { it.tmdbId }            │
│            │      • Remove self (the show being added)              │
│            │                                                         │
│            └─► Repository.updateRelatedShowIds(tmdbId, relatedIds)  │
│                   └─► Store as JSON in FollowedShow entity          │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼ User opens Show Detail
┌─────────────────────────────────────────────────────────────────────┐
│                      DISPLAY SPINOFFS                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ShowDetailView.onAppear                                             │
│     │                                                                │
│     ▼                                                                │
│  Repository.getRelatedShowIds(showId)                                │
│     │                                                                │
│     ▼ Returns [Int] (list of TMDB IDs)                              │
│  For each relatedId:                                                 │
│     │                                                                │
│     ▼                                                                │
│  TMDBService.getShowDetails(relatedId)                               │
│     │                                                                │
│     ▼                                                                │
│  Display in SpinoffsSection (horizontal scroll)                      │
│     • Show poster                                                    │
│     • Show name                                                      │
│     • Follow/Following button                                        │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

### 6. KEY POINTS FOR ANDROID

1. **TMDB ID is the primary identifier** - Used for:
   - Following/unfollowing shows
   - Fetching show details
   - Linking spinoffs via relatedShowIds
   - Cloud sync (only TMDB IDs stored in Firebase)

2. **Franchise data in Firebase** - Structure:
   ```
   franchises/
     breaking-bad/
       parentShow: { tmdbId: 1396, ... }
       spinoffs: [{ tmdbId: 60059, ... }, ...]
     game-of-thrones/
       ...
   ```

3. **O(1) lookups** - Build a map on app launch:
   - Key: TMDB ID (any show in franchise)
   - Value: Franchise object

4. **relatedShowIds stored locally** - As JSON in Room entity:
   - Persists across sessions
   - Updated when adding a show
   - Migration service updates existing shows on app launch

5. **Premium gating** - Spinoffs section requires premium:
   - Free users see blurred/locked section
   - Tapping shows paywall
