# Spec — Seek frame preview

- **Date:** 2026-09-02
- **Status:** Implemented (dummy frame strip)

## Problem

Seeking on a TV remote is blind. The viewer holds Right, watches a timestamp climb, and has no idea
whether they have landed before or after the moment they wanted — so they overshoot, correct, and
overshoot again. Every mainstream TV player answers this with a still frame above the seek bar.

## User stories

- **As a viewer scrubbing a video**, I see the frame at the position I am seeking to, so I can stop
  where I meant to.
- **As a viewer of a video with no frame strip**, seeking still works exactly as before, with no gap
  or placeholder where a preview would have been.
- **As a viewer who stops scrubbing**, the title and actions come back, so the frame strip never
  becomes permanent furniture.

## Acceptance criteria

| # | Given | When | Then |
|---|---|---|---|
| 1 | A seekable video whose frame strip has loaded | the viewer presses Left or Right on the seek bar | a frame card appears above the track, showing the frame at the new position |
| 2 | The frame card is showing | the viewer keeps seeking | the card slides with the thumb and swaps to the frame for each new position |
| 3 | The frame card is showing | the viewer stops seeking for 1.6s | the card fades out and the title row and action buttons fade back in |
| 4 | The frame card is showing | focus leaves the seek bar | the card goes immediately, without waiting out the idle timer |
| 5 | A video with no frame strip | the viewer seeks | no card appears and the title row stays put |
| 6 | Position is at 0% or 100% | the card is placed | it sits flush with the track's left or right edge and never overhangs it |
| 7 | A live stream | the controller opens | there is no seek bar, so the question does not arise |

## Platform notes

TV only. The card is placed by track fraction, not by a pointer, because a remote has no cursor —
the thumb *is* the pointer.

## Out of scope

- Fetching a real frame strip. The repository returns a fixed list of stills; wiring a BIF or
  WebVTT thumbnail track to it is a data-layer change that needs no UI work (see the impl plan).
- Chapter markers and preview timestamps overlaid on the card.
