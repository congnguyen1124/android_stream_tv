# Implementation plan — Home banner trailer

Implements `docs/home-banner-trailer/home-banner-trailer-spec.md`.

## 1. Files

| Layer | File | Change |
|---|---|---|
| Data | `feature/home/data/model/HomeContentData.kt` | `trailerUrl` on the sealed interface and all four items |
| Data | `feature/home/data/source/HomeDummyDataSource.kt` | A trailer stream per item, rotated out of the existing VOD pool |
| Data | `feature/home/data/mapper/HomeDataMapper.kt` | Carry `trailerUrl` |
| Domain | `feature/home/domain/model/Content.kt` | `trailerUrl` on the sealed interface and all four models |
| Presentation | `feature/home/presentation/mapper/HomeUiMapper.kt` | Carry `trailerUrl` |
| Presentation | `feature/home/presentation/model/HomeContentUiItem.kt` | `trailerUrl` on the sealed interface and all four items |
| Presentation | `feature/home/presentation/HomeBannerTrailerUiState.kt` | **New** — UiState, decision type, pure `reducePlayback` fold |
| Presentation | `feature/home/presentation/HomeBannerTrailerViewModel.kt` | **New** — owns one player; load, loop, unload, close |
| Presentation | `feature/home/presentation/HomeRoute.kt` | Supplies the trailer as a slot to `HomeContent` |
| UI | `feature/home/presentation/component/HomeBannerTrailer.kt` | **New** — dwell timing and the player surface |
| UI | `feature/home/presentation/component/HomeBannerSection.kt` | `bannerTrailer` slot, drawn over the thumbnail |
| UI | `feature/home/presentation/component/HomeContent.kt` | Threads the slot to the banner section |
| Core | `core/player/StreamTvPlayerFactory.kt` | **Moved** from `feature/player/presentation/` |

`trailerUrl` goes on the sealed interfaces rather than on `Video` alone, so it sits beside `videoUrl`
and `vastUrl` for every content type and a future trailer on another surface needs no model change.

## 2. Where the player lives

`HomeBannerTrailerViewModel` owns exactly one `StreamTvPlayerManager`, built through
`StreamTvPlayerFactory`. It is a second ViewModel on the Home back-stack entry rather than a branch of
`HomeViewModel`, because a player needs an owner with a `close()` and `HomeViewModel` has no reason to
have one.

The factory moved to `core/player/` in the process: two features build players now, and Home reaching
into `feature/player/presentation/` for the seam would be a sideways dependency between features.

The player deliberately outlives the banner composable. Scrolling the banner out of the lazy column
disposes the surface, and rebuilding a player on the way back would pay for a decoder handshake to
show the same frames; the session is stopped instead and the player reused. Nothing is built at all
until a banner section actually renders, because `hiltViewModel()` is called from inside the slot.

## 3. Session state machine

The composable owns *when* a session runs; the ViewModel owns *what happens during one*.

```
       focused && resumed && trailerUrl != ""
                    │
            wait 5 s (thumbnail on screen)
                    │
              startTrailer()  ── load, prepare, play
                    │
   ┌────────────────┴─────────────────┐
   │                                  │
isPlaying                          error
   │                                  │
isTrailerRendering = true       thumbnail stays, no retry
   │
Ended ──► replay() ──► (buffering keeps the video visible)

any of: focus lost · item changed · screen paused · banner disposed
                    │
              stopTrailer()  ── clear (stop + unload)
```

One `LaunchedEffect` in `HomeBannerTrailer` keyed on `(item, isBannerFocused, isScreenResumed)` drives
the whole left column: the 5 s `delay`, then `awaitCancellation()` in a `try` whose `finally` is the
single place a trailer is ever stopped. Every exit — key change, dispose, navigation, background —
reaches it, so there is no transition that needs its own handler.

`isTrailerRendering` is sticky within a session: set when frames advance, cleared only by a stop or a
failure. Clearing it on `Buffering` would flash the thumbnail back on every loop.

## 4. Why a slot instead of hoisted state

`HomeContent` and `HomeSection` take
`bannerTrailer: @Composable (VideoUiItem, Boolean) -> Unit`, defaulting to empty. The route fills it
in. The alternative — hoisting the player, the flag and two callbacks up to `HomeRoute` — would thread
four playback parameters through two composables that have nothing to do with video, and would make a
banner without a player the special case instead of the default. With the slot, previews and
`HomeContentTest` render a thumbnail-only banner and need no player, no Hilt and no changes.

## 5. Test plan

| Test | Covers |
|---|---|
| `HomeBannerTrailerTest` (new, 6 cases) | The whole fold: thumbnail holds until frames advance, replay on end, no thumbnail flash mid-loop, failure is final, events from a stopped session are ignored |
| `DummyHomeRepositoryTest` (2 new cases) | Every dummy item has a trailer, it is a different stream from its own `videoUrl`, and no trailer is a live manifest |
| `HomeContentTest`, `HomeUiMapperTest`, `HomeSectionTest`, `HomeViewModelTest` | Updated for the new field; the banner keeps taking initial focus |

**Not covered by unit tests:** the command dispatch path (`Load` / `Prepare` / `Play` / `Clear`) and
the dwell timing. `HomeBannerTrailerViewModel.startTrailer` calls `String.toUri()`, and
`android.net.Uri.parse` is not available in a local JVM test — the mockable `android.jar` throws. That
is why the decision logic is a pure function taking primitives instead of living in the ViewModel:
everything that could plausibly be wrong is testable without a device. Covering the dispatch path
would mean adding Robolectric, which is a project-level decision rather than part of this change.

Manual verification on a device or emulator:

```bash
./gradlew :app:assembleDebug
```

1. Launch: banner holds its still, then the trailer fades in after ~5 s.
2. Press Right: video stops, thumbnail returns, new trailer after ~5 s.
3. Press Down twice, then Up: quiet on the rows, trailer restarts on return to the banner.
4. Let a trailer run to its end: it starts over with no thumbnail flash.
5. Point one dummy `trailerUrl` at an unreachable host and confirm the banner just stays a still.
6. Background and foreground the app: playback stops and the wait restarts.

## 6. Risks

| Risk | Mitigation |
|---|---|
| Two players alive when the full-screen player opens from the banner | `stopTrailer` unloads (`stop` + `clearMediaItems`), so the banner's decoder is released, and the library builds players with `enableDecoderFallback = true` |
| A viewer resting on the banner streams video indefinitely | Accepted — this is the point of the feature. Playback ends the moment focus leaves |
| Trailer audio plays unexpectedly | Known; no volume command exists in `stream-player`. Tracked in the spec's "Open points" |
