# Implementation plan — Seek frame preview

- **Date:** 2026-09-02
- **Spec:** `player-seek-preview-spec.md`

## Shape of the data

```kotlin
// domain/model/PlayerDetails.kt
data class PlayerSeekPreview(val frameUrls: List<String>)
```

Frames are **evenly spaced across the whole video**, and the spacing is deliberately not stored.
Playback duration is only known once the stream is prepared, so a fixed interval baked into the model
would drift further from the truth the longer the video ran. Position resolves by slice instead:

```kotlin
val slice = (fraction.coerceIn(0f, 1f) * frameUrls.size).toInt()
frameUrls[slice.coerceIn(0, frameUrls.lastIndex)]
```

An empty list is the "no strip" state — no separate flag, and no way to be available and empty at the
same time. `PlayerSeekPreviewUiStateTest` covers the slice arithmetic and both clamps.

## Layers touched

| Layer | Change |
|---|---|
| domain | `PlayerSeekPreview`; `PlayerDetails.seekPreview` |
| data | `PlayerDetailsData.seekPreviewFrameUrls`; mapper; a fixed strip in `PlayerDummyDataSource` |
| presentation | `PlayerSeekPreviewUiState` with `isAvailable` / `frameUrlAt`; `PlayerDetailsUiState.seekPreview` |
| ui | `PlayerSeekPreviewCard.kt` (lane + card), `PlayerSeekBar.kt` (extracted from the controller) |

## Replacing the dummy strip

Only the data layer changes. Fetch the strip in a data source, map it to `seekPreviewFrameUrls`, and
the UI needs no edit — an empty list already means "no preview", so a strip that arrives late or not
at all is the state the UI was written against. Request thumbnails at roughly 320px wide: the card is
148.dp, and pulling full-size stills to fill it would move megabytes while the viewer scrubs.

## UI decisions

**The card replaces the title row rather than sitting above it.** The controller's upper lane holds
both; the title and actions fade to alpha 0 while the card is up. They stay in the composition and
stay focusable — pressing Up moves focus to them, which clears the scrubbing state and fades them
straight back in. Removing them instead would leave `focusProperties { up = … }` pointing at a
detached `FocusRequester`.

**Placement is `offset(x = (trackWidth - cardWidth) * fraction)`**, not a centre on the thumb. That
puts the card flush with the track's left edge at 0 and its right edge at 1 with no clamping code, and
matches how the reference player behaves at both ends.

**Scrubbing is a state, not just focus.** The seek bar is where focus lands by default, so keying the
card off focus alone would hide the title the moment the controller opened. The card appears on the
first seek key and retires 1.6s after the last one.

## Test plan

- `PlayerSeekPreviewUiStateTest` — slice boundaries, both clamps, single-frame and empty strips.
- `PlayerControllerSeekPreviewTest` — no card before the first seek; card after; no card for a video
  without a strip; the seek bar takes focus when the controller opens.
