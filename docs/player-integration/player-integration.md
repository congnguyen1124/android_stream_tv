# Player integration — stream-player

How StreamTV plays video. This document covers the dependency wiring, the two player screens, how the
app decides between them, and how to verify the whole path.

## 1. What was integrated

Playback is provided by **`com.congnguyencn:stream-player`**, a standalone Media3/ExoPlayer library
maintained in its own repository. The library exposes playback as *commands in, one immutable state
out*: the app dispatches command values and collects a single `StreamTvPlayerState`, and never touches
an ExoPlayer listener.

The library deliberately ships **no player UI** — no play button, seek bar, or settings sheet. A D-pad
TV surface and a touch surface need different controls, so the app owns all of them. What this app
added on top:

| Surface | Content | Framing |
|---|---|---|
| `PlayerScreen` | Videos, series episodes, live channels | Landscape, letterboxed (`RESIZE_MODE_FIT`) |
| `VerticalPlayerScreen` | Shorts, vertical banner | Portrait 9:16 stage, cropped (`RESIZE_MODE_ZOOM`) |
| `HomeBannerTrailer` | The focused home banner item's trailer | Full-bleed hero, cropped (`RESIZE_MODE_ZOOM`), no chrome at all |

The two screens are driven by **one `PlayerViewModel`**. The banner trailer has its own
`HomeBannerTrailerViewModel` and its own player, because it is ambient playback with a different
lifetime and no controls — see `docs/home-banner-trailer/`.

## 2. Dependency wiring

`stream-player` lives outside this repository, so `settings.gradle.kts` pulls it in as a **composite
build**:

```kotlin
val streamPlayerDir = file("../stream_player")
require(streamPlayerDir.isDirectory) { /* actionable message */ }
includeBuild(streamPlayerDir)
```

Gradle substitutes the local checkout for the `com.congnguyencn:stream-player` coordinate declared in
`gradle/libs.versions.toml`, so edits to the library are picked up on the next build with no publish
step. Verify the substitution with:

```bash
./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep stream-player
```

which prints `com.congnguyencn:stream-player:0.1.0 -> project ':stream_player:stream-player'`.

### Requirement: the sibling checkout

**The build fails without `../stream_player` on disk.** The `require` above states the fix rather than
letting Gradle emit an unresolved-dependency error. Clone the library next to this repository:

```
CN_Develop/
├── steam_tv/          ← this repository
└── stream_player/     ← the library
```

### Alternative: Maven Local

For a machine or CI job that cannot have the sibling checkout, publish the library instead:

```bash
cd ../stream_player && ./gradlew :stream-player:publishToMavenLocal
```

Then delete the `includeBuild` block and add `mavenLocal()` to
`dependencyResolutionManagement.repositories`. The version in the catalog (`0.1.0`) already matches
the published artifact, so nothing else changes.

### Core library desugaring

`app/build.gradle.kts` enables it:

```kotlin
compileOptions {
  isCoreLibraryDesugaringEnabled = true
}
dependencies {
  coreLibraryDesugaring(libs.desugar.jdk.libs)
}
```

Required because `stream-player` links against the Google IMA SDK for client-side ad insertion, and
IMA's AAR metadata demands desugaring. **This app runs ads-off** (see §6), but the dependency exists at
compile time and AAR metadata is checked regardless of whether the code path ever runs. Removing this
requirement for ad-free consumers means splitting the library's IMA code into a separate optional
module — see §9.

## 3. Files

### Added

