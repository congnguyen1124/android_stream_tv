#!/usr/bin/env python3
"""Capture StreamTV screenshots and navigation GIFs from a connected Android TV device.

The showcase in `README.md` needs two kinds of evidence, and they have different costs:

* A **still** is enough when the point is layout, hierarchy or colour.
* A **GIF** is the only honest way to show focus and navigation, because the thing being
  demonstrated is the *change* between frames.

Both are produced here rather than by hand so that a capture can be re-run after a UI change and
land byte-identically in the same place, instead of drifting into a folder of stale screenshots
nobody can reproduce.

Usage
-----
    python3 tools/capture_media.py list
    python3 tools/capture_media.py shot home-overview
    python3 tools/capture_media.py gif topbar-focus
    python3 tools/capture_media.py all

Requirements: `adb` on PATH with exactly one device, and `ffmpeg` for GIF conversion.
"""

from __future__ import annotations

import argparse
import shutil
import subprocess
import sys
import time
from dataclasses import dataclass, field
from pathlib import Path

PACKAGE = "com.congnguyencn.stream_tv"
ACTIVITY = f"{PACKAGE}/.MainActivity"
OUTPUT_DIR = Path(__file__).resolve().parent.parent / "docs" / "images"

# Wide enough to stay readable when GitHub scales it into a table cell, small enough that a
# handful of them do not dominate a clone. A GIF of live video changes on every frame, so its size
# is driven by frame count and palette far more than by any single setting.
GIF_WIDTH = 600
GIF_FPS = 10
GIF_COLORS = 96

# Stills are WebP: these are photographic screenshots, and lossless PNG costs roughly ten times as
# much for no visible gain at the size a README renders them.
STILL_QUALITY = 84


@dataclass(frozen=True)
class Capture:
    """One reproducible capture.

    `setup` runs before recording starts and is not shown; `steps` are the keys that make up the
    demonstration itself. Splitting them is what keeps a GIF focused on the interaction rather than
    on the navigation needed to reach it.
    """

    name: str
    description: str
    setup: list[str] = field(default_factory=list)
    steps: list[str] = field(default_factory=list)
    settle: float = 2.0
    step_delay: float = 1.0
    duration: int = 6


def run(args: list[str], **kwargs) -> subprocess.CompletedProcess:
    return subprocess.run(args, check=True, capture_output=True, **kwargs)


def adb(*args: str) -> subprocess.CompletedProcess:
    return run(["adb", *args])


def key(name: str) -> None:
    """Send one remote key. Accepts bare names like `DPAD_DOWN` or a `sleep:1.5` pause."""
    if name.startswith("sleep:"):
        time.sleep(float(name.split(":", 1)[1]))
        return
    adb("shell", "input", "keyevent", f"KEYCODE_{name}")


def relaunch() -> None:
    """Start every capture from a cold screen so one capture cannot inherit another's focus."""
    adb("shell", "am", "force-stop", PACKAGE)
    time.sleep(1.0)
    adb("shell", "am", "start", "-n", ACTIVITY)


def apply_setup(capture: Capture) -> None:
    relaunch()
    time.sleep(capture.settle)
    for step in capture.setup:
        key(step)
        time.sleep(capture.step_delay)


def take_screenshot(capture: Capture) -> Path:
    apply_setup(capture)
    for step in capture.steps:
        key(step)
        time.sleep(capture.step_delay)
    time.sleep(0.6)

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    raw = OUTPUT_DIR / f"{capture.name}.raw.png"
    with raw.open("wb") as output:
        subprocess.run(["adb", "exec-out", "screencap", "-p"], check=True, stdout=output)

    destination = OUTPUT_DIR / f"{capture.name}.webp"
    run(["ffmpeg", "-v", "error", "-y", "-i", str(raw),
         "-c:v", "libwebp", "-quality", str(STILL_QUALITY), "-compression_level", "6",
         str(destination)])
    raw.unlink(missing_ok=True)
    return destination


def take_gif(capture: Capture) -> Path:
    apply_setup(capture)

    device_mp4 = "/sdcard/streamtv_capture.mp4"
    recorder = subprocess.Popen(
        ["adb", "shell", "screenrecord", "--bit-rate", "6000000",
         "--time-limit", str(capture.duration), device_mp4],
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )
    # screenrecord needs a moment before it is actually capturing; keys sent earlier are lost.
    time.sleep(1.5)

    for step in capture.steps:
        key(step)
        time.sleep(capture.step_delay)

    recorder.wait(timeout=capture.duration + 30)
    # The encoder finishes writing after the process exits; pulling too early truncates the file.
    time.sleep(1.5)

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    local_mp4 = OUTPUT_DIR / f"{capture.name}.mp4"
    adb("pull", device_mp4, str(local_mp4))
    adb("shell", "rm", "-f", device_mp4)

    destination = OUTPUT_DIR / f"{capture.name}.gif"
    convert_to_gif(local_mp4, destination)
    local_mp4.unlink(missing_ok=True)
    return destination


