# ADR — Lottie for the player's buffering indicator

- **Date:** 2026-09-02
- **Status:** Accepted

## Context

`androidx.tv.material3` ships no progress indicator, so the buffering spinner was drawn by hand: a
`rememberInfiniteTransition` rotating a 90° arc on a `Canvas`. It worked, but every change to how the
spinner looks was a code change, and the app had no way to take a spinner from design as an asset.

## Decision

Play the spinner from a Lottie animation.

- `com.airbnb.android:lottie-compose` (6.7.1) — the only Compose-native Lottie renderer, and it needs
  no second design system the way pulling in `compose-material3` for one arc would have.
- The animation lives at `app/src/main/res/raw/loading_lottie.json`. It is the single source of truth
  for how the spinner looks: `PlayerBufferingIndicator` does not tint, re-time or re-scale it, so
  restyling the spinner means replacing that one file.
- The file currently in the repository is a **placeholder** — a hand-written 1-second rotating ring,
  120×120, white stroke, 25% trim — so the pipeline is exercised end to end until the designed
  animation replaces it.
- The hand-drawn arc survives as a private fallback, rendered while the composition is null: during
  the frames the file is still parsing, and permanently if it fails to parse. This indicator is the
  only thing on screen telling a viewer that playback is still coming, so it must never be an empty
  box.

`PlayerBufferingIndicatorTest.loadingAnimationParses` asserts the raw resource actually parses. Without
it a malformed file would leave the fallback arc spinning forever — which looks exactly like a working
spinner, so nothing else would catch it.

## Consequences

- One new dependency, ~1.2 MB of library. Justified by the asset workflow it unlocks: any future
  animated affordance (empty states, transitions, loading skeletons) now has a renderer.
- Replacing `loading_lottie.json` needs no Kotlin change, but the parse test must stay green — it is
  the guard on the new asset.
- `PlayerOverlayDefaults` lost its spinner tokens; sizing and the fallback arc's geometry now live
  with the indicator in `PlayerBufferingIndicator.kt`.
- Lottie's own renderer is used, not `LottieAnimation`'s software fallback; on a TV device the
  animation is small and static in size, so the cost is negligible next to video decode.
