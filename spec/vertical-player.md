# Portrait player screen specification

Read [the shared StreamTV specification](README.md) and then
[the landscape player specification](player.md) first. This document states only what the portrait
player does differently; the data contract, focus-ownership model, side-section behaviour and error
handling are identical and are not repeated.

## Purpose

Full-screen playback for content shot portrait: shorts, and the items in the vertical banner. A
television panel is landscape, so a portrait video cannot fill it. Rather than letterbox it into
narrow bars or crop away the top and bottom of the frame, the screen presents a centred portrait
stage and gives the freed width to the content's own actions.

## Screen composition

Three regions, arranged across the panel:

1. **Ambient background** — a horizontal gradient filling the panel, darkest on the leading edge and
   picking up the brand tint on the trailing edge. It exists so the area beside the stage reads as
   deliberate rather than as empty letterboxing.
2. **Portrait stage** — a 9:16 box, full panel height, centred and nudged toward the leading edge to
   leave room for the panel beside it. Rounded corners. The video fills the stage by cropping, not by
   fitting, so no bars appear inside the stage.
3. **Interaction panel** — on the trailing edge, as wide as the space the stage leaves, at least a
   fixed minimum. Holds the title and the action row.

The stage is the focus target for playback itself; it is not a passive surface.

### Stage chrome

- While paused and not buffering, a static play glyph is centred on the stage. It is **state, not an
  acknowledgement animation**: with no control row on this surface, a paused short would otherwise
  look identical to a stalled one. It must be on screen for exactly as long as playback is paused,
  and must not be focusable.
- While buffering, the buffering indicator is centred on the stage instead.
- When the content is seekable, a progress bar sits along the bottom inside edge of the stage under a
  short upward scrim. There is no thumb and no time labels: this surface offers no scrubbing.

### Interaction panel

Bottom-aligned within the panel:

- **Title block** — the optional collection line above the title, on up to two lines. Focusable:
  selecting it opens the metadata section. Its container is tinted while anything in the panel holds
  focus, so the panel reads as one region.
- **Action row** — like, comment, save, and settings when available. Circular controls, smaller than
  the landscape player's, spaced evenly.

Unlike the landscape player, this screen has **no transport cluster, no seek bar, no `Description`
pill and no captions under focused controls**. The title is the metadata entry point here, and the
stage itself is the play/pause control.

## Section stack

Identical to the landscape player's, including the transition phases, the parked anchor and the rule
that every level stays composed. The section tree is the same tree — Settings → Quality, Audio,
Subtitles — and behaves identically on both screens.

Two differences, both stated below in full: sections here are dismissible with Left as well as Back,
and closing one returns focus to the stage rather than to the control that opened it.

## Focus ownership

Identical groups and precedence to the landscape player. The differences:

- There is no `Controller` group. The stage and the interaction panel are both part of the base level
  and are focusable together, so nothing needs revealing and nothing auto-hides.
- The **whole base level** — the stage *and* the interaction panel — must stop being focusable while
  a section is open or animating, and become focusable again as the section returns to the base
  level. Excluding only the stage leaves the panel's own buttons reachable behind an open section,
  so a viewer walking left out of the section lands on a control the section is covering.

## Focus graph

Entry focus is the stage.

| From | Up | Down | Left | Right | Select |
|---|---|---|---|---|---|
| Stage | — | — | — | Like | Toggle playback |
| Title block | — | Like | Stage | — | Open metadata |
| Like | Title block | — | Stage | Comment | Toggle like |
| Comment | Title block | — | Like | Save | Open comments |
| Save | Title block | — | Comment | Settings | Toggle save |
| Settings | Title block | — | Save | — | Open settings |

Right from the stage enters the panel at the **first action**, not at the title block: the action row
is what a viewer reaches for, and the title block is one step Up from there.

Left from the title block and from the first action must return to the stage. Left from a later action
moves within the row. This asymmetry is deliberate: the stage is the only thing to the left of the
panel, so only the panel's leading edge should reach it.

## Interaction outcomes

| Input | Focus | Outcome |
|---|---|---|
| Select | Stage | Toggle playback |
| Play/pause key | Stage | Toggle playback |
| Right | Stage | Move to the interaction panel |
| Back | Base level | Leave the player |
| Back | Section | Close the section and return focus to the stage |

Closing a section returns focus to the **stage**, not to the control that opened it. The landscape
player restores the opening control because its controller is transient and has to be rebuilt; here
the panel never goes away, and the stage is where a viewer expects to land when a panel closes.

Side sections on this screen are additionally dismissible with Left, matching the direction they
entered from. On the landscape player they are not.

## Scope

A portrait feed — swiping up and down between videos without leaving the player — is **not** part of
this specification. This screen plays one item. Adding a feed later changes what the stage shows and
adds page-change handling; it does not change the section stack, the focus graph, or anything else
stated here.

## Visual differences from landscape

| | Landscape | Portrait |
|---|---|---|
| Video fit | Letterboxed, full panel | Cropped into a centred 9:16 stage |
| Background | The video itself | Horizontal ambient gradient |
| Chrome lifetime | Transient, auto-hides | Permanent |
| Transport controls | Rewind, play/pause, forward | None; the stage is the control |
| Seek bar | Focusable, with thumb and times | Non-interactive progress line |
| Metadata entry | `Description` pill | Title block |
| Focused control caption | Yes | No |
| Section panel | Rounded translucent panel | Transparent over the ambient background |
| Section dismissal | Back | Back or Left |

## Acceptance scenarios

1. Opening the player shows a centred portrait stage with the video cropped to fill it and an ambient
   gradient beside it; focus is on the stage.
2. Selecting on the stage pauses playback and shows the static play glyph centred on the stage; the
   glyph stays until playback resumes.
3. Buffering shows the buffering indicator instead of the play glyph.
4. Pressing right from the stage focuses the first action and tints the panel; pressing up from it
   focuses the title block.
5. Pressing down from the title block focuses the first action; pressing left from it returns to the
   stage.
6. Pressing left from a later action moves within the action row rather than to the stage.
7. Selecting the title block opens metadata; pressing back closes it and returns focus to the stage.
8. A section is dismissible with Left as well as Back.
9. A seekable short shows a progress line inside the bottom of the stage with no thumb and no times.
10. A retryable failure shows the message with a focused retry control, as on the landscape player.
