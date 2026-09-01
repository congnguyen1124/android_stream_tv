package com.congnguyencn.stream_tv.feature.home.presentation.navigation

import com.congnguyencn.stream_tv.feature.home.presentation.model.ChannelUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.HomeContentUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.SeriesUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.ShortUiItem
import com.congnguyencn.stream_tv.feature.home.presentation.model.VideoUiItem

/**
 * Which player a home item opens in.
 *
 * The decision lives with the home models because it is a property of the content, not of the player:
 * a short is shot portrait and has to be framed that way, whatever screen plays it. Keeping it here
 * also means the player feature never has to know home's item types.
 */
internal enum class HomePlayerTarget {
  /** Landscape playback — videos, series episodes, live channels. */
  Horizontal,

  /** Portrait playback — shorts, which is also what the vertical banner section carries. */
  Vertical,
}

/**
 * Exhaustive on purpose: a new content type must be classified at compile time rather than silently
 * defaulting to landscape.
 */
internal fun HomeContentUiItem.playerTarget(): HomePlayerTarget = when (this) {
  is ShortUiItem -> HomePlayerTarget.Vertical
  is VideoUiItem, is SeriesUiItem, is ChannelUiItem -> HomePlayerTarget.Horizontal
}
