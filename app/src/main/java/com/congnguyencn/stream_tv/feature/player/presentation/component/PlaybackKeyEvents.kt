package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/**
 * Maps a remote key press to a playback action.
 *
 * A plain function rather than a lambda inside the screen: both player screens dispatch the same keys,
 * and keeping it out of the composable makes the mapping unit-testable without a player or a surface.
 *
 * @param isSeekable False for a live stream, where the seek keys are consumed but do nothing — better
 *   than letting them fall through and move focus off a full-screen player.
 * @return true when the event was consumed.
 */
internal fun handlePlaybackKeyEvent(
  event: KeyEvent,
  isSeekable: Boolean,
  onTogglePlayPause: () -> Unit,
  onSeekForward: () -> Unit,
  onSeekBack: () -> Unit,
): Boolean {
  if (event.type != KeyEventType.KeyDown) {
    return false
  }

  return when (event.key) {
    Key.DirectionCenter, Key.Enter, Key.NumPadEnter, Key.Spacebar -> {
      onTogglePlayPause()
      true
    }

    Key.MediaPlayPause -> {
      onTogglePlayPause()
      true
    }

    Key.DirectionRight, Key.MediaFastForward -> {
      if (isSeekable) {
        onSeekForward()
      }
      true
    }

    Key.DirectionLeft, Key.MediaRewind -> {
      if (isSeekable) {
        onSeekBack()
      }
      true
    }

    // Everything else — Back above all — must stay unconsumed so navigation still works.
    else -> false
  }
}
