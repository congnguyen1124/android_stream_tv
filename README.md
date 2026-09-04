# StreamTV

**A complete, running Android TV UI kit — not a folder of sample screens.**

StreamTV is an Android TV application built with Jetpack Compose for TV, organised along Clean
Architecture inside **a single app module**. Everything the app displays is written in English.

The project ships seven full screens — Home, Search, Calendar, Setting, Profile and two players —
together with a set of reusable TV components: a hero banner that plays its own trailer, a portrait
carousel, an infinitely looping `ContentRow`, a two-dimensional EPG grid, an on-screen keyboard, and
a player whose controller auto-hides over three side sections. Every focus behaviour in the project
is **deliberate and reproducible**, never an accident of the focus-search algorithm.

> Every image and GIF below is captured automatically from an emulator by
> [`tools/capture_media.py`](tools/capture_media.py). All player demos play the same content — the
> Big Buck Bunny stream — so the two orientations can be compared directly.

---

## Contents

| | |
|---|---|
| [1. Home](#1-home--content-before-interface) | Hero banner, trailer, content rails |
| [2. Focus is the cursor on television](#2-focus-is-the-cursor-on-television) | Top bar, ContentRow, the D-pad contract |
| [3. Search, Calendar, Setting, Profile](#3-search-calendar-setting-and-profile) | The four remaining destinations |
| [4. The landscape player](#4-the-landscape-player) | Controller, focus restore, three side sections |
| [5. The portrait player](#5-the-portrait-player) | The 9:16 stage, the interaction panel |
| [6. Two players, one ViewModel](#6-two-players-one-viewmodel) | Comparison table |
| [7. Reproducing these captures](#7-reproducing-these-captures) | `tools/capture_media.py` |
| [Technical reference](#technical-reference) | Architecture, navigation, DI, build |

---

## 1. Home — content before interface

Viewers sit several metres from the screen with four directional buttons in hand. Home therefore
gives most of the first viewport to artwork: a short title, a concise description, and **one**
primary action, over a layered dark scrim that protects legibility without covering the image.

![Home overview](docs/images/home-overview.webp)

*The 600dp hero banner sits behind the top-bar overlay. The first rail begins immediately below the
hero, signalling that the viewer can continue downward.*

### The hero banner plays its own trailer

This is the detail a still cannot convey. The thumbnail holds for five seconds, then the trailer of
**the item currently focused** fades in over it and loops — no controller, no prominent audio.

![Banner trailer hand-off](docs/images/home-banner-trailer.gif)

*From artwork to trailer: the title, description and call to action hold their position and stay
readable throughout.*

The trailer runs only while the carousel holds focus **and** the screen is `RESUMED`. Losing focus,
changing item, opening the player, or sending the app to the background all stop and unload the
player. If the trailer fails or the item has no `trailerUrl`, the banner simply stays a still image —
the thumbnail is never thrown away. Details: [`docs/home-banner-trailer/`](docs/home-banner-trailer/).

### Content rails

Home receives a vertical list of sections; each section owns a `title`, a `viewType` and a horizontal
list of content. Four different view types, all built on the same `ContentRow` foundation.

<table>
<tr>
<td width="50%"><img src="docs/images/home-rows.webp" alt="Popular videos ranked row"></td>
<td width="50%"><img src="docs/images/home-series.webp" alt="Documentary series row"></td>
</tr>
<tr>
<td><em><strong>Popular videos</strong> — a ranked rail whose numerals bleed past the left edge of the card.</em></td>
<td><em><strong>Documentary series</strong> — cards carrying an episode-count badge; finite, because there are only four items.</em></td>
</tr>
<tr>
<td><img src="docs/images/home-channels.webp" alt="Live channels row"></td>
<td><img src="docs/images/home-shorts.webp" alt="Shorts row"></td>
</tr>
<tr>
<td><em><strong>Live channels</strong> — a red LIVE badge; these streams have no duration, so the player drops its seek bar.</em></td>
<td><em><strong>Fresh shorts</strong> — 2:3 portrait thumbnails, opened in the portrait player rather than the landscape one.</em></td>
</tr>
</table>

Thumbnails are **never darkened** by selection. Selection only brightens the title and adds a white
border — darkening the image makes a rail look disabled when the viewer moves quickly.

### The portrait carousel

`VerticalBanner` presents `Short` content at 2:3 on a long virtual pager, looping once there are at
least five items, preloading five pages around the viewport.

![Portrait carousel](docs/images/home-vertical-banner.webp)

*The centre item is scaled up, and the whole section's background takes its colour from a palette
extracted from the active thumbnail — every item change is also a change of tone.*

---

## 2. Focus is the cursor on television

There is no mouse pointer on a television. Focus **is** the cursor, so it must always be visible and
must have exactly one owner after every transition. StreamTV uses no shared
`FocusRequesterModifiers`: focus behaviour is declared by the composable that owns it.

### Top-bar items expand on focus

![Top bar focus](docs/images/topbar-focus.gif)

*A top-bar item normally shows only its icon; on focus it expands horizontally over about 180ms and
reveals its English label. Profile stays an icon-only circle. While the top bar holds focus, a
translucent `surface` layer covers the content beneath it, so it is obvious who currently owns
navigation.*

Three rules matter most:

- **Destinations do not steal focus.** Selecting a different destination leaves focus on the top bar.
  A destination claims focus only when the top bar is not holding it — on cold launch, and on return
  from a player. See [`docs/adr/2026-09-02-shell-focus-ownership.md`](docs/adr/2026-09-02-shell-focus-ownership.md).
- **Re-entering the top bar restores the selected destination**, rather than jumping to the first item.
- **The top bar has its own overlay** — a vertical gradient from `surface` to transparent — switched on
  and off by the current destination. Home enables it only once focus leaves the first section,
  because the full-bleed banner already carries its own scrim.

### ContentRow: one focus target, with the list sliding beneath it

![Row navigation](docs/images/home-row-navigation.gif)

*Down into a rail, then right along it. The selector stays put at the leading content edge; it is the
list that slides underneath.*

This is the largest departure from an ordinary `LazyRow`:

- The whole row has exactly **one** focus target — a transparent bordered `SelectedItem` pinned to the
  leading content edge. The cards beneath it are **not** `focusable`.
- The selector's border is 2dp wider than the content on each side, giving it room to breathe without
  changing the card's size.
- The row always measures extra items beyond both edges, so movement never exposes a gap.
- With **more than five items**, the provider appends a full extra cycle of the collection, and after
  the animation past the end the state rebases onto the first cycle — an infinite loop with no visual
  jump. A collection of **five items or fewer** stays finite: Right on the last item does not reset to
  item `0`.
- At item `0` there is no phantom item to the left, and D-pad Left hands handling back to
  `FocusRequester.Default` so focus can leave the row.

---

## 3. Search, Calendar, Setting and Profile

The shell's four remaining destinations, each solving a different television problem.

### Search — the keyboard and the results on one screen

![Search](docs/images/search.webp)

*The input field, the search history and the a–z keyboard grid all sit in the first viewport. There is
no separate entry screen — the viewer never has to leave the results in order to type. Below,
"Recommended for you" is split by content type.*

### Calendar — a two-dimensional EPG grid

![Calendar](docs/images/calendar.webp)

*Time runs down, channels run across. A programme cell is as tall as its actual duration, so a
two-hour programme really is twice the height of a one-hour one. A gap in the schedule is a genuine
empty cell, not a placeholder. Focus is a white border around the cell.*

### Setting — two panes, the left list driving the right

![Setting](docs/images/setting.webp)

*The left list is grouped into Account / About / Privacy. The selected item inverts to a white
surface with dark text instead of scaling — in a dense column, scaling would make items collide.*

### Profile — signing in from another device

![Profile](docs/images/profile.webp)

*Typing a password on a D-pad is a poor experience, so this screen leads with a QR code and a
time-limited sign-in code. The QR is rendered in place. "Sign in with phone number" remains as a
fallback.*

---

## 4. The landscape player

`PlayerScreen` handles content shot landscape: videos, series episodes and live channels. The video
fills the panel and **every piece of chrome is transient**.

![Player surface](docs/images/player-surface.webp)

*The default state: nothing but the picture. An invisible full-screen input target holds the D-pad and
waits for the first key.*

### The controller

Press any direction to reveal the controller. It occupies the top and bottom edges and **leaves the
middle band clear**.

![Player controller](docs/images/player-controller.webp)

*A vertical scrim darkens both edges and stays fully transparent across the middle, so text remains
legible over any frame without dimming the part the viewer is actually watching.*

The control row is divided into three clusters:

| Cluster | Alignment | Contents |
|---|---|---|
| Leading | Leading edge | The `Description` pill |
| Transport | **Centred on the panel** | Rewind, play/pause, forward |
| Trailing | Trailing edge | Like, save and comment on one shared pill; settings on its own circle |

The transport cluster is centred on the **panel**, not on the space between the other two — which is
why its position does not shift when a stream has no settings and that control disappears.

A focused control **does not scale**; it **inverts**: an opaque white fill, a dark glyph, and a
caption naming it directly beneath. A control sitting on the shared pill would grow out of it.

On a live stream the seek bar is replaced by a single elapsed-time label, a `LIVE` badge precedes the
title, and rewind and forward are absent.

### Down from the seek bar returns to the control you last used

![Focus restore](docs/images/player-focus-restore.gif)

*Play/pause → right to **Save** → up to the seek bar (the thumb grows, Save stops being filled) → down
again to **Save**, not back to play/pause.*

Worth calling out: this behaviour does **not** use `Modifier.focusRestorer` or `saveFocusedChild()`.
Both hook into focus-search enter and exit, and here the control row is left and re-entered by direct
`FocusRequester` requests — which bypass those hooks, so the restore always finds nothing saved and
falls back. StreamTV remembers the last-used control in state instead.

Down from the control row **does nothing at all**. Letting it fall through hands focus to the video
surface, which immediately hides the controller the viewer is still using.

### Three side sections

All three open on the trailing edge inside a rounded, dark, translucent panel.

<table>
<tr>
<td width="50%"><img src="docs/images/player-metadata-section.webp" alt="Metadata section"></td>
<td width="50%"><img src="docs/images/player-comments-section.webp" alt="Comments section"></td>
</tr>
<tr>
<td><em><strong>Metadata</strong> — opened from the <code>Description</code> pill.</em></td>
<td><em><strong>Comments</strong> — a D-pad scroll viewport with a focus-aware scrollbar; Up and Down scroll to the boundary, then release the key so focus can move on.</em></td>
</tr>
</table>

![Quality settings](docs/images/player-settings-section.webp)

***Settings → Quality*** — *the rendition list read straight from the HLS manifest. Settings never shows
an empty category: this stream carries no subtitles and no alternative audio, so the root panel holds
a single Quality row.*

Back from a section returns focus to **the control that opened it** — Metadata to `Description`,
Comments to comment, Settings to settings. Back while the controller is showing only hides the
controller; the next Back leaves the player.

---

## 5. The portrait player

A television panel is landscape; a short is not. Rather than letterboxing it into two narrow bars or
cropping away the top and bottom of the frame, `VerticalPlayerScreen` builds a 9:16 stage centred and
nudged toward the leading edge, and **gives the freed width back to the content itself**.

![Vertical player](docs/images/vertical-player.webp)

*Three regions: a horizontal ambient gradient, a rounded 9:16 stage (the video crops to fill it, so no
bars appear inside the stage), and the interaction panel on the trailing edge. The stage carries an
inset white focus border — it is a real focus target, not a passive surface.*

This screen has **no** transport cluster, no seek bar, no `Description` pill and no caption under its
controls. The stage itself is the play/pause control, and the title block is the way into metadata.

![Vertical panel navigation](docs/images/vertical-player-panel.gif)

*Right from the stage lands on the **first action** — not the title block, which is one step Up from
there; the title container is tinted so the panel reads as a single region. Left from the first action
returns to the stage, while Left from a later action only moves within the row.*

![Vertical metadata](docs/images/vertical-player-metadata.webp)

*The same section tree as the landscape player, but drawn **transparent** over the ambient background
rather than inside a rounded panel. That framing difference lives at the screen boundary; the section
content itself is shared.*

---

## 6. Two players, one ViewModel

Both screens share one `PlayerViewModel`, one retained section tree under `component/section`, and one
`StreamTvPlayerManager` from the `stream_player` library (a separate project — see
[`docs/player-integration/`](docs/player-integration/)). The differences are all presentational:

| | Landscape player | Portrait player |
|---|---|---|
| Video fit | Letterboxed, fills the panel | Cropped into a centred 9:16 stage |
| Background | The video itself | Horizontal ambient gradient |
| Chrome lifetime | Transient, auto-hides after 5s | Permanent |
| Transport | Rewind, play/pause, forward | None; the stage is the control |
| Seek bar | Focusable, with thumb and time labels | Non-interactive progress line |
| Metadata entry | The `Description` pill | The title block |
| Caption under controls | Yes | No |
| Section panel | Rounded, dark, translucent | Transparent over the ambient background |
| Section dismissal | Back | Back or Left |
| Focus after closing a section | The control that opened it | The stage |

The two things they share matter most:

- **Exactly one group owns the D-pad at any moment**, and that group is *a single derived value*, not a
  set of independent flags. Precedence, highest first: `Error`, `Parked`, `Section`, `Controller`,
  `Surface`.
- **Focus is handed out in exactly one place.** A subtree must not request focus for itself while the
  owner is also deciding — a panel that did raced the owner and lost, leaving the screen with nothing
  focusable.

Full specifications, including the focus-graph tables and acceptance scenarios:
[`spec/player.md`](spec/player.md) and [`spec/vertical-player.md`](spec/vertical-player.md).

---

## 7. Reproducing these captures

Every image and GIF above is reproducible. None of them is a hand-taken screenshot left to drift out
of date.

```bash
python3 tools/capture_media.py list
python3 tools/capture_media.py shot player-controller
python3 tools/capture_media.py gif player-focus-restore
python3 tools/capture_media.py all
```

Requires `adb` on PATH with **exactly one** attached device, and `ffmpeg` for GIF conversion.

Each capture separates `setup` (the key path needed to get there, not recorded) from `steps` (the
demonstration itself), and every run force-stops and relaunches the app, so no capture inherits
another's focus. Stills are written as WebP, GIFs through a two-pass ffmpeg palette. Output lands in
[`docs/images/`](docs/images/).

[`updateReadme.md`](updateReadme.md) is the companion runbook: which captures to re-run for a given
source change, when a GIF is warranted over a still, and which dummy item each demo depends on.

---

# Technical reference

## Current functionality

- The top bar navigates between Search, Home, Calendar, Setting and Profile with the D-pad. While navigation holds focus, the app lays a translucent `surface` over the whole screen and keeps the top bar above it.
- Home receives a vertical list of sections; each section owns a `title`, a `viewType` and a horizontal content list.
- `Banner` is a full-width 600dp hero behind the top-bar overlay, with a layered hero scrim, a call to action, edge pages, an indicator, and lifecycle-aware auto-scroll while unfocused.
- `VerticalBanner` presents `Short` content at 2:3, looping on a long virtual pager once there are at least five items, preloading five pages around the viewport, scaling the active item and recolouring its background from a palette extracted from the active thumbnail.
- Initial focus belongs to the banner; Up returns to the top bar. Left and right move between items within the carousel.
- `Videos`, `ListSeries`, `Channels` and `Shorts` all use `ContentRow`: a horizontal lazy layout with a selector fixed at the leading content edge that always measures extra items beyond both edges, so movement never exposes a gap.
- Online images are loaded with Coil 3; the dummy `videoUrl` and `trailerUrl` values are HLS test streams (`trailerUrl` is drawn from the same VOD pool but rotated one step, so it never matches an item's own `videoUrl`), and `logoUrl` is currently left empty.
- Dependency injection uses Hilt; the graph is verified and generated at compile time by KSP.

## Home feature structure

```text
feature/home/
├── data/
│   ├── model/                  # Polymorphic DTOs and the source's viewType
│   ├── source/                 # HomeDummyDataSource
│   ├── mapper/                 # DTO -> domain
│   └── repository/             # DummyHomeRepository adapter
├── domain/
│   ├── model/                  # Content, Video, Series, Channel, Short, HomeSection
│   └── repository/             # Suspend HomeRepository contract
└── presentation/
    ├── component/              # HomeContent, Banner, BannerTrailer, VerticalBanner, ContentRow section, cards
    ├── mapper/                 # Domain -> UI model
    ├── model/                  # UI items and UI view types
    ├── HomeRoute.kt            # HomeScreen: binds the ViewModel, gives the banner its trailer slot
    ├── HomeUiState.kt
    ├── HomeViewModel.kt
    ├── HomeBannerTrailerUiState.kt   # UiState plus the pure fold deciding when video is shown
    └── HomeBannerTrailerViewModel.kt # owns one player for the banner's trailer
```

The app composition root is `app/di/HomeModule.kt`. Presentation never constructs a data source or a repository itself.

## Navigation

Navigation is split into two graphs. `MainScreen` is the content-browsing shell: it owns
`StreamTvTopBar` and a nested `MainNavHost`. The two player screens are siblings of `MainScreen` in
the outer graph, so they take the whole screen without anyone having to hide the top bar.

```
StreamTvNavHost (outer)
├── MainRoute ─────────► MainScreen
│                        ├── StreamTvTopBar
│                        └── MainNavHost (inner)
│                            ├── HomeRoute
│                            ├── SearchRoute
│                            ├── SettingRoute
│                            └── ProfileRoute
├── PlayerRoute
└── VerticalPlayerRoute
```

- A destination has a top bar if and only if it is registered in `MainNavHost`. There is no longer an
  `isPlayerRoute` predicate matching route prefixes to decide whether to hide the bar.
- Back needs no extra code: the inner NavHost handles it first, and once its stack is empty the event
  falls through to the outer NavHost.
- `MainNavHost` cannot see the outer controller, so opening a player goes through `onOpenPlayer` /
  `onOpenVerticalPlayer`. Which player opens is still decided by `HomeContentUiItem.playerTarget()`.
- Each browsing feature has exactly **one** `XxxScreen` composable, declared in `XxxRoute.kt`. Home
  keeps its stateless UI separate as `HomeContent` because it is large and is what the Compose tests
  exercise.

The decision, the alternatives and the consequences: [`docs/adr/2026-09-01-nested-main-navigation.md`](docs/adr/2026-09-01-nested-main-navigation.md).

## Dependency injection with Hilt

- `StreamTvApplication` is annotated `@HiltAndroidApp` to create the application-level container.
- `MainActivity` is annotated `@AndroidEntryPoint` to connect the Android entry point to the Hilt graph.
- `HomeModule` is installed into `SingletonComponent` and provides `HomeDummyDataSource`, `HomeRepository` and `HomeUiMapper`.
- `PlayerModule` provides `StreamTvPlayerFactory`, `PlayerDummyDataSource` and `PlayerDetailsRepository`;
  the UI never constructs a data source or an ExoPlayer directly.
- `HomeViewModel`, `SearchViewModel`, `SettingViewModel` and `ProfileViewModel` all use `@HiltViewModel` with constructor injection.
- Every feature route obtains its ViewModel with `hiltViewModel()`; there is no manual factory or dependency container left in production code.

The domain layer stays plain Kotlin. Hilt wiring lives only at the app composition root and the presentation entry points, which is what makes it possible to swap a dummy repository for a remote one without touching a ViewModel or any UI.

## Data flow

```text
HomeDummyDataSource
    -> DummyHomeRepository
    -> HomeDataMapper
    -> HomeViewModel
    -> HomeUiMapper
    -> HomeUiState
    -> HomeScreen
    -> HomeContent
```

`HomeViewModel` calls the suspend repository directly inside `viewModelScope`, cancels the previous request on reload, and does not swallow `CancellationException`. `PlayerViewModel` calls `PlayerDetailsRepository` directly — there is no intermediate use case — then `combine`s playback state with content and action state into an immutable `PlayerUiState` via `stateIn(SharingStarted.Eagerly)`.

`Content` is a sealed hierarchy of:

- `Video`: a single video, 16:9 thumbnail.
- `Series`: content that additionally carries `episodes: List<Video>`.
- `Channel`: content broadcast live.
- `Short`: portrait video, 2:3 thumbnail.

Every content item has `id`, `videoUrl`, `trailerUrl`, `thumbnailUrl`, `vastUrl`, `title`, `description`, `ageRestriction` and `logoUrl`. `id` exists to give Compose a stable key; the `Url` suffix on `logoUrl` makes its type unambiguous.

Valid pairings are checked in the constructors of `HomeSection` and `HomeSectionUiItem`:

| `viewType` | Required item type |
|---|---|
| `Banner` | `Video` |
| `VerticalBanner` | `Short` |
| `Videos` | `Video` |
| `ListSeries` | `Series` |
| `Channels` | `Channel` |
| `Shorts` | `Short` |

A mistyped section is rejected at the boundary rather than silently filtered out inside Compose.

## Dummy imagery

Dummy thumbnails use Pexels photographs covering sport, animals, Chinese culture and Japanese culture.

The dummy sections deliberately cover both `ContentRow` boundaries: Videos has 8 items, Channels has 6 and Shorts has 8, all of which loop; Documentary Series has 4, which keeps it finite.

- [Basketball](https://www.pexels.com/photo/men-playing-basketball-9839903/)
- [Football](https://www.pexels.com/photo/soccer-player-on-field-during-match-36958062/)
- [Cricket](https://www.pexels.com/photo/a-man-holding-a-wooden-paddle-11023865/)
- [Bengal tiger](https://www.pexels.com/photo/tiger-in-a-forest-25785873/)
- [Tiger portrait](https://www.pexels.com/photo/photo-of-a-tiger-12167844/)
- [Chinese festival](https://www.pexels.com/photo/vibrant-traditional-chinese-cultural-festival-30765119/)
- [Chinese New Year](https://www.pexels.com/photo/young-woman-celebrating-lunar-new-year-outdoors-36603900/)
- [Tokyo street](https://www.pexels.com/photo/people-walking-in-city-in-japan-12343886/)
- [Japanese ceremony](https://www.pexels.com/photo/traditional-japanese-ceremony-with-participants-31370378/)

## Focus on Android TV

StreamTV does not use the reference project's `FocusRequesterModifiers`. Focus behaviour is declared by the composable that owns it:

- `HomeBannerSection` attaches `contentFocusRequester` and declares `up = topBarFocusRequester`.
- Banner and VerticalBanner handle D-pad left and right themselves via `onPreviewKeyEvent`.
- Auto-scroll stops when the carousel receives focus.
- The trailer runs only while the carousel holds focus and the screen is RESUMED. A single `LaunchedEffect` keyed on `(item, isBannerFocused, isScreenResumed)` owns both the five-second delay and the one stop call in its `finally`, so every exit — losing focus, changing item, disposal, navigation — stops the player.
- Top-bar items declare Down toward the content focus requester.
- `HomeContent` tracks which section holds focus through `onFocusChanged` and enables the top-bar overlay once the index is greater than zero. `MainScreen` lowers the overlay itself on every destination change.

### ContentRow

The base component lives in `core/designsystem/component/contentrow` and offers a DSL close to `LazyRow`:

```kotlin
val state = rememberContentRowState()

ContentRow(state = state) {
    items(
        items = videos,
        key = VideoUiItem::id,
    ) { video ->
        VideoCard(video)
    }
}
```

- `ContentRow` is built on `LazyLayout`; only items inside the viewport and its adjacent buffer are composed and measured.
- The whole row has one focus target — a transparent bordered `SelectedItem` fixed at the leading content edge. The cards beneath are not `focusable`.
- The selector's border is 2dp wider than the content on each side, giving room to breathe without changing card size.
- D-pad Left and Right move the list beneath the selector; Center and Enter invoke the callback for the real selected index.
- Moving right slides the previous item past the leading edge but still leaves a sliver of it at the screen edge while the row sits on later indices.
- At item `0` there is no phantom item to the left, and D-pad Left hands handling back to `FocusRequester.Default`.
- With more than five items, the provider appends a full extra cycle of the collection. Items `0, 1, 2...` are therefore always visible beyond the end of the row; after the animation past the end, the state rebases onto the first cycle without a gap or a visual jump.
- A collection of five items or fewer stays finite: D-pad Right on the last item does not reset to item `0`.
- `ContentRowState.scrollToItem(index)` wraps the index for looping rows and clamps it for finite ones.

## Player

The player is a full-screen destination in the outer graph with two presentations sharing one
`PlayerViewModel`:

- `PlayerScreen` plays landscape content on a 16:9 surface. The controller is an overlay inside a `Box`
  that auto-hides after five seconds while playback advances, and carries only the title, Like, Save,
  Comment, Settings and the duration/progress; there is no related or episodes list and no `LazyColumn`
  for content.
- `VerticalPlayerScreen` keeps a 9:16 stage centred and nudged left, a dark ambient background, and the
  interaction section on the trailing edge. D-pad Right moves from the player to the first action;
  D-pad Left returns to the player.
- Both orientations share the retained section tree in `component/section`: Metadata,
  Comments → Replies → Reply detail, and Settings → Quality/Subtitles/Audio. A parent stays composed
  beneath its child so it keeps its list state and selected item.
- When a section begins entering or a child begins exiting, focus is moved to a pending target that
  always exists. After the animation the new section takes focus; this stops Compose from jumping focus
  back to the player or to some other control while a node is disappearing.
- The landscape player remembers exactly which control opened a root section: Back from Metadata
  returns to `Description`, from Comments to Comment, and from Settings to Settings. Back while the
  controller is visible only hides the controller; the next Back leaves the player.
- The portrait player uses Left or Back to pop one level at a time. A child returns to the row that
  opened it; a root returns to the portrait player, after which Right moves focus into the interaction
  section.
- The track snapshot from `stream_player` is mapped once in `presentation/mapper`. Selecting an option
  dispatches `selectVideoTrack`, `selectTextTrack` or `selectAudioTrack` straight through
  `PlayerViewModel`.
- Settings never shows an empty category; Subtitles carries an Off option, and Quality carries Auto
  whenever the manifest offers several renditions.
- Dummy metadata and comments come from `PlayerDummyDataSource` through
  `DummyPlayerDetailsRepository -> domain model -> PlayerDetailsUiMapper -> PlayerUiState`; all
  displayed copy is English.

## Adding a ContentRow section

1. Map the view type to a `HomeContentRowStyle` in `HomeScreen.kt`.
2. Pass typed items through `requireItemsOfType()`; the mapper and the model already guarantee the item type.
3. Render the card inside the `ContentRow` DSL, supplying a stable `key` and a `contentType`.
4. Do not make the card `focusable`; focus, looping and D-pad handling are all encapsulated in the base component.

## Per-screen specifications

`spec/` is the framework-neutral product contract — read it before implementing, or before porting to
another platform.

| Screen | Specification |
|---|---|
| Shared foundations, top bar, D-pad contract | [`spec/README.md`](spec/README.md) |
| Home | [`spec/home.md`](spec/home.md) |
| Landscape player | [`spec/player.md`](spec/player.md) |
| Portrait player | [`spec/vertical-player.md`](spec/vertical-player.md) |
| Search | [`spec/search.md`](spec/search.md) |
| Calendar | [`spec/calendar.md`](spec/calendar.md) |
| Setting | [`spec/setting.md`](spec/setting.md) |
| Profile | [`spec/profile.md`](spec/profile.md) |

## Build and test

Requires JDK 17 or newer and Android SDK 37.

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebugAndroidTest
./gradlew :app:lintDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.