```
app/src/main/java/com/congnguyencn/stream_tv/
├── app/di/PlayerModule.kt                              ← provides StreamTvPlayerFactory
├── core/player/StreamTvPlayerFactory.kt                ← test seam for player creation
├── feature/home/presentation/navigation/
│   └── HomePlayerTarget.kt                              ← which player an item opens in
└── feature/player/presentation/
    ├── PlayerUiState.kt                                 ← contract + state/error mappers
    ├── PlayerViewModel.kt                               ← shared by both screens
    ├── PlayerRoute.kt                                   ← binds VM → PlayerScreen
    ├── VerticalPlayerRoute.kt                           ← binds VM → VerticalPlayerScreen
    ├── PausePlaybackWhenStopped.kt                       ← lifecycle policy
    ├── navigation/PlayerNavigation.kt                   ← routes, args, nav extensions
    └── component/
        ├── PlayerScreen.kt                              ← landscape screen
        ├── VerticalPlayerScreen.kt                      ← portrait screen
        ├── PlayerOverlay.kt                             ← shared chrome pieces
        └── PlaybackKeyEvents.kt                         ← remote key → action mapping
app/src/main/res/drawable/ic_pause.xml
docs/player-integration/player-integration.md            ← this file
```

### Changed

| File | Change |
|---|---|
| `settings.gradle.kts` | `includeBuild("../stream_player")` |
| `app/build.gradle.kts` | `stream-player` dependency, core library desugaring |
| `gradle/libs.versions.toml` | `stream-player`, `desugar-jdk-libs` entries |
| `feature/home/data/source/HomeDummyDataSource.kt` | Real HLS URLs on all 35 items (§5) |
| `feature/home/presentation/HomeScreen.kt` | Threads `onItemClick` to all six sections |
| `feature/home/presentation/HomeRoute.kt` | Accepts and forwards `onItemClick` |
| `feature/home/presentation/navigation/HomeNavigation.kt` | `homeScreen(onItemClick = …)` |
| `app/navigation/StreamTvNavHost.kt` | Player destinations + target dispatch |
| `app/StreamTvApp.kt` | Hides the top bar on player destinations |
| `app/res/values/strings.xml` | Player error copy, control labels |

## 4. How the flow works

```
  Home item tapped
        │
        ▼
  item.playerTarget()                   HomePlayerTarget.kt — exhaustive on item type
        │
        ├── Horizontal ──► navigateToPlayer(url, title) ──────► PlayerRoute
        └── Vertical ────► navigateToVerticalPlayer(url, title) ► VerticalPlayerRoute
                                                  │
                                                  ▼
                                          PlayerViewModel (one class, one instance per route)
                                                  │  loadAndPlay(uri)
                                                  ▼
                                          StreamTvPlayerManager  (the library)
                                                  │  StateFlow<StreamTvPlayerState>
                                                  ▼
                                          toPlayerUiState(title) ──► PlayerUiState
                                                  │
                                                  ▼
                                     PlayerScreen / VerticalPlayerScreen
```

### Choosing the player

`HomePlayerTarget.kt` owns the decision:

```kotlin
internal fun HomeContentUiItem.playerTarget(): HomePlayerTarget = when (this) {
  is ShortUiItem -> HomePlayerTarget.Vertical
  is VideoUiItem, is SeriesUiItem, is ChannelUiItem -> HomePlayerTarget.Horizontal
}
```

Two decisions worth stating:

- **It keys off the content type, not the section.** A short is shot portrait and must be framed that
  way whether it was tapped in the vertical banner or in the "Fresh shorts" row. Because
  `HomeSectionViewTypeUi.VerticalBanner` and `Shorts` both carry `ShortUiItem` exclusively (enforced
  by `HomeSectionViewTypeUi.accepts`), keying off the item covers both required cases.
- **It lives in the home feature, not the player feature.** The player never learns home's item types,
  so a second caller can reuse both screens without publishing into home's model.

The `when` is exhaustive with no `else`, so a new content type fails to compile until it is
classified.

### One ViewModel, two screens

`PlayerViewModel` serves both routes. Loading, play/pause, seeking, error recovery and teardown are
identical whether the video is 16:9 or 9:16 — only framing and the offered controls differ. Splitting
the ViewModel would mean maintaining the same playback logic twice.

Each route still gets **its own instance** via `hiltViewModel()`, because they are separate
destinations. That is correct: one player per screen, released when that screen leaves the back stack.

