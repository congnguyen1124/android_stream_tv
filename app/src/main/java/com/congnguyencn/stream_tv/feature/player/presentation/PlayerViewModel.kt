package com.congnguyencn.stream_tv.feature.player.presentation

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.congnguyencn.stream_tv.feature.player.domain.model.PlayerDetailsRequest
import com.congnguyencn.stream_tv.feature.player.domain.repository.PlayerDetailsRepository
import com.congnguyencn.stream_tv.feature.player.presentation.mapper.toUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerDetailsUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingCategory
import com.congnguyencn.stream_tv.feature.player.presentation.navigation.PlayerArgs
import com.congnguyencn.streamplayer.StreamTvPlayerManager
import com.congnguyencn.streamplayer.loadAndPlay
import com.congnguyencn.streamplayer.pause
import com.congnguyencn.streamplayer.play
import com.congnguyencn.streamplayer.prepare
import com.congnguyencn.streamplayer.seekBack
import com.congnguyencn.streamplayer.seekForward
import com.congnguyencn.streamplayer.selectAudioTrack
import com.congnguyencn.streamplayer.selectTextTrack
import com.congnguyencn.streamplayer.selectVideoTrack
import com.congnguyencn.streamplayer.togglePlayPause
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

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
@Suppress("TooManyFunctions")
internal class PlayerViewModel @Inject constructor(
  playerFactory: StreamTvPlayerFactory,
  playerDetailsRepository: PlayerDetailsRepository,
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

  private val contentState = MutableStateFlow(
    PlayerContentState(
      details = playerDetailsRepository.getDetails(
        PlayerDetailsRequest(
          title = args.title,
          description = args.description,
          ageRestriction = args.ageRestriction,
        ),
      ).toUiState(),
    ),
  )

  val uiState: StateFlow<PlayerUiState> = combine(
    playerManager.playerState,
    contentState,
  ) { playerState, contentState ->
    playerState.toPlayerUiState(
      title = args.title,
      details = contentState.details,
      isLiked = contentState.isLiked,
      isSaved = contentState.isSaved,
    )
  }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = PlayerUiState.Initial.copy(
        title = args.title,
        details = contentState.value.details,
      ),
    )

  init {
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

  fun selectTrack(category: PlayerSettingCategory, id: String) {
    when (category) {
      PlayerSettingCategory.Quality -> playerManager.selectVideoTrack(id)
      PlayerSettingCategory.Subtitles -> playerManager.selectTextTrack(id)
      PlayerSettingCategory.Audio -> playerManager.selectAudioTrack(id)
    }
  }

  fun toggleLike() {
    contentState.value = contentState.value.copy(isLiked = !contentState.value.isLiked)
  }

  fun toggleSaved() {
    contentState.value = contentState.value.copy(isSaved = !contentState.value.isSaved)
  }

  fun toggleCommentLike(commentId: Long) {
    contentState.value = contentState.value.copy(
      details = contentState.value.details.toggleLike(commentId),
    )
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

private data class PlayerContentState(
  val details: PlayerDetailsUiState,
  val isLiked: Boolean = false,
  val isSaved: Boolean = false,
)
