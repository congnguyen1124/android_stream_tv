# StreamTV Engineering Guidelines

## Product language

- All user-facing content, preview fixtures, dummy data, accessibility labels, and UI copy must be written in English.

## Compose TV UI

- Every new or materially changed UI composable must include a deterministic `@Preview` using an appropriate TV device. Preview code must not require a real player, network access, or runtime-only dependency.
- Keep focus navigation explicit and deterministic. Park focus before replacing or animating a focused subtree, disable focus for retained hidden layers, and restore the previously selected row when a child section closes.
- Preserve Back and D-pad exit behavior from the reference `ottclouds-android` player. A section transition must finish before its first focus request is issued.
- Share player section content between landscape and portrait screens. Keep orientation-specific framing at the screen boundary: landscape uses a rounded dark translucent panel; portrait sections remain transparent over the ambient background.
- The portrait player surface uses an inset white focus border. The border softens after focus is held and restarts its animation after a center press.
- Long comments and replies must expose a D-pad scroll viewport with a visible focus-aware scrollbar. Up and Down scroll content until a boundary, then release the key so focus can move.
- Reuse StreamTV design tokens and icons. Do not introduce OttClouds names or dependencies into this project.

## Architecture

- Keep the app as a single Gradle module while preserving feature, domain, data, and core package boundaries.
- ViewModels call repositories directly and expose coroutine-backed immutable UI state. Do not add a use-case layer unless a future requirement explicitly needs reusable domain orchestration.
- Use Hilt for dependency injection.

## Validation

- Run the narrowest relevant Kotlin compile and tests after changes.
- Do not run Spotless or Detekt unless the user explicitly requests them.
- Do not commit or push changes unless the user explicitly requests it for the current task.
