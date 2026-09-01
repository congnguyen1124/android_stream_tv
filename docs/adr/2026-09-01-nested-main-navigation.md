# ADR — Nested navigation: a `MainScreen` shell around the browsing destinations

- **Date:** 2026-09-01
- **Status:** Accepted

## Context

The app started with a single `NavHost` holding every destination — Home, Search, Setting, Profile
and both players — and drew `StreamTvTopBar` above that host in `StreamTvApp`.

Because the bar was drawn above *every* destination, playback had to opt out of it explicitly. The
shell asked `isPlayerRoute(currentRoute)` and returned early from its `Box` before laying the bar
out:

```kotlin
val isPlayerVisible = isPlayerRoute(currentRoute)
// ...
if (isPlayerVisible) return@Box
StreamTvTopBar(/* ... */)
```

That put three costs on the codebase:

1. **The shell knew about the player.** `StreamTvApp` imported a player-feature helper purely to
   decide not to draw a bar, and `PlayerNavigation.kt` exported `isPlayerRoute` for no other reason.
2. **Every new full-screen destination had to be added to that predicate**, or it silently inherited
   a top bar it did not want.
3. **The condition was a string prefix match.** `isPlayerRoute` compared route strings against
   `"player"` / `"verticalPlayer"`, so a future destination whose route happened to start with those
   characters would have lost its top bar.

## Decision

Split navigation into two graphs.

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

`MainScreen` owns the top bar, the screen dim that appears while the bar has focus, the two
`FocusRequester`s that move focus between bar and content, and the inner `NavHost`. The players are
siblings of `MainScreen` in the outer graph.

**Structure now expresses the rule that a predicate used to encode.** A destination gets a top bar
if and only if it is registered in `MainNavHost`; nothing has to ask what it is. `isPlayerRoute` was
deleted along with the shell's import of it.

Consequences worth naming:

- **Back behaves as expected without extra code.** The inner host's back handler is enabled only
  while its own back stack is non-empty, so Back inside the shell pops the inner stack and Back on
  the inner start destination falls through to the outer one.
- **Playback navigation is now a callback, not a `NavController` reach-through.** `MainNavHost`
  cannot see the outer controller, so opening a player travels out through
  `onOpenPlayer` / `onOpenVerticalPlayer`, which `StreamTvNavHost` binds to `navigateToPlayer` and
  `navigateToVerticalPlayer`. Which of the two a home item opens still comes from
  `HomeContentUiItem.playerTarget()`, unchanged.
- **The inner back stack survives a trip to a player.** `MainNavHost`'s controller comes from
  `rememberNavController()`, which saves through `rememberSaveable`, so returning from playback
  restores the destination the viewer left.

## Top bar overlay

`StreamTvTopBar` gained a scrim of its own: a vertical gradient from `colorScheme.surface` at the
top to transparent at the bottom, painted behind the items and animated in and out.

It is **driven by the visible destination, not by the top bar**. A screen calls
`onTopBarOverlayVisibilityChange(true)` when whatever sits under the bar would swallow the icons.
Home raises it once focus moves past the opening section: the first section is a full-bleed banner
that runs behind the bar and already paints its own gradients, so a second scrim there would only
darken artwork that is already legible; the rows below it scroll under the bar and do need one.

`MainScreen` lowers the overlay on every destination change, because the screen that raised it has
already left composition by the time the next one composes and cannot lower it itself.

The gradient uses three stops (`0.62` alpha → `0.24` at the halfway point → transparent) over
`StreamTvDimensions.TopBarOverlayHeight` = 168 dp, which is taller than the 80 dp bar on purpose: a
scrim that stops at the bar's own edge ends on a visible line instead of dissolving into the content.

## Composable naming

Each browsing feature previously had two composables — `XxxRoute` (ViewModel binding) and `XxxScreen`
(UI) — in two files. The pair is now a single `XxxScreen` composable per feature, still living in
`XxxRoute.kt`:

| Feature | Before | After |
|---|---|---|
| Home | `HomeRoute.kt` → `HomeRoute`, `HomeScreen.kt` → `HomeScreen` | `HomeRoute.kt` → `HomeScreen`, `component/HomeContent.kt` → `HomeContent` |
| Search | `SearchRoute.kt` → `SearchRoute`, `SearchScreen.kt` → `SearchScreen` | `SearchRoute.kt` → `SearchScreen` |
| Setting | `SettingRoute.kt` → `SettingRoute`, `SettingScreen.kt` → `SettingScreen` | `SettingRoute.kt` → `SettingScreen` |
| Profile | `ProfileRoute.kt` → `ProfileRoute`, `ProfileScreen.kt` → `ProfileScreen` | `ProfileRoute.kt` → `ProfileScreen` |

Search, Setting and Profile render one `StreamTvActionScreen` call each, so the stateless half had
nothing left to hold and was folded into `XxxScreen`. Home's UI is substantial and stays hoisted as
the stateless `HomeContent`, which is what the Compose tests drive.

The player keeps `PlayerRoute` / `VerticalPlayerRoute` bound to `component/PlayerScreen` and
`component/VerticalPlayerScreen`. Those two screens differ only in framing and share one ViewModel;
the split there separates portrait from landscape rather than state from UI, so collapsing it would
lose a real distinction.

## Alternatives considered

**Keep one graph and gate the bar on a destination flag.** Replacing the route-prefix check with,
say, a `NavDestination` argument saying "full screen" would have fixed the string-matching fragility
but not the other two costs: the shell would still draw a bar for every destination and then take it
away, and every new full-screen destination would still have to remember to set the flag.

**Use `navigation()` sub-graphs in one `NavHost`.** A nested graph groups the browsing destinations
but does not give them a shared parent composable, so the top bar would still be drawn by the outer
shell and still need to know when to withdraw. Only a nested `NavHost` puts the bar and its
destinations inside the same composable.

**A `CompositionLocal` for the overlay condition.** This would spare `homeScreen()` a parameter, at
the cost of an invisible dependency: a screen's effect on the shell would not appear in its
signature, and the Compose test could not assert the condition without a provider. The explicit
`onTopBarOverlayVisibilityChange` callback is threaded only to the destination that uses it.

## Verification

```bash
./gradlew compileDebugKotlin compileDebugUnitTestKotlin compileDebugAndroidTestKotlin
./gradlew testDebugUnitTest
./gradlew spotlessCheck detekt
```

Instrumented coverage lives in `MainScreenTest` (top bar focus, screen dim, overlay appearing once
focus leaves the banner, cross-destination navigation) and `HomeContentTest` (initial banner focus,
and the overlay callback emitting `false` then `true` as focus leaves the opening section).
