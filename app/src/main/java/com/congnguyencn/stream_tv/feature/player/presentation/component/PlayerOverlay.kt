package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.component.StreamTvButton
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import com.congnguyencn.stream_tv.feature.player.presentation.PlayerErrorUiItem
import kotlin.time.Duration

/**
 * Pieces both player screens share.
 *
 * The two screens differ in how they frame the video, not in what they say about it — so the badge,
 * the spinner, the progress bar and the error panel live here rather than being written twice with a
 * chance to drift apart.
 */
internal object PlayerOverlayDefaults {
  val PlaybackBadgeSize: Dp = 96.dp
  val PlaybackBadgeIconSize: Dp = 40.dp
  val SpinnerSize: Dp = 56.dp
  val SpinnerStrokeWidth: Dp = 4.dp
  const val SpinnerSweepDegrees = 90f
  const val FullTurnDegrees = 360f
  const val SpinnerRotationDurationMillis = 900
  val ProgressBarHeight: Dp = 4.dp
  val ProgressBarShape = RoundedCornerShape(2.dp)
  val ErrorPanelPadding: Dp = 48.dp
  val ErrorMessageSpacing: Dp = 20.dp
}

/**
 * The centred play/pause glyph.
 *
 * Shown only while paused. A permanent badge over playing video is the one overlay viewers
 * consistently ask to be rid of.
 */
@Composable
internal fun PlayerPlaybackBadge(isPlaying: Boolean, modifier: Modifier = Modifier) {
  val icon: ImageVector = if (isPlaying) {
    ImageVector.vectorResource(R.drawable.ic_pause)
  } else {
    ImageVector.vectorResource(R.drawable.ic_play)
  }
  val contentDescription = if (isPlaying) {
    stringResource(R.string.player_pause)
  } else {
    stringResource(R.string.player_play)
  }

  Box(
    modifier = modifier
      .size(PlayerOverlayDefaults.PlaybackBadgeSize)
      .clip(RoundedCornerShape(percent = 50))
      .background(StreamTvColors.TransparentBlack60)
      .testTag("player-playback-badge"),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      modifier = Modifier.size(PlayerOverlayDefaults.PlaybackBadgeIconSize),
      tint = StreamTvColors.NeutralWhite,
    )
  }
}

/**
 * An indeterminate spinner, drawn rather than imported.
 *
 * `androidx.tv.material3` ships no progress indicator, and pulling in `compose-material3` purely for
 * a rotating arc would add a second, differently-themed design system to the app.
 */
@Composable
internal fun PlayerBufferingIndicator(modifier: Modifier = Modifier) {
  val transition = rememberInfiniteTransition(label = "PlayerBuffering")
  val rotation by transition.animateFloat(
    initialValue = 0f,
    targetValue = PlayerOverlayDefaults.FullTurnDegrees,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = PlayerOverlayDefaults.SpinnerRotationDurationMillis,
        easing = LinearEasing,
      ),
      repeatMode = RepeatMode.Restart,
    ),
    label = "PlayerBufferingRotation",
  )

  Canvas(
    modifier = modifier
      .size(PlayerOverlayDefaults.SpinnerSize)
      .testTag("player-buffering"),
  ) {
    val strokeWidth = PlayerOverlayDefaults.SpinnerStrokeWidth.toPx()
    val inset = strokeWidth / 2f
    drawArc(
      color = StreamTvColors.TransparentWhite20,
      startAngle = 0f,
      sweepAngle = PlayerOverlayDefaults.FullTurnDegrees,
      useCenter = false,
      topLeft = Offset(inset, inset),
      size = Size(size.width - strokeWidth, size.height - strokeWidth),
      style = Stroke(width = strokeWidth),
    )
    drawArc(
      color = StreamTvColors.NeutralWhite,
      startAngle = rotation,
      sweepAngle = PlayerOverlayDefaults.SpinnerSweepDegrees,
      useCenter = false,
      topLeft = Offset(inset, inset),
      size = Size(size.width - strokeWidth, size.height - strokeWidth),
      style = Stroke(width = strokeWidth),
    )
  }
}

