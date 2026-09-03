# ADR — Where focus goes when one destination opens another

- **Date:** 2026-09-03
- **Status:** Accepted

## Context

Settings' account entries are gated behind sign-in, and their pane offers “Get started”. Sign-in
already exists as its own top-bar destination (Profile), so the action should open that destination
rather than grow a second pairing flow inside Settings.

That is the first time a destination inside `MainScreen` opens another one. Every earlier route
change came from the top bar, where
[shell focus ownership](2026-09-02-shell-focus-ownership.md) settles the question: the bar keeps
focus on the item that was selected, and the arriving destination does not steal it. A destination
opening a sibling has no such answer, and getting it wrong is not cosmetic — the focused control is
destroyed by the navigation, and focus left nowhere means a remote with no way to move.

Two candidates were implemented and tested on an `Android_TV_720p` emulator:

**Park focus on the top bar.** Matches what selecting the destination by hand does. It does not
work. The bar restores focus to its *selected* item via `focusProperties { onEnter }`, and during a
route change that is not yet the item being opened: a `FocusRequester` on the bar fired right after
`navigate` landed focus on the **Search** item — the bar's fallback when it sees no selection —
confirmed by dumping every node carrying `SemanticsProperties.Focused` after the jump. Waiting for
the new route to become current did not fix it either, because tearing down the old destination
already hands focus to the bar, and requesting focus on a group that already holds it is a no-op.
Clearing focus first to force re-entry only made the sequence more fragile.

**Hand focus to the arriving destination.** The shell's own rule already allows it: a destination
may claim initial focus when the top bar does not own it. Focus was in Settings' content when the
viewer pressed the action, so the top bar never owned it, and content is where focus belongs.

## Decision

`MainScreen` records the route it is opening, and once that route is current it waits one frame and
requests focus on `contentFocusRequester` — the shared requester every destination points at its own
entry target.

- The record is the route, not a boolean. Comparing it against the current route is what makes the
  request fire exactly once, after navigation lands.
- The frame wait is what makes the request legal: the target only exists once the arriving
  destination has composed.
- `contentFocusRequester` is already the top bar's Down target, so the arriving screen needs no new
  parameter and no knowledge of who opened it. “Get started” lands on Profile's sign-in action.
- Settings navigates through a callback the shell owns (`onOpenSignIn`); the feature knows nothing
  about routes or focus.

The removal of a focused control *within* a screen keeps the existing park-first idiom instead:
Settings' clearing action moves focus to the selected menu entry before flipping the state that
removes the button.

## Consequences

- One shell-level mechanism covers any future destination-to-destination hand-off. A second caller
  passes its own route.
- `MainScreenTest.settingSignInActionOpensProfileWithFocusOnItsSignInButton` is the guard: it drives
  the whole path with D-pad keys and asserts focus lands on Profile's sign-in action. It fails on
  every variant described above, which is how the top-bar approach was ruled out.
- The jump is visually identical to selecting Profile in the bar, except that focus is already in
  content — so the shell's dim overlay does not appear, correctly, since the bar has no focus.
- A destination that claims focus this way must have an entry target composed in the same frame as
  the destination. That holds for every current destination; one that loads before it can show a
  focusable control would need to publish its target later, and this mechanism would need to wait
  for it rather than for the route alone.
