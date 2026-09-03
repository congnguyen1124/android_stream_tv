# Landscape player screen specification

Read [the shared StreamTV specification](README.md) first. This document defines the observable
landscape player, independent of UI framework. The portrait player is specified separately in
[vertical-player.md](vertical-player.md); everything both screens share is stated here and referenced
from there.

## Purpose

Full-screen playback for content shot landscape: videos, series episodes and live channels. The video
fills the panel and every affordance is transient — the screen shows nothing but the picture until the
viewer asks for controls, and returns to nothing but the picture when they stop.

## Data and state contract

The screen renders one immutable snapshot per frame. It must expose:

| Value | Meaning |
|---|---|
| `title` | Content title. |
| `collectionTitle`, `releaseYear` | Optional secondary line, joined with a bullet separator when both are present. |
| `isPlaying` | Frames are advancing. Distinct from a request to play. |
| `isBuffering` | Loading, with nothing renderable yet. |
| `position`, `duration`, `bufferedPosition` | Playback progress. `duration` of zero means the stream is live. |
| `isSeekable` | Derived: `duration` is greater than zero. |
| `isLiked`, `isSaved` | Viewer action state, reflected in the action icons. |
| `settings.isAvailable` | Whether any quality, subtitle or audio option exists for this stream. |
| `seekPreview` | Optional ordered frame stills for scrubbing. |
| `error` | Typed failure with a message and whether a retry is worth offering. |

A live stream must be identified by absent duration, not by a separate flag. The screen must not
compute or cache playback values of its own.

## Screen composition

Four layers, back to front:

1. **Video surface** — fills the panel, letterboxed to preserve the source aspect ratio.
2. **Input target** — a full-screen focus holder that owns the D-pad while no chrome is shown.
3. **Controller** — the transient chrome, described below.
4. **Side section** — metadata, comments or settings, in a panel on the trailing edge.

An error replaces layers 3 and 4 entirely.

### Controller

The controller occupies the top and bottom edges and leaves the middle of the picture clear.

- A vertical scrim darkens both edges and stays fully transparent across the middle band, so the
  chrome is legible over any frame without dimming the content the viewer is watching.
- **Title block**, upper leading corner: title on up to two lines, then the optional secondary line.
  Not focusable. On a live stream a `LIVE` badge precedes the title.
- **Seek bar**, above the control row: elapsed time on the leading edge and total duration on the
  trailing edge, both **above** the track; then the track itself with a played portion, a buffered
  portion behind it, and a thumb at the playhead. The thumb grows while the bar holds focus.
- **Control row**, along the bottom edge, in three clusters:

| Cluster | Alignment | Contents |
|---|---|---|
| Leading | Leading edge | `Description` pill |
| Transport | **Centred on the panel** | Rewind, play/pause, forward |
| Trailing | Trailing edge | Like, save, comment on one shared pill; settings on its own circle |

The transport cluster must be centred on the panel, not on the space between the other two clusters:
its position must not move when the trailing cluster gains or loses the settings control.

On a live stream the seek bar is replaced by a single elapsed-time label, and rewind and forward are
absent.

### Control appearance

- A control is a circle. The play/pause control is larger than the others, marking it as the primary.
- **Unfocused**: translucent light fill, white glyph. A control inside the trailing pill draws no fill
  of its own, so only the shared pill is visible.
- **Focused**: opaque white fill, dark glyph, and a caption naming the control directly beneath it.
  Focus must not be signalled by scaling — a control on the shared pill would grow out of it.
- The play/pause control shows no caption; its glyph already states what it does. Every control must
  still carry an accessibility name whether or not it shows a caption.
- The seek bar itself shows no caption and no fill change; its thumb growing is its focus signal.

### Seek preview

While the viewer is scrubbing and the content provides frame stills, a still rides above the track at
the playhead and the title block fades out. The still must stay within the track's bounds at both
extremes rather than centring on the thumb. It appears only while scrubbing is active and stands down
about one and a half seconds after the last seek, restoring the title — holding it up for as long as
the bar merely has focus would hide the title for no reason.

## Focus ownership

Exactly one group owns the D-pad at any moment. The group is a single derived value, not a set of
independent flags, and every visibility and focusability decision reads it.

| Group | Owns focus when | Focus target |
|---|---|---|
| `Surface` | No chrome, no error | Full-screen input target |
| `Controller` | Controller shown, no section, no error | A control inside the controller |
| `Section` | A side section is open | Inside the section |
| `Parked` | A section transition is animating | Off-screen anchor |
| `Error` | Playback failed | The retry control, when one is offered |

Precedence, highest first: `Error`, `Parked`, `Section`, `Controller`, `Surface`. An error must
outrank everything; a transition must outrank the section it animates; a live section must outrank the
controller beneath it.

Focus must be handed out in one place. A subtree must not request focus for itself while the owner is
also deciding — a panel that did so raced the owner's decision and lost, leaving the screen with
nothing focusable.

### Focus-safe transitions