def convert_to_gif(source: Path, destination: Path) -> None:
    """Two-pass palette conversion.

    A single-pass GIF of a dark UI bands badly in the gradients this app is built from, and the
    white focus fill is exactly where banding is most visible.
    """
    scale = f"fps={GIF_FPS},scale={GIF_WIDTH}:-1:flags=lanczos"
    run(["ffmpeg", "-v", "error", "-y", "-i", str(source),
         "-vf", f"{scale},split[a][b];"
                f"[a]palettegen=max_colors={GIF_COLORS}:stats_mode=diff[p];"
                f"[b][p]paletteuse=dither=bayer:bayer_scale=4",
         "-loop", "0", str(destination)])


# Home is the launch destination, so its captures need no setup. Everything else is reached with
# the same key path a viewer would use, which keeps the captures honest about reachability.
CAPTURES: dict[str, Capture] = {
    "home-overview": Capture(
        name="home-overview",
        description="Home with the hero banner focused",
        settle=6.0,
    ),
    "home-rows": Capture(
        name="home-rows",
        description="Content rails below the hero",
        steps=["DPAD_DOWN", "DPAD_DOWN"],
        settle=6.0,
    ),
    # Section order from the top: Banner, Videos, Videos Popular, Series, Channels, Vertical Banner,
    # Shorts, Shorts Popular.
    "home-vertical-banner": Capture(
        name="home-vertical-banner",
        description="Portrait carousel section",
        steps=["DPAD_DOWN"] * 5,
        settle=6.0,
    ),
    "home-series": Capture(
        name="home-series",
        description="Series rail with landscape episode cards",
        steps=["DPAD_DOWN"] * 3,
        settle=6.0,
    ),
    "home-shorts": Capture(
        name="home-shorts",
        description="Shorts rail with portrait cards",
        steps=["DPAD_DOWN"] * 6,
        settle=6.0,
    ),
    "home-channels": Capture(
        name="home-channels",
        description="Live channel row",
        steps=["DPAD_DOWN"] * 4,
        settle=6.0,
    ),
    "topbar-focus": Capture(
        name="topbar-focus",
        description="Top bar items expanding as focus moves across them",
        setup=["sleep:5"],
        steps=["DPAD_UP", "DPAD_LEFT", "DPAD_LEFT", "DPAD_LEFT", "DPAD_RIGHT", "DPAD_RIGHT"],
        settle=6.0,
        step_delay=0.9,
        duration=10,
    ),
    "home-banner-trailer": Capture(
        name="home-banner-trailer",
        description="Hero banner handing over from artwork to its muted trailer",
        setup=["sleep:2", "DPAD_RIGHT", "DPAD_RIGHT", "DPAD_RIGHT"],
        steps=[],
        settle=2.0,
        duration=13,
    ),
    "home-row-navigation": Capture(
        name="home-row-navigation",
        description="Moving down through rails and along a rail",
        setup=["sleep:5"],
        steps=["DPAD_DOWN", "DPAD_RIGHT", "DPAD_RIGHT", "DPAD_DOWN", "DPAD_RIGHT", "DPAD_RIGHT"],
        settle=6.0,
        step_delay=0.9,
        duration=10,
    ),
    # Top-bar order is Search, Home, Calendar, Setting, Profile, and Home is selected on launch.
    # Each of these ends on Down: selecting a destination leaves focus on the top bar, which dims the
    # content behind it, and a dimmed screen is the wrong thing to put in a showcase.
    "search": Capture(
        name="search",
        description="Search destination",
        setup=["sleep:5", "DPAD_UP"],
        steps=["DPAD_LEFT", "DPAD_CENTER", "DPAD_DOWN"],
        settle=6.0,
    ),
    "calendar": Capture(
        name="calendar",
        description="Calendar destination",
        setup=["sleep:5", "DPAD_UP"],
        steps=["DPAD_RIGHT", "DPAD_CENTER", "DPAD_DOWN"],
        settle=6.0,
    ),
    "setting": Capture(
        name="setting",
        description="Two-pane Settings",
        setup=["sleep:5", "DPAD_UP"],
        steps=["DPAD_RIGHT", "DPAD_RIGHT", "DPAD_CENTER", "DPAD_DOWN"],
        settle=6.0,
    ),
    "profile": Capture(
        name="profile",
        description="Profile destination",
        setup=["sleep:5", "DPAD_UP"],
        steps=["DPAD_RIGHT", "DPAD_RIGHT", "DPAD_RIGHT", "DPAD_CENTER", "DPAD_DOWN"],
        settle=6.0,
    ),
    "player-surface": Capture(
        name="player-surface",
        description="Landscape playback with no chrome at all",
        setup=["sleep:5", "DPAD_RIGHT", "DPAD_RIGHT", "DPAD_CENTER"],
        steps=["sleep:14"],
        settle=2.0,
    ),
    "player-controller": Capture(
        name="player-controller",
        description="Landscape player with the controller revealed",
        setup=["sleep:5", "DPAD_RIGHT", "DPAD_RIGHT", "DPAD_CENTER", "sleep:14"],
        steps=["DPAD_UP"],
        settle=6.0,
    ),
    "player-focus-restore": Capture(
        name="player-focus-restore",
        description="Control row focus, up to the seek bar, and back to the same control",
        setup=["sleep:5", "DPAD_RIGHT", "DPAD_RIGHT", "DPAD_CENTER", "sleep:14", "DPAD_UP"],
        steps=["DPAD_RIGHT", "DPAD_RIGHT", "DPAD_RIGHT", "DPAD_UP", "DPAD_DOWN"],
        settle=6.0,
        step_delay=1.1,
        duration=10,
    ),
    "player-metadata-section": Capture(
        name="player-metadata-section",
        description="Metadata section opened from the Description pill",
        setup=["sleep:5", "DPAD_RIGHT", "DPAD_RIGHT", "DPAD_CENTER", "sleep:14", "DPAD_UP"],
        steps=["DPAD_LEFT", "DPAD_LEFT", "DPAD_CENTER", "sleep:2"],
        settle=6.0,
    ),
    "player-comments-section": Capture(
        name="player-comments-section",
        description="Comments section with its D-pad scroll viewport",
        setup=["sleep:5", "DPAD_RIGHT", "DPAD_RIGHT", "DPAD_CENTER", "sleep:14", "DPAD_UP"],
        steps=["DPAD_RIGHT", "DPAD_RIGHT", "DPAD_RIGHT", "DPAD_RIGHT", "DPAD_CENTER", "sleep:2"],
        settle=6.0,
    ),
    "player-settings-section": Capture(
        name="player-settings-section",
        description="Quality list reached from the settings section",
        setup=["sleep:5", "DPAD_RIGHT", "DPAD_RIGHT", "DPAD_CENTER", "sleep:14", "DPAD_UP"],
        steps=["DPAD_RIGHT", "DPAD_RIGHT", "DPAD_RIGHT", "DPAD_RIGHT", "DPAD_RIGHT", "DPAD_CENTER",
                   "sleep:2", "DPAD_CENTER", "sleep:2"],
        settle=6.0,
    ),
    "vertical-player": Capture(
        name="vertical-player",
        description="Portrait player stage and interaction panel",
        setup=["sleep:5"] + ["DPAD_DOWN"] * 6 + ["DPAD_CENTER"],
        steps=["sleep:18"],
        settle=6.0,
    ),
    "vertical-player-metadata": Capture(
        name="vertical-player-metadata",
        description="Portrait section drawn transparent over the ambient gradient",
        setup=["sleep:5"] + ["DPAD_DOWN"] * 6 + ["DPAD_CENTER", "sleep:18"],
        steps=["DPAD_RIGHT", "DPAD_UP", "DPAD_CENTER", "sleep:2"],
        settle=6.0,
    ),
    "vertical-player-panel": Capture(
        name="vertical-player-panel",
        description="Moving from the portrait stage into the action panel",
        setup=["sleep:5"] + ["DPAD_DOWN"] * 6 + ["DPAD_CENTER", "sleep:18"],
        steps=["DPAD_RIGHT", "DPAD_UP", "DPAD_DOWN", "DPAD_RIGHT", "DPAD_RIGHT", "DPAD_LEFT"],
        settle=6.0,
        step_delay=1.1,
        duration=10,
    ),
}

