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
- Place the query surface first. When the keyboard is visible, place suggestions and keyboard below
  it, then keep enough viewport to reveal the heading and beginning of the first result row.
- Results occupy the remaining height in a vertically lazy list. Use 22 units between result blocks.

## Query surface and caret

- The query surface is a 720×54 reference pill with a 28-unit radius and a leading Search icon.
- Empty text shows “Search movies, series, channels and shorts”.
- Focus uses a 2-unit white border with no scale change.
- The field is a display controlled by the virtual keyboard, not a platform text input that summons
  a system keyboard.
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

- Suggestions occupy a 390-unit column below the query, with 4 units vertical spacing.
- Each suggestion is 38 units high, uses a compact rounded focus surface, a history icon, and one
  ellipsized text line.
- Focus changes the row from transparent gray content to a subtle translucent surface with white
  content. Do not scale the row.
- Selecting a suggestion replaces the entire query, moves the caret to the end, and immediately
  performs the same submission flow as the Search key.

## Virtual keyboard

The keyboard occupies the area to the right of suggestions. Its reference workspace is 276 units
high, begins 32 units after the suggestion column, and may use up to 610 units of width.

- Character keys form a seven-column grid.
- Letter mode contains a through z.
- Symbol mode contains digits 0–9 and: period, comma, slash, colon, semicolon, hyphen, underscore,
  tilde, question mark, exclamation mark, equals, plus, ampersand, at sign, percent, hash, asterisk,
  and vertical bar.
- A right-side function column contains the “?123”/“ABC” mode switch, Shift, and Backspace.
- A bottom row contains Space, caret Left, caret Right, Clear, and Search.
- Shift changes letter case and has no effect on symbols. Keep its selected state visually visible.
- Derive key size from both available width and height, clamped to 32–52 units. Reference gap is 7
  units.
- Character keys use the darkest surface, function keys use a subtle translucent surface, and the
  Search key uses the blue primary surface.
- Focus turns a key white with dark content and a 2-unit border. Keep focus scale at exactly 1 so
  dense keys never overlap.

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
| Video | 260 | 16:9 | 54 |
| Short | 142 | 2:3 | 64 |

- Use 16 units between cards and 10 units between section heading and row.
- The 3-unit white selection frame surrounds the thumbnail area, not the text block.
- The selected title becomes white; unselected titles remain medium gray.
- Selection does not modify thumbnail color or opacity.
- Show a lower-left “VIDEO” or “SHORT” badge and optional upper-right age restriction.
- Video descriptions use one line; Short descriptions may use two.

## Focus graph

The screen has stable focus targets for the query, first suggestion, first keyboard key, keyboard
Search key, every result-row selector, and an invisible parking target.

- Down from the Search top-bar item enters the query surface.
- Up from the query returns to the Search top-bar item.
- When the keyboard is visible, Down from query goes to the first suggestion if one exists;
  otherwise it goes to the first keyboard character.
- Right from a suggestion goes to the first keyboard character. Up from the first suggestion or the
  first keyboard character returns to query.
- Down from the last visible suggestion and from the keyboard Search key goes to the first result
  row when it exists.
- When the keyboard is hidden, Down from query goes directly to the first result row.
- Up from the first result row returns to the keyboard Search key while the keyboard is visible, or
  to query while it is hidden.
- Up and Down between result rows move between their fixed selectors.
- Left and Right inside a result row move its content while its selector retains focus.

### Focus-safe transitions

- A blank or already-running submission is ignored and leaves focus unchanged.
- Before submission or suggestion selection collapses the keyboard, move focus to the parking target.
- Trim the query, move the caret to its end, hide the keyboard, and show active-search state.
- After successful results are laid out, move focus from parking to the first result row.
- Selecting the query surface reopens the keyboard without clearing results and refreshes suggestions.
- Back is handled by Search only while its content owns focus and the keyboard is visible. It parks
  focus, closes the keyboard, then restores focus to query after layout. With the keyboard already
  hidden, Back is released to application navigation.
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
- The latest results remain visible when the keyboard is reopened.
- A six-item result row loops without an empty trailing edge; a five-item row stops at its end.
- Selecting a Short always opens portrait playback.
