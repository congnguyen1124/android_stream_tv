# Calendar screen specification

Read [the shared StreamTV specification](README.md) first. This document defines the 24-hour channel
calendar and its single-focus, two-dimensional program grid.

## Purpose

Calendar is a compact electronic program guide. Channels run horizontally, time runs vertically,
and each program's height is proportional to its duration. The viewer controls the entire grid
through one moving selection frame; program cards and headers never become independent focus nodes.

## Data contract

A calendar response represents one local date and an ordered list of channels.

Each channel contains:

- stable channel identifier;
- English display name;
- optional logo URL;
- ordered or unordered program list.

Each program contains:

- stable identifier within its channel;
- English title and description;
- optional thumbnail URL;
- start minute and end minute relative to the day's midnight.

Rules:

- Start minute is non-negative and end minute is strictly greater than start minute.
- The visible day spans minute 0 through minute 1440.
- Filter programs that do not overlap the visible day. Clamp partially overlapping programs to the
  day boundary for layout.
- Sort each channel by start minute, then end minute.
- Empty channels remain visible in the header row and are skipped by program navigation.
- Format program time as 24-hour `HH:mm – HH:mm`; minute 1440 displays as `24:00`.
- Format the leading date as an uppercase short weekday plus `DD MMM`, for example `WED` and
  `02 SEP` on separate lines.

## Screen composition

- Calendar starts 2 units below the 80-unit top bar and fills the remaining viewport.
- Use only 6 units of horizontal and bottom padding so the guide occupies nearly the full width.
- Do not show a page title such as “Program guide”. The guide itself is the screen content.
- Clip the complete guide to a compact 10-unit radius.
- The fixed leading column is 76 units wide. The fixed channel header row is 64 units high.
- Reference channel width is 214 units with 8 units between channels.
- One hour occupies 76 vertical units. Program height equals duration in hours multiplied by this
  value, minus the small inter-program spacing.
- Reference spacing between vertically adjacent programs is 3 units.

## Headers and grid

### Leading date header

- Center the two-line date in the leading header.
- Use small blue bold weekday text above slightly larger white bold date text.
- Draw a subtle right divider and bottom divider. Keep the header background transparent.

### Channel headers

- Center a 28-unit circular channel image above its one-line name.
- Crop a valid logo to the circle. While a logo is missing or loading, show deterministic colored
  fallback artwork with up to two initials from the channel name.
- Use white medium-weight name text and ellipsize overflow.
- Header backgrounds remain transparent. Draw subtle vertical column dividers and a bottom divider
  so the data grid remains legible.
- Channel headers move horizontally in exact sync with program columns and never move vertically.

### Time ruler

- Show only the two-digit hour, `00` through `24`; omit minutes and punctuation.
- Align labels immediately left of the grid with 14 units right padding.
- Draw a continuous vertical divider at the grid edge and a short tick at every hour.
- The time ruler moves vertically in exact sync with programs and never moves horizontally.

### Program grid

- Draw subtle vertical channel lines and horizontal hourly lines over a very light translucent
  surface. Lines, headers, and programs must remain aligned throughout movement.
- A program card has 2 units horizontal inset, 1.5 units vertical inset, a 6-unit radius, and a dark
  blue surface.
- Programs lasting at least 60 minutes show artwork when a thumbnail URL exists. Crop artwork to fill
  and add a top-to-bottom dark gradient so lower text remains readable.
- Programs shorter than 60 minutes show no artwork and only a one-line title.
- Programs lasting 60 minutes or more show up to two title lines followed by their formatted time.
- The selected program title is white and bold. Other titles use near-white medium weight.
- Program description belongs to the data contract for future details but is not shown in the grid.

## Lazy rendering and continuity

- Lazily compose in both horizontal and vertical axes.
- Preload at least one column beyond each horizontal edge and two hours beyond each vertical edge.
- Always precompose the selected program, the pending destination, and every possible immediate
  D-pad target, even when they lie outside normal overscan.
- A movement must never arrive at an unloaded blank card. The destination content is present before
  the 190 ms transition starts.
- Headers, time ruler, grid lines, cards, and selection frame share the same offsets. Visible drift
  between these layers is a parity failure.

## Single-focus selection model

- The entire calendar owns exactly one focusable node: a transparent selection frame over the active
  program.
- Program cards themselves are not focusable.
- Add 2 units between card and selection frame. Use a 3-unit white border while focused and a subtle
  1-unit translucent border otherwise.
- During movement, animate horizontal offset, vertical offset, and selection bounds together over
  about 190 ms. Treat the destination as selected during the animation so its content styling is
  already correct.
- Ignore further direction commands until the current transition finishes.
- Save the selected channel and program indices. Normalize them when refreshed data removes a
  channel or program. If the selected channel is empty, choose the first program in the first
  non-empty channel.

### Vertical movement

- Up selects the previous program in the same channel.
- Down selects the next program in the same channel.
- At the first program, Up is released so focus can reach the top bar.
- At the final program, Down is released so focus may leave the calendar if another destination is
  available.

### Horizontal movement

- Left and Right search in that direction for the next non-empty channel, skipping any number of
  empty channels.
- In the target channel, first prefer a program whose interval contains the midpoint of the current
  program.
- If no interval contains that midpoint, choose the program with the smallest distance from it.
  Break equal-distance ties by the closest program midpoint.
- This means moving right from a late-night program in column one reaches the corresponding
  late-night program in the next non-empty column, even when schedules have different boundaries.
- At the first non-empty channel, Left is released. At the last non-empty channel, Right is released.

### Viewport boundaries

- While space remains beyond the active program, keep the active selection at the guide's leading
  and top edges and move content beneath it.
- When the content reaches its final horizontal or vertical extent, stop scrolling and move the
  selection frame toward the last visible program instead.
- Never scroll beyond the first or final channel or beyond minute 0 or 1440.
- The last program of every column must remain selectable at its real height; it must not be clipped
  to preserve a stationary selector.

## Focus ownership and top bar

- Calendar requests the top-bar readability gradient for its entire visible lifetime and clears that
  request when disposed.
- Selecting Calendar from the top bar leaves focus on the Calendar top-bar item.
- Down from the Calendar top-bar item enters the selected program. On first load this is the first
  program of the first non-empty channel.
- On cold entry or player return, after schedule layout completes, Calendar may claim its selected
  program only if the top bar does not own focus.
- Up at the first program returns to the Calendar top-bar item.
- Center/Enter is consumed by the selection frame. The current product has no program-details or
  playback action attached yet; a future action must preserve the same selected position on return.

## Loading, error, and empty states

- Loading centers “Loading program guide…” below the top bar.
- Repository failure shows its English message or “Unable to load the program guide”.
- A valid response with no selectable programs shows “No programs are available for this day”.
- Replacing a populated grid with an empty or error state must move focus to a stable destination
  before removing the selection frame.

## Acceptance scenarios

- A 30-minute program is half the height of a 60-minute program and shows title only.
- A 120-minute program is twice the height of a 60-minute program and may show artwork, title, and
  time.
- Right navigation skips an empty channel and lands on the program closest to the current time.
- Reaching the bottom of a channel moves the selector to the final program without scrolling past
  24:00.
- Headers, hour ticks, cards, and focus frame remain aligned during diagonal traversal across the
  schedule.
- The guide fills the screen below the top bar with only 6 units of outer horizontal padding.
