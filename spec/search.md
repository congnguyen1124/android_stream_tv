# Search screen specification

Read [the shared StreamTV specification](README.md) first. This document defines Search as a TV-first
screen with a virtual keyboard, caret-aware query editing, suggestions, and fixed-selection result
rows.

## Purpose

Search must be fully usable with a television D-pad and no hardware keyboard. Query editing remains
visible at all times. The virtual keyboard may collapse after submission, but the latest results stay
on screen and the viewer can reopen the keyboard without losing them.

## Data and state contract

Search has one immutable state containing:

- query text and caret position;
- whether the virtual keyboard is visible;
- initial-loading and active-search flags;
- last successfully submitted query;
- up to six suggestions;
- ordered result sections;
- optional English error message.

Result content contains a stable identifier, playback URL, thumbnail URL, title, description,
optional age restriction, and one of two types: Video or Short. Video routes to landscape playback;
Short routes to portrait playback.

The current dummy catalog is derived from Home's unique Video and Short items. Duplicate identifiers
are removed. Recommendations show the complete catalog before any query is submitted.

### Matching behavior

- Normalize the submitted query by trimming outer whitespace, converting it for case-insensitive
  comparison, and splitting it into non-empty whitespace-separated tokens.
- An item matches only when every token occurs in its title or description.
- Preserve separate Videos and Shorts sections and omit an empty section.
- The dummy implementation must still demonstrate a populated layout for an arbitrary unmatched
  query. Use a deterministic query-based rotation of each catalog section as fallback, so submitting
  the same unmatched query always yields the same order.
- Suggestion candidates include recent searches, curated searches, and catalog titles. Remove
  duplicates, filter case-insensitively by the current query, preserve candidate order, and show at
  most six.
- Cache the dummy catalog after its first load. Cancel stale suggestion and search requests before
  starting newer ones.

## Screen composition

- Use the near-black app background.
- Start content 12 units below the 80-unit top bar.
- Use 48 units left padding, 24 units right padding, and 18 units bottom padding.
- Use a two-column editor workspace. The left column contains the persistent query surface followed
  by suggestions; the keyboard occupies the right column and aligns with the top of the query field.
  Keep enough viewport below both columns to reveal the heading and complete first result row on a
  720-unit-high TV viewport.
- Results occupy the remaining height in a vertically lazy list. Use 18 units between result blocks.

## Query surface and caret

- The query surface is a compact 500×48 reference pill with a fully rounded shape and a leading
  Search icon.
- Empty text shows “Search movies, series, channels and shorts”.
- The field is a display controlled by the virtual keyboard, not a platform text input that summons
  a system keyboard. It has no click action, cannot receive focus, and never draws a focus border.
- Store the caret as an integer from zero through query length. Clamp it before every edit.
- Inserting a character places it at the caret and moves the caret by the inserted text length.
- Backspace at position zero does nothing. Otherwise remove the character immediately before the
  caret and move left once.
- Clear sets query to empty and caret to zero.
- Caret Left and Right clamp at the beginning and end.
- Render query text as the substring before the caret, a 2-unit blue blinking caret, and the
  substring after it. The caret alternates between full and low opacity about every 620 ms.
- Horizontally scroll long text so an end-position caret remains visible.
- Keep the latest submitted results when the query changes or the keyboard reopens.

## Suggestions

- Suggestions occupy a 340-unit column below the query, with 2 units vertical spacing.
- Each suggestion is 24 units high and contains one ellipsized label plus a compact history icon.
- Keep the suggestion surface transparent in every state. Focus is communicated by a narrow blue
  leading rail, a blue circular icon background, and brighter text; it must not become a large
  full-width filled rectangle. Do not scale the row.
- Selecting a suggestion replaces the entire query, moves the caret to the end, and immediately
  performs the same submission flow as the Search key.

## Virtual keyboard

The keyboard occupies the right editor column. Its reference workspace is 210 units high, begins 24
units after the 500-unit left column, and may use up to 364 units of width. It aligns with the top of
the query surface rather than beginning below it. All six suggestions and every keyboard row must
fit inside the editor workspace without overlapping results.

- Character keys form a seven-column grid.
- Letter mode contains a through z.
- Symbol mode contains digits 0–9 and: period, comma, slash, colon, semicolon, hyphen, underscore,
  tilde, question mark, exclamation mark, equals, plus, ampersand, at sign, percent, hash, asterisk,
  and vertical bar.
- A right-side function column contains the “?123”/“ABC” mode switch, Shift, and Backspace.
- A bottom row contains Space, caret Left, caret Right, Clear, and Search.
- Shift changes letter case and has no effect on symbols. Keep its selected state visually visible.
- Derive key size from both available width and height, clamped to 24–60 units. Reference gap is 8
  units.
