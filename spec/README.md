# StreamTV cross-platform UI specifications

This directory is the framework-neutral product contract for recreating StreamTV on Android TV,
Flutter, another television platform, or a future UI stack. A port may use different widgets and
state-management tools, but it must preserve the visual hierarchy, focus ownership, D-pad outcomes,
loading behavior, and navigation described here.

Read this file before a feature specification:

- [Home](home.md)
- [Search](search.md)
- [Calendar](calendar.md)
- [Profile](profile.md)

## Interpretation

The words **must**, **should**, and **may** are intentional:

- **Must** defines observable product behavior required for parity.
- **Should** defines the preferred visual or interaction result when platform constraints differ.
- **May** allows an implementation choice that does not change observable behavior.

Dimensions are logical television design units. They are reference values for a 16:9 television
viewport and should scale consistently with the target platform's density system. Preserve ratios,
alignment, safe areas, and focus visibility before pursuing pixel identity on a different device.

All user-facing text, dummy content, labels, accessibility descriptions, and error messages must be
English.

## Shared visual language

- The app background is near-black with cool blue surfaces and restrained blue accents.
- Primary content is white. Secondary and inactive content uses progressively dimmer neutral gray.
- Focus is unmistakable: use a white border or a white filled surface with dark content.
- Dense TV layouts must not enlarge focused controls when enlargement would overlap adjacent items.
- Thumbnails retain their original appearance between selected and unselected states. Selection may
  brighten text and add a border; it must not recolor or darken the image.
- Rounded corners are compact rather than pill-shaped, except for the search field and circular
  profile/channel artwork.
- Motion is short and calm. Typical focus-driven movement lasts about 180–190 ms. Content changes
  should cross-fade rather than flash.

## Browsing shell and top bar

The shared top bar is 80 logical units high and overlays destination content.

- The StreamTV logo remains at the left safe edge.
- Search, Home, Calendar, Setting, and Profile form one right-aligned navigation group.
- A navigation item normally shows only its icon. When it receives focus, it expands horizontally
  over about 180 ms and reveals its English label.
- Profile remains an icon-only circular item even when focused.
- The selected destination remains visually selected when focus leaves the top bar.
- A top-bar readability layer, when requested by the current destination, fades over about 300 ms
  from the screen surface color at the top to transparent at the bottom.
- While any top-bar item owns focus, a light transparent surface overlay covers the destination
  beneath it. This makes navigation ownership obvious without hiding the underlying screen.

### Top-bar focus contract

- Entering the top bar restores focus to the selected destination. If no destination is selected,
  focus the first navigation item.
- Pressing Down from any top-bar destination transfers focus to that destination's declared entry
  target.
- Selecting a different destination leaves focus on the selected top-bar item. The new destination
  must not steal it during composition or data loading.
- A destination may claim initial focus only when the top bar does not own focus. This occurs on cold
  app launch and after returning from a full-screen player.
- Pressing Up from the destination's topmost boundary returns focus to the selected top-bar item.

## Shared D-pad contract

- Every screen must have one deterministic focus owner after each completed transition.
- Before removing, replacing, or collapsing a focused subtree, move focus to a stable parking target.
  Restore focus only after the destination target is laid out.
- Ignore additional directional actions while the current scroll animation is running. Holding a key
  must not skip content unpredictably.
- At an internal boundary, consume the key only when the component intentionally traps focus. When
  the user has reached a screen boundary, release the key so focus can move to the shell or another
  declared target.
- Center, Enter, and television select keys are equivalent.
- Restoring a screen must restore the meaningful selection, not merely its first focusable control.

## Shared asynchronous states

Each feature exposes one immutable screen state with explicit loading, content, and error branches.

- Loading replaces content with a quiet centered English message.
- An error stops loading and displays a human-readable English message.
- A canceled request must not publish an error or overwrite a newer request.
- Reloading cancels the previous in-flight request.
- Empty content has an intentional English empty state; it must never leave focus attached to a node
  that no longer exists.

## Port acceptance

A port is complete only when all of the following are true:

- Every D-pad path in the relevant feature specification reaches the same semantic target.
- Focus remains visible during scrolling, animation, loading completion, Back, and player return.
- Content types preserve their required aspect ratios and destination player orientation.
- Loading, error, empty, and populated states are represented.
- The screen remains usable at both 1280×720 and 1920×1080 television viewports.
- Repeated content identifiers are treated as data errors rather than silently producing unstable
  list identity.
