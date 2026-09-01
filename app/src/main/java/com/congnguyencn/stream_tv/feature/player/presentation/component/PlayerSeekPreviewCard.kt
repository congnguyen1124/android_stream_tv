package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvNetworkImage
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.model.PlayerSeekPreviewUiState

private object PlayerSeekPreviewDefaults {
  @Stable
  val CardWidth: Dp = 148.dp

  const val CardAspectRatio = 16f / 9f

  @Stable
  val CardShape: Shape = RoundedCornerShape(8.dp)

  @Stable
  val BorderWidth: Dp = 2.dp

  const val EnterMillis = 180
  const val ExitMillis = 140
  const val EnterScale = 0.88f
}

/**
 * The frame strip lane: one still, riding the seek position, drawn above the track.
 *
 * Occupies the full track width and slides the card inside it, so the card is flush with the track's
 * left edge at 0 and its right edge at 1 and never overhangs either — which is also why the offset
 * is over `maxWidth - CardWidth` rather than centred on the thumb.
 *
 * @param isVisible Whether the viewer is on the seek bar. The strip is a scrubbing aid, so it stays
 *   out of the way the rest of the time.
 */
@Composable
internal fun PlayerSeekPreviewLane(
  seekPreview: PlayerSeekPreviewUiState,
  progressFraction: Float,
  isVisible: Boolean,
  modifier: Modifier = Modifier,
) {
  val frameUrl = seekPreview.frameUrlAt(progressFraction)

  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val travel = (maxWidth - PlayerSeekPreviewDefaults.CardWidth).coerceAtLeast(0.dp)

    AnimatedVisibility(
      visible = isVisible && frameUrl != null,
      modifier = Modifier.offset(x = travel * progressFraction.coerceIn(0f, 1f)),
      enter = fadeIn(animationSpec = tween(PlayerSeekPreviewDefaults.EnterMillis)) +
        scaleIn(
          animationSpec = tween(PlayerSeekPreviewDefaults.EnterMillis),
          initialScale = PlayerSeekPreviewDefaults.EnterScale,
        ),
      exit = fadeOut(animationSpec = tween(PlayerSeekPreviewDefaults.ExitMillis)) +
        scaleOut(
          animationSpec = tween(PlayerSeekPreviewDefaults.ExitMillis),
          targetScale = PlayerSeekPreviewDefaults.EnterScale,
        ),
    ) {
      PlayerSeekPreviewCard(frameUrl = frameUrl.orEmpty())
    }
  }
}

@Composable
private fun PlayerSeekPreviewCard(frameUrl: String, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .width(PlayerSeekPreviewDefaults.CardWidth)
      .aspectRatio(PlayerSeekPreviewDefaults.CardAspectRatio)
      .clip(PlayerSeekPreviewDefaults.CardShape)
      .background(StreamTvColors.NeutralBlack)
      .border(
        width = PlayerSeekPreviewDefaults.BorderWidth,
        color = StreamTvColors.NeutralWhite,
        shape = PlayerSeekPreviewDefaults.CardShape,
      )
      .testTag("player-seek-preview"),
  ) {
    StreamTvNetworkImage(
      imageUrl = frameUrl,
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF102838)
@Composable
private fun PlayerSeekPreviewLaneStartPreview() {
  StreamTvTheme {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      PlayerSeekPreviewLane(
        seekPreview = PlayerSeekPreviewUiState(frameUrls = listOf("", "", "")),
        progressFraction = 0f,
        isVisible = true,
      )
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF102838)
@Composable
private fun PlayerSeekPreviewLaneMidPreview() {
  StreamTvTheme {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      PlayerSeekPreviewLane(
        seekPreview = PlayerSeekPreviewUiState(frameUrls = listOf("", "", "")),
        progressFraction = 0.5f,
        isVisible = true,
      )
    }
  }
}
