package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.tv.material3.Icon
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvTheme
import androidx.compose.foundation.shape.CircleShape

private object PlayerPlaybackIndicatorDefaults {
  @Stable
  val BadgeSize: Dp = 92.dp

  @Stable
  val IconSize: Dp = 38.dp

  @Stable
  val RingWidth: Dp = 2.dp

  /** How far past the badge the ripple travels before it is gone. */
  const val RingMaxScale = 1.9f
  const val BadgeEnterScale = 0.74f
  const val PulseDurationMillis = 620
  const val IdleFadeMillis = 200
  const val IdleEnterScale = 0.85f

  /** Progress at which the ripple has fully faded; the glyph outlives it to settle into the badge. */
  const val RingFadeEnd = 0.85f
}

/**
 * What the screen shows about playback state when the chrome is away.
 *
 * Two things in one composable because they are one gesture to the viewer: pressing play or pause
 * fires a ripple that carries the glyph outward, and if the result is a paused player the same glyph
 * stays behind as the resting badge. Splitting them produced two circles animating over each other.
 *
 * @param isIdleBadgeVisible Whether the resting badge should be shown at all — the screen suppresses
 *   it while the controller or a side section is up, since those already say the player is paused.
 */
@Composable
internal fun PlayerPlaybackIndicator(
  isPlaying: Boolean,
  isIdleBadgeVisible: Boolean,
  modifier: Modifier = Modifier,
) {
  val pulse = remember { Animatable(1f) }
  // Sampled when the pulse starts rather than read live: a fast double-toggle would otherwise swap
  // the glyph under a ripple that is already halfway out.
  var pulsedIsPlaying by remember { mutableStateOf(isPlaying) }
  var hasSeenInitialState by remember { mutableStateOf(false) }

  LaunchedEffect(isPlaying) {
    if (!hasSeenInitialState) {
      hasSeenInitialState = true
      return@LaunchedEffect
    }
    pulsedIsPlaying = isPlaying
    pulse.snapTo(0f)
    pulse.animateTo(
      targetValue = 1f,
      animationSpec = tween(
        durationMillis = PlayerPlaybackIndicatorDefaults.PulseDurationMillis,
        easing = LinearOutSlowInEasing,
      ),
    )
  }

  Box(
    modifier = modifier.size(
      PlayerPlaybackIndicatorDefaults.BadgeSize * PlayerPlaybackIndicatorDefaults.RingMaxScale,
    ),
    contentAlignment = Alignment.Center,
  ) {
    val progress = pulse.value
    if (progress < 1f) {
      PlaybackPulseRing(progress = progress)
      PlayerPlaybackBadge(
        glyph = if (pulsedIsPlaying) PlayerPlaybackGlyph.Play else PlayerPlaybackGlyph.Pause,
        modifier = Modifier
          .scale(lerp(PlayerPlaybackIndicatorDefaults.BadgeEnterScale, 1f, progress))
          .alpha(1f - progress),
      )
    }

    AnimatedVisibility(
      visible = isIdleBadgeVisible,
      enter = fadeIn(animationSpec = tween(PlayerPlaybackIndicatorDefaults.IdleFadeMillis)) +
        scaleIn(
          animationSpec = tween(PlayerPlaybackIndicatorDefaults.IdleFadeMillis),
          initialScale = PlayerPlaybackIndicatorDefaults.IdleEnterScale,
        ),
      exit = fadeOut(animationSpec = tween(PlayerPlaybackIndicatorDefaults.IdleFadeMillis)) +
        scaleOut(
          animationSpec = tween(PlayerPlaybackIndicatorDefaults.IdleFadeMillis),
          targetScale = PlayerPlaybackIndicatorDefaults.IdleEnterScale,
        ),
    ) {
      PlayerPlaybackBadge(glyph = PlayerPlaybackGlyph.Play)
    }
  }
}

@Composable
private fun PlaybackPulseRing(progress: Float, modifier: Modifier = Modifier) {
  val eased = FastOutSlowInEasing.transform(progress)
  val ringAlpha = 1f - (progress / PlayerPlaybackIndicatorDefaults.RingFadeEnd).coerceAtMost(1f)

  Box(
    modifier = modifier
      .size(PlayerPlaybackIndicatorDefaults.BadgeSize)
      .scale(lerp(1f, PlayerPlaybackIndicatorDefaults.RingMaxScale, eased))
      .alpha(ringAlpha)
      .border(
        width = PlayerPlaybackIndicatorDefaults.RingWidth,
        color = StreamTvColors.NeutralWhite,
        shape = CircleShape,
      ),
  )
}

/**
 * Which glyph a badge carries.
 *
 * Named after the glyph rather than the playback state because the two badges disagree about what
 * "playing" should draw: the ripple reports the state just entered, while the resting badge is an
 * invitation to resume. One boolean could not serve both without one of them being backwards.
 */
internal enum class PlayerPlaybackGlyph {
  Play,
  Pause,
}

/** The circular play/pause glyph. */
@Composable
internal fun PlayerPlaybackBadge(glyph: PlayerPlaybackGlyph, modifier: Modifier = Modifier) {
  val iconResId = when (glyph) {
    PlayerPlaybackGlyph.Play -> R.drawable.ic_play
    PlayerPlaybackGlyph.Pause -> R.drawable.ic_pause
  }
  val contentDescriptionResId = when (glyph) {
    PlayerPlaybackGlyph.Play -> R.string.player_play
    PlayerPlaybackGlyph.Pause -> R.string.player_pause
  }
  val icon: ImageVector = ImageVector.vectorResource(iconResId)
  val contentDescription = stringResource(contentDescriptionResId)

  Box(
    modifier = modifier
      .size(PlayerPlaybackIndicatorDefaults.BadgeSize)
      .clip(CircleShape)
      .background(StreamTvColors.TransparentBlack60)
      .testTag("player-playback-badge"),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      modifier = Modifier.size(PlayerPlaybackIndicatorDefaults.IconSize),
      tint = StreamTvColors.NeutralWhite,
    )
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF102838)
@Composable
private fun PlayerPlaybackIndicatorPausedPreview() {
  StreamTvTheme {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      PlayerPlaybackIndicator(isPlaying = false, isIdleBadgeVisible = true)
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF102838)
@Composable
private fun PlayerPlaybackPulseMidwayPreview() {
  StreamTvTheme {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Box(
        modifier = Modifier.size(
          PlayerPlaybackIndicatorDefaults.BadgeSize * PlayerPlaybackIndicatorDefaults.RingMaxScale,
        ),
        contentAlignment = Alignment.Center,
      ) {
        PlaybackPulseRing(progress = 0.45f)
        PlayerPlaybackBadge(glyph = PlayerPlaybackGlyph.Pause)
      }
    }
  }
}
