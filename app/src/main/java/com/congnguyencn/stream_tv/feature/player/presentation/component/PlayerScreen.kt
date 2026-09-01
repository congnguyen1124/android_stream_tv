package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

private object PlayerScreenDefaults {
  val BottomBarHeight: Dp = 200.dp
  val BottomBarPadding: Dp = 48.dp
  val TitleSpacing: Dp = 12.dp
}

/**
 * Landscape playback for videos, series episodes and live channels.
 *
 * Letterboxes the video so a 16:9 film keeps its framing on a 16:9 panel. The overlay is deliberately
 * sparse — a lean-back viewer holds a remote, not a pointer, so the D-pad handles everything and the
 * chrome only reports state.
 *
 * @param onTogglePlayPause D-pad centre.
 * @param onSeekForward D-pad right. Ignored on a live stream, which has nowhere to seek to.
 * @param onSeekBack D-pad left.
 */
@OptIn(UnstableApi::class)
@Composable
internal fun PlayerScreen(
  uiState: PlayerUiState,
  playerManager: StreamTvPlayerManager,
  onTogglePlayPause: () -> Unit,
  onSeekForward: () -> Unit,
  onSeekBack: () -> Unit,
  onRetry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val focusRequester = remember { FocusRequester() }
  val interactionSource = remember { MutableInteractionSource() }

  // The remote has no other target on this screen, so playback must own focus the moment it opens or
  // the first D-pad press goes nowhere.
  LaunchedEffect(focusRequester) {
    focusRequester.requestFocus()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(StreamTvColors.NeutralBlack)
      .testTag("player-screen"),
  ) {
    StreamTvPlayerSurface(
      playerManager = playerManager,
      modifier = Modifier.fillMaxSize(),
      resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT,
    )

    Box(
      modifier = Modifier
        .fillMaxSize()
        .focusRequester(focusRequester)
        .onPreviewKeyEvent { event ->
          handlePlaybackKeyEvent(
            event = event,
            isSeekable = uiState.isSeekable,
            onTogglePlayPause = onTogglePlayPause,
            onSeekForward = onSeekForward,
            onSeekBack = onSeekBack,
          )
        }
        .focusable(interactionSource = interactionSource),
    )

    when (val error = uiState.error) {
      null -> PlayerScreenChrome(
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
}

/** Buffering spinner, paused badge, and the bottom bar with title, seek bar and elapsed time. */
@Composable
private fun PlayerScreenChrome(uiState: PlayerUiState, modifier: Modifier = Modifier) {
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
        .height(PlayerScreenDefaults.BottomBarHeight)
        .background(
          Brush.verticalGradient(
            colors = listOf(StreamTvColors.Transparent, StreamTvColors.TransparentBlack80),
          ),
        )
        .padding(PlayerScreenDefaults.BottomBarPadding),
      verticalArrangement = Arrangement.Bottom,
    ) {
      PlayerTitleRow(
        title = uiState.title,
        isLive = !uiState.isSeekable,
        modifier = Modifier.fillMaxWidth(),
      )
      Column(verticalArrangement = Arrangement.spacedBy(PlayerScreenDefaults.TitleSpacing)) {
        if (uiState.isSeekable) {
          PlayerProgressBar(
            progressFraction = uiState.progressFraction,
            bufferedFraction = uiState.bufferedFraction,
            modifier = Modifier.fillMaxWidth(),
          )
        }
        PlayerTimeLabel(
          position = uiState.position,
          duration = uiState.duration,
        )
      }
    }
  }
}
