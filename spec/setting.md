# Setting screen specification

Read [the shared StreamTV specification](README.md) first. This document defines Settings as a
two-pane screen — a grouped menu beside the pane it describes — independent of UI framework.

## Purpose

Settings is a reading surface with a handful of actions. A viewer moves down a short menu with the
D-pad and the pane beside it answers immediately, so browsing what the app can tell them costs no
confirmations and no screen transitions.

Only account state and privacy history are presentational in this release. Everything Settings
reports about the build and the device is real, read from the running application and the platform.

## Data and state contract

Settings has one immutable state containing:

- the selected menu entry;
- a loading flag, the system information, and an optional English error message;
- whether search history has been cleared;
- whether watch history has been cleared.

The selected entry is the entire navigation model of the screen: the menu draws it as selected and
the pane renders it. There is no second level, no dialog, and no route below Settings.

The menu itself is not state. It is a fixed structure of three labeled groups:

| Group | Entries |
|---|---|
| Account | Manage subscription, Payment history, Manage devices, Gift code |
| About StreamTV | Terms of service, Privacy policy, Send feedback |
| Privacy | Clear search history, Clear watch history |

Entries are a closed set. Every place that decides what an entry means must handle all of them, so
adding an entry without giving it a pane is a build failure rather than a blank pane at runtime.

### System information

One read supplies the Manage devices pane:

- application version name, version code, and build type;
- device manufacturer, model, and brand;
- Android release;
- the device's time-zone identifier.

Rules:

- These are platform reads, not content. Keep them behind the same data boundary as any other
  repository so the presentation layer stays free of platform APIs.
- Values are stored raw and formatted where the surrounding English copy lives.
- When the model name already begins with the manufacturer, show the model alone. Repeating it reads
  as a defect.
- The read happens once when the screen opens. A failure must publish its message and stop loading;
  the pane must never sit on a loading message forever.

### Privacy actions

- Clearing is idempotent and one-way within a session. Clearing one history must not affect the
  other.
- A cleared entry replaces its explanation with a confirmation and removes its action, rather than
  keeping a control that would do nothing.
- Clearing is presentational in this release: it reports what a real implementation would remove.

## Screen composition

- Use the near-black app background. Settings requests no top-bar readability layer.
- Content occupies the area between the 80-unit top bar plus 8 units of clearance and 20 units of
  bottom padding, inside the 48-unit horizontal safe edges.
- The screen title “Settings” sits at the top left, 22-unit type, with 10 units beneath it.
- Below the title, lay out two columns: a 208-unit menu, a 32-unit gap, and the detail pane taking
  the remaining width and height.

### Menu

- Group labels are dim, 11-unit type, and never focusable. Give a label 8 units of space above it,
  except the first, and 4 units below.
- Entries are 29 units high with 4 units between them, 6-unit corners, and centered 13-unit labels
  that ellipsize rather than wrap.
- All nine entries and all three labels must fit the viewport at 720p without scrolling. The menu
  may still be a scrolling container so that a future entry cannot be stranded off-screen.
- Entry appearance has three states:

| State | Appearance |
|---|---|
| Idle | Dark neutral fill, near-white label |
| Selected, focus elsewhere | Translucent white fill with a translucent white border |
| Focused | White fill, black label |

- Entries must not grow when focused. At this density a scaled entry would overlap its neighbours.
- The selected-but-unfocused state is required. It is what tells the viewer which entry the pane
  belongs to while they are acting inside the pane. This is a deliberate departure from a reference
  implementation that dropped the selection highlight entirely once focus left the menu.

### Detail panes

Each entry maps to exactly one pane:

| Entry | Pane |
|---|---|
| Manage subscription, Payment history, Gift code | Sign-in required |
| Manage devices | System information |
| Terms of service, Privacy policy | Document |
| Send feedback | Feedback |
| Clear search history, Clear watch history | Privacy action |

- Panes cross-fade over about 180 ms. Selection changes on every menu keypress, and a hard swap
  reads as flashing.