### Navigation arguments

The stream URL travels in the route, percent-encoded:

```kotlin
"$base?videoUrl=${Uri.encode(videoUrl)}&title=${Uri.encode(title)}"
```

Encoding is mandatory — an HLS URL contains `/`, `?` and `&`, all of which Navigation Compose would
otherwise parse as route structure, and the destination would simply not match.

The URL is passed rather than a content id so playback does not depend on the home repository. An id
would force the player to know where content comes from, and every future caller to publish into the
same catalogue.

## 5. Stream catalogue

`HomeDummyDataSource` previously left `videoUrl` empty on every item, so nothing could play. It now
carries free public HLS test streams, assigned by section so each surface exercises a different case:

| Section | Streams |
|---|---|
| Videos, series, episodes | Apple BipBop (TS / fMP4 / HEVC), Tears of Steel, Big Buck Bunny ABR, Shaka Angel One, Bitmovin Sintel, Mux IMSC captions, Mux DAI discontinuity |
| Shorts, vertical banner | JW Player BBB, Longtail BipBop, Mux Test 001, Big Buck Bunny fixed, Mux PTS shift, Mux SAMPLE-AES, hls.js issue 666 |
| Live channels | Akamai Live, Akamai Eight Live, Shaka Live |

URLs are grouped in a `private object StreamUrls` with shared base constants (`AppleBase`, `MuxBase`,
`UnifiedBase`) so each line stays within the 120-character limit and the host appears once.

Live streams are on the channel rows on purpose: they report no duration, which is what exercises the
seek-bar-suppressed and `LIVE`-badge paths in `PlayerScreen`.

Every item also carries a `trailerUrl` for the home banner, drawn from the same VOD pool but rotated
one position, so no item's trailer is its own feature stream and the banner's hand-off from thumbnail
to trailer is visible. Live manifests stay out of that rotation: a trailer has to reach its end for
the banner's loop-back to run at all.

## 6. Player configuration

`PlayerModule` provides every player with `StreamTvPlayerConfig.Tv`:

```kotlin
StreamTvPlayerManager.create(context = context, config = StreamTvPlayerConfig.Tv)
```

That preset means steady buffering (50 s), **no disk cache**, and **no ads**. Every destination here
plays one item to its end, so there is nothing for a small per-item cache to make faster — and with no
cache there are no background writes competing with the video for bandwidth.

Switch to `StreamTvPlayerConfig.Feed` if a swipeable shorts feed lands: it enables the cache and the
low-latency buffer, which is the right trade when an item is re-entered seconds after being swiped
past. Nothing else has to change.

`StreamTvPlayerFactory` exists so a ViewModel does not call `StreamTvPlayerManager.create` directly.
Creating a real player needs a `Context`, a decoder and a surface, none of which a unit test can
supply; with the seam, a test binds a fake and the ViewModel's state mapping becomes testable off
device. It lives in `core/player/` rather than in this feature, because Home's banner trailer builds a
player too and neither feature should reach into the other's package for the seam.

## 7. Controls and lifecycle

### Remote keys

`PlaybackKeyEvents.handlePlaybackKeyEvent` maps presses to actions, as a plain function so both
screens share it and it is testable without a surface:

| Key | Landscape | Portrait |
|---|---|---|
| D-pad centre / Enter / Space / Play-Pause | Toggle play/pause | Toggle play/pause |
| D-pad right / Fast-forward | Seek forward | Consumed, no-op |
| D-pad left / Rewind | Seek back | Consumed, no-op |
| Back | Unconsumed → `popBackStack()` | Unconsumed → `popBackStack()` |

Seek keys are consumed even when they do nothing, so they cannot move focus off a full-screen player.
They are inert on live streams (nowhere to seek) and in the portrait player (a seek step would
overshoot a whole short).

### Top bar

`StreamTvApp` draws the top bar above the nav host for every destination. It is **skipped entirely**
on player routes via `isPlayerRoute(currentRoute)` — not merely hidden, because a present-but-invisible
bar would still take D-pad focus away from the player.

