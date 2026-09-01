package com.congnguyencn.stream_tv.feature.player.presentation

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.congnguyencn.stream_tv.feature.player.presentation.navigation.PlayerArgs
import com.congnguyencn.streamplayer.StreamTvPlayerManager
import com.congnguyencn.streamplayer.loadAndPlay
import com.congnguyencn.streamplayer.pause
import com.congnguyencn.streamplayer.play
import com.congnguyencn.streamplayer.prepare
import com.congnguyencn.streamplayer.seekBack
import com.congnguyencn.streamplayer.seekForward
import com.congnguyencn.streamplayer.togglePlayPause
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

/**
 * Drives both [com.congnguyencn.stream_tv.feature.player.presentation.component.PlayerScreen] and
 * [com.congnguyencn.stream_tv.feature.player.presentation.component.VerticalPlayerScreen].
 *
 * One ViewModel for both because orientation is a rendering decision, not a playback one: loading,
 * play/pause, seeking, error recovery and teardown are identical whether the video is 16:9 or 9:16.
 * The screens differ only in how they frame the surface and which controls they offer, so splitting
 * the ViewModel would mean maintaining the same playback logic twice.
 *
 * Each screen still gets its own instance — they are separate navigation destinations, so one player
 * per screen is exactly right.
 */
@HiltViewModel
internal class PlayerViewModel @Inject constructor(
  playerFactory: StreamTvPlayerFactory,
  savedStateHandle: SavedStateHandle,
) : ViewModel() {

  private val args = PlayerArgs.from(savedStateHandle)

  /**
   * Exposed so the Compose surface can bind to it.
   *
   * The screens receive this value directly rather than the ViewModel, so they stay previewable and
   * hold no reference to presentation logic.
   */
  val playerManager: StreamTvPlayerManager = playerFactory.create()

  private val mutableUiState = MutableStateFlow(PlayerUiState.Initial)
  val uiState: StateFlow<PlayerUiState> = mutableUiState.asStateFlow()

  init {
    observePlayerState()
    startPlayback()
  }

  private inline fun emitState(reduce: (PlayerUiState) -> PlayerUiState) = mutableUiState.update(reduce)

  private fun observePlayerState() {
    playerManager.playerState
      .onEach { state -> emitState { state.toPlayerUiState(title = args.title) } }
      .launchIn(viewModelScope)
  }

  private fun startPlayback() {
    playerManager.loadAndPlay(uri = args.videoUrl.toUri())
  }

  fun togglePlayPause() {
    playerManager.togglePlayPause()
  }

  fun seekForward() {
    playerManager.seekForward()
  }

  fun seekBack() {
    playerManager.seekBack()
  }

  /** Pauses without tearing down, for when the screen stops but the destination is still on the stack. */
  fun pausePlayback() {
    playerManager.pause()
  }

  fun resumePlayback() {
    playerManager.play()
  }

  /**
   * Re-prepares the current stream after a retryable failure.
   *
   * `prepare` is what clears the error, so it must run before `play` — playing a still-errored
   * player is a no-op and leaves the viewer looking at the same panel.
   */
  fun retryPlayback() {
    playerManager.prepare()
    playerManager.play()
  }

  override fun onCleared() {
    playerManager.close()
  }
}
