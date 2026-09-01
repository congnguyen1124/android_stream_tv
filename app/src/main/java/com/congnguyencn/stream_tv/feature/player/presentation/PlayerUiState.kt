package com.congnguyencn.stream_tv.feature.player.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.feature.player.presentation.mapper.buildPlayerSettingsUiState
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSettingsUiState
import com.congnguyencn.streamplayer.model.StreamTvPlaybackError
import com.congnguyencn.streamplayer.model.StreamTvPlaybackState
import com.congnguyencn.streamplayer.model.StreamTvPlayerState
import kotlin.time.Duration

/**
 * Everything both player screens render, derived from the library's own snapshot.
 *
 * A separate type rather than passing `StreamTvPlayerState` straight to the UI: the screens need a
 * title the player never knows about, and errors have to become string resources, which is an app
 * concern the library deliberately leaves open.
 */
@Immutable
internal data class PlayerUiState(
  val title: String,
  val isPlaying: Boolean,
  val isBuffering: Boolean,
  val position: Duration,
  val duration: Duration,
  val bufferedPosition: Duration,
  val settings: PlayerSettingsUiState,
  val error: PlayerErrorUiItem?,
) {
  /**
   * Whether a seek bar makes sense at all.
   *
   * A live stream reports no duration, so a progress bar would sit permanently at zero and a seek
   * would have nowhere to land.
   */
  val isSeekable: Boolean get() = duration > Duration.ZERO

  /** Playback progress in `0f..1f`, or `0f` while the duration is still unknown. */
  val progressFraction: Float
    get() = if (isSeekable) {
      (position / duration).toFloat().coerceIn(0f, 1f)
    } else {
      0f
    }

  /** Buffered progress in `0f..1f`, for the trailing portion of the seek bar. */
  val bufferedFraction: Float
    get() = if (isSeekable) {
      (bufferedPosition / duration).toFloat().coerceIn(0f, 1f)
    } else {
      0f
    }

  companion object {
    val Initial: PlayerUiState
      get() = PlayerUiState(
        title = "",
        isPlaying = false,
        isBuffering = false,
        position = Duration.ZERO,
        duration = Duration.ZERO,
        bufferedPosition = Duration.ZERO,
        settings = PlayerSettingsUiState.Empty,
        error = null,
      )
  }
}

/**
 * A playback failure as the UI needs it: one line of copy, and whether offering a retry is honest.
 *
 * @property isRetryable Straight from the library's classification. Showing a retry button for a
 *   forbidden stream only invites the viewer to fail twice.
 */
@Immutable
internal data class PlayerErrorUiItem(@param:StringRes val messageResId: Int, val isRetryable: Boolean)

internal fun StreamTvPlayerState.toPlayerUiState(title: String): PlayerUiState = PlayerUiState(
  title = title,
  isPlaying = isPlaying,
  isBuffering = playbackState is StreamTvPlaybackState.Buffering,
  position = position,
  duration = duration,
  bufferedPosition = bufferedPosition,
  settings = buildPlayerSettingsUiState(
    audioTracks = audioTracks,
    textTracks = textTracks,
    videoTracks = videoTracks,
  ),
  error = playbackError?.toPlayerErrorUiItem(),
)

internal fun StreamTvPlaybackError.toPlayerErrorUiItem(): PlayerErrorUiItem = PlayerErrorUiItem(
  messageResId = when (this) {
    is StreamTvPlaybackError.NoNetwork -> R.string.player_error_no_network
    is StreamTvPlaybackError.NotFound -> R.string.player_error_not_found
    is StreamTvPlaybackError.NotEntitled -> R.string.player_error_not_entitled
    is StreamTvPlaybackError.UnsupportedFormat -> R.string.player_error_unsupported
    is StreamTvPlaybackError.Unknown -> R.string.player_error_generic
  },
  isRetryable = isRetryable,
)
