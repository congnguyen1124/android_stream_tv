package com.congnguyencn.stream_tv.feature.player.presentation.component

import com.congnguyencn.stream_tv.feature.player.presentation.PlayerUiState
import com.congnguyencn.stream_tv.feature.player.presentation.component.section.PlayerSectionNavigationState

/**
 * Which subtree owns D-pad focus. Exactly one at a time.
 *
 * Replaces the set of booleans the screens used to gate focus on — controller visible, section in
 * play, parking, error. Those could disagree: a section entering while the controller was still
 * marked visible left two subtrees each believing focus was theirs, and the resulting focus request
 * depended on which `LaunchedEffect` ran last. Naming the owner makes that state unrepresentable
 * and gives every gate one value to read.
 *
 * Mirrors the reference TV player's `PlayerFocusableGroup`.
 */
internal enum class PlayerFocusableGroup {
  /** The bare video surface. Any D-pad press reveals the controller. */
  Surface,

  /** The controller chrome: seek bar and control row. */
  Controller,

  /** A side section — metadata, comments or settings — owns focus. */
  Section,

  /**
   * Focus is held on an off-screen anchor for the length of a section transition.
   *
   * Without this, the focused control disappears mid-animation and Compose falls back to whatever
   * is spatially nearest — usually the video surface, which then swallows the next key press.
   */
  Parked,

  /** The error panel owns focus, so its retry button can be reached. */
  Error,
}

/**
 * Resolves the single focus owner.
 *
 * Order is the precedence: an error outranks everything, a transition outranks the section it is
 * animating, and a live section outranks the controller underneath it.
 *
 * Pure on purpose — the ordering is the part worth testing, and it needs no composition to check.
 */
internal fun resolvePlayerFocusableGroup(
  hasError: Boolean,
  isControllerVisible: Boolean,
  navigationState: PlayerSectionNavigationState,
): PlayerFocusableGroup = when {
  hasError -> PlayerFocusableGroup.Error
  navigationState.shouldParkFocus -> PlayerFocusableGroup.Parked
  navigationState.hasSectionInPlay -> PlayerFocusableGroup.Section
  isControllerVisible -> PlayerFocusableGroup.Controller
  else -> PlayerFocusableGroup.Surface
}

/**
 * Every control the landscape controller can hold focus on, in visual order.
 *
 * Kept as a closed set so the screen can name the control to restore when a section closes —
 * "reopen the controller on Settings" survives the controller subtree being destroyed and rebuilt,
 * which a `FocusRequester` alone does not.
 */
internal enum class PlayerControllerFocusTarget {
  /** The seek bar. Spans the full width above the control row. */
  Progress,

  /** Leading pill. Opens the metadata section. */
  Description,

  Rewind,
  PlayPause,
  Forward,

  Like,
  Save,
  Comment,
  Settings,
  ;

  companion object {
    /** The trailing cluster, in the order the lazy row lays them out. */
    val ActionTargets: List<PlayerControllerFocusTarget> = listOf(Like, Save, Comment)
  }
}

/** False when the target's control is not on screen for this state, so focus must not be sent to it. */
internal fun PlayerUiState.isControllerTargetAvailable(target: PlayerControllerFocusTarget): Boolean = when (target) {
  PlayerControllerFocusTarget.Progress,
  PlayerControllerFocusTarget.Rewind,
  PlayerControllerFocusTarget.Forward,
  -> isSeekable

  PlayerControllerFocusTarget.Settings -> settings.isAvailable

  PlayerControllerFocusTarget.Description,
  PlayerControllerFocusTarget.PlayPause,
  PlayerControllerFocusTarget.Like,
  PlayerControllerFocusTarget.Save,
  PlayerControllerFocusTarget.Comment,
  -> true
}

/**
 * Where focus lands when the controller opens with no prior selection.
 *
 * The primary transport control, matching how the reference UI opens: the viewer's most likely next
 * action is pause, and it sits dead centre where the eye already is.
 */
internal fun PlayerUiState.defaultControllerFocusTarget(): PlayerControllerFocusTarget =
  PlayerControllerFocusTarget.PlayPause
