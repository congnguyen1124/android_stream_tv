package com.congnguyencn.stream_tv.feature.player.presentation

import com.congnguyencn.streamplayer.StreamTvPlayerManager

/**
 * Builds the player a [PlayerViewModel] owns.
 *
 * An interface rather than calling `StreamTvPlayerManager.create` inside the ViewModel: creating a
 * real player needs a `Context`, a decoder and a surface, none of which a unit test can supply. With
 * the seam here, a test binds a fake and the ViewModel's state mapping and command dispatch become
 * testable without a device.
 */
internal interface StreamTvPlayerFactory {
  /**
   * Returns a new player, configured for this app's surface.
   *
   * The caller owns it and must call `close()`.
   */
  fun create(): StreamTvPlayerManager
}