GIF_CAPTURES = {
    "topbar-focus",
    "home-row-navigation",
    "home-banner-trailer",
    "player-focus-restore",
    "vertical-player-panel",
}


def require_tools() -> None:
    missing = [tool for tool in ("adb", "ffmpeg") if shutil.which(tool) is None]
    if missing:
        sys.exit(f"Missing required tool(s): {', '.join(missing)}")

    devices = run(["adb", "devices"]).stdout.decode()
    attached = [line for line in devices.splitlines()[1:] if line.strip().endswith("device")]
    if len(attached) != 1:
        sys.exit(f"Expected exactly one attached device, found {len(attached)}")


def capture_one(name: str) -> None:
    capture = CAPTURES[name]
    kind = "gif" if name in GIF_CAPTURES else "shot"
    print(f"  {kind:4}  {name} — {capture.description}")
    path = take_gif(capture) if kind == "gif" else take_screenshot(capture)
    print(f"        -> {path.relative_to(OUTPUT_DIR.parent.parent)}")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", choices=["list", "shot", "gif", "all"])
    parser.add_argument("name", nargs="?")
    args = parser.parse_args()

    if args.command == "list":
        for name, capture in CAPTURES.items():
            kind = "gif" if name in GIF_CAPTURES else "shot"
            print(f"{kind:4}  {name:26}  {capture.description}")
        return

    require_tools()

    if args.command == "all":
        for name in CAPTURES:
            capture_one(name)
        return

    if not args.name:
        sys.exit("A capture name is required. Run `list` to see them.")
    if args.name not in CAPTURES:
        sys.exit(f"Unknown capture '{args.name}'. Run `list` to see them.")

    capture = CAPTURES[args.name]
    path = take_gif(capture) if args.command == "gif" else take_screenshot(capture)
    print(path)


if __name__ == "__main__":
    main()
