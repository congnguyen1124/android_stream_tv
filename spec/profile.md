# Profile screen specification

Read [the shared StreamTV specification](README.md) first. This document defines Profile as a
signed-out sign-in screen: an invitation on the left and mobile-app pairing instructions on the
right, independent of UI framework.

## Purpose

Profile is where a viewer starts an account on a device that has no keyboard. The screen must make
two paths obvious at a glance — pair with the phone app, or sign in with a phone number — while
staying readable from across a room and operable with four direction keys and Center.

Pairing is presentation-only in this release. Nothing on the screen confirms a session, so Profile
has exactly one appearance: signed out. A port must not invent a signed-in variant, an avatar
picker, or an account menu.

## Data and state contract

Profile has one immutable state containing:

- a pairing URL, which is what the QR symbol encodes;
- a pairing code, which is the same credential printed for manual entry;
- a validity label, shown as a wall-clock time;
- whether phone sign-in has been selected.

Rules:

- The pairing URL must contain the pairing code, so that the symbol and the printed code are never
  two different credentials.
- The dummy session must be constant for the lifetime of the screen. It must not be regenerated on
  recomposition, refreshed on a timer, or counted down. Every render of the same state produces the
  same symbol, the same code, and the same label.
- The state must not carry a signed-in flag, a profile identity, or an entitlement.
- A port replacing the dummy session with a real one must keep this contract: one issued URL, its
  matching code, and one expiry the viewer can read.

Profile performs no asynchronous work, so the shared loading, error, and empty branches described in
[shared asynchronous states](README.md#shared-asynchronous-states) do not apply. A port that issues
real pairing sessions must add them: an unavailable session must state so in English in place of the
symbol and the code, and must never show a stale credential as if it were live.

## Screen composition

- Use the near-black app background. Profile requests no top-bar readability layer.
- Content occupies the area between the 80-unit top bar plus 8 units of clearance and 20 units of
  bottom padding, inside the 48-unit horizontal safe edges.
- Lay out two columns, vertically centered in that area, separated by 32 units:
  - the sign-in column takes the remaining width and centers its content, up to 400 units;
  - the pairing panel is a fixed 344 units wide on the trailing side.
- The composition must fit a 540-unit-high viewport without scrolling. Nothing in the pairing panel
  can take focus, so no scroll gesture could ever reveal clipped content.

### Sign-in column

Show, centered and in order:

1. the StreamTV logo, about 168 × 34 units;
2. “Sign In or Sign Up” as the screen headline, 16 units below the logo;
3. supporting copy 10 units below the headline;
4. a “Sign in with phone number” action 24 units below the copy.

The supporting copy reserves three lines of height whether or not it fills them. The action must not
move when the copy changes.

Copy for the supporting line:

| Phone sign-in selected | Text |
|---|---|
| No | “Start streaming the channels, movies and shows made for your big screen.” |
| Yes | “Phone sign-in continues on your mobile device. Scan the code to finish on this TV.” |

Selecting the action must not alter the pairing session, and must not navigate. In this release it
only swaps that line. A port that implements phone sign-in for real replaces the swap with its own
flow; it must still leave the pairing panel unchanged and the code valid.

### Pairing panel

The panel is a compact rounded surface — 14-unit corners, a 1-unit translucent white border, and a
vertical gradient from a translucent cool blue at the top to a barely-lit translucent white at the
bottom. It uses 20 units of horizontal and 16 units of vertical padding, and centers its children.

Show, in order:

1. the heading “Use the StreamTV app”;
2. 14 units of space;
3. numbered step 1, “Open “Account” in the StreamTV mobile app”, with the mobile-app illustration
   beneath its text;
4. 12 units of space;
5. numbered step 2, “Scan the QR code or enter the sign-in code to sign in”;
6. 14 units of space;
7. the QR symbol, 124 units square;
8. 12 units of space;
9. a rule–“OR”–rule divider;
10. 10 units of space;
11. the label “Sign-in code”;
12. the pairing code, 28-unit type with 2 units of letter spacing;
13. 6 units of space;
14. “Valid until <time>”.

Step text is limited to two lines and ellipsizes rather than growing the panel. Step numbers sit in
20-unit circular blue badges with light blue digits.

### QR symbol

- Encode the pairing URL as a QR symbol at error-correction level M, in UTF-8, at the smallest
  version that fits the content.
- Draw dark modules on a white plate with 10-unit corners and a 10-unit quiet zone contributed as
  plate padding, not as encoder margin.
- Module edges must snap outward so adjacent modules never show a seam at television densities.
- Encoding must be pure computation. It must not require network access, a player, or any
  runtime-only dependency, so the symbol renders in a design-time preview.
- Content that cannot be encoded leaves the plate blank. A partial or malformed symbol must never be
  displayed, because a camera would silently fail on it.
- A symbol drawn at 124 units on a 540-unit-high viewport must decode back to the exact pairing URL.
  This is the screen's acceptance criterion, not its appearance.

### Mobile-app illustration

Beneath step 1, draw a miniature of the mobile app's bottom navigation rather than shipping a
screenshot: a 30-unit-high dark rounded strip containing a highlighted Home item with its label,
three dimmed destination icons, and an Account icon inside a blue ring. The ring identifies where the
viewer taps on their phone. Reuse the shell's own icons so the illustration cannot drift from the
mobile app's iconography by more than its layout.

## Focus graph

Profile has exactly one focusable control: the phone sign-in action.

- Profile must never claim focus on appearance. It is reachable only from the top bar, which keeps
  focus on the Profile item until the viewer presses Down. See
  [the shared top-bar focus contract](README.md#top-bar-focus-contract).
- Down from the Profile top-bar item focuses the phone action.
- Up from the phone action returns focus to the top bar.
- Left, Right, and Down from the phone action have no target and release the key.
- The phone action uses the shared focus treatment: a white filled surface with dark content.
- The pairing panel, the QR symbol, the code, and the illustration are never focusable and never
  draw a focus border.
- While the top bar owns focus, the shell's translucent overlay dims the whole screen, including the
  pairing panel. The panel must remain legible enough to read the code through the overlay.

## Accessibility

- The QR symbol carries the description “QR code that opens StreamTV sign-in in the mobile app”.
- The logo carries the application name.
- Decorative icons inside the illustration carry no description.
- The pairing code is exposed as text, so a viewer using a screen reader can hear the credential
  without decoding the symbol.

## Acceptance scenarios

- Opening Profile from the top bar leaves focus on the Profile top-bar item; no element of the
  screen takes focus during composition.
- Down focuses the phone action; Up from it returns to the top bar.
- The QR symbol, the printed code, and the validity label are all visible at 1280×720 and
  1920×1080 without scrolling or clipping.
- Photographing or capturing the rendered screen and decoding the symbol yields exactly the pairing
  URL, and that URL ends with the printed code.
- Pressing Center on the phone action swaps only the supporting copy. The action does not move, the
  symbol does not change, and the code does not change.
- Re-entering Profile shows the same code as before; nothing regenerates it.
- Blank pairing content renders an empty white plate rather than a partial symbol.
- Both step numbers, both step texts, and the illustration remain readable with the top-bar dim
  overlay applied.
