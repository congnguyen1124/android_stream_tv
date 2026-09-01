package com.congnguyencn.stream_tv.core.player

import com.congnguyencn.streamplayer.StreamTvPlayerManager

/**
 * Builds the players this app plays video with.
 *
 * An interface rather than calling `StreamTvPlayerManager.create` inside a ViewModel: creating a real
 * player needs a `Context`, a decoder and a surface, none of which a unit test can supply. With the
 * seam here, a test binds a fake and a ViewModel's state mapping and command dispatch become testable
 * without a device.
 *
 * Lives in `core` because more than one feature plays video — the full-screen player screens and
 * Home's banner trailer — and neither should have to reach into the other's package for the seam.
 */
internal interface StreamTvPlayerFactory {
  /**
   * Returns a new player, configured for this app's surface.
   *
   * The caller owns it and must call `close()`.
   */
  fun create(): StreamTvPlayerManager
}
