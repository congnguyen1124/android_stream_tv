# Spec — Home banner trailer

The Home hero banner shows a still thumbnail. This adds ambient trailer playback to it: the still
holds long enough to be read, then the focused item's trailer plays over it, silently and on loop,
with no controls of any kind.

## User stories

1. **As a viewer landing on Home**, I see the featured item's artwork and title, and a few seconds
   later it comes alive with a trailer — so the shelf feels like television rather than a catalogue.
2. **As a viewer arrowing through the banner**, moving between items never triggers playback, so the
   carousel stays instant and the app makes no stream requests I did not ask for.
3. **As a viewer browsing the rows below**, the banner goes quiet the moment I leave it, so nothing
   is decoding video I cannot see.
4. **As a viewer whose network drops**, a trailer that cannot play leaves the banner exactly as it
   was before this feature existed — artwork, title, CTA — with no error surface of its own.

## Acceptance criteria

| # | Given | When | Then |
|---|---|---|---|
| 1 | The banner is focused on an item with a trailer | 5 s pass with focus unmoved | The trailer loads and fades in over the thumbnail |
| 2 | The banner is focused | D-pad Left/Right moves to another item | Any playing trailer stops, the thumbnail returns, and the 5 s wait restarts for the new item |
| 3 | A trailer is playing | Focus moves to the top bar or to a row below | Playback stops and the stream is unloaded |
| 4 | A trailer is playing | It reaches its end | It restarts from the beginning, with no flash of the thumbnail in between |
| 5 | A trailer is playing | The viewer opens the full-screen player, or the app is backgrounded | Playback stops; returning to Home starts a fresh 5 s wait |
| 6 | The banner is focused on an item whose trailer fails to load | Any playback error | The thumbnail stays on screen, no retry is attempted, and no error UI appears |
| 7 | The banner is focused on an item with a blank `trailerUrl` | 5 s pass | Nothing loads; the banner stays a still image |
| 8 | The banner is **not** focused | Auto-scroll advances the carousel | Thumbnails cross-fade as before; no trailer plays at any point |

## Behaviour notes

- **No chrome.** No play/pause badge, no seek bar, no buffering spinner, no subtitles. The banner's
  existing title, age rating, CTA and dot indicator are unchanged and stay drawn over the video.
- **Framing.** The video is cropped to fill (`RESIZE_MODE_ZOOM`), matching the thumbnail's
  `ContentScale.Crop`. Letterboxing would lay black bars over the banner's gradients.
- **The thumbnail is never removed.** It stays underneath the video for the whole session, which is
  what makes criteria 6 and 7 fall out for free rather than needing a fallback path.
- **Audio is not muted**, because the player library exposes no volume command. See "Open points".

## Error scenarios

| Scenario | Handling |
|---|---|
| Network unavailable, 404, 403, unsupported codec | Identical: stop showing video, keep the thumbnail. The banner does not classify playback errors — that distinction only matters where a viewer chose to watch something. |
| Trailer URL missing or blank | Never dispatched to the player. |
| Decoder unavailable because another player holds it | Reported as a playback error, handled as above. |

## Out of scope

- Muting, or any audio control.
- A trailer on the vertical (`Short`) banner or on `ContentRow` cards.
- Analytics events for trailer impressions.
- Preloading the next item's trailer.

## Open points

- **Audio.** Streaming apps usually play hero trailers muted, or muted-with-a-toggle. The
  `stream-player` command set has no volume command, so muting would mean either adding one to the
  library or reaching through `StreamTvPlayerQuery.ExoPlayer` to set `volume = 0f`. Deferred until
  the product decision is made.
- **Dwell duration.** 5 s is chosen to match the banner's existing auto-scroll interval; it has not
  been validated with anyone watching a real TV.
