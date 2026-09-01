package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme

private object PlayerBufferingIndicatorDefaults {
  val Size: Dp = 56.dp

  //region Fallback arc — see PlayerBufferingIndicator for why one exists at all
  val FallbackStrokeWidth: Dp = 4.dp
  const val FallbackSweepDegrees = 90f
  const val FallbackRotationDurationMillis = 900
  const val FullTurnDegrees = 360f
  //endregion
}

/**
 * The buffering spinner, played from `R.raw.loading_lottie`.
 *
 * The animation file is the single source of truth for how the spinner looks: nothing here tints,
 * scales or re-times it, so restyling the spinner means replacing that raw resource and nothing else.
 *
 * A composition that is not available — the frames before the file finishes parsing, or a malformed
 * animation — falls back to a drawn arc. This indicator is the only thing on screen telling the
 * viewer that playback is still coming, so it must never be an empty box.
 */
@Composable
internal fun PlayerBufferingIndicator(modifier: Modifier = Modifier) {
  val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.loading_lottie))
  val loadedComposition = composition

  Box(
    modifier = modifier
      .size(PlayerBufferingIndicatorDefaults.Size)
      .testTag("player-buffering"),
    contentAlignment = Alignment.Center,
  ) {
    if (loadedComposition == null) {
      PlayerBufferingFallbackArc(modifier = Modifier.fillMaxSize())
    } else {
      LottieAnimation(
        composition = loadedComposition,
        modifier = Modifier.fillMaxSize(),
        iterations = LottieConstants.IterateForever,
      )
    }
  }
}

@Composable
private fun PlayerBufferingFallbackArc(modifier: Modifier = Modifier) {
  val transition = rememberInfiniteTransition(label = "PlayerBufferingFallback")
  val rotation by transition.animateFloat(
    initialValue = 0f,
    targetValue = PlayerBufferingIndicatorDefaults.FullTurnDegrees,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = PlayerBufferingIndicatorDefaults.FallbackRotationDurationMillis,
        easing = LinearEasing,
      ),
      repeatMode = RepeatMode.Restart,
    ),
    label = "PlayerBufferingFallbackRotation",
  )

  Canvas(modifier = modifier) {
    val strokeWidth = PlayerBufferingIndicatorDefaults.FallbackStrokeWidth.toPx()
    val inset = strokeWidth / 2f
    drawArc(
      color = StreamTvColors.TransparentWhite20,
      startAngle = 0f,
      sweepAngle = PlayerBufferingIndicatorDefaults.FullTurnDegrees,
      useCenter = false,
      topLeft = Offset(inset, inset),
      size = Size(size.width - strokeWidth, size.height - strokeWidth),
      style = Stroke(width = strokeWidth),
    )
    drawArc(
      color = StreamTvColors.NeutralWhite,
      startAngle = rotation,
      sweepAngle = PlayerBufferingIndicatorDefaults.FallbackSweepDegrees,
      useCenter = false,
      topLeft = Offset(inset, inset),
      size = Size(size.width - strokeWidth, size.height - strokeWidth),
      style = Stroke(width = strokeWidth),
    )
  }
}

@Preview(device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PlayerBufferingIndicatorPreview() {
  StreamTvTheme {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      PlayerBufferingIndicator()
    }
  }
}

@Preview(device = Devices.TV_720p, showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PlayerBufferingFallbackArcPreview() {
  StreamTvTheme {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      PlayerBufferingFallbackArc(modifier = Modifier.size(PlayerBufferingIndicatorDefaults.Size))
    }
  }
}
