package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.PlayerUiState

internal object PlayerSeekBarDefaults {
  @Stable
  val ControlHeight: Dp = 40.dp

  @Stable
  val TrackHeight: Dp = 20.dp

  @Stable
  val ThumbSize: Dp = 14.dp

  @Stable
  val ThumbIdleSize: Dp = 10.dp

  const val ThumbResizeMillis = 140
}

/**
 * The seek bar: track, thumb, and the two time labels.
 *
 * A single focus target for the whole bar — the D-pad seeks in place rather than moving between
 * separate rewind and forward buttons, which is what the reference TV player does and what a remote
 * makes natural.
 */
@Composable
@Suppress("LongParameterList")
internal fun PlayerSeekBar(
  uiState: PlayerUiState,
  onTogglePlayPause: () -> Unit,
  onSeekForward: () -> Unit,
  onSeekBack: () -> Unit,
  onFocusChanged: (isFocused: Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()

  Surface(
    onClick = onTogglePlayPause,
    modifier = modifier
      .fillMaxWidth()
      .height(PlayerSeekBarDefaults.ControlHeight)
      .onFocusChanged { focusState -> onFocusChanged(focusState.hasFocus) }
      .onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

        when (event.key) {
          Key.DirectionLeft, Key.MediaRewind -> onSeekBack()
          Key.DirectionRight, Key.MediaFastForward -> onSeekForward()
          Key.MediaPlayPause -> onTogglePlayPause()
          else -> return@onPreviewKeyEvent false
        }
        true
      }
      .testTag("player-seek-control"),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = StreamTvColors.Transparent,
      focusedContainerColor = StreamTvColors.Transparent,
      pressedContainerColor = StreamTvColors.Transparent,
    ),
    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    interactionSource = interactionSource,
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
    ) {
      PlayerSeekTrack(
        progressFraction = uiState.progressFraction,
        bufferedFraction = uiState.bufferedFraction,
        isFocused = isFocused,
        modifier = Modifier.fillMaxWidth(),
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        PlayerSeekTimeLabel(text = uiState.position.coerceAtMost(uiState.duration).toClockString())
        PlayerSeekTimeLabel(text = uiState.duration.toClockString())
      }
    }
  }
}

@Composable
private fun PlayerSeekTrack(
  progressFraction: Float,
  bufferedFraction: Float,
  isFocused: Boolean,
  modifier: Modifier = Modifier,
) {
  val thumbSize by animateDpAsState(
    targetValue = if (isFocused) PlayerSeekBarDefaults.ThumbSize else PlayerSeekBarDefaults.ThumbIdleSize,
    animationSpec = tween(durationMillis = PlayerSeekBarDefaults.ThumbResizeMillis),
    label = "PlayerSeekThumbSize",
  )

  BoxWithConstraints(
    modifier = modifier.height(PlayerSeekBarDefaults.TrackHeight),
    contentAlignment = Alignment.CenterStart,
  ) {
    PlayerProgressBar(
      progressFraction = progressFraction,
      bufferedFraction = bufferedFraction,
      modifier = Modifier.fillMaxWidth(),
    )
    val travel = (maxWidth - thumbSize).coerceAtLeast(0.dp)
    Box(
      modifier = Modifier
        .offset(x = travel * progressFraction)
        .size(thumbSize)
        .background(StreamTvColors.NeutralWhite, CircleShape),
    )
  }
}

@Composable
private fun PlayerSeekTimeLabel(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text,
    modifier = modifier,
    color = StreamTvColors.Neutral10,
    style = StreamTvTheme.typography.labelMedium.copy(fontSize = 12.sp),
  )
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF102838)
@Composable
private fun PlayerSeekBarPreview() {
  StreamTvTheme {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      PlayerSeekBar(
        uiState = playerControllerPreviewUiState(),
        onTogglePlayPause = {},
        onSeekForward = {},
        onSeekBack = {},
        onFocusChanged = {},
      )
    }
  }
}
