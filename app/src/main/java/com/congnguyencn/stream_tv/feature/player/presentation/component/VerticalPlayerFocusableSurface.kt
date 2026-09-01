package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import kotlinx.coroutines.delay

internal object VerticalPlayerFocusableSurfaceDefaults {
  const val FocusedBorderHoldMillis = 2_000L
  const val FocusedBorderFadeMillis = 1_000
  const val FocusedScale = 1f

  @Stable
  val Shape = RoundedCornerShape(16.dp)

  @Stable
  val FocusedBorderWidth = 6.dp

  @Stable
  val FocusedBorderInset = 2.dp
}

/**
 * Portrait-player focus surface whose bright inset border softens after focus is held for a moment.
 * Pressing the surface restarts the border animation, matching the visual feedback of the reference
 * player without scaling the video frame.
 */
@Composable
internal fun VerticalPlayerFocusableSurface(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  content: @Composable BoxScope.() -> Unit,
) {
  val isFocused by interactionSource.collectIsFocusedAsState()
  var hasHeldFocus by remember { mutableStateOf(false) }
  var focusDelayRestartKey by remember { mutableStateOf(false) }

  LaunchedEffect(isFocused, focusDelayRestartKey) {
    hasHeldFocus = false
    if (isFocused) {
      delay(VerticalPlayerFocusableSurfaceDefaults.FocusedBorderHoldMillis)
      hasHeldFocus = true
    }
  }

  val focusedBorderColor by animateColorAsState(
    targetValue = if (hasHeldFocus) {
      StreamTvColors.TransparentWhite20
    } else {
      StreamTvColors.NeutralWhite
    },
    animationSpec = tween(
      durationMillis = VerticalPlayerFocusableSurfaceDefaults.FocusedBorderFadeMillis,
    ),
    label = "VerticalPlayerFocusedBorderColor",
  )

  Surface(
    onClick = {
      focusDelayRestartKey = !focusDelayRestartKey
      onClick()
    },
    modifier = modifier,
    interactionSource = interactionSource,
    scale = ClickableSurfaceDefaults.scale(
      focusedScale = VerticalPlayerFocusableSurfaceDefaults.FocusedScale,
    ),
    shape = ClickableSurfaceDefaults.shape(VerticalPlayerFocusableSurfaceDefaults.Shape),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = StreamTvColors.Transparent,
      focusedContainerColor = StreamTvColors.Transparent,
      pressedContainerColor = StreamTvColors.Transparent,
    ),
    border = ClickableSurfaceDefaults.border(
      focusedBorder = Border(
        border = BorderStroke(
          width = VerticalPlayerFocusableSurfaceDefaults.FocusedBorderWidth,
          color = focusedBorderColor,
        ),
        shape = VerticalPlayerFocusableSurfaceDefaults.Shape,
        inset = VerticalPlayerFocusableSurfaceDefaults.FocusedBorderInset,
      ),
    ),
    content = content,
  )
}

@Preview(device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun VerticalPlayerFocusableSurfacePreview() {
  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  StreamTvTheme {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      VerticalPlayerFocusableSurface(
        onClick = {},
        modifier = Modifier
          .size(width = 280.dp, height = 500.dp)
          .focusRequester(focusRequester),
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(StreamTvColors.Primary100),
        )
      }
    }
  }
}