/**
 * A two-layer seek bar: buffered ahead behind, played position in front.
 *
 * @param progressFraction Played position in `0f..1f`.
 * @param bufferedFraction How far the buffer reaches, in `0f..1f`.
 */
@Composable
internal fun PlayerProgressBar(progressFraction: Float, bufferedFraction: Float, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(PlayerOverlayDefaults.ProgressBarHeight)
      .clip(PlayerOverlayDefaults.ProgressBarShape)
      .background(StreamTvColors.TransparentWhite20)
      .testTag("player-progress"),
  ) {
    // Each layer is skipped at zero rather than drawn with zero width: a Box aligns a zero-width
    // child, and an empty child still costs a measure pass on every progress tick.
    if (bufferedFraction > 0f) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .fillMaxWidth(fraction = bufferedFraction)
          .background(StreamTvColors.TransparentWhite40),
      )
    }
    if (progressFraction > 0f) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .fillMaxWidth(fraction = progressFraction)
          .background(StreamTvColors.Primary30),
      )
    }
  }
}

/** `elapsed / total`, or just the elapsed time when the total is unknown, as it is for live. */
@Composable
internal fun PlayerTimeLabel(position: Duration, duration: Duration, modifier: Modifier = Modifier) {
  val label = if (duration > Duration.ZERO) {
    "${position.toClockString()} / ${duration.toClockString()}"
  } else {
    position.toClockString()
  }

  Text(
    text = label,
    modifier = modifier,
    color = StreamTvColors.Neutral10,
    style = StreamTvTheme.typography.labelMedium,
  )
}

@Composable
internal fun PlayerLiveBadge(modifier: Modifier = Modifier) {
  Text(
    text = stringResource(R.string.player_live_badge),
    modifier = modifier
      .background(StreamTvColors.LiveBadge, RoundedCornerShape(4.dp))
      .padding(horizontal = 8.dp, vertical = 2.dp),
    color = StreamTvColors.NeutralWhite,
    style = StreamTvTheme.typography.labelMedium,
  )
}

/**
 * Replaces the video entirely on failure.
 *
 * @param onRetry Null when the library classified the failure as not worth retrying, in which case
 *   no button is offered rather than one that is guaranteed to fail again.
 */
@Composable
internal fun PlayerErrorPanel(error: PlayerErrorUiItem, onRetry: (() -> Unit)?, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(StreamTvColors.NeutralBlack)
      .padding(PlayerOverlayDefaults.ErrorPanelPadding)
      .testTag("player-error"),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = stringResource(error.messageResId),
      color = StreamTvColors.Neutral10,
      style = StreamTvTheme.typography.titleLarge,
      textAlign = TextAlign.Center,
      maxLines = 3,
      overflow = TextOverflow.Ellipsis,
    )
    if (onRetry != null) {
      Spacer(modifier = Modifier.height(PlayerOverlayDefaults.ErrorMessageSpacing))
      StreamTvButton(
        text = stringResource(R.string.player_retry),
        onClick = onRetry,
      )
    }
  }
}

/** Title plus an optional live badge, as the bottom bar of either screen renders it. */
@Composable
internal fun PlayerTitleRow(title: String, isLive: Boolean, modifier: Modifier = Modifier) {
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    if (isLive) {
      PlayerLiveBadge()
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
      text = title,
      color = StreamTvColors.NeutralWhite,
      style = StreamTvTheme.typography.titleLarge,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

private fun Duration.toClockString(): String = toComponents { hours, minutes, seconds, _ ->
  if (hours > 0) {
    "%d:%02d:%02d".format(hours, minutes, seconds)
  } else {
    "%d:%02d".format(minutes, seconds)
  }
}
