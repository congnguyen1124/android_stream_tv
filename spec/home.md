# Home screen specification

Read [the shared StreamTV specification](README.md) first. This document defines the observable Home
screen, independent of UI framework.

## Purpose

Home is a vertically scrolling feed of heterogeneous editorial sections. The first section is the
primary discovery surface; every later section exposes one fixed focus target while its items move
under that target. Opening content routes to a landscape or portrait player according to the content
type, not according to the section that happened to contain it.

## Content contract

Every content item has a stable identifier and these common values:

- playback URL;
- optional trailer URL;
- thumbnail URL;
- optional advertising URL;
- title;
- description;
- optional age restriction;
- optional logo URL.

Supported content types:

| Type | Additional rule | Player |
|---|---|---|
| Video | Standalone landscape program | Landscape |
| Series | Contains at least one Video episode | Landscape |
| Channel | Represents a live stream | Landscape |
| Short | Portrait program with 2:3 artwork | Portrait |

Home receives an ordered list of sections. A section has a stable identifier, an English title, a
view type, and at least one compatible item. Reject a section containing an incompatible type.

| View type | Allowed content | Visual form |
|---|---|---|
| Banner | Video | Full-width hero carousel |
| Vertical Banner | Short | Centered portrait carousel over an ambient background |
| Videos | Video | Standard 16:9 row |
| Videos Popular | Video | Ranked 16:9 row |
| Series | Series | Standard 16:9 row |
| Channels | Channel | Standard 16:9 row with live labeling |
| Shorts | Short | Standard 2:3 row |
| Shorts Popular | Short | Ranked 2:3 row |

## Screen composition

- Render sections in their server-provided order in one vertical lazy feed.
- Use 34 logical units between sections and 54 units of bottom breathing room.
- Section headings use white title text aligned to the 48-unit horizontal safe edge.
- If the first section is either banner type, it starts at the top edge behind the top bar. Otherwise
  the feed starts below the 80-unit top bar.
- A loading state centers “Loading your StreamTV home...”. An error state centers the repository
  error, falling back to “Unable to load Home content”.

## Landscape hero banner

### Layout

- The hero fills the viewport width and sits behind the top bar.
- Its height leaves 124 logical units of the next section visible. Clamp the resulting height between
  320 and 600 units. The visible next-section slice is required: it communicates that Home continues.
- Artwork crops to fill the hero.
- Apply a dark horizontal gradient from the left across roughly 68% of the banner and a dark vertical
  gradient rising from the bottom across roughly 58% of its height.
- Place the information block at the lower-left 48-unit safe edge with 30 units bottom padding. Its
  working width is about 470 units.

### Information hierarchy

Show, in order:

1. a small “STREAMTV FEATURED” badge and optional age badge;
2. title, up to two lines;
3. description, up to two lines;
4. a “Watch now” affordance;
5. one position dot per real item.

The “Watch now” affordance is visual content inside the banner's single focus target. It is not a
second focusable button. When the banner is focused, render it as a white surface with dark content;
otherwise use a subtle translucent surface.

### Paging and trailer behavior

- The entire hero owns exactly one focus target.
- Left and Right move one item. Center opens the active Video in the landscape player.
- Up returns to the Home item in the top bar. Down is released to the next Home section.
- Left at the first real item and Right at the last real item do not move past the real range.
- When more than two items exist, use duplicated edge items internally so automatic paging can wrap
  without a visible empty frame. Internal duplicates must never affect dots or item identity.
- While the hero is not focused, automatically advance every 5 seconds. Pause automatic paging
  during user scrolling, while focused, while the app is not resumed, or when only one item exists.
- Cross-fade artwork when the active item changes.
- When a focused item remains unchanged for 5 seconds and has a playable trailer, start the trailer
  from its beginning with no controls, subtitles, or focus target. Crop it exactly like the thumbnail
  and fade it in over about 300 ms.
- Stop and reset the trailer when focus leaves, the active item changes, playback opens, the screen
  pauses, or Home is disposed. Keep the thumbnail beneath the video so missing or failed playback
  degrades cleanly.

## Vertical banner