- Character keys use the darkest surface, function keys use a subtle translucent surface, and the
  Search key uses the blue primary surface.
- Follow the Downloader keyboard's button treatment: 10-unit corners, no added focus border, a blue
  focused fill with dark content, and focus scale fixed at exactly 1 so dense keys never overlap.

## Result sections

- Before submission, heading text is “Recommended for you”.
- After successful submission, heading text is `Search results for “query”`, preserving the trimmed
  submitted query.
- During initial load show “Loading recommendations…”. During a request show “Searching…”.
- On failure show the repository message or “Search is temporarily unavailable”. Retain previous
  results when available rather than replacing them with an empty frame.
- Render a Videos section and a Shorts section when each has content.

Every result section uses the shared fixed-selection row behavior described in
[Home](home.md#fixed-selection-content-rows): exactly one focus target, leading stationary frame,
190 ms movement, preloaded next content, finite behavior for five or fewer items, and continuous
looping for more than five.

Search-specific card sizing:

| Type | Width | Artwork ratio | Detail height |
|---|---:|---:|---:|
| Video | 190 | 16:9 | 46 |
| Short | 112 | 2:3 | 54 |

- Use 16 units between cards and 10 units between section heading and row. Suppress the shared
  ContentRow's additional horizontal inset so the fixed selector aligns with the section heading.
- The 3-unit white selection frame surrounds the thumbnail area, not the text block.
- The selected title becomes white; unselected titles remain medium gray.
- Selection does not modify thumbnail color or opacity.
- Show a lower-left “VIDEO” or “SHORT” badge and optional upper-right age restriction.
- Video descriptions use one line; Short descriptions may use two.

## Focus graph

The screen has stable focus targets for the first suggestion, first keyboard key, keyboard Search
key, every result-row selector, and an invisible parking target. The query display is deliberately
excluded from the focus graph.

- Down from the Search top-bar item enters the first suggestion when suggestions exist; otherwise it
  enters the first keyboard character.
- Right from a suggestion goes to the first keyboard character. Up from the first suggestion or the
  first keyboard character returns to the Search top-bar item.
- Down from the last visible suggestion or any key in the keyboard's bottom action row begins the
  keyboard-to-results transition when at least one result section exists. It does not move focus
  directly while the keyboard still occupies the layout.
- Up from the first result row reopens the keyboard and focuses its Search key. This gives the user
  one predictable return point instead of entering the keyboard at a spatially variable key.
- Up and Down between result rows move between their fixed selectors.
- Left and Right inside a result row move its content while its selector retains focus.
- At item zero, another Left press is cancelled and the result-row selector stays focused. Search
  has no valid destination to the left, and the invisible transition parking target is never an edge
  destination. Other screens may retain the shared ContentRow default that releases Left at item zero.

### Focus-safe transitions

- A blank or already-running submission is ignored and leaves focus unchanged.
- Before submission or suggestion selection collapses the keyboard, move focus to the parking target.
- Trim the query, move the caret to its end, hide the keyboard, and show active-search state.
- Keep focus parked through the complete 180 ms keyboard collapse. Reset the result list to its
  leading viewport only after the transition is idle, wait for the new layout frame, and then focus
  the first result-row selector. This prevents recommendation cards from appearing late or being
  clipped by the previous keyboard-sized viewport.
- When Up is pressed on the first result row, park focus, expand the keyboard, wait until the
  transition and layout are idle, and then focus the Search key. Reopening does not clear results.
- Back is handled by Search only while its content owns focus and the keyboard is visible. It parks
  focus and closes the keyboard. After layout, it focuses the first result row when results exist;
  otherwise it returns to the Search top-bar item. With the keyboard already hidden, Back is
  released to application navigation.
- Switching to Search from the top bar leaves focus on the top-bar Search item until Down is pressed.

## Navigation outcomes

- Center on a Video result opens landscape playback with URL, title, description, and age rating.
- Center on a Short result opens portrait playback with the same metadata.
- Returning from playback returns to the browsing shell without automatically reopening the virtual
  keyboard.

## Acceptance scenarios

- Query editing can insert and delete in the middle of text, not only at its end.
- Suggestions update after every character and never exceed six rows.
- Submitting an unmatched query still produces deterministic dummy Videos and Shorts.
- Focus never disappears during the keyboard's 180 ms expand/collapse animation.
- The query display never receives focus or click semantics.
- Down from a bottom keyboard control hides the keyboard before the first result row receives focus;
  Up from that first row restores the keyboard with focus on Search.
- The latest results remain visible when the keyboard is reopened.
- With the keyboard open, the first result row begins above 450 units and is fully readable on a
  720-unit-high viewport.
- Returning a result row to item zero and pressing Left again keeps that row focused.
- A six-item result row loops without an empty trailing edge; a five-item row stops at its end.
- Selecting a Short always opens portrait playback.
