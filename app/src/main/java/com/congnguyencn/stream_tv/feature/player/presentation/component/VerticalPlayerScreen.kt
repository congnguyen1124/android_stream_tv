package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.feature.player.presentation.PlayerUiState
import com.congnguyencn.streamplayer.StreamTvPlayerManager
import com.congnguyencn.streamplayer.ui.StreamTvPlayerSurface

private object VerticalPlayerScreenDefaults {
  /** Portrait framing, matching how shorts are shot and how every feed presents them. */
  const val PortraitAspectRatio = 9f / 16f
  val StageShape = RoundedCornerShape(12.dp)
  val InfoPadding: Dp = 24.dp
  val InfoSpacing: Dp = 10.dp
}

/**
 * Portrait playback for shorts and the vertical banner.
 *
 * A TV panel is landscape, so a portrait video cannot fill it without either black bars down both
 * sides or a crop that throws away the top and bottom of the frame. This centres a 9:16 stage and
 * crops the video into it — the framing every shorts feed uses — leaving the rest of the panel dark
 * so the eye stays on the stage.
 */
@OptIn(UnstableApi::class)
@Composable
internal fun VerticalPlayerScreen(
  uiState: PlayerUiState,
  playerManager: StreamTvPlayerManager,
  onTogglePlayPause: () -> Unit,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val focusRequester = remember { FocusRequester() }
  val interactionSource = remember { MutableInteractionSource() }

  LaunchedEffect(focusRequester) {
    focusRequester.requestFocus()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(StreamTvColors.NeutralBlack)
      .testTag("vertical-player-screen"),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier
        .fillMaxHeight()
        .aspectRatio(VerticalPlayerScreenDefaults.PortraitAspectRatio)
        .clip(VerticalPlayerScreenDefaults.StageShape),
    ) {
      StreamTvPlayerSurface(
        playerManager = playerManager,
        modifier = Modifier.fillMaxSize(),
        // Crop rather than letterbox: the test streams are landscape, and fitting them inside a
        // portrait stage would leave the stage mostly empty.
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
      )

      when (val error = uiState.error) {
        null -> VerticalPlayerChrome(
          uiState = uiState,
          modifier = Modifier.fillMaxSize(),
        )

        else -> PlayerErrorPanel(
          error = error,
          onRetry = onRetry.takeIf { error.isRetryable },
          modifier = Modifier.fillMaxSize(),
        )
      }
    }

    Box(
      modifier = Modifier
        .fillMaxSize()
        .focusRequester(focusRequester)
        .onPreviewKeyEvent { event ->
          handlePlaybackKeyEvent(
            event = event,
            // Shorts are short: a seek step would overshoot the whole clip, so only play/pause is
            // bound and the seek keys stay free for a future item-to-item swipe.
            isSeekable = false,
            onTogglePlayPause = onTogglePlayPause,
            onSeekForward = {},
            onSeekBack = {},
          )
        }
        .focusable(interactionSource = interactionSource),
    )
  }
}

@Composable
private fun VerticalPlayerChrome(uiState: PlayerUiState, modifier: Modifier = Modifier) {
  Box(modifier = modifier) {
    if (uiState.isBuffering) {
      PlayerBufferingIndicator(modifier = Modifier.align(Alignment.Center))
    } else if (!uiState.isPlaying) {
      PlayerPlaybackBadge(
        isPlaying = false,
        modifier = Modifier.align(Alignment.Center),
      )
    }

    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(StreamTvColors.Transparent, StreamTvColors.TransparentBlack80),
          ),
        )
        .padding(VerticalPlayerScreenDefaults.InfoPadding),
      verticalArrangement = Arrangement.spacedBy(VerticalPlayerScreenDefaults.InfoSpacing),
    ) {
      PlayerTitleRow(
        title = uiState.title,
        isLive = false,
        modifier = Modifier.fillMaxWidth(),
      )
      if (uiState.isSeekable) {
        PlayerProgressBar(
          progressFraction = uiState.progressFraction,
          bufferedFraction = uiState.bufferedFraction,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}
