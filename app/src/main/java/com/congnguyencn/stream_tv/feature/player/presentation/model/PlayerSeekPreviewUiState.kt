package com.congnguyencn.stream_tv.feature.player.presentation.model

import androidx.compose.runtime.Immutable

/**
 * The still frames the seek bar shows while the viewer scrubs.
 *
 * Frames are evenly spaced across the whole video, so a position is resolved by which of
 * [frameUrls] equal slices it falls in rather than by any stored timestamp — see
 * [com.congnguyencn.stream_tv.feature.player.domain.model.PlayerSeekPreview] for why.
 */
@Immutable
internal data class PlayerSeekPreviewUiState(val frameUrls: List<String>) {
  /** False until a frame strip has been fetched, which is when the seek bar draws no preview. */
  val isAvailable: Boolean get() = frameUrls.isNotEmpty()

  /**
   * The frame covering [fraction] of the video, or null when no strip is available.
   *
   * @param fraction Playback position in `0f..1f`; values outside are clamped rather than rejected,
   *   because a seek that overshoots the end still has to show the last frame.
   */
  fun frameUrlAt(fraction: Float): String? {
    if (frameUrls.isEmpty()) return null
    val slice = (fraction.coerceIn(0f, 1f) * frameUrls.size).toInt()
    return frameUrls[slice.coerceIn(0, frameUrls.lastIndex)]
  }

  companion object {
    val Empty = PlayerSeekPreviewUiState(frameUrls = emptyList())
  }
}
