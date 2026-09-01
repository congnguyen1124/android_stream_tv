package com.congnguyencn.stream_tv.feature.home.presentation

import androidx.compose.runtime.Immutable
import com.congnguyencn.streamplayer.model.StreamTvPlaybackState

/**
 * What the banner needs to know about its trailer, which is only ever "is there a picture yet".
 *
 * Deliberately not "is the player playing": the banner keeps the thumbnail underneath the video for
 * the whole session, so the one thing it decides is whether to draw the video surface over it. Every
 * other playback detail — position, duration, tracks, retryability — belongs to the full-screen
 * player, and a banner that surfaced it would be growing controls it must not have.
 */
@Immutable
internal data class HomeBannerTrailerUiState(val isTrailerRendering: Boolean) {
  companion object {
    val Initial: HomeBannerTrailerUiState
      get() = HomeBannerTrailerUiState(isTrailerRendering = false)
  }
}

/**
 * The banner's answer to one playback snapshot: the state to render, and whether the trailer has to
 * start over.
 *
 * The restart is carried separately because it is an effect on the player rather than something the
 * viewer sees, and keeping it out of the fold is what lets the fold stay pure.
 */
@Immutable
internal data class HomeBannerTrailerDecision(val uiState: HomeBannerTrailerUiState, val shouldReplayTrailer: Boolean)

/**
 * Folds one playback snapshot into the banner's trailer state.
 *
 * Pure, and separate from [HomeBannerTrailerViewModel], because the rules worth getting right are all
 * here and none of them need a decoder: the thumbnail holds until frames actually advance, a failure
 * is final, the gap between repeats must not flash the thumbnail back on, and an event from a session
 * that has already been stopped must not put video over the next item's thumbnail.
 *
 * @param isSessionActive Whether a trailer is still wanted. False once focus has moved on, which the
 *   library's conflated state cannot express — its last event of a stopped session still arrives.
 * @param hasFailed Whether the player is reporting an error. Which error does not matter here: every
 *   one of them ends with the viewer looking at the thumbnail.
 */
internal fun HomeBannerTrailerUiState.reducePlayback(
  isSessionActive: Boolean,
  playbackState: StreamTvPlaybackState,
  isPlaying: Boolean,
  hasFailed: Boolean,
): HomeBannerTrailerDecision {
  if (!isSessionActive || hasFailed) {
    return HomeBannerTrailerDecision(
      uiState = HomeBannerTrailerUiState.Initial,
      shouldReplayTrailer = false,
    )
  }

  return when (playbackState) {
    // The trailer loops for as long as the item holds focus.
    is StreamTvPlaybackState.Ended -> HomeBannerTrailerDecision(
      uiState = this,
      shouldReplayTrailer = true,
    )

    is StreamTvPlaybackState.Ready -> HomeBannerTrailerDecision(
      uiState = if (isPlaying) copy(isTrailerRendering = true) else this,
      shouldReplayTrailer = false,
    )

    // Buffering deliberately does not hide the video: the gap between the last frame and the loop's
    // first one would otherwise flash the thumbnail back on every repeat.
    is StreamTvPlaybackState.Buffering, is StreamTvPlaybackState.Idle -> HomeBannerTrailerDecision(
      uiState = this,
      shouldReplayTrailer = false,
    )
  }
}
