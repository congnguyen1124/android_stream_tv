# ADR — ZXing core for the QR symbol on Profile

- **Date:** 2026-09-03
- **Status:** Accepted

## Context

Profile's sign-in screen shows a QR symbol a viewer photographs with the mobile app. A QR symbol is
the one thing on that screen that is either exactly right or completely useless: a phone camera
either decodes it or silently refuses, and no visual review catches a symbol whose modules are half
a pixel off.

Three ways to get one on screen:

- fetch a rendered image from a QR web service. Rejected: it puts a network dependency in front of
  sign-in, breaks the `@Preview` requirement in `AGENTS.md`, and leaks the pairing URL to a third
  party;
- hand-write an encoder. Rejected: Reed–Solomon error correction, mask selection and version
  arithmetic are a lot of code whose bugs surface as "some phones can't scan it";
- encode with a library and draw the modules ourselves.

## Decision

Encode with `com.google.zxing:core` (3.5.3) and draw the result on a Compose `Canvas`, in
`core/designsystem/component/StreamTvQrCode.kt`.

- ZXing core is pure Java with no Android or network dependency, so `encodeStreamTvQrMatrix` runs in
  a JVM unit test and inside a design-time preview. The `android` and `javase` ZXing artifacts are
  deliberately not used: the first drags in a camera stack this app has no use for, the second pulls
  `java.awt`.
- The encoder is asked for a zero-margin matrix (`EncodeHintType.MARGIN = 0`) and the quiet zone is
  drawn as layout padding on the white plate instead. The quiet zone is then a design token rather
  than an encoder setting, and the plate's rounded corners cannot eat into it.
- `BitMatrix` is converted to a small `StreamTvQrMatrix` at the boundary, so ZXing types stay out of
  the composable's signature and out of the feature package.
- Module rectangles snap outward — `floor` on the leading edge, `ceil` on the trailing one — because
  a fractional module pitch is the normal case on a television, and adjacent modules drawn at exact
  float offsets show anti-aliased seams that cost decode margin.
- Unencodable content renders an empty plate. A partial symbol looks like a working one.

Error correction is level M, the middle setting: enough redundancy for a photograph taken across a
living room, without inflating the version and shrinking each module.

## Consequences

- One new dependency, ~590 KB. It is also the encoder to reach for if pairing later needs a second
  code surface.
- `StreamTvQrCodeTest` guards the part that matters: it rasterizes the matrix the way the composable
  draws it, at a deliberately awkward fractional pitch, and decodes it back with ZXing's reader. A
  rounding mistake in the drawing rule fails that test instead of failing a viewer's camera.
- The symbol was also verified end to end on the `Android_TV_720p` emulator: a `screencap` of the
  rendered 124-unit symbol decodes to `https://tv.streamtv.example.com/pair?code=XHSZ-QBKX`, the
  pairing URL the dummy session publishes.
- The composable takes content as a string and knows nothing about pairing, so a real pairing
  session replaces the dummy values in `ProfileViewModel` without touching the design system.
