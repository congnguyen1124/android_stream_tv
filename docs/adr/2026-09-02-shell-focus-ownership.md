# ADR — Who claims D-pad focus in the browsing shell

- **Date:** 2026-09-02
- **Status:** Accepted

## Context

Every destination under `MainScreen` used to request focus for itself as soon as it composed:

```kotlin
// HomeContent.kt
LaunchedEffect(uiState.sections) {
  if (uiState.sections.any { it.viewType == HomeSectionViewTypeUi.Banner }) {
    contentFocusRequester.requestFocus()
  }
}

// StreamTvActionScreen.kt — Search, Setting, Profile
LaunchedEffect(contentFocusRequester) {
  contentFocusRequester.requestFocus()
}
```

`contentFocusRequester` is attached to the opening section, so "request focus" always meant "focus
the banner". That produced two behaviours viewers reported as bugs:

1. **Returning from playback lost the viewer's place.** The players are siblings of `MainRoute` in
   the outer graph (see [nested main navigation](2026-09-01-nested-main-navigation.md)), so opening
   one disposes the whole shell. Popping back builds a new `MainScreen`, a new `NavHostController`,
   and a new `HomeViewModel` — which reloads, so `uiState.sections` changes and the effect fires
   again. The row the viewer had been on kept its *selected item* (`ContentRowState` is saveable) but
   lost focus to the banner.
2. **Picking a top bar item threw focus out of the bar.** The bar keeps focus on the item that was
   clicked; the arriving destination immediately took it away, so a viewer scanning the bar had to
   press Up again after every selection.

## Decision

**A destination may claim focus only when the top bar is not holding it.**

The bar's focus state already exists in `MainScreen` (it drives the screen scrim), so it is threaded
down to Home as `isTopBarFocused` and read once, when the restore effect launches:

| Situation | Top bar focused? | Result |
|---|---|---|
| App opens on Home | no — nothing is focused yet | Home claims focus (the banner, as before) |
| Return from a player | no — the shell is brand new | Home claims focus, on the remembered section |
| Top bar item picked | yes — the item keeps it | destination leaves focus alone |

Two supporting pieces make the middle row work:

- **`HomeFocusState`** — a saveable holder for the focused section index. `rememberSaveable` is what
  survives the shell being disposed, the same mechanism `ContentRowState` already relies on for the
  selected item. The two together restore the exact card the viewer opened.
- **One `FocusRequester` per section**, attached to that section's own single focus target (the
  banner's overlay box, the row's fixed selection overlay). `contentFocusRequester` stays where it
  was — on the opening section — because it means something different: it is where the bar's Down key
  lands. Restoring focus scrolls the target section into view first, since a section outside the
  viewport is never composed and a `FocusRequester` with no laid-out node cannot take focus.

`StreamTvActionScreen` simply stopped requesting focus; Down from the bar still reaches its button.

## Consequences

- Adding a destination means deciding whether it claims focus, and it gets `isTopBarFocused` if so.
  Silence is now the safe default rather than the broken one.
- Home's restore is section-granular. A future section type has to expose a `FocusRequester` that
  reaches its real focus target, or it cannot be restored into.
- `isTopBarFocused` is deliberately read inside the effect body rather than used as its key, so focus
  moving off the bar does not re-trigger a claim.
- Covered by `HomeContentTest.focusReturnsToTheRowThatHadItAfterHomeIsRebuilt` (via
  `StateRestorationTester`, which exercises the same saveable path a shell rebuild does),
  `HomeContentTest.contentLeavesFocusAloneWhileTheTopBarHoldsIt`, and
  `MainScreenTest.searchItemNavigatesWithoutTakingFocusOffTheTopBar`.

## Alternatives considered

**Restore with `Modifier.focusRestorer()`.** Restores the focused child of a focus group, but its
state is `remember`-scoped: it does not survive the composition being disposed, which is the only
case that matters here.

**Track "has Home ever handed over focus" instead of the bar's state.** A flag would have to live
somewhere that outlives the shell, and it still could not tell a return-from-playback (should claim)
from a top-bar selection (must not). The bar's focus state distinguishes them directly.

**Route the bar's Down key through the remembered section too.** Tempting, but Up only reaches the
bar from the opening section, so Down landing there is symmetric. Left as is.