- Focus must be parked on an off-screen anchor for the length of a section transition. Without it the
  focused control disappears mid-animation and focus falls back to whatever is spatially nearest,
  which is the video surface, which then swallows the next key press.
- The surface must stop being focusable the moment the controller appears. It must **not** also be
  parked at that moment: parking and the controller's own entry request are two claims on the same
  focus, and the anchor wins, leaving the controller visible and unfocused.
- The controller must place focus **once per appearance**, not whenever the focused control changes.
  Re-placing on every change re-enters the same effect on each move, which oscillates focus and, as a
  side effect, continually restarts the auto-hide timer so the controller never hides.
- A control's visual focus state must follow the focus the screen actually observes for that control,
  and must use "this control is focused", not "this control or a descendant is focused". The latter
  stays true after focus has moved away, leaving a control painted as focused while another one has
  the D-pad.

## Focus graph

Entry focus is the play/pause control.

| From | Up | Down | Left | Right |
|---|---|---|---|---|
| Input target | reveal controller | reveal controller | reveal controller | reveal controller |
| Seek bar | — | last-used control | seek backward | seek forward |
| `Description` | seek bar | — | — | Rewind |
| Rewind | seek bar | — | `Description` | Play/pause |
| Play/pause | seek bar | — | Rewind | Forward |
| Forward | seek bar | — | Play/pause | Like |
| Like | seek bar | — | Forward | Save |
| Save | seek bar | — | Like | Comment |
| Comment | seek bar | — | Save | Settings |
| Settings | seek bar | — | Comment | — |

On a live stream, Rewind and Forward are absent: `Description` goes right to play/pause, and
play/pause goes right to Like. Up from any control does nothing, there being no seek bar.

Down from the control row must do nothing. Letting it fall through hands focus to the video surface,
which then hides the controller the viewer is still using.

### Restoring the control row

Down from the seek bar must return focus to **the control the viewer last used**, not to a fixed
control. Before any control has been used it lands on play/pause.

This must be implemented by remembering the last focused control. Framework focus-restoration that
keys off a focus group's search-enter and search-exit hooks does **not** work here: the row is left
and re-entered by direct focus requests, which bypass those hooks, so the restore finds nothing saved
and always falls back.

## Interaction outcomes

| Input | Group | Outcome |
|---|---|---|
| Any D-pad direction or select | `Surface` | Reveal the controller |
| Play/pause key | `Surface` | Toggle playback **and** reveal the controller |
| Select on play/pause | `Controller` | Toggle playback |
| Select on rewind / forward | `Controller` | Seek by the configured increment |
| Left / right on the seek bar | `Controller` | Seek by the configured increment, focus unchanged |
| Select on `Description` | `Controller` | Open the metadata section |
| Select on like / save | `Controller` | Toggle that state, controller stays open |
| Select on comment | `Controller` | Open the comments section |
| Select on settings | `Controller` | Open the settings section |
| Back | `Controller` | Hide the controller |
| Back | `Surface` | Leave the player |
| Back | `Section` | Close the section, reopening the controller on the control that opened it |

A control must ignore activation for a short interval after the controller appears. The key press that
revealed the controller is still in flight, and its release otherwise lands on whichever control just
took focus and triggers it immediately.

The controller hides itself after about five seconds of no interaction, and only while playback is
advancing. It must stay up while paused, while a section is open, and while an error is shown.

Opening a section must set the control to restore before the controller is dismissed, so closing the
section returns focus to the control that opened it rather than to the entry control.

## Error handling

An error replaces the controller and the sections, resets the section stack, and hides the controller.
It shows the message and, only when the failure is worth retrying, a retry control that takes focus.

Retry must re-prepare and then resume, in that order — preparing is what clears the error, and
resuming a still-errored player does nothing.

## Acceptance scenarios

1. Opening the player shows video with no chrome; focus is on the input target.
2. Pressing any direction reveals the controller with focus on play/pause; the press itself does not
   also activate a control.
3. Pressing up from play/pause focuses the seek bar and enlarges its thumb; the play/pause control
   stops being painted as focused.
4. Pressing right from the seek bar seeks forward and leaves focus on the seek bar.
5. Moving to the save control, then up to the seek bar, then down, returns focus to the save control.
6. Focusing any control except play/pause shows its caption beneath it; the caption is not clipped by
   the cluster it belongs to.
7. Adding or removing the settings control does not move the transport cluster.
8. Scrubbing shows the frame still over the track and hides the title; about one and a half seconds
   after the last seek the title returns.
9. On a live stream the seek bar is replaced by an elapsed-time label, a `LIVE` badge precedes the
   title, and rewind and forward are absent.
10. Opening settings, then pressing back, reopens the controller with focus on the settings control.
11. Leaving playback idle for five seconds while playing hides the controller; pausing keeps it up.
12. A retryable failure shows the message with a focused retry control; a non-retryable one shows the
    message alone.

## Known deviation

Stream-provided subtitles are drawn by the video surface and can sit behind the control row while the
controller is shown. Subtitles should be lifted clear of the chrome, or suppressed while the
controller is visible.
