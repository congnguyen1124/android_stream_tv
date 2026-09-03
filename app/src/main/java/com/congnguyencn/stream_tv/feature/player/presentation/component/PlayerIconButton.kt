package com.congnguyencn.stream_tv.feature.player.presentation.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.Surface
import com.congnguyencn.stream_tv.R
import com.congnguyencn.stream_tv.core.designsystem.theme.StreamTvColors

internal object PlayerIconButtonDefaults {
  @Stable
  val Size: Dp = 44.dp

  @Stable
  val IconSize: Dp = 22.dp

  /** The transport primary. Larger so the eye finds pause without hunting. */
  @Stable
  val PrimarySize: Dp = 60.dp

  @Stable
  val PrimaryIconSize: Dp = 26.dp
}

/**
 * A circular player control.
 *
 * Focus is signalled by inverting the fill — white circle, dark glyph — rather than by scaling.
 * Scaling a control that sits on a shared pill background pushes it out of the pill; inverting keeps
 * the row's geometry fixed while still reading unmistakably at ten feet.
 *
 * @param isFocused Whether the caller observed focus on this button. The fill is derived from this
 *   rather than from `Surface`'s own focused state: after a `FocusRequester` moved focus elsewhere,
 *   `Surface` kept reporting itself focused and the button held its white fill while another control
 *   actually had the D-pad.
 *
 * @param containerColor Idle fill. Pass [Color.Transparent] for a button that sits on a cluster's
 *   shared pill, so only the pill is drawn until the button takes focus.
 */
@Composable
internal fun PlayerIconButton(
  @DrawableRes iconResId: Int,
  contentDescription: String,
  onClick: () -> Unit,
  isFocused: Boolean,
  modifier: Modifier = Modifier,
  iconSize: Dp = PlayerIconButtonDefaults.IconSize,
  containerColor: Color = StreamTvColors.TransparentWhite10,
  content: (@Composable BoxScope.() -> Unit)? = null,
) {
  val resolvedContainerColor = if (isFocused) StreamTvColors.NeutralWhite else containerColor
  val resolvedContentColor = if (isFocused) StreamTvColors.NeutralBlack else StreamTvColors.NeutralWhite

  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
    scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = resolvedContainerColor,
      contentColor = resolvedContentColor,
      focusedContainerColor = resolvedContainerColor,
      focusedContentColor = resolvedContentColor,
      pressedContainerColor = StreamTvColors.Primary30,
      pressedContentColor = StreamTvColors.NeutralBlack,
    ),
  ) {
    if (content != null) {
      content()
    } else {
      Icon(
        imageVector = ImageVector.vectorResource(iconResId),
        contentDescription = contentDescription,
        modifier = Modifier
          .align(Alignment.Center)
          .size(iconSize),
        tint = LocalContentColor.current,
      )
    }
  }
}

/**
 * A static paused glyph, for a surface with no control row of its own.
 *
 * The portrait player has no transport controls, so a paused short would otherwise look identical to
 * a stalled one. This is state, not an acknowledgement animation: it is on screen for exactly as
 * long as playback is paused, and it is not focusable — the surface behind it owns the click.
 */
@Composable
internal fun PlayerPausedBadge(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .size(PlayerIconButtonDefaults.PrimarySize)
      .background(StreamTvColors.TransparentBlack60, CircleShape)
      .testTag("player-paused-badge"),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = ImageVector.vectorResource(R.drawable.ic_play),
      contentDescription = stringResource(R.string.player_play),
      modifier = Modifier.size(PlayerIconButtonDefaults.PrimaryIconSize),
      tint = StreamTvColors.NeutralWhite,
    )
  }
}
