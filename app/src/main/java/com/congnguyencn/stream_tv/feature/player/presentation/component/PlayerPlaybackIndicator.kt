package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
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

private object PlayerPlaybackIndicatorDefaults {
  @Stable
  val BadgeSize: Dp = 82.dp

  @Stable
  val IconSize: Dp = 34.dp

  @Stable
  val RingWidth: Dp = 1.5.dp

  const val EffectDurationMillis = 560
  const val RingMaxScale = 1.72f
  const val BadgeInitialScale = 0.78f
  const val BadgeSettleScale = 1.02f
  const val BadgeExitScale = 1.24f
  const val EnterEnd = 0.2f
  const val ExitStart = 0.38f
}

/**
 * A playback effect requested by a user interaction.
 *
 * [sequence] deliberately separates this effect from player state. Media callbacks, autoplay and
 * buffering can update `isPlaying` without creating a new sequence and therefore never flash the
 * center glyph.
 */
@Immutable
internal data class PlayerPlaybackEffect(val sequence: Int, val glyph: PlayerPlaybackGlyph)

/** Shows one short play/pause acknowledgement for an explicit user click. */
@Composable
internal fun PlayerPlaybackIndicator(effect: PlayerPlaybackEffect?, modifier: Modifier = Modifier) {
  val progress = remember { Animatable(1f) }

  LaunchedEffect(effect?.sequence) {
    if (effect == null) return@LaunchedEffect
    progress.snapTo(0f)
    progress.animateTo(
      targetValue = 1f,
      animationSpec = tween(
        durationMillis = PlayerPlaybackIndicatorDefaults.EffectDurationMillis,
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
    if (effect != null && progress.value < 1f) {
      PlayerPlaybackEffectContent(
        glyph = effect.glyph,
        progress = progress.value,
      )
    }
  }
}

@Composable
private fun PlayerPlaybackEffectContent(glyph: PlayerPlaybackGlyph, progress: Float, modifier: Modifier = Modifier) {
  val enterProgress = (progress / PlayerPlaybackIndicatorDefaults.EnterEnd).coerceIn(0f, 1f)
  val exitProgress = (
    (progress - PlayerPlaybackIndicatorDefaults.ExitStart) /
      (1f - PlayerPlaybackIndicatorDefaults.ExitStart)
    ).coerceIn(0f, 1f)
  val enterEased = FastOutSlowInEasing.transform(enterProgress)
  val exitEased = FastOutSlowInEasing.transform(exitProgress)
  val badgeScale = if (progress <= PlayerPlaybackIndicatorDefaults.EnterEnd) {
    lerp(
      PlayerPlaybackIndicatorDefaults.BadgeInitialScale,
      PlayerPlaybackIndicatorDefaults.BadgeSettleScale,
      enterEased,
    )
  } else {
    lerp(
      PlayerPlaybackIndicatorDefaults.BadgeSettleScale,
      PlayerPlaybackIndicatorDefaults.BadgeExitScale,
      exitEased,
    )
  }
  val badgeAlpha = enterEased * (1f - exitEased)
  val ringProgress = ((progress - 0.08f) / 0.82f).coerceIn(0f, 1f)

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Box(
      modifier = Modifier
        .size(PlayerPlaybackIndicatorDefaults.BadgeSize)
        .scale(
          lerp(
            0.96f,
            PlayerPlaybackIndicatorDefaults.RingMaxScale,
            FastOutSlowInEasing.transform(ringProgress),
          ),
        )
        .alpha((1f - ringProgress) * badgeAlpha)
        .border(
          width = PlayerPlaybackIndicatorDefaults.RingWidth,
          color = StreamTvColors.NeutralWhite,
          shape = CircleShape,
        ),
    )
    PlayerPlaybackBadge(
      glyph = glyph,
      modifier = Modifier
        .scale(badgeScale)
        .alpha(badgeAlpha),
    )
  }
}

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

  Box(
    modifier = modifier
      .size(PlayerPlaybackIndicatorDefaults.BadgeSize)
      .clip(CircleShape)
      .background(StreamTvColors.TransparentBlack60)
      .testTag("player-playback-badge"),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = ImageVector.vectorResource(iconResId),
      contentDescription = stringResource(contentDescriptionResId),
      modifier = Modifier.size(PlayerPlaybackIndicatorDefaults.IconSize),
      tint = StreamTvColors.NeutralWhite,
    )
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF102838)
@Composable
private fun PlayerPlaybackPlayEffectPreview() {
  StreamTvTheme {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Box(
        modifier = Modifier.size(
          PlayerPlaybackIndicatorDefaults.BadgeSize * PlayerPlaybackIndicatorDefaults.RingMaxScale,
        ),
        contentAlignment = Alignment.Center,
      ) {
        PlayerPlaybackEffectContent(glyph = PlayerPlaybackGlyph.Play, progress = 0.18f)
      }
    }
  }
}

@Preview(device = Devices.TV_1080p, showBackground = true, backgroundColor = 0xFF102838)
@Composable
private fun PlayerPlaybackPauseEffectPreview() {
  StreamTvTheme {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Box(
        modifier = Modifier.size(
          PlayerPlaybackIndicatorDefaults.BadgeSize * PlayerPlaybackIndicatorDefaults.RingMaxScale,
        ),
        contentAlignment = Alignment.Center,
      ) {
        PlayerPlaybackEffectContent(glyph = PlayerPlaybackGlyph.Pause, progress = 0.48f)
      }
    }
  }
}