- The pane leaving a cross-fade must not be focusable while it fades. Otherwise a Right press during
  the transition can land on a control that is disappearing.
- **Sign-in required** centers the app logo, a two-line English explanation, and a “Get started”
  action.
- **System information** stacks two labeled cards from the top of the pane: version information
  (application name with version name; version code and build type beneath) and current device
  (device name; Android release, brand, model, and time zone beneath, separated by middle dots).
  Cards are translucent white with a 1-unit translucent border and 10-unit corners.
- **Document** shows a 20-unit heading and body copy at 14 units with a 22-unit line height, capped
  at 520 units wide. Copy must fit the pane at 720p: nothing in a document pane is focusable, so no
  D-pad action could scroll a longer document into view.
- **Feedback** centers a heading, an instruction, a 116-unit QR symbol encoding the feedback URL, and
  that URL as text. Feedback is composed on a phone; a television keyboard is the wrong instrument
  for a paragraph. The symbol follows the QR rules in [Profile](profile.md#qr-symbol).
- **Privacy action** shows a heading, the explanation, and the clearing action, aligned to the top of
  the pane. Once cleared, the confirmation replaces the explanation and the action is gone.

## Focus graph

Settings has at most two focusable things at a time: the selected menu entry and the current pane's
action, when it has one.

- Settings must never claim focus on appearance. It is reached from the top bar, which keeps focus on
  its own item until Down is pressed. See
  [the shared top-bar focus contract](README.md#top-bar-focus-contract).
- Down from the Setting top-bar item focuses the **selected** entry, not the first entry. Returning
  to Settings therefore resumes where the viewer left off.
- **Selection follows focus.** Moving focus onto an entry selects it and swaps the pane. Center on an
  entry only re-states the selection it already has; it must not be required to see the pane.
- Up and Down move between entries and skip group labels.
- Up from the first entry returns focus to the top bar.
- Down from the last entry has no target and releases the key.
- Right moves focus to the pane's action when the current pane has one, and otherwise leaves focus
  where it is. It must never leave focus nowhere.
- Left from a pane action returns focus to the selected entry.
- Nothing else in a pane is focusable: no card, document, QR symbol, or label.

### Focus-safe transitions

- Pressing a clearing action removes that action. Park focus on the selected menu entry **before**
  the state changes; focus must not be left on a node that is being removed, and it must not fall
  back to the top of the menu.
- “Get started” leaves Settings for the sign-in screen. Focus was in content when the viewer asked,
  so it must arrive in content: the shell hands focus to the arriving destination's entry target
  once that destination is current. Parking on the top bar instead is wrong here — the bar restores
  focus to the item it considers selected, which during a route change is not yet the one being
  opened.

## Navigation outcomes

- “Get started” opens the sign-in destination as if the viewer had selected it in the top bar: the
  bar shows it as the selected destination, and focus lands on its sign-in action.
- No other entry navigates. Back is not handled by Settings and is released to application
  navigation.

## Acceptance scenarios

- Opening Settings from the top bar leaves focus on the Setting top-bar item; nothing in the screen
  takes focus during composition or while system information loads.
- Down focuses the selected entry; Up from the first entry returns to the bar.
- Moving down two entries from the top shows the version and device cards with no Center press.
- Manage devices reports the version and device of the build actually running, not sample values.
- A device whose model already contains its manufacturer is not shown with the name twice.
- Right from a gated entry focuses “Get started”; Left returns to that same entry, which is still
  drawn as selected throughout.
- Right from Terms of service, Privacy policy, Send feedback, or Manage devices leaves focus on the
  entry.
- Clearing a history replaces the pane copy with its confirmation, removes the action, and leaves
  focus on the entry it came from.
- Clearing search history leaves watch history untouched, and the reverse.
- “Get started” opens sign-in with focus on that screen's sign-in action, and the D-pad still moves.
- All nine entries and all three group labels are visible at 1280×720 and 1920×1080 without
  scrolling or clipping.
