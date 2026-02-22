# Technical Report: Mediathek Experience Improvement

## 1. Artwork Retrieval
**Findings:**
- **API**: The MediathekView API (`https://mediathekviewweb.de/api/query`) does **not** return image URLs. It provides `url_website` which links to the broadcaster's page.
- **Broadcaster Websites**:
    - **ARD**: Uses `og:image` meta tags (e.g., `https://img.ardmediathek.de/...`) and `json-ld` structured data.
    - **ZDF**: Uses `og:image` tags.
- **Strategy**:
    - **Primary**: Use a lightweight, asynchronous scraper to fetch `og:image` from `url_website` on demand. Cache this aggressively (LRU Cache / Disk).
    - **Fallback**: Generate visual placeholders using the channel's primary color and logo (SVG/PNG available in app resources).
    - **Implementation**: Create a `MetadataRepository` that handles scraping and caching.

## 2. Content Density & Scrolling
**Strategy:**
- **Pagination**: The API supports offset/size pagination.
- **Rows**: Implement specific queries for:
    - **Continue Watching**: Local DB query (Room).
    - **New Releases**: API query `sortBy=timestamp`, `sortOrder=desc`.
    - **Trending**: API query filtered by "popular" topics (e.g., "Tatort", "Nachrichten", "Show") or duration > 20min to filter out clips.
    - **Documentaries**: API query with `topic="Doku"`.
    - **Movies**: API query with `topic="Film"`.
- **UI**: Use `LazyColumn` containing `LazyRow`s (Mobile) and `TvLazyColumn` containing `TvLazyRow`s (TV). Implement "infinite" scrolling for the vertical feed by loading more categories or "More Like This" rows.

## 3. Content Version Grouping
**Strategy:**
- **Normalization**: Clean titles by removing suffixes:
    - `(Hörfassung)`, `(Audiodeskription)`, `(AD)`
    - `(Originalversion)`, `(OmU)`, `(OV)`
    - `(Gebärdensprache)`, `(DGS)`
- **Grouping Logic**: Group shows by `cleaned_title` + `topic`.
- **Data Model**:
    ```kotlin
    data class ContentGroup(
        val primaryShow: MediathekShow,
        val variants: List<MediathekShow>
    )
    ```
- **Player Integration**:
    - **Launch**: Start `primaryShow`.
    - **In-Player**: Add a "Versions" menu (similar to Subtitle/Audio tracks) that lists available variants. Switching replaces the current media item.

## 4. UX & Implementation Plan
**Phases:**
1.  **Foundation**: Implement `MetadataRepository` (Scraping) and `ContentGrouping` logic.
2.  **ViewModel**: Update `MediathekUiViewModel` to fetch grouped rows and handle pagination.
3.  **UI Components**:
    - Build `HeroBanner` (TV/Mobile).
    - Build `ContentRow` (TV/Mobile).
    - Implement Skeleton Loading state.
4.  **Player**: Add "Version Selection" dialog/menu.
5.  **Performance**: Optimize image loading (Coil) and list rendering (Keys).

**Milestones:**
- [x] API Analysis
- [ ] Scraper Implementation
- [ ] Grouping Logic
- [ ] UI Row Architecture
- [ ] Final Polish & Animation
