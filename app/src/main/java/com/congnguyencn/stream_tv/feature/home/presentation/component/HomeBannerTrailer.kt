package com.congnguyencn.stream_tv.feature.home.presentation.component

import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import com.congnguyencn.stream_tv.feature.home.presentation.HomeBannerTrailerViewModel
import com.congnguyencn.stream_tv.feature.home.presentation.model.VideoUiItem
import com.congnguyencn.streamplayer.StreamTvPlayerManager
import com.congnguyencn.streamplayer.ui.StreamTvPlayerSurface
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay

private object HomeBannerTrailerDefaults {
  /**
   * How long the thumbnail holds the banner before the trailer takes over.
   *
   * Long enough to read the title and the CTA — and long enough that arrowing through the carousel
   * with the D-pad costs no stream requests at all, because no item is held that long on the way past.
   */
  val StartDelay: Duration = 5.seconds

  /** Compose's own default tween, which is what the thumbnail cross-fade beside it uses. */
  const val FadeDurationMillis = 300
}

/**
 * The banner's trailer layer: chrome-less playback that fades in over the thumbnail.
 *
 * Nothing plays until [item] has held banner focus for [HomeBannerTrailerDefaults.StartDelay]. Losing
 * focus, moving to another banner item, opening a player, or backgrounding the app all end the
 * session, and the next one starts from the top of whichever item is focused then — a trailer is
 * ambient, so resuming one halfway would be stranger than replaying it.
 *
 * The thumbnail underneath is never removed. A missing or unplayable trailer therefore degrades to
 * exactly what the banner showed before this layer existed, with nothing to handle at the call site.
 *
 * Bound to [HomeBannerTrailerViewModel] here rather than in `HomeRoute` because playback is the
 * banner's own concern: hoisting it would thread a player, a flag and two callbacks through
 * `HomeContent` and `HomeSection`, neither of which has anything to do with video. The route supplies
 * this composable as a slot instead, so a banner with no trailer layer — previews, Compose tests — is
 * the default rather than a special case.
 */
@Composable
internal fun HomeBannerTrailer(
  item: VideoUiItem,
  isBannerFocused: Boolean,
  modifier: Modifier = Modifier,
  viewModel: HomeBannerTrailerViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
  val isScreenResumed = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

  LaunchedEffect(item, isBannerFocused, isScreenResumed) {
    if (!isBannerFocused || !isScreenResumed) return@LaunchedEffect
    delay(HomeBannerTrailerDefaults.StartDelay)
    try {
      viewModel.startTrailer(item = item)
      // Holds the session open until a key above changes or the banner leaves composition, which
      // makes this the one place a trailer is ever stopped.
      awaitCancellation()
    } finally {
      viewModel.stopTrailer()
    }
  }

  HomeBannerTrailerSurface(
    playerManager = viewModel.playerManager,
    isVisible = uiState.isTrailerRendering,
    modifier = modifier,
  )
}

/**
 * Fades the video in and out rather than adding and removing it.
 *
 * The surface stays attached for the banner's whole life so the decoder has somewhere to draw before
 * playback starts — attaching it only once frames are already advancing would drop the trailer's
 * opening second, which is the part the fade is meant to reveal. `PlayerView` with its controller off
 * is neither clickable nor focusable, so an always-present surface cannot take D-pad focus from the
 * carousel.
 */
@OptIn(UnstableApi::class)
@Composable
private fun HomeBannerTrailerSurface(
  playerManager: StreamTvPlayerManager,
  isVisible: Boolean,
  modifier: Modifier = Modifier,
) {
  val videoAlpha by animateFloatAsState(
    targetValue = if (isVisible) 1f else 0f,
    animationSpec = tween(durationMillis = HomeBannerTrailerDefaults.FadeDurationMillis),
    label = "HomeBannerTrailerAlpha",
  )

  StreamTvPlayerSurface(
    playerManager = playerManager,
    modifier = modifier
      .fillMaxSize()
      .graphicsLayer { alpha = videoAlpha }
      .testTag("home-banner-trailer"),
    // Transparent so what shows through before the first frame, and in the gap where the trailer
    // loops, is the thumbnail rather than a black rectangle.
    shutterColor = Color.Transparent,
    // Crop to fill, matching the thumbnail's ContentScale.Crop. Letterboxing inside a full-bleed
    // hero would lay black bars over the gradients the banner paints on top.
    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
    // A banner is not a viewing surface; subtitles here would compete with the title and the CTA.
    showSubtitles = false,
  )
}