- Reserve 80 units above its cards for the top bar. The carousel area is about 272 units high.
- Show five portrait positions when space permits. Reference card width is 164 units, ratio is 2:3,
  and spacing is 22 units.
- Center the active Short. Scale it to approximately 1.10 and scale neighboring items to 0.94.
- A focused active item has a 3-unit white border; inactive cards retain only a subtle 1-unit border.
- Generate an ambient 16:9 backdrop from the active thumbnail's dominant color. Place it behind the
  cards and blend it into the app background with left/right and bottom gradients. Loading a palette
  must never delay focus or paging.
- The whole carousel owns one focus target. Left and Right change the active Short; Center opens the
  portrait player. Up and Down leave the section.
- Five or more items behave as an effectively infinite carousel with preloaded neighboring pages.
  Fewer than five form a finite carousel and stop at their ends.
- Start at the middle real item. Automatically advance every 5 seconds only when looping is enabled
  and the carousel is not focused.

## Fixed-selection content rows

Every standard or ranked row uses the same fixed-selection interaction:

- The row owns exactly one focus target: a transparent selection frame at the leading 48-unit safe
  edge. Individual cards are never focusable.
- The selected card sits inside the frame with 2 units of air between artwork and border.
- Left and Right animate the content beneath the stationary frame over roughly 190 ms.
- Compose or preload at least one item beyond each visible edge and the next possible target before
  movement begins. A card must already exist when it scrolls into view.
- At item zero, Left is released so focus may leave the row. Once the user has moved right, Left
  returns through earlier items normally.
- For non-ranked rows with more than five real items, append a duplicate cycle outside the viewport.
  Moving right from the final item must look continuous and then normalize selection to item zero.
- Rows with five or fewer items are finite. Right at the final item leaves selection on that item.
- Ranked rows are always finite regardless of size.
- Center opens the selected content. Ignore repeated direction presses while movement is animating.
- Save each row's real selected index independently so player return restores the same item.

### Cards

| Style | Width | Artwork | Detail area |
|---|---:|---:|---:|
| Video, Series, Channel | 272 | 16:9 | 52 high |
| Short | 152 | 2:3 | 70 high |

- Use 18 units between normal cards and 32 units between ranked cards.
- Crop thumbnails. Never alter thumbnail tint or opacity because selection changes.
- Show a lower-left badge: “VIDEO”, “SHORT”, “LIVE”, or “N EPISODES”. Live uses a red badge.
- Show the optional age restriction in the upper-right.
- Title is one line. Description is one line for landscape cards and up to two lines for Shorts.
- The selected title is white; unselected title is a brighter gray. Description remains secondary.
- The focus border is white, 3 units, and follows the artwork corner radius.
- Ranked rows show supplied artwork for ranks 1–9 at the upper-left. The rank artwork deliberately
  extends left and above the thumbnail and must not be clipped by the card or row viewport.

## Vertical focus and restoration

- On cold launch, focus the first section if the top bar does not own focus.
- Track the section that most recently contained focus. Save this section index separately from each
  row's selected item.
- Returning from a player first scrolls the remembered section into the viewport, then restores its
  single focus target. A restored section below the hero must clear the top bar rather than land
  underneath it.
- If refreshed data contains fewer sections, clamp the remembered section to the new last section.
- While the opening banner section owns focus, Home does not request an additional top-bar gradient.
  Once focus moves to a lower row, enable the top-bar readability gradient. Disable it when Home is
  disposed.

## Navigation outcomes

- Video, Series, and Channel open landscape playback.
- Short opens portrait playback, even when presented by a section whose name does not mention
  portrait orientation.
- Back from playback rebuilds the browsing shell and restores the last focused Home section and its
  selected real item.

## Acceptance scenarios

- The opening hero and the top of the next section are visible simultaneously at 720p and 1080p.
- A six-item row can move from item six to item one without revealing an empty trailing edge.
- A five-item row stops at item five.
- Any ranked row stops at its final rank and its number artwork remains unclipped.
- Returning from a player restores the exact section and card used to open it.
- Switching to Home from the top bar leaves focus on the Home top-bar item until Down is pressed.
