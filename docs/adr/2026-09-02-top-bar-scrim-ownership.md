# ADR — Who raises and lowers the top bar scrim

- **Date:** 2026-09-02
- **Status:** Accepted

## Context

`StreamTvTopBar` paints a scrim behind its items so they stay readable over whatever sits under the
bar. It runs the bar's own height, from solid `surface` at the top edge to fully transparent at the
bottom, and fades in and out rather than snapping.

It is not always wanted. Home's opening section is a full-bleed banner that already paints its own
gradients, so a second scrim over it is just a dark band. The rule the scrim implements is therefore
positional: **hidden while the opening section holds focus, raised once focus moves down onto the
rows underneath.**

Only the destination knows where focus is, but the bar lives in the shell (`MainScreen`), which
outlives every destination. So Home reports its state upwards and the shell owns the flag:

```kotlin
// HomeContent.kt
val isTopBarOverlayVisible = focusState.focusedSectionIndex > HomeContentDefaults.FirstSectionIndex

LaunchedEffect(isTopBarOverlayVisible) {
  onTopBarOverlayVisibilityChange(isTopBarOverlayVisible)
}
```

That left the shell needing to lower the scrim when the destination that raised it went away, and it
did so off the current route:

```kotlin
// MainRoute.kt — removed by this decision
LaunchedEffect(currentRoute) {
  isTopBarOverlayVisible = false
}
```

**This raced, and lost.** The players are siblings of `MainRoute` in the outer graph (see
[nested main navigation](2026-09-01-nested-main-navigation.md)), so returning from one rebuilds the
whole shell. On that first composition `currentBackStackEntryAsState()` has not resolved yet, so
`currentRoute` is `null` and settles to the home route a frame later. The sequence:

1. Home composes, restores `focusedSectionIndex` to the row the viewer opened the video from, and
   reports `true`.
2. The route resolves, `LaunchedEffect(currentRoute)` restarts, and sets the flag back to `false`.
3. Home's effect never re-runs — its key, `isTopBarOverlayVisible`, never changed.

The scrim stayed down over scrolled rows. On an Android TV 720p emulator, returning from a video
opened out of a content row put the bar's logo and icons directly on top of full-brightness card
artwork, with the card titles legible straight through where the scrim should have been.

## Decision

**The destination that raises the scrim lowers it on the way out.** The shell no longer resets the
flag on route change.

```kotlin
// HomeContent.kt
DisposableEffect(Unit) {
  onDispose { onTopBarOverlayVisibilityChange(false) }
}
```

Teardown is now tied to the thing whose state the flag actually describes, rather than to a route
value that updates on its own schedule. There is nothing left to order against: the shell's flag
starts `false`, and the only writes come from the destination.

The raise stays a separate `LaunchedEffect` keyed on the value. Folding both into one
`DisposableEffect(isTopBarOverlayVisible)` would work visually, but every key change would emit a
spurious `false` before the new value, which is noise for anything observing the callback.

## Consequences

- A destination that raises the scrim must also lower it on dispose. Not raising it at all — which is
  what Search, Setting, and Profile do today — needs no code, and stays the safe default.
- Wiring a second destination to the scrim will need the exit-transition overlap looked at: during a
  navigation both destinations are briefly composed, and the outgoing one's `onDispose` runs after
  the incoming one has composed. Today only Home raises the scrim, so a late `false` clobbers
  nothing.
- Covered by `HomeContentTest.topBarOverlayIsLoweredWhenHomeLeavesTheComposition`, alongside the
  existing `topBarOverlayStaysHiddenWhileTheOpeningSectionIsFocused` and
  `topBarOverlayIsRaisedOnceFocusLeavesTheOpeningSection`.

## Alternatives considered

**Key the shell's reset on the destination rather than the route.** Same race with a different
trigger — anything the shell derives from navigation state can settle after the destination has
already reported.

**Have the shell hold scrim state per route instead of one flag.** Removes the clobber, but makes the
shell carry a map keyed by destinations it otherwise knows nothing about, to solve a problem the
destination can answer for itself in three lines.

**Let `StreamTvTopBar` read focus position directly.** The bar would have to know what a "section"
is and how deep the focused one is, which is Home's concept, not the design system's.
