package com.congnguyencn.stream_tv.feature.home.presentation

import com.congnguyencn.streamplayer.model.StreamTvPlaybackState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeBannerTrailerTest {
  @Test
  fun `the thumbnail holds until frames actually advance`() {
    val buffering = HomeBannerTrailerUiState.Initial.reduce(
      playbackState = StreamTvPlaybackState.Buffering,
      isPlaying = false,
    )
    assertFalse(buffering.uiState.isTrailerRendering)

    // Ready alone is not enough: the surface would fade in over a frame the decoder has not produced.
    val ready = HomeBannerTrailerUiState.Initial.reduce(
      playbackState = StreamTvPlaybackState.Ready,
      isPlaying = false,
    )
    assertFalse(ready.uiState.isTrailerRendering)

    val playing = HomeBannerTrailerUiState.Initial.reduce(
      playbackState = StreamTvPlaybackState.Ready,
      isPlaying = true,
    )
    assertTrue(playing.uiState.isTrailerRendering)
  }

  @Test
  fun `a finished trailer replays without flashing the thumbnail back`() {
    val ended = rendering().reduce(
      playbackState = StreamTvPlaybackState.Ended,
      isPlaying = false,
    )

    assertTrue(ended.shouldReplayTrailer)
    assertTrue(ended.uiState.isTrailerRendering)
  }

  @Test
  fun `the loop's own buffering gap does not uncover the thumbnail`() {
    val restarting = rendering().reduce(
      playbackState = StreamTvPlaybackState.Buffering,
      isPlaying = false,
    )

    assertTrue(restarting.uiState.isTrailerRendering)
    assertFalse(restarting.shouldReplayTrailer)
  }

  @Test
  fun `a failure uncovers the thumbnail and is not retried`() {
    val failed = rendering().reducePlayback(
      isSessionActive = true,
      playbackState = StreamTvPlaybackState.Idle,
      isPlaying = false,
      hasFailed = true,
    )

    assertEquals(HomeBannerTrailerUiState.Initial, failed.uiState)
    assertFalse(failed.shouldReplayTrailer)
  }

  @Test
  fun `playback from a stopped session cannot bring the video back`() {
    // The library conflates state, so the last event of a stopped session still arrives after the
    // stop. Acting on it would put the previous item's video over the newly focused item's thumbnail.
    val afterStop = rendering().reducePlayback(
      isSessionActive = false,
      playbackState = StreamTvPlaybackState.Ready,
      isPlaying = true,
      hasFailed = false,
    )

    assertEquals(HomeBannerTrailerUiState.Initial, afterStop.uiState)
    assertFalse(afterStop.shouldReplayTrailer)
  }

  @Test
  fun `an ended trailer from a stopped session is not replayed`() {
    val afterStop = rendering().reducePlayback(
      isSessionActive = false,
      playbackState = StreamTvPlaybackState.Ended,
      isPlaying = false,
      hasFailed = false,
    )

    assertFalse(afterStop.shouldReplayTrailer)
  }

  private fun rendering() = HomeBannerTrailerUiState(isTrailerRendering = true)

  /** The healthy path: a live session with no error, which is most of what this fold decides. */
  private fun HomeBannerTrailerUiState.reduce(playbackState: StreamTvPlaybackState, isPlaying: Boolean) =
    reducePlayback(
      isSessionActive = true,
      playbackState = playbackState,
      isPlaying = isPlaying,
      hasFailed = false,
    )
}