### Lifecycle

`PausePlaybackWhenStopped` pauses on `ON_STOP` and resumes on `ON_START`. The library ignores
lifecycle by design (a PiP or background-audio surface wants to keep playing), so each screen states
its own policy. A full-screen TV player has no reason to decode video nobody can see, but should not
lose its position — so it pauses rather than tearing down.

Teardown is `PlayerViewModel.onCleared() → playerManager.close()`. `close()` is mandatory and
idempotent; skipping it leaks a decoder, and on most devices the third or fourth leaked decoder is
where playback stops initialising.

### Errors

The library classifies failures; the app owns the copy. `PlayerUiState.error` carries a
`@StringRes` message and `isRetryable`, and the retry button is omitted entirely when the library says
retrying cannot help:

```kotlin
onRetry = onRetry.takeIf { error.isRetryable }
```

Retry is `prepare()` then `play()`, in that order — `prepare` is what clears the error, and playing a
still-errored player is a no-op.

## 8. Verification

Run the gate:

```bash
./gradlew spotlessApply
./gradlew spotlessCheck detekt
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Measured on an `Android_TV_720p` emulator (API 36, `sdk_google_atv64_arm64`) with the debug APK:

- `spotlessCheck` + `detekt` — clean
- `:app:testDebugUnitTest` — 19 tests, 0 failures
- **Landscape player**: opened from the banner. `ExoPlayerImpl: Init … [AndroidXMedia3/1.11.0]`,
  audio and video `MediaCodec` adapters created (`c2.goldfish.h264.decoder`), frames rendering, title
  and `0:18 / 10:00` shown, progress bar advancing from the left edge with the buffered segment ahead
  of it.
- **Portrait player**: opened from the vertical banner. 9:16 stage centred on the 1280×720 panel,
  video cropped to fill, black surround, title and progress bar at the bottom of the stage, top bar
  absent.
- **Back navigation**: returns to Home with focus and scroll position intact, top bar restored, no
  `FATAL`/`AndroidRuntime` entries in logcat.

Reproduce the walkthrough with:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.congnguyencn.stream_tv/.MainActivity
adb shell input keyevent KEYCODE_DPAD_CENTER          # banner → landscape player
adb shell input keyevent KEYCODE_BACK
adb shell input keyevent KEYCODE_DPAD_DOWN            # ×4 → vertical banner
adb shell input keyevent KEYCODE_DPAD_CENTER          # → portrait player
```

## 9. Known limitations

| Limitation | Why, and what would fix it |
|---|---|
| Requires `../stream_player` on disk | The library is intentionally a separate repository. The Maven Local route in §2 removes the requirement at the cost of a publish step. Neither works unchanged on CI without one of the two set up. |
| Core library desugaring forced by IMA | The library compiles IMA in unconditionally. Moving `internal/ads/` into an optional `stream-player-ima` module would let ad-free consumers skip both the dependency and the desugaring requirement. |
| Portrait player plays a single item | No swipe between shorts. The library already ships `StreamTvPlayerPool` and `StreamTvPreloader` for exactly this; a vertical pager over pool-acquired players is the natural next step, together with switching to `StreamTvPlayerConfig.Feed`. |
| No seek-to-position UI | Seeking is D-pad increments only (10 s, from the config). A scrubbing thumb needs a focusable seek bar. |
| No `@Preview` functions | Consistent with the rest of this module, which has none, and there is no theme-wrapping preview convention here yet. |
| Landscape player has no track selection UI | `PlayerUiState` does not surface the library's `audioTracks` / `textTracks` / `videoTracks` yet; the library exposes them and `selectAudioTrack` / `selectTextTrack` / `selectVideoTrack` are one dispatch away. |

## 10. Reference

The library's own `architecture.md` is the complete reference for its API, configuration presets,
invariants and recipes — including the player pool, preloader, signed-URL auth and CSAI ads that this
integration does not yet use.
