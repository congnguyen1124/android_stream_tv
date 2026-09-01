package com.congnguyencn.stream_tv.feature.player.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Pauses when the app leaves the foreground and resumes when it returns.
 *
 * The player library deliberately ignores lifecycle — a picture-in-picture or background-audio
 * surface wants to keep playing — so each screen states its own policy. A full-screen TV player has
 * no reason to keep decoding video nobody can see, but it also should not lose its position, so this
 * pauses rather than tearing down.
 */
@Composable
internal fun PausePlaybackWhenStopped(viewModel: PlayerViewModel) {
  val lifecycleOwner = LocalLifecycleOwner.current

  DisposableEffect(lifecycleOwner, viewModel) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_STOP -> viewModel.pausePlayback()
        Lifecycle.Event.ON_START -> viewModel.resumePlayback()
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}
