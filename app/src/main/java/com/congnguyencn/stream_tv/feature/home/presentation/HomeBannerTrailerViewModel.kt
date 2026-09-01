package com.congnguyencn.stream_tv.feature.home.presentation

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.congnguyencn.stream_tv.core.player.StreamTvPlayerFactory
import com.congnguyencn.stream_tv.feature.home.presentation.model.VideoUiItem
import com.congnguyencn.streamplayer.StreamTvPlayerManager
import com.congnguyencn.streamplayer.clear
import com.congnguyencn.streamplayer.loadAndPlay
import com.congnguyencn.streamplayer.model.StreamTvPlaybackState
import com.congnguyencn.streamplayer.model.StreamTvPlayerState
import com.congnguyencn.streamplayer.replay
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

/**
 * Plays the focused banner item's trailer, on loop, with no controls.
 *
 * Its own ViewModel rather than a branch of [HomeViewModel]: a player holds a decoder and several
 * threads, so it needs an owner with a lifetime and a `close()`, and [HomeViewModel] has neither
 * reason nor means to manage one. Keeping it separate also means the banner is the only thing that has
 * to change if the trailer surface moves or goes away.
 *
 * The player outlives the banner composable on purpose. Scrolling the banner out of the lazy column
 * disposes the surface, and rebuilding a player each time it scrolls back would pay for a fresh
 * decoder handshake to show the same frames. The session is stopped instead ([stopTrailer]) and the
 * same player is reused.
 *
 * Every decision about what the banner shows lives in [reducePlayback], which needs no player and is
 * tested on its own. What is left here is the wiring: create, load, loop, unload, close.
 */
@HiltViewModel
internal class HomeBannerTrailerViewModel @Inject constructor(playerFactory: StreamTvPlayerFactory) : ViewModel() {

  /**
   * Exposed so the banner's Compose surface can bind to it.
   *
   * The surface receives this value directly rather than the ViewModel, so it stays previewable and
   * holds no reference to presentation logic.
   */
  val playerManager: StreamTvPlayerManager = playerFactory.create()

  private val _uiState = MutableStateFlow(HomeBannerTrailerUiState.Initial)
  val uiState: StateFlow<HomeBannerTrailerUiState> = _uiState.asStateFlow()

  /**
   * The item this session is playing, or null while stopped.
   *
   * Not UI state — nothing renders it. It is what tells [reducePlayback] that a playback event belongs
   * to a session that is already over.
   */
  private var trailerItemId: String? = null

  init {
    observePlayback()
  }

  private fun observePlayback() {
    playerManager.playerState
      // Position ticks arrive twice a second while playing and change nothing here.
      .distinctUntilChangedBy { playerState ->
        TrailerPlaybackKey(
          playbackState = playerState.playbackState,
          isPlaying = playerState.isPlaying,
          hasFailed = playerState.error != null,
        )
      }
      .onEach(::handlePlayback)
      .launchIn(viewModelScope)
  }

  /**
   * Starts [item]'s trailer from the beginning.
   *
   * A blank `trailerUrl` is a no-op: the banner simply keeps the thumbnail, which is the same outcome
   * as a trailer that fails, so there is nothing for the caller to branch on.
   */
  fun startTrailer(item: VideoUiItem) {
    if (item.trailerUrl.isBlank()) return
    trailerItemId = item.id
    emitState { HomeBannerTrailerUiState.Initial }
    playerManager.loadAndPlay(uri = item.trailerUrl.toUri())
  }

  /**
   * Ends the session and unloads the stream.
   *
   * Unloading rather than pausing, because the banner has nothing to resume to: focus leaving means
   * the next session starts from the top of whichever item is focused then. It also stops a decoder
   * from holding a stream nobody is watching while the viewer browses the rows below.
   */
  fun stopTrailer() {
    trailerItemId = null
    emitState { HomeBannerTrailerUiState.Initial }
    playerManager.clear()
  }

  private fun handlePlayback(playerState: StreamTvPlayerState) {
    val decision = _uiState.value.reducePlayback(
      isSessionActive = trailerItemId != null,
      playbackState = playerState.playbackState,
      isPlaying = playerState.isPlaying,
      hasFailed = playerState.error != null,
    )

    emitState { decision.uiState }
    if (decision.shouldReplayTrailer) {
      playerManager.replay()
    }
  }

  private inline fun emitState(update: (HomeBannerTrailerUiState) -> HomeBannerTrailerUiState) = _uiState.update(update)

  override fun onCleared() {
    playerManager.close()
  }
}

/** The only three things [reducePlayback] reads from a player state, so the rest can be conflated away. */
private data class TrailerPlaybackKey(
  val playbackState: StreamTvPlaybackState,
  val isPlaying: Boolean,
  val hasFailed: Boolean,
)
